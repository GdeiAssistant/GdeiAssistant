#!/usr/bin/env python3
"""Exercise the trusted risk-gate JavaScript offline with synthetic API fixtures."""
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
OTHER = 'b' * 40
TEMPLATE = ('## AI assistance\nCoding assistant drafted the change.\n'
            '## What changed\nUnified the risk gate.\n'
            '## How verified\nOffline regression tests.\n')

HARNESS = r"""
const fs = require('node:fs');
const input = JSON.parse(fs.readFileSync(0, 'utf8'));
const f = input.fixture;
const calls = [], statuses = [], failures = [];
let reads = 0;
const rest = {};
const bind = (name, handler) => {
  const wrapped = async args => {
    calls.push([name, args]);
    return handler(args);
  };
  wrapped.__name = name;
  const [group, method] = name.split('.');
  rest[group] = rest[group] || {};
  rest[group][method] = wrapped;
};
bind('pulls.get', async () => {
  if (f.errors?.includes('pulls.get')) throw new Error('Synthetic PR read failure');
  return {data: ++reads > 1 && f.current ? f.current : f.pr};
});
bind('pulls.listFiles', async () => ({data: f.files || []}));
bind('pulls.list', async () => ({data: f.open || []}));
bind('issues.listEvents', async () => ({data: f.events || []}));
bind('issues.listComments', async () => ({data: f.comments || []}));
bind('repos.getBranch', async () => ({data: {commit: {sha: f.baseHead ?? (f.pr || {}).head?.sha}}}));
bind('repos.compareCommits', async args => ({data: {merge_base_commit: {sha: f.mergeBase ?? args.base}}}));
bind('repos.getCommit', async () => ({data: {
  author: f.headAuthor === null ? null : {login: f.headAuthor ?? 'synthetic-author'},
  commit: {author: {name: f.headAuthorName ?? 'Synthetic Author'}},
}}));
bind('repos.getContent', async args => {
  if (f.contents && Object.hasOwn(f.contents, args.path)) {
    return {data: {content: Buffer.from(f.contents[args.path]).toString('base64')}};
  }
  throw new Error('Not Found');
});
bind('repos.createCommitStatus', async args => {
  statuses.push(args);
  return {data: {}};
});
bind('actions.createWorkflowDispatch', async () => ({data: {}}));
const github = {
  rest,
  paginate: async (endpoint, args = {}) => {
    if (f.errors?.includes(endpoint.__name)) throw new Error('Synthetic API failure');
    return (await endpoint(args)).data;
  },
};
const core = {setFailed: message => failures.push(message), info: () => {}};
const context = {
  repo: {owner: 'GdeiAssistant', repo: 'SyntheticExample'},
  serverUrl: 'https://example.invalid',
  runId: 1,
  eventName: f.eventName || 'pull_request_target',
  payload: {
    pull_request: f.pr || undefined,
    issue: f.issue || undefined,
    repository: {default_branch: f.defaultBranch || 'master'},
  },
};
(async () => {
  let error;
  try {
    await new (Object.getPrototypeOf(async function(){}).constructor)(
      'github', 'context', 'core', input.script)(github, context, core);
  } catch (e) { error = e.message; }
  process.stdout.write(JSON.stringify({error, statuses, failures, calls}));
})();
"""


def label_event(event_id=1, name='owner-approved', event='labeled', actor='synthetic-owner'):
    return {'id': event_id, 'event': event, 'label': {'name': name}, 'actor': {'login': actor}}


def receipt(sha=SHA, status='completed', findings=False, updated='2026-01-02T00:00:00Z',
            login='chatgpt-codex-connector[bot]'):
    body = ('Rolling Codex summary.\n<!-- codex-review-state:v1 '
            f'{{"code_review":{{"status":"{status}","commit_sha":"{sha}",'
            f'"has_unresolved_findings":{str(findings).lower()}}}}} -->\n')
    return {'user': {'login': login}, 'body': body, 'updated_at': updated}


def workflow_file(patch, name='.github/workflows/build.yml'):
    return {'filename': name, 'status': 'modified', 'patch': '@@ -1,4 +1,4 @@\n' + patch}


