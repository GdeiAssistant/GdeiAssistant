/**
 * 首页功能元数据，与功能管理、首页共用。
 * 结构：{ id, nameKey, descriptionKey, path, defaultVisible }，外链项含 type、key。
 */
export const ALL_FEATURES = [
  { id: 'grade', nameKey: 'feature.grade.name', descriptionKey: 'feature.grade.description', path: '/grade', defaultVisible: true },
  { id: 'schedule', nameKey: 'feature.schedule.name', descriptionKey: 'feature.schedule.description', path: '/schedule', defaultVisible: true },
  { id: 'cet', nameKey: 'feature.cet.name', descriptionKey: 'feature.cet.description', path: '/cet', defaultVisible: true },
  { id: 'kaoyan', nameKey: 'feature.kaoyan.name', descriptionKey: 'feature.kaoyan.description', path: '/kaoyan', defaultVisible: true },
  { id: 'spare', nameKey: 'feature.spare.name', descriptionKey: 'feature.spare.description', path: '/spare', defaultVisible: true },
  { id: 'collection', nameKey: 'feature.collection.name', descriptionKey: 'feature.collection.description', path: '/library', defaultVisible: true },
  { id: 'card', nameKey: 'feature.card.name', descriptionKey: 'feature.card.description', path: '/card', defaultVisible: true },
  { id: 'pe', nameKey: 'feature.pe.name', descriptionKey: 'feature.pe.description', path: '/pe', defaultVisible: true },
  { id: 'data', nameKey: 'feature.data.name', descriptionKey: 'feature.data.description', path: '/data', defaultVisible: true },
  { id: 'evaluate', nameKey: 'feature.evaluate.name', descriptionKey: 'feature.evaluate.description', path: '/evaluate', defaultVisible: true },
  { id: 'ershou', nameKey: 'feature.ershou.name', descriptionKey: 'feature.ershou.description', path: '/marketplace', defaultVisible: true },
  { id: 'delivery', nameKey: 'feature.delivery.name', descriptionKey: 'feature.delivery.description', path: '/delivery', defaultVisible: true },
  { id: 'lostandfound', nameKey: 'feature.lostandfound.name', descriptionKey: 'feature.lostandfound.description', path: '/lostandfound', defaultVisible: true },
  { id: 'secret', nameKey: 'feature.secret.name', descriptionKey: 'feature.secret.description', path: '/secret', defaultVisible: true },
  { id: 'dating', nameKey: 'feature.dating.name', descriptionKey: 'feature.dating.description', path: '/dating', defaultVisible: true },
  { id: 'express', nameKey: 'feature.express.name', descriptionKey: 'feature.express.description', path: '/express', defaultVisible: true },
  { id: 'topic', nameKey: 'feature.topic.name', descriptionKey: 'feature.topic.description', path: '/topic', defaultVisible: true },
  { id: 'photograph', nameKey: 'feature.photograph.name', descriptionKey: 'feature.photograph.description', path: '/photograph', defaultVisible: true },
]

export function getFeatureName(feature, t) {
  return t(feature.nameKey)
}

export function getFeatureDescription(feature, t) {
  return t(feature.descriptionKey)
}

export function getLocalizedFeatures(features, t) {
  return features.map((feature) => ({
    ...feature,
    name: getFeatureName(feature, t),
    description: getFeatureDescription(feature, t)
  }))
}

