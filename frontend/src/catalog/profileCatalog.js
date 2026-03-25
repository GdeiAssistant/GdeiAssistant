import i18n from '../i18n'

const FACULTY_DEFINITIONS = [
  { code: 0, key: 'unselected', majors: ['unselected'] },
  { code: 1, key: 'faculty.education', majors: ['unselected', 'education', 'preschool_education', 'primary_education', 'special_education'] },
  { code: 2, key: 'faculty.politics_law', majors: ['unselected', 'law', 'ideological_political_education', 'social_work'] },
  { code: 3, key: 'faculty.chinese', majors: ['unselected', 'chinese_language_literature', 'history', 'secretarial_studies'] },
  { code: 4, key: 'faculty.mathematics', majors: ['unselected', 'mathematics_applied_mathematics', 'information_computing_science', 'statistics'] },
  { code: 5, key: 'faculty.foreign_languages', majors: ['unselected', 'english', 'business_english', 'japanese', 'translation'] },
  { code: 6, key: 'faculty.physics_information', majors: ['unselected', 'physics', 'electronic_information_engineering', 'communication_engineering'] },
  { code: 7, key: 'faculty.chemistry', majors: ['unselected', 'chemistry', 'applied_chemistry', 'materials_chemistry'] },
  { code: 8, key: 'faculty.biology_food', majors: ['unselected', 'biological_science', 'biotechnology', 'food_science_engineering'] },
  { code: 9, key: 'faculty.physical_education', majors: ['unselected', 'physical_education', 'social_sports_guidance_management'] },
  { code: 10, key: 'faculty.fine_arts', majors: ['unselected', 'fine_arts', 'visual_communication_design', 'environmental_design'] },
  { code: 11, key: 'faculty.computer_science', majors: ['unselected', 'software_engineering', 'network_engineering', 'computer_science_technology', 'internet_of_things_engineering'] },
  { code: 12, key: 'faculty.music', majors: ['unselected', 'musicology', 'music_performance', 'dance'] },
  { code: 13, key: 'faculty.teacher_training', majors: ['unselected', 'education', 'educational_technology'] },
  { code: 14, key: 'faculty.continuing_education', majors: ['unselected', 'chinese_language_literature', 'preschool_education', 'public_administration'] },
  { code: 15, key: 'faculty.online_education', majors: ['unselected', 'computer_science_technology', 'business_administration', 'accounting'] },
  { code: 16, key: 'faculty.marxism', majors: ['unselected', 'ideological_political_education', 'marxist_theory'] }
]

const MARKETPLACE_KEYS = [
  'marketplace.campus_transportation', 'marketplace.phone', 'marketplace.computer', 'marketplace.digital_accessories',
  'marketplace.digital_devices', 'marketplace.home_appliances', 'marketplace.sports_fitness',
  'marketplace.clothing_accessories', 'marketplace.books_textbooks', 'marketplace.rental',
  'marketplace.lifestyle_entertainment', 'marketplace.other'
]

const LOST_FOUND_KEYS = [
  'lostfound.phone', 'lostfound.campus_card', 'lostfound.id_card', 'lostfound.bank_card',
  'lostfound.book', 'lostfound.keys', 'lostfound.bag', 'lostfound.clothing',
  'lostfound.campus_transportation', 'lostfound.sports_fitness', 'lostfound.digital_accessories',
  'lostfound.other'
]

const LOST_FOUND_MODE_KEYS = ['lostfound.notice_seek', 'lostfound.notice_found']

