"""Regression tests for versioned agent instructions and safe diagnostics."""
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
            "\n".join("## " + name for name in hygiene.AGENTS_SECTIONS),
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


if __name__ == "__main__":
    unittest.main()
