#!/usr/bin/env python3
"""Redacted history baseline and exact artifact-path scans for Gitleaks 8.30.1."""
from __future__ import annotations

import argparse
from collections import Counter
import glob
import io
import json
import os
import re
import stat
import subprocess
import tempfile
import zipfile
import zlib
from pathlib import Path

IDENTITY_FIELDS = ('RuleID', 'File', 'Commit', 'StartLine', 'Fingerprint')
BASELINE_FIELDS = set(IDENTITY_FIELDS) | {'Secret', 'Match'}
ROOT = Path(__file__).resolve().parents[2]
MAX_ARCHIVE_DEPTH = 2
MAX_ARTIFACT_ENTRIES = 10000
MAX_ARTIFACT_FILE_BYTES = 32 * 1024 * 1024
MAX_ARTIFACT_BYTES = 256 * 1024 * 1024
ARCHIVE_SUFFIXES = ('.zip', '.gz', '.tgz', '.bz2', '.xz', '.7z', '.rar', '.tar', '.zst')
ARCHIVE_MAGIC = (b'\x1f\x8b', b'BZh', b'\xfd7zXZ\x00', b'7z\xbc\xaf\x27\x1c',
                 b'Rar!', b'\x28\xb5\x2f\xfd')


def identity(finding):
    return tuple(finding[field] for field in IDENTITY_FIELDS)


def project(finding):
    return {**{field: finding[field] for field in IDENTITY_FIELDS},
            'Secret': 'REDACTED', 'Match': 'REDACTED'}


def load_baseline(path):
    baseline = json.loads(path.read_text())
    if not isinstance(baseline, list):
        raise ValueError('Invalid baseline list')
    for finding in baseline:
        if set(finding) != BASELINE_FIELDS or any(
            finding[field] != 'REDACTED' for field in ('Secret', 'Match')
        ):
            raise ValueError('Baseline must contain only redacted identity fields')
        if not re.fullmatch(r'[0-9a-f]{40}', finding['Commit']):
            raise ValueError('Baseline must identify an immutable commit')
        if not isinstance(finding['StartLine'], int) or finding['StartLine'] < 1:
            raise ValueError('Invalid baseline line')
        fingerprint = f"{finding['Commit']}:{finding['File']}:{finding['RuleID']}:{finding['StartLine']}"
        if finding['Fingerprint'] != fingerprint:
            raise ValueError('Baseline fingerprint does not match its identity')
    return baseline


def scan(binary, mode, target, report, baseline=None):
    # Gitleaks also loads a source-root ignore file regardless of its CLI flag.
    # Refuse it rather than silently accepting content-authorized exemptions.
    try:
        (Path(target) / '.gitleaksignore').lstat()
    except FileNotFoundError:
        pass
    else:
        raise RuntimeError('Source ignore files are forbidden for this scan')
    command = [binary, mode, str(target), '--config', str(ROOT / '.gitleaks.toml'),
               '--redact', '--no-banner', '--ignore-gitleaks-allow',
               '--gitleaks-ignore-path', os.devnull,
               '--report-format', 'json', '--report-path', str(report)]
    if mode == 'git':
        command.append('--log-opts=--all')
    if baseline is not None:
        command.extend(['--baseline-path', str(baseline)])
    result = subprocess.run(command, capture_output=True, check=False)
    if result.returncode not in (0, 1) or not report.is_file():
        raise RuntimeError('Gitleaks failed; no artifact may be uploaded')
    findings = json.loads(report.read_text())
    if not isinstance(findings, list) or bool(findings) != bool(result.returncode):
        raise RuntimeError('Gitleaks returned an inconsistent result')
    return findings


def write_summary(path, findings):
    path.parent.mkdir(parents=True, exist_ok=True)
    rows = [{'File': finding['File'], 'RuleID': finding['RuleID'],
             'Commit': finding.get('Commit', '')[:12]} for finding in findings]
    path.write_text(json.dumps(rows, indent=2) + '\n')


def history(binary, baseline_path, summary, create=False):
    with tempfile.TemporaryDirectory(prefix='gitleaks-', dir=os.environ.get('RUNNER_TEMP')) as directory:
        temporary = Path(directory)
        findings = scan(binary, 'git', ROOT, temporary / 'history.json')
        if create:
            if baseline_path.exists():
                raise ValueError('Refusing to replace an existing history baseline')
            baseline_path.write_text(json.dumps([project(finding) for finding in findings], indent=2) + '\n')
        known = Counter(identity(finding) for finding in load_baseline(baseline_path))
        remaining = known.copy()
        new_findings = []
        for finding in findings:
            key = identity(finding)
            if remaining[key] > 0:
                remaining[key] -= 1
            else:
                new_findings.append(finding)
        write_summary(summary, new_findings)
        print(f'History scan: {len(new_findings)} new finding(s); {len(known)} baseline identities')
        return int(bool(new_findings))


