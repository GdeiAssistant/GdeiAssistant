/**
 * GdeiAssistant MongoDB 初始化脚本（开箱即用）
 * 执行方式: mongosh < init.js  或  mongo < init.js（旧版）
 * 用途：为测试账号 gdeiassistant 提供成绩缓存、课表缓存、馆藏/借阅模拟数据，便于试用功能。
 * 重要：trial 集合必须包含 type=spare（空课室）、type=card（饭卡/消费记录）等文档，
 *       否则测试账号访问空课室、饭卡时会提示「样板间数据未初始化」。
 */
const dbName = 'gdeiassistant';
db = db.getSiblingDB(dbName);

// --------- 成绩缓存 grade（测试账号 gdeiassistant） ---------
db.grade.deleteMany({ username: 'gdeiassistant' });
db.grade.insertOne({
  username: 'gdeiassistant',
  gradeLists: [
    [
      { gradeYear: '2023', gradeTerm: '1', gradeId: 'CS101', gradeName: '高等数学A(1)', gradeCredit: '4', gradeType: '必修', gradeGpa: '4.0', gradeScore: '92' },
      { gradeYear: '2023', gradeTerm: '1', gradeId: 'CS102', gradeName: '大学英语(1)', gradeCredit: '3', gradeType: '必修', gradeGpa: '3.7', gradeScore: '87' }
    ]
  ],
  firstTermGPAList: [4.0, 3.7],
  firstTermIGPList: [4.0, 3.7],
  secondTermGPAList: [],
  secondTermIGPList: [],
  updateDateTime: new Date()
});

// --------- 课表缓存 schedule ---------
db.schedule.deleteMany({ username: 'gdeiassistant' });
db.schedule.insertOne({
  username: 'gdeiassistant',
  scheduleList: [
    {
      id: '1',
      scheduleName: '高等数学A',
      scheduleType: '必修',
      scheduleLesson: '1-2',
      scheduleTeacher: '张老师',
      scheduleLocation: '综合楼B101',
      scheduleLength: 2,
      scheduleWeek: '1-16',
      minScheduleWeek: 1,
      maxScheduleWeek: 16,
      position: 0,
      row: 0,
      column: 0,
      colorCode: '#4CAF50'
    },
    {
      id: '2',
      scheduleName: '大学英语',
      scheduleType: '必修',
      scheduleLesson: '3-4',
      scheduleTeacher: '李老师',
      scheduleLocation: '教学楼A201',
      scheduleLength: 2,
      scheduleWeek: '1-16',
      minScheduleWeek: 1,
      maxScheduleWeek: 16,
      position: 14,
      row: 2,
      column: 0,
      colorCode: '#2196F3'
    }
  ],
  updateDateTime: new Date()
});

// --------- 馆藏模拟数据 collection（测试账号馆藏检索/详情） ---------
db.collection.deleteMany({ username: 'gdeiassistant' });
db.collection.insertOne({
  username: 'gdeiassistant',
  collectionList: [
    { bookname: 'Java核心技术 卷I', author: 'Cay S. Horstmann', publishingHouse: '机械工业出版社', detailURL: 'detailURL=test001' },
    { bookname: '深入理解计算机系统', author: 'Randal E. Bryant', publishingHouse: '机械工业出版社', detailURL: 'detailURL=test002' }
  ],
  collectionDetailList: [
    {
      detailURL: 'detailURL=test001',
      detail: {
        bookname: 'Java核心技术 卷I',
        author: 'Cay S. Horstmann',
        principal: '霍斯特曼',
        publishingHouse: '机械工业出版社 2019',
        price: 'ISBN 978-7-111-61301-9',
        physicalDescriptionArea: 'xxx页',
        personalPrincipal: '霍斯特曼',
        subjectTheme: 'Java',
        chineseLibraryClassification: 'TP312',
        collectionDistributionList: [
          { barcode: 'B001', callNumber: 'TP312/123', location: '海珠馆科技书库', state: '可借' }
        ]
      }
    }
  ]
});

// --------- 借阅列表模拟 book ---------
db.book.deleteMany({ username: 'gdeiassistant' });
db.book.insertOne({
  username: 'gdeiassistant',
  bookList: [
    { id: 'BAR001', name: 'Java核心技术 卷I', author: 'Cay S. Horstmann', borrowDate: '2024-02-01', returnDate: '2024-03-01', renewTime: 0, sn: 'SN001', code: 'CODE001' }
  ]
});

// --------- 样板间 trial 集合：grade / schedule ---------
// 说明：
// TrialDataService.loadTrialData("grade"/"schedule") 会从 trial 集合按 type 字段读取数据，
// 并将 data 反序列化为 GradeQueryResult / ScheduleQueryResult。
// 因此前端样板间依赖的结构必须如下所示。

