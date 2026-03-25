export function createScheduleDayLabels(t) {
  return [
    t('schedule.day.monday'),
    t('schedule.day.tuesday'),
    t('schedule.day.wednesday'),
    t('schedule.day.thursday'),
    t('schedule.day.friday'),
    t('schedule.day.saturday'),
    t('schedule.day.sunday')
  ]
}

export function createScheduleDayOptions(t) {
  return createScheduleDayLabels(t).map((label, index) => ({
    label,
    value: index + 1
  }))
}

export function createWeekOptions(t, totalWeeks = 20) {
  return Array.from({ length: totalWeeks }, (_, index) => ({
    label: t('schedule.weekLabel', { week: index + 1 }),
    value: index + 1
  }))
}

export function createSectionOptions(t, totalSections = 10) {
  return Array.from({ length: totalSections }, (_, index) => ({
    label: t('schedule.sectionLabel', { section: index + 1 }),
    value: index + 1
  }))
}

export function createScheduleLengthOptions(startSection, t, maxDailySections = 10, maxCourseLength = 5) {
  const normalizedStart = Math.max(1, startSection || 1)
  const maxLength = Math.max(1, Math.min(maxCourseLength, maxDailySections + 1 - normalizedStart))

  return Array.from({ length: maxLength }, (_, index) => ({
    label: t('schedule.lengthLabel', { count: index + 1 }),
    value: index + 1
  }))
}

export function createScheduleMessages(t) {
  return {
    syncLoading: t('schedule.message.syncLoading'),
    syncSuccess: t('schedule.message.syncSuccess'),
    nameRequired: t('schedule.message.nameRequired'),
    locationRequired: t('schedule.message.locationRequired'),
    invalidWeekRange: t('schedule.message.invalidWeekRange'),
    invalidSectionRange: t('schedule.message.invalidSectionRange'),
    duplicateCourse: t('schedule.message.duplicateCourse'),
    addSuccess: t('schedule.message.addSuccess'),
    deleteConfirm: t('schedule.message.deleteConfirm'),
    deleteSuccess: t('schedule.message.deleteSuccess')
  }
}
