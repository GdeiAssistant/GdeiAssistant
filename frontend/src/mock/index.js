import Mock from 'mockjs'

// 统一延迟，让 WEUI Loading 有机会展示（约 500ms）
Mock.setup({
  timeout: 500
})

// 拦截登录接口：POST /api/userlogin
Mock.mock(/\/api\/userlogin/, 'post', () => {
  return {
    success: true,
    message: '登录成功'
  }
})

// 个人头像：GET /api/user/avatar（Mock 返回当前头像路径）
Mock.mock(/\/api\/user\/avatar/, 'get', () => ({
  success: true,
  data: { avatar: '/img/login/qq.png' }
}))

// 成绩查询：POST /api/gradequery，仅返回截图核心字段（课程名、学分、成绩）
// 请求体可选：{ year: 0|1|2|3 }，按 year 返回不同学年数据
const gradeByYear = {
  0: {
    firstTermGPA: 3.58,
    secondTermGPA: 3.4,
    firstTermGradeList: [
      { gradeName: '大学英语Ⅰ', gradeCredit: '4', gradeScore: '85' },
      { gradeName: '高等数学A(上)', gradeCredit: '5', gradeScore: '78' },
      { gradeName: '计算机导论', gradeCredit: '2', gradeScore: '92' },
      { gradeName: '思想道德与法治', gradeCredit: '3', gradeScore: '88' }
    ],
    secondTermGradeList: [
      { gradeName: '大学英语Ⅱ', gradeCredit: '4', gradeScore: '82' },
      { gradeName: '高等数学A(下)', gradeCredit: '5', gradeScore: '80' },
      { gradeName: '程序设计基础', gradeCredit: '4', gradeScore: '90' }
    ]
  },
  1: {
    firstTermGPA: 3.66,
    secondTermGPA: 3.52,
    firstTermGradeList: [
      { gradeName: '创业管理实战', gradeCredit: '1.0', gradeScore: '89' },
      { gradeName: '创业人生', gradeCredit: '4', gradeScore: '96.6' },
      { gradeName: '毛泽东思想和中国特色社会主义理论体系概论', gradeCredit: '2', gradeScore: '77.1' },
      { gradeName: '中国近现代史纲要', gradeCredit: '2', gradeScore: '85' }
    ],
    secondTermGradeList: [
      { gradeName: '马克思主义基本原理', gradeCredit: '3', gradeScore: '82' },
      { gradeName: '数据结构', gradeCredit: '4', gradeScore: '88' },
      { gradeName: '大学英语Ⅲ', gradeCredit: '4', gradeScore: '79' }
    ]
  },
  2: {
    firstTermGPA: 3.48,
    secondTermGPA: 3.61,
    firstTermGradeList: [
      { gradeName: '操作系统', gradeCredit: '4', gradeScore: '86' },
      { gradeName: '计算机网络', gradeCredit: '3', gradeScore: '81' },
      { gradeName: '数据库原理', gradeCredit: '3', gradeScore: '90' }
    ],
    secondTermGradeList: [
      { gradeName: '软件工程', gradeCredit: '3', gradeScore: '84' },
      { gradeName: '编译原理', gradeCredit: '4', gradeScore: '78' }
    ]
  },
  3: {
    firstTermGPA: 3.55,
    secondTermGPA: null,
    firstTermGradeList: [
      { gradeName: '毕业设计(论文)', gradeCredit: '8', gradeScore: '—' },
      { gradeName: '专业实习', gradeCredit: '4', gradeScore: '优良' }
    ],
    secondTermGradeList: []
  }
}

Mock.mock(/\/api\/gradequery/, 'post', (options) => {
  const body = typeof options.body === 'string' ? JSON.parse(options.body || '{}') : (options.body || {})
  const year = body.year === undefined ? 0 : Math.min(3, Math.max(0, parseInt(body.year, 10) || 0))
  const data = gradeByYear[year]
  return {
    success: true,
    data: {
      year,
      firstTermGPA: data.firstTermGPA,
      secondTermGPA: data.secondTermGPA,
      firstTermGradeList: data.firstTermGradeList,
      secondTermGradeList: data.secondTermGradeList
    }
  }
})

// 课表查询：POST /api/schedulequery，与旧版 schedule.js 一致的 14 种预设半透明感色彩
const SCHEDULE_COLORS = [
  'rgba(7, 178, 255, 0.92)',   // #07b2ff
  'rgba(56, 94, 249, 0.92)',   // #385ef9
  'rgba(249, 29, 27, 0.92)',   // #f91d1b
  'rgba(254, 165, 16, 0.92)',  // #fea510
  'rgba(248, 100, 14, 0.92)',  // #f8640e
  'rgba(26, 233, 97, 0.92)',   // #1ae961
  'rgba(232, 68, 187, 0.92)',  // #e844bb
  'rgba(253, 116, 113, 0.92)', // #fd7471
  'rgba(253, 202, 0, 0.92)',   // #fdca00
  'rgba(33, 165, 237, 0.92)',  // #21a5ed
  'rgba(254, 167, 22, 0.92)',  // #fea716
  'rgba(97, 184, 34, 0.92)',   // #61b822
  'rgba(55, 92, 239, 0.92)',   // #375cef
  'rgba(255, 97, 96, 0.92)'    // #ff6160
]
function getScheduleColor(index) {
  return SCHEDULE_COLORS[index % SCHEDULE_COLORS.length]
}

const mockScheduleList = [
  { row: 0, column: 0, scheduleLength: 2, scheduleName: '大学英语Ⅰ', scheduleTeacher: '张老师', scheduleLocation: '教学楼A201', minScheduleWeek: 1, maxScheduleWeek: 16 },
  { row: 2, column: 0, scheduleLength: 2, scheduleName: '高等数学A(上)', scheduleTeacher: '李老师', scheduleLocation: '教学楼B102', minScheduleWeek: 1, maxScheduleWeek: 16 },
  { row: 4, column: 1, scheduleLength: 2, scheduleName: '计算机导论', scheduleTeacher: '王老师', scheduleLocation: '实验楼301', minScheduleWeek: 1, maxScheduleWeek: 12 },
  { row: 0, column: 2, scheduleLength: 2, scheduleName: '思想道德与法治', scheduleTeacher: '刘老师', scheduleLocation: '教学楼C105', minScheduleWeek: 1, maxScheduleWeek: 16 },
  { row: 2, column: 2, scheduleLength: 1, scheduleName: '体育Ⅰ', scheduleTeacher: '陈老师', scheduleLocation: '田径场', minScheduleWeek: 1, maxScheduleWeek: 18 },
  { row: 0, column: 3, scheduleLength: 2, scheduleName: '大学物理', scheduleTeacher: '赵老师', scheduleLocation: '教学楼A305', minScheduleWeek: 3, maxScheduleWeek: 16 },
  { row: 4, column: 3, scheduleLength: 2, scheduleName: '程序设计基础', scheduleTeacher: '王老师', scheduleLocation: '实验楼302', minScheduleWeek: 5, maxScheduleWeek: 14 },
  { row: 6, column: 4, scheduleLength: 2, scheduleName: '数据结构', scheduleTeacher: '孙老师', scheduleLocation: '教学楼B203', minScheduleWeek: 1, maxScheduleWeek: 16 }
].map((item, i) => ({
  ...item,
  position: item.row * 7 + item.column,
  colorCode: getScheduleColor(i)
}))

Mock.mock(/\/api\/schedulequery/, 'post', (options) => {
  const body = typeof options.body === 'string' ? JSON.parse(options.body || '{}') : (options.body || {})
  const week = body.week != null ? Math.min(20, Math.max(1, parseInt(body.week, 10) || 1)) : 1
  return {
    success: true,
    data: {
      week,
      scheduleList: mockScheduleList
    }
  }
})

// 验证码：GET /api/cet/vcode，返回占位图（每次请求不同文字，点击即刷新）
Mock.mock(/\/api\/cet\/vcode/, 'get', () => ({
  success: true,
  data: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="120" height="50"%3E%3Crect fill="%23f0f0f0" width="120" height="50"/%3E%3Ctext x="60" y="28" text-anchor="middle" fill="%23999" font-size="12"%3E' + Math.random().toString(36).slice(2, 6) + '%3C/text%3E%3C/svg%3E'
}))

// 导入考号：GET /api/cet/number。有数据时返回 number+name，无数据时 success:false
let mockHasSavedNumber = true
Mock.mock(/\/api\/cet\/number/, 'get', () => {
  if (mockHasSavedNumber) {
    return { success: true, data: { number: '440123456789012', name: '张三' } }
  }
  return { success: false, message: '你未保存准考证号' }
})

// 四六级查询：POST /api/cet/query
Mock.mock(/\/api\/cet\/query/, 'post', (options) => {
  const body = typeof options.body === 'string' ? JSON.parse(options.body || '{}') : (options.body || {})
  const { number, name, checkcode } = body
  if (!number || !name || !checkcode) {
    return { success: false, message: '请将信息填写完整！' }
  }
  if (String(number).length !== 15) {
    return { success: false, message: '准考证号长度不正确！' }
  }
  return {
    success: true,
    cet: {
      name: name || '张三',
      type: '大学英语四级',
      school: '某某大学',
      totalScore: '450',
      listeningScore: '150',
      readingScore: '160',
      writingAndTranslatingScore: '140'
    }
  }
})

// 保存考号：POST /api/cet/save
Mock.mock(/\/api\/cet\/save/, 'post', (options) => {
  const body = typeof options.body === 'string' ? JSON.parse(options.body || '{}') : (options.body || {})
  const { number, name } = body
  if (!number || String(number).length !== 15) {
    return { success: false, message: '请输入15位准考证号' }
  }
  mockHasSavedNumber = true
  return { success: true, message: '保存成功' }
})

