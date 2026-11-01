const test = require('node:test');
const assert = require('node:assert/strict');
const {eligible, checksReady, mergeArguments} = require('./dependabot-policy.cjs');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const {spawnSync} = require('node:child_process');
function fixture() {
  const repo = 'GdeiAssistant/Example', head = 'a'.repeat(40);
  return {repo, eventHead: head, config: {repo, branch: 'master', files: ['requirements.txt'], directories: ['/']},
    pr: {user: {login: 'dependabot[bot]'}, state: 'open', draft: false, commits: 1, changed_files: 1,
      base: {ref: 'master', repo: {full_name: repo}}, head: {sha: head, repo: {full_name: repo}}},
    files: [{filename: 'requirements.txt', status: 'modified'}],
    commits: [{sha: head, author: {login: 'dependabot[bot]'}, commit: {verification: {verified: true}}}],
    dependencies: [{dependencyName: 'example', packageEcosystem: 'pip', directory: '/', targetBranch: 'master',
      updateType: 'version-update:semver-patch', maintainerChanges: false, prevVersion: '1.2.3', newVersion: '1.2.4'}]};
}
test('signed same-repository patch is eligible', () => assert.equal(eligible(fixture()), true));
for (const [name, mutate] of [
  ['other owner', f => {f.repo = f.config.repo = 'Pigbibi/Example';}],
  ['paused example', f => {f.repo = f.config.repo = 'GdeiAssistant/PausedExample';}],
  ['fork', f => {f.pr.head.repo.full_name = 'Someone/Example';}],
  ['human author', f => {f.pr.user.login = 'someone';}],
  ['draft', f => {f.pr.draft = true;}],
  ['other base', f => {f.pr.base.ref = 'other';}],
  ['stale head', f => {f.pr.head.sha = 'b'.repeat(40);}],
  ['unsigned commit', f => {f.commits[0].commit.verification.verified = false;}],
  ['extra human commit', f => {f.commits[0].author.login = 'someone';}],
  ['truncated commits', f => {f.pr.commits = 2;}],
  ['truncated files', f => {f.pr.changed_files = 2;}],
  ['source modification', f => {f.files[0].filename = 'src/code.py';}],
  ['workflow modification', f => {f.files[0].filename = '.github/workflows/ci.yml';}],
  ['deleted manifest', f => {f.files[0].status = 'removed';}],
  ['renamed source', f => {f.files[0].previous_filename = 'src/code.py';}],
  ['missing metadata', f => {f.dependencies = null;}],
  ['major', f => {f.dependencies[0].newVersion = '2.0.0';}],
  ['minor', f => {f.dependencies[0].newVersion = '1.3.0';}],
  ['zero major', f => {f.dependencies[0].prevVersion = '0.2.3';}],
  ['prerelease', f => {f.dependencies[0].newVersion = '1.2.4-rc1';}],
  ['downgrade', f => {f.dependencies[0].newVersion = '1.2.2';}],
  ['metadata mismatch', f => {f.dependencies[0].updateType = 'version-update:semver-minor';}],
  ['group unknown', f => {f.dependencies.push({...f.dependencies[0], prevVersion: null});}],
  ['directory mismatch', f => {f.dependencies[0].directory = '/other';}],
  ['maintainer changes', f => {f.dependencies[0].maintainerChanges = true;}],
  ['Actions', f => {f.dependencies[0].packageEcosystem = 'github_actions';}],
]) test(name + ' requires review', () => {const f = fixture(); mutate(f); assert.equal(eligible(f), false);});
const sha = 'a'.repeat(40), required = ['Repository hygiene', 'Safe dependency update'];
const check = name => ({name, app: {id: 15368}, head_sha: sha, status: 'completed', conclusion: 'success'});
test('unique current required checks pass', () => assert.equal(checksReady(required.map(check), required, sha), true));
for (const [name, mutate] of [
  ['missing', c => c.pop()], ['pending', c => {c[0].status = 'in_progress';}],
  ['failure', c => {c[0].conclusion = 'failure';}], ['skipped required', c => {c[0].conclusion = 'skipped';}],
  ['wrong app', c => {c[0].app.id = 1;}], ['old head', c => {c[0].head_sha = 'b'.repeat(40);}],
  ['optional pending', c => c.push({...check('Other'), status: 'queued', conclusion: null})],
  ['optional failure', c => c.push({...check('Other'), conclusion: 'failure'})],
]) test(name + ' prevents merge', () => {const c = required.map(check); mutate(c); assert.equal(checksReady(c, required, sha), false);});
test('successful push and PR records on the same head can satisfy a required check', () => {
  assert.equal(checksReady([...required.map(check), check(required[0])], required, sha), true);
});
for (const [label, record] of [
  ['failed', {...check(required[0]), conclusion: 'failure'}],
  ['pending', {...check(required[0]), status: 'in_progress', conclusion: null}],
  ['skipped', {...check(required[0]), conclusion: 'skipped'}],
]) test('duplicate ' + label + ' required record prevents merge', () => {
  assert.equal(checksReady([...required.map(check), record], required, sha), false);
});
test('incomplete required check policy is rejected', () => assert.equal(checksReady([check('CI')], ['CI'], sha), false));
test('merge matches the head, uses squash and no admin bypass', () => {
  const args = mergeArguments('GdeiAssistant/Example', 1, sha);
  assert.equal(args[args.indexOf('--match-head-commit') + 1], sha);
  assert.equal(args.includes('--admin'), false);
  assert.equal(args.includes('--squash'), true);
  assert.equal(args.includes('--delete-branch'), false);
  assert.equal(args.includes('--auto'), false);
});

