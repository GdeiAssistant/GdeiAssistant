"""Run actual workflow shell blocks against success, failure and Git rename fixtures."""
import os
import subprocess
import tempfile
import unittest
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = yaml.load((ROOT / '.github/workflows/frontend-ci.yml').read_text(), Loader=yaml.BaseLoader)
GATE = WORKFLOW['jobs']['verification']['steps'][0]['run']
SCOPE = WORKFLOW['jobs']['changes']['steps'][1]['run']


class FrontendGateTests(unittest.TestCase):
    def test_only_valid_success_combinations_pass(self):
        cases = [
            ('success', 'true', 'success', 0),
            ('success', 'false', 'skipped', 0),
            ('success', 'true', 'failure', 1),
            ('success', 'true', 'skipped', 1),
            ('failure', 'false', 'skipped', 1),
            ('cancelled', 'true', 'success', 1),
            ('success', 'false', 'success', 1),
        ]
        for scope, needed, build, expected in cases:
            with self.subTest(scope=scope, needed=needed, build=build):
                result = subprocess.run(
                    ['bash', '-c', GATE], capture_output=True, text=True,
                    env=dict(os.environ, SCOPE_RESULT=scope, FRONTEND_NEEDED=needed, BUILD_RESULT=build),
                )
                self.assertEqual(result.returncode, expected, result.stderr)

    def test_frontend_move_is_checked_and_docs_only_is_skipped(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            repo = root / 'repo'
            repo.mkdir()
            scratch = root / 'scratch'
            scratch.mkdir()
            output = root / 'output'

            def git(*args):
                return subprocess.check_output(['git', '-C', str(repo), *args], text=True).strip()

            git('init', '--quiet')
            git('config', 'user.name', 'CI fixture')
            git('config', 'user.email', 'fixture@example.invalid')
            (repo / 'frontend').mkdir()
            (repo / 'frontend/a.txt').write_text('fixture')
            git('add', '.')
            git('commit', '-qm', 'fixture')
            base = git('rev-parse', 'HEAD')
            (repo / 'docs').mkdir()
            git('mv', 'frontend/a.txt', 'docs/a.txt')
            git('commit', '-qm', 'move')
            head = git('rev-parse', 'HEAD')

            def scope(before, after):
                if output.exists():
                    output.unlink()
                result = subprocess.run(
                    ['bash', '-c', SCOPE], cwd=repo, capture_output=True, text=True,
                    env=dict(os.environ, BASE_SHA=before, HEAD_SHA=after,
                             RUNNER_TEMP=str(scratch), GITHUB_OUTPUT=str(output)),
                )
                self.assertEqual(result.returncode, 0, result.stderr)
                return output.read_text().strip()

            self.assertEqual(scope(base, head), 'frontend=true')
            (repo / 'README.md').write_text('fixture')
            git('add', '.')
            git('commit', '-qm', 'docs')
            self.assertEqual(scope(head, git('rev-parse', 'HEAD')), 'frontend=false')


if __name__ == '__main__':
    unittest.main()
