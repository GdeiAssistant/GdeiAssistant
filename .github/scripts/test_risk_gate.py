#!/usr/bin/env python3
"""Execute the real github-script bodies with offline GitHub API doubles."""
import json
import os
import subprocess
import unittest
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = yaml.load((ROOT / '.github/workflows/risk-gate.yml').read_text(), Loader=yaml.BaseLoader)
SHA = 'a' * 40

# Only explicitly mocked endpoints exist; no Octokit client or network is used.
HARNESS = r'''
const fs = require('fs');
const input = JSON.parse(fs.readFileSync(0, 'utf8'));
const f = input.fixture;
const calls = [], statuses = [], comments = [], failures = [];
let prReads = 0, reviewReads = 0;
const github = {rest: {
  pulls: {get: async args => {
    calls.push(['pulls.get', args]);
    if (f.getError) throw new Error('mock PR API failure');
    if (input.job === 'targets') return {data: f.prs[String(args.pull_number)]};
    return {data: ++prReads > 1 && f.current ? f.current : f.pr};
  }, listFiles: 'files', listReviews: 'reviews', list: 'open'},
  issues: {listEvents: 'events', listComments: 'comments', createComment: async args => comments.push(args)},
  repos: {listPullRequestsAssociatedWithCommit: 'associated', get: async () => ({data: {stargazers_count: 0}}),
    createCommitStatus: async args => statuses.push(args)}
}, paginate: async (endpoint, args) => {
  calls.push([endpoint, args]);
  if (f.errors?.includes(endpoint)) throw new Error('mock ' + endpoint + ' API failure');
  if (endpoint === 'reviews') return f.reviewReads ? f.reviewReads[reviewReads++] : (f.reviews || []);
  if (endpoint === 'comments') return [];
  if (!Object.hasOwn(f, endpoint)) throw new Error('Unmocked API: ' + endpoint);
  return f[endpoint];
}};
const core = {setFailed: message => failures.push(message), warning: () => {}};
const context = {repo: {owner: 'example', repo: 'project'}, serverUrl: 'https://example.invalid',
  runId: 1, eventName: input.event || 'pull_request_target', payload: f.payload || {pull_request: f.pr}};
(async () => {
  let result, error;
  try {
    result = await new (Object.getPrototypeOf(async function(){}).constructor)('github', 'context', 'core', input.script)(github, context, core);
  } catch (e) { error = e.message; }
  process.stdout.write(JSON.stringify({result, error, statuses, comments, failures, calls}));
})();
'''


def label_event(event_id=1, event='labeled', actor='Pigbibi'):
    return {'id': event_id, 'event': event, 'label': {'name': 'owner-approved'}, 'actor': {'login': actor}}


def review(state='APPROVED', sha=SHA, review_id=1):
    return {'id': review_id, 'state': state, 'commit_id': sha,
            'submitted_at': '2026-01-01T00:00:00Z', 'user': {'login': 'coderabbitai[bot]', 'type': 'Bot'}}