// 馆藏查询（全校图书）：GET /api/collection/search?keyword=xxx
const mockCollectionList = [
  { id: '1', title: '数据库原理与技术 (第三版) 实验指导(随书光盘)', author: '主编程传庆', publisher: '中国水利水电出版社2018' },
  { id: '2', title: 'Oracle数据库管理从入门到精通', author: '何明编著', publisher: '中国水利水电出版社2017' },
  { id: '3', title: '数据库技术与应用实践教程.SQL Server 2008.第2版', author: '主编严晖, 周肆清', publisher: '电子工业出版社2018' },
  { id: '4', title: '高效办公.婉转Access数据库', author: '刘璐编著', publisher: '电子工业出版社2018' },
  { id: '5', title: '构建Oracle数据库云最佳实践.使用Oracle enterprise manager cloud control 13c', author: '(印) Porus Homi Havewala著', publisher: '清华大学出版社2018' }
]
Mock.mock(/\/api\/collection\/search/, 'get', () => mockCollectionList)

// 馆藏详情：GET /api/collection/detail/:id
Mock.mock(/\/api\/collection\/detail\/\w+/, 'get', (options) => {
  const id = (options.url || '').split('/detail/')[1]?.split('?')[0] || ''
  const book = mockCollectionList.find((b) => String(b.id) === String(id))
  return book || { id, title: '—', author: '—', publisher: '—' }
})

// 我的图书馆（个人借阅）：GET /api/book/query
Mock.mock(/\/api\/book\/query/, 'get', () => [
  { id: 'b1', title: '数据库原理与技术 (第三版)', borrowDate: '2025-01-10', dueDate: '2025-03-10', renewStatus: '未续借' },
  { id: 'b2', title: 'Oracle数据库管理从入门到精通', borrowDate: '2025-02-01', dueDate: '2025-04-01', renewStatus: '已续借1次' }
])

// 饭卡消费查询：GET /api/card/query?date=yyyy-mm-dd（消费负数、充值/进账正数，用于测试颜色区分）
const mockCardList = [
  { location: '第一食堂', type: '持卡人消费', time: '2026/2/16 07:25:10', amount: -6 },
  { location: '校园超市', type: '持卡人消费', time: '2026/2/16 12:08:33', amount: -15.5 },
  { location: '圈存机', type: '银行卡充值', time: '2026/2/16 08:00:00', amount: 200 },
  { location: '第二食堂', type: '持卡人消费', time: '2026/2/16 18:20:15', amount: -8.5 },
  { location: '图书馆', type: '持卡人消费', time: '2026/2/16 09:15:00', amount: -2 },
  { location: '一卡通中心', type: '现金充值', time: '2026/2/15 14:30:00', amount: 100 },
  { location: '第三食堂', type: '持卡人消费', time: '2026/2/15 12:22:08', amount: -9 },
  { location: '医务室', type: '持卡人消费', time: '2026/2/15 10:05:00', amount: -3.5 },
  { location: '自助转账', type: '账户转入', time: '2026/2/14 16:00:00', amount: 50 }
]
Mock.mock(/\/api\/card\/query/, 'get', () => mockCardList)

// 我的校园卡状态：GET /api/card/info
Mock.mock(/\/api\/card\/info/, 'get', () => ({
  name: '陈彩银',
  studentId: '17550401072',
  cardNo: '40341',
  balance: '34.2',
  transitionBalance: '0',
  lossStatus: '正常卡',
  freezeStatus: '正常'
}))

// 校园卡挂失：POST /api/card/report_loss
Mock.mock(/\/api\/card\/report_loss/, 'post', () => ({ success: true }))

// 一键评教：POST /api/evaluate，body: { directSubmit: boolean }
Mock.mock(/\/api\/evaluate/, 'post', () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ success: true, message: '评教成功' })
    }, 1500)
  })
})

// 空课室查询：极宽泛正则，只要 URL 包含 spare/query 就拦截，直接返回对象
Mock.mock(RegExp('.*/spare/query.*'), 'get', (options) => {
  console.log('🎯 Mock 成功拦截到空课室请求:', options.url)
  const mockSpareList = [
    { id: 101, name: '花都-教学楼502', type: '多媒体教室', seats: 120 },
    { id: 201, name: '海珠-综合楼402', type: '普通教室', seats: 80 },
    { id: 102, name: '海珠-教学楼A101', type: '多媒体教室', seats: 60 },
    { id: 202, name: '花都-教学楼D105', type: '专业课教室', seats: 80 }
  ]
  return {
    success: true,
    data: mockSpareList
  }
})

// 考研成绩查询：GET /api/kaoyan/query，正则拦截，返回纯对象
Mock.mock(RegExp('.*/kaoyan/query.*'), 'get', () => {
  return {
    success: true,
    data: {
      name: '张三',
      candidateNo: '111',
      totalScore: 368,
      politics: 72,
      foreignLanguage: 68,
      business1: 118,
      business2: 110
    }
  }
})

// 新闻通知列表：每页 20 条、共 40 条，强制第一页即可撑满屏幕出现滚动条
const NEWS_TOTAL = 40
const PAGE_SIZE = 20
const newsTitlePrefixes = [
  '关于申报2019年度校级大学生创新创业训练计划项目的通知',
  '关于转发广州市教育局关于组织开展2020年广州市高校创新创业(就业)教育项目申报工作的通知',
  '关于做好我校2019年新增学士学位授予专业审核工作的通知',
  '关于2018-2019学年第1学期实践课程实施工作的通知',
  '关于做好2018-2019学年第2学期排课工作的通知',
  '关于2019年春季学期开学教学安排及补考缓考事项的补充通知',
  '关于组织申报省级质量工程项目与校级教改立项工作的通知',
  '关于做好本学期期末考试考务工作及成绩录入的说明',
  '关于公布校级教学成果奖获奖名单及申报省级成果奖的通知',
  '关于举办教师教学能力提升培训班与教学沙龙活动的通知',
  '关于2020届毕业生图像信息采集与学历证书电子注册工作的通知',
  '关于做好学生选课及退改选、重修报名工作的通知',
  '关于公布本学期校级公选课开课名单及选课时间的通知',
  '关于组织学生参加学科竞赛与创新创业训练的补充通知',
  '关于做好实习实践基地建设申报与年度检查工作的通知',
  '关于教学实验室安全检查与整改及危险品管理的通知',
  '关于举办青年教师教学竞赛与教学名师讲座系列活动的通知',
  '关于做好教材征订与发放及选用审核工作的通知',
  '关于学期末成绩录入与提交及学籍异动处理说明',
  '关于学生学业预警与帮扶及转专业工作安排的通知'
]
function randomDate() {
  const y = 2018 + Math.floor(Math.random() * 3)
  const m = String(1 + Math.floor(Math.random() * 12)).padStart(2, '0')
  const d = String(1 + Math.floor(Math.random() * 28)).padStart(2, '0')
  return `${y}-${m}-${d}`
}
Mock.mock(RegExp('.*/news/list.*'), 'get', (options) => {
  const url = options.url || ''
  const typeMatch = url.match(/[?&]type=(\d+)/)
  const pageMatch = url.match(/[?&]page=(\d+)/)
  const type = typeMatch ? Math.min(5, Math.max(1, parseInt(typeMatch[1], 10) || 1)) : 1
  const page = pageMatch ? Math.max(1, parseInt(pageMatch[1], 10) || 1) : 1
  const total = NEWS_TOTAL
  const start = (page - 1) * PAGE_SIZE
  const hasMore = page * PAGE_SIZE < total
  const list = []
  for (let i = 0; i < PAGE_SIZE && start + i < total; i++) {
    const idx = start + i
    list.push({
      id: `news-${type}-${idx + 1}`,
      title: newsTitlePrefixes[idx % newsTitlePrefixes.length],
      date: randomDate()
    })
  }
  return {
    success: true,
    data: {
      list,
      hasMore
    }
  }
})

// 电费查询：POST /api/data/electricfees
Mock.mock(/\/api\/data\/electricfees/, 'post', (options) => {
  const body = typeof options.body === 'string' ? JSON.parse(options.body || '{}') : (options.body || {})
  const { name, number, year } = body
  if (!name || !number || !year) {
    return { success: false, message: '请将信息填写完整' }
  }
  if (!/^\d{11}$/.test(String(number))) {
    return { success: false, message: '请输入正确的学号' }
  }
  if (year < 2016 || year > 2050) {
    return { success: false, message: '请选择正确的年份' }
  }
  return {
    success: true,
    data: {
      year: String(year),
      buildingNumber: 'A',
      roomNumber: '301',
      peopleNumber: '4',
      usedElectricAmount: '156.5',
      freeElectricAmount: '120',
      feeBasedElectricAmount: '36.5',
      electricPrice: '0.65',
      totalElectricBill: '23.73',
      averageElectricBill: '5.93'
    }
  }
})

