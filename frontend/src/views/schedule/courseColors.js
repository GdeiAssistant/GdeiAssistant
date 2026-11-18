// Course-keyed colour assignment (RE-REVIEW 2.2). The server's position-based colorCode is ignored.
export const COURSE_PAIR_COUNT = 8

// Pairs whose light backgrounds sit closest together; never given to back-to-back blocks on one day.
const CLOSE_PAIRS = [1, 5]

export function hashCourseName(name) {
  let hash = 2166136261
  for (let index = 0; index < name.length; index += 1) {
    hash ^= name.charCodeAt(index)
    hash = Math.imul(hash, 16777619) >>> 0
  }
  return hash
}

const courseKey = (course) => String(course.scheduleName ?? '').trim()

function blockRange(course) {
  const start = course.row != null ? course.row : Math.floor((course.position || 0) / 7)
  const length = course.scheduleLength != null ? course.scheduleLength : 1
  return { start, end: start + length - 1 }
}

function blockColumn(course) {
  return course.column != null ? course.column : (course.position || 0) % 7
}

function weeksOverlap(a, b) {
  const aMin = a.minScheduleWeek != null ? a.minScheduleWeek : 1
  const aMax = a.maxScheduleWeek != null ? a.maxScheduleWeek : 20
  const bMin = b.minScheduleWeek != null ? b.minScheduleWeek : 1
  const bMax = b.maxScheduleWeek != null ? b.maxScheduleWeek : 20
  return aMin <= bMax && bMin <= aMax
}

function backToBack(a, b) {
  if (blockColumn(a) !== blockColumn(b) || !weeksOverlap(a, b)) return false
  const ra = blockRange(a)
  const rb = blockRange(b)
  return ra.end + 1 === rb.start || rb.end + 1 === ra.start
}

/**
 * Returns Map<course name, pair 1..8>.
 * Order of first appearance in the term (earliest week, then day, then period; name hash breaks ties)
 * hands out p1..p8; later courses reuse the pair least adjacent to them.
 */
export function assignCoursePairs(courses) {
  const blocksByName = new Map()
  for (const course of courses || []) {
    const key = courseKey(course)
    if (!key) continue
    if (!blocksByName.has(key)) blocksByName.set(key, [])
    blocksByName.get(key).push(course)
  }
  const appearance = (blocks) => blocks.reduce((best, block) => {
    const candidate = [block.minScheduleWeek != null ? block.minScheduleWeek : 1, blockColumn(block), blockRange(block).start]
    for (let index = 0; index < candidate.length; index += 1) {
      if (candidate[index] !== best[index]) return candidate[index] < best[index] ? candidate : best
    }
    return best
  }, [Infinity, Infinity, Infinity])
  const names = [...blocksByName.keys()].sort((a, b) => {
    const ka = appearance(blocksByName.get(a))
    const kb = appearance(blocksByName.get(b))
    for (let index = 0; index < ka.length; index += 1) {
      if (ka[index] !== kb[index]) return ka[index] - kb[index]
    }
    return hashCourseName(a) - hashCourseName(b) || (a < b ? -1 : 1)
  })

  const assigned = new Map()
  const adjacencyCount = (name, pair) => {
    let count = 0
    for (const [other, otherPair] of assigned) {
      if (otherPair !== pair) continue
      for (const block of blocksByName.get(name)) {
        for (const otherBlock of blocksByName.get(other)) {
          if (backToBack(block, otherBlock)) count += 1
        }
      }
    }
    return count
  }
  const clashesClosePair = (name, pair) => {
    if (!CLOSE_PAIRS.includes(pair)) return false
    const partner = CLOSE_PAIRS.find((candidate) => candidate !== pair)
    return adjacencyCount(name, partner) > 0
  }

  for (const name of names) {
    const all = Array.from({ length: COURSE_PAIR_COUNT }, (_, index) => index + 1)
    const used = new Set(assigned.values())
    const allowed = all.filter((pair) => !clashesClosePair(name, pair))
    const pool = allowed.length ? allowed : all
    const unused = pool.filter((pair) => !used.has(pair))
    if (unused.length) {
      assigned.set(name, unused[0])
      continue
    }
    let best = pool[0]
    let bestCount = adjacencyCount(name, best)
    for (const pair of pool.slice(1)) {
      const count = adjacencyCount(name, pair)
      if (count < bestCount) {
        best = pair
        bestCount = count
      }
    }
    assigned.set(name, best)
  }
  return assigned
}

// Grid rows: header, periods 1-4, 8px lunch band, periods 5-8, 8px dinner band, periods 9-12.
export const SCHEDULE_GRID_ROWS = '36px repeat(4, 52px) 8px repeat(4, 52px) 8px repeat(4, 52px)'
export const LUNCH_BAND_ROW = 6
export const DINNER_BAND_ROW = 11

/** 0-based period index to its CSS grid row line (header is row 1). */
export function periodGridRow(period) {
  return period + 2 + (period >= 4 ? 1 : 0) + (period >= 8 ? 1 : 0)
}

export function blockGridRow(row, length) {
  return `${periodGridRow(row)} / ${periodGridRow(row + length - 1) + 1}`
}
