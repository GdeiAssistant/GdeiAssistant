#!/usr/bin/env python3
"""仓库文档与命名检查。规则见 AI_AGENT_STANDARD.md。

用法：
    python scripts/check_hygiene.py            # 检查当前仓库
    python scripts/check_hygiene.py --strict   # 警告也当失败

退出码 0 通过，1 有违规。
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
import unicodedata
import urllib.parse
from pathlib import Path

# GitHub / 社区约定固定写法的文件，不做大小写与连字符检查
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

# 1.4 禁用模式
BANNED_PATTERNS = [
    (re.compile(r"(?<!\d)20\d{2}[-_]?\d{2}[-_]?\d{2}(?!\d)"), "文件名含日期（日期属于 commit 历史）"),
    (re.compile(r"[-_]v\d+(\.\d+)*$"), "文件名以版本号结尾（这是文档版本，不是内容）"),
    (re.compile(r"^(v|version)\d+$"), "文件名就是版本号"),
    (re.compile(r"[-_](new|old|tmp|temp|final|draft|latest|backup)([-_.]|$)", re.I), "文件名含阶段词"),
    (re.compile(r"[-_](status|current|wip)([-_.]|$)", re.I), "文件名含状态词（状态一变文件名就成假话）"),
    (re.compile(r"(luna|handoff|superpower)", re.I), "文件名含内部代号"),
]

# 1.3 按阶段划分的目录
BANNED_DIR = re.compile(
    r"^(upgrade-v\d+|v\d+|plans?|handoffs?|audits?|superpowers?|archive|old|tmp)$", re.I
)

# 2.2 不该进仓库的文档。prompts/ 下的是模板，不是记录，跳过。
FORBIDDEN_DOC = re.compile(
    r"(audit|postmortem|retrospective|-review-|_review_|^review-|-handoff)", re.I
)
FORBIDDEN_DOC_SKIP_DIRS = {"prompts", ".github"}

AGENTS_SECTIONS = ["项目是什么", "技术栈", "目录", "本地验证", "命名与文档", "不要做", "提交与 PR"]

LINK_RE = re.compile(r"\[[^\]]*\]\(([^)\s]+)\)")
LOCALE_RE = re.compile(r"^(?P<stem>.+?)\.(?P<loc>[A-Za-z_-]+)\.md$")

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
    """把 runbook.zh-CN.md 拆成 ('runbook', 'zh-CN')。"""
    m = LOCALE_RE.match(name)
    if m and m.group("loc").lower() in {"zh", "zh-cn", "zh_cn", "cn", "en", "zh-hans"}:
        return m.group("stem"), m.group("loc")
    return name[:-3], None


def check_filename(root: Path, path: Path) -> None:
    rel = path.relative_to(root)
    name = path.name

    # .github 下的文件名由 GitHub 规定，不改
    if rel.parts and rel.parts[0] == ".github":
        return

    stem, locale = split_locale(name) if name.endswith(".md") else (name, None)
    base = f"{stem}.md"

    if base in CONVENTIONAL_ANYWHERE:
        if base in CONVENTIONAL_ROOT_ONLY and rel.parent != Path("."):
            warn(f"{rel}: {base} 约定放在仓库根目录")
        if locale and locale != "zh-CN":
            err(f"{rel}: 语言后缀只能是 .zh-CN.md，不是 .{locale}.md")
        return

    if path.suffix not in {".md", ".yml", ".yaml"}:
        return

    if stem != stem.lower():
        err(f"{rel}: 文件名必须全小写")
    if "_" in stem:
        err(f"{rel}: 用 - 连接，不用 _")
    if " " in name:
        err(f"{rel}: 文件名不能含空格")
    if any(unicodedata.east_asian_width(c) in "WF" for c in name):
        err(f"{rel}: 文件名不能含非 ASCII 字符")

    for pattern, reason in BANNED_PATTERNS:
        if pattern.search(stem):
            err(f"{rel}: {reason}")

    if locale and locale != "zh-CN":
        err(f"{rel}: 语言后缀只能是 .zh-CN.md，不是 .{locale}.md")


def check_dirs(root: Path) -> None:
    for p in root.rglob("*"):
        if any(part in SKIP_DIRS for part in p.parts):
            continue
        if p.is_dir() and BANNED_DIR.match(p.name):
            err(f"{p.relative_to(root)}/: 不建按阶段划分的目录，见标准 1.3")


def check_doc_type(root: Path, path: Path) -> None:
    rel = path.relative_to(root)
    if any(part in FORBIDDEN_DOC_SKIP_DIRS for part in rel.parts[:-1]):
        return
    if FORBIDDEN_DOC.search(path.stem):
        err(f"{rel}: 审计/复盘/交接类文档不进仓库，见标准 2.2")


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
        if not resolved.exists():
            err(f"{rel}: 链接失效 -> {target}")


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
                    warn(f"{rel}: 段落 {n} 字，超过 {MAX_PARAGRAPH}")
                buf = []
            continue
        if line.startswith("#") or line.strip().startswith(("|", "-", "*", ">", "!", "1.", "2.")):
            continue
        buf.append(line.strip())
    if buf and len("".join(buf)) > MAX_PARAGRAPH:
        warn(f"{rel}: 段落超过 {MAX_PARAGRAPH} 字")


def check_agents(root: Path) -> None:
    tracked = subprocess.run(
        ["git", "ls-files", "--cached", "-z", "--", "AGENTS.md", "CLAUDE.md"],
        cwd=root, capture_output=True, text=True, check=False,
    )
    if tracked.returncode:
        err("无法读取 Git 跟踪状态，请在仓库根目录检查 AGENTS.md / CLAUDE.md")
    else:
        names = set(tracked.stdout.split("\0"))
        for name in ("AGENTS.md", "CLAUDE.md"):
            if name not in names:
                err(f"{name} 未被 Git 跟踪；本地存在不代表已提交")

    agents = root / "AGENTS.md"
    if not agents.exists():
        err("缺少 AGENTS.md，见标准第四章")
        return
    lines = agents.read_text(encoding="utf-8", errors="ignore").split("\n")
    if len(lines) > MAX_AGENTS_LINES:
        err(f"AGENTS.md 有 {len(lines)} 行，超过 {MAX_AGENTS_LINES} 行")
    text = "\n".join(lines)
    missing = [s for s in AGENTS_SECTIONS if f"## {s}" not in text]
    if missing:
        err(f"AGENTS.md 缺少章节：{', '.join(missing)}")

    claude = root / "CLAUDE.md"
    if not claude.exists():
        err("缺少 CLAUDE.md，内容应为单行 @AGENTS.md")
    else:
        content = claude.read_text(encoding="utf-8", errors="ignore").strip()
        if content != "@AGENTS.md":
            err("CLAUDE.md 内容应为单行 @AGENTS.md，不要复制 AGENTS.md 正文")


def check_gitignore(root: Path) -> None:
    gi = root / ".gitignore"
    if not gi.exists():
        warn("缺少 .gitignore")
        return
    text = gi.read_text(encoding="utf-8", errors="ignore")
    if "docs/audits/" not in text:
        warn(".gitignore 缺少审计文档拦截规则，见标准第五章")


SECRET_PATTERNS = [
    (re.compile(r"ghp_[A-Za-z0-9]{20,}"), "GitHub PAT"),
    (re.compile(r"gho_[A-Za-z0-9]{20,}"), "GitHub OAuth"),
    (re.compile(r"sk-[A-Za-z0-9]{32,}"), "API key"),
    (re.compile(r"AKIA[0-9A-Z]{16}"), "AWS key"),
    (re.compile(r"xox[baprs]-[A-Za-z0-9-]{20,}"), "Slack token"),
    # PEM 头后面必须跟着真实长度的 base64，测试 fixture 里的占位符不算
    (re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----\s*\n[A-Za-z0-9+/=]{40,}"), "私钥"),
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
            for m in pat.finditer(text):
                err(f"{p.relative_to(root)}: 疑似硬编码{label}（值不输出）")


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".", help="仓库根目录")
    ap.add_argument("--strict", action="store_true", help="警告也视为失败")
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
    check_gitignore(root)
    check_secrets(root)

    for w in sorted(set(warnings)):
        print(f"警告  {w}")
    for e in sorted(set(errors)):
        print(f"错误  {e}")

    print(f"\n{len(set(errors))} 个错误，{len(set(warnings))} 个警告")

    if errors:
        return 1
    if args.strict and warnings:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