// 黄页查询：GET /api/data/yellowpage（使用真实数据）
Mock.mock(/\/api\/data\/yellowpage/, 'get', () => {
  return {
    success: true,
    data: {
      type: [
        { typeCode: 1, typeName: '倾听与倾诉' },
        { typeCode: 2, typeName: '故障保修' },
        { typeCode: 3, typeName: '网络' },
        { typeCode: 4, typeName: '党务' },
        { typeCode: 5, typeName: '医疗与救援' },
        { typeCode: 6, typeName: '生活与保障' },
        { typeCode: 7, typeName: '职能部门' },
        { typeCode: 8, typeName: '就业创业' },
        { typeCode: 9, typeName: '院系部门' },
        { typeCode: 10, typeName: '举报和申诉' },
        { typeCode: 11, typeName: '紧急求助' }
      ],
      data: [
        { typeCode: 1, section: '心理健康教育与辅导中心（海珠校区）', typeName: '倾听与倾诉', campus: '海珠', majorPhone: '34114108', minorPhone: null, address: '田家炳大楼2楼', email: null, website: null },
        { typeCode: 1, section: '心理健康教育与辅导中心（花都校区）', typeName: '倾听与倾诉', campus: '花都', majorPhone: '36967712', minorPhone: null, address: '花都校区实验楼205', email: null, website: null },
        { typeCode: 1, section: '心灵之约（海珠校区）', typeName: '倾听与倾诉', campus: '海珠', majorPhone: '34113456', minorPhone: null, address: '学生宿舍4栋1楼，党员工作站内', email: null, website: null },
        { typeCode: 1, section: '心灵之约（花都校区）', typeName: '倾听与倾诉', campus: '花都', majorPhone: '34113987', minorPhone: null, address: '学生宿舍4栋1楼，党员工作站内', email: null, website: null },
        { typeCode: 2, section: '宿舍管理科', typeName: '故障保修', campus: '海珠', majorPhone: '34113723', minorPhone: null, address: '学生宿舍4栋007、008', email: null, website: null },
        { typeCode: 2, section: '物业服务中心', typeName: '故障保修', campus: '花都', majorPhone: '18011902709', minorPhone: null, address: '学生宿舍1栋1楼', email: null, website: null },
        { typeCode: 3, section: '网络中心（海珠校区）', typeName: '网络', campus: '海珠', majorPhone: '34113702', minorPhone: null, address: '综合楼B2203', email: null, website: 'http://web.gdei.edu.cn/nic/' },
        { typeCode: 3, section: '网络中心（花都校区）', typeName: '网络', campus: '花都', majorPhone: '36967722', minorPhone: null, address: '图书馆220', email: null, website: null },
        { typeCode: 4, section: '党代表工作室（海珠校区）', typeName: '党务', campus: '海珠', majorPhone: '34113023', minorPhone: null, address: '综合楼18楼1806', email: null, website: null },
        { typeCode: 4, section: '党代表工作室（花都校区）', typeName: '党务', campus: '花都', majorPhone: '36967719', minorPhone: null, address: '图书馆708', email: null, website: null },
        { typeCode: 4, section: '党政办公室', typeName: '党务', campus: '海珠', majorPhone: '34113736', minorPhone: null, address: '综合楼15楼', email: null, website: 'http://web.gdei.edu.cn/xyb/' },
        { typeCode: 5, section: '监控中心（紧急事务报告）', typeName: '医疗与救援', campus: '花都', majorPhone: '36967733', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 5, section: '物业安保24小时值班', typeName: '医疗与救援', campus: '花都', majorPhone: '36967733', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 5, section: '电梯应急救援（海珠校区）', typeName: '医疗与救援', campus: '海珠', majorPhone: '96333', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 5, section: '电梯应急救援', typeName: '医疗与救援', campus: '花都', majorPhone: '18837490602', minorPhone: '18837490597', address: null, email: null, website: null },
        { typeCode: 5, section: '医务室（海珠校区）', typeName: '医疗与救援', campus: '海珠', majorPhone: '34113271', minorPhone: null, address: '学生宿舍', email: null, website: null },
        { typeCode: 5, section: '医务室（花都校区）', typeName: '医疗与救援', campus: '花都', majorPhone: '36967710', minorPhone: null, address: '学生宿舍2栋1楼', email: null, website: null },
        { typeCode: 5, section: '医保办', typeName: '医疗与救援', campus: '海珠', majorPhone: '34113511', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 6, section: '水电中心值班处', typeName: '生活与保障', campus: '海珠', majorPhone: '34113363', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 6, section: '卡务中心', typeName: '生活与保障', campus: '花都', majorPhone: '36967715', minorPhone: null, address: '学生宿舍1栋1楼', email: null, website: null },
        { typeCode: 6, section: '5栋宿舍楼送水', typeName: '生活与保障', campus: '海珠', majorPhone: '15920841618', minorPhone: null, address: '学生宿舍5栋', email: null, website: null },
        { typeCode: 6, section: '培训楼招待处', typeName: '生活与保障', campus: '海珠', majorPhone: '34113500', minorPhone: null, address: '培训楼', email: null, website: null },
        { typeCode: 6, section: '体育中心', typeName: '生活与保障', campus: '海珠', majorPhone: '34113643', minorPhone: '34113232', address: '体育中心', email: null, website: null },
        { typeCode: 7, section: '后勤基建处（海珠校区）', typeName: '职能部门', campus: '海珠', majorPhone: '34113274', minorPhone: null, address: '综合楼9楼', email: null, website: 'http://web.gdei.edu.cn/hqc/' },
        { typeCode: 7, section: '后勤基建处（花都校区）', typeName: '职能部门', campus: '花都', majorPhone: '36967702', minorPhone: null, address: '图书馆705', email: null, website: 'http://web.gdei.edu.cn/hqc/' },
        { typeCode: 7, section: '饭堂负责人（海珠校区）', typeName: '职能部门', campus: '海珠', majorPhone: '13380052368', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 7, section: '饭堂负责人（花都校区第一饭堂）', typeName: '职能部门', campus: '花都', majorPhone: '13533936083', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 7, section: '饭堂负责人（花都校区第二饭堂）', typeName: '职能部门', campus: '花都', majorPhone: '15697632188', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 7, section: '饭堂负责人（花都校区第三饭堂）', typeName: '职能部门', campus: '花都', majorPhone: '13725408927', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 7, section: '图书馆（海珠校区）', typeName: '职能部门', campus: '海珠', majorPhone: '34113372', minorPhone: null, address: '图书馆大楼', email: null, website: 'http://lib.gdei.edu.cn/' },
        { typeCode: 7, section: '图书馆（花都校区）', typeName: '职能部门', campus: '花都', majorPhone: '39697731', minorPhone: null, address: '图书馆3楼大厅', email: null, website: 'http://lib.gdei.edu.cn/' },
        { typeCode: 7, section: '教务处（海珠校区）', typeName: '职能部门', campus: '海珠', majorPhone: '34113407', minorPhone: '34113249', address: '综合楼13楼', email: 'jwc@gdei.edu.cn', website: 'http://web.gdei.edu.cn/jwc/' },
        { typeCode: 7, section: '教务处（花都校区）', typeName: '职能部门', campus: '花都', majorPhone: '36967708', minorPhone: null, address: '图书馆703', email: null, website: 'http://web.gdei.edu.cn/jwc/' },
        { typeCode: 7, section: '财务处（海珠校区）', typeName: '职能部门', campus: '海珠', majorPhone: '34113275', minorPhone: null, address: '综合楼14楼', email: null, website: 'http://web.gdei.edu.cn/cwc/' },
        { typeCode: 7, section: '财务处（花都校区）', typeName: '职能部门', campus: '花都', majorPhone: '36967721', minorPhone: null, address: '学生宿舍3栋1楼', email: null, website: 'http://web.gdei.edu.cn/cwc/' },
        { typeCode: 7, section: '学生工作部（处）（海珠校区）', typeName: '职能部门', campus: '海珠', majorPhone: '34113360', minorPhone: null, address: '综合楼12楼', email: null, website: 'http://web.gdei.edu.cn/xsc/' },
        { typeCode: 7, section: '学生工作部（处）（花都校区）', typeName: '职能部门', campus: '花都', majorPhone: '36967703', minorPhone: null, address: '图书馆702', email: null, website: 'http://web.gdei.edu.cn/xsc/' },
        { typeCode: 7, section: '团委（海珠校区）', typeName: '职能部门', campus: '海珠', majorPhone: '34113325', minorPhone: null, address: '综合楼12楼', email: null, website: 'http://web.gdei.edu.cn/tw/' },
        { typeCode: 7, section: '团委（花都校区）', typeName: '职能部门', campus: '花都', majorPhone: '36967703', minorPhone: null, address: '图书馆702', email: null, website: 'http://web.gdei.edu.cn/tw/' },
        { typeCode: 8, section: '招生办公室', typeName: '就业创业', campus: '海珠', majorPhone: '34113327', minorPhone: null, address: '综合楼12楼', email: null, website: 'http://web.gdei.edu.cn/zsb/' },
        { typeCode: 8, section: '纪检监察处', typeName: '就业创业', campus: '海珠', majorPhone: '34113624', minorPhone: null, address: '综合楼18楼', email: 'jj@gdei.edu.cn', website: 'http://web.gdei.edu.cn/jwb/' },
        { typeCode: 8, section: '就业指导中心', typeName: '就业创业', campus: '海珠', majorPhone: '34114466', minorPhone: null, address: '综合楼12楼', email: null, website: 'http://210.38.64.162:9000/job' },
        { typeCode: 9, section: '就业创业咨询预约（海珠校区）', typeName: '院系部门', campus: '海珠', majorPhone: '34113716', minorPhone: null, address: '综合楼12楼1211室', email: 'jyzd@gdei.edu.cn', website: null },
        { typeCode: 9, section: '就业创业咨询预约（花都校区）', typeName: '院系部门', campus: '花都', majorPhone: '36967716', minorPhone: null, address: '图书馆2楼216室', email: 'jyzd@gdei.edu.cn', website: null },
        { typeCode: 9, section: '教育学院', typeName: '院系部门', campus: '海珠', majorPhone: '34113297', minorPhone: null, address: null, email: null, website: 'http://web.gdei.edu.cn/jyx/' },
        { typeCode: 9, section: '物理与信息工程系', typeName: '院系部门', campus: '海珠', majorPhone: '34113256', minorPhone: null, address: null, email: null, website: 'http://web.gdei.edu.cn/wlx/' },
        { typeCode: 9, section: '生物与食品工程学院', typeName: '院系部门', campus: '海珠', majorPhone: '34113257', minorPhone: null, address: null, email: null, website: 'http://web.gdei.edu.cn/swx/' },
        { typeCode: 9, section: '体育学院', typeName: '院系部门', campus: '海珠', majorPhone: '34113269', minorPhone: null, address: null, email: null, website: 'http://web.gdei.edu.cn/tyx/' },
        { typeCode: 9, section: '中文系', typeName: '院系部门', campus: '花都', majorPhone: '36967743', minorPhone: null, address: null, email: null, website: 'http://web.gdei.edu.cn/zwx/' },
        { typeCode: 9, section: '政法系', typeName: '院系部门', campus: '花都', majorPhone: '34113290', minorPhone: '34113397', address: null, email: null, website: 'http://web.gdei.edu.cn/zfx/' },
        { typeCode: 9, section: '外语系', typeName: '院系部门', campus: '花都', majorPhone: '36967750', minorPhone: '34113295', address: '实验楼509', email: null, website: 'http://web.gdei.edu.cn/wyx/' },
        { typeCode: 9, section: '数学系', typeName: '院系部门', campus: '花都', majorPhone: '34113296', minorPhone: '36967738', address: null, email: null, website: 'http://web.gdei.edu.cn/sxx/' },
        { typeCode: 9, section: '化学系', typeName: '院系部门', campus: '花都', majorPhone: '36967768', minorPhone: '34113456', address: null, email: null, website: 'http://web.gdei.edu.cn/hxx/' },
        { typeCode: 9, section: '计算机科学系', typeName: '院系部门', campus: '花都', majorPhone: '34115714', minorPhone: '36967761', address: null, email: null, website: 'http://web.gdei.edu.cn/jsjx/' },
        { typeCode: 9, section: '音乐系', typeName: '院系部门', campus: '花都', majorPhone: '36967776', minorPhone: '34114436', address: null, email: null, website: 'http://web.gdei.edu.cn/yyx/' },
        { typeCode: 9, section: '美术学院', typeName: '院系部门', campus: '花都', majorPhone: '36967771', minorPhone: '34113634', address: null, email: null, website: 'http://web.gdei.edu.cn/msx/' },
        { typeCode: 10, section: '国家邮政局申诉', typeName: '举报和申诉', campus: '国家', majorPhone: null, minorPhone: null, address: null, email: null, website: 'http://sswz.spb.gov.cn/' },
        { typeCode: 10, section: '司法服务热线', typeName: '举报和申诉', campus: '司法', majorPhone: '12368', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 10, section: '司法援助热线', typeName: '举报和申诉', campus: '司法', majorPhone: '12348', minorPhone: null, address: null, email: null, website: 'http://www.12348.gov.cn/' },
        { typeCode: 10, section: '国家工业和信息化部电信用户申诉', typeName: '举报和申诉', campus: '国家', majorPhone: '12300', minorPhone: null, address: '北京市西城区月坛南街11号电信用户申诉受理中心', email: 'accept@chinatcc.gov.cn', website: 'http://www.chinatcc.gov.cn:8080/cms/shensus/' },
        { typeCode: 10, section: '国家教育部统一监督举报', typeName: '举报和申诉', campus: '国家', majorPhone: '010-66092315', minorPhone: null, address: null, email: '12391@moe.edu.cn', website: 'http://www.moe.gov.cn/jyb_hygq/hygq_tsjb/201505/t20150520_184529.html' },
        { typeCode: 10, section: '国家信访局投诉和建议', typeName: '举报和申诉', campus: '国家', majorPhone: '010—68015310', minorPhone: null, address: '北京市西城区月坛南街8号', email: null, website: 'http://wsxf.gjxfj.gov.cn/zfp/webroot/index.html' },
        { typeCode: 10, section: '国家工商总局消费者维权申诉', typeName: '举报和申诉', campus: '国家', majorPhone: '12315', minorPhone: null, address: null, email: null, website: 'http://www.315.gov.cn' },
        { typeCode: 10, section: '广州市人民政府服务', typeName: '举报和申诉', campus: '市政', majorPhone: '12345', minorPhone: null, address: null, email: null, website: 'http://gz12345.gz.gov.cn/' },
        { typeCode: 11, section: '消防报警', typeName: '紧急求助', campus: '国家', majorPhone: '119', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 11, section: '医疗急救', typeName: '紧急求助', campus: '国家', majorPhone: '120', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 11, section: '交通事故报警', typeName: '紧急求助', campus: '国家', majorPhone: '122', minorPhone: null, address: null, email: null, website: null },
        { typeCode: 11, section: '短信报警', typeName: '紧急求助', campus: '国家', majorPhone: '12110', minorPhone: null, address: null, email: null, website: null }
      ]
    }
  }
})