function exerciseMerge(mutate = () => {}) {
  const temp = fs.mkdtempSync(path.join(os.tmpdir(), 'dependency-policy-'));
  try {
    const f = fixture(), base = 'd'.repeat(40);
    f.config.required_checks = required;
    f.pr.base.sha = base;
    f.pr.mergeable = true;
    f.pr.mergeable_state = 'clean';
    const state = {pr: f.pr, files: f.files, commits: f.commits, base, compare: 'ahead',
      checks: required.map(check), legacy: {statuses: [], state: 'pending'}};
    mutate(state);
    fs.copyFileSync(path.join(__dirname, 'dependabot-policy.cjs'), path.join(temp, 'policy.cjs'));
    fs.writeFileSync(path.join(temp, 'dependabot-policy.json'), JSON.stringify(f.config));
    fs.writeFileSync(path.join(temp, 'event.json'), JSON.stringify({workflow_run: {
      event: state.event || 'pull_request', conclusion: 'success', repository: {full_name: f.repo}, head_sha: f.eventHead,
      pull_requests: [{number: 1}],
    }}));
    fs.writeFileSync(path.join(temp, 'state.json'), JSON.stringify(state));
    fs.writeFileSync(path.join(temp, 'gh'), `#!/usr/bin/env node
const fs = require('node:fs'), path = require('node:path');
const root = __dirname, s = JSON.parse(fs.readFileSync(path.join(root, 'state.json'))), args = process.argv.slice(2);
if (args.includes('event=APPROVE')) {fs.writeFileSync(path.join(root, 'approved.json'), JSON.stringify(args)); process.stdout.write('{}'); process.exit(0);}
if (args[0] === 'pr') {fs.writeFileSync(path.join(root, 'merged.json'), JSON.stringify(args)); process.exit(0);}
const route = args.at(-1); let result;
if (route.endsWith('/pulls/1')) result = s.pr;
else if (route.includes('/files?')) result = [s.files];
else if (route.includes('/commits?')) result = [s.commits];
else if (route.includes('/check-runs?')) result = [{check_runs: s.checks}];
else if (route.endsWith('/status')) result = s.legacy;
else if (route.includes('/compare/')) result = {status: s.compare};
else if (route.endsWith('/branches/master')) {
 const marker = path.join(root, 'base-read');
 result = {commit: {sha: s.advanceBase && fs.existsSync(marker) ? 'e'.repeat(40) : s.base}};
 fs.writeFileSync(marker, 'read');
} else throw new Error('unexpected route');
process.stdout.write(JSON.stringify(result));
`, {mode: 0o700});
    const result = spawnSync(process.execPath, [path.join(temp, 'policy.cjs'), 'merge'], {
      env: {...process.env, PATH: temp + path.delimiter + process.env.PATH,
        GITHUB_EVENT_PATH: path.join(temp, 'event.json'), GITHUB_REPOSITORY: f.repo}, encoding: 'utf8',
    });
    assert.equal(result.status, 0, result.stderr);
    if (fs.existsSync(path.join(temp, 'merged.json'))) {
      const approval = JSON.parse(fs.readFileSync(path.join(temp, 'approved.json')));
      assert.ok(approval.includes('commit_id=' + f.eventHead));
    }
    return fs.existsSync(path.join(temp, 'merged.json')) ? JSON.parse(fs.readFileSync(path.join(temp, 'merged.json'))) : null;
  } finally {fs.rmSync(temp, {recursive: true, force: true});}
}
test('merge CLI accepts successful push and PR duplicates without ignoring either', () => {
  assert.deepEqual(exerciseMerge(s => s.checks.push(check(required[0]))), mergeArguments('GdeiAssistant/Example', 1, sha));
});
test('real merge CLI consumes paginated evidence and sends exact-head merge', () => {
  assert.deepEqual(exerciseMerge(), mergeArguments('GdeiAssistant/Example', 1, sha));
});
for (const [name, mutate] of [
  ['human PR', s => {s.pr.user.login = 'human';}],
  ['unsigned', s => {s.commits[0].commit.verification.verified = false;}],
  ['head changed', s => {s.pr.head.sha = 'c'.repeat(40);}],
  ['truncated files', s => {s.pr.changed_files = 2;}],
  ['workflow changed', s => {s.files[0].filename = '.github/workflows/ci.yml';}],
  ['old base', s => {s.pr.base.sha = 'c'.repeat(40);}],
  ['diverged base', s => {s.compare = 'diverged';}],
  ['pending CI', s => {s.checks[0].status = 'in_progress';}],
  ['missing CI', s => {s.checks.pop();}],
  ['skipped required CI', s => {s.checks[0].conclusion = 'skipped';}],
  ['failed legacy status', s => {s.legacy = {statuses: [{}], state: 'failure'};}],
  ['base changed during reads', s => {s.advanceBase = true;}],
]) test('merge CLI performs no write with ' + name, () => assert.equal(exerciseMerge(mutate), null));

