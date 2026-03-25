/**
 * 首页功能元数据，与功能管理、首页共用。
 * 结构：{ id, nameKey, descriptionKey, icon, path, defaultVisible }，外链项含 type、key。
 */
export const ALL_FEATURES = [
  { id: 'grade', nameKey: 'feature.grade.name', descriptionKey: 'feature.grade.description', icon: 'icon-grade', path: '/grade', defaultVisible: true },
  { id: 'schedule', nameKey: 'feature.schedule.name', descriptionKey: 'feature.schedule.description', icon: 'icon-schedule', path: '/schedule', defaultVisible: true },
  { id: 'cet', nameKey: 'feature.cet.name', descriptionKey: 'feature.cet.description', icon: 'icon-cet', path: '/cet', defaultVisible: true },
  { id: 'kaoyan', nameKey: 'feature.kaoyan.name', descriptionKey: 'feature.kaoyan.description', icon: 'icon-kaoyan', path: '/kaoyan', defaultVisible: true },
  { id: 'spare', nameKey: 'feature.spare.name', descriptionKey: 'feature.spare.description', icon: 'icon-spare', path: '/spare', defaultVisible: true },
  { id: 'collection', nameKey: 'feature.collection.name', descriptionKey: 'feature.collection.description', icon: 'icon-collection', path: '/library', defaultVisible: true },
  { id: 'card', nameKey: 'feature.card.name', descriptionKey: 'feature.card.description', icon: 'icon-card', path: '/card', defaultVisible: true },
  { id: 'pe', nameKey: 'feature.pe.name', descriptionKey: 'feature.pe.description', icon: 'icon-pe', path: '/pe', defaultVisible: true },
  { id: 'data', nameKey: 'feature.data.name', descriptionKey: 'feature.data.description', icon: 'icon-data', path: '/data', defaultVisible: true },
  { id: 'evaluate', nameKey: 'feature.evaluate.name', descriptionKey: 'feature.evaluate.description', icon: 'icon-evaluate', path: '/evaluate', defaultVisible: true },
  { id: 'ershou', nameKey: 'feature.ershou.name', descriptionKey: 'feature.ershou.description', icon: 'icon-ershou', path: '/ershou', defaultVisible: true },
  { id: 'delivery', nameKey: 'feature.delivery.name', descriptionKey: 'feature.delivery.description', icon: 'icon-delivery', path: '/delivery', defaultVisible: true },
  { id: 'lostandfound', nameKey: 'feature.lostandfound.name', descriptionKey: 'feature.lostandfound.description', icon: 'icon-lostandfound', path: '/lostandfound', defaultVisible: true },
  { id: 'secret', nameKey: 'feature.secret.name', descriptionKey: 'feature.secret.description', icon: 'icon-secret', path: '/secret', defaultVisible: true },
  { id: 'dating', nameKey: 'feature.dating.name', descriptionKey: 'feature.dating.description', icon: 'icon-dating', path: '/dating', defaultVisible: true },
  { id: 'express', nameKey: 'feature.express.name', descriptionKey: 'feature.express.description', icon: 'icon-express', path: '/express', defaultVisible: true },
  { id: 'topic', nameKey: 'feature.topic.name', descriptionKey: 'feature.topic.description', icon: 'icon-topic', path: '/topic', defaultVisible: true },
  { id: 'photograph', nameKey: 'feature.photograph.name', descriptionKey: 'feature.photograph.description', icon: 'icon-photograph', path: '/photograph', defaultVisible: true },
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

/** 首页图标路径映射（id -> 图片路径），与 public/img/function 对应 */
export const FEATURE_ICON_SRC = {
  grade: '/img/function/grade.png',
  schedule: '/img/function/schedule.png',
  cet: '/img/function/cet.png',
  collection: '/img/function/collection.png',
  evaluate: '/img/function/evaluate.png',
  spare: '/img/function/spare.png',
  kaoyan: '/img/function/kaoyan.png',
  card: '/img/function/card.png',
  data: '/img/function/data.png',
  ershou: '/img/function/ershou.png',
  lostandfound: '/img/function/lostandfound.png',
  secret: '/img/function/secret.png',
  photograph: '/img/function/photograph.png',
  express: '/img/function/express.png',
  dating: '/img/function/dating.png',
  topic: '/img/function/topic.png',
  delivery: '/img/function/delivery.png',
  pe: '/img/function/pe.png',
  about: '/img/function/about.png',
}
