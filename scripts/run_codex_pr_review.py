#!/usr/bin/env python3
"""Run Codex review on a PR diff and block merge when serious issues are found.

Uses the existing Codex audit service backend (same as monthly reviews).
Evaluates findings against the repo's codex_auto_merge_policy.json.
Exits non-zero when blocked, which fails the GitHub Actions check run.
"""

from __future__ import annotations

import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from string import Template
from typing import Any

# ---------------------------------------------------------------------------
# Configuration (aligned with CodexAuditBridge)
# ---------------------------------------------------------------------------

API_BASE = "https://api.github.com"
ROOT = Path(__file__).resolve().parents[1]
POLICY_PATH = ROOT / ".github" / "codex_auto_merge_policy.json"
PROMPT_TEMPLATE_PATH = ROOT / "prompts" / "pr_review.md"
DEFAULT_SERVICE_AUDIENCE = "quant-codex-audit"
DEFAULT_TIMEOUT_MINUTES = 20
DEFAULT_MAX_CONTEXT_LINES = 800

# Risk → block mapping
BLOCK_SEVERITIES = frozenset({"critical", "high"})
COMMENT_SEVERITIES = frozenset({"critical", "high", "medium", "low"})


class ReviewError(RuntimeError):
    pass


# ---------------------------------------------------------------------------
# GitHub API helpers
# ---------------------------------------------------------------------------