const LOCALIZED_LABELS = {
  'zh-CN': {
    unselected: '未选择', 'faculty.education': '教育学院', 'faculty.politics_law': '政法系', 'faculty.chinese': '中文系',
    'faculty.mathematics': '数学系', 'faculty.foreign_languages': '外语系', 'faculty.physics_information': '物理与信息工程系',
    'faculty.chemistry': '化学系', 'faculty.biology_food': '生物与食品工程学院', 'faculty.physical_education': '体育学院',
    'faculty.fine_arts': '美术学院', 'faculty.computer_science': '计算机科学系', 'faculty.music': '音乐系',
    'faculty.teacher_training': '教师研修学院', 'faculty.continuing_education': '成人教育学院',
    'faculty.online_education': '网络教育学院', 'faculty.marxism': '马克思主义学院',
    education: '教育学', preschool_education: '学前教育', primary_education: '小学教育', special_education: '特殊教育',
    law: '法学', ideological_political_education: '思想政治教育', social_work: '社会工作', chinese_language_literature: '汉语言文学',
    history: '历史学', secretarial_studies: '秘书学', mathematics_applied_mathematics: '数学与应用数学',
    information_computing_science: '信息与计算科学', statistics: '统计学', english: '英语', business_english: '商务英语',
    japanese: '日语', translation: '翻译', physics: '物理学', electronic_information_engineering: '电子信息工程',
    communication_engineering: '通信工程', chemistry: '化学', applied_chemistry: '应用化学', materials_chemistry: '材料化学',
    biological_science: '生物科学', biotechnology: '生物技术', food_science_engineering: '食品科学与工程',
    physical_education: '体育教育', social_sports_guidance_management: '社会体育指导与管理', fine_arts: '美术学',
    visual_communication_design: '视觉传达设计', environmental_design: '环境设计', software_engineering: '软件工程',
    network_engineering: '网络工程', computer_science_technology: '计算机科学与技术', internet_of_things_engineering: '物联网工程',
    musicology: '音乐学', music_performance: '音乐表演', dance: '舞蹈学', educational_technology: '教育技术学',
    public_administration: '行政管理', business_administration: '工商管理', accounting: '会计学', marxist_theory: '马克思主义理论',
    'marketplace.campus_transportation': '校园代步', 'marketplace.phone': '手机', 'marketplace.computer': '电脑',
    'marketplace.digital_accessories': '数码配件', 'marketplace.digital_devices': '数码', 'marketplace.home_appliances': '电器',
    'marketplace.sports_fitness': '运动健身', 'marketplace.clothing_accessories': '衣物伞帽', 'marketplace.books_textbooks': '图书教材',
    'marketplace.rental': '租赁', 'marketplace.lifestyle_entertainment': '生活娱乐', 'marketplace.other': '其他',
    'lostfound.phone': '手机', 'lostfound.campus_card': '校园卡', 'lostfound.id_card': '身份证', 'lostfound.bank_card': '银行卡',
    'lostfound.book': '书', 'lostfound.keys': '钥匙', 'lostfound.bag': '包包', 'lostfound.clothing': '衣帽',
    'lostfound.campus_transportation': '校园代步', 'lostfound.sports_fitness': '运动健身', 'lostfound.digital_accessories': '数码配件',
    'lostfound.other': '其他', 'lostfound.notice_seek': '寻物启事', 'lostfound.notice_found': '失物招领'
  },
  'zh-HK': {
    unselected: '未選擇', 'faculty.education': '教育學院', 'faculty.politics_law': '政法系', 'faculty.chinese': '中文系',
    'faculty.mathematics': '數學系', 'faculty.foreign_languages': '外語系', 'faculty.physics_information': '物理與信息工程系',
    'faculty.chemistry': '化學系', 'faculty.biology_food': '生物與食品工程學院', 'faculty.physical_education': '體育學院',
    'faculty.fine_arts': '美術學院', 'faculty.computer_science': '計算機科學系', 'faculty.music': '音樂系',
    'faculty.teacher_training': '教師研修學院', 'faculty.continuing_education': '成人教育學院',
    'faculty.online_education': '網絡教育學院', 'faculty.marxism': '馬克思主義學院',
    education: '教育學', preschool_education: '學前教育', primary_education: '小學教育', special_education: '特殊教育',
    law: '法學', ideological_political_education: '思想政治教育', social_work: '社會工作', chinese_language_literature: '漢語言文學',
    history: '歷史學', secretarial_studies: '秘書學', mathematics_applied_mathematics: '數學與應用數學',
    information_computing_science: '信息與計算科學', statistics: '統計學', english: '英語', business_english: '商務英語',
    japanese: '日語', translation: '翻譯', physics: '物理學', electronic_information_engineering: '電子信息工程',
    communication_engineering: '通信工程', chemistry: '化學', applied_chemistry: '應用化學', materials_chemistry: '材料化學',
    biological_science: '生物科學', biotechnology: '生物技術', food_science_engineering: '食品科學與工程',
    physical_education: '體育教育', social_sports_guidance_management: '社會體育指導與管理', fine_arts: '美術學',
    visual_communication_design: '視覺傳達設計', environmental_design: '環境設計', software_engineering: '軟件工程',
    network_engineering: '網絡工程', computer_science_technology: '計算機科學與技術', internet_of_things_engineering: '物聯網工程',
    musicology: '音樂學', music_performance: '音樂表演', dance: '舞蹈學', educational_technology: '教育技術學',
    public_administration: '行政管理', business_administration: '工商管理', accounting: '會計學', marxist_theory: '馬克思主義理論',
    'marketplace.campus_transportation': '校園代步', 'marketplace.phone': '手機', 'marketplace.computer': '電腦',
    'marketplace.digital_accessories': '數碼配件', 'marketplace.digital_devices': '數碼', 'marketplace.home_appliances': '電器',
    'marketplace.sports_fitness': '運動健身', 'marketplace.clothing_accessories': '衣物傘帽', 'marketplace.books_textbooks': '圖書教材',
    'marketplace.rental': '租賃', 'marketplace.lifestyle_entertainment': '生活娛樂', 'marketplace.other': '其他',
    'lostfound.phone': '手機', 'lostfound.campus_card': '校園卡', 'lostfound.id_card': '身份證', 'lostfound.bank_card': '銀行卡',
    'lostfound.book': '書', 'lostfound.keys': '鎖匙', 'lostfound.bag': '包包', 'lostfound.clothing': '衣帽',
    'lostfound.campus_transportation': '校園代步', 'lostfound.sports_fitness': '運動健身', 'lostfound.digital_accessories': '數碼配件',
    'lostfound.other': '其他', 'lostfound.notice_seek': '尋物啟事', 'lostfound.notice_found': '失物招領'
  },
  'zh-TW': {
    unselected: '未選擇', 'faculty.education': '教育學院', 'faculty.politics_law': '政法系', 'faculty.chinese': '中文系',
    'faculty.mathematics': '數學系', 'faculty.foreign_languages': '外語系', 'faculty.physics_information': '物理與資訊工程系',
    'faculty.chemistry': '化學系', 'faculty.biology_food': '生物與食品工程學院', 'faculty.physical_education': '體育學院',
    'faculty.fine_arts': '美術學院', 'faculty.computer_science': '計算機科學系', 'faculty.music': '音樂系',
    'faculty.teacher_training': '教師研修學院', 'faculty.continuing_education': '成人教育學院',
    'faculty.online_education': '網路教育學院', 'faculty.marxism': '馬克思主義學院',
    education: '教育學', preschool_education: '學前教育', primary_education: '國小教育', special_education: '特殊教育',
    law: '法學', ideological_political_education: '思想政治教育', social_work: '社會工作', chinese_language_literature: '漢語言文學',
    history: '歷史學', secretarial_studies: '秘書學', mathematics_applied_mathematics: '數學與應用數學',
    information_computing_science: '資訊與計算科學', statistics: '統計學', english: '英語', business_english: '商務英語',
    japanese: '日語', translation: '翻譯', physics: '物理學', electronic_information_engineering: '電子資訊工程',
    communication_engineering: '通訊工程', chemistry: '化學', applied_chemistry: '應用化學', materials_chemistry: '材料化學',
    biological_science: '生物科學', biotechnology: '生物技術', food_science_engineering: '食品科學與工程',
    physical_education: '體育教育', social_sports_guidance_management: '社會體育指導與管理', fine_arts: '美術學',
    visual_communication_design: '視覺傳達設計', environmental_design: '環境設計', software_engineering: '軟體工程',
    network_engineering: '網路工程', computer_science_technology: '計算機科學與技術', internet_of_things_engineering: '物聯網工程',
    musicology: '音樂學', music_performance: '音樂表演', dance: '舞蹈學', educational_technology: '教育技術學',
    public_administration: '行政管理', business_administration: '工商管理', accounting: '會計學', marxist_theory: '馬克思主義理論',
    'marketplace.campus_transportation': '校園代步', 'marketplace.phone': '手機', 'marketplace.computer': '電腦',
    'marketplace.digital_accessories': '數位配件', 'marketplace.digital_devices': '數位', 'marketplace.home_appliances': '家電',
    'marketplace.sports_fitness': '運動健身', 'marketplace.clothing_accessories': '衣物傘帽', 'marketplace.books_textbooks': '圖書教材',
    'marketplace.rental': '租賃', 'marketplace.lifestyle_entertainment': '生活娛樂', 'marketplace.other': '其他',
    'lostfound.phone': '手機', 'lostfound.campus_card': '校園卡', 'lostfound.id_card': '身分證', 'lostfound.bank_card': '銀行卡',
    'lostfound.book': '書', 'lostfound.keys': '鑰匙', 'lostfound.bag': '包包', 'lostfound.clothing': '衣帽',
    'lostfound.campus_transportation': '校園代步', 'lostfound.sports_fitness': '運動健身', 'lostfound.digital_accessories': '數位配件',
    'lostfound.other': '其他', 'lostfound.notice_seek': '尋物啟事', 'lostfound.notice_found': '失物招領'
  },
  en: {
    unselected: 'Not selected', 'faculty.education': 'School of Education', 'faculty.politics_law': 'Department of Politics and Law',
    'faculty.chinese': 'Department of Chinese', 'faculty.mathematics': 'Department of Mathematics', 'faculty.foreign_languages': 'Department of Foreign Languages',
    'faculty.physics_information': 'Department of Physics and Information Engineering', 'faculty.chemistry': 'Department of Chemistry',
    'faculty.biology_food': 'School of Biology and Food Engineering', 'faculty.physical_education': 'School of Physical Education',
    'faculty.fine_arts': 'School of Fine Arts', 'faculty.computer_science': 'Department of Computer Science', 'faculty.music': 'Department of Music',
    'faculty.teacher_training': 'Teacher Training Institute', 'faculty.continuing_education': 'Continuing Education Institute',
    'faculty.online_education': 'Online Education Institute', 'faculty.marxism': 'School of Marxism',
    education: 'Education', preschool_education: 'Preschool Education', primary_education: 'Primary Education', special_education: 'Special Education',
    law: 'Law', ideological_political_education: 'Ideological and Political Education', social_work: 'Social Work',
    chinese_language_literature: 'Chinese Language and Literature', history: 'History', secretarial_studies: 'Secretarial Studies',
    mathematics_applied_mathematics: 'Mathematics and Applied Mathematics', information_computing_science: 'Information and Computing Science',
    statistics: 'Statistics', english: 'English', business_english: 'Business English', japanese: 'Japanese', translation: 'Translation',
    physics: 'Physics', electronic_information_engineering: 'Electronic Information Engineering', communication_engineering: 'Communication Engineering',
    chemistry: 'Chemistry', applied_chemistry: 'Applied Chemistry', materials_chemistry: 'Materials Chemistry', biological_science: 'Biological Science',
    biotechnology: 'Biotechnology', food_science_engineering: 'Food Science and Engineering', physical_education: 'Physical Education',
    social_sports_guidance_management: 'Social Sports Guidance and Management', fine_arts: 'Fine Arts',
    visual_communication_design: 'Visual Communication Design', environmental_design: 'Environmental Design', software_engineering: 'Software Engineering',
    network_engineering: 'Network Engineering', computer_science_technology: 'Computer Science and Technology',
    internet_of_things_engineering: 'Internet of Things Engineering', musicology: 'Musicology', music_performance: 'Music Performance',
    dance: 'Dance', educational_technology: 'Educational Technology', public_administration: 'Public Administration',
    business_administration: 'Business Administration', accounting: 'Accounting', marxist_theory: 'Marxist Theory',
    'marketplace.campus_transportation': 'Campus Transportation', 'marketplace.phone': 'Phone', 'marketplace.computer': 'Computer',
    'marketplace.digital_accessories': 'Digital Accessories', 'marketplace.digital_devices': 'Digital Devices', 'marketplace.home_appliances': 'Home Appliances',
    'marketplace.sports_fitness': 'Sports and Fitness', 'marketplace.clothing_accessories': 'Clothing and Accessories', 'marketplace.books_textbooks': 'Books and Textbooks',
    'marketplace.rental': 'Rental', 'marketplace.lifestyle_entertainment': 'Lifestyle and Entertainment', 'marketplace.other': 'Other',
    'lostfound.phone': 'Phone', 'lostfound.campus_card': 'Campus Card', 'lostfound.id_card': 'ID Card', 'lostfound.bank_card': 'Bank Card',
    'lostfound.book': 'Book', 'lostfound.keys': 'Keys', 'lostfound.bag': 'Bag', 'lostfound.clothing': 'Clothing',
    'lostfound.campus_transportation': 'Campus Transportation', 'lostfound.sports_fitness': 'Sports and Fitness', 'lostfound.digital_accessories': 'Digital Accessories',
    'lostfound.other': 'Other', 'lostfound.notice_seek': 'Lost Item Notice', 'lostfound.notice_found': 'Found Item Notice'
  },
  ja: {
    unselected: '未選択', 'faculty.education': '教育学院', 'faculty.politics_law': '政治法律学科', 'faculty.chinese': '中国語学科',
    'faculty.mathematics': '数学科', 'faculty.foreign_languages': '外国語学科', 'faculty.physics_information': '物理情報工学科',
    'faculty.chemistry': '化学科', 'faculty.biology_food': '生物食品工学学院', 'faculty.physical_education': '体育学院',
    'faculty.fine_arts': '美術学院', 'faculty.computer_science': '計算機科学科', 'faculty.music': '音楽学科',
    'faculty.teacher_training': '教員研修学院', 'faculty.continuing_education': '成人教育学院', 'faculty.online_education': 'オンライン教育学院',
    'faculty.marxism': 'マルクス主義学院', education: '教育学', preschool_education: '幼児教育', primary_education: '初等教育',
    special_education: '特別支援教育', law: '法学', ideological_political_education: '思想政治教育', social_work: '社会福祉',
    chinese_language_literature: '中国語中国文学', history: '歴史学', secretarial_studies: '秘書学', mathematics_applied_mathematics: '数学・応用数学',
    information_computing_science: '情報計算科学', statistics: '統計学', english: '英語', business_english: 'ビジネス英語',
    japanese: '日本語', translation: '翻訳', physics: '物理学', electronic_information_engineering: '電子情報工学',
    communication_engineering: '通信工学', chemistry: '化学', applied_chemistry: '応用化学', materials_chemistry: '材料化学',
    biological_science: '生物科学', biotechnology: 'バイオテクノロジー', food_science_engineering: '食品科学工学',
    physical_education: '体育教育', social_sports_guidance_management: '社会体育指導管理', fine_arts: '美術学',
    visual_communication_design: 'ビジュアルコミュニケーションデザイン', environmental_design: '環境デザイン', software_engineering: 'ソフトウェア工学',
    network_engineering: 'ネットワーク工学', computer_science_technology: '計算機科学技術', internet_of_things_engineering: 'IoT工学',
    musicology: '音楽学', music_performance: '音楽パフォーマンス', dance: '舞踊学', educational_technology: '教育技術学',
    public_administration: '行政管理', business_administration: '経営学', accounting: '会計学', marxist_theory: 'マルクス主義理論',
    'marketplace.campus_transportation': 'キャンパス移動', 'marketplace.phone': '携帯電話', 'marketplace.computer': 'パソコン',
    'marketplace.digital_accessories': 'デジタル周辺機器', 'marketplace.digital_devices': 'デジタル機器', 'marketplace.home_appliances': '家電',
    'marketplace.sports_fitness': 'スポーツ・フィットネス', 'marketplace.clothing_accessories': '衣類・小物', 'marketplace.books_textbooks': '書籍・教科書',
    'marketplace.rental': '賃貸', 'marketplace.lifestyle_entertainment': '生活・娯楽', 'marketplace.other': 'その他',
    'lostfound.phone': '携帯電話', 'lostfound.campus_card': '学生証', 'lostfound.id_card': '身分証', 'lostfound.bank_card': '銀行カード',
    'lostfound.book': '本', 'lostfound.keys': '鍵', 'lostfound.bag': 'かばん', 'lostfound.clothing': '衣類',
    'lostfound.campus_transportation': 'キャンパス移動', 'lostfound.sports_fitness': 'スポーツ・フィットネス',
    'lostfound.digital_accessories': 'デジタル周辺機器', 'lostfound.other': 'その他', 'lostfound.notice_seek': '落とし物捜索', 'lostfound.notice_found': '拾得物案内'
  },
  ko: {
    unselected: '선택 안 함', 'faculty.education': '교육대학', 'faculty.politics_law': '정치법학과', 'faculty.chinese': '중문과',
    'faculty.mathematics': '수학과', 'faculty.foreign_languages': '외국어과', 'faculty.physics_information': '물리정보공학과',
    'faculty.chemistry': '화학과', 'faculty.biology_food': '생물식품공학대학', 'faculty.physical_education': '체육대학',
    'faculty.fine_arts': '미술대학', 'faculty.computer_science': '컴퓨터과학과', 'faculty.music': '음악과',
    'faculty.teacher_training': '교사연수대학', 'faculty.continuing_education': '평생교육대학', 'faculty.online_education': '원격교육대학',
    'faculty.marxism': '마르크스주의대학', education: '교육학', preschool_education: '유아교육', primary_education: '초등교육',
    special_education: '특수교육', law: '법학', ideological_political_education: '사상정치교육', social_work: '사회복지',
    chinese_language_literature: '중국어문학', history: '역사학', secretarial_studies: '비서학', mathematics_applied_mathematics: '수학 및 응용수학',
    information_computing_science: '정보계산과학', statistics: '통계학', english: '영어', business_english: '비즈니스 영어',
    japanese: '일본어', translation: '번역', physics: '물리학', electronic_information_engineering: '전자정보공학',
    communication_engineering: '통신공학', chemistry: '화학', applied_chemistry: '응용화학', materials_chemistry: '재료화학',
    biological_science: '생물과학', biotechnology: '생명공학', food_science_engineering: '식품과학공학', physical_education: '체육교육',
    social_sports_guidance_management: '사회체육지도관리', fine_arts: '미술학', visual_communication_design: '시각디자인',
    environmental_design: '환경디자인', software_engineering: '소프트웨어공학', network_engineering: '네트워크공학',
    computer_science_technology: '컴퓨터과학기술', internet_of_things_engineering: '사물인터넷공학', musicology: '음악학',
    music_performance: '음악공연', dance: '무용학', educational_technology: '교육기술학', public_administration: '행정관리',
    business_administration: '경영학', accounting: '회계학', marxist_theory: '마르크스주의 이론',
    'marketplace.campus_transportation': '캠퍼스 이동수단', 'marketplace.phone': '휴대폰', 'marketplace.computer': '컴퓨터',
    'marketplace.digital_accessories': '디지털 액세서리', 'marketplace.digital_devices': '디지털 기기', 'marketplace.home_appliances': '가전제품',
    'marketplace.sports_fitness': '운동·피트니스', 'marketplace.clothing_accessories': '의류·잡화', 'marketplace.books_textbooks': '도서·교재',
    'marketplace.rental': '임대', 'marketplace.lifestyle_entertainment': '생활·엔터테인먼트', 'marketplace.other': '기타',
    'lostfound.phone': '휴대폰', 'lostfound.campus_card': '학생증', 'lostfound.id_card': '신분증', 'lostfound.bank_card': '은행카드',
    'lostfound.book': '책', 'lostfound.keys': '열쇠', 'lostfound.bag': '가방', 'lostfound.clothing': '의류',
    'lostfound.campus_transportation': '캠퍼스 이동수단', 'lostfound.sports_fitness': '운동·피트니스',
    'lostfound.digital_accessories': '디지털 액세서리', 'lostfound.other': '기타', 'lostfound.notice_seek': '분실물 찾기', 'lostfound.notice_found': '습득물 안내'
  }
}

