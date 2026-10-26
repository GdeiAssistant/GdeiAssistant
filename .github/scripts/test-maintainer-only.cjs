const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const {spawnSync} = require('node:child_process');

test('dependency policy cannot execute automatic merges', () => {
  const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'maintainer-only-'));
  try {
    const event = path.join(directory, 'event.json');
    fs.writeFileSync(event, '{}');
    const result = spawnSync(process.execPath, [path.join(__dirname, 'dependabot-policy.cjs'), 'merge'], {
      env: {...process.env, GITHUB_EVENT_PATH: event, GITHUB_REPOSITORY: 'GdeiAssistant/GdeiAssistant', GH_TOKEN: ''},
      encoding: 'utf8',
    });
    assert.notEqual(result.status, 0, 'merge mode must be rejected rather than execute or silently accept a merge request');
    assert.match(result.stderr, /maintainer-only/);
  } finally {
    fs.rmSync(directory, {recursive: true, force: true});
  }
});
