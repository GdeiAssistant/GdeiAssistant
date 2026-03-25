export function createFaqList(t) {
  return [
    {
      title: t('feedback.faq.login.title'),
      content: t('feedback.faq.login.content')
    },
    {
      title: t('feedback.faq.password.title'),
      content: t('feedback.faq.password.content')
    },
    {
      title: t('feedback.faq.schedule.title'),
      content: t('feedback.faq.schedule.content')
    },
    {
      title: t('feedback.faq.cache.title'),
      content: t('feedback.faq.cache.content')
    },
    {
      title: t('feedback.faq.delete.title'),
      content: t('feedback.faq.delete.content')
    }
  ]
}

export function createFeedbackTypes(t) {
  return [
    t('feedback.type.crash'),
    t('feedback.type.login'),
    t('feedback.type.profile'),
    t('feedback.type.verification'),
    t('feedback.type.account'),
    t('feedback.type.appError'),
    t('feedback.type.other')
  ]
}
