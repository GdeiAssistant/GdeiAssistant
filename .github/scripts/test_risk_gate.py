#!/usr/bin/env python3
"""Exercise trusted risk-gate JavaScript offline with synthetic API fixtures."""
import json
import os
import re
import subprocess
import unittest
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = yaml.load((ROOT / '.github/workflows/risk-gate.yml').read_text(), Loader=yaml.BaseLoader)
SHA = 'a' * 40
REPOSITORY = 'GdeiAssistant/SyntheticExample'

HARNESS = r"""
const fs = require('node:fs');
const input = JSON.parse(fs.readFileSync(0, 'utf8'));
const f = input.fixture;
const calls = [], statuses = [], failures = [];
let reads = 0;
const github = {rest: {
  pulls: {get: async args => {
    calls.push(['pulls.get', args]);
    if (f.getError) throw new Error('Synthetic PR read failure');
    return {data: ++reads > 1 && f.current ? f.current : f.pr};
  }, listFiles: 'files', listReviews: 'reviews', listCommits: 'commits'},
  issues: {listEvents: 'events'},
  repos: {createCommitStatus: async args => statuses.push(args)}
}, paginate: async (endpoint, args) => {
  calls.push([endpoint, args]);
  if (f.errors?.includes(endpoint)) throw new Error('Synthetic API failure');
  if (!Object.hasOwn(f, endpoint)) throw new Error('Unmocked API');
  return f[endpoint];
}};
const core = {setFailed: message => failures.push(message)};
const context = {repo: {owner: 'GdeiAssistant', repo: 'SyntheticExample'},
  serverUrl: 'https://example.invalid', runId: 1, payload: {pull_request: f.pr}};
const localRequire = name => {
  const value = require(process.cwd() + '/' + name);
  return name.endsWith('dependabot-policy.json') ? {...value, repo: 'GdeiAssistant/SyntheticExample'} : value;
};
(async () => {
  let error;
  try {
    await new (Object.getPrototypeOf(async function(){}).constructor)(
      'github', 'context', 'core', 'require', input.script)(github, context, core, localRequire);
  } catch (e) { error = e.message; }
  process.stdout.write(JSON.stringify({error, statuses, failures, calls}));
})();
"""


def label_event(event_id=1, event='labeled', actor='synthetic-owner'):
    return {'id': event_id, 'event': event, 'label': {'name': 'owner-approved'}, 'actor': {'login': actor}}


def review(state='APPROVED', sha=SHA):
    return {'id': 1, 'state': state, 'commit_id': sha, 'submitted_at': '2026-01-01T00:00:00Z',
            'user': {'login': 'coderabbitai[bot]', 'type': 'Bot'}}