// 资讯信息聚合：GET /api/information/list，与 info.jsp 结构一致（通知公告 / 校园公众号 / 专题阅读 / 世界上的今日）
Mock.mock(RegExp('.*/information/list.*'), 'get', () => {
  return {
    success: true,
    data: {
      notice: {
        title: '关于寒假期间校园安全管理的通知',
        publishTime: '2024-01-15',
        content: '请各学院做好寒假期间留校学生的安全教育管理工作，落实值班与报备制度；离校同学请注意旅途与居家安全，按时返校。祝全体师生新春愉快。'
      },
      accounts: [
        {
          name: '广东第二师范学院',
          description: '学校官方微信公众平台',
          avatar: 'https://via.placeholder.com/50/09bb07/fff?text=GDEI',
          article: '新学期开学安排通知',
          biz: 'MzI1NjM4OTIwMA=='
        },
        {
          name: '广二师教务处',
          description: '教务信息发布与服务',
          avatar: 'https://via.placeholder.com/50/10aeff/fff?text=EDU',
          article: '',
          biz: 'MzIzNDU2Nzg5MA=='
        }
      ],
      topics: [
        { title: '四六级备考指南', description: '名师指点，带你一次过级', link: 'https://mp.weixin.qq.com/s/example1' },
        { title: '毕业论文排版规范', description: '教务处官方排版要求解读', link: 'https://mp.weixin.qq.com/s/example2' }
      ],
      festival: {
        name: '国际母语日',
        description: [
          '联合国教科文组织于1999年设立，旨在促进语言和文化的多样性。',
          '母语是每个人最早接触的语言，承载着民族记忆与文化认同。'
        ]
      }
    }
  }
})

// 二手交易商品列表：GET /api/ershou/items，支持 query：type（分类 0–11）、keyword（标题/描述包含）
const ershouTitles = [
  '古驰GUCCI BLOOM香水 正品全新', '城野377美白精华 日期新没用过', 'CCTV 联名款礼盒 全新未拆',
  '紫色框眼镜 九成新', '大学英语四级真题 带解析', '自行车 校园代步 毕业出', 'iPhone 12 128G 自用一年',
  'MacBook Pro 2019 13寸', '索尼WH-1000XM4 降噪耳机', '佳能EOS M50 微单套机', '小冰箱 制冷良好',
  '瑜伽垫 加厚 送收纳袋', '秋冬卫衣 多色 仅试穿', '高数教材 第七版 有笔记', '宿舍小锅 功率合规',
  'Switch 健身环 全套', '收纳架 多层 可拆', '其他闲置 看图'
]
function buildErshouItemsList() {
  const count = 50
  const list = []
  for (let i = 0; i < count; i++) {
    list.push({
      id: i + 1,
      typeId: i % 12,
      title: ershouTitles[i % ershouTitles.length] + (i > 11 ? ` ${i}` : ''),
      desc: ['正品,全新,包装完好', '看图 日期新没用过', '二手自用 功能正常', '毕业甩卖 可小刀'][i % 4],
      price: [88, 180, 280, 350, 520, 880, 1200, 1999][i % 8],
      coverImg: `https://picsum.photos/400/400?random=${i + 1}`
    })
  }
  return list
}
Mock.mock(/\/api\/ershou\/items/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const typeParam = params.get('type')
  const keywordParam = params.get('keyword') ? params.get('keyword').trim() : ''
  const pageParam = params.get('page')
  const limitParam = params.get('limit')

  let list = buildErshouItemsList()
  if (typeParam !== null && typeParam !== '') {
    const typeNum = parseInt(typeParam, 10)
    if (!isNaN(typeNum) && typeNum >= 0 && typeNum <= 11) {
      list = list.filter((item) => item.typeId === typeNum)
    }
  }
  if (keywordParam !== '') {
    const k = keywordParam.toLowerCase()
    list = list.filter(
      (item) =>
        (item.title && item.title.toLowerCase().includes(k)) ||
        (item.desc && item.desc.toLowerCase().includes(k))
    )
  }

  const total = list.length
  const page = pageParam ? Math.max(1, parseInt(pageParam, 10) || 1) : 1
  const limit = limitParam ? Math.max(1, parseInt(limitParam, 10) || 10) : 10
  const start = (page - 1) * limit
  const end = start + limit
  const paginatedList = list.slice(start, end)
  const hasMore = end < total

  return {
    code: 200,
    data: {
      list: paginatedList,
      total,
      hasMore
    }
  }
})

