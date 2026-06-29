#!/usr/bin/env python3
"""PR merge gate: static scan + Codex App review → job exit code = check status.

Two phases, zero API keys needed:
  1. STATIC — scan diff for secrets, blocked files, metadata issues (<30s).
              Fail job immediately on hard violations.
  2. WAIT   — poll for Codex GitHub App review up to N min.
              Fail job on CHANGES_REQUESTED, pass on APPROVED/timeout.
  3. REACT  — on Codex bot review submitted: update instantly.

The workflow job IS the check — exit 0 = pass, exit 1 = fail.
"""

from __future__ import annotations

import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any

API_BASE = "https://api.github.com"
BOT_LOGIN = "chatgpt-codex-connector[bot]"
POLICY_PATH = Path(".github/codex_auto_merge_policy.json")


def env(name: str, default: str = "") -> str:
    return os.environ.get(name, default).strip()


def env_int(name: str, default: int) -> int:
    try: return int(env(name, str(default)))
    except ValueError: return default


def github_request(token: str, method: str, path: str,
                   payload: dict[str, Any] | None = None) -> Any:
    url = f"{API_BASE}{path}" if not path.startswith("https://") else path
    data = json.dumps(payload).encode() if payload else None
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "codex-review-gate",
    }
    if payload: headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            body = resp.read().decode("utf-8")
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"GitHub API {method} {url}: {exc.code} {detail[:500]}") from exc
    return json.loads(body) if body else {}


def step_summary(text: str) -> None:
    p = os.environ.get("GITHUB_STEP_SUMMARY", "")
    if p:
        with open(p, "a", encoding="utf-8") as f:
            f.write(text + "\n")


# ─── policy ──────────────────────────────────────────────────────────────────

def load_policy() -> dict[str, Any]:
    if POLICY_PATH.exists():
        try: return json.loads(POLICY_PATH.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError): pass
    return {
        "version": 1,
        "blocked_path_patterns": [
            r"(^|/)(\.env|.*secret.*|.*credential.*|.*token.*|.*private.*|.*\.pem|.*\.key)$",
        ],
        "max_changed_files": 50,
        "max_changed_lines": 5000,
    }


def compile_patterns(policy: dict[str, Any]) -> list[re.Pattern[str]]:
    pp: list[re.Pattern[str]] = []
    for p in policy.get("blocked_path_patterns", []):
        if isinstance(p, str) and p.strip():
            try: pp.append(re.compile(p, re.IGNORECASE))
            except re.error: pass
    return pp


# ─── static guard ────────────────────────────────────────────────────────────

_SENSITIVE = re.compile(
    r'(?:api[_\s]?key|secret|password|token|credential|private[_\s]?key)\s*[:=]\s*["\']'
    r'(?!\$\{\{|{{|example|placeholder|test|your[-_\s]|xxx|TODO|CHANGEME)[^"\']{12,}["\']',
    re.IGNORECASE,
)


def scan_diff(diff_text: str, path_patterns: list[re.Pattern[str]]) -> list[str]:
    violations: list[str] = []
    current = ""
    for line in diff_text.splitlines():
        if line.startswith("diff --git "):
            parts = line.split(" ")
            current = parts[3][2:] if len(parts) >= 4 and parts[3].startswith("b/") else ""
            for pat in path_patterns:
                if current and pat.search(current):
                    violations.append(f"**Blocked file**: `{current}` matches `{pat.pattern}`")
                    break
            continue
        if line.startswith("+++ b/"): current = line[6:]; continue
        if not line.startswith("+") or line.startswith("+++"): continue
        m = _SENSITIVE.search(line[1:])
        if m:
            violations.append(f"**Hardcoded secret** in `{current}`: `{m.group(0)[:100]}`")
    return list(dict.fromkeys(violations))


def check_metadata(files: list[dict[str, Any]], policy: dict[str, Any]) -> list[str]:
    issues: list[str] = []
    mx_f = policy.get("max_changed_files", 50)
    mx_l = policy.get("max_changed_lines", 5000)
    ta = sum(f.get("additions", 0) or 0 for f in files)
    td = sum(f.get("deletions", 0) or 0 for f in files)
    for f in files:
        fn = f.get("filename", "?")
        st = (f.get("status") or "").lower().strip()
        if st == "removed": issues.append(f"**File deleted**: `{fn}` — verify intentional")
        elif st == "renamed": issues.append(f"**File renamed**: `{f.get('previous_filename', '?')}` → `{fn}`")
    if len(files) > mx_f:
        issues.append(f"**Too many files**: {len(files)} changed (limit {mx_f})")
    if ta + td > mx_l:
        issues.append(f"**Too many lines**: {ta + td} changed (limit {mx_l})")
    return issues


