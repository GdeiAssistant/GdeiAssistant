import importlib.util
import json
import os
import secrets
import shutil
import string
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location('secret_scan', Path(__file__).with_name('secret_scan.py'))
SCANNER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(SCANNER)
BINARY = os.environ.get('GITLEAKS_BIN', 'gitleaks')


def fake_value(length=32):
    return ''.join(secrets.choice(string.ascii_letters + string.digits) for _ in range(length))


class SecretScanTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.output = self.root / 'report.json'
        self.source = self.root / 'payload'
        self.source.mkdir()

    def detect(self, text, filename='credentials.properties'):
        (self.source / filename).write_text(text)
        return SCANNER.scan(BINARY, 'dir', self.source, self.output)

    def test_mongodb_protocols_are_detected_without_keywords(self):
        for protocol in ('mongodb', 'mongodb+srv'):
            with self.subTest(protocol=protocol):
                findings = self.detect(f'{protocol}://fixture:{fake_value()}@example.invalid/db')
                self.assertIn('mongodb-userinfo-password', {row['RuleID'] for row in findings})

    def test_alibaba_access_key_secret_is_detected(self):
        findings = self.detect('ALIBABA_ACCESS_KEY_SECRET=' + fake_value(30))
        self.assertIn('alibaba-access-key-secret', {row['RuleID'] for row in findings})

    def test_configuration_credentials_are_detected(self):
        for name in ('jwt.secret', 'wechat.appsecret', 'request.validate.token',
                     'PASSWORD', 'passwd', 'access-key'):
            with self.subTest(name=name):
                findings = self.detect(f'{name}={fake_value()}')
                self.assertIn('configuration-secret-value', {row['RuleID'] for row in findings})

    def test_yaml_and_env_assignments_are_detected(self):
        for filename, assignment in (('config.yaml', '  appsecret: "'), ('.env', 'export TOKEN="')):
            with self.subTest(filename=filename):
                findings = self.detect(assignment + fake_value() + '"', filename)
                self.assertIn('configuration-secret-value', {row['RuleID'] for row in findings})

    def test_clean_placeholders_pass(self):
        self.assertEqual(self.detect('jwt.secret=${JWT_SECRET}\npassword=\n'
                                     'token=your-token-placeholder\npassword=xxxxxxxxxxxxxxxx\n'), [])

    def test_punctuation_in_password_is_detected(self):
        findings = self.detect('password=' + fake_value(16) + '&$*()!')
        self.assertIn('configuration-secret-value', {row['RuleID'] for row in findings})

    def test_clean_directory_passes(self):
        self.assertEqual(SCANNER.scan(BINARY, 'dir', self.source, self.output), [])

    def test_fake_value_outside_test_paths_is_not_allowlisted(self):
        findings = self.detect('token=fake-' + fake_value())
        self.assertTrue(findings)

    def test_artifact_findings_block_upload(self):
        self.detect('jwt.secret=' + fake_value())
        self.assertEqual(SCANNER.artifacts(BINARY, str(self.source / '*.properties')), 1)

    def test_empty_artifact_glob_passes_without_scanning_workspace(self):
        self.detect('jwt.secret=' + fake_value())
        self.assertEqual(SCANNER.artifacts(BINARY, str(self.source / '*.absent')), 0)

    def test_missing_binary_fails(self):
        with self.assertRaises(OSError):
            SCANNER.scan(str(self.root / 'missing'), 'dir', self.source, self.output)

    def test_unredacted_baseline_is_rejected(self):
        self.output.write_text(json.dumps([{'Secret': fake_value()}]))
        with self.assertRaises(ValueError):
            SCANNER.load_baseline(self.output)

    def test_history_baseline_passes_and_new_remote_commit_fails(self):
        repository = self.root / 'repository'
        subprocess.run(['git', 'init', '--quiet', '-b', 'security/leak-prevention-20261101', str(repository)], check=True)

        def git(*arguments):
            return subprocess.run(['git', '-C', str(repository), *arguments], check=True,
                                  capture_output=True, text=True).stdout.strip()

        git('config', 'user.name', 'Synthetic Test')
        git('config', 'user.email', 'fixture@example.invalid')
        shutil.copyfile(SCANNER.ROOT / '.gitleaks.toml', repository / '.gitleaks.toml')
        payload = repository / 'credentials.properties'
        payload.write_text('jwt.secret=' + fake_value())
        git('add', '.')
        git('commit', '--quiet', '-m', 'test: synthetic historical fixture')
        old_commit = git('rev-parse', 'HEAD')
        baseline = repository / '.gitleaks-baseline.json'
        summary = self.root / 'summary.json'
        with patch.object(SCANNER, 'ROOT', repository):
            self.assertEqual(SCANNER.history(BINARY, baseline, summary, create=True), 0)
            self.assertTrue(SCANNER.load_baseline(baseline))
            self.assertTrue(SCANNER.scan(BINARY, 'git', repository, self.output, baseline))
            self.assertEqual(SCANNER.history(BINARY, baseline, summary), 0)
            payload.write_text('jwt.secret=' + fake_value())
            git('add', 'credentials.properties')
            git('commit', '--quiet', '-m', 'test: new synthetic credential')
            git('update-ref', 'refs/remotes/origin/security/leak-prevention-20261101', 'HEAD')
            git('checkout', '--quiet', '--detach', old_commit)
            self.assertEqual(SCANNER.history(BINARY, baseline, summary), 1)
        rows = json.loads(summary.read_text())
        self.assertTrue(rows)
        self.assertTrue(all(set(row) == {'File', 'RuleID', 'Commit'} for row in rows))


if __name__ == '__main__':
    unittest.main()