// 二手交易商品详情：GET /api/ershou/item/:id（正则匹配 URL 解析 id）
Mock.mock(RegExp('^.*/api/ershou/item/\\d+$'), 'get', (options) => {
  const url = options.url || ''
  const id = url.replace(/.*\/item\//, '').replace(/\?.*$/, '') || '1'
  const numId = parseInt(id, 10) || 1
  return {
    success: true,
    data: {
      id: String(numId),
      seller: {
        avatar: '/img/avatar/default.png',
        name: '某某同学',
        publishTime: '2小时前',
        username: 'mockuser'
      },
      title: '九成新各种二手好物',
      price: '180.00',
      originalPrice: '300.00',
      desc: '这是详细的商品描述，记录了商品的新旧程度、购买渠道和转手原因。功能完好，无拆修，支持面交或快递，可小刀。',
      location: '花都校区 宿舍区',
      images: [
        'https://picsum.photos/800/800?random=21',
        'https://picsum.photos/800/800?random=22',
        'https://picsum.photos/800/800?random=23'
      ],
      contact: {
        qq: '12345678',
        phone: '13800000000'
      }
    }
  }
})

// 失物招领商品列表：GET /api/lostandfound/items，支持 query：page, limit, type (0: 寻物, 1: 招领)
const lostTitles = [
  '丢失iPhone 12手机', '寻找校园卡', '丢失身份证', '寻找银行卡', '丢失教材书',
  '寻找钥匙串', '丢失背包', '寻找外套', '丢失自行车', '寻找运动鞋',
  '丢失耳机', '其他物品丢失', '捡到手机一部', '捡到校园卡', '捡到身份证',
  '捡到银行卡', '捡到教材书', '捡到钥匙', '捡到背包', '捡到外套',
  '捡到自行车', '捡到运动鞋', '捡到耳机', '其他物品招领'
]
function buildLostAndFoundItemsList() {
  const count = 40
  const list = []
  for (let i = 0; i < count; i++) {
    list.push({
      id: i + 1,
      type: i % 2, // 0: 寻物, 1: 招领
      title: lostTitles[i % lostTitles.length] + (i > 23 ? ` ${i}` : ''),
      desc: ['物品特征描述', '详细情况说明', '如有拾到请联系', '感谢好心人'][i % 4],
      location: ['花都校区 教学楼', '海珠校区 图书馆', '花都校区 食堂', '海珠校区 宿舍区'][i % 4],
      time: ['2小时前', '5小时前', '1天前', '2天前', '3天前'][i % 5],
      contact: {
        qq: i % 3 === 0 ? String(10000000 + i) : '',
        wechat: i % 3 === 1 ? `wx_${i}` : '',
        phone: i % 3 === 2 ? `138${String(i).padStart(8, '0')}` : ''
      },
      images: [
        `https://picsum.photos/200/200?random=${i + 100}`,
        ...(i % 3 === 0 ? [`https://picsum.photos/200/200?random=${i + 200}`] : [])
      ]
    })
  }
  return list
}
Mock.mock(/\/api\/lostandfound\/items/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const typeParam = params.get('type')
  const pageParam = params.get('page')
  const limitParam = params.get('limit')

  let list = buildLostAndFoundItemsList()
  if (typeParam !== null && typeParam !== '') {
    const typeNum = parseInt(typeParam, 10)
    if (!isNaN(typeNum) && (typeNum === 0 || typeNum === 1)) {
      list = list.filter((item) => item.type === typeNum)
    }
  }

  const total = list.length
  const page = pageParam ? Math.max(1, parseInt(pageParam, 10) || 1) : 1
  const limit = limitParam ? Math.max(1, parseInt(limitParam, 10) || 10) : 10
  const start = (page - 1) * limit
  const end = start + limit
  const paginatedList = list.slice(start, end)
  const hasMore = end < total

  return {
    code: 200,
    data: {
      list: paginatedList,
      total,
      hasMore
    }
  }
})

// 失物招领商品详情：GET /api/lostandfound/item/:id
Mock.mock(RegExp('^.*/api/lostandfound/item/\\d+$'), 'get', (options) => {
  const url = options.url || ''
  const match = url.match(/\/api\/lostandfound\/item\/(\d+)$/)
  const id = match ? parseInt(match[1], 10) : 1
  const baseList = buildLostAndFoundItemsList()
  const baseItem = baseList[(id - 1) % baseList.length]

  return {
    code: 200,
    data: {
      id: String(id),
      type: baseItem.type,
      title: baseItem.title,
      desc: baseItem.desc + ' 这是更详细的描述信息，包括物品的具体特征、丢失/捡到的详细情况等。',
      location: baseItem.location,
      time: baseItem.time,
      seller: {
        avatar: '/img/avatar/default.png',
        name: '某某同学'
      },
      contact: baseItem.contact,
      images: baseItem.images.length > 0 ? baseItem.images : [
        'https://picsum.photos/800/800?random=' + (id + 100)
      ]
    }
  }
})

// ========== 校园树洞 (Secret) Mock API ==========

// 生成树洞列表数据
const secretContents = [
  '今天天气真好，心情也不错',
  '希望考试能顺利通过',
  '最近有点累，想休息一下',
  '遇到了一个很有趣的人',
  '今天学到了很多新知识',
  '有点想念家乡的味道',
  '希望明天会更好',
  '今天做了一件有意义的事',
  '心情有点复杂，不知道该怎么办',
  '感谢身边的朋友们',
  '今天看到了美丽的夕阳',
  '希望一切都能顺利',
  '有点紧张，但也很期待',
  '今天过得很充实',
  '想分享一些有趣的事情',
  '希望每个人都能开心',
  '今天是个特别的日子',
  '感谢所有帮助过我的人',
  '希望未来会更好',
  '今天心情不错，想分享给大家'
]

function buildSecretItemsList() {
  const count = 50
  const list = []
  for (let i = 0; i < count; i++) {
    const theme = (i % 12) + 1 // 1-12
    const type = i % 3 === 0 ? 0 : 1 // 0=文字, 1=语音
    list.push({
      id: i + 1,
      type,
      theme,
      content: type === 0 ? secretContents[i % secretContents.length] : '',
      audioUrl: type !== 0 ? `https://example.com/audio/${i + 1}.mp3` : '',
      likeCount: Math.floor(Math.random() * 100),
      commentCount: Math.floor(Math.random() * 50),
      liked: i % 3 === 0 ? 1 : 0, // 0=未点赞, 1=已点赞
      publishTime: new Date(Date.now() - i * 3600000).toISOString()
    })
  }
  return list
}

// 树洞列表：GET /api/secret/items（支持分页）
Mock.mock(/\/api\/secret\/items/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const pageParam = params.get('page')
  const limitParam = params.get('limit')

  const totalList = buildSecretItemsList()
  const total = totalList.length
  const page = pageParam ? Math.max(1, parseInt(pageParam, 10) || 1) : 1
  const limit = limitParam ? Math.max(1, parseInt(limitParam, 10) || 10) : 10
  const start = (page - 1) * limit
  const end = start + limit
  const paginatedList = totalList.slice(start, end)
  const hasMore = end < total

  return {
    code: 200,
    data: {
      list: paginatedList,
      total,
      hasMore
    }
  }
})

// 树洞详情：GET /api/secret/item/:id
Mock.mock(RegExp('^.*/api/secret/item/\\d+$'), 'get', (options) => {
  const url = options.url || ''
  const match = url.match(/\/api\/secret\/item\/(\d+)$/)
  const id = match ? parseInt(match[1], 10) : 1
  const baseList = buildSecretItemsList()
  const baseItem = baseList[(id - 1) % baseList.length]

  return {
    code: 200,
    data: {
      ...baseItem,
      id: String(id)
    }
  }
})

// 树洞评论列表：GET /api/secret/item/:id/comments
Mock.mock(RegExp('^.*/api/secret/item/\\d+/comments$'), 'get', (options) => {
  const url = options.url || ''
  const match = url.match(/\/api\/secret\/item\/(\d+)\/comments$/)
  const id = match ? parseInt(match[1], 10) : 1
  const commentCount = (id % 5) + 2 // 2-6条评论

  const comments = []
  for (let i = 0; i < commentCount; i++) {
    comments.push({
      id: i + 1,
      comment: `这是第${i + 1}条评论，内容很有趣`,
      avatarTheme: (i % 10) + 1,
      publishTime: new Date(Date.now() - i * 3600000).toLocaleString('zh-CN')
    })
  }

  return {
    code: 200,
    data: comments
  }
})

// 点赞/取消点赞：POST /api/secret/like/:id
Mock.mock(RegExp('^.*/api/secret/like/\\d+$'), 'post', (options) => {
  return {
    code: 200,
    success: true,
    message: '操作成功'
  }
})

// 提交评论：POST /api/secret/item/:id/comment
Mock.mock(RegExp('^.*/api/secret/item/\\d+/comment$'), 'post', (options) => {
  return {
    code: 200,
    success: true,
    message: '评论成功'
  }
})

// 发布树洞：POST /api/secret/info
Mock.mock(/\/api\/secret\/info/, 'post', (options) => {
  return {
    code: 200,
    success: true,
    message: '发布成功'
  }
})

// 我的树洞列表：GET /api/secret/profile
Mock.mock(/\/api\/secret\/profile/, 'get', (options) => {
  const baseList = buildSecretItemsList()
  // 返回前10条作为"我的树洞"
  const myList = baseList.slice(0, 10).map(item => ({
    id: item.id,
    type: item.type,
    content: item.content,
    publishTime: item.publishTime
  }))

  return {
    code: 200,
    data: myList
  }
})

// ========== 拍好校园 (Photograph) Mock API ==========

function buildPhotographList() {
  const list = []
  const titles = [
    '夕阳下的校园操场',
    '图书馆的一角',
    '雨后教学楼',
    '清晨的校园小路',
    '操场上的运动会',
    '实验楼的夜色',
    '校门口的日出',
    '宿舍楼前的晚霞',
    '食堂里的温暖灯光',
    '湖边的倒影'
  ]
  const authors = ['小明', '小红', '阿豪', '阿婷', '摄影社-阿杰', '摄影社-阿玲']

  for (let i = 0; i < 40; i++) {
    const id = i + 1
    const photoCount = (i % 4) + 1
    const images = []
    for (let j = 0; j < photoCount; j++) {
      images.push(`https://picsum.photos/600/800?random=${id + 300 + j}`)
    }
    list.push({
      id,
      type: (i % 3) + 1, // 1: 生活照, 2: 校园照, 3: 毕业照
      imgUrl: `https://picsum.photos/600/800?random=${id + 300}`,
      title: titles[i % titles.length] + (i > 20 ? ` #${i}` : ''),
      author: {
        name: authors[i % authors.length],
        avatar: `/img/avatar/${(i % 10) + 1}.png`
      },
      likes: Math.floor(Math.random() * 500),
      description: '这是一张关于校园的摄影作品，记录了美好的瞬间。',
      time: ['1分钟前', '5分钟前', '1小时前', '昨天', '2天前'][i % 5],
      photoCount,
      commentCount: Math.floor(Math.random() * 10),
      images
    })
  }
  return list
}

// 摄影作品列表：GET /api/photograph/items?page=&limit=
Mock.mock(/\/api\/photograph\/items/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const pageParam = params.get('page')
  const limitParam = params.get('limit')
  const typeParam = params.get('type')

  let totalList = buildPhotographList()
  if (typeParam) {
    const t = parseInt(typeParam, 10)
    if (!Number.isNaN(t)) {
      totalList = totalList.filter((item) => item.type === t)
    }
  }
  const total = totalList.length
  const page = pageParam ? Math.max(1, parseInt(pageParam, 10) || 1) : 1
  const limit = limitParam ? Math.max(1, parseInt(limitParam, 10) || 10) : 10
  const start = (page - 1) * limit
  const end = start + limit
  const paginatedList = totalList.slice(start, end)
  const hasMore = end < total

  return {
    code: 200,
    data: {
      list: paginatedList,
      total,
      hasMore
    }
  }
})

