import unittest
import subprocess
import tempfile
from pathlib import Path

from check_workflows import check_entries, validate


class WorkflowSafetyTests(unittest.TestCase):
    def fixture(self, **changes):
        job = {'runs-on': 'ubuntu-latest', 'steps': [{'uses': 'actions/checkout@' + 'a' * 40, 'with': {'persist-credentials': 'false'}}]}
        job.update(changes)
        return {'on': {'pull_request': {}}, 'permissions': {'contents': 'read'}, 'jobs': {'check': job}}

    def test_read_only_fork_ci_is_allowed(self):
        self.assertEqual(validate(self.fixture()), [])

    def test_mutable_action_is_rejected(self):
        self.assertTrue(validate(self.fixture(steps=[{'uses': 'actions/checkout@v7'}])))

    def test_missing_explicit_permissions_is_rejected(self):
        data = self.fixture()
        del data['permissions']
        self.assertTrue(validate(data))

    def test_fork_write_scope_is_rejected(self):
        self.assertTrue(validate(self.fixture(permissions={'contents': 'write'})))

    def test_codeql_upload_scope_is_allowed(self):
        self.assertEqual(validate(self.fixture(permissions={'contents': 'read', 'security-events': 'write'})), [])

    def test_fork_cannot_run_on_persistent_runner(self):
        self.assertTrue(validate(self.fixture(**{'runs-on': ['self-hosted', 'linux']})))

    def test_external_text_cannot_be_interpolated_into_shell(self):
        self.assertTrue(validate(self.fixture(steps=[{'run': 'echo "${{ github.event.pull_request.title }}"'}])))

    def test_external_text_can_be_passed_as_data(self):
        self.assertEqual(validate(self.fixture(steps=[{'run': 'printf "%s" "$TITLE"', 'env': {'TITLE': '${{ github.event.pull_request.title }}'}}])), [])

    def test_fork_checkout_cannot_keep_credentials(self):
        self.assertTrue(validate(self.fixture(steps=[{'uses': 'actions/checkout@' + 'a' * 40}])))

    def test_trusted_schedule_can_write_state(self):
        data = self.fixture(permissions={'contents': 'write'})
        data['on'] = {'schedule': []}
        self.assertEqual(validate(data), [])

    def test_ignored_local_policy_is_not_a_delivery(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            subprocess.run(['git', 'init', '--quiet', directory], check=True)
            (root / '.github').mkdir()
            for name in ['engineering.md', 'copilot-instructions.md', 'CODEOWNERS']:
                (root / '.github' / name).write_text('local-only policy')
            (root / '.gitignore').write_text('.github/\n')
            self.assertTrue(check_entries(root))

    def test_tracked_policy_is_a_delivery(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            subprocess.run(['git', 'init', '--quiet', directory], check=True)
            (root / '.github').mkdir()
            for name in ['engineering.md', 'copilot-instructions.md', 'CODEOWNERS']:
                (root / '.github' / name).write_text('versioned policy')
            subprocess.run(['git', '-C', directory, 'add', '-f', '.github'], check=True)
            self.assertEqual(check_entries(root), [])


if __name__ == '__main__':
    unittest.main()
