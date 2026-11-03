import importlib.util
import contextlib
import io
import gzip
import zipfile
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
        for name in ('ALIBABA_ACCESS_KEY_SECRET', 'accessKeySecret', 'access_key_secret',
                     'access-key', 'accessKey'):
            with self.subTest(name=name):
                findings = self.detect(name + '=' + fake_value(30))
                self.assertIn('alibaba-access-key-secret', {row['RuleID'] for row in findings})

    def test_configuration_credentials_are_detected(self):
        for name in ('jwt.secret', 'wechat.appsecret', 'request.validate.token',
                     'PASSWORD', 'passwd', 'access-key', 'aliyunAccessKey'):
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
                                     'token=${LONG_TOKEN_VARIABLE}\ntoken=${_TOKEN_1}\n'
                                     'token=your-token-placeholder\npassword=xxxxxxxxxxxxxxxx\n'), [])

    def test_punctuation_in_password_is_detected(self):
        findings = self.detect('password=' + fake_value(16) + '&$*()!<>')
        self.assertIn('configuration-secret-value', {row['RuleID'] for row in findings})

    def test_placeholder_with_literal_text_is_detected(self):
        for value in ('${X}' + fake_value(), fake_value() + '${X}',
                      '${LONG_TOKEN_VARIABLE}' + fake_value(),
                      '${LONG_TOKEN_VARIABLE};' + fake_value(),
                      '${LONG_TOKEN_VARIABLE}#' + fake_value(),
                      '${LONG_TOKEN_VARIABLE:-' + fake_value() + '}',
                      '${LONG_TOKEN_VARIABLE}${OTHER_VARIABLE}'):
            findings = self.detect('token=' + value)
            self.assertIn('configuration-secret-value', {row['RuleID'] for row in findings})

    def test_alibaba_access_key_id_is_not_a_secret_assignment(self):
        for name in ('accessKeyId', 'access_key_id', 'ALIBABA_ACCESS_KEY_ID'):
            with self.subTest(name=name):
                findings = self.detect(name + '=' + fake_value(30))
                rules = {row['RuleID'] for row in findings}
                self.assertNotIn('alibaba-access-key-secret', rules)
                self.assertNotIn('configuration-secret-value', rules)

    def test_short_low_entropy_password_is_detected(self):
        for length in (8, 12, 15):
            with self.subTest(length=length):
                value = (''.join(secrets.SystemRandom().sample(string.ascii_lowercase, 2)) * length)[:length]
                findings = self.detect('password=' + value)
                self.assertIn('configuration-secret-value', {row['RuleID'] for row in findings})

    def test_junit_xml_credentials_block_artifacts(self):
        for prefix in ('<system-out><![CDATA[\n', '<system-out><![CDATA[',
                       '<system-err><![CDATA['):
            with self.subTest(prefix=prefix):
                value = ''.join(secrets.SystemRandom().sample(string.ascii_lowercase, 3)) * 8
                findings = self.detect(prefix + 'password=' + value + ']]></system-out>',
                                       'TEST-fixture.xml')
                self.assertIn('configuration-secret-value', {row['RuleID'] for row in findings})
                output = io.StringIO()
                with contextlib.redirect_stdout(output):
                    self.assertEqual(SCANNER.artifacts(BINARY, str(self.source / '*.xml')), 1)
                self.assertNotIn(value, output.getvalue())

    def test_junit_xml_placeholder_passes(self):
        self.assertEqual(self.detect('<system-out><![CDATA[token=${TOKEN_VARIABLE}'
                                     ']]></system-out>', 'TEST-fixture.xml'), [])

    def test_history_baseline_counts_same_identity_findings(self):
        repository = self.root / 'repository'
        subprocess.run(['git', 'init', '--quiet', str(repository)], check=True)

        def git(*arguments):
            return subprocess.run(['git', '-C', str(repository), *arguments], check=True,
                                  capture_output=True, text=True).stdout.strip()

        git('config', 'user.name', 'Synthetic Test')
        git('config', 'user.email', 'fixture@example.invalid')
        shutil.copyfile(SCANNER.ROOT / '.gitleaks.toml', repository / '.gitleaks.toml')
        # Different matches on the same line receive the same Gitleaks identity.
        (repository / 'credentials.txt').write_text(' '.join(
            f'mongodb://fixture:{fake_value()}@example.invalid/db' for _ in range(2)))
        git('add', '.')
        git('commit', '--quiet', '-m', 'test: same-line synthetic credentials')
        baseline = repository / '.gitleaks-baseline.json'
        summary = self.root / 'summary.json'
        with patch.object(SCANNER, 'ROOT', repository):
            findings = SCANNER.scan(BINARY, 'git', repository, self.output)
            matches = [row for row in findings if row['RuleID'] == 'mongodb-userinfo-password']
            self.assertEqual(len(matches), 2)
            self.assertEqual(SCANNER.identity(matches[0]), SCANNER.identity(matches[1]))
            baseline.write_text(json.dumps([SCANNER.project(row) for row in findings]))
            self.assertEqual(SCANNER.history(BINARY, baseline, summary), 0)
            # Simulate a baseline approved before a rule exposed the second match.
            findings.remove(matches[0])
            baseline.write_text(json.dumps([SCANNER.project(row) for row in findings]))
            self.assertEqual(SCANNER.history(BINARY, baseline, summary), 1)
            rows = json.loads(summary.read_text())
            self.assertEqual(len(rows), 1)
            self.assertEqual(rows[0]['RuleID'], 'mongodb-userinfo-password')

    def test_fixture_directories_still_detect_unapproved_credentials(self):
        for directory in ('src/test', 'frontend/test', 'frontend/tests'):
            with self.subTest(directory=directory):
                repository = self.root / directory.replace('/', '-')
                repository.mkdir()
                subprocess.run(['git', 'init', '--quiet', str(repository)], check=True)
                payload = repository / directory / 'credentials.properties'
                payload.parent.mkdir(parents=True)
                payload.write_text('token=fake-' + fake_value() + '\n')
                shutil.copyfile(SCANNER.ROOT / '.gitleaks.toml', repository / '.gitleaks.toml')
                with patch.object(SCANNER, 'ROOT', repository):
                    subprocess.run(['git', '-C', str(repository), 'add', '.'], check=True)
                    subprocess.run(['git', '-C', str(repository), '-c', 'user.name=Synthetic Test',
                                    '-c', 'user.email=fixture@example.invalid', 'commit', '--quiet',
                                    '-m', 'test: approved synthetic fixture'], check=True)
                    self.assertEqual(SCANNER.scan(BINARY, 'git', repository, self.output), [])
                    # Keep the default rule's entropy and word filters deterministic.
                    api_value = ''.join(character + str(index % 10) for index, character in enumerate(
                        secrets.SystemRandom().sample(string.ascii_letters, 16)))
                    payload.write_text('token=fake-' + fake_value() + '\n'
                                       'token=${X}' + fake_value() + '\n'
                                       'mongodb://fixture:' + fake_value() + '@example.invalid/db\n'
                                       'ALIBABA_ACCESS_KEY_SECRET=' + fake_value(30) + '\n'
                                       'api_key=' + api_value + '\n')
                    subprocess.run(['git', '-C', str(repository), 'add', '.'], check=True)
                    subprocess.run(['git', '-C', str(repository), '-c', 'user.name=Synthetic Test',
                                    '-c', 'user.email=fixture@example.invalid', 'commit', '--quiet',
                                    '-m', 'test: unapproved synthetic credentials'], check=True)
                    findings = SCANNER.scan(BINARY, 'git', repository, self.output)
                    expected = {'configuration-secret-value', 'mongodb-userinfo-password',
                                'alibaba-access-key-secret', 'generic-api-key'}
                    self.assertTrue(expected <= {row['RuleID'] for row in findings},
                                    {row['RuleID'] for row in findings})
                    # Directory scanning traverses relative paths before checking values.
                    result = subprocess.run(
                        [BINARY, 'dir', '.', '--config', str(repository / '.gitleaks.toml'),
                         '--redact', '--no-banner', '--report-format', 'json',
                         '--report-path', str(self.output)],
                        cwd=repository, capture_output=True, check=False)
                    self.assertEqual(result.returncode, 1)
                    findings = json.loads(self.output.read_text())
                    self.assertTrue(expected <= {row['RuleID'] for row in findings},
                                    {row['RuleID'] for row in findings})

    def test_runtime_uri_expression_exception_does_not_skip_source_file(self):
        filename = '.github/scripts/test_secret_scan.py'
        (self.source / filename).parent.mkdir(parents=True)
        # This source expression is evaluated only when the fixture is generated.
        expression = 'mongodb://fixture:' + '{fake_value()}' + '@example.invalid/db'
        self.assertEqual(self.detect(expression, filename), [])
        findings = self.detect(expression + '\n' + 'mongodb://fixture:' + fake_value()
                               + '@example.invalid/db', filename)
        self.assertIn('mongodb-userinfo-password', {row['RuleID'] for row in findings})
        # The same literal is not exempted outside the scanner regression source.
        findings = self.detect(expression, 'other.py')
        self.assertIn('mongodb-userinfo-password', {row['RuleID'] for row in findings})

    def test_clean_directory_passes(self):
        self.assertEqual(SCANNER.scan(BINARY, 'dir', self.source, self.output), [])

    def test_fake_value_outside_test_paths_is_not_allowlisted(self):
        findings = self.detect('token=fake-' + fake_value())
        self.assertTrue(findings)

    def test_artifact_findings_block_upload(self):
        value = fake_value()
        self.detect('jwt.secret=' + value)
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            self.assertEqual(SCANNER.artifacts(BINARY, str(self.source / '*.properties')), 1)
        self.assertIn('RuleID', output.getvalue())
        self.assertNotIn(value, output.getvalue())

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



class ArtifactArchiveTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.payload = 'mongodb://fixture:' + fake_value() + '@example.invalid/db'

    def zip_bytes(self, content, name='0-trace.network', comment=b'', member_comment=b''):
        buffer = io.BytesIO()
        with zipfile.ZipFile(buffer, 'w', zipfile.ZIP_DEFLATED) as archive:
            member = zipfile.ZipInfo(name)
            member.compress_type = zipfile.ZIP_DEFLATED
            member.comment = member_comment
            archive.writestr(member, content)
            archive.comment = comment
        return buffer.getvalue()

    def status(self):
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            try:
                status = SCANNER.artifacts(BINARY, str(self.root))
            except (OSError, ValueError, RuntimeError, zipfile.BadZipFile):
                status = 2
        self.assertNotIn(self.payload, output.getvalue())
        return status

    def test_compressed_trace_credential_blocks(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes(self.payload))
        self.assertEqual(self.status(), 1)

    def test_clean_compressed_trace_passes(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes('synthetic clean trace'))
        self.assertEqual(self.status(), 0)

    def test_zip_archive_comment_credential_blocks(self):
        (self.root / 'trace.zip').write_bytes(
            self.zip_bytes('clean', comment=self.payload.encode()))
        self.assertEqual(self.status(), 1)

    def test_zip_member_comment_credential_blocks(self):
        (self.root / 'trace.zip').write_bytes(
            self.zip_bytes('clean', member_comment=self.payload.encode()))
        self.assertEqual(self.status(), 1)

    def test_zip_member_filename_credential_blocks(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes('clean', name=self.payload))
        self.assertEqual(self.status(), 1)

    def test_zip_prepended_credential_blocks(self):
        (self.root / 'trace.zip').write_bytes(
            self.payload.encode() + b'\n' + self.zip_bytes('clean'))
        self.assertEqual(self.status(), 1)

    def test_zip_trailing_credential_blocks(self):
        (self.root / 'trace.zip').write_bytes(
            self.zip_bytes('clean') + b'\n' + self.payload.encode() + b'\n')
        self.assertEqual(self.status(), 1)

    def test_zip_extra_field_credential_blocks(self):
        member = zipfile.ZipInfo('clean.txt')
        # Valid unknown extra field: a tag and little-endian length precede data.
        member.extra = b'\xfe\xca' + len(self.payload).to_bytes(2, 'little') + self.payload.encode()
        with zipfile.ZipFile(self.root / 'trace.zip', 'w') as archive:
            archive.writestr(member, 'clean')
        self.assertEqual(self.status(), 1)

    def test_outer_member_comment_survives_nested_archive_staging(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes(
            self.zip_bytes('clean'), name='inner.zip', member_comment=self.payload.encode()))
        self.assertEqual(self.status(), 1)

    def test_empty_directory_metadata_credential_blocks(self):
        (self.root / 'trace.zip').write_bytes(
            self.zip_bytes('', name='directory/', member_comment=self.payload.encode()))
        self.assertEqual(self.status(), 1)

    def test_nested_zip_trailing_credential_blocks(self):
        inner = self.zip_bytes('clean') + b'\n' + self.payload.encode() + b'\n'
        (self.root / 'trace.zip').write_bytes(self.zip_bytes(inner, name='inner.zip'))
        self.assertEqual(self.status(), 1)

    def test_nested_trace_credential_blocks(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes(self.zip_bytes(self.payload), 'inner.zip'))
        self.assertEqual(self.status(), 1)

    def test_archive_disguised_as_resource_blocks(self):
        (self.root / 'resource.dat').write_bytes(self.zip_bytes(self.payload))
        self.assertEqual(self.status(), 1)

    def test_depth_limit_fails_closed(self):
        data = self.zip_bytes('clean')
        for _ in range(2):
            data = self.zip_bytes(data, 'inner.zip')
        (self.root / 'trace.zip').write_bytes(data)
        self.assertNotEqual(self.status(), 0)

    def test_entry_limit_fails_closed(self):
        buffer = io.BytesIO()
        with zipfile.ZipFile(buffer, 'w') as archive:
            for index in range(4):
                archive.writestr(str(index), 'clean')
        (self.root / 'trace.zip').write_bytes(buffer.getvalue())
        with patch.object(SCANNER, 'MAX_ARTIFACT_ENTRIES', 3, create=True):
            self.assertNotEqual(self.status(), 0)

    def test_expanded_size_limit_fails_closed(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes('x' * 4096))
        with patch.object(SCANNER, 'MAX_ARTIFACT_BYTES', 2048, create=True):
            self.assertNotEqual(self.status(), 0)

    def test_member_size_limit_fails_closed(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes('x' * 4096))
        with patch.object(SCANNER, 'MAX_ARTIFACT_FILE_BYTES', 2048, create=True):
            self.assertNotEqual(self.status(), 0)

    def test_invalid_zip_fails_closed(self):
        (self.root / 'trace.zip').write_bytes(b'not a zip')
        self.assertNotEqual(self.status(), 0)

    def test_unsupported_archive_fails_closed(self):
        (self.root / 'trace.gz').write_bytes(gzip.compress(self.payload.encode()))
        self.assertNotEqual(self.status(), 0)

    def test_symlink_fails_closed(self):
        (self.root / 'link').symlink_to(self.root / 'absent')
        self.assertNotEqual(self.status(), 0)

    def test_zip_symlink_fails_closed(self):
        info = zipfile.ZipInfo('link')
        info.create_system = 3
        info.external_attr = 0o120777 << 16
        with zipfile.ZipFile(self.root / 'trace.zip', 'w') as archive:
            archive.writestr(info, 'elsewhere')
        self.assertNotEqual(self.status(), 0)

    def test_member_allowlist_path_cannot_hide_credential(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes(self.payload, 'node_modules/payload.txt'))
        self.assertEqual(self.status(), 1)

    def test_directory_entry_payload_cannot_hide_credential(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes(self.payload, 'directory/'))
        self.assertEqual(self.status(), 1)

    def test_invalid_archive_cli_fails_without_raw_diagnostics(self):
        value = fake_value()
        (self.root / (value + '.zip')).write_bytes(b'invalid')
        result = subprocess.run(
            [os.sys.executable, str(Path(SCANNER.__file__)), 'artifacts', '--gitleaks', BINARY],
            env={**os.environ, 'ARTIFACT_PATHS': str(self.root)}, capture_output=True, text=True)
        self.assertEqual(result.returncode, 2)
        self.assertIn('failed safely', result.stdout)
        self.assertNotIn(value, result.stdout + result.stderr)
        self.assertEqual(result.stderr, '')

    def test_encrypted_archive_fails_closed(self):
        data = bytearray(self.zip_bytes('clean'))
        data[6] |= 1
        offset = data.index(b'PK\x01\x02')
        data[offset + 8] |= 1
        (self.root / 'trace.zip').write_bytes(data)
        self.assertNotEqual(self.status(), 0)

    def test_archive_member_path_is_never_extracted(self):
        (self.root / 'trace.zip').write_bytes(self.zip_bytes('clean', '../escaped.txt'))
        self.assertEqual(self.status(), 0)
        self.assertFalse((self.root.parent / 'escaped.txt').exists())

    def test_sensitive_artifact_filename_is_suppressed(self):
        value = fake_value()
        (self.root / (value + '.txt')).write_text(self.payload)
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            self.assertEqual(SCANNER.artifacts(BINARY, str(self.root)), 1)
        self.assertNotIn(value, output.getvalue())
        self.assertNotIn(self.payload, output.getvalue())

    def test_corrupt_member_fails_closed(self):
        data = bytearray(self.zip_bytes('clean'))
        # Corrupt the stored CRC in the central directory.
        offset = data.index(b'PK\x01\x02')
        data[offset + 16] ^= 1
        (self.root / 'trace.zip').write_bytes(data)
        self.assertNotEqual(self.status(), 0)

if __name__ == '__main__':
    unittest.main()
