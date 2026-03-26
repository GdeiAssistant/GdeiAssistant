export function createGradeYearTabs(t) {
  return [
    { value: 0, label: t('gradePage.year.freshman') },
    { value: 1, label: t('gradePage.year.sophomore') },
    { value: 2, label: t('gradePage.year.junior') },
    { value: 3, label: t('gradePage.year.senior') }
  ]
}

export function createGradeTableHeaders(t) {
  return {
    course: t('gradePage.table.course'),
    credit: t('gradePage.table.credit'),
    score: t('gradePage.table.score')
  }
}

export function createGradeActionSheetItems(t) {
  return {
    manageCache: t('gradePage.actionSheet.manageCache'),
    updateCache: t('gradePage.actionSheet.updateCache'),
    cancel: t('gradePage.actionSheet.cancel')
  }
}
