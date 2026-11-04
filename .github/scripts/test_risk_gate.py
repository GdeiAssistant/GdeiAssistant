#!/usr/bin/env python3
"""Exercise the trusted risk-gate JavaScript offline with synthetic API fixtures."""
import json
import os
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
bind('pulls.listCommits', async () => ({data: f.commits || []}));
bind('repos.getContent', async () => {
  if (f.policyError) throw new Error('Synthetic policy read failure');
  const policy = f.policy || {action_files: ['.github/workflows/build.yml']};
  return {data: {type: 'file', content: Buffer.from(JSON.stringify(policy)).toString('base64')}};
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
    action: f.action || undefined,
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
        env = {'PATH': os.environ['PATH'], 'PR_NUMBER': fixture.get('prNumber', '7'),
               'APPROVAL_OWNER': fixture.get('approvalOwner', 'synthetic-owner')}
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

    def test_missing_approval_owner_blocks_high_risk(self):
        # Without the configured account nobody matches, so the label cannot be
        # trusted even when the actor name happens to look right.
        self.assert_blocked(self.fixture(files=[workflow_file('-      - run: npm test')],
                                         approvalOwner=''))

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

    def test_added_step_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False, files=[workflow_file('+      - run: echo hello')]))

    def test_added_verification_switch_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False, files=[workflow_file('+        if: false')]))

    def test_added_permissions_block_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False, files=[workflow_file('+permissions: write-all')]))

    def test_added_secret_reference_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False,
                                         files=[workflow_file('+          token: ${{ secrets.TOKEN }}')]))

    def test_comment_only_change_is_high_risk(self):
        # A `#` line inside `run: |` is shell text, and GitHub expands `${{ }}`
        # before Bash parses the comment, so no comment is provably inert.
        files = [workflow_file('-  # old note\n+  # clearer note')]
        self.assert_blocked(self.fixture(labels=False, files=files))

    def test_comment_with_an_expanded_expression_is_high_risk(self):
        files = [workflow_file('+  # ${{ fromJSON(\'"\\nexit 0"\') }}')]
        self.assert_blocked(self.fixture(labels=False, files=files))

    def test_workflow_name_change_is_high_risk(self):
        files = [workflow_file('-name: CI\n+name: Build')]
        self.assert_blocked(self.fixture(labels=False, files=files))

    def test_job_name_change_is_high_risk(self):
        # A job-level name is the required status context; renaming it can drop
        # the check the branch protection still waits for.
        self.assert_blocked(self.fixture(labels=False,
                                         files=[workflow_file('+    name: Renamed tests')]))

    def test_shell_line_that_looks_like_a_step_label_is_high_risk(self):
        # Inside `run: |` this is shell text, not a YAML step label; matching it
        # as prose would let command substitution run unreviewed.
        self.assert_blocked(self.fixture(labels=False, files=[workflow_file(
            '+      - name: $(printf executed >&2) || true')]))

    def test_schedule_removal_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False, files=[workflow_file(
            '-  schedule:\n-    - cron: \'0 0 * * *\'')]))

    def test_moving_a_workflow_out_of_the_directory_is_high_risk(self):
        files = [{'filename': 'docs/ci.yml', 'previous_filename': '.github/workflows/ci.yml',
                  'status': 'renamed', 'patch': '@@ -1 +1 @@\n-name: CI\n+name: CI'}]
        self.assert_blocked(self.fixture(labels=False, files=files))

    def test_disabling_a_workflow_by_rename_is_high_risk(self):
        files = [{'filename': '.github/workflows/ci.yml.disabled',
                  'previous_filename': '.github/workflows/ci.yml',
                  'status': 'renamed', 'patch': '@@ -1 +1 @@\n-name: CI\n+name: CI'}]
        self.assert_blocked(self.fixture(labels=False, files=files))

    def test_dependency_policy_scripts_are_high_risk(self):
        for name in '.github/scripts/dependabot-policy.cjs', '.github/scripts/dependabot-policy.json':
            with self.subTest(name=name):
                self.assert_blocked(self.fixture(labels=False, files=[{'filename': name, 'status': 'modified'}]))

    def test_runtime_configuration_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False,
                                         files=[{'filename': 'kuangmo_sms_cluster/config.py',
                                                 'status': 'modified'}]))
        self.assert_passes(self.fixture(labels=False,
                                        files=[{'filename': 'docs/configuration.md',
                                                'status': 'modified'}]))

    def test_workflow_logic_change_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False,
                                         files=[workflow_file('+      env:\n+        NODE_ENV: test')]))

    def test_removed_line_inside_a_multiline_run_is_high_risk(self):
        matches = ['-        test "$CHECK_RESULT" = "success"', '-        true', '+        true']
        self.assert_blocked(self.fixture(labels=False, files=[workflow_file('\n'.join(matches))]))

    def test_removed_non_step_setting_is_high_risk(self):
        self.assert_blocked(self.fixture(labels=False,
                                         files=[workflow_file('-          persist-credentials: false')]))

    def test_unreadable_workflow_diff_fails_closed(self):
        self.assert_blocked(self.fixture(labels=False,
                                         files=[{'filename': '.github/workflows/build.yml', 'status': 'modified'}]))

    # Diff completeness and races -----------------------------------------

    def test_incomplete_diff_fails_closed(self):
        fixture = self.fixture()
        fixture['pr']['changed_files'] += 1
        self.assert_blocked(fixture)

    def test_file_api_error_fails_closed(self):
        self.assert_blocked(self.fixture(errors=['pulls.listFiles']))

    def test_shared_head_with_another_open_pr_fails_closed(self):
        # Commit statuses are scoped to the SHA, so two PRs on one head would
        # overwrite each other's verdicts.
        self.assert_blocked(self.fixture(open=[{'number': 8, 'head': {'sha': SHA}}]))

    def test_closed_pr_publishes_no_statuses(self):
        fixture = self.fixture(labels=False)
        fixture['pr']['state'] = 'closed'
        fixture['eventName'] = 'issue_comment'
        fixture['issue'] = {'number': 7, 'pull_request': {}}
        result = self.run_script(fixture)
        self.assertFalse(result.get('error'), result)
        self.assertFalse(result['statuses'], result)

    def test_closing_a_pr_reevaluates_open_prs(self):
        fixture = self.fixture(labels=False)
        fixture['pr']['state'] = 'closed'
        fixture['eventName'] = 'pull_request_target'
        fixture['action'] = 'closed'
        fixture['open'] = [{'number': 4}]
        result = self.run_script(fixture)
        self.assertFalse(result.get('error'), result)
        self.assertFalse(result['statuses'], result)
        dispatched = [a['inputs']['pr_number'] for n, a in result['calls']
                      if n == 'actions.createWorkflowDispatch']
        self.assertEqual(dispatched, ['4'])

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
        fixture['commits'] = [{'sha': SHA, 'author': {'login': 'dependabot[bot]'},
                               'committer': {'login': 'web-flow'},
                               'commit': {'verification': {'verified': True}}}]
        fixture.update(extra)
        return fixture

    def test_authenticated_dependabot_head_skips_the_review(self):
        states = self.assert_passes(self.dependabot_fixture())
        self.assertIn('Codex review not required', states['ai-review']['description'])
        self.assertEqual(states['pr-template']['state'], 'success')

    def reviewed(self, fixture):
        fixture['pr']['body'] = TEMPLATE
        states = self.assert_passes(fixture)
        self.assertIn('Codex reviewed this head', states['ai-review']['description'])

    def test_human_push_onto_a_dependabot_branch_is_reviewed(self):
        fixture = self.dependabot_fixture()
        fixture['commits'].append({'sha': OTHER, 'author': {'login': 'synthetic-author'},
                                   'commit': {'verification': {'verified': True}}})
        self.reviewed(fixture)

    def test_unsigned_dependabot_commit_does_not_skip_the_review(self):
        fixture = self.dependabot_fixture()
        fixture['commits'][0]['commit']['verification']['verified'] = False
        self.reviewed(fixture)

    def test_signature_from_a_human_committer_does_not_skip_the_review(self):
        # A verified signature only proves some key signed; the committer must
        # be GitHub itself, otherwise an author who can set the bot's email and
        # sign with their own key would inherit the exemption.
        fixture = self.dependabot_fixture()
        fixture['commits'][0]['committer'] = {'login': 'synthetic-author'}
        self.reviewed(fixture)

    def test_missing_committer_does_not_skip_the_review(self):
        fixture = self.dependabot_fixture()
        fixture['commits'][0]['committer'] = None
        self.reviewed(fixture)

    def test_free_text_dependabot_name_is_not_trusted(self):
        fixture = self.dependabot_fixture()
        fixture['commits'][0]['author'] = None
        self.reviewed(fixture)

    def test_dependabot_with_no_commits_is_not_trusted(self):
        fixture = self.dependabot_fixture()
        fixture['commits'] = []
        self.reviewed(fixture)

    def test_verified_dependabot_action_repin_stays_low_risk(self):
        files = [workflow_file('- uses: actions/checkout@' + 'a' * 40 + '\n'
                               '+ uses: actions/checkout@' + 'b' * 40)]
        fixture = self.dependabot_fixture()
        fixture['files'] = files
        fixture['pr']['changed_files'] = 1
        self.assert_passes(fixture)

    def test_repin_swapping_in_a_different_action_needs_approval(self):
        files = [workflow_file('- uses: actions/checkout@' + 'a' * 40 + '\n'
                               '+ uses: evil/cache@' + 'b' * 40)]
        fixture = self.dependabot_fixture()
        fixture['files'] = files
        fixture['pr']['changed_files'] = 1
        self.assert_blocked(fixture)

    def test_repin_with_mismatched_pairs_needs_approval(self):
        files = [workflow_file('- uses: actions/checkout@' + 'a' * 40 + '\n'
                               '- uses: actions/setup-node@' + 'a' * 40 + '\n'
                               '+ uses: actions/checkout@' + 'b' * 40)]
        fixture = self.dependabot_fixture()
        fixture['files'] = files
        fixture['pr']['changed_files'] = 1
        self.assert_blocked(fixture)

    def test_dependabot_repin_outside_the_policy_allowlist_needs_approval(self):
        files = [workflow_file('- uses: actions/checkout@' + 'a' * 40 + '\n'
                               '+ uses: actions/checkout@' + 'b' * 40)]
        fixture = self.dependabot_fixture(policy={'action_files': ['.github/workflows/other.yml']})
        fixture['files'] = files
        fixture['pr']['changed_files'] = 1
        self.assert_blocked(fixture)

    def test_dependabot_repin_with_unreadable_policy_needs_approval(self):
        files = [workflow_file('- uses: actions/checkout@' + 'a' * 40 + '\n'
                               '+ uses: actions/checkout@' + 'b' * 40)]
        fixture = self.dependabot_fixture(policyError=True)
        fixture['files'] = files
        fixture['pr']['changed_files'] = 1
        self.assert_blocked(fixture)

    def test_dependabot_workflow_edit_beyond_a_repin_needs_approval(self):
        files = [workflow_file('- uses: actions/checkout@' + 'a' * 40 + '\n'
                               '+ uses: actions/checkout@' + 'b' * 40 + '\n'
                               '+ env:\n+   A: b')]
        fixture = self.dependabot_fixture()
        fixture['files'] = files
        fixture['pr']['changed_files'] = 1
        self.assert_blocked(fixture)

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

    def test_schedule_sweep_dispatches_each_open_pull_request(self):
        result = self.run_script({'eventName': 'schedule', 'open': [{'number': 3}]})
        dispatch = [args for name, args in result['calls'] if name == 'actions.createWorkflowDispatch']
        self.assertEqual([d['inputs']['pr_number'] for d in dispatch], ['3'])

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
