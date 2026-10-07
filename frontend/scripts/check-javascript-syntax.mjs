import { readdirSync } from 'node:fs'
import { join } from 'node:path'
import { execFileSync } from 'node:child_process'
function check(directory) {
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    const file = join(directory, entry.name)
    if (entry.isDirectory()) check(file)
    else if (entry.name.endsWith('.js')) execFileSync(process.execPath, ['--check', file], { stdio: 'inherit' })
  }
}
check('src')
console.log('JavaScript syntax checks passed.')