class ScriptTests(unittest.TestCase):
    def run_script(self, fixture):
        script = next(s['with']['script'] for s in WORKFLOW['jobs']['evaluate']['steps']
                      if 'script' in s.get('with', {}))
        # Substitute only the fixed owner comparator in the offline double; never copy the owner into fixtures.
        script = re.sub(r"(latest\.actor\?\.login === )'[^']+'", r"\1'synthetic-owner'", script)
        result = subprocess.run(['node', '-e', HARNESS], input=json.dumps({
            'fixture': fixture, 'script': script,
        }), capture_output=True, text=True, check=True, timeout=10, cwd=ROOT,
            env={'PATH': os.environ['PATH'], 'PR_NUMBER': '7',
                 'DEPENDABOT_DEPENDENCIES': json.dumps(fixture.get('dependencies'))})
        return json.loads(result.stdout)

    def fixture(self, labels=True, events=None, files=None, **extra):
        files = files if files is not None else [{'filename': '.github/workflows/build.yml'}]
        return {'pr': {'number': 7, 'state': 'open', 'draft': False,
                       'user': {'login': 'synthetic-author'}, 'head': {'sha': SHA},
                       'changed_files': len(files), 'labels': [{'name': 'owner-approved'}] if labels else [],
                       'body': '## AI assistance\nCoding assistant.\n## What changed\nGate validation.\n## How verified\nOffline regression tests.\n'},
                'files': files, 'events': events if events is not None else [label_event()],
                'reviews': [], **extra}

    def assert_gate(self, fixture, allowed, template=True):
        result = self.run_script(fixture)
        states = {s['context']: s['state'] for s in result['statuses']}
        self.assertTrue(states, result)
        if allowed:
            self.assertEqual(states['risk-gate'], 'success', result)
        else:
            self.assertIn(states['risk-gate'], ('failure', 'error'), result)
        if allowed and template:
            self.assertFalse(result.get('error'), result)
            self.assertFalse(result['failures'], result)
        else:
            self.assertTrue(result.get('error') or result['failures'], result)
        return result

    def test_right_actor_passes(self):
        self.assert_gate(self.fixture(), True)

    def test_wrong_actor_fails(self):
        self.assert_gate(self.fixture(events=[label_event(actor='other')]), False)

    def test_latest_unlabeled_fails(self):
        self.assert_gate(self.fixture(events=[label_event(9, 'unlabeled'), label_event(3)]), False)

    def test_latest_wrong_actor_overrides_old_owner(self):
        self.assert_gate(self.fixture(events=[label_event(9, actor='other'), label_event(3)]), False)

    def test_no_event_fails(self):
        self.assert_gate(self.fixture(events=[]), False)

    def test_event_api_error_fails_closed_even_with_bot_approval(self):
        self.assert_gate(self.fixture(errors=['events'], reviews=[review()]), False)

    def test_unknown_event_id_fails_closed(self):
        self.assert_gate(self.fixture(events=[label_event(None)]), False)

    def test_missing_actor_fails(self):
        event = label_event()
        del event['actor']
        self.assert_gate(self.fixture(events=[event]), False)

    def test_label_absent_does_not_use_event(self):
        result = self.assert_gate(self.fixture(labels=False), False)
        self.assertNotIn('events', [name for name, _ in result['calls']])

    def test_bot_approval_without_owner_label_cannot_authorize(self):
        self.assert_gate(self.fixture(labels=False, reviews=[review()]), False)

    def test_wrong_actor_cannot_use_bot_approval(self):
        self.assert_gate(self.fixture(events=[label_event(actor='other')], reviews=[review()]), False)

    def test_coderabbit_cannot_change_gate_outcome(self):
        for labels in (True, False):
            for state in ('APPROVED', 'COMMENTED', 'CHANGES_REQUESTED', 'DISMISSED', 'PENDING'):
                with self.subTest(labels=labels, state=state):
                    result = self.assert_gate(self.fixture(labels=labels, reviews=[review(state)]), labels)
                    self.assertNotIn('reviews', [name for name, _ in result['calls']])
        self.assert_gate(self.fixture(errors=['reviews']), True)
        self.assert_gate(self.fixture(labels=False, errors=['reviews']), False)

    def test_pr_head_body_label_and_closed_races_fail_closed(self):
        for change in ({'head': {'sha': 'b' * 40}}, {'body': 'edited'}, {'labels': []}, {'state': 'closed'}):
            with self.subTest(change=change):
                fixture = self.fixture()
                fixture['current'] = dict(fixture['pr'], **change)
                self.assert_gate(fixture, False)

    def test_low_risk_still_passes(self):
        self.assert_gate(self.fixture(labels=False, files=[{'filename': 'docs/guide.md'}]), True)

    def test_renamed_high_risk_path_still_needs_owner(self):
        self.assert_gate(self.fixture(labels=False, files=[{
            'filename': 'docs/guide.md', 'previous_filename': '.github/workflows/build.yml'}]), False)

    def test_incomplete_diff_fails_closed(self):
        fixture = self.fixture()
        fixture['pr']['changed_files'] += 1
        self.assert_gate(fixture, False)

    def test_file_transport_failure_fails_closed(self):
        self.assert_gate(self.fixture(errors=['files']), False)

    def test_missing_template_still_fails(self):
        fixture = self.fixture()
        fixture['pr']['body'] = 'Incomplete description'
        result = self.assert_gate(fixture, True, template=False)
        self.assertEqual(result['statuses'][-1]['state'], 'failure')

    def dependency_fixture(self, **extra):
        fixture = self.fixture(labels=False, files=[{'filename': 'frontend/package-lock.json', 'status': 'modified'}])
        fixture['pr'].update(user={'login': 'dependabot[bot]'}, commits=1,
            base={'ref': 'master', 'repo': {'full_name': REPOSITORY}},
            head={'sha': SHA, 'repo': {'full_name': REPOSITORY}}, body='Dependabot generated description')
        fixture['commits'] = [{'sha': SHA, 'author': {'login': 'dependabot[bot]'},
            'committer': {'login': 'web-flow'}, 'commit': {'committer': {'email': '@'.join(('noreply', 'github.com'))},
                                                       'verification': {'verified': True}}}]
        fixture['dependencies'] = [{'dependencyName': 'example-library', 'packageEcosystem': 'npm_and_yarn',
            'directory': '/frontend', 'targetBranch': 'master', 'updateType': 'version-update:semver-patch',
            'maintainerChanges': False, 'prevVersion': '1.2.3', 'newVersion': '1.2.4'}]
        fixture.update(extra)
        return fixture

    def test_verified_dependabot_template_exemption_preserved(self):
        result = self.assert_gate(self.dependency_fixture(), True)
        self.assertEqual(result['statuses'][-1]['state'], 'success')

    def test_spoofed_dependabot_description_does_not_skip_template(self):
        fixture = self.dependency_fixture()
        fixture['commits'][0]['author']['login'] = 'synthetic-author'
        self.assert_gate(fixture, True, template=False)

    def test_dependency_api_failure_fails_closed(self):
        self.assert_gate(self.dependency_fixture(errors=['commits']), False)

    def test_verified_action_update_uses_master_policy(self):
        fixture = self.dependency_fixture()
        fixture['files'] = [{'filename': '.github/workflows/backend-ci.yml', 'status': 'modified',
            'additions': 1, 'deletions': 1, 'patch': '- uses: actions/checkout@' + 'a' * 40 +
                                                '\n+ uses: actions/checkout@' + 'b' * 40}]
        fixture['dependencies'] = [{'dependencyName': 'actions/checkout', 'packageEcosystem': 'github_actions',
            'directory': '/', 'targetBranch': 'master', 'updateType': 'version-update:semver-major',
            'maintainerChanges': False, 'prevVersion': '1', 'newVersion': '2'}]
        self.assert_gate(fixture, True)
        fixture['commits'][0]['commit']['verification']['verified'] = False
        self.assert_gate(fixture, False)

    def test_dependency_cannot_change_protected_workflow(self):
        fixture = self.dependency_fixture()
        fixture['files'] = [{'filename': '.github/workflows/risk-gate.yml', 'status': 'modified'}]
        self.assert_gate(fixture, False)

    def test_dependency_missing_metadata_cannot_bypass_owner(self):
        fixture = self.dependency_fixture()
        fixture['files'] = [{'filename': '.github/workflows/backend-ci.yml', 'status': 'modified',
            'additions': 1, 'deletions': 1, 'patch': '- uses: actions/checkout@' + 'a' * 40 +
                                                '\n+ uses: actions/checkout@' + 'b' * 40}]
        fixture['dependencies'] = None
        self.assert_gate(fixture, False)

    def test_master_trusted_policy_and_concurrency_are_retained(self):
        self.assertEqual(WORKFLOW['concurrency'], {
            'group': 'risk-gate-${{ github.event.pull_request.number || inputs.pr_number }}',
            'cancel-in-progress': 'true'})
        checkout, metadata = WORKFLOW['jobs']['evaluate']['steps'][:2]
        self.assertEqual(checkout['with']['ref'], '${{ github.event.repository.default_branch }}')
        self.assertEqual(checkout['with']['persist-credentials'], 'false')
        self.assertIn("pull_request.user.login == 'dependabot[bot]'", metadata['if'])
        self.assertIn('pull_request.head.repo.full_name == github.repository', metadata['if'])
        self.assertEqual(set(WORKFLOW['jobs']), {'evaluate'})

    def test_coderabbit_configuration_is_advisory(self):
        config = yaml.safe_load((ROOT / '.coderabbit.yaml').read_text())
        self.assertFalse(config['reviews']['request_changes_workflow'])
        self.assertFalse(config['reviews']['auto_approvals']['enabled'])


if __name__ == '__main__':
    unittest.main()