function getLabels(locale) {
  const currentLocale = i18n.global?.locale?.value || 'zh-CN'
  const normalizedLocale = normalizeCatalogLocale(locale || currentLocale)
  return LOCALIZED_LABELS[normalizedLocale] || LOCALIZED_LABELS['zh-CN']
}

export function normalizeCatalogLocale(locale) {
  var value = String(locale || '').trim().replace(/_/g, '-').toLowerCase()
  if (!value) return 'zh-CN'
  if (value === 'zh-cn' || value === 'zh-hans' || value === 'zh-hans-cn' || value === 'zh') return 'zh-CN'
  if (value === 'zh-hk' || value === 'zh-hant-hk') return 'zh-HK'
  if (value === 'zh-tw' || value === 'zh-hant' || value === 'zh-hant-tw') return 'zh-TW'
  if (value.indexOf('zh-hk') === 0) return 'zh-HK'
  if (value.indexOf('zh-tw') === 0 || value.indexOf('zh-hant') === 0) return 'zh-TW'
  if (value.indexOf('zh') === 0) return 'zh-CN'
  if (value.indexOf('en') === 0) return 'en'
  if (value.indexOf('ja') === 0) return 'ja'
  if (value.indexOf('ko') === 0) return 'ko'
  return 'zh-CN'
}

