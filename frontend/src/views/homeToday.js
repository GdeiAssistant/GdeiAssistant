/**
 * 首页「今日课程」数据整理。
 * 课表接口返回整学期课程，column 0-6 对应周一到周日，row 为起始节次（0 起）。
 */

// Campus day, independent of the browser/device time zone. 0=Monday … 6=Sunday.
export function todayColumn(date = new Date()) {
  const campusDate = new Date(date.getTime() + 8 * 60 * 60 * 1000)
  return (campusDate.getUTCDay() + 6) % 7
}

export function campusDateLabel(date = new Date(), locale) {
  return new Intl.DateTimeFormat(locale, {
    timeZone: 'Asia/Shanghai', month: 'long', day: 'numeric', weekday: 'long'
  }).format(date)
}

function validInteger(value, min, max) {
  return Number.isInteger(value) && value >= min && value <= max
}

/**
 * @param {Array} scheduleList 课表接口的 scheduleList
 * @param {number} week API返回的真实当前周次
 * @param {number} column 今天的列（0=周一）
 * @returns {Array<{ key: string, name: string, location: string, lesson: string, row: number }>|null} null means unavailable metadata.
 */
export function selectTodayCourses(scheduleList, week, column) {
  if (!Array.isArray(scheduleList) || !validInteger(week, 1, 20) || !validInteger(column, 0, 6)) return null
  const courses = []
  for (const course of scheduleList) {
    if (!course || typeof course !== 'object') return null
    if (course.position != null && !validInteger(course.position, 0, 69)) return null
    const courseColumn = course.column ?? (course.position != null ? course.position % 7 : null)
    const row = course.row ?? (course.position != null ? Math.floor(course.position / 7) : null)
    const minWeek = course.minScheduleWeek
    const maxWeek = course.maxScheduleWeek
    if (!validInteger(courseColumn, 0, 6) || !validInteger(row, 0, 9)
      || !validInteger(minWeek, 1, 20) || !validInteger(maxWeek, minWeek, 20)
      || typeof course.scheduleName !== 'string' || !course.scheduleName.trim()) return null
    if (courseColumn !== column || week < minWeek || week > maxWeek) continue
    courses.push({
      key: course.id || `${courseColumn}-${row}-${minWeek}-${maxWeek}-${course.scheduleName}`,
      name: course.scheduleName,
      location: typeof course.scheduleLocation === 'string' ? course.scheduleLocation : '',
      lesson: typeof course.scheduleLesson === 'string' ? course.scheduleLesson : '',
      row
    })
  }
  return courses.sort((a, b) => a.row - b.row)
}
