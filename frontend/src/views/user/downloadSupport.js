export const EXPORT_STATUS = {
  NOT_EXPORT: 0,
  EXPORTING: 1,
  EXPORTED: 2,
}

export function getDownloadStatusTitle(t, exportStatus, isLoading) {
  if (isLoading) return t('downloadPage.status.loadingTitle')
  if (exportStatus === EXPORT_STATUS.EXPORTING) return t('downloadPage.status.exportingTitle')
  if (exportStatus === EXPORT_STATUS.EXPORTED) return t('downloadPage.status.exportedTitle')
  return t('downloadPage.status.idleTitle')
}

export function getDownloadStatusDescription(t, exportStatus, isLoading) {
  if (isLoading) return t('downloadPage.status.loadingDescription')
  if (exportStatus === EXPORT_STATUS.EXPORTING) return t('downloadPage.status.exportingDescription')
  if (exportStatus === EXPORT_STATUS.EXPORTED) return t('downloadPage.status.exportedDescription')
  return t('downloadPage.status.idleDescription')
}