// ========== 校园表白墙 (Express) Mock API ==========

const datingContents = [
  '希望每天都能在图书馆遇见你',
  '谢谢你上次帮我捡起掉落的书',
  '从第一次见面就心动了',
  '想和你一起看校园的夕阳',
  '每次路过球场都会多看一眼',
  '希望你能注意到我',
  '谢谢你一直以来的陪伴',
  '想对你说一声谢谢',
  '希望未来能和你一起走下去',
  '你的笑容真的很治愈',
  '默默关注你很久了',
  '希望有机会认识你',
  '谢谢你让我相信爱情',
  '想和你分享生活的点滴',
  '每次见到你都很开心'
]

function buildDatingItemsList() {
  const count = 50
  const list = []
  const senders = ['小明', '小红', '阿杰', '阿婷', '小华', '小美', '阿强', '阿芳', '匿名', '某同学']
  const receivers = ['小丽', '小刚', '阿伟', '阿玲', '小芳', '小强', '阿明', '阿静', '某人', 'TA']
  const genders = ['male', 'female', 'secret']
  // 真实姓名池（用于猜名字游戏）
  const trueNames = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十', '郑一', '王二']

  for (let i = 0; i < count; i++) {
    const commentCount = Math.floor(Math.random() * 10)
    const comments = []
    for (let j = 0; j < commentCount; j++) {
      comments.push({
        id: j + 1,
        nickname: `同学${j + 1}`,
        comment: `这是第${j + 1}条评论，内容很有趣`,
        publishTime: ['1分钟前', '5分钟前', '1小时前', '昨天'][j % 4]
      })
    }
    // 随机决定是否可以猜名字（约 60% 可以猜，40% 匿名）
    const canGuess = Math.random() > 0.4
    list.push({
      id: i + 1,
      senderName: senders[i % senders.length] + (i > 9 ? ` ${i}` : ''),
      senderGender: genders[i % 3],
      senderTrueName: canGuess ? trueNames[i % trueNames.length] : '', // 只有 canGuess 为 true 时才有真实姓名
      canGuess: canGuess, // 是否可以参与猜名字游戏
      receiverName: receivers[i % receivers.length] + (i > 9 ? ` ${i}` : ''),
      receiverGender: genders[(i + 1) % 3],
      content: datingContents[i % datingContents.length],
      time: ['1分钟前', '5分钟前', '1小时前', '昨天', '2天前', '3天前'][i % 6],
      likeCount: Math.floor(Math.random() * 99),
      guessCount: Math.floor(Math.random() * 20),
      correctCount: 0, // 猜对的次数（默认为 0）
      commentCount: commentCount,
      comments: comments
    })
  }
  return list
}

// 表白墙列表：GET /api/express/items（支持分页、关键词搜索）
Mock.mock(/\/api\/express\/items/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const pageParam = params.get('page')
  const limitParam = params.get('limit')
  const keywordParam = params.get('keyword') ? params.get('keyword').trim() : ''

  let totalList = buildDatingItemsList()
  if (keywordParam !== '') {
    const k = keywordParam.toLowerCase()
    totalList = totalList.filter(
      (item) =>
        (item.senderName && item.senderName.toLowerCase().includes(k)) ||
        (item.receiverName && item.receiverName.toLowerCase().includes(k)) ||
        (item.content && item.content.toLowerCase().includes(k))
    )
  }

  const total = totalList.length
  const page = pageParam ? Math.max(1, parseInt(pageParam, 10) || 1) : 1
  const limit = limitParam ? Math.max(1, parseInt(limitParam, 10) || 10) : 10
  const start = (page - 1) * limit
  const end = start + limit
  const paginatedList = totalList.slice(start, end)
  const hasMore = end < total

  return {
    code: 200,
    data: {
      list: paginatedList,
      total,
      hasMore
    }
  }
})

// 发布表白：POST /api/express/publish
Mock.mock(/\/api\/express\/publish/, 'post', () => ({
  code: 200,
  success: true,
  message: '发布成功'
}))

// 表白详情：GET /api/express/item/:id
Mock.mock(RegExp('^.*/api/express/item/\\d+$'), 'get', (options) => {
  const url = options.url || ''
  const match = url.match(/\/api\/express\/item\/(\d+)$/)
  const id = match ? parseInt(match[1], 10) : 1
  const baseList = buildDatingItemsList()
  const baseItem = baseList[(id - 1) % baseList.length]
  return {
    code: 200,
    data: {
      ...baseItem,
      id: String(id)
    }
  }
})

// 提交猜测：POST /api/express/id/:id/guess
Mock.mock(RegExp('^.*/api/express/id/\\d+/guess$'), 'post', () => ({
  code: 200,
  success: true,
  message: '猜测已提交'
}))

// 提交评论：POST /api/express/id/:id/comment
Mock.mock(RegExp('^.*/api/express/id/\\d+/comment$'), 'post', () => ({
  code: 200,
  success: true,
  message: '评论成功'
}))

// 摄影作品详情：GET /api/photograph/item/:id
Mock.mock(RegExp('^.*/api/photograph/item/\\d+$'), 'get', (options) => {
  const url = options.url || ''
  const match = url.match(/\/api\/photograph\/item\/(\d+)$/)
  const id = match ? parseInt(match[1], 10) : 1
  const baseList = buildPhotographList()
  const baseItem = baseList[(id - 1) % baseList.length]
  const comments = []
  const count = baseItem.commentCount || 0
  for (let i = 0; i < count; i++) {
    comments.push({
      id: i + 1,
      author: `同学${i + 1}`,
      avatar: `/img/avatar/${(i % 10) + 1}.png`,
      text: '这张照片真好看！',
      time: new Date(Date.now() - i * 3600000).toLocaleString('zh-CN')
    })
  }

  return {
    code: 200,
    data: {
      ...baseItem,
      id: String(id),
      comments
    }
  }
})

// ========== 卖室友/交友 (Dating) Mock API ==========
const datingNames = ['小美', '阿婷', '小芳', '晓琳', '雨萱', '思琪', '小杰', '阿伟', '浩然', '子轩']
const datingFaculties = ['计算机科学', '汉语言文学', '教育学', '数学与应用数学', '英语', '心理学']
const datingHometowns = ['广州', '深圳', '佛山', '东莞', '汕头', '湛江', '梅州', '珠海']
const datingBios = [
  '喜欢看书和旅行，希望找一个一起上自习的TA',
  '性格开朗，爱运动，想认识更多朋友',
  '安静型，喜欢听歌，期待有共同话题的你',
  '吃货一枚，周末喜欢探店，找饭搭子',
  '热爱生活，喜欢拍照，希望遇见有趣的灵魂'
]

function buildDatingList() {
  const list = []
  for (let i = 0; i < 50; i++) {
    const area = i % 2 // 0 女 1 男
    const grade = (i % 4) + 1
    list.push({
      id: i + 1,
      area,
      name: datingNames[i % datingNames.length] + (i > 9 ? i : ''),
      gender: area === 0 ? 'female' : 'male',
      grade,
      faculty: datingFaculties[i % datingFaculties.length],
      hometown: datingHometowns[i % datingHometowns.length],
      bio: datingBios[i % datingBios.length],
      content: datingBios[i % datingBios.length],
      qq: '123456' + i,
      wechat: 'wx_' + (1000 + i),
      contactVisible: false,
      images: ['https://picsum.photos/seed/dating' + (i + 1) + '/400/400'],
      image: 'https://picsum.photos/seed/dating' + (i + 1) + '/400/400',
      likeCount: Math.floor(Math.random() * 99),
      isLiked: false
    })
  }
  return list
}

const datingListCache = buildDatingList()

// 卖室友列表：GET /api/dating/items（area: 0 小姐姐 1 小哥哥，分页）
Mock.mock(/\/api\/dating\/items/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const page = Math.max(1, parseInt(params.get('page') || '1', 10))
  const limit = Math.max(1, parseInt(params.get('limit') || '10', 10))
  const area = parseInt(params.get('area') || '0', 10)
  const filtered = datingListCache.filter((item) => item.area === area)
  const start = (page - 1) * limit
  const list = filtered.slice(start, start + limit)
  const hasMore = start + list.length < filtered.length
  return {
    code: 200,
    data: {
      list,
      total: filtered.length,
      hasMore
    }
  }
})

// 卖室友详情：GET /api/dating/item/:id
Mock.mock(RegExp('^.*/api/dating/item/\\d+$'), 'get', (options) => {
  const url = options.url || ''
  const match = url.match(/\/api\/dating\/item\/(\d+)$/)
  const id = match ? parseInt(match[1], 10) : 1
  const item = datingListCache[(id - 1) % datingListCache.length]
  return {
    code: 200,
    data: {
      ...item,
      id: String(id)
    }
  }
})

// 发布卖室友：POST /api/dating/publish（支持 FormData 或 JSON）
Mock.mock(/\/api\/dating\/publish/, 'post', () => ({
  code: 200,
  success: true,
  message: '发布成功'
}))

// 撩一下：POST /api/dating/pick
Mock.mock(/\/api\/dating\/pick/, 'post', () => ({
  code: 200,
  success: true,
  message: '发送成功'
}))

// ========== 卖室友互动中心 Mock API ==========

// 收到的撩：GET /api/dating/my/received
Mock.mock(/\/api\/dating\/my\/received/, 'get', () => {
  const list = []
  for (let i = 0; i < 8; i++) {
    list.push({
      id: i + 1,
      senderName: datingNames[i % datingNames.length] + (i > 4 ? i : ''),
      avatar: `https://picsum.photos/seed/received${i + 1}/100/100`,
      image: `https://picsum.photos/seed/received${i + 1}/100/100`,
      content: ['你好，可以认识一下吗？', '看了你的资料，感觉很有缘', '想和你交个朋友', '周末一起出去玩吗？'][i % 4],
      time: ['1小时前', '3小时前', '昨天', '2天前'][i % 4],
      status: i < 3 ? 0 : (i < 5 ? 1 : 2), // 0待处理 1已同意 2已拒绝
      contactVisible: i >= 3 && i < 5
    })
  }
  return {
    code: 200,
    data: list
  }
})

