import { MOCK_ACCOUNT_USERNAME, MOCK_ACCOUNT_PASSWORD } from '../constants/mock.js'

function formatLocationDisplay(regionName, stateName, cityName) {
  return [regionName, stateName, cityName].filter(function(item, index, list) {
    return !!item && item !== list[index - 1]
  }).join(' ')
}

export const MOCK_ACCOUNT_DATA = {
  username: MOCK_ACCOUNT_USERNAME,
  password: MOCK_ACCOUNT_PASSWORD
}

export const BASE_PROFILE = {
  username: 'gdeiassistant',
  nickname: '林知远',
  avatar: '',
  birthday: '2004-09-16',
  faculty: '计算机科学系',
  major: '软件工程',
  enrollment: '2023',
  location: formatLocationDisplay('中国', '广东', '广州'),
  locationRegion: 'CN',
  locationState: '44',
  locationCity: '1',
  hometown: formatLocationDisplay('中国', '广东', '汕头'),
  hometownRegion: 'CN',
  hometownState: '44',
  hometownCity: '5',
  introduction: '喜欢做实用的小工具，也在准备移动端开发实习。',
  ipArea: '广东'
}

export const GRADE_REPORTS = [
  {
    year: 0,
    firstTermGPA: '3.72',
    firstTermGradeList: [
      { gradeName: '高等数学', gradeScore: '92', gradeCredit: '4' },
      { gradeName: '程序设计基础', gradeScore: '95', gradeCredit: '3' },
      { gradeName: '大学英语', gradeScore: '88', gradeCredit: '3' }
    ],
    secondTermGPA: '3.65',
    secondTermGradeList: [
      { gradeName: '数据结构', gradeScore: '94', gradeCredit: '4' },
      { gradeName: '离散数学', gradeScore: '90', gradeCredit: '3' },
      { gradeName: '大学物理', gradeScore: '85', gradeCredit: '3' }
    ]
  },
  {
    year: 1,
    firstTermGPA: '3.68',
    firstTermGradeList: [
      { gradeName: '数据库系统概论', gradeScore: '93', gradeCredit: '3' },
      { gradeName: '操作系统', gradeScore: '89', gradeCredit: '4' },
      { gradeName: '计算机网络', gradeScore: '91', gradeCredit: '3' }
    ],
    secondTermGPA: '3.55',
    secondTermGradeList: [
      { gradeName: '软件工程', gradeScore: '95', gradeCredit: '3' },
      { gradeName: '编译原理', gradeScore: '86', gradeCredit: '4' },
      { gradeName: '概率论与数理统计', gradeScore: '87', gradeCredit: '3' }
    ]
  },
  {
    year: 2,
    firstTermGPA: '3.78',
    firstTermGradeList: [
      { gradeName: 'iOS 移动开发', gradeScore: '97', gradeCredit: '3' },
      { gradeName: '前端工程化', gradeScore: '92', gradeCredit: '3' },
      { gradeName: '计算机图形学', gradeScore: '85', gradeCredit: '3' }
    ],
    secondTermGPA: '3.62',
    secondTermGradeList: [
      { gradeName: '软件测试', gradeScore: '91', gradeCredit: '3' },
      { gradeName: '项目管理', gradeScore: '90', gradeCredit: '2' },
      { gradeName: '人工智能导论', gradeScore: '89', gradeCredit: '3' }
    ]
  },
  {
    year: 3,
    firstTermGPA: null,
    firstTermGradeList: [
      { gradeName: '毕业实习', gradeScore: '优', gradeCredit: '6' },
      { gradeName: '创新创业实践', gradeScore: '良', gradeCredit: '2' }
    ],
    secondTermGPA: null,
    secondTermGradeList: [
      { gradeName: '毕业设计', gradeScore: '进行中', gradeCredit: '8' }
    ]
  }
]