class ScriptTests(unittest.TestCase):
    def run_script(self, fixture, job='evaluate', event=None):
        script = WORKFLOW['jobs'][job]['steps'][0]['with']['script']
        result = subprocess.run(['node', '-e', HARNESS], input=json.dumps({
            'fixture': fixture, 'script': script, 'job': job, 'event': event,
        }), capture_output=True, text=True, check=True, timeout=10,
            env={'PATH': os.environ['PATH'], 'PR_NUMBER': '7'})
        return json.loads(result.stdout)

    def fixture(self, labels=True, events=None, files=None, reviews=None, **extra):
        files = files if files is not None else [{'filename': '.github/workflows/build.yml'}]
        return {'pr': {'number': 7, 'state': 'open', 'head': {'sha': SHA}, 'changed_files': len(files),
                       'labels': [{'name': 'owner-approved'}] if labels else [],
                       'body': '## AI assistance\nCoding assistant.\n## What changed\nGate validation.\n## How verified\nOffline regression tests.\n'},
                'files': files, 'events': events if events is not None else [label_event()],
                'reviews': reviews or [], **extra}

    def assert_gate(self, fixture, allowed):
        result = self.run_script(fixture)
        states = [s['state'] for s in result['statuses'] if s['context'] == 'risk-gate']
        self.assertTrue(states, result)
        if allowed:
            self.assertEqual(states[-1], 'success', result)
            self.assertFalse(result.get('error'), result)
            self.assertFalse(result['failures'], result)
        else:
            self.assertIn(states[-1], ('failure', 'error'), result)
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
        self.assert_gate(self.fixture(labels=False), False)

    def test_bot_approval_without_label_passes(self):
        result = self.assert_gate(self.fixture(labels=False, reviews=[review()]), True)
        self.assertNotIn('events', [name for name, _ in result['calls']])

    def test_wrong_actor_can_still_use_valid_bot_approval(self):
        self.assert_gate(self.fixture(events=[label_event(actor='other')], reviews=[review()]), True)

    def test_stale_bot_approval_fails(self):
        self.assert_gate(self.fixture(labels=False, reviews=[review(sha='b' * 40)]), False)

    def test_latest_nonapproval_blocks_earlier_bot_approval(self):
        for state in ('COMMENTED', 'CHANGES_REQUESTED', 'DISMISSED'):
            with self.subTest(state=state):
                self.assert_gate(self.fixture(labels=False, reviews=[review(), review(state, review_id=2)]), False)

    def test_review_race_fails_closed(self):
        self.assert_gate(self.fixture(labels=False, reviewReads=[[review()], [review('DISMISSED')]]), False)

    def test_unknown_review_metadata_fails_closed(self):
        self.assert_gate(self.fixture(labels=False, reviews=[review(review_id=None)]), False)

    def test_review_api_error_fails_closed(self):
        self.assert_gate(self.fixture(labels=False, errors=['reviews']), False)

    def test_pr_head_race_fails_closed(self):
        fixture = self.fixture()
        fixture['current'] = dict(fixture['pr'], head={'sha': 'b' * 40})
        self.assert_gate(fixture, False)

    def test_low_risk_still_passes(self):
        self.assert_gate(self.fixture(labels=False, files=[{'filename': 'docs/guide.md'}]), True)

    def test_incomplete_diff_fails_closed(self):
        fixture = self.fixture()
        fixture['pr']['changed_files'] += 1
        self.assert_gate(fixture, False)

    def test_only_evaluator_gains_issue_read_permission(self):
        self.assertEqual(WORKFLOW['permissions'], {})
        self.assertEqual(WORKFLOW['jobs']['evaluate']['permissions'], {
            'pull-requests': 'read', 'issues': 'read', 'statuses': 'write'})
        self.assertEqual(WORKFLOW['jobs']['targets']['permissions'], {'pull-requests': 'read'})


