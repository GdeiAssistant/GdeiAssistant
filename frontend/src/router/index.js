import { createRouter, createWebHistory } from 'vue-router'

const layoutModules = import.meta.glob('../layout/**/*.vue')
const viewModules = import.meta.glob('../views/**/*.vue')

function lazyModule(modules, modulePath) {
  const loader = modules[modulePath]
  if (!loader) {
    throw new Error(`Failed to resolve route component: ${modulePath}`)
  }
  return loader
}

function lazyLayout(relativePath) {
  return lazyModule(layoutModules, `../layout/${relativePath}.vue`)
}

function lazyView(relativePath) {
  return lazyModule(viewModules, `../views/${relativePath}.vue`)
}

const MainLayout = lazyLayout('MainLayout')
const Login = lazyView('Login')
const About = lazyView('About')
const Home = lazyView('Home')
const Schedule = lazyView('schedule/Schedule')
const Profile = lazyView('Profile')
const Grade = lazyView('grade/Grade')
const Cet = lazyView('cet/Cet')
const CetSave = lazyView('cet/CetSave')
const Collection = lazyView('collection/Collection')
const CollectionSearch = lazyView('collection/CollectionSearch')
const CollectionList = lazyView('collection/CollectionList')
const CollectionDetail = lazyView('collection/CollectionDetail')
const Book = lazyView('book/Book')
const CardSearch = lazyView('card/CardSearch')
const CardRecordSearch = lazyView('card/CardRecordSearch')
const CardList = lazyView('card/CardList')
const CardInfo = lazyView('card/CardInfo')
const Evaluate = lazyView('evaluate/Evaluate')
const Spare = lazyView('spare/Spare')
const KaoyanSearch = lazyView('graduateExam/GraduateExamSearch')
const KaoyanResult = lazyView('graduateExam/GraduateExamResult')
const PeIndex = lazyView('pe/PeIndex')
const NewsList = lazyView('news/NewsList')
const NewsDetail = lazyView('news/NewsDetail')
const Info = lazyView('Info')
const UserPrivacySetting = lazyView('user/PrivacySetting')
const UserFunctions = lazyView('user/Functions')
const UserFeatureManage = lazyView('user/FeatureManage')
const UserPassword = lazyView('user/Password')
const UserLoginRecord = lazyView('user/LoginRecord')
const UserRealname = lazyView('user/RealName')
const UserBindPhone = lazyView('user/BindPhone')
const UserBindEmail = lazyView('user/BindEmail')
const UserDelete = lazyView('user/DeleteAccount')
const UserDownload = lazyView('user/Download')
const UserFeedback = lazyView('user/Feedback')
const DataIndex = lazyView('data/DataIndex')
const ElectricityFees = lazyView('data/ElectricityFees')
const YellowPage = lazyView('data/YellowPage')
const Security = lazyView('about/Security')
const Account = lazyView('about/Account')
const UserAgreement = lazyView('about/UserAgreement')
const PrivacyPolicy = lazyView('about/PrivacyPolicy')
const CookiePolicy = lazyView('about/CookiePolicy')
const SocialPolicy = lazyView('about/SocialPolicy')
const License = lazyView('about/License')
const IntellectualProperty = lazyView('about/IntellectualProperty')
const ErshouIndex = lazyView('marketplace/Index')
const ErshouHome = lazyView('marketplace/Home')
const ErshouPublish = lazyView('marketplace/Publish')
const ErshouProfile = lazyView('marketplace/Profile')
const ErshouDetail = lazyView('marketplace/Detail')
const ErshouType = lazyView('marketplace/Type')
const ErshouSearch = lazyView('marketplace/Search')
const LostAndFoundIndex = lazyView('lostandfound/Index')
const LostAndFoundHome = lazyView('lostandfound/Home')
const LostAndFoundPublish = lazyView('lostandfound/Publish')
const LostAndFoundProfile = lazyView('lostandfound/Profile')
const LostAndFoundDetail = lazyView('lostandfound/Detail')
const SecretHome = lazyView('secret/Home')
const SecretPublish = lazyView('secret/Publish')
const SecretDetail = lazyView('secret/Detail')
const SecretProfile = lazyView('secret/Profile')
const ExpressIndex = lazyView('express/Index')
const ExpressHome = lazyView('express/Home')
const ExpressPublish = lazyView('express/Publish')
const ExpressSearch = lazyView('express/Search')
const ExpressDetail = lazyView('express/Detail')
const DatingHome = lazyView('dating/Home')
const DatingPublish = lazyView('dating/Publish')
const DatingDetail = lazyView('dating/Detail')
const DatingCenter = lazyView('dating/Center')
const TopicIndex = lazyView('topic/Index')
const TopicHome = lazyView('topic/Home')
const TopicPublish = lazyView('topic/Publish')
const TopicSearch = lazyView('topic/Search')
const TopicDetail = lazyView('topic/Detail')
const PhotographHome = lazyView('photograph/Home')
const PhotographDetail = lazyView('photograph/Detail')
const PhotographPublish = lazyView('photograph/Publish')
const DeliveryIndex = lazyView('delivery/Index')
const DeliveryHome = lazyView('delivery/Home')
const DeliveryPublish = lazyView('delivery/Publish')
const DeliveryDetail = lazyView('delivery/Detail')
const DeliveryMine = lazyView('delivery/Mine')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  {
    path: '/grade',
    name: 'Grade',
    component: Grade
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: Schedule
  },
  {
    path: '/cet',
    name: 'Cet',
    component: Cet
  },
  {
    path: '/cet/save',
    name: 'CetSave',
    component: CetSave
  },
  // 旧路由兼容重定向：/collection/* → /library/*, /book → /library/borrow
  { path: '/collection', redirect: '/library' },
  { path: '/collection/search', redirect: '/library/search' },
  { path: '/collection/list', redirect: '/library/list' },
  { path: '/collection/detail', redirect: '/library/detail' },
  { path: '/book', redirect: '/library/borrow' },
  {
    path: '/library',
    name: 'Collection',
    component: Collection
  },
  {
    path: '/library/search',
    name: 'CollectionSearch',
    component: CollectionSearch
  },
  {
    path: '/library/list',
    name: 'CollectionList',
    component: CollectionList
  },
  {
    path: '/library/detail',
    name: 'CollectionDetail',
    component: CollectionDetail
  },
  {
    path: '/library/borrow',
    name: 'Book',
    component: Book
  },
  {
    path: '/card',
    name: 'CardSearch',
    component: CardSearch
  },
  {
    path: '/card/records',
    name: 'CardRecordSearch',
    component: CardRecordSearch
  },
  {
    path: '/card/list',
    name: 'CardList',
    component: CardList
  },
  {
    path: '/card/info',
    name: 'CardInfo',
    component: CardInfo
  },
  {
    path: '/evaluate',
    name: 'Evaluate',
    component: Evaluate
  },
  {
    path: '/spare',
    name: 'Spare',
    component: Spare
  },
  {
    path: '/spare/list',
    redirect: '/spare'
  },
  {
    path: '/kaoyan',
    name: 'KaoyanSearch',
    component: KaoyanSearch
  },
  {
    path: '/kaoyan/result',
    name: 'KaoyanResult',
    component: KaoyanResult
  },
  {
    path: '/pe',
    name: 'PeIndex',
    component: PeIndex
  },
  {
    path: '/news',
    name: 'NewsList',
    component: NewsList
  },
  {
    path: '/news/id/:id',
    name: 'NewsDetail',
    component: NewsDetail
  },
  // 用户相关页面
  {
    path: '/user/privacy-setting',
    name: 'UserPrivacySetting',
    component: UserPrivacySetting
  },
  {
    path: '/user/functions',
    name: 'UserFunctions',
    component: UserFunctions
  },
  {
    path: '/user/feature-manage',
    name: 'UserFeatureManage',
    component: UserFeatureManage
  },
  {
    path: '/user/password',
    name: 'UserPassword',
    component: UserPassword
  },
  {
    path: '/user/login-record',
    name: 'UserLoginRecord',
    component: UserLoginRecord
  },
  {
    path: '/user/realname',
    name: 'UserRealname',
    component: UserRealname
  },
  {
    path: '/user/bind-phone',
    name: 'UserBindPhone',
    component: UserBindPhone
  },
  {
    path: '/user/bind-email',
    name: 'UserBindEmail',
    component: UserBindEmail
  },
  {
    path: '/user/delete',
    name: 'UserDelete',
    component: UserDelete
  },
  {
    path: '/user/feedback',
    name: 'UserFeedback',
    component: UserFeedback
  },
  {
    path: '/user/download',
    name: 'UserDownload',
    component: UserDownload
  },
  {
    path: '/user/avatar-edit',
    name: 'AvatarEdit',
    component: () => import('../views/user/AvatarEdit.vue'),
    meta: { title: '头像管理' }
  },
  // 数据查询模块（对应后端 Data 模块）
  {
    path: '/data',
    name: 'DataIndex',
    component: DataIndex
  },
  {
    path: '/data/electricity',
    name: 'ElectricityFees',
    component: ElectricityFees
  },
  {
    path: '/data/yellowpage',
    name: 'YellowPage',
    component: YellowPage
  },
  // About 相关静态页面
  {
    path: '/about/security',
    name: 'Security',
    component: Security
  },
  {
    path: '/about/account',
    name: 'Account',
    component: Account
  },
  {
    path: '/agreement',
    name: 'UserAgreement',
    component: UserAgreement
  },
  {
    path: '/policy/privacy',
    name: 'PrivacyPolicy',
    component: PrivacyPolicy
  },
  {
    path: '/policy/cookie',
    name: 'CookiePolicy',
    component: CookiePolicy
  },
  {
    path: '/policy/social',
    name: 'SocialPolicy',
    component: SocialPolicy
  },
  {
    path: '/license',
    name: 'License',
    component: License
  },
  {
    path: '/policy/intellectualproperty',
    name: 'IntellectualProperty',
    component: IntellectualProperty
  },
  // 二手模块：详情、分类、搜索使用独立路由
  {
    path: '/marketplace/detail/:id',
    name: 'ErshouDetail',
    component: ErshouDetail
  },
  {
    path: '/marketplace/type',
    name: 'ErshouType',
    component: ErshouType
  },
  {
    path: '/marketplace/search',
    name: 'ErshouSearch',
    component: ErshouSearch
  },
  {
    path: '/ershou',
    component: ErshouIndex,
    redirect: '/marketplace/home',
    children: [
      { path: 'home', name: 'ErshouHome', component: ErshouHome },
      { path: 'publish', name: 'ErshouPublish', component: ErshouPublish },
      { path: 'profile', name: 'ErshouProfile', component: ErshouProfile }
    ]
  },
  // 失物招领模块：详情使用独立路由
  {
    path: '/lostandfound/detail/:id',
    name: 'LostAndFoundDetail',
    component: LostAndFoundDetail
  },
  {
    path: '/lostandfound',
    component: LostAndFoundIndex,
    redirect: '/lostandfound/home',
    children: [
      { path: 'home', name: 'LostAndFoundHome', component: LostAndFoundHome },
      { path: 'publish', name: 'LostAndFoundPublish', component: LostAndFoundPublish },
      { path: 'profile', name: 'LostAndFoundProfile', component: LostAndFoundProfile }
    ]
  },
  // 校园表白墙：详情使用独立路由
  {
    path: '/express/detail/:id',
    name: 'ExpressDetail',
    component: ExpressDetail
  },
  // 校园表白墙主路由，使用黑色 Tabbar 主题
  {
    path: '/express',
    component: ExpressIndex,
    redirect: '/express/home',
    children: [
      { path: 'home', name: 'ExpressHome', component: ExpressHome },
      { path: 'publish', name: 'ExpressPublish', component: ExpressPublish },
      { path: 'search', name: 'ExpressSearch', component: ExpressSearch }
    ]
  },
  // 卖室友/交友模块
  {
    path: '/dating',
    redirect: '/dating/home'
  },
  {
    path: '/dating/home',
    name: 'DatingHome',
    component: DatingHome
  },
  {
    path: '/dating/publish',
    name: 'DatingPublish',
    component: DatingPublish
  },
  {
    path: '/dating/detail/:id',
    name: 'DatingDetail',
    component: DatingDetail
  },
  {
    path: '/dating/center',
    name: 'DatingCenter',
    component: DatingCenter
  },
  // 校园话题主路由，使用绿色 Tabbar 主题
  {
    path: '/topic/detail/:id',
    name: 'TopicDetail',
    component: TopicDetail
  },
  {
    path: '/topic',
    component: TopicIndex,
    redirect: '/topic/home',
    children: [
      { path: 'home', name: 'TopicHome', component: TopicHome },
      { path: 'publish', name: 'TopicPublish', component: TopicPublish },
      { path: 'search', name: 'TopicSearch', component: TopicSearch }
    ]
  },
  // 全民快递 / 校园跑腿主路由
  {
    path: '/delivery',
    component: DeliveryIndex,
    redirect: '/delivery/home',
    children: [
      { path: 'home', name: 'DeliveryHome', component: DeliveryHome },
      { path: 'publish', name: 'DeliveryPublish', component: DeliveryPublish },
      { path: 'mine', name: 'DeliveryMine', component: DeliveryMine }
    ]
  },
  {
    path: '/delivery/detail/:id',
    name: 'DeliveryDetail',
    component: DeliveryDetail
  },
  // 拍好校园模块
  {
    path: '/photograph',
    redirect: '/photograph/home'
  },
  {
    path: '/photograph/home',
    name: 'PhotographHome',
    component: PhotographHome
  },
  {
    path: '/photograph/detail/:id',
    name: 'PhotographDetail',
    component: PhotographDetail
  },
  {
    path: '/photograph/publish',
    name: 'PhotographPublish',
    component: PhotographPublish
  },
  // 校园树洞：所有页面均为独立路由
  {
    path: '/secret',
    redirect: '/secret/home'
  },
  {
    path: '/secret/home',
    name: 'SecretHome',
    component: SecretHome
  },
  {
    path: '/secret/publish',
    name: 'SecretPublish',
    component: SecretPublish
  },
  {
    path: '/secret/profile',
    name: 'SecretProfile',
    component: SecretProfile
  },
  {
    path: '/secret/detail/:id',
    name: 'SecretDetail',
    component: SecretDetail
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: Home
      },
      {
        path: 'info',
        name: 'InfoList',
        component: Info
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 与后端 SettingConstantUtils.LOGIN_INTERCEPTOR_EXCEPTION_LIST 同步：无需登录即可访问的路径前缀
export const AUTH_WHITELIST = ['/login', '/logout', '/about', '/agreement', '/policy', '/license', '/announcement', '/download', '/covid19']

export function hasToken(storage = globalThis?.localStorage) {
  return !!storage?.getItem?.('token')
}

export function isWhitelisted(path) {
  if (path === '/') return true
  return AUTH_WHITELIST.some(prefix => path === prefix || path.startsWith(prefix + '/'))
}

export function resolveNavigationTarget(path, loggedIn) {
  if (path === '/') {
    return loggedIn ? '/home' : '/login'
  }
  if (path === '/login') {
    return loggedIn ? '/home' : null
  }
  if (isWhitelisted(path)) {
    return null
  }
  return loggedIn ? null : '/login'
}

router.beforeEach((to, from, next) => {
  const redirectTarget = resolveNavigationTarget(to.path, hasToken())
  if (redirectTarget == null) {
    next()
    return
  }
  next(redirectTarget)
})

// 固定浏览器标题，禁止根据路由动态修改
export const FIXED_TITLE = '广东第二师范学院校园助手系统'
router.afterEach(() => {
  if (typeof document !== 'undefined') {
    document.title = FIXED_TITLE
  }
})

export default router