export const SCHEDULE_TEMPLATE = [
  {
    column: 0, row: 0, scheduleLength: 2, position: 0,
    scheduleLesson: '第 1-2 节', scheduleName: '移动应用开发',
    minScheduleWeek: 1, maxScheduleWeek: 16,
    scheduleTeacher: '陈老师', scheduleLocation: '教学楼 A201'
  },
  {
    column: 1, row: 2, scheduleLength: 2, position: 15,
    scheduleLesson: '第 3-4 节', scheduleName: '软件测试',
    minScheduleWeek: 1, maxScheduleWeek: 16,
    scheduleTeacher: '张老师', scheduleLocation: '教学楼 C402'
  },
  {
    column: 2, row: 0, scheduleLength: 2, position: 2,
    scheduleLesson: '第 1-2 节', scheduleName: '数据库原理',
    minScheduleWeek: 1, maxScheduleWeek: 16,
    scheduleTeacher: '李老师', scheduleLocation: '教学楼 B305'
  },
  {
    column: 3, row: 4, scheduleLength: 2, position: 31,
    scheduleLesson: '第 5-6 节', scheduleName: '编译原理',
    minScheduleWeek: 1, maxScheduleWeek: 16,
    scheduleTeacher: '王老师', scheduleLocation: '教学楼 B402'
  },
  {
    column: 4, row: 0, scheduleLength: 2, position: 4,
    scheduleLesson: '第 1-2 节', scheduleName: '计算机网络',
    minScheduleWeek: 1, maxScheduleWeek: 16,
    scheduleTeacher: '周老师', scheduleLocation: '教学楼 D202'
  },
  {
    column: 4, row: 6, scheduleLength: 2, position: 46,
    scheduleLesson: '第 7-8 节', scheduleName: '就业指导',
    minScheduleWeek: 1, maxScheduleWeek: 16,
    scheduleTeacher: '辅导员', scheduleLocation: '教学楼 A101'
  },
  {
    column: 5, row: 2, scheduleLength: 2, position: 19,
    scheduleLesson: '第 3-4 节', scheduleName: '人工智能导论',
    minScheduleWeek: 2, maxScheduleWeek: 14,
    scheduleTeacher: '吴老师', scheduleLocation: '教学楼 B101'
  }
]

export const CARD_TRANSACTIONS = [
  { merchantName: '第一食堂', tradeName: '消费', tradePrice: '-13.50', tradeTime: '2026-03-15 12:16:20' },
  { merchantName: '第三食堂', tradeName: '消费', tradePrice: '-11.00', tradeTime: '2026-03-15 07:42:11' },
  { merchantName: '校园卡服务中心', tradeName: '充值', tradePrice: '100.00', tradeTime: '2026-03-14 18:03:51' },
  { merchantName: '超市', tradeName: '消费', tradePrice: '-26.80', tradeTime: '2026-03-14 16:35:07' }
]

export const COLLECTION_ITEMS = [
  { bookname: 'SwiftUI 界面开发实践', author: '李明', publishingHouse: '人民邮电出版社', detailURL: 'detail_swiftui' },
  { bookname: '操作系统概念', author: 'Abraham Silberschatz', publishingHouse: '机械工业出版社', detailURL: 'detail_os' },
  { bookname: '大学英语六级真题精讲', author: '刘洋', publishingHouse: '外语教学出版社', detailURL: 'detail_cet' },
  { bookname: '研究生入学考试数学复习全书', author: '张宇', publishingHouse: '高等教育出版社', detailURL: 'detail_kaoyan' }
]

export const COLLECTION_DETAILS = {
  detail_swiftui: {
    bookname: 'SwiftUI 界面开发实践', author: '李明',
    principal: 'SwiftUI 界面开发实践 李明', publishingHouse: '人民邮电出版社 2025',
    price: '9787115600001 68.00', physicalDescriptionArea: '16 开，附录含实验案例与索引',
    personalPrincipal: '李明', subjectTheme: '移动开发 / 界面设计', chineseLibraryClassification: 'TP312.8',
    collectionDistributionList: [
      { location: '北校区图书馆 3 楼 A 区', callNumber: 'TP312.8/S12', barcode: 'B1002381', state: '在馆' },
      { location: '南校区图书馆 2 楼借阅区', callNumber: 'TP312.8/S12', barcode: 'B1002382', state: '可借' }
    ]
  },
  detail_os: {
    bookname: '操作系统概念', author: 'Abraham Silberschatz',
    principal: '操作系统概念 Abraham Silberschatz', publishingHouse: '机械工业出版社 2024',
    price: '9787111700002 99.00', physicalDescriptionArea: '精装，配套习题集',
    personalPrincipal: 'Abraham Silberschatz', subjectTheme: '操作系统', chineseLibraryClassification: 'TP316',
    collectionDistributionList: [
      { location: '图书馆 4 楼计算机专区', callNumber: 'TP316/O11', barcode: 'B2003291', state: '在馆' }
    ]
  },
  detail_cet: {
    bookname: '大学英语六级真题精讲', author: '刘洋',
    principal: '大学英语六级真题精讲 刘洋', publishingHouse: '外语教学出版社 2026',
    price: '9787567899999 49.80', physicalDescriptionArea: '附赠音频资料',
    personalPrincipal: '刘洋', subjectTheme: '英语六级', chineseLibraryClassification: 'H319.6',
    collectionDistributionList: [
      { location: '图书馆 2 楼外语专区', callNumber: 'H319.6/L25', barcode: 'B5200081', state: '可借' }
    ]
  },
  detail_kaoyan: {
    bookname: '研究生入学考试数学复习全书', author: '张宇',
    principal: '研究生入学考试数学复习全书 张宇', publishingHouse: '高等教育出版社 2026',
    price: '9787040654321 59.00', physicalDescriptionArea: '附章节练习与答案',
    personalPrincipal: '张宇', subjectTheme: '考研数学', chineseLibraryClassification: 'O13',
    collectionDistributionList: [
      { location: '图书馆 1 楼考试专区', callNumber: 'O13/Z11', barcode: 'B8801022', state: '在馆' }
    ]
  }
}