class ScriptTests(unittest.TestCase):
    def run_script(self, fixture):
        script = next(s['with']['script'] for s in WORKFLOW['jobs']['evaluate']['steps']
                      if 'script' in s.get('with', {}))
        # Substitute only the fixed owner comparator in the offline double; never copy the owner into fixtures.
        script = re.sub(r"(latest\.actor\?\.login === )'[^']+'", r"\1'synthetic-owner'", script)
        env = {'PATH': os.environ['PATH'], 'PR_NUMBER': fixture.get('prNumber', '7')}
        result = subprocess.run(['node', '-e', HARNESS], input=json.dumps({
            'fixture': fixture, 'script': script,
        }), capture_output=True, text=True, check=True, timeout=10, cwd=ROOT, env=env)
        return json.loads(result.stdout)

    def evaluate(self, fixture):
        result = self.run_script(fixture)
        states = {s['context']: s for s in result['statuses']}
        self.assertIn('risk-gate', states, result)
        return result, states

    def fixture(self, labels=True, events=None, files=None, **extra):
        files = files if files is not None else [{'filename': 'docs/guide.md', 'status': 'modified'}]
        return {
            'pr': {'number': 7, 'state': 'open', 'draft': False,
                   'user': {'login': 'synthetic-author'}, 'head': {'sha': SHA},
                   'base': {'ref': 'master'}, 'changed_files': len(files),
                   'labels': [{'name': 'owner-approved'}] if labels else [],
                   'body': TEMPLATE},
            'files': files,
            'events': events if events is not None else [label_event()],
            'comments': [receipt()],
            **extra,
        }

    def assert_passes(self, fixture, state='success'):
        result, states = self.evaluate(fixture)
        self.assertEqual(states['risk-gate']['state'], state, result)
        self.assertFalse(result.get('error'), result)
        self.assertFalse(result['failures'], result)
        return states

    def assert_blocked(self, fixture):
        result, states = self.evaluate(fixture)
        self.assertIn(states['risk-gate']['state'], ('failure', 'error'), result)
        self.assertTrue(result.get('error') or result['failures'], result)
        return states

    # Risk classification -------------------------------------------------

    def test_low_risk_path_passes_without_approval(self):
        fixture = self.fixture(labels=False)
        states = self.assert_passes(fixture)
        self.assertIn('Low-risk paths', states['risk-gate']['description'])
        self.assertNotIn('issues.listEvents', [name for name, _ in self.run_script(fixture)['calls']])

    def test_high_risk_without_approval_fails(self):
        fixture = self.fixture(labels=False, files=[workflow_file('-      - run: npm test')])
        states = self.assert_blocked(fixture)
        self.assertIn('needs-owner-approval', states['risk-gate']['description'])

    def test_owner_approval_clears_high_risk(self):
        states = self.assert_passes(self.fixture(files=[workflow_file('-      - run: npm test')]))
        self.assertIn('owner-approved label verified', states['risk-gate']['description'])

    def test_agent_approval_clears_high_risk_and_says_it_is_not_owner_confirmed(self):
        fixture = self.fixture(labels=False, files=[workflow_file('-      - run: npm test')],
                               events=[label_event(name='agent-approved')])
        fixture['pr']['labels'] = [{'name': 'agent-approved'}]
        states = self.assert_passes(fixture)
        self.assertIn('agent-approved', states['risk-gate']['description'])
        self.assertIn('NOT confirmed by the owner', states['risk-gate']['description'])

    def test_owner_approval_outranks_agent_approval(self):
        fixture = self.fixture(files=[workflow_file('-      - run: npm test')],
                               events=[label_event(1, 'agent-approved'), label_event(2, 'owner-approved')])
        fixture['pr']['labels'] = [{'name': 'agent-approved'}, {'name': 'owner-approved'}]
        states = self.assert_passes(fixture)
        self.assertIn('owner-approved label verified', states['risk-gate']['description'])

    def test_wrong_actor_fails(self):
        self.assert_blocked(self.fixture(files=[workflow_file('-      - run: npm test')],
                                         events=[label_event(actor='other')]))

    def test_latest_unlabel_revokes_approval(self):
        self.assert_blocked(self.fixture(files=[workflow_file('-      - run: npm test')],
                                         events=[label_event(9, event='unlabeled'), label_event(3)]))

    def test_missing_actor_fails_closed(self):
        event = label_event()
        del event['actor']
        self.assert_blocked(self.fixture(files=[workflow_file('-      - run: npm test')], events=[event]))

    def test_renamed_high_risk_path_needs_approval(self):
        self.assert_blocked(self.fixture(labels=False, files=[{
            'filename': 'docs/guide.md', 'previous_filename': '.github/workflows/build.yml'}]))

    def test_protected_workflow_is_always_high_risk(self):
        for name in '.github/workflows/risk-gate.yml', '.github/workflows/dependabot-policy.yml':
            with self.subTest(name=name):
                self.assert_blocked(self.fixture(labels=False, files=[{'filename': name, 'status': 'modified'}]))

    # Workflow diff rules -------------------------------------------------

    def test_removed_step_line_is_high_risk_even_when_net_steps_grow(self):
        files = [workflow_file('-      - run: npm test\n'
                               '+      - run: npm run lint\n'
                               '+      - run: npm test')]
        self.assert_blocked(self.fixture(labels=False, files=files))

    def test_pure_addition_in_a_plain_workflow_stays_low_risk(self):
        files = [workflow_file('+      - run: echo hello')]
        fixture = self.fixture(labels=False, files=files, contents={files[0]['filename']: 'name: build\non: push\n'})
        states = self.assert_passes(fixture)
        self.assertIn('Low-risk paths', states['risk-gate']['description'])

    def test_added_permissions_block_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False, files=[workflow_file('+permissions: write-all')]))

    def test_added_secret_reference_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False,
                                         files=[workflow_file('+          token: ${{ secrets.TOKEN }}')]))

    def test_cosmetic_only_change_does_not_read_workflow_content(self):
        files = [workflow_file('-  timeout-minutes: 5\n+  timeout-minutes: 10')]
        result = self.run_script(self.fixture(labels=False, files=files))
        states = {s['context']: s['state'] for s in result['statuses']}
        self.assertEqual(states['risk-gate'], 'success', result)
        self.assertNotIn('repos.getContent', [name for name, _ in result['calls']])

    def test_logic_change_in_a_credential_workflow_is_high_risk(self):
        files = [workflow_file('+      env:\n+        NODE_ENV: test')]
        content = ('name: build\non: push\njobs:\n  test:\n    steps:\n'
                   '      - run: curl -H "Authorization: ${{ secrets.TOKEN }}"\n')
        self.assert_blocked(self.fixture(labels=False, files=files,
                                         contents={files[0]['filename']: content}))

    def test_logic_change_in_a_plain_workflow_stays_low_risk(self):
        files = [workflow_file('+      env:\n+        NODE_ENV: test')]
        self.assert_passes(self.fixture(labels=False, files=files,
                                        contents={files[0]['filename']: 'name: build\non: push\n'}))

    def test_unreadable_workflow_content_fails_closed(self):
        self.assert_blocked(self.fixture(labels=False, files=[workflow_file('+      env:\n+        A: b')]))

    # Diff completeness and races -----------------------------------------

    def test_incomplete_diff_fails_closed(self):
        fixture = self.fixture()
        fixture['pr']['changed_files'] += 1
        self.assert_blocked(fixture)

    def test_file_api_error_fails_closed(self):
        self.assert_blocked(self.fixture(errors=['pulls.listFiles']))

    def test_pr_mutation_during_evaluation_fails_closed(self):
        for change in ({'head': {'sha': OTHER}}, {'body': 'edited'}, {'labels': []}):
            with self.subTest(change=change):
                fixture = self.fixture()
                fixture['current'] = dict(fixture['pr'], **change)
                self.assert_blocked(fixture)

    # Codex receipt -------------------------------------------------------

    def test_missing_receipt_keeps_the_gate_pending(self):
        states = self.assert_passes(self.fixture(comments=[]), state='pending')
        self.assertIn('No Codex review yet', states['ai-review']['description'])

    def test_unfinished_review_keeps_the_gate_pending(self):
        states = self.assert_passes(self.fixture(comments=[receipt(status='in_progress')]), state='pending')

    def test_unparsable_receipt_fails_closed(self):
        comment = {'user': {'login': 'chatgpt-codex-connector[bot]'},
                   'body': '<!-- codex-review-state:v1 {not json} -->',
                   'updated_at': '2026-01-02T00:00:00Z'}
        self.assert_blocked(self.fixture(comments=[comment]))

    def test_receipt_for_another_head_fails(self):
        self.assert_blocked(self.fixture(comments=[receipt(sha=OTHER)]))

    def test_unresolved_findings_fail(self):
        self.assert_blocked(self.fixture(comments=[receipt(findings=True)]))

    def test_receipt_from_another_author_is_ignored(self):
        states = self.assert_passes(self.fixture(comments=[receipt(login='synthetic-author')]), state='pending')

    def test_newest_receipt_wins(self):
        fixture = self.fixture(comments=[receipt(sha=OTHER, updated='2026-01-01T00:00:00Z'),
                                         receipt(updated='2026-01-03T00:00:00Z')])
        self.assert_passes(fixture)

    def test_base_branch_advance_blocks_the_review(self):
        fixture = self.fixture(baseHead='c' * 40, mergeBase=OTHER)
        states = self.assert_blocked(fixture)
        self.assertIn('Base branch advanced', states['ai-review']['description'])
        self.assertNotIn('issues.listComments', [name for name, _ in self.run_script(fixture)['calls']])

    def test_head_containing_base_passes(self):
        self.assert_passes(self.fixture(baseHead='c' * 40, mergeBase='c' * 40))

    # Dependabot ----------------------------------------------------------

    def dependabot_fixture(self, **extra):
        fixture = self.fixture(labels=False, files=[{'filename': 'package-lock.json', 'status': 'modified'}])
        fixture['pr'].update(user={'login': 'dependabot[bot]'}, body='Dependabot generated description')
        fixture.update({'headAuthor': 'dependabot[bot]', 'headAuthorName': 'dependabot[bot]'})
        fixture.update(extra)
        return fixture

    def test_authenticated_dependabot_head_skips_the_review(self):
        states = self.assert_passes(self.dependabot_fixture())
        self.assertIn('Codex review not required', states['ai-review']['description'])
        self.assertEqual(states['pr-template']['state'], 'success')

    def test_human_push_onto_a_dependabot_branch_is_reviewed(self):
        fixture = self.dependabot_fixture(headAuthor='synthetic-author')
        fixture['pr']['body'] = TEMPLATE
        states = self.assert_passes(fixture)
        self.assertIn('Codex reviewed this head', states['ai-review']['description'])

    def test_free_text_dependabot_name_is_not_trusted(self):
        fixture = self.dependabot_fixture(headAuthor=None)
        fixture['pr']['body'] = TEMPLATE
        states = self.assert_passes(fixture)
        self.assertIn('Codex reviewed this head', states['ai-review']['description'])

    # Template ------------------------------------------------------------

    def test_incomplete_template_fails(self):
        fixture = self.fixture()
        fixture['pr']['body'] = 'Incomplete description'
        result, states = self.evaluate(fixture)
        self.assertEqual(states['pr-template']['state'], 'failure')
        self.assertEqual(states['risk-gate']['state'], 'success')
        self.assertTrue(result['failures'])

    # Workflow shape ------------------------------------------------------

    def test_trusted_trigger_and_minimal_permissions(self):
        self.assertEqual(WORKFLOW['permissions'], {
            'contents': 'read', 'pull-requests': 'read', 'issues': 'read',
            'statuses': 'write', 'actions': 'write'})
        trigger = WORKFLOW['on']
        self.assertIn('pull_request_target', trigger)
        self.assertIn('workflow_dispatch', trigger)
        self.assertEqual(set(WORKFLOW['jobs']), {'evaluate'})
        job = WORKFLOW['jobs']['evaluate']
        self.assertEqual(job['timeout-minutes'], '5')
        self.assertEqual(job['concurrency']['group'],
                         'risk-gate-${{ github.event.pull_request.number || '
                         'github.event.issue.number || inputs.pr_number }}')
        self.assertEqual(job['concurrency']['cancel-in-progress'], 'true')

    def test_gate_runs_no_repository_code(self):
        steps = WORKFLOW['jobs']['evaluate']['steps']
        self.assertEqual(len(steps), 1)
        self.assertTrue(all(step.get('uses', '').startswith('actions/github-script@') for step in steps))

    def test_push_sweep_dispatches_each_open_pull_request(self):
        result = self.run_script({'eventName': 'push', 'open': [{'number': 1}, {'number': 2}]})
        self.assertFalse(result.get('error'), result)
        dispatches = [args for name, args in result['calls'] if name == 'actions.createWorkflowDispatch']
        self.assertEqual([d['inputs']['pr_number'] for d in dispatches], ['1', '2'])
        for dispatch in dispatches:
            self.assertEqual(dispatch['ref'], 'master')
            self.assertEqual(dispatch['workflow_id'], 'risk-gate.yml')

    def test_invalid_pull_request_number_is_rejected(self):
        result = self.run_script({'pr': None, 'prNumber': 'not-a-number'})
        self.assertIn('Invalid PR number', result['error'])
        self.assertFalse(result['statuses'], result)


if __name__ == '__main__':
    unittest.main()
