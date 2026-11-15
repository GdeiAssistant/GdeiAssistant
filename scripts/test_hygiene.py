"""Regression tests for the documentation check and its diagnostics.

A rule that is written wrong silently passes a whole class of files, so the
checker itself is tested. Keep this file next to check_hygiene.py.
"""

from __future__ import annotations

import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest

spec = importlib.util.spec_from_file_location(
    "repository_hygiene", Path(__file__).with_name("check_hygiene.py")
)
hygiene = importlib.util.module_from_spec(spec)
spec.loader.exec_module(hygiene)


class AgentInstructionsTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        hygiene.errors.clear()
        hygiene.warnings.clear()
        self.git("init", "--quiet")
        (self.root / "AGENTS.md").write_text(
            "\n".join("## " + name for name in hygiene.AGENTS_SECTIONS)
            + "\n" + hygiene.CONTENT_BOUNDARY,
            encoding="utf-8",
        )
        (self.root / "CLAUDE.md").write_text("@AGENTS.md\n", encoding="utf-8")

    def git(self, *args):
        subprocess.run(
            ["git", "-C", str(self.root), *args],
            check=True, capture_output=True,
        )

    def test_ignored_local_files_are_not_a_delivery(self):
        (self.root / ".gitignore").write_text("AGENTS.md\nCLAUDE.md\n")
        hygiene.check_agents(self.root)
        self.assertTrue(any("AGENTS.md" in e for e in hygiene.errors))
        self.assertTrue(any("CLAUDE.md" in e for e in hygiene.errors))

    def test_tracked_instructions_pass(self):
        self.git("add", "AGENTS.md", "CLAUDE.md")
        hygiene.check_agents(self.root)
        self.assertEqual(hygiene.errors, [])

    def test_missing_claude_entry_is_reported(self):
        (self.root / "CLAUDE.md").unlink()
        self.git("add", "AGENTS.md")
        hygiene.check_agents(self.root)
        self.assertTrue(any("CLAUDE.md" in e for e in hygiene.errors))

    def test_content_boundary_block_is_required(self):
        (self.root / "AGENTS.md").write_text(
            "\n".join("## " + name for name in hygiene.AGENTS_SECTIONS),
            encoding="utf-8",
        )
        self.git("add", "AGENTS.md", "CLAUDE.md")
        hygiene.check_agents(self.root)
        self.assertTrue(any("content-boundary" in e for e in hygiene.errors))

    def test_git_metadata_is_required(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for name in ("AGENTS.md", "CLAUDE.md"):
                (root / name).write_bytes((self.root / name).read_bytes())
            hygiene.check_agents(root)
            self.assertTrue(hygiene.errors)

    def test_secret_diagnostic_does_not_print_the_value(self):
        value = "ghp_" + "X" * 30
        (self.root / "example.txt").write_text(value)
        hygiene.check_secrets(self.root)
        self.assertTrue(hygiene.errors)
        self.assertNotIn(value[:12], "\n".join(hygiene.errors))


class LocaleSuffixTest(unittest.TestCase):
    """Section 1.2 — only .zh-CN.md and .en.md exist."""

    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        hygiene.errors.clear()
        hygiene.warnings.clear()

    def check(self, name: str):
        path = self.root / name
        path.write_text("# Title\n", encoding="utf-8")
        hygiene.check_filename(self.root, path)
        return list(hygiene.errors)

    def test_en_suffix_is_accepted(self):
        self.assertEqual(self.check("guide.en.md"), [])

    def test_zh_cn_suffix_is_accepted(self):
        self.assertEqual(self.check("guide.zh-CN.md"), [])

    def test_lowercase_zh_cn_is_rejected(self):
        self.assertTrue(self.check("guide.zh-cn.md"))

    def test_regional_english_variant_is_rejected(self):
        self.assertTrue(self.check("guide.en-us.md"))


class BilingualPairTest(unittest.TestCase):
    """Section 1.2 — both files link to each other near the top."""

    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        hygiene.errors.clear()
        hygiene.warnings.clear()

    def write(self, name: str, body: str):
        (self.root / name).write_text(body, encoding="utf-8")

    def test_missing_link_is_an_error(self):
        self.write("guide.md", "# Guide\n\nEnglish | [简体中文](guide.zh-CN.md)\n")
        self.write("guide.zh-CN.md", "# 指南\n\n中文正文。\n")
        hygiene.check_bilingual_pair(self.root)
        self.assertTrue(any("guide.md" in e for e in hygiene.errors))

    def test_mutual_links_pass(self):
        self.write("guide.md", "# Guide\n\nEnglish | [简体中文](guide.zh-CN.md)\n")
        self.write("guide.zh-CN.md", "# 指南\n\n[English](guide.md) | 简体中文\n")
        hygiene.check_bilingual_pair(self.root)
        self.assertEqual(hygiene.errors, [])

    def test_orphan_translation_warns_but_does_not_fail(self):
        self.write("guide.zh-CN.md", "# 指南\n\n[English](guide.md) | 简体中文\n")
        hygiene.check_bilingual_pair(self.root)
        self.assertEqual(hygiene.errors, [])
        self.assertTrue(hygiene.warnings)


class CommentLanguageTest(unittest.TestCase):
    """Section 3.6 — comments are English, and the backlog only warns."""

    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        hygiene.errors.clear()
        hygiene.warnings.clear()

    def test_chinese_comment_warns_without_failing(self):
        (self.root / "module.py").write_text("# 这里等待重试\nvalue = 1\n", encoding="utf-8")
        hygiene.check_comment_language(self.root)
        self.assertEqual(hygiene.errors, [])
        self.assertTrue(hygiene.warnings)

    def test_chinese_string_literal_is_not_a_comment(self):
        (self.root / "module.py").write_text('MESSAGE = "重试失败"\n', encoding="utf-8")
        hygiene.check_comment_language(self.root)
        self.assertEqual(hygiene.warnings, [])

    def test_english_comment_passes(self):
        (self.root / "module.py").write_text("# wait before retrying\nvalue = 1\n", encoding="utf-8")
        hygiene.check_comment_language(self.root)
        self.assertEqual(hygiene.warnings, [])


class OpenSourceFilesTest(unittest.TestCase):
    """Section 5.2 item 9 — an open-source repository ships these, a private one must not."""

    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        hygiene.errors.clear()
        hygiene.warnings.clear()

    def test_missing_files_are_reported(self):
        hygiene.check_opensource_files(self.root)
        joined = "\n".join(hygiene.errors)
        for name in ("LICENSE", "CONTRIBUTING.md", "SECURITY.md", "CODE_OF_CONDUCT.md"):
            self.assertIn(name, joined)

    def test_complete_repository_passes(self):
        for name in ("LICENSE", "CONTRIBUTING.md", "SECURITY.md", "CODE_OF_CONDUCT.md"):
            (self.root / name).write_text("x\n", encoding="utf-8")
        (self.root / ".github" / "ISSUE_TEMPLATE").mkdir(parents=True)
        (self.root / ".github" / "ISSUE_TEMPLATE" / "bug.yml").write_text("name: Bug\n", encoding="utf-8")
        (self.root / ".github" / "pull_request_template.md").write_text("## Summary\n", encoding="utf-8")
        hygiene.check_opensource_files(self.root)
        self.assertEqual(hygiene.errors, [])

    def test_empty_issue_template_directory_is_reported(self):
        for name in ("LICENSE", "CONTRIBUTING.md", "SECURITY.md", "CODE_OF_CONDUCT.md"):
            (self.root / name).write_text("x\n", encoding="utf-8")
        (self.root / ".github" / "ISSUE_TEMPLATE").mkdir(parents=True)
        (self.root / ".github" / "pull_request_template.md").write_text("## Summary\n", encoding="utf-8")
        hygiene.check_opensource_files(self.root)
        self.assertTrue(any("ISSUE_TEMPLATE" in e for e in hygiene.errors))



class AntiLeakAndReadmeHygieneTest(unittest.TestCase):
    def _init_repo(self, repo: Path, public: bool = False) -> None:
        sections = "\n".join(f"## {s}\nOK" for s in hygiene.AGENTS_SECTIONS_ZH)
        (repo / "AGENTS.md").write_text(f"{sections}\n{hygiene.CONTENT_BOUNDARY}\n", encoding="utf-8")
        (repo / "CLAUDE.md").write_text("@AGENTS.md\n", encoding="utf-8")
        (repo / "SECURITY.md").write_text("# Security\n\nReport issues privately.\n", encoding="utf-8")
        if public:
            (repo / "LICENSE").write_text("MIT\n", encoding="utf-8")
            (repo / "CONTRIBUTING.md").write_text("# Contributing\n", encoding="utf-8")
            (repo / "CODE_OF_CONDUCT.md").write_text("# Code of Conduct\n", encoding="utf-8")
            (repo / ".github" / "ISSUE_TEMPLATE").mkdir(parents=True, exist_ok=True)
            (repo / ".github" / "ISSUE_TEMPLATE" / "bug.yml").write_text("name: Bug\n", encoding="utf-8")
            (repo / ".github" / "pull_request_template.md").write_text("## Summary\n", encoding="utf-8")
        (repo / ".gitignore").write_text("\n".join(hygiene.REQUIRED_GITIGNORE_PATTERNS) + "\n", encoding="utf-8")
        subprocess.run(["git", "init", "-q"], cwd=repo, check=True)
        subprocess.run(["git", "config", "user.name", "Test User"], cwd=repo, check=True)
        subprocess.run(["git", "config", "user.email", "test@example.com"], cwd=repo, check=True)
        subprocess.run(["git", "add", "AGENTS.md", "CLAUDE.md", "SECURITY.md", ".gitignore"], cwd=repo, check=True)

    def test_retired_engineering_and_changes_docs_are_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo = Path(tmp)
            self._init_repo(repo)
            (repo / ".github").mkdir(exist_ok=True)
            (repo / ".github" / "engineering.md").write_text("# Legacy\n", encoding="utf-8")
            (repo / "docs" / "changes").mkdir(parents=True)
            (repo / "docs" / "changes" / "note.md").write_text("# Note\n", encoding="utf-8")
            (repo / "docs" / "acceptance-policy.md").write_text("# Policy\n", encoding="utf-8")
            subprocess.run(["git", "add", "-f", "docs/changes/note.md", "docs/acceptance-policy.md"], cwd=repo, check=True)
            hygiene.errors.clear()
            hygiene.warnings.clear()
            hygiene.check_agents(repo)
            hygiene.check_public_internal_artifacts(repo)
            joined = "\n".join(hygiene.errors)
            self.assertIn(".github/engineering.md is retired", joined)
            self.assertIn("docs/changes/", joined)
            self.assertIn("docs/acceptance-policy.md", joined)

    def test_gitignored_notes_pass_while_public_templates_are_enforced(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo = Path(tmp)
            self._init_repo(repo, public=True)
            (repo / "docs" / "changes").mkdir(parents=True)
            (repo / "docs" / "changes" / "note.md").write_text("# Note\n", encoding="utf-8")
            (repo / "docs" / "plans").mkdir(parents=True)
            (repo / "docs" / "plans" / "draft.md").write_text("# Draft\n", encoding="utf-8")
            (repo / "docs" / "acceptance-policy.md").write_text("# Policy\n", encoding="utf-8")
            self.assertEqual(hygiene.main(["--root", str(repo)]), 0, msg="\n".join(hygiene.errors))

            (repo / ".github" / "pull_request_template.md").unlink()
            self.assertEqual(hygiene.main(["--root", str(repo)]), 1)
            self.assertIn(".github/pull_request_template.md", "\n".join(hygiene.errors))

            (repo / ".github" / "pull_request_template.md").write_text("## Summary\n", encoding="utf-8")
            (repo / ".github" / "ISSUE_TEMPLATE" / "bug.yml").unlink()
            self.assertEqual(hygiene.main(["--root", str(repo)]), 1)
            self.assertIn("ISSUE_TEMPLATE", "\n".join(hygiene.errors))
            self.assertEqual(hygiene.main(["--root", str(repo), "--private"]), 0)

if __name__ == "__main__":
    unittest.main()