class TargetTests(unittest.TestCase):
    run_script = ScriptTests.run_script

    def targets(self, associated=None, payload_prs=None, sha=SHA, **extra):
        fixture = {'payload': {'workflow_run': {'head_sha': sha, 'pull_requests': payload_prs or []}},
                   'associated': associated if associated is not None else [{'number': 7}],
                   'open': [{'number': 7}, {'number': 8}],
                   'prs': {'7': {'number': 7, 'state': 'open', 'head': {'sha': SHA}},
                           '8': {'number': 8, 'state': 'open', 'head': {'sha': SHA}}}, **extra}
        selected = self.run_script(fixture, job='targets', event='workflow_run')
        if selected.get('result', 'error') is None and not selected.get('error'):
            scanned = self.run_script(fixture, job='scan', event='workflow_run')
            scanned['calls'] = selected['calls'] + scanned['calls']
            return scanned
        return selected

    def test_fork_without_payload_association_uses_commit_lookup(self):
        result = self.targets()
        self.assertEqual(result['result'], [7], result)
        self.assertNotIn('open', [name for name, _ in result['calls']])
        lookup = [args for name, args in result['calls'] if name == 'associated']
        self.assertEqual(lookup, [{'owner': 'example', 'repo': 'project', 'commit_sha': SHA, 'per_page': 100}])

    def test_shared_head_and_payload_candidates_are_deduplicated(self):
        result = self.targets(associated=[{'number': 7}, {'number': 8}, {'number': 7}],
                              payload_prs=[{'number': 8}, {'number': 7}])
        self.assertEqual(result['result'], [7, 8], result)
        reads = [args['pull_number'] for name, args in result['calls'] if name == 'pulls.get']
        self.assertEqual(sorted(reads), [7, 8])

    def test_payload_adds_fork_missing_from_commit_lookup(self):
        self.assertEqual(self.targets(associated=[{'number': 7}], payload_prs=[{'number': 8}])['result'], [7, 8])

    def test_no_candidates_falls_back_to_full_scan(self):
        self.assertEqual(self.targets(associated=[])['result'], [7, 8])

    def test_lookup_api_error_falls_back_even_with_payload(self):
        result = self.targets(errors=['associated'], payload_prs=[{'number': 7}])
        self.assertEqual(result['result'], [7, 8])
        self.assertIn('open', [name for name, _ in result['calls']])

    def test_missing_or_unknown_sha_falls_back(self):
        for sha in (None, '', 'not-a-sha'):
            with self.subTest(sha=sha):
                self.assertEqual(self.targets(sha=sha)['result'], [7, 8])

    def test_unknown_candidate_number_falls_back(self):
        self.assertEqual(self.targets(associated=[{'number': '7'}])['result'], [7, 8])

    def test_candidate_read_error_falls_back(self):
        self.assertEqual(self.targets(getError=True)['result'], [7, 8])

    def test_unknown_candidate_state_falls_back(self):
        result = self.targets(prs={'7': {'number': 7, 'state': None}})
        self.assertEqual(result['result'], [7, 8])

    def test_all_closed_candidates_fall_back(self):
        self.assertEqual(self.targets(prs={'7': {'number': 7, 'state': 'closed'}})['result'], [7, 8])

    def test_closed_candidate_is_excluded_when_an_open_candidate_resolves(self):
        result = self.targets(associated=[{'number': 7}, {'number': 8}], prs={
            '7': {'number': 7, 'state': 'closed'}, '8': {'number': 8, 'state': 'open'}})
        self.assertEqual(result['result'], [8])

    def test_full_scan_keeps_256_cap(self):
        for count in (256, 257):
            with self.subTest(count=count):
                result = self.targets(associated=[], open=[{'number': n + 1} for n in range(count)])
                if count == 256:
                    self.assertEqual(len(result['result']), 256)
                else:
                    self.assertIn('Too many PRs', result['error'])
                    self.assertNotIn('result', result)

    def test_resolved_candidates_keep_256_cap(self):
        result = self.targets(associated=[{'number': n + 1} for n in range(257)],
                              open=[{'number': n + 1} for n in range(257)])
        self.assertIn('Too many PRs', result['error'])

    def test_fallback_api_error_does_not_return_empty_success(self):
        result = self.targets(associated=[], errors=['open'])
        self.assertIn('API failure', result['error'])
        self.assertNotIn('result', result)

    def test_overlapping_target_scans_serialize(self):
        concurrency = WORKFLOW['jobs']['scan']['concurrency']
        self.assertEqual(concurrency['group'], 'risk-gate-targets')
        self.assertEqual(concurrency['cancel-in-progress'], 'false')

    def test_only_full_scans_share_the_serial_group(self):
        self.assertNotIn('concurrency', WORKFLOW['jobs']['targets'])
        self.assertEqual(WORKFLOW['jobs']['scan']['if'], "needs.targets.outputs.numbers == 'null'")
        self.assertEqual(WORKFLOW['jobs']['scan']['permissions'], {'pull-requests': 'read'})
        selected = self.run_script({'payload': {'workflow_run': {'head_sha': SHA}},
                                    'associated': []}, job='targets', event='workflow_run')
        self.assertIsNone(selected['result'])
        # The evaluator must run for a skipped scan (resolved targets), but never
        # treat a failed scan or a failed resolver as a successful evaluation.
        evaluate = WORKFLOW['jobs']['evaluate']
        self.assertEqual(evaluate['needs'], ['targets', 'scan'])
        self.assertIn("needs.targets.result == 'success'", evaluate['if'])
        self.assertIn("needs.scan.result == 'success' || needs.scan.result == 'skipped'", evaluate['if'])
        expression = evaluate['strategy']['matrix']['pr_number']
        self.assertIn("needs.targets.outputs.numbers == 'null' && needs.scan.outputs.numbers", expression)

    def test_stale_payload_only_candidate_falls_back(self):
        self.assertEqual(self.targets(associated=[], payload_prs=[{'number': 7}],
                         prs={'7': {'number': 7, 'state': 'open', 'head': {'sha': 'b' * 40}}})['result'], [7, 8])

    def test_direct_pr_event_and_dispatch_are_unchanged(self):
        result = self.run_script({'payload': {'pull_request': {'number': 9}}}, job='targets')
        self.assertEqual(result['result'], [9])
        result = self.run_script({'payload': {}}, job='targets', event='workflow_dispatch')
        self.assertEqual(result['result'], [7])


if __name__ == '__main__':
    unittest.main()
