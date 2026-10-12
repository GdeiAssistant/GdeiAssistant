/**
 * 首页「今日课程」数据整理。
 * 课表接口返回整学期课程，column 0-6 对应周一到周日，row 为起始节次（0 起）。
 */

// 0=周一 … 6=周日，与课表页 todayIndex 一致
export function todayColumn(date = new Date()) {
  return (date.getDay() + 6) % 7
}

function inWeek(course, week) {
  if (week == null) return true
  const min = Number(course?.minScheduleWeek)
  const max = Number(course?.maxScheduleWeek)
  if (!Number.isFinite(min) || !Number.isFinite(max)) return false
  return week >= min && week <= max
}

function courseRow(course) {
  if (Number.isFinite(Number(course?.row))) return Number(course.row)
  return Math.floor(Number(course?.position || 0) / 7)
}

function courseColumn(course) {
  if (Number.isFinite(Number(course?.column))) return Number(course.column)
  return Number(course?.position || 0) % 7
}

/**
 * @param {Array} scheduleList 课表接口的 scheduleList
 * @param {number|null} week 当前周次；为空时不按周过滤
 * @param {number} column 今天的列（0=周一）
 * @returns {Array<{ key: string, name: string, location: string, lesson: string, row: number }>}
 */
export function selectTodayCourses(scheduleList, week, column) {
  if (!Array.isArray(scheduleList)) return []
  return scheduleList
    .filter((course) => course && courseColumn(course) === column && inWeek(course, week))
    .map((course) => ({
      key: `${course.position ?? ''}-${course.scheduleName ?? ''}`,
      name: String(course.scheduleName || ''),
      location: String(course.scheduleLocation || ''),
      lesson: String(course.scheduleLesson || ''),
      row: courseRow(course)
    }))
    .sort((a, b) => a.row - b.row)
}
