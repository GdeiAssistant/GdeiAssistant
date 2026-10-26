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
  fs.appendFileSync(process.env.GITHUB_STEP_SUMMARY, safe ? 'Verified patch metadata passed; maintainer review and current CI are still required.\n' : 'Maintainer review required. This change is outside the verified patch classification.\n');
}

module.exports = {eligible, dependencyFilesAllowed};
if (require.main === module) {
  if (process.argv[2] === 'metadata') runMetadata();
  else throw new Error('Expected metadata command; merging is maintainer-only');
}