db.trial.deleteMany({ type: { $in: ['grade', 'schedule', 'collection', 'collection_detail', 'book', 'card', 'spare'] } });
db.trial.insertMany([
  {
    type: 'grade',
    data: {
      year: 0,
      firstTermGPA: 0.0,
      firstTermIGP: 0.0,
      secondTermGPA: 0.0,
      secondTermIGP: 0.0,
      firstTermGradeList: [
        {
          gradeYear: '2023',
          gradeTerm: '1',
          gradeId: 'CS101',
          gradeName: '高等数学A(1)',
          gradeCredit: '4',
          gradeType: '必修',
          gradeGpa: '4.0',
          gradeScore: '92'
        },
        {
          gradeYear: '2023',
          gradeTerm: '1',
          gradeId: 'CS102',
          gradeName: '大学英语(1)',
          gradeCredit: '3',
          gradeType: '必修',
          gradeGpa: '3.7',
          gradeScore: '87'
        }
      ],
      secondTermGradeList: []
    },
    updateDateTime: new Date()
  },
  {
    type: 'schedule',
    data: {
      week: 1,
      scheduleList: [
        {
          id: '1',
          scheduleName: '高等数学A',
          scheduleType: '必修',
          scheduleLesson: '1-2',
          scheduleTeacher: '张老师',
          scheduleLocation: '综合楼B101',
          scheduleLength: 2,
          scheduleWeek: '1-16',
          minScheduleWeek: 1,
          maxScheduleWeek: 16,
          position: 0,
          row: 0,
          column: 0,
          colorCode: '#4CAF50'
        },
        {
          id: '2',
          scheduleName: '大学英语',
          scheduleType: '必修',
          scheduleLesson: '3-4',
          scheduleTeacher: '李老师',
          scheduleLocation: '教学楼A201',
          scheduleLength: 2,
          scheduleWeek: '1-16',
          minScheduleWeek: 1,
          maxScheduleWeek: 16,
          position: 14,
          row: 2,
          column: 0,
          colorCode: '#2196F3'
        }
      ]
    },
    updateDateTime: new Date()
  },
  {
    // 馆藏检索列表（对应 CollectionQueryResult）
    type: 'collection',
    data: {
      sumPage: 1,
      collectionList: [
        {
          bookname: 'Java核心技术 卷I',
          author: 'Cay S. Horstmann',
          publishingHouse: '机械工业出版社',
          detailURL: 'detailURL=test001'
        },
        {
          bookname: '深入理解计算机系统',
          author: 'Randal E. Bryant',
          publishingHouse: '机械工业出版社',
          detailURL: 'detailURL=test002'
        }
      ]
    },
    updateDateTime: new Date()
  },
  {
    // 馆藏详情（对应 CollectionDetail）
    type: 'collection_detail',
    data: {
      bookname: 'Java核心技术 卷I',
      author: 'Cay S. Horstmann',
      principal: '霍斯特曼',
      publishingHouse: '机械工业出版社 2019',
      price: 'ISBN 978-7-111-61301-9',
      physicalDescriptionArea: 'xxx页',
      personalPrincipal: '霍斯特曼',
      subjectTheme: 'Java',
      chineseLibraryClassification: 'TP312',
      collectionDistributionList: [
        {
          barcode: 'B001',
          callNumber: 'TP312/123',
          location: '海珠馆科技书库',
          state: '可借'
        }
      ]
    },
    updateDateTime: new Date()
  },
  {
    // 图书借阅列表（对应 List<Book>）
    type: 'book',
    data: [
      {
        id: 'BAR001',
        name: 'Java核心技术 卷I',
        author: 'Cay S. Horstmann',
        borrowDate: '2024-02-01',
        returnDate: '2024-03-01',
        renewTime: 0,
        sn: 'SN001',
        code: 'CODE001'
      }
    ],
    updateDateTime: new Date()
  },
  {
    // 校园卡信息与消费流水（对应 CardQueryResult）
    type: 'card',
    data: {
      cardQuery: {
        year: 2024,
        month: 1,
        date: 1
      },
      cardInfo: {
        name: '测试用户',
        number: '20240001',
        cardBalance: '100.00',
        cardInterimBalance: '0.00',
        cardNumber: '66880001',
        cardLostState: '正常',
        cardFreezeState: '正常'
      },
      cardList: [
        {
          tradeTime: '2024-01-01 12:00:00',
          merchantName: '一饭堂',
          tradeName: '一卡通消费',
          tradePrice: '-10.00',
          accountBalance: '90.00'
        },
        {
          tradeTime: '2024-01-02 08:30:00',
          merchantName: '早餐档口',
          tradeName: '一卡通消费',
          tradePrice: '-5.50',
          accountBalance: '84.50'
        }
      ]
    },
    updateDateTime: new Date()
  },
  {
    // 空课室查询结果（对应 List<SpareRoom>）
    type: 'spare',
    data: [
      {
        number: 'A101',
        name: '第一教学楼 A101',
        type: '多媒体教室',
        zone: '大学城校区',
        classSeating: '80',
        section: '信息学院',
        examSeating: '60'
      },
      {
        number: 'B202',
        name: '第二教学楼 B202',
        type: '普通教室',
        zone: '大学城校区',
        classSeating: '60',
        section: '数学学院',
        examSeating: '50'
      }
    ],
    updateDateTime: new Date()
  }
]);

print('MongoDB init.js 执行完成，数据库: ' + dbName + '，测试用户: gdeiassistant');