function buildDefaultProfileOptionsPayload(locale) {
  const labels = getLabels(locale)
  return {
    faculties: FACULTY_DEFINITIONS.map(function(definition) {
      return {
        code: definition.code,
        label: labels[definition.key],
        majors: definition.majors.map(function(code) {
          return { code: code, label: labels[code] }
        })
      }
    }),
    marketplaceItemTypes: MARKETPLACE_KEYS.map(function(key, index) {
      return { code: index, label: labels[key] }
    }),
    lostFoundItemTypes: LOST_FOUND_KEYS.map(function(key, index) {
      return { code: index, label: labels[key] }
    }),
    lostFoundModes: LOST_FOUND_MODE_KEYS.map(function(key, index) {
      return { code: index, label: labels[key] }
    })
  }
}

export function getProfileCatalog(locale) {
  const labels = getLabels(locale)
  const facultyMap = new Map(FACULTY_DEFINITIONS.map((item) => [item.code, item]))

  const dictionaryKeysByType = {
    marketplaceItemTypes: MARKETPLACE_KEYS,
    lostFoundItemTypes: LOST_FOUND_KEYS,
    lostFoundModes: LOST_FOUND_MODE_KEYS,
  }

  return {
    locale: normalizeCatalogLocale(locale || i18n.global?.locale?.value),
    unselectedLabel: labels.unselected,
    otherLabel: labels['marketplace.other'],
    facultyLabel(code) {
      const definition = facultyMap.get(Number(code))
      return definition ? labels[definition.key] || '' : ''
    },
    majorLabel(facultyCode, majorCode) {
      const definition = facultyMap.get(Number(facultyCode))
      if (!definition) return ''
      return definition.majors.includes(majorCode) ? (labels[majorCode] || '') : ''
    },
    dictionaryLabel(type, code) {
      const keys = dictionaryKeysByType[type] || []
      const key = keys[Number(code)]
      return key ? labels[key] || '' : ''
    },
  }
}

