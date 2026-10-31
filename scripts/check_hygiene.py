#!/usr/bin/env python3
"""Repository documentation and naming check. Rules live in AI_AGENT_STANDARD.md.

Usage:
    python scripts/check_hygiene.py            # check the current repository
    python scripts/check_hygiene.py --strict   # treat warnings as failures

Exit code 0 means pass, 1 means there is at least one violation.
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
import unicodedata
import urllib.parse
from pathlib import Path

# Files whose names are fixed by GitHub or by long-standing community
# convention. They are exempt from the lowercase / hyphen rules.
CONVENTIONAL_ANYWHERE = {
    "README.md",
    "SECURITY.md",
    "CONTRIBUTING.md",
    "CODE_OF_CONDUCT.md",
    "SUPPORT.md",
    "LICENSE",
    "LICENSE.md",
    "THIRD_PARTY_NOTICES.md",
    "TRADEMARK.md",
    "CHANGELOG.md",
    "AGENTS.md",
    "CLAUDE.md",
}
CONVENTIONAL_ROOT_ONLY = {"AGENTS.md", "CLAUDE.md", "LICENSE"}

SKIP_DIRS = {
    ".git", "node_modules", ".venv", "venv", "__pycache__",
    "build", "dist", ".next", ".gradle", ".agents",
}

# Section 1.4 — banned filename patterns.
BANNED_PATTERNS = [
    (re.compile(r"(?<!\d)20\d{2}[-_]?\d{2}[-_]?\d{2}(?!\d)"), "filename contains a date (dates belong to commit history)"),
    (re.compile(r"[-_]v\d+(\.\d+)*$"), "filename ends with a version number (that is a document version, not content)"),
    (re.compile(r"^(v|version)\d+$"), "filename is just a version number"),
    (re.compile(r"[-_](new|old|tmp|temp|final|draft|latest|backup)([-_.]|$)", re.I), "filename contains a stage word"),
    (re.compile(r"[-_](status|current|wip)([-_.]|$)", re.I), "filename contains a status word (it becomes a lie the moment the status changes)"),
    (re.compile(r"(luna|handoff|superpower)", re.I), "filename contains an internal codename"),
]

# Section 1.3 — directories split by phase rather than by subject.
BANNED_DIR = re.compile(
    r"^(upgrade-v\d+|v\d+|plans?|handoffs?|audits?|superpowers?|archive|old|tmp)$", re.I
)

# Section 2.2 — documents that must not enter the repository.
# Files under prompts/ are templates, not records, so they are skipped.
FORBIDDEN_DOC = re.compile(
    r"(audit|postmortem|retrospective|-review-|_review_|^review-|-handoff)", re.I
)
FORBIDDEN_DOC_SKIP_DIRS = {"prompts", ".github"}

AGENTS_SECTIONS = ["项目是什么", "技术栈", "目录", "本地验证", "命名与文档", "不要做", "提交与 PR"]

# The block `tools/sync_agents.py` writes. A rule stated only in the guide is
# read by whoever opens the guide; this one is read by every agent that opens
# the repository, which is the only point early enough to matter.
CONTENT_BOUNDARY = "<!-- content-boundary:begin -->"

LINK_RE = re.compile(r"\[[^\]]*\]\(([^)\s]+)\)")
LOCALE_RE = re.compile(r"^(?P<stem>.+?)\.(?P<loc>[A-Za-z_-]+)\.md$")

# Section 1.2 — only .zh-CN.md and .en.md are valid; every other variant is wrong.
ALLOWED_LOCALES = {"zh-CN", "en"}
# Language roots used to tell a locale suffix apart from a dotted filename.
# A tag with a known root but a bad shape (en-us, zh_CN, zh-Hans) is reported.
LANGUAGE_ROOTS = {"zh", "cn", "en"}

MAX_PARAGRAPH = 400
MAX_AGENTS_LINES = 120

errors: list[str] = []
warnings: list[str] = []


def err(msg: str) -> None:
    errors.append(msg)


def warn(msg: str) -> None:
    warnings.append(msg)


def iter_files(root: Path):
    for p in root.rglob("*"):
        if any(part in SKIP_DIRS for part in p.parts):
            continue
        if p.is_file():
            yield p


def split_locale(name: str) -> tuple[str, str | None]:
    """Split runbook.zh-CN.md into ('runbook', 'zh-CN').

    Any tag whose language root is a known one counts as a locale suffix, even
    when the tag itself is invalid (en-us, zh_CN, zh-Hans). Otherwise a wrong
    variant would slip through as an ordinary dotted filename and never be
    reported.
    """
    m = LOCALE_RE.match(name)
    if m:
        tag = m.group("loc")
        root = re.split(r"[-_]", tag)[0].lower()
        if root in LANGUAGE_ROOTS:
            return m.group("stem"), tag
    return name[:-3], None


def locale_error(locale: str) -> str | None:
    """Return the error for a bad locale suffix, or None when it is valid."""
    if locale in ALLOWED_LOCALES:
        return None
    return f"locale suffix must be .zh-CN.md or .en.md, not .{locale}.md"


def check_filename(root: Path, path: Path) -> None:
    rel = path.relative_to(root)
    name = path.name

    # GitHub decides the filenames under .github, so they are left alone.
    if rel.parts and rel.parts[0] == ".github":
        return

    stem, locale = split_locale(name) if name.endswith(".md") else (name, None)
    base = f"{stem}.md"

    if base in CONVENTIONAL_ANYWHERE:
        if base in CONVENTIONAL_ROOT_ONLY and rel.parent != Path("."):
            warn(f"{rel}: {base} belongs at the repository root")
        if locale:
            msg = locale_error(locale)
            if msg:
                err(f"{rel}: {msg}")
        return

    if path.suffix not in {".md", ".yml", ".yaml"}:
        return

    if stem != stem.lower():
        err(f"{rel}: filename must be all lowercase")
    if "_" in stem:
        err(f"{rel}: join words with -, not _")
    if " " in name:
        err(f"{rel}: filename must not contain spaces")
    if any(unicodedata.east_asian_width(c) in "WF" for c in name):
        err(f"{rel}: filename must be ASCII")

    for pattern, reason in BANNED_PATTERNS:
        if pattern.search(stem):
            err(f"{rel}: {reason}")

    if locale:
        msg = locale_error(locale)
        if msg:
            err(f"{rel}: {msg}")


def check_dirs(root: Path) -> None:
    for p in root.rglob("*"):
        if any(part in SKIP_DIRS for part in p.parts):
            continue
        if p.is_dir() and BANNED_DIR.match(p.name):
            err(f"{p.relative_to(root)}/: do not split directories by phase, see section 1.3")


def check_doc_type(root: Path, path: Path) -> None:
    rel = path.relative_to(root)
    if any(part in FORBIDDEN_DOC_SKIP_DIRS for part in rel.parts[:-1]):
        return
    if FORBIDDEN_DOC.search(path.stem):
        err(f"{rel}: audit / retrospective / handoff documents do not enter the repository, see section 2.2")


def check_links(root: Path, path: Path) -> None:
    rel = path.relative_to(root)
    try:
        text = path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return
    for m in LINK_RE.finditer(text):
        target = m.group(1)
        if target.startswith(("http://", "https://", "mailto:", "#", "<")):
            continue
        resolved = (path.parent / urllib.parse.unquote(target.split("#")[0])).resolve()
        # A link that climbs out of the repository points at a sibling checkout.
        # CI only has this repository, so the target cannot exist there — that is
        # a reference this check cannot verify, not a broken link.
        if not resolved.is_relative_to(root):
            warn(f"{rel}: cross-repository reference, not checked -> {target}")
            continue
        if not resolved.exists():
            err(f"{rel}: broken link -> {target}")


HEAD_LINES = 15


def check_bilingual_pair(root: Path) -> None:
    """Section 1.2 — when both languages exist, the two files link to each other."""
    for p in iter_files(root):
        if p.suffix != ".md":
            continue
        stem, locale = split_locale(p.name)
        if not locale:
            continue
        base = p.with_name(f"{stem}.md")
        rel = p.relative_to(root)
        if not base.exists():
            warn(f"{rel}: has a .{locale}.md version but no {base.name}; a single-language file needs no suffix")
            continue
        try:
            translated = "\n".join(p.read_text(encoding="utf-8", errors="ignore").split("\n")[:HEAD_LINES])
            original = "\n".join(base.read_text(encoding="utf-8", errors="ignore").split("\n")[:HEAD_LINES])
        except OSError:
            continue
        if base.name not in translated:
            err(f"{rel}: no link back to {base.name} near the top")
        if p.name not in original:
            err(f"{base.relative_to(root)}: no link to {p.name} near the top")


CJK_RE = re.compile(r"[\u3400-\u4dbf\u4e00-\u9fff]")
COMMENT_SYNTAX = {
    ".py": ("#",), ".rb": ("#",), ".sh": ("#",), ".bash": ("#",), ".zsh": ("#",),
    ".yml": ("#",), ".yaml": ("#",), ".toml": ("#",), ".tf": ("#",),
    ".js": ("//",), ".jsx": ("//",), ".ts": ("//",), ".tsx": ("//",), ".mjs": ("//",),
    ".java": ("//",), ".kt": ("//",), ".kts": ("//",), ".go": ("//",), ".rs": ("//",),
    ".swift": ("//",), ".cs": ("//",), ".c": ("//",), ".h": ("//",),
    ".cpp": ("//",), ".hpp": ("//",), ".m": ("//",), ".mm": ("//",),
    ".gradle": ("//",), ".scala": ("//",), ".dart": ("//",), ".php": ("//",),
}
CODE_SKIP_DIRS = SKIP_DIRS | {"migrations", "testdata", "fixtures", "vendor", "third_party"}


def check_comment_language(root: Path) -> None:
    """Section 3.6 — comments are English. Chinese inside string literals is fine.

    Existing code is grandfathered: new comments are English, and old ones get
    translated when someone touches the surrounding code. That is why this is a
    warning and does not affect the exit code.
    """
    offenders: list[tuple[str, int]] = []
    for p in iter_files(root):
        prefixes = COMMENT_SYNTAX.get(p.suffix)
        if not prefixes:
            continue
        if any(part in CODE_SKIP_DIRS for part in p.relative_to(root).parts):
            continue
        try:
            text = p.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        hits = 0
        for line in text.split("\n"):
            stripped = line.strip()
            if not stripped.startswith(prefixes):
                continue
            if CJK_RE.search(stripped):
                hits += 1
        if hits:
            offenders.append((str(p.relative_to(root)), hits))

    if offenders:
        total = sum(n for _, n in offenders)
        offenders.sort(key=lambda x: -x[1])
        top = ", ".join(f"{f}({n})" for f, n in offenders[:5])
        warn(f"{len(offenders)} files carry {total} lines of non-English comments; write new ones in English — worst: {top}")


def check_paragraphs(root: Path, path: Path) -> None:
    rel = path.relative_to(root)
    try:
        text = path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return
    in_code = False
    buf: list[str] = []
    for line in text.split("\n"):
        if line.strip().startswith("```"):
            in_code = not in_code
            continue
        if in_code:
            continue
        if not line.strip():
            if buf:
                n = len("".join(buf))
                if n > MAX_PARAGRAPH:
                    warn(f"{rel}: paragraph runs {n} characters, limit is {MAX_PARAGRAPH}")
                buf = []
            continue
        if line.startswith("#") or line.strip().startswith(("|", "-", "*", ">", "!", "1.", "2.")):
            continue
        buf.append(line.strip())
    if buf and len("".join(buf)) > MAX_PARAGRAPH:
        warn(f"{rel}: paragraph exceeds {MAX_PARAGRAPH} characters")


def check_agents(root: Path) -> None:
    tracked = subprocess.run(
        ["git", "ls-files", "--cached", "-z", "--", "AGENTS.md", "CLAUDE.md"],
        cwd=root, capture_output=True, text=True, check=False,
    )
    if tracked.returncode:
        err("cannot read Git tracking state; run this from the repository root")
    else:
        names = set(tracked.stdout.split("\0"))
        for name in ("AGENTS.md", "CLAUDE.md"):
            if name not in names:
                err(f"{name} is not tracked by Git; existing on disk does not mean committed")

    agents = root / "AGENTS.md"
    if not agents.exists():
        err("AGENTS.md is missing, see section 4")
        return
    lines = agents.read_text(encoding="utf-8", errors="ignore").split("\n")
    if len(lines) > MAX_AGENTS_LINES:
        err(f"AGENTS.md has {len(lines)} lines, limit is {MAX_AGENTS_LINES}")
    text = "\n".join(lines)
    missing = [s for s in AGENTS_SECTIONS if f"## {s}" not in text]
    if missing:
        err(f"AGENTS.md is missing sections: {', '.join(missing)}")
    if CONTENT_BOUNDARY not in text:
        err("AGENTS.md is missing the content-boundary block, see section 5")

    claude = root / "CLAUDE.md"
    if not claude.exists():
        err("CLAUDE.md is missing; its content should be the single line @AGENTS.md")
    else:
        content = claude.read_text(encoding="utf-8", errors="ignore").strip()
        if content != "@AGENTS.md":
            err("CLAUDE.md must be the single line @AGENTS.md, not a copy of the AGENTS.md body")


def check_gitignore(root: Path) -> None:
    gi = root / ".gitignore"
    if not gi.exists():
        warn(".gitignore is missing")
        return
    text = gi.read_text(encoding="utf-8", errors="ignore")
    if "docs/audits/" not in text:
        warn(".gitignore is missing the audit-document block, see section 5")


# Section 5.2 item 9 — files an open-source repository must ship.
# This is the one rule Pigbibi's private repositories must NOT follow, and the
# reason this organisation keeps its own copy of the checker.
REQUIRED_OPEN_SOURCE = [
    "CONTRIBUTING.md",
    "SECURITY.md",
    "CODE_OF_CONDUCT.md",
    ".github/pull_request_template.md",
]


def check_opensource_files(root: Path) -> None:
    for rel in REQUIRED_OPEN_SOURCE:
        if not (root / rel).is_file():
            err(f"{rel} is missing; an open-source repository must ship it")

    if not any((root / name).is_file() for name in ("LICENSE", "LICENSE.md", "LICENSE.txt")):
        err("LICENSE is missing; an open-source repository must ship it")

    issue_dir = root / ".github" / "ISSUE_TEMPLATE"
    if not issue_dir.is_dir() or not any(issue_dir.iterdir()):
        err(".github/ISSUE_TEMPLATE/ is missing or empty; add at least one issue form")


SECRET_PATTERNS = [
    (re.compile(r"ghp_[A-Za-z0-9]{20,}"), "GitHub PAT"),
    (re.compile(r"gho_[A-Za-z0-9]{20,}"), "GitHub OAuth token"),
    (re.compile(r"sk-[A-Za-z0-9]{32,}"), "API key"),
    (re.compile(r"AKIA[0-9A-Z]{16}"), "AWS key"),
    (re.compile(r"xox[baprs]-[A-Za-z0-9-]{20,}"), "Slack token"),
    # The PEM header must be followed by real base64; placeholders in test
    # fixtures are not a leak.
    (re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----\s*\n[A-Za-z0-9+/=]{40,}"), "private key"),
]


def check_secrets(root: Path) -> None:
    for p in iter_files(root):
        if p.suffix in {".png", ".jpg", ".pdf", ".zip", ".jar", ".apk", ".so", ".dylib", ".lock"}:
            continue
        try:
            text = p.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for pat, label in SECRET_PATTERNS:
            for _ in pat.finditer(text):
                # Never echo the matched value into CI logs.
                err(f"{p.relative_to(root)}: possible hardcoded {label} (value withheld)")


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".", help="repository root")
    ap.add_argument("--strict", action="store_true", help="treat warnings as failures")
    args = ap.parse_args()

    root = Path(args.root).resolve()

    check_dirs(root)
    for path in iter_files(root):
        check_filename(root, path)
        if path.suffix == ".md":
            check_doc_type(root, path)
            check_links(root, path)
            check_paragraphs(root, path)
    check_agents(root)
    check_bilingual_pair(root)
    check_comment_language(root)
    check_gitignore(root)
    check_opensource_files(root)
    check_secrets(root)

    for w in sorted(set(warnings)):
        print(f"warning  {w}")
    for e in sorted(set(errors)):
        print(f"error    {e}")

    print(f"\n{len(set(errors))} errors, {len(set(warnings))} warnings")

    if errors:
        return 1
    if args.strict and warnings:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