def github_request(
    token: str, method: str, path: str, payload: dict[str, Any] | None = None
) -> Any:
    url = path if path.startswith("https://") else f"{API_BASE}{path}"
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    req = urllib.request.Request(
        url,
        data=data,
        method=method,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "codex-pr-review",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            body = resp.read().decode("utf-8")
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise ReviewError(f"GitHub API {method} {url} failed: {exc.code} {body[:600]}") from exc
    return json.loads(body) if body else {}


def env_value(name: str, default: str = "") -> str:
    return os.environ.get(name, default).strip()


def parse_bool(value: str | bool | None) -> bool:
    if isinstance(value, bool):
        return value
    return str(value or "").strip().lower() in {"1", "true", "yes", "y", "on"}


# ---------------------------------------------------------------------------
# Policy loading (reuses evaluate_codex_pr_merge.py logic)
# ---------------------------------------------------------------------------


def load_policy() -> dict[str, Any]:
    """Load the risk policy, falling back to safe defaults."""
    if not POLICY_PATH.exists():
        return _default_policy()

    try:
        payload = json.loads(POLICY_PATH.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return _fail_closed("invalid auto-merge policy JSON")

    if not isinstance(payload, dict):
        return _fail_closed("invalid auto-merge policy format")

    if payload.get("version") != 1:
        return _fail_closed("unsupported policy version")

    return payload


def _default_policy() -> dict[str, Any]:
    return {
        "version": 1,
        "blocked_path_patterns": [
            r"(^|/)(\.env|.*secret.*|.*credential.*|.*token.*|.*private.*|.*\.pem|.*\.key)$",
        ],
        "risk_policy": {
            "low": {
                "prefixes": ["docs/", "tests/"],
                "exact": ["README.md", "README.zh-CN.md"],
                "reason": "docs/tests/readme-only changes",
            },
            "high": {"reason": "source code changes require review"},
        },
        "max_changed_files": 30,
        "max_changed_lines": 2000,
    }


def _fail_closed(reason: str) -> dict[str, Any]:
    return {
        "policy_errors": [reason],
        "blocked_path_patterns": [r".*"],
        "risk_policy": {
            "low": {"prefixes": [], "exact": [], "reason": reason},
            "high": {"reason": reason},
        },
    }


# ---------------------------------------------------------------------------
# File risk classification
# ---------------------------------------------------------------------------


def classify_file_risk(
    file_path: str, policy: dict[str, Any]
) -> tuple[str, str]:
    """Return (risk_level, reason) for a single file path."""
    policy_errors = policy.get("policy_errors", [])

    # Blocked patterns (secrets, credentials, etc.)
    blocked_patterns = policy.get("blocked_path_patterns", [])
    for pattern in blocked_patterns:
        try:
            if re.search(pattern, file_path, re.IGNORECASE):
                return ("high", f"matches blocked path pattern: {pattern}")
        except re.error:
            continue

    risk_policy = policy.get("risk_policy", {})
    low = risk_policy.get("low", {})
    low_prefixes = low.get("prefixes", [])
    low_exact = set(low.get("exact", []))
    medium_exact = set(
        risk_policy.get("medium", {}).get("exact", [])
    )

    # Normalize path
    normalized = file_path.strip()
    while normalized.startswith("./"):
        normalized = normalized[2:]

    if not normalized:
        return ("high", "empty path")

    if normalized in low_exact or any(
        normalized.startswith(prefix) for prefix in low_prefixes
    ):
        return ("low", "docs/test/readme change")

    if normalized in medium_exact:
        return ("medium", "monthly-review helper changed")

    return ("high", "source code change")


# ---------------------------------------------------------------------------
# PR diff fetching
# ---------------------------------------------------------------------------


def fetch_pr_diff(token: str, repo: str, pr_number: int) -> str:
    """Fetch the unified diff for a PR."""
    diff_url = f"{API_BASE}/repos/{repo}/pulls/{pr_number}"
    req = urllib.request.Request(
        diff_url,
        method="GET",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github.v3.diff",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "codex-pr-review",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return resp.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise ReviewError(f"Failed to fetch PR diff: {exc.code} {body[:600]}") from exc


def fetch_pr_files(token: str, repo: str, pr_number: int) -> list[dict[str, Any]]:
    """Fetch the list of changed files in a PR."""
    files: list[dict[str, Any]] = []
    page = 1
    while True:
        payload = github_request(
            token,
            "GET",
            f"/repos/{repo}/pulls/{pr_number}/files?per_page=100&page={page}",
        )
        if not isinstance(payload, list) or not payload:
            break
        files.extend(payload)
        if len(payload) < 100:
            break
        page += 1
    return files


# ---------------------------------------------------------------------------
# Review prompt
# ---------------------------------------------------------------------------


def build_review_prompt(diff: str, pr_title: str, pr_body: str, repo: str) -> str:
    """Build the Codex review prompt with the PR diff and structured output instructions."""
    diff_limited = _truncate_lines(diff, DEFAULT_MAX_CONTEXT_LINES * 3)

    template = Template(
        """You are reviewing a pull request for a production codebase. Your job is to find bugs, security issues, and logic errors that could cause real problems.

## PR Context

- Repository: ${REPO}
- PR Title: ${TITLE}

${BODY}

## Review Instructions

1. Focus on **security vulnerabilities, logic errors, data corruption, crash bugs, race conditions, and API compatibility breaks**.
2. Do NOT flag: code style, formatting, naming suggestions, minor refactoring preferences, or documentation issues.
3. For each finding, classify its severity:
   - **critical**: security vulnerability, data loss, production crash
   - **high**: logic error that produces wrong results, API break, memory/connection leak
   - **medium**: missing error handling, performance degradation, race condition
   - **low**: misleading comment, unclear variable name, redundant code

## Output Format

Return exactly one JSON object and no surrounding prose:

```json
{
  "summary": "Brief summary of the review (1-3 sentences)",
  "findings": [
    {
      "severity": "critical|high|medium|low",
      "category": "security|bug|performance|logic|reliability",
      "file": "relative/path/to/file.py",
      "line": 42,
      "description": "What the problem is",
      "suggestion": "How to fix it"
    }
  ]
}
```

If there are no findings, return an empty `findings` array.

## PR Diff

```diff
${DIFF}
```"""
    )

    return template.safe_substitute(
        REPO=repo,
        TITLE=pr_title,
        BODY=f"### PR Description\n\n{pr_body}" if pr_body.strip() else "",
        DIFF=diff_limited,
    )


def _truncate_lines(text: str, max_lines: int) -> str:
    lines = text.splitlines()
    if len(lines) <= max_lines:
        return text
    half = max_lines // 2
    return (
        "\n".join(lines[:half])
        + f"\n\n... [{len(lines) - max_lines} lines truncated] ...\n\n"
        + "\n".join(lines[-half:])
    )


# ---------------------------------------------------------------------------
# Codex service integration
# ---------------------------------------------------------------------------


def request_github_oidc_token(audience: str) -> str:
    request_url = env_value("ACTIONS_ID_TOKEN_REQUEST_URL")
    request_token = env_value("ACTIONS_ID_TOKEN_REQUEST_TOKEN")
    if not request_url or not request_token:
        raise ReviewError(
            "GitHub OIDC environment unavailable. Set permissions: id-token: write."
        )
    separator = "&" if "?" in request_url else "?"
    url = f"{request_url}{separator}audience={urllib.parse.quote(audience)}"
    req = urllib.request.Request(
        url,
        method="GET",
        headers={
            "Authorization": f"Bearer {request_token}",
            "Accept": "application/json",
            "User-Agent": "codex-pr-review-oidc",
        },
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        payload = json.loads(resp.read().decode("utf-8"))
    token = payload.get("value") if isinstance(payload, dict) else None
    if not isinstance(token, str) or not token:
        raise ReviewError("GitHub OIDC token response missing token value")
    return token


def run_codex_service_review(prompt: str, timeout_minutes: int) -> str:
    """Submit a review job to the Codex audit service and wait for completion."""
    service_url = env_value("CODEX_AUDIT_SERVICE_URL")
    if not service_url:
        raise ReviewError("CODEX_AUDIT_SERVICE_URL is not configured")

    service_url = service_url.strip().rstrip("/")
    audience = env_value("CODEX_AUDIT_SERVICE_AUDIENCE", DEFAULT_SERVICE_AUDIENCE)

    # Submit job
    oidc_token = request_github_oidc_token(audience)
    payload = {
        "source_repository": env_value("GITHUB_REPOSITORY"),
        "source_ref": env_value("GITHUB_REF_NAME", "main"),
        "task": "pr_review",
        "mode": "review_only",
        "prompt": prompt,
        "timeout_seconds": timeout_minutes * 60,
    }
    submit_resp = _service_request(
        "POST",
        f"{service_url}/v1/codex-audit/jobs",
        oidc_token,
        payload,
    )
    job_id = submit_resp.get("job_id")
    if not isinstance(job_id, str) or not job_id:
        raise ReviewError("Codex service did not return a job id")

    # Poll for completion
    deadline = time.time() + timeout_minutes * 60 + 120
    poll_interval = 5
    job_url = f"{service_url}/v1/codex-audit/jobs/{job_id}"
    while time.time() < deadline:
        time.sleep(poll_interval)
        poll_interval = min(poll_interval * 2, 30)
        job_payload = _service_request("GET", job_url, oidc_token, None)
        status = job_payload.get("status")
        if status == "succeeded":
            output = job_payload.get("output")
            if not isinstance(output, str):
                raise ReviewError("Codex service response missing text output")
            return output.strip()
        if status == "failed":
            error = str(job_payload.get("error") or "unknown failure")
            raise ReviewError(f"Codex service job failed: {error[:600]}")
        if status not in {"queued", "running"}:
            raise ReviewError(f"Unexpected Codex service status: {status!r}")

    raise ReviewError("Codex service job timed out")


def _service_request(
    method: str, url: str, oidc_token: str, payload: dict[str, Any] | None
) -> dict[str, Any]:
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    req = urllib.request.Request(
        url,
        data=data,
        method=method,
        headers={
            "Authorization": f"Bearer {oidc_token}",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "User-Agent": "codex-pr-review-client",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            body = resp.read().decode("utf-8")
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise ReviewError(f"Codex service request failed: {exc.code} {detail[:600]}") from exc
    result = json.loads(body)
    if not isinstance(result, dict):
        raise ReviewError("Codex service returned invalid JSON")
    return result


# ---------------------------------------------------------------------------
# Direct API review (fallback when service is unavailable)
# ---------------------------------------------------------------------------


def run_direct_api_review(prompt: str) -> str:
    """Run review directly via Anthropic or OpenAI API."""
    anthropic_key = env_value("ANTHROPIC_API_KEY")
    if anthropic_key:
        return _run_anthropic_review(prompt, anthropic_key)

    openai_key = env_value("OPENAI_API_KEY")
    if openai_key:
        return _run_openai_review(prompt, openai_key)

    raise ReviewError(
        "No Codex service URL or API key configured. "
        "Set CODEX_AUDIT_SERVICE_URL, ANTHROPIC_API_KEY, or OPENAI_API_KEY."
    )


def _run_anthropic_review(prompt: str, api_key: str) -> str:
    model = env_value("ANTHROPIC_MODEL", "claude-sonnet-4-6")
    system = "You are a careful code reviewer. Return only the JSON object as specified."
    payload = {
        "model": model,
        "max_tokens": 4000,
        "system": system,
        "messages": [{"role": "user", "content": prompt}],
    }
    req = urllib.request.Request(
        "https://api.anthropic.com/v1/messages",
        data=json.dumps(payload).encode("utf-8"),
        method="POST",
        headers={
            "x-api-key": api_key,
            "anthropic-version": "2023-06-01",
            "Content-Type": "application/json",
            "User-Agent": "codex-pr-review",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            body = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise ReviewError(f"Anthropic API failed: {exc.code} {detail[:600]}") from exc

    content = body.get("content", [])
    if not isinstance(content, list):
        raise ReviewError("Unexpected Anthropic response format")
    text_parts = [
        str(block.get("text", ""))
        for block in content
        if isinstance(block, dict) and block.get("type") == "text"
    ]
    return "\n\n".join(text_parts)


def _run_openai_review(prompt: str, api_key: str) -> str:
    model = env_value("OPENAI_MODEL", "gpt-5.4-mini")
    system = "You are a careful code reviewer. Return only the JSON object as specified."
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": system},
            {"role": "user", "content": prompt},
        ],
    }
    req = urllib.request.Request(
        f"{env_value('OPENAI_API_BASE_URL', 'https://api.openai.com/v1').rstrip('/')}/chat/completions",
        data=json.dumps(payload).encode("utf-8"),
        method="POST",
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "User-Agent": "codex-pr-review",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            body = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise ReviewError(f"OpenAI API failed: {exc.code} {detail[:600]}") from exc

    choices = body.get("choices", [])
    if not isinstance(choices, list) or not choices:
        raise ReviewError("Unexpected OpenAI response format")
    message = choices[0].get("message", {})
    return str(message.get("content", ""))


# ---------------------------------------------------------------------------
# Response parsing
# ---------------------------------------------------------------------------


def parse_review_output(text: str) -> dict[str, Any]:
    """Extract the JSON review result from Codex/API output."""
    stripped = text.strip()

    # Try to extract from markdown code fence
    fence_match = re.fullmatch(
        r"```(?:json)?\s*(.*?)\s*```", stripped, flags=re.DOTALL | re.IGNORECASE
    )
    if fence_match:
        stripped = fence_match.group(1).strip()

    # Try to find JSON object boundaries
    if not stripped.startswith("{"):
        start = stripped.find("{")
        end = stripped.rfind("}")
        if start >= 0 and end > start:
            stripped = stripped[start : end + 1]

    try:
        payload = json.loads(stripped)
    except json.JSONDecodeError:
        raise ReviewError(f"Failed to parse Codex review output as JSON: {stripped[:500]}")

    if not isinstance(payload, dict):
        raise ReviewError("Review output is not a JSON object")

    return payload


# ---------------------------------------------------------------------------
# Findings evaluation
# ---------------------------------------------------------------------------


def evaluate_findings(
    findings: list[dict[str, Any]],
    changed_files: list[dict[str, Any]],
    policy: dict[str, Any],
) -> dict[str, Any]:
    """Evaluate Codex findings against the risk policy.

    Returns a decision dict with:
    - blocked: whether merge should be blocked
    - blocking_findings: findings that cause blocking
    - non_blocking_findings: findings that are reported but don't block
    - risk_summary: human-readable summary
    """
    blocking: list[dict[str, Any]] = []
    non_blocking: list[dict[str, Any]] = []
    file_risk_cache: dict[str, tuple[str, str]] = {}

    # Build a set of changed file paths
    changed_paths: set[str] = set()
    file_statuses: dict[str, str] = {}
    for f in changed_files:
        path = f.get("filename", "").strip()
        if path:
            changed_paths.add(path)
            file_statuses[path] = f.get("status", "")

    for finding in findings:
        if not isinstance(finding, dict):
            continue

        severity = str(finding.get("severity", "")).strip().lower()
        file_path = str(finding.get("file", "")).strip()

        # Classify the file's risk level
        if file_path not in file_risk_cache:
            file_risk_cache[file_path] = classify_file_risk(file_path, policy)
        file_risk, file_risk_reason = file_risk_cache[file_path]

        # Determine if this finding should block
        should_block = (
            severity in BLOCK_SEVERITIES
            and file_risk == "high"
            and file_path in changed_paths  # only block on actually changed files
        )

        enriched = {
            **finding,
            "file_risk": file_risk,
            "file_risk_reason": file_risk_reason,
        }

        if should_block:
            blocking.append(enriched)
        else:
            non_blocking.append(enriched)

    blocked = len(blocking) > 0

    # Build summary
    all_findings = blocking + non_blocking
    summary_parts = []
    if blocked:
        summary_parts.append(
            f"🚫 **Merge blocked**: {len(blocking)} serious issue(s) found in high-risk files"
        )
    elif all_findings:
        total = len(all_findings)
        summary_parts.append(
            f"✅ **Merge allowed**: {total} finding(s) reported but none are blocking"
        )
    else:
        summary_parts.append("✅ **Merge allowed**: No issues found")

    return {
        "blocked": blocked,
        "blocking_findings": blocking,
        "non_blocking_findings": non_blocking,
        "total_findings": len(all_findings),
        "summary": "\n\n".join(summary_parts),
    }


# ---------------------------------------------------------------------------
# PR comment
# ---------------------------------------------------------------------------


def build_pr_comment(decision: dict[str, Any], pr_url: str) -> str:
    """Build a markdown comment to post on the PR."""
    lines = [
        "<!-- codex-pr-review -->",
        "## 🤖 Codex PR Review",
        "",
        decision["summary"],
        "",
    ]

    blocking = decision["blocking_findings"]
    if blocking:
        lines.extend([
            "### 🚫 Blocking Issues",
            "",
            "These issues must be fixed before this PR can be merged:",
            "",
        ])
        for i, f in enumerate(blocking, 1):
            lines.extend(_format_finding(i, f))

    non_blocking = decision["non_blocking_findings"]
    if non_blocking:
        lines.extend([
            "### ℹ️ Other Findings",
            "",
        ])
        for i, f in enumerate(non_blocking, 1):
            lines.extend(_format_finding(i, f))

    lines.extend([
        "---",
        f"*Review by Codex PR Review bot • [PR]({pr_url})*",
    ])

    return "\n".join(lines)


def _format_finding(index: int, finding: dict[str, Any]) -> list[str]:
    severity = finding.get("severity", "unknown")
    category = finding.get("category", "general")
    file_path = finding.get("file", "?")
    line = finding.get("line")
    description = finding.get("description", "No description")
    suggestion = finding.get("suggestion", "")

    emoji = {"critical": "🔴", "high": "🟠", "medium": "🟡", "low": "🔵"}.get(severity, "⚪")

    lines = [
        f"#### {index}. {emoji} [{severity.upper()}] {category.title()} in `{file_path}`",
        f"",
        f"> {description}",
    ]
    if line:
        lines[-1] += f" (line {line})"
    if suggestion:
        lines.extend(["", f"**Suggestion:** {suggestion}"])
    lines.append("")
    return lines


# ---------------------------------------------------------------------------
# Existing comment management
# ---------------------------------------------------------------------------


def find_existing_review_comment(
    token: str, repo: str, pr_number: int
) -> int | None:
    """Find an existing Codex review comment on the PR."""
    marker = "<!-- codex-pr-review -->"
    page = 1
    while True:
        comments = github_request(
            token,
            "GET",
            f"/repos/{repo}/issues/{pr_number}/comments?per_page=100&page={page}&sort=created&direction=desc",
        )
        if not isinstance(comments, list):
            break
        for comment in comments:
            if isinstance(comment, dict) and marker in str(comment.get("body", "")):
                return comment.get("id")
        if len(comments) < 100:
            break
        page += 1
    return None


def upsert_pr_comment(
    token: str, repo: str, pr_number: int, body: str
) -> None:
    """Create or update the Codex review comment on the PR."""
    existing_id = find_existing_review_comment(token, repo, pr_number)
    if existing_id:
        github_request(
            token,
            "PATCH",
            f"/repos/{repo}/issues/comments/{existing_id}",
            {"body": body},
        )
        print(f"Updated existing review comment #{existing_id}")
    else:
        github_request(
            token,
            "POST",
            f"/repos/{repo}/issues/{pr_number}/comments",
            {"body": body},
        )
        print("Posted new review comment")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------


def main() -> int:
    token = env_value("GH_TOKEN") or env_value("GITHUB_TOKEN")
    if not token:
        print("::error::GH_TOKEN or GITHUB_TOKEN is required", file=sys.stderr)
        return 1

    repo = env_value("GITHUB_REPOSITORY")
    if not repo:
        print("::error::GITHUB_REPOSITORY is not set", file=sys.stderr)
        return 1

    # Get PR context from the event
    event_path = Path(os.environ.get("GITHUB_EVENT_PATH", ""))
    if not event_path.exists():
        print("::error::GITHUB_EVENT_PATH not found", file=sys.stderr)
        return 1

    event = json.loads(event_path.read_text(encoding="utf-8"))
    pr = event.get("pull_request") or {}
    pr_number = pr.get("number")
    if not pr_number:
        print("::error::No pull request number in event", file=sys.stderr)
        return 1

    pr_title = str(pr.get("title", ""))
    pr_body = str(pr.get("body", ""))
    pr_url = str(pr.get("html_url", ""))

    print(f"Reviewing PR #{pr_number}: {pr_title}")

    # Fetch changed files for risk classification
    changed_files = fetch_pr_files(token, repo, pr_number)
    changed_paths = [f.get("filename", "") for f in changed_files]
    print(f"Changed files ({len(changed_paths)}): {', '.join(changed_paths[:10])}"
          + (f" and {len(changed_paths) - 10} more..." if len(changed_paths) > 10 else ""))

    # Load policy
    policy = load_policy()
    if policy.get("policy_errors"):
        print(f"::warning::Policy errors: {policy['policy_errors']}")

    # First pass: classify files. If all files are low-risk, skip review.
    all_low_risk = all(
        classify_file_risk(p, policy)[0] == "low" for p in changed_paths
    )
    if all_low_risk and changed_paths:
        print("All changed files are low-risk (docs/tests). Skipping Codex review.")
        decision = {
            "blocked": False,
            "blocking_findings": [],
            "non_blocking_findings": [],
            "total_findings": 0,
            "summary": "✅ **Merge allowed**: All changes are in docs/tests — Codex review skipped.",
        }
        upsert_pr_comment(token, repo, pr_number, build_pr_comment(decision, pr_url))
        return 0

    # Fetch PR diff
    diff = fetch_pr_diff(token, repo, pr_number)
    print(f"Fetched diff: {len(diff)} chars, {len(diff.splitlines())} lines")

    # Build review prompt
    prompt = build_review_prompt(diff, pr_title, pr_body, repo)
    print(f"Built review prompt: {len(prompt)} chars")

    # Run Codex review
    try:
        service_url = env_value("CODEX_AUDIT_SERVICE_URL")
        if service_url:
            print(f"Running Codex review via service: {service_url}")
            output = run_codex_service_review(prompt, DEFAULT_TIMEOUT_MINUTES)
        else:
            print("Running Codex review via direct API")
            output = run_direct_api_review(prompt)
    except ReviewError as exc:
        print(f"::error::Codex review failed: {exc}", file=sys.stderr)
        # Don't block on review failures — post a warning comment
        warning_body = (
            "<!-- codex-pr-review -->\n"
            "## 🤖 Codex PR Review\n\n"
            "⚠️ **Review skipped**: The Codex review could not be completed.\n\n"
            f"```\n{exc}\n```\n\n"
            "Please ensure a human reviewer checks this PR before merging.\n"
        )
        upsert_pr_comment(token, repo, pr_number, warning_body)
        return 0  # Don't block on infrastructure failures

    print(f"Codex output: {len(output)} chars")

    # Parse review output
    try:
        review = parse_review_output(output)
    except ReviewError as exc:
        print(f"::warning::Failed to parse review output: {exc}")
        # Post raw output as comment
        raw_body = (
            "<!-- codex-pr-review -->\n"
            "## 🤖 Codex PR Review\n\n"
            "⚠️ **Review completed** but output could not be parsed for automated blocking.\n\n"
            "<details><summary>Raw review output</summary>\n\n"
            f"{output[:8000]}\n\n"
            "</details>\n"
        )
        upsert_pr_comment(token, repo, pr_number, raw_body)
        return 0

    findings = review.get("findings", [])
    if not isinstance(findings, list):
        findings = []
    print(f"Found {len(findings)} issue(s)")

    # Evaluate findings
    decision = evaluate_findings(findings, changed_files, policy)

    # Post comment
    comment_body = build_pr_comment(decision, pr_url)
    upsert_pr_comment(token, repo, pr_number, comment_body)

    # Write decision for downstream use
    output_dir = Path("data/output/codex_pr_review")
    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "decision.json").write_text(
        json.dumps(decision, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    # Output for GitHub Actions
    github_output = os.environ.get("GITHUB_OUTPUT")
    if github_output:
        with open(github_output, "a", encoding="utf-8") as f:
            f.write(f"blocked={'true' if decision['blocked'] else 'false'}\n")
            f.write(f"total_findings={decision['total_findings']}\n")
            f.write(f"blocking_count={len(decision['blocking_findings'])}\n")

    if decision["blocked"]:
        print("::error::Merge blocked: serious issues found")
        return 1

    print("Review passed: no blocking issues")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
