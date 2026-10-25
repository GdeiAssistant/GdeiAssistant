#!/usr/bin/env python3
"""Check concrete workflow safety properties; syntax is checked by actionlint."""
from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

import yaml

UNTRUSTED = re.compile(r"\$\{\{\s*(?:github\.(?:head_ref|event\.(?:issue|comment|pull_request)\.[^}]*?(?:body|title|head\.ref)))[^}]*\}\}")


def check_entries(root: Path) -> list[str]:
    entries = ['.github/engineering.md', '.github/copilot-instructions.md', '.github/CODEOWNERS']
    result = subprocess.run(
        ['git', 'ls-files', '--error-unmatch', '--', *entries],
        cwd=root, capture_output=True, text=True, check=False,
    )
    return [] if result.returncode == 0 else ['Engineering instructions, AI entry point and CODEOWNERS must be tracked in Git.']


def validate(data: dict) -> list[str]:
    errors = []
    permissions = data.get('permissions')
    if not isinstance(permissions, dict):
        errors.append('set an explicit workflow permissions map')
    jobs = data.get('jobs', {})
    events = data.get('on', {})
    pr = 'pull_request' in events if isinstance(events, (dict, list)) else events == 'pull_request'
    for name, job in jobs.items():
        effective = job.get('permissions', permissions)
        if not isinstance(effective, dict):
            errors.append(f'{name}: permissions must be an explicit map')
        if pr and isinstance(effective, dict):
            for scope in ('contents', 'pull-requests', 'issues', 'actions', 'packages', 'deployments', 'id-token'):
                if effective.get(scope) == 'write':
                    errors.append(f'{name}: untrusted PR job requests {scope}: write')
        runner = job.get('runs-on', [])
        if pr and 'self-hosted' in runner:
            errors.append(f'{name}: untrusted PR cannot use a persistent self-hosted runner')
        steps = job.get('steps', [])
        for step in [job] + steps:
            use = step.get('uses', '')
            if use and not use.startswith('./') and not re.search(r'@[0-9a-fA-F]{40}$', use):
                errors.append(f'{name}: external actions must use a full commit SHA')
            scripts = [step.get('run', ''), step.get('with', {}).get('script', '')]
            if any(UNTRUSTED.search(s) for s in scripts):
                errors.append(f'{name}: pass external text through environment variables, not script interpolation')
            if pr and 'actions/checkout@' in use and str(step.get('with', {}).get('persist-credentials', '')).lower() != 'false':
                errors.append(f'{name}: PR checkout must disable persisted credentials')
    return errors


def main() -> int:
    failures = check_entries(Path.cwd())
    for path in sorted(Path('.github/workflows').glob('*')):
        if path.suffix not in ('.yml', '.yaml'):
            continue
        try:
            data = yaml.load(path.read_text(), Loader=yaml.BaseLoader)
            failures.extend(f'{path}: {e}' for e in validate(data or {}))
        except (OSError, yaml.YAMLError) as exc:
            failures.append(f'{path}: invalid workflow ({type(exc).__name__})')
    print('\n'.join(failures) if failures else 'Workflow safety checks passed.')
    return bool(failures)


if __name__ == '__main__':
    sys.exit(main())
