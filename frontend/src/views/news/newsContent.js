export function getNewsSourceLabel(type, t) {
  switch (Number(type)) {
    case 1:
      return t('news.tab.school')
    case 2:
      return t('news.tab.department')
    case 3:
      return t('news.tab.notice')
    case 4:
      return t('news.tab.academic')
    default:
      return t('news.defaultTitle')
  }
}

export function getNewsAttachmentTitle(rawTitle, t) {
  const title = String(rawTitle || '').trim().replace(/[：:]+$/, '')
  return title || t('news.openAttachment')
}