export const DEFAULT_BORROWED_BOOKS = [
  { name: 'iOS 架构设计实践', id: 'B0001001', sn: 'sn_1001', code: 'code_1001', author: '王磊', borrowDate: '2026-03-01', returnDate: '2026-03-22', renewTime: 0 },
  { name: '数据库系统概论', id: 'B0001002', sn: 'sn_1002', code: 'code_1002', author: '陈小华', borrowDate: '2026-02-24', returnDate: '2026-03-17', renewTime: 1 }
]

export const NEWS_BY_TYPE = {
  1: [
    { id: 'news_1_1', title: '下周《移动应用开发》实验课教室调整', publishDate: '2026-03-08', content: '实验课将调整到教学楼 B402，请同学们提前查看课表通知。' },
    { id: 'news_1_2', title: '春季学期专业选修课补退选开放通知', publishDate: '2026-03-06', content: '补退选时间为 3 月 10 日 9:00 至 3 月 12 日 17:00。' }
  ],
  2: [
    { id: 'news_2_1', title: '大学英语六级模拟考试安排发布', publishDate: '2026-03-07', content: '模拟考试将于本周六上午 9 点进行，地点见准考证。' },
    { id: 'news_2_2', title: '计算机等级考试报名信息说明', publishDate: '2026-03-04', content: '报名通道已开启，请在规定时间内完成缴费。' }
  ],
  3: [
    { id: 'news_3_1', title: '教务系统维护公告', publishDate: '2026-03-05', content: '教务系统将于周末凌晨维护，请提前保存选课和成绩查询信息。' },
    { id: 'news_3_2', title: '课程成绩复核流程更新', publishDate: '2026-03-02', content: '如需申请成绩复核，请按学院要求提交纸质材料。' }
  ],
  4: [
    { id: 'news_4_1', title: '宿舍区热水系统例行检修', publishDate: '2026-03-03', content: '北区宿舍热水系统将于明晚分时段检修，请提前安排洗漱时间。' },
    { id: 'news_4_2', title: '图书馆临时闭馆维护通知', publishDate: '2026-03-01', content: '因设备维护，图书馆将于周日 8:00-12:00 暂停开放。' }
  ],
  5: [
    { id: 'news_5_1', title: '春季校园招聘双选会报名开启', publishDate: '2026-03-05', content: '双选会将于体育馆举行，报名截止至周四中午。' },
    { id: 'news_5_2', title: '体育馆临时借用安排更新', publishDate: '2026-03-04', content: '本周末体育馆将优先保障校级活动，部分场地借用时间已调整。' }
  ]
}

export const ANNOUNCEMENT_LIST = [
  { id: 'announcement_001', title: '系统维护通知', publishTime: '1小时前', content: '为配合学期中服务器扩容，本周三 18:00 至 20:00 将进行例行维护。维护期间消息中心、校园社区和部分查询服务可能出现短暂不可用，建议提前保存正在编辑的内容。' },
  { id: 'announcement_002', title: '春季双选会入场安排', publishTime: '今天 09:10', content: '春季校园双选会将于本周五 14:30 在体育馆举行。请已报名同学提前准备校园卡，按学院分批入场，现场会同步开放企业岗位二维码与志愿者咨询台。' },
  { id: 'announcement_003', title: '图书馆夜间开放时段调整', publishTime: '昨天', content: '从下周起，图书馆一楼自习区开放时间延长至 23:00，二楼研讨室仍需预约。若遇到插座、座位预约或入馆设备异常，可直接在资讯页提交反馈。' },
  { id: 'announcement_004', title: '校医院门诊排班更新', publishTime: '昨天 18:40', content: '校医院本周起调整晚间门诊排班，工作日 18:30 后优先接待急诊与发热相关问诊，普通门诊请尽量在白天时段前往。' },
  { id: 'announcement_005', title: '宿舍门禁系统升级提醒', publishTime: '前天', content: '北区与中区宿舍门禁将于本周末夜间分批升级，升级期间刷卡开门可能存在短暂延迟，请提前留意楼栋群通知。' },
  { id: 'announcement_006', title: '就业指导中心咨询时段开放', publishTime: '2026-03-01', content: '就业指导中心新增春招一对一简历咨询时段，已开放线上预约。需要模拟面试或简历修改的同学可在工作日预约。' }
]

