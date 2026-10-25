const test = require('node:test');
const assert = require('node:assert/strict');
const {eligible, checksReady, mergeArguments} = require('./dependabot-policy.cjs');

function fixture() {
  return {
    repo: 'GdeiAssistant/GdeiAssistant-WechatApp', eventHead: 'a'.repeat(40),
    pr: {user: {login: 'dependabot[bot]'}, state: 'open', draft: false, base: {ref: 'master', repo: {full_name: 'GdeiAssistant/GdeiAssistant-WechatApp'}}, head: {sha: 'a'.repeat(40), repo: {full_name: 'GdeiAssistant/GdeiAssistant-WechatApp'}}},
    dependencies: [{dependencyName: 'example-library', packageEcosystem: 'npm_and_yarn', directory: '/', targetBranch: 'master', updateType: 'version-update:semver-patch', maintainerChanges: false, prevVersion: '1.2.3', newVersion: '1.2.4'}],
    files: [{filename: 'package-lock.json'}], commits: [{sha: 'a'.repeat(40), author: {login: 'dependabot[bot]'}, commit: {verification: {verified: true}}}],
  };
}

test('verified patch in permitted dependency files is eligible', () => assert.equal(eligible(fixture()), true));
for (const [name, change] of [
  ['external author', f => {f.pr.user.login = 'someone';}],
  ['fork head', f => {f.pr.head.repo.full_name = 'someone/fork';}],
  ['different target branch', f => {f.pr.base.ref = 'release';}],
  ['new head after event', f => {f.pr.head.sha = 'b'.repeat(40);}],
  ['unsigned commit', f => {f.commits[0].commit.verification.verified = false;}],
  ['additional human commit', f => {f.commits.push({...f.commits[0], author: {login: 'someone'}});}],
  ['changed source code', f => {f.files.push({filename: 'src/auth.js'});}],
  ['renamed source file', f => {f.files[0].previous_filename = 'src/auth.js';}],
  ['major update', f => {f.dependencies[0].updateType = 'version-update:semver-major';}],
  ['group containing major update', f => {f.dependencies.push({...f.dependencies[0], updateType: 'version-update:semver-major'});}],
  ['unknown grouped version', f => {f.dependencies.push({...f.dependencies[0], prevVersion: ''});}],
  ['zero-major version', f => {f.dependencies[0].prevVersion = '0.2.3';}],
  ['prerelease', f => {f.dependencies[0].newVersion = '1.2.4-beta';}],
  ['downgrade', f => {f.dependencies[0].newVersion = '1.2.2';}],
  ['unknown metadata', f => {f.dependencies = null;}],
  ['directory mismatch', f => {f.dependencies[0].directory = '/other';}],
  ['sensitive dependency', f => {f.dependencies[0].dependencyName = 'spring-security';}],
  ['build tool update', f => {f.dependencies[0].dependencyName = 'vite';}],
  ['action update', f => {f.dependencies[0].packageEcosystem = 'github_actions';}],
]) test(name + ' needs maintainer review', () => {const f = fixture(); change(f); assert.equal(eligible(f), false);});

test('merge is bound to the verified SHA and never uses admin bypass', () => {
  const args = mergeArguments('owner/repo', 3, 'a'.repeat(40));
  assert.equal(args[args.indexOf('--match-head-commit') + 1], 'a'.repeat(40));
  assert.equal(args.includes('--admin'), false);
});

function check(conclusion = 'success', name = 'Required') {
  return {name, app: {id: 15368}, head_sha: 'a'.repeat(40), status: 'completed', conclusion};
}
test('current, unique Actions check succeeds', () => assert.equal(checksReady([check()], ['Required'], 'a'.repeat(40)), true));
for (const [name, values] of [
  ['missing', []], ['pending', [{...check(), status: 'in_progress', conclusion: null}]],
  ['failed', [check('failure')]], ['skipped', [check('skipped')]],
  ['old head', [{...check(), head_sha: 'b'.repeat(40)}]],
  ['other app', [{...check(), app: {id: 1}}]], ['ambiguous names', [check(), check()]],
]) test(name + ' checks cannot authorize merge', () => assert.equal(checksReady(values, ['Required'], 'a'.repeat(40)), false));
