/**
 * 首页功能元数据，与功能管理、首页共用。
 * 结构：{ id, name, icon, path, defaultVisible }，外链项含 type、key。
 */
export const ALL_FEATURES = [
  { id: 'grade', name: '成绩查询', icon: 'icon-grade', path: '/grade', defaultVisible: true },
  { id: 'schedule', name: '课表查询', icon: 'icon-schedule', path: '/schedule', defaultVisible: true },
  { id: 'cet', name: '四六级查询', icon: 'icon-cet', path: '/cet', defaultVisible: true },
  { id: 'kaoyan', name: '考研查询', icon: 'icon-kaoyan', path: '/kaoyan', defaultVisible: true },
  { id: 'spare', name: '教室查询', icon: 'icon-spare', path: '/spare', defaultVisible: true },
  { id: 'collection', name: '图书馆', icon: 'icon-collection', path: '/library', defaultVisible: true },
  { id: 'card', name: '校园卡', icon: 'icon-card', path: '/card', defaultVisible: true },
  { id: 'data', name: '数据查询', icon: 'icon-data', path: '/data', defaultVisible: true },
  { id: 'evaluate', name: '教学评价', icon: 'icon-evaluate', path: '/evaluate', defaultVisible: true },
  { id: 'ershou', name: '二手交易', icon: 'icon-ershou', path: '/ershou', defaultVisible: true },
  { id: 'delivery', name: '全民快递', icon: 'icon-delivery', path: '/delivery', defaultVisible: true },
  { id: 'lostandfound', name: '失物招领', icon: 'icon-lostandfound', path: '/lostandfound', defaultVisible: true },
  { id: 'secret', name: '校园树洞', icon: 'icon-secret', path: '/secret', defaultVisible: true },
  { id: 'dating', name: '卖室友', icon: 'icon-dating', path: '/dating', defaultVisible: true },
  { id: 'express', name: '表白墙', icon: 'icon-express', path: '/express', defaultVisible: true },
  { id: 'topic', name: '话题', icon: 'icon-topic', path: '/topic', defaultVisible: true },
  { id: 'photograph', name: '拍好校园', icon: 'icon-photograph', path: '/photograph', defaultVisible: true },
]

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
}