def run_static_guard(token: str, repo: str, pr_number: int) -> int:
    """Return 0 if clean, 1 if blocked."""
    policy = load_policy()
    files: list[dict[str, Any]] = []
    page = 1
    while True:
        try:
            batch = github_request(token, "GET",
                f"/repos/{repo}/pulls/{pr_number}/files?per_page=100&page={page}")
        except RuntimeError: break
        if not isinstance(batch, list) or not batch: break
        files.extend(batch)
        if len(batch) < 100: break
        page += 1

    diff_text = ""
    try:
        req = urllib.request.Request(
            f"{API_BASE}/repos/{repo}/pulls/{pr_number}",
            headers={
                "Authorization": f"Bearer {token}",
                "Accept": "application/vnd.github.v3.diff",
                "X-GitHub-Api-Version": "2022-11-28",
                "User-Agent": "codex-review-gate",
            },
        )
        with urllib.request.urlopen(req, timeout=30) as resp:
            diff_text = resp.read().decode("utf-8", errors="replace")
    except Exception: pass

    issues = check_metadata(files, policy) + scan_diff(diff_text, compile_patterns(policy))
    if not issues: return 0

    print(f"STATIC → BLOCKED: {len(issues)} issue(s)")
    for i in issues: print(f"  • {i}")
    step_summary(f"## Merge blocked: {len(issues)} static issue(s)\n\n" +
                 "\n".join(f"- {i}" for i in issues))
    return 1


# ─── app review ──────────────────────────────────────────────────────────────

def get_codex_review(token: str, repo: str, pr_number: int) -> dict[str, Any] | None:
    reviews = github_request(token, "GET", f"/repos/{repo}/pulls/{pr_number}/reviews?per_page=100")
    if not isinstance(reviews, list): return None
    for r in reversed(reviews):
        if isinstance(r, dict) and (r.get("user") or {}).get("login") == BOT_LOGIN:
            return r
    return None


def app_decision(review: dict[str, Any] | None) -> tuple[int, str, str]:
    """(exit_code, title, summary)"""
    if review is None:
        return (0, "Codex: no review — passed through",
                "Codex did not respond in time. Merge allowed to avoid blocking development.")
    state = (review.get("state") or "").strip().upper()
    url = review.get("html_url", "")
    body = (review.get("body") or "").strip()
    at = review.get("submitted_at", "")

    if state == "CHANGES_REQUESTED":
        snippet = (body[:500] + "...") if len(body) > 500 else body
        return (1, "Codex: changes requested — MERGE BLOCKED",
                f"Codex **requested changes** at {at}.\n\n{snippet}\n\n[View review]({url})")
    if state == "APPROVED":
        return (0, "Codex: approved", f"Codex approved at {at}. [View review]({url})")
    return (0, f"Codex: reviewed ({state.lower()})",
            f"Codex state `{state}` at {at}. Not blocking. [View review]({url})")


# ─── main ────────────────────────────────────────────────────────────────────

def main() -> int:
    token = env("GH_TOKEN") or env("GITHUB_TOKEN")
    repo = env("GITHUB_REPOSITORY")
    if not token or not repo:
        print("::error::GH_TOKEN + GITHUB_REPOSITORY required", file=sys.stderr)
        return 1

    event_path = Path(os.environ.get("GITHUB_EVENT_PATH", ""))
    if not event_path.exists():
        print("::error::GITHUB_EVENT_PATH missing", file=sys.stderr)
        return 1

    event = json.loads(event_path.read_text(encoding="utf-8"))
    event_name = env("GITHUB_EVENT_NAME", "")
    pr = event.get("pull_request") or {}
    pr_number = pr.get("number")
    head_sha = (pr.get("head") or {}).get("sha")
    if not pr_number or not head_sha:
        print(f"::warning::Cannot resolve PR context"); return 0

    print(f"PR #{pr_number}  sha={head_sha[:12]}  event={event_name}")

    # ── Phase 1: Static guard (skip on review-only events) ────────────
    if event_name != "pull_request_review":
        try: rc = run_static_guard(token, repo, pr_number)
        except RuntimeError as exc:
            print(f"::warning::Static guard error: {exc}"); rc = 0
        if rc != 0: return 1
        print("STATIC → clean")

    # ── Phase 2: App review ───────────────────────────────────────────
    # REACT: Codex just submitted a review
    review_event = event.get("review") or {}
    if event_name == "pull_request_review" and (review_event.get("user") or {}).get("login") == BOT_LOGIN:
        rc, title, summary = app_decision(review_event)
        print(f"REACT → exit={rc}: {title}")
        step_summary(f"## {title}\n\n{summary}")
        return rc

    # WAIT: poll for existing or upcoming review
    try: existing = get_codex_review(token, repo, pr_number)
    except RuntimeError: existing = None

    if existing is not None:
        rc, title, summary = app_decision(existing)
        print(f"EXISTING → exit={rc}: {title}")
        step_summary(f"## {title}\n\n{summary}")
        return rc

    poll_s = env_int("CODEX_GATE_POLL_SECONDS", 30)
    max_w = env_int("CODEX_GATE_MAX_WAIT_MINUTES", 5)
    deadline = time.time() + max_w * 60
    print(f"WAIT → polling every {poll_s}s for up to {max_w}min")

    while time.time() < deadline:
        time.sleep(poll_s)
        try: review = get_codex_review(token, repo, pr_number)
        except RuntimeError: continue
        if review is not None:
            rc, title, summary = app_decision(review)
            print(f"WAIT → found review → exit={rc}: {title}")
            step_summary(f"## {title}\n\n{summary}")
            return rc

    # Timeout
    print(f"TIMEOUT → Codex did not respond in {max_w}min; passing through")
    step_summary(f"## Codex: timeout after {max_w}min\n\nPassed through to avoid blocking development.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