def stage_artifacts(paths, destination):
    """Bound all input and ZIP expansion; never extract attacker-controlled paths."""
    entries = 0
    total = 0

    def count_entry():
        nonlocal entries
        entries += 1
        if entries > MAX_ARTIFACT_ENTRIES:
            raise ValueError('Artifact entry limit exceeded')

    def read_bounded(stream):
        nonlocal total
        limit = min(MAX_ARTIFACT_FILE_BYTES, MAX_ARTIFACT_BYTES - total)
        data = stream.read(limit + 1)
        if len(data) > limit:
            raise ValueError('Artifact size limit exceeded')
        total += len(data)
        return data

    def inspect(data, name, depth):
        stream = io.BytesIO(data)
        if zipfile.is_zipfile(stream):
            if depth >= MAX_ARCHIVE_DEPTH:
                raise ValueError('Artifact archive depth exceeded')
            # Preserve every original byte, including headers, extra fields and
            # any prefix/suffix accepted by zipfile, under a neutral text path.
            # A newline prevents Gitleaks' ZIP magic detection from replacing
            # this byte scan with another member-only archive traversal.
            (destination / f'{entries}-archive.txt').write_bytes(b'\n' + data)
            with zipfile.ZipFile(stream) as archive:
                (destination / f'{entries}-archive-comment.txt').write_bytes(archive.comment)
                for member in archive.infolist():
                    count_entry()
                    # Scan decoded names and isolated comments as well: binary
                    # ZIP headers must not hide text or provide regex boundaries.
                    (destination / f'{entries}-name.txt').write_text(
                        member.filename, encoding='utf-8')
                    (destination / f'{entries}-member-comment.txt').write_bytes(member.comment)
                    if member.flag_bits & 1 or stat.S_ISLNK(member.external_attr >> 16):
                        raise ValueError('Encrypted or linked artifact member')
                    if member.is_dir() and member.file_size == 0:
                        continue
                    if (member.file_size > MAX_ARTIFACT_FILE_BYTES or
                            member.file_size > MAX_ARTIFACT_BYTES - total):
                        raise ValueError('Artifact member size limit exceeded')
                    with archive.open(member) as source:
                        content = read_bounded(source)  # Also verifies decompression and CRC.
                    inspect(content, member.filename, depth + 1)
            return
        if (name.lower().endswith(ARCHIVE_SUFFIXES) or data.startswith(b'PK') or
                data.startswith(ARCHIVE_MAGIC) or data[257:262] == b'ustar'):
            raise ValueError('Unsupported or invalid artifact archive')
        # Neutral names prevent archive paths from activating source/fixture allowlists.
        (destination / f'{entries}.txt').write_bytes(data)

    pending = list(paths)
    while pending:
        path = pending.pop()
        count_entry()
        mode = path.lstat().st_mode
        if stat.S_ISDIR(mode):
            with os.scandir(path) as children:
                for child in children:
                    if len(pending) + entries >= MAX_ARTIFACT_ENTRIES:
                        raise ValueError('Artifact entry limit exceeded')
                    pending.append(Path(child.path))
        elif stat.S_ISREG(mode):
            # No FIFO/device reads or symlink following, if the final path changes before opening.
            descriptor = os.open(path, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK)
            with os.fdopen(descriptor, 'rb') as source:
                if not stat.S_ISREG(os.fstat(source.fileno()).st_mode):
                    raise ValueError('Artifact file type changed')
                inspect(read_bounded(source), path.name, 0)
        else:
            raise ValueError('Unsupported artifact file type')


def artifacts(binary, patterns):
    paths = sorted({Path(match) for pattern in patterns.splitlines() if pattern.strip()
                    for match in glob.glob(pattern.strip(), recursive=True)})
    with tempfile.TemporaryDirectory(prefix='gitleaks-', dir=os.environ.get('RUNNER_TEMP')) as directory:
        staged = Path(directory) / 'payload'
        staged.mkdir()
        stage_artifacts(paths, staged)
        if paths:
            findings = scan(binary, 'dir', staged, Path(directory) / 'report.json')
            if findings:
                # Artifact and member names can themselves contain credentials.
                print('Artifact scan blocked upload: credential finding(s); File and RuleID details suppressed')
                return 1
    print(f'Artifact scan passed: {len(paths)} matching upload path(s)')
    return 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('mode', choices=('history', 'artifacts'))
    parser.add_argument('--gitleaks', default='gitleaks')
    parser.add_argument('--baseline', type=Path, default=ROOT / '.gitleaks-baseline.json')
    parser.add_argument('--summary', type=Path)
    parser.add_argument('--create-baseline', action='store_true')
    arguments = parser.parse_args()
    try:
        if arguments.mode == 'artifacts':
            return artifacts(arguments.gitleaks, os.environ['ARTIFACT_PATHS'])
        if arguments.summary is None:
            parser.error('history requires --summary')
        return history(arguments.gitleaks, arguments.baseline, arguments.summary, arguments.create_baseline)
    except (OSError, ValueError, KeyError, TypeError, RuntimeError,
            zipfile.BadZipFile, NotImplementedError, EOFError, zlib.error):
        print('Secret scan failed safely; values and raw reports suppressed')
        return 2


if __name__ == '__main__':
    raise SystemExit(main())
