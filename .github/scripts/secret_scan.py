#!/usr/bin/env python3
"""Redacted history baseline and exact artifact-path scans for Gitleaks 8.30.1."""
from __future__ import annotations

import argparse
import glob
import json
import os
import re
import subprocess
import tempfile
from pathlib import Path

IDENTITY_FIELDS = ('RuleID', 'File', 'Commit', 'StartLine', 'Fingerprint')
BASELINE_FIELDS = set(IDENTITY_FIELDS) | {'Secret', 'Match'}
ROOT = Path(__file__).resolve().parents[2]


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
    command = [binary, mode, str(target), '--config', str(ROOT / '.gitleaks.toml'),
               '--redact', '--no-banner', '--report-format', 'json', '--report-path', str(report)]
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
        known = {identity(finding) for finding in load_baseline(baseline_path)}
        native = temporary / 'native-baseline.json'
        native.write_text(json.dumps([
            {**finding, 'Secret': 'REDACTED', 'Match': 'REDACTED'}
            for finding in findings if identity(finding) in known
        ]))
        new_findings = scan(binary, 'git', ROOT, temporary / 'new.json', native)
        write_summary(summary, new_findings)
        print(f'History scan: {len(new_findings)} new finding(s); {len(known)} baseline identities')
        return int(bool(new_findings))


def artifacts(binary, patterns):
    paths = sorted({Path(match) for pattern in patterns.splitlines() if pattern.strip()
                    for match in glob.glob(pattern.strip(), recursive=True)})
    with tempfile.TemporaryDirectory(prefix='gitleaks-', dir=os.environ.get('RUNNER_TEMP')) as directory:
        for index, path in enumerate(paths):
            if scan(binary, 'dir', path, Path(directory) / f'{index}.json'):
                print('Artifact scan blocked upload: credential finding(s); values suppressed')
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
    except (OSError, ValueError, KeyError, TypeError, RuntimeError):
        print('Secret scan failed safely; values and raw reports suppressed')
        return 2


if __name__ == '__main__':
    raise SystemExit(main())