test('stable minor update is eligible', () => {
  const f = fixture(); Object.assign(f.dependencies[0], {newVersion: '1.3.0', updateType: 'version-update:semver-minor'});
  assert.equal(eligible(f), true);
});
test('stable framework patch is eligible after CI', () => {
  const f = fixture(); f.dependencies[0].dependencyName = 'react-dom'; assert.equal(eligible(f), true);
});
function actionFixture() {
  const f = fixture(); f.config.action_files = ['.github/workflows/ci.yml'];
  f.files = [{filename: '.github/workflows/ci.yml', status: 'modified', additions: 1, deletions: 1, patch:
    '-      - uses: actions/checkout@' + 'b'.repeat(40) + ' # v6\n+      - uses: actions/checkout@' + 'c'.repeat(40) + ' # v7'}];
  Object.assign(f.dependencies[0], {dependencyName: 'actions/checkout', packageEcosystem: 'github_actions',
    prevVersion: '6.0.0', newVersion: '7.0.0', updateType: 'version-update:semver-major'});
  return f;
}
test('pinned Actions major version-only update is eligible', () => assert.equal(eligible(actionFixture()), true));
for (const [name, mutate] of [
  ['injected command', f => {f.files[0].patch += '\n+      - run: echo unexpected';}],
  ['changed action owner', f => {f.files[0].patch = f.files[0].patch.replace('+      - uses: actions/', '+      - uses: other/');}],
  ['missing patch', f => {delete f.files[0].patch;}],
  ['truncated patch', f => {f.files[0].additions = 2;}],
  ['unpinned action', f => {f.files[0].patch = f.files[0].patch.replace('c'.repeat(40), 'v7');}],
  ['mixed group', f => {f.dependencies.push(fixture().dependencies[0]);}],
  ['wrong directory', f => {f.dependencies[0].directory = '/elsewhere';}],
]) test('Actions rejects ' + name, () => {const f = actionFixture(); mutate(f); assert.equal(eligible(f), false);});

test('two-component stable requirement supports a minor update', () => {
  const f = fixture(); Object.assign(f.dependencies[0], {prevVersion: '1.45', newVersion: '1.64.0', updateType: 'version-update:semver-minor'});
  assert.equal(eligible(f), true);
});

test('trusted governance completion can trigger the same exact-head merge', () => {
  assert.deepEqual(exerciseMerge(s => {s.event = 'pull_request_target';}), mergeArguments('GdeiAssistant/Example', 1, sha));
});
test('unrelated workflow event cannot trigger merge', () => {
  assert.equal(exerciseMerge(s => {s.event = 'push';}), null);
});