export const INTERACTION_MESSAGES = [
  { id: 'msg_interaction_001', module: 'dating', type: 'interaction', targetType: 'sent', targetId: '802', targetSubId: '701', title: '卖室友互动', content: '你发出的卖室友申请已被对方查看，去看看最新状态。', createdAt: '刚刚', isRead: false },
  { id: 'msg_interaction_002', module: 'delivery', type: 'interaction', targetType: 'published', targetId: '601', targetSubId: '9001', title: '全民快递提醒', content: '你发布的订单已被接单，建议尽快和接单同学确认送达时间。', createdAt: '6分钟前', isRead: false },
  { id: 'msg_interaction_003', module: 'delivery', type: 'interaction', targetType: 'accepted', targetId: '602', targetSubId: '9001', title: '全民快递提醒', content: '你接的订单已完成，系统已同步为已完成状态。', createdAt: '12分钟前', isRead: false },
  { id: 'msg_interaction_004', module: 'secret', type: 'comment', targetType: 'comment', targetId: '301', targetSubId: '1', title: '树洞互动', content: '有人回复了你的树洞，打开详情即可查看最新评论。', createdAt: '10分钟前', isRead: false },
  { id: 'msg_interaction_005', module: 'express', type: 'comment', targetType: 'comment', targetId: '401', targetSubId: '1', title: '表白墙互动', content: '有人给你的表白留了言，打开详情即可查看最新评论。', createdAt: '14分钟前', isRead: false },
  { id: 'msg_interaction_006', module: 'express', type: 'interaction', targetType: 'guess', targetId: '401', targetSubId: '', title: '表白墙互动', content: '有人参与了你的猜名字互动，去看看最新猜测次数。', createdAt: '18分钟前', isRead: true },
  { id: 'msg_interaction_007', module: 'topic', type: 'like', targetType: 'like', targetId: '501', targetSubId: '', title: '话题互动', content: '你的话题收到了新的点赞。', createdAt: '25分钟前', isRead: true },
  { id: 'msg_interaction_008', module: 'photograph', type: 'comment', targetType: 'comment', targetId: '901', targetSubId: '1', title: '拍好校园互动', content: '有人评论了你的作品，打开详情即可查看最新评论。', createdAt: '40分钟前', isRead: false }
]

export const YELLOW_PAGE_RESULT = {
  type: [
    { typeCode: 1, typeName: '教务服务' },
    { typeCode: 2, typeName: '后勤服务' },
    { typeCode: 3, typeName: '学生服务' },
    { typeCode: 4, typeName: '图书与网络' }
  ],
  data: [
    { typeCode: 1, section: '教务处值班室', majorPhone: '020-12345678', minorPhone: '', address: '行政楼 302', email: 'jwc@gdei.edu.cn', website: 'https://www.gdei.edu.cn/jwc' },
    { typeCode: 1, section: '考务办公室', majorPhone: '020-12345680', minorPhone: '020-12345681', address: '行政楼 306', email: 'exam@gdei.edu.cn', website: '' },
    { typeCode: 2, section: '宿舍报修', majorPhone: '020-87654321', minorPhone: '', address: '后勤楼 1 楼', email: 'repair@gdei.edu.cn', website: '' },
    { typeCode: 2, section: '校园保卫处', majorPhone: '020-87654323', minorPhone: '020-87654324', address: '保卫处值班室', email: '', website: '' },
    { typeCode: 3, section: '学生工作部', majorPhone: '020-66554411', minorPhone: '', address: '行政楼 201', email: 'xgb@gdei.edu.cn', website: '' },
    { typeCode: 3, section: '就业指导中心', majorPhone: '020-66554413', minorPhone: '', address: '创新创业楼', email: 'job@gdei.edu.cn', website: 'https://job.gdei.edu.cn' },
    { typeCode: 4, section: '图书馆总服务台', majorPhone: '020-99887711', minorPhone: '', address: '图书馆 1 楼', email: 'library@gdei.edu.cn', website: 'https://lib.gdei.edu.cn' },
    { typeCode: 4, section: '网络信息中心', majorPhone: '020-99887712', minorPhone: '020-99887713', address: '信息楼 402', email: 'nic@gdei.edu.cn', website: 'https://nic.gdei.edu.cn' }
  ]
}

export const SPARE_ROOMS = [
  { number: 'A201', name: '教学楼 A201', type: '多媒体课室', zone: '海珠校区', classSeating: '120', section: '第 1-2 节', examSeating: '96' },
  { number: 'B305', name: '教学楼 B305', type: '普通课室', zone: '海珠校区', classSeating: '80', section: '第 1-2 节', examSeating: '64' },
  { number: 'E402', name: '实验楼 E402', type: '机房', zone: '海珠校区', classSeating: '60', section: '第 1-2 节', examSeating: '48' }
]