// 我发出的：GET /api/dating/my/sent
Mock.mock(/\/api\/dating\/my\/sent/, 'get', () => {
  const list = []
  for (let i = 0; i < 6; i++) {
    list.push({
      id: i + 1,
      targetName: datingNames[(i + 2) % datingNames.length] + (i > 2 ? i : ''),
      targetAvatar: `https://picsum.photos/seed/sent${i + 1}/100/100`,
      targetImage: `https://picsum.photos/seed/sent${i + 1}/100/100`,
      content: ['你好，想认识一下', '看了你的资料，很有兴趣', '可以加个微信吗？'][i % 3],
      status: i < 2 ? 0 : (i < 4 ? 1 : 2), // 0待处理 1已同意 2已拒绝
      targetQq: i >= 2 && i < 4 ? '123456789' : null,
      targetWechat: i >= 2 && i < 4 ? 'wx_friend_' + (i + 1) : null
    })
  }
  return {
    code: 200,
    data: list
  }
})

// 我的发布：GET /api/dating/my/posts
Mock.mock(/\/api\/dating\/my\/posts/, 'get', () => {
  const list = []
  for (let i = 0; i < 5; i++) {
    list.push({
      id: i + 1,
      name: datingNames[i % datingNames.length] + (i > 2 ? i : ''),
      images: [`https://picsum.photos/seed/post${i + 1}/200/200`],
      image: `https://picsum.photos/seed/post${i + 1}/200/200`,
      publishTime: ['1天前', '3天前', '1周前', '2周前', '1个月前'][i]
    })
  }
  return {
    code: 200,
    data: list
  }
})

// 同意请求：POST /api/dating/action/accept
Mock.mock(/\/api\/dating\/action\/accept/, 'post', () => ({
  code: 200,
  success: true,
  message: '已同意'
}))

// 拒绝请求：POST /api/dating/action/reject
Mock.mock(/\/api\/dating\/action\/reject/, 'post', () => ({
  code: 200,
  success: true,
  message: '已拒绝'
}))

// 下架发布：POST /api/dating/action/delete
Mock.mock(/\/api\/dating\/action\/delete/, 'post', () => ({
  code: 200,
  success: true,
  message: '已下架'
}))

// ========== 校园话题 (Topic) Mock API ==========
const topicUsers = ['小美', '阿婷', '小芳', '晓琳', '雨萱', '思琪', '小杰', '阿伟', '浩然', '子轩']
const topicTags = ['#交友#', '#学习#', '#生活#', '#运动#', '#美食#', '#旅行#', '#音乐#', '#电影#']
const topicContents = [
  '今天天气真好，适合出去走走',
  '有没有一起上自习的小伙伴？',
  '推荐一家超好吃的餐厅！',
  '周末想去爬山，有人一起吗？',
  '最近在学摄影，分享几张作品',
  '这个学期课程好难啊',
  '有没有喜欢打篮球的朋友？',
  '分享一个学习小技巧',
  '今天看到一只超可爱的小猫',
  '有没有推荐的电影？'
]

function buildTopicList() {
  const list = []
  for (let i = 0; i < 30; i++) {
    const imageCount = i % 10 === 0 ? 1 : (i % 10 === 1 ? 3 : (i % 10 === 2 ? 9 : Math.floor(Math.random() * 5)))
    const images = []
    for (let j = 0; j < imageCount; j++) {
      images.push(`https://picsum.photos/seed/topic${i}_${j}/400/400`)
    }
    list.push({
      id: i + 1,
      userAvatar: `https://picsum.photos/seed/avatar${i}/100/100`,
      userName: topicUsers[i % topicUsers.length] + (i > 9 ? i : ''),
      time: ['刚刚', '5分钟前', '1小时前', '昨天', '2天前'][i % 5],
      topicTag: topicTags[i % topicTags.length],
      content: topicContents[i % topicContents.length],
      images: images,
      likeCount: Math.floor(Math.random() * 99),
      isLiked: Math.random() > 0.7
    })
  }
  return list
}

const topicListCache = buildTopicList()

// 话题列表：GET /api/topic/items（分页）
Mock.mock(/\/api\/topic\/items/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const page = Math.max(1, parseInt(params.get('page') || '1', 10))
  const limit = Math.max(1, parseInt(params.get('limit') || '10', 10))
  const start = (page - 1) * limit
  const list = topicListCache.slice(start, start + limit)
  const hasMore = start + list.length < topicListCache.length
  return {
    code: 200,
    data: {
      list,
      total: topicListCache.length,
      hasMore
    }
  }
})

// 搜索话题：GET /api/topic/search
Mock.mock(/\/api\/topic\/search/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const keyword = (params.get('keyword') || '').toLowerCase()
  if (!keyword) {
    return { code: 200, data: [] }
  }
  const filtered = topicListCache.filter(item =>
    item.topicTag.toLowerCase().includes(keyword) ||
    item.content.toLowerCase().includes(keyword) ||
    item.userName.toLowerCase().includes(keyword)
  )
  return {
    code: 200,
    data: filtered.slice(0, 20)
  }
})

// 发布话题：POST /api/topic/publish
Mock.mock(/\/api\/topic\/publish/, 'post', () => ({
  code: 200,
  success: true,
  message: '发布成功'
}))

// 点赞话题：POST /api/topic/like
Mock.mock(/\/api\/topic\/like/, 'post', () => ({
  code: 200,
  success: true,
  message: '操作成功'
}))

// ========== 全民快递/校园跑腿模块 Mock 数据 ==========
const deliveryPickupAddresses = ['菜鸟驿站', '南门快递点', '北门快递点', '东门快递点', '顺丰快递点', '京东快递点']
const deliveryDeliveryAddresses = ['南苑1栋301', '南苑2栋205', '北苑3栋401', '东苑4栋102', '西苑5栋203', '南苑6栋305']
const deliveryTypes = ['express', 'food', 'other']
const deliverySizes = ['small', 'medium', 'large']
const deliveryDescriptions = ['请小心轻放', '急用，谢谢', '请尽快送达', '物品较重', '请送到楼下即可', '']

function buildDeliveryList() {
  const list = []
  const currentUserId = 'user123' // Mock当前用户ID
  
  for (let i = 0; i < 50; i++) {
    const status = i % 3 // 0待接单, 1配送中, 2已完成
    const type = deliveryTypes[i % deliveryTypes.length]
    const size = deliverySizes[i % deliverySizes.length]
    
    // 满足「我的跑腿」列表展示所需数据量
    // 前15个任务：user123发布的（5个待接单，5个配送中，5个已完成）
    // 第16-30个任务：user123接的单（5个配送中，5个已完成，5个已完成）
    let publisherId, runnerId
    
    if (i < 15) {
      // 我发布的任务
      publisherId = currentUserId
      runnerId = status === 0 ? null : (status === 1 ? currentUserId : `runner${(i % 5) + 1}`)
    } else if (i >= 15 && i < 30) {
      // 我接的单
      publisherId = `user${(i % 10) + 1}`
      runnerId = status === 0 ? null : currentUserId
    } else {
      // 其他用户的任务
      publisherId = `user${(i % 10) + 1}`
      runnerId = status === 0 ? null : `runner${(i % 5) + 1}`
    }
    
    list.push({
      id: i + 1,
      type: type,
      pickupAddress: deliveryPickupAddresses[i % deliveryPickupAddresses.length],
      pickupCode: status === 0 ? null : `1-2-${String(i + 1000).slice(-4)}`,
      pickupImage: status === 0 ? null : `https://picsum.photos/seed/delivery${i}/300/300`,
      deliveryAddress: deliveryDeliveryAddresses[i % deliveryDeliveryAddresses.length],
      contactPhone: status === 0 ? null : `138${String(i + 1000000).slice(-8)}`,
      size: size,
      description: deliveryDescriptions[i % deliveryDescriptions.length] || null,
      reward: parseFloat((Math.random() * 20 + 3).toFixed(2)), // 3-23元
      status: status,
      time: ['刚刚', '5分钟前', '10分钟前', '30分钟前', '1小时前', '2小时前', '昨天'][i % 7],
      publisherId: publisherId,
      runnerId: runnerId
    })
  }
  return list
}

const deliveryListCache = buildDeliveryList()

// 任务列表：GET /api/delivery/items（分页，支持status筛选）
Mock.mock(/\/api\/delivery\/items/, 'get', (options) => {
  const url = options.url || ''
  const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
  const params = new URLSearchParams(query)
  const page = Math.max(1, parseInt(params.get('page') || '1', 10))
  const limit = Math.max(1, parseInt(params.get('limit') || '10', 10))
  const statusFilter = params.get('status') || ''
  
  let filtered = deliveryListCache
  if (statusFilter) {
    const statusMap = { 'pending': 0, 'delivering': 1, 'completed': 2 }
    const statusNum = statusMap[statusFilter]
    if (statusNum !== undefined) {
      filtered = deliveryListCache.filter(item => item.status === statusNum)
    }
  }
  
  const start = (page - 1) * limit
  const list = filtered.slice(start, start + limit)
  const hasMore = start + list.length < filtered.length
  
  return {
    code: 200,
    data: {
      list,
      total: filtered.length,
      hasMore
    }
  }
})

// 任务详情：GET /api/delivery/item/:id
Mock.mock(/\/api\/delivery\/item\/\d+/, 'get', (options) => {
  const url = options.url || ''
  const id = parseInt(url.match(/\/(\d+)$/)?.[1] || '0', 10)
  const item = deliveryListCache.find(d => d.id === id)
  if (!item) {
    return { code: 404, message: '任务不存在' }
  }
  return {
    code: 200,
    data: item
  }
})

// 发布任务：POST /api/delivery/publish
Mock.mock(/\/api\/delivery\/publish/, 'post', () => ({
  code: 200,
  success: true,
  message: '发布成功',
  data: { id: deliveryListCache.length + 1 }
}))

