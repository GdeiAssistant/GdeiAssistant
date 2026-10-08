// Compare stable codes, not translated labels. Omitted fields stay unchanged.
export function buildProfilePatch(current, saved) {
  const patch = {}
  for (const key of ['nickname', 'introduction', 'majorCode', 'facultyCode', 'enrollment', 'birthday']) {
    if (String(current[key] ?? '') === String(saved[key] ?? '')) continue
    if (key === 'majorCode') patch.major = current[key] || null
    else if (key === 'facultyCode') patch.faculty = current[key]
    else if (key === 'enrollment') patch.enrollment = current[key] ? Number(current[key]) : null
    else if (key === 'birthday') {
      const [year, month, date] = (current[key] || '').split('-').map(Number)
      patch.birthday = current[key] ? { year, month, date } : null
    } else patch[key] = current[key]
  }
  for (const key of ['location', 'hometown']) {
    if (['Region', 'State', 'City'].some(part => String(current[key + part] || '') !== String(saved[key + part] || ''))) {
      patch[key] = current[key + 'Region'] ? { region: current[key + 'Region'], state: current[key + 'State'] || null, city: current[key + 'City'] || null } : null
    }
  }
  return patch
}
