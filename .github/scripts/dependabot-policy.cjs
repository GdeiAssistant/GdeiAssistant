const fs = require('node:fs');
const {execFileSync} = require('node:child_process');

const layouts = {
  'GdeiAssistant/GdeiAssistant': {
    branch: 'master',
    files: ['frontend/package.json', 'frontend/package-lock.json', 'build.gradle'],
    directories: ['/', '/frontend'],
  },
  'GdeiAssistant/GdeiAssistant-WechatApp': {
    branch: 'master', files: ['package.json', 'package-lock.json'], directories: ['/'],
  },
  'GdeiAssistant/GdeiAssistant-Android': {
    branch: 'master', files: ['build.gradle.kts', 'app/build.gradle.kts', 'gradle/libs.versions.toml'], directories: ['/'],
  },
  'GdeiAssistant/GdeiAssistant-iOS': {branch: 'main', files: [], directories: []},
};
const sensitive = /(?:^|[/@.:-])(?:spring(?:framework|boot)?|security|oauth\w*|auth\w*|jwt\w*|jsonwebtoken|passport|crypto\w*|gradle|kotlin|vite|webpack|playwright|puppeteer|electron|wrangler|esbuild)(?:$|[/@.:-])/i;

function dependencyFilesAllowed(repo, files) {
  const config = layouts[repo];
  return Boolean(config && files.length && files.every(f => config.files.includes(f.filename) && (!f.previous_filename || config.files.includes(f.previous_filename))));
}

function eligible({repo, pr, eventHead, dependencies, files, commits}) {
  const config = layouts[repo];
  if (!config || pr.user?.login !== 'dependabot[bot]' || pr.state !== 'open' || pr.draft) return false;
  if (pr.base?.repo?.full_name !== repo || pr.head?.repo?.full_name !== repo || pr.base?.ref !== config.branch) return false;
  if (!eventHead || pr.head.sha !== eventHead || !dependencyFilesAllowed(repo, files)) return false;
  if (!commits.length || commits.at(-1).sha !== eventHead || !commits.every(c => c.author?.login === 'dependabot[bot]' && c.commit?.verification?.verified === true)) return false;
  // Unknown/group versions, toolchains and prereleases need a maintainer's review.
  if (!Array.isArray(dependencies) || !dependencies.length) return false;
  return dependencies.every(d => {
    if (!['npm_and_yarn', 'npm', 'gradle'].includes(d.packageEcosystem) || !config.directories.includes(d.directory)) return false;
    if (d.targetBranch !== config.branch || d.updateType !== 'version-update:semver-patch' || d.maintainerChanges !== false) return false;
    if (!d.dependencyName || sensitive.test(d.dependencyName)) return false;
    const previous = /^v?([1-9]\d*)\.(\d+)\.(\d+)$/.exec(d.prevVersion || '');
    const next = /^v?([1-9]\d*)\.(\d+)\.(\d+)$/.exec(d.newVersion || '');
    return Boolean(previous && next && previous[1] === next[1] && previous[2] === next[2] && Number(next[3]) > Number(previous[3]));
  });
}

function gh(args) {
  return execFileSync('gh', args, {encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe']}).trim();
}
function api(path) {return JSON.parse(gh(['api', path]));}
function pages(path) {return JSON.parse(gh(['api', '--paginate', '--slurp', path])).flat();}

function runMetadata() {
  const event = JSON.parse(fs.readFileSync(process.env.GITHUB_EVENT_PATH, 'utf8'));
  const repo = process.env.GITHUB_REPOSITORY;
  const number = event.pull_request?.number;
  if (!Number.isSafeInteger(number)) throw new Error('Missing PR number');
  const pr = api(`repos/${repo}/pulls/${number}`);
  const files = pages(`repos/${repo}/pulls/${number}/files?per_page=100`);
  const commits = pages(`repos/${repo}/pulls/${number}/commits?per_page=100`);
  let dependencies;
  try {dependencies = JSON.parse(process.env.DEPENDABOT_DEPENDENCIES || 'null');} catch {dependencies = null;}
  const safe = files.length === pr.changed_files && eligible({repo, pr, files, commits, dependencies, eventHead: event.pull_request.head.sha});
  fs.appendFileSync(process.env.GITHUB_OUTPUT, `eligible=${safe}\n`);
  fs.appendFileSync(process.env.GITHUB_STEP_SUMMARY, safe ? 'A verified patch update is eligible for automatic merge after current CI succeeds.\n' : 'Maintainer review required. Automatic merge is not authorized by this policy.\n');
}

function mergeArguments(repo, number, sha) {
  return ['pr', 'merge', String(number), '--repo', repo, '--merge', '--delete-branch', '--match-head-commit', sha];
}

function checksReady(checks, names, sha) {
  return names.every(name => {
    const matching = checks.filter(c => c.name === name && c.app?.id === 15368 && c.head_sha === sha);
    // Reject ambiguous check names instead of letting an unrelated run satisfy the policy.
    return matching.length === 1 && matching[0].status === 'completed' && matching[0].conclusion === 'success';
  });
}

function runMerge() {
  const event = JSON.parse(fs.readFileSync(process.env.GITHUB_EVENT_PATH, 'utf8'));
  const run = event.workflow_run;
  const repo = process.env.GITHUB_REPOSITORY;
  const config = layouts[repo];
  if (!config || run?.event !== 'pull_request' || run.conclusion !== 'success' || run.repository?.full_name !== repo) return;
  let numbers = (run.pull_requests || []).map(p => p.number);
  if (!numbers.length) numbers = api(`repos/${repo}/commits/${run.head_sha}/pulls`).map(p => p.number);
  numbers = [...new Set(numbers)];
  if (numbers.length !== 1 || !Number.isSafeInteger(numbers[0])) return;
  const number = numbers[0];
  const pr = api(`repos/${repo}/pulls/${number}`);
  if (pr.user?.login !== 'dependabot[bot]' || pr.state !== 'open' || pr.draft || pr.base?.ref !== config.branch || pr.base?.repo?.full_name !== repo || pr.head?.repo?.full_name !== repo) return;
  if (pr.head.sha !== run.head_sha || pr.mergeable !== true || pr.mergeable_state !== 'clean') return;
  const files = pages(`repos/${repo}/pulls/${number}/files?per_page=100`);
  if (files.length !== pr.changed_files || !dependencyFilesAllowed(repo, files)) return;
  const required = (process.env.REQUIRED_CHECKS || '').split('\n').map(s => s.trim()).filter(Boolean);
  if (!required.includes('Repository hygiene') || !required.includes('Safe dependency update')) throw new Error('Incomplete merge policy');
  const checkPages = JSON.parse(gh(['api', '--paginate', '--slurp', `repos/${repo}/commits/${pr.head.sha}/check-runs?filter=latest&per_page=100`]));
  const checks = checkPages.flatMap(p => p.check_runs || []);
  const failure = new Set(['failure', 'cancelled', 'timed_out', 'action_required', 'stale', 'startup_failure']);
  if (checks.some(c => failure.has(c.conclusion)) || !checksReady(checks, required, pr.head.sha)) return;
  // GitHub rejects the merge if the head changes after the preceding API reads.
  gh(mergeArguments(repo, number, pr.head.sha));
  console.log(`Merged verified Dependabot PR #${number} at ${pr.head.sha}.`);
}

module.exports = {eligible, dependencyFilesAllowed, checksReady, mergeArguments};
if (require.main === module) {
  if (process.argv[2] === 'metadata') runMetadata();
  else if (process.argv[2] === 'merge') runMerge();
  else throw new Error('Expected metadata or merge command');
}