// 接单：POST /api/delivery/accept
Mock.mock(/\/api\/delivery\/accept/, 'post', (options) => {
  const body = JSON.parse(options.body || '{}')
  const id = body.id
  const item = deliveryListCache.find(d => d.id === id)
  if (!item) {
    return { code: 404, message: '任务不存在' }
  }
  if (item.status !== 0) {
    return { code: 400, message: '任务已被接单' }
  }
  item.status = 1
  item.runnerId = 'user123' // Mock当前用户ID
  return {
    code: 200,
    success: true,
    message: '接单成功'
  }
})

// 完成订单：POST /api/delivery/complete（仅发布者可操作）
Mock.mock(/\/api\/delivery\/complete/, 'post', (options) => {
  const body = JSON.parse(options.body || '{}')
  const id = body.id
  const item = deliveryListCache.find(d => d.id === id)
  if (!item) {
    return { code: 404, message: '任务不存在' }
  }
  if (item.status !== 1) {
    return { code: 400, message: '订单状态不正确，无法完成' }
  }
  const currentUserId = 'user123' // Mock当前用户ID
  if (item.publisherId !== currentUserId) {
    return { code: 403, message: '只有发布者可以确认完成订单' }
  }
  item.status = 2 // 已完成
  return {
    code: 200,
    success: true,
    message: '订单已完成'
  }
})

// 我发布的任务：GET /api/delivery/my/published
Mock.mock(/\/api\/delivery\/my\/published/, 'get', () => {
  const currentUserId = 'user123' // Mock当前用户ID
  const published = deliveryListCache.filter(item => item.publisherId === currentUserId)
  return {
    code: 200,
    data: published
  }
})

// 我接的单：GET /api/delivery/my/accepted
Mock.mock(/\/api\/delivery\/my\/accepted/, 'get', () => {
  const currentUserId = 'user123' // Mock当前用户ID
  const accepted = deliveryListCache.filter(item => item.runnerId === currentUserId)
  return {
    code: 200,
    data: accepted
  }
})

// 用户反馈：POST /api/user/feedback
Mock.mock(/\/api\/user\/feedback/, 'post', () => ({
  code: 200,
  success: true,
  message: '反馈已接收，我们会尽快处理'
}))

// ========== 用户数据导出模块 Mock 数据 ==========
// 导出状态存储（生产环境建议使用 session 或 redis）
let userDataExportStatus = 0 // 0: 未导出, 1: 导出中, 2: 已导出
let exportTimer = null

// 检查导出状态：GET /api/userdata/state
Mock.mock(/\/api\/userdata\/state/, 'get', () => {
  return {
    code: 200,
    success: true,
    data: userDataExportStatus
  }
})

// 提交导出请求：POST /api/userdata/export
Mock.mock(/\/api\/userdata\/export/, 'post', () => {
  if (userDataExportStatus === 1) {
    return {
      code: 200,
      success: false,
      message: '系统正在导出用户数据，请稍候再返回下载'
    }
  }
  if (userDataExportStatus === 2) {
    return {
      code: 200,
      success: false,
      message: '24小时内已导出过用户数据，请勿重复提交请求'
    }
  }
  
  // 开始导出
  userDataExportStatus = 1
  
  // 模拟异步打包过程（3-5秒后完成）
  const delay = 3000 + Math.random() * 2000
  if (exportTimer) clearTimeout(exportTimer)
  exportTimer = setTimeout(() => {
    userDataExportStatus = 2
  }, delay)
  
  return {
    code: 200,
    success: true,
    message: '已提交用户数据导出请求'
  }
})

// 获取下载链接：POST /api/userdata/download
Mock.mock(/\/api\/userdata\/download/, 'post', () => {
  if (userDataExportStatus !== 2) {
    return {
      code: 200,
      success: false,
      message: '请先提交用户数据导出请求'
    }
  }
  
  // 模拟下载链接（生产环境为 OSS 预签名 URL，前端可用 Blob 模拟下载）
  const mockDownloadUrl = 'mock://userdata-export.zip'
  
  return {
    code: 200,
    success: true,
    data: mockDownloadUrl
  }
})

// 注销账户：POST /api/user/delete
Mock.mock(/\/api\/user\/delete/, 'post', () => {
  // 注销接口 Mock
  return {
    code: 200,
    success: true,
    message: '账号已注销'
  }
})

// 发送邮箱验证码：POST /api/user/send-email-code
Mock.mock(/\/api\/user\/send-email-code/, 'post', () => {
  return {
    code: 200,
    success: true,
    message: '验证码已发送，请检查邮箱'
  }
})

// 绑定邮箱：POST /api/user/bind-email
Mock.mock(/\/api\/user\/bind-email/, 'post', () => {
  return {
    code: 200,
    success: true,
    message: '绑定成功'
  }
})

// 获取邮箱绑定状态：GET /api/user/email-status
let mockCurrentEmail = '123***@qq.com'
Mock.mock(/\/api\/user\/email-status/, 'get', () => {
  return {
    code: 200,
    success: true,
    data: mockCurrentEmail
  }
})

// 解除绑定邮箱：POST /api/user/unbind-email
Mock.mock(/\/api\/user\/unbind-email/, 'post', () => {
  mockCurrentEmail = ''
  return {
    code: 200,
    success: true,
    message: '已解除绑定'
  }
})

// ========== 绑定手机模块 Mock 数据 ==========
// 获取手机绑定状态：GET /api/user/phone-status
let mockCurrentPhone = '138****1234'
let mockBoundCountryCode = '+86'
Mock.mock(/\/api\/user\/phone-status/, 'get', () => {
  return {
    code: 200,
    success: true,
    data: {
      phone: mockCurrentPhone,
      countryCode: mockBoundCountryCode
    }
  }
})

// 发送手机验证码：POST /api/user/send-phone-code
Mock.mock(/\/api\/user\/send-phone-code/, 'post', () => {
  return {
    code: 200,
    success: true,
    message: '验证码已发送，请查看短信'
  }
})

// 绑定手机：POST /api/user/bind-phone
Mock.mock(/\/api\/user\/bind-phone/, 'post', (options) => {
  const body = JSON.parse(options.body || '{}')
  if (body.phone) {
    // 模拟脱敏显示
    const phone = body.phone
    if (phone.length === 11) {
      mockCurrentPhone = `${phone.substring(0, 3)}****${phone.substring(7)}`
    } else if (phone.length >= 5) {
      mockCurrentPhone = `${phone.substring(0, 3)}****${phone.substring(phone.length - 2)}`
    } else {
      mockCurrentPhone = phone
    }
    mockBoundCountryCode = body.countryCode || '+86'
  }
  return {
    code: 200,
    success: true,
    message: '绑定成功'
  }
})

// 解除绑定手机：POST /api/user/unbind-phone
Mock.mock(/\/api\/user\/unbind-phone/, 'post', () => {
  mockCurrentPhone = ''
  mockBoundCountryCode = '+86'
  return {
    code: 200,
    success: true,
    message: '已解除绑定'
  }
})

// ========== 用户信息模块 Mock 数据 ==========
// 获取用户信息：GET /api/user/info
Mock.mock(/\/api\/user\/info/, 'get', () => {
  return {
    success: true,
    data: {
      avatar: '/img/avatar/default.png',
      username: 'testuser',
      nickname: '二师小助手',
      ipArea: '中国广东广州',
      faculty: '计算机学院',
      major: '软件工程',
      enrollment: '2022',
      age: '22',
      location: '广东省广州市',
      hometown: '广东',
      introduction: '这是一个测试用户的个人简介，用于展示个人信息页面的功能。',
      birthday: '2002-05-15',
      college: '11',
      enrollYear: 2022,
      location: '44',
      hometown: '44'
    }
  }
})

// 更新个人资料：POST /api/user/profile/update
Mock.mock(/\/api\/user\/profile\/update/, 'post', (options) => {
  const body = typeof options.body === 'string' ? JSON.parse(options.body || '{}') : (options.body || {})
  // 模拟保存成功
  return {
    success: true,
    message: '保存成功'
  }
})

// ========== 隐私设置模块 Mock 数据 ==========
// 模拟隐私设置数据存储
let mockPrivacySettings = {
  facultyOpen: false,
  majorOpen: false,
  locationOpen: false,
  hometownOpen: false,
  introductionOpen: false,
  enrollmentOpen: false,
  ageOpen: false,
  cacheAllow: false,
  robotsIndexAllow: false
}

// 获取隐私设置：GET /api/privacy
Mock.mock(/\/api\/privacy/, 'get', () => {
  return {
    success: true,
    data: { ...mockPrivacySettings }
  }
})

// 更新隐私设置：POST /api/privacy?tag=FACULTY&state=true
Mock.mock(/\/api\/privacy/, 'post', (options) => {
  // 从 URL 或 config.params 中获取参数
  let tag = ''
  let state = false
  
  if (options.url) {
    const url = options.url || ''
    const query = url.indexOf('?') >= 0 ? url.split('?')[1] : ''
    const params = new URLSearchParams(query)
    tag = params.get('tag') || ''
    state = params.get('state') === 'true'
  } else if (options.config && options.config.params) {
    tag = options.config.params.tag || ''
    state = options.config.params.state === true || options.config.params.state === 'true'
  }

  // 映射 tag 到字段名
  const tagToField = {
    FACULTY: 'facultyOpen',
    MAJOR: 'majorOpen',
    LOCATION: 'locationOpen',
    HOMETOWN: 'hometownOpen',
    INTRODUCTION: 'introductionOpen',
    ENROLLMENT: 'enrollmentOpen',
    AGE: 'ageOpen',
    CACHE: 'cacheAllow',
    ROBOTS: 'robotsIndexAllow'
  }

  const field = tagToField[tag.toUpperCase()]
  if (!field) {
    return {
      success: false,
      message: '请求参数不合法'
    }
  }

  // 更新设置
  mockPrivacySettings[field] = state

  return {
    success: true
  }
})