export function formatProfileOptions(payload, locale) {
  const catalog = getProfileCatalog(locale)
  const safePayload = payload || {}
  return {
    faculties: (Array.isArray(safePayload.faculties) ? safePayload.faculties : []).map((faculty) => ({
      code: faculty.code,
      label: catalog.facultyLabel(faculty.code),
      majors: (Array.isArray(faculty.majors) ? faculty.majors : []).map((majorCode) => ({
        code: majorCode,
        label: catalog.majorLabel(faculty.code, majorCode),
      })),
    })),
    marketplaceItemTypes: (Array.isArray(safePayload.marketplaceItemTypes) ? safePayload.marketplaceItemTypes : []).map((code) => ({
      code,
      label: catalog.dictionaryLabel('marketplaceItemTypes', code),
    })),
    lostFoundItemTypes: (Array.isArray(safePayload.lostFoundItemTypes) ? safePayload.lostFoundItemTypes : []).map((code) => ({
      code,
      label: catalog.dictionaryLabel('lostFoundItemTypes', code),
    })),
    lostFoundModes: (Array.isArray(safePayload.lostFoundModes) ? safePayload.lostFoundModes : []).map((code) => ({
      code,
      label: catalog.dictionaryLabel('lostFoundModes', code),
    })),
  }
}

export {
  buildDefaultProfileOptionsPayload,
  getLabels,
}
