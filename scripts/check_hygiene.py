#!/usr/bin/env python3
"""Repository documentation, naming, anti-leak, and agent-file hygiene checker.

Usage:
    python scripts/check_hygiene.py            # check public repository
    python scripts/check_hygiene.py --private  # check private repository
    python scripts/check_hygiene.py --strict   # treat warnings as failures

Exit code 0 means pass, 1 means at least one blocking violation.
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
CONVENTIONAL_ROOT_ONLY = {"CLAUDE.md", "LICENSE"}

SKIP_DIRS = {
    ".git", "node_modules", ".venv", "venv", "__pycache__",
    "build", "dist", ".next", ".gradle", ".agents",
}

# Banned filename patterns.
BANNED_PATTERNS = [
    (re.compile(r"(?<!\d)20\d{2}[-_]?\d{2}[-_]?\d{2}(?!\d)"), "filename contains a date (dates belong to commit history)"),
    (re.compile(r"[-_]v\d+(\.\d+)*$"), "filename ends with a version number (that is a document version, not content)"),
    (re.compile(r"^(v|version)\d+$"), "filename is just a version number"),
    (re.compile(r"[-_](new|old|tmp|temp|final|draft|latest|backup)([-_.]|$)", re.I), "filename contains a stage word"),
    (re.compile(r"[-_](status|current|wip)([-_.]|$)", re.I), "filename contains a status word (it becomes a lie the moment the status changes)"),
    (re.compile(r"(luna|handoff|superpower)", re.I), "filename contains an internal codename"),
]

# Directories split by phase rather than by subject.
BANNED_DIR = re.compile(
    r"^(upgrade-v\d+|v\d+|plans?|handoffs?|audits?|superpowers?|archive|old|tmp)$", re.I
)

# Documents that must not enter the repository.
# Files under prompts/, .github/, and templates/ are templates, not records.
FORBIDDEN_DOC = re.compile(
    r"(audit|postmortem|retrospective|-review-|_review_|^review-|-handoff)", re.I
)
FORBIDDEN_DOC_SKIP_DIRS = {"prompts", ".github", "templates"}

AGENTS_SECTIONS = ["项目是什么", "技术栈", "目录", "本地验证", "命名与文档", "不要做", "提交与 PR"]
AGENTS_SECTIONS_ZH = AGENTS_SECTIONS
AGENTS_SECTIONS_EN = [
    "What this repository is",
    "Stack",
    "Layout",
    "Local checks",
    "Do not",
    "Commits and pull requests",
]

CONTENT_BOUNDARY = "<!-- content-boundary:begin -->"

REQUIRED_GITIGNORE_PATTERNS = [
    "docs/audits/",
    "docs/plans/",
    "docs/handoffs/",
    "docs/changes/",
    "docs/acceptance-policy.md",
    ".claude/",
    ".codex/",
    ".local/",
    "ops-notes/",
    "*.local.md",
    "*.internal.md",
]

FENCED_BLOCK_RE = re.compile(r"^```.*?^```", re.DOTALL | re.MULTILINE)
CODE_SPAN_RE = re.compile(r"`[^`\n]+`")
RE_PATCH_PROSE = re.compile(
    r"\b(this change|this pr|in this pr|this policy pr|remains? unchanged)\b",
    re.IGNORECASE,
)
RE_PHASE_HEADING = re.compile(r"^#{1,4}\s+.*\bP0[–-]P\d+\b", re.MULTILINE)
RE_LICENSE_HEADING = re.compile(r"^##\s+(?:License|许可证)\s*$")
RE_H2_HEADING = re.compile(r"^##\s+\S+")

LINK_RE = re.compile(r"\[[^\]]*\]\(([^)\s]+)\)")
LOCALE_RE = re.compile(r"^(?P<stem>.+?)\.(?P<loc>[A-Za-z_-]+)\.md$")

ALLOWED_LOCALES = {"zh-CN", "en"}
LANGUAGE_ROOTS = {"zh", "cn", "en"}

MAX_PARAGRAPH = 400
SOFT_AGENTS_LINES = 100
MAX_AGENTS_LINES = 120

REQUIRED_OPEN_SOURCE = [
    "CONTRIBUTING.md",
    "SECURITY.md",
    "CODE_OF_CONDUCT.md",
    ".github/pull_request_template.md",
]

errors: list[str] = []
warnings: list[str] = []


def err(msg: str) -> None:
    errors.append(msg)


def warn(msg: str) -> None:
    warnings.append(msg)


def git_visible_files(root: Path) -> set[Path] | None:
    """Return tracked and unignored working-tree files when root is a Git checkout."""
    if not (root / ".git").exists():
        return None
    res = subprocess.run(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"],
        cwd=root,
        capture_output=True,
        check=False,
    )
    if res.returncode != 0:
        return None
    visible: set[Path] = set()
    for raw in res.stdout.split(b"\0"):
        if not raw:
            continue
        rel = raw.decode("utf-8", errors="surrogateescape")
        candidate = root / rel
        if candidate.is_file():
            visible.add(candidate)
    return visible


def is_untracked_gitignored(root: Path, rel_path: str) -> bool:
    """Return True only when rel_path has no tracked/unignored files and is gitignored."""
    if not (root / ".git").exists():
        return False
    listed = subprocess.run(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z", "--", rel_path],
        cwd=root,
        capture_output=True,
        check=False,
    )
    if listed.returncode == 0 and listed.stdout.strip(b"\0"):
        return False
    ignored = subprocess.run(
        ["git", "check-ignore", "-q", "--", rel_path],
        cwd=root,
        check=False,
    )
    return ignored.returncode == 0


def iter_files(root: Path):
    visible = git_visible_files(root)
    if visible is not None:
        for p in sorted(visible):
            if any(part in SKIP_DIRS for part in p.relative_to(root).parts):
                continue
            yield p
        return
    for p in sorted(root.rglob("*")):
        if any(part in SKIP_DIRS for part in p.relative_to(root).parts):
            continue
        if p.is_file():
            yield p


def strip_fenced_blocks(text: str) -> str:
    return FENCED_BLOCK_RE.sub(lambda m: "\n" * m.group(0).count("\n"), text)


def blank_code_spans(text: str) -> str:
    return CODE_SPAN_RE.sub(lambda m: " " * len(m.group(0)), text)


def split_locale(name: str) -> tuple[str, str | None]:
    """Split runbook.zh-CN.md into ('runbook', 'zh-CN')."""
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
    for p in sorted(root.rglob("*")):
        rel = p.relative_to(root)
        if any(part in SKIP_DIRS for part in rel.parts):
            continue
        if p.is_dir() and BANNED_DIR.match(p.name):
            if is_untracked_gitignored(root, f"{rel.as_posix()}/"):
                continue
            err(f"{rel}/: do not split directories by phase, see section 1.3")


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
    cleaned = blank_code_spans(strip_fenced_blocks(text))
    for m in LINK_RE.finditer(cleaned):
        target = m.group(1)
        if target.startswith(("http://", "https://", "mailto:", "#", "<")):
            continue
        resolved = (path.parent / urllib.parse.unquote(target.split("#")[0])).resolve()
        if not resolved.is_relative_to(root):
            warn(f"{rel}: cross-repository reference, not checked -> {target}")
            continue
        if not resolved.exists():
            err(f"{rel}: broken link -> {target}")


def check_readme_quality(root: Path) -> None:
    for name in ("README.md", "README.zh-CN.md"):
        path = root / name
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8", errors="ignore")
        unfenced = strip_fenced_blocks(text)
        prose = blank_code_spans(unfenced)

        for m in RE_PATCH_PROSE.finditer(prose):
            err(f"{name}: PR-diff narration in README prose -> '{m.group(0)}'")
        for m in RE_PHASE_HEADING.finditer(prose):
            err(f"{name}: phase-milestone heading in README -> '{m.group(0).strip()}'")

        seen_license = False
        for raw_line in unfenced.splitlines():
            stripped = raw_line.strip()
            if RE_LICENSE_HEADING.match(stripped):
                seen_license = True
                continue
            if seen_license and RE_H2_HEADING.match(stripped):
                err(f"{name}: section placed after License/许可证 heading -> '{stripped}'")


def check_public_internal_artifacts(root: Path, private: bool = False) -> None:
    if private:
        return
    if (root / "docs" / "changes").is_dir() and not is_untracked_gitignored(root, "docs/changes/"):
        err("docs/changes/: internal change directory must not remain in a public repository")
    if (root / "docs" / "acceptance-policy.md").exists() and not is_untracked_gitignored(
        root, "docs/acceptance-policy.md"
    ):
        err("docs/acceptance-policy.md: internal acceptance policy must not remain in a public repository")


HEAD_LINES = 15


def check_bilingual_pair(root: Path) -> None:
    """When both languages exist, the two files link to each other near the top."""
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
    """Comments are English; grandfathered backlog emits warnings."""
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

    def flush() -> None:
        if buf:
            n = len("".join(buf))
            if n > MAX_PARAGRAPH:
                warn(f"{rel}: paragraph runs {n} characters, limit is {MAX_PARAGRAPH}")
            buf.clear()

    for line in text.split("\n"):
        if line.strip().startswith("```"):
            flush()
            in_code = not in_code
            continue
        if in_code:
            continue
        stripped = line.strip()
        if (
            not stripped
            or line.startswith("#")
            or re.match(r"^(?:[|*>-]|\d+\.)(?:\s|$)", stripped)
            or stripped.startswith("!")
        ):
            flush()
            continue
        buf.append(stripped)
    flush()


def check_agents(root: Path) -> None:
    if (root / ".github" / "engineering.md").exists() and not is_untracked_gitignored(
        root, ".github/engineering.md"
    ):
        err(".github/engineering.md is retired; keep repository rules in AGENTS.md")

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
        err("AGENTS.md is missing")
        return
    lines = agents.read_text(encoding="utf-8", errors="ignore").split("\n")
    if len(lines) > MAX_AGENTS_LINES:
        err(f"AGENTS.md has {len(lines)} lines, limit is {MAX_AGENTS_LINES}")
    elif len(lines) > SOFT_AGENTS_LINES:
        warn(f"AGENTS.md has {len(lines)} lines; keep it under {SOFT_AGENTS_LINES}")

    text = "\n".join(lines)
    zh_missing = [
        s for s in AGENTS_SECTIONS_ZH
        if not re.search(rf"^##\s+{re.escape(s)}\b", text, re.MULTILINE)
    ]
    en_missing = [
        s for s in AGENTS_SECTIONS_EN
        if not re.search(rf"^##\s+{re.escape(s)}\b", text, re.MULTILINE)
    ]
    missing = en_missing if len(en_missing) < len(zh_missing) else zh_missing
    if missing:
        err(f"AGENTS.md is missing sections: {', '.join(missing)}")
    if CONTENT_BOUNDARY not in text:
        err("AGENTS.md is missing the content-boundary block")

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
        err(".gitignore is missing")
        return
    text = gi.read_text(encoding="utf-8", errors="ignore")
    missing = [pat for pat in REQUIRED_GITIGNORE_PATTERNS if pat not in text]
    if missing:
        err(".gitignore is missing required anti-leak patterns: " + ", ".join(missing))
    if re.search(r"^\s*!\s*/?\.github/engineering\.md\s*$", text, re.MULTILINE):
        err(".gitignore still unignores retired .github/engineering.md")


def check_opensource_files(root: Path) -> None:
    for rel in REQUIRED_OPEN_SOURCE:
        if not (root / rel).is_file():
            err(f"{rel} is missing; an open-source repository must ship it")

    if not any((root / name).is_file() for name in ("LICENSE", "LICENSE.md", "LICENSE.txt")):
        err("LICENSE is missing; an open-source repository must ship it")

    issue_dir = root / ".github" / "ISSUE_TEMPLATE"
    if not issue_dir.is_dir() or not any(issue_dir.iterdir()):
        err(".github/ISSUE_TEMPLATE/ is missing or empty; add at least one issue form")


def check_docs_baseline(root: Path, private: bool = False) -> None:
    """Require community health files for public repos and SECURITY.md for private ones."""
    has_license = any((root / name).exists() for name in ("LICENSE", "LICENSE.md", "LICENSE.txt"))
    if not private and has_license:
        required = ["CONTRIBUTING.md", "SECURITY.md", "CODE_OF_CONDUCT.md"]
        kind = "public"
    else:
        required = ["SECURITY.md"]
        kind = "private"
    missing = [name for name in required if not (root / name).exists()]
    if missing:
        err(f"{kind} repository is missing {', '.join(missing)}")


SECRET_PATTERNS = [
    (re.compile(r"ghp_[A-Za-z0-9]{20,}"), "GitHub PAT"),
    (re.compile(r"gho_[A-Za-z0-9]{20,}"), "GitHub OAuth token"),
    (re.compile(r"sk-[A-Za-z0-9]{32,}"), "API key"),
    (re.compile(r"AKIA[0-9A-Z]{16}"), "AWS key"),
    (re.compile(r"xox[baprs]-[A-Za-z0-9-]{20,}"), "Slack token"),
    # Require base64 payload after the PEM header so header-only test fixtures stay valid.
    (re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----\s*\n[A-Za-z0-9+/=]{40,}"), "private key"),
]
SYNTHETIC_SECRET_LITERALS = {"xoxb-1234567890-abcdefghij"}


def check_secrets(root: Path) -> None:
    for p in iter_files(root):
        if p.suffix in {".png", ".jpg", ".pdf", ".zip", ".jar", ".apk", ".so", ".dylib", ".lock"}:
            continue
        try:
            text = p.read_text(encoding="utf-8", errors="ignore")
        except OSError:
            continue
        for pat, label in SECRET_PATTERNS:
            for m in pat.finditer(text):
                if m.group(0) in SYNTHETIC_SECRET_LITERALS:
                    continue
                # Never echo the matched value into CI logs.
                err(f"{p.relative_to(root)}: possible hardcoded {label} (value withheld)")


def main(argv: list[str] | None = None) -> int:
    errors.clear()
    warnings.clear()

    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--root", default=".", help="repository root")
    ap.add_argument("--strict", action="store_true", help="treat warnings as failures")
    ap.add_argument("--private", action="store_true", help="skip public-only OSS baseline and leak checks")
    args = ap.parse_args(argv)

    root = Path(args.root).resolve()

    check_dirs(root)
    for path in iter_files(root):
        check_filename(root, path)
        if path.suffix == ".md":
            check_doc_type(root, path)
            check_links(root, path)
            check_paragraphs(root, path)
    check_readme_quality(root)
    check_public_internal_artifacts(root, private=args.private)
    check_agents(root)
    check_bilingual_pair(root)
    check_comment_language(root)
    check_gitignore(root)
    if args.private:
        check_docs_baseline(root, private=True)
    else:
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
