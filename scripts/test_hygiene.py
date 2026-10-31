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


if __name__ == "__main__":
    unittest.main()
