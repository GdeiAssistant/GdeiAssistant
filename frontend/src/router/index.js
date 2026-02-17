import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import Login from '../views/Login.vue'
import About from '../views/About.vue'
import Home from '../views/Home.vue'
import Schedule from '../views/schedule/Schedule.vue'
import Discover from '../views/discover/Discover.vue'
import Profile from '../views/Profile.vue'
import Grade from '../views/grade/Grade.vue'
import Cet from '../views/cet/Cet.vue'
import CetSave from '../views/cet/CetSave.vue'
import CollectionSearch from '../views/collection/CollectionSearch.vue'
import CollectionList from '../views/collection/CollectionList.vue'
import CollectionDetail from '../views/collection/CollectionDetail.vue'
import Book from '../views/book/Book.vue'
import CardSearch from '../views/card/CardSearch.vue'
import CardList from '../views/card/CardList.vue'
import CardInfo from '../views/card/CardInfo.vue'
import Evaluate from '../views/evaluate/Evaluate.vue'
import SpareSearch from '../views/spare/SpareSearch.vue'
import SpareList from '../views/spare/SpareList.vue'
import KaoyanSearch from '../views/kaoyan/KaoyanSearch.vue'
import KaoyanResult from '../views/kaoyan/KaoyanResult.vue'
import PeIndex from '../views/pe/PeIndex.vue'
import NewsList from '../views/news/NewsList.vue'
import Info from '../views/Info.vue'
import WechatAccountList from '../views/wechat/WechatAccountList.vue'
import UserPrivacy from '../views/user/Privacy.vue'
import UserPrivacySetting from '../views/user/PrivacySetting.vue'
import UserFunctions from '../views/user/Functions.vue'
import UserFeatureManage from '../views/user/FeatureManage.vue'
import UserPassword from '../views/user/Password.vue'
import UserLoginHistory from '../views/user/LoginHistory.vue'
import UserLoginRecord from '../views/user/LoginRecord.vue'
import UserRealname from '../views/user/RealName.vue'
import UserPhone from '../views/user/Phone.vue'
import UserBindPhone from '../views/user/BindPhone.vue'
import UserBindEmail from '../views/user/BindEmail.vue'
import UserDelete from '../views/user/DeleteAccount.vue'
import UserDownload from '../views/user/Download.vue'
import UserFeedback from '../views/user/Feedback.vue'
import UserAvatar from '../views/user/Avatar.vue'
import DataIndex from '../views/data/DataIndex.vue'
import ElectricityFees from '../views/data/ElectricityFees.vue'
import YellowPage from '../views/data/YellowPage.vue'
// About 相关静态页面
import Security from '../views/about/Security.vue'
import Account from '../views/about/Account.vue'
import UserAgreement from '../views/about/UserAgreement.vue'
import PrivacyPolicy from '../views/about/PrivacyPolicy.vue'
import CookiePolicy from '../views/about/CookiePolicy.vue'
import SocialPolicy from '../views/about/SocialPolicy.vue'
import License from '../views/about/License.vue'
import IntellectualProperty from '../views/about/IntellectualProperty.vue'
// 二手交易模块（嵌套路由 + Tabbar）
import ErshouIndex from '../views/ershou/Index.vue'
import ErshouHome from '../views/ershou/Home.vue'
import ErshouPublish from '../views/ershou/Publish.vue'
import ErshouProfile from '../views/ershou/Profile.vue'
// 独立全屏页（不显示底部 Tabbar）
import ErshouDetail from '../views/ershou/Detail.vue'
import ErshouType from '../views/ershou/Type.vue'
import ErshouSearch from '../views/ershou/Search.vue'
// 失物招领模块（嵌套路由 + Tabbar）
import LostAndFoundIndex from '../views/lostandfound/Index.vue'
import LostAndFoundHome from '../views/lostandfound/Home.vue'
import LostAndFoundPublish from '../views/lostandfound/Publish.vue'
import LostAndFoundProfile from '../views/lostandfound/Profile.vue'
// 独立全屏页（不显示底部 Tabbar）
import LostAndFoundDetail from '../views/lostandfound/Detail.vue'
// 校园树洞模块
import SecretHome from '../views/secret/Home.vue'
import SecretPublish from '../views/secret/Publish.vue'
import SecretDetail from '../views/secret/Detail.vue'
import SecretProfile from '../views/secret/Profile.vue'
// 校园表白墙模块（express：嵌套路由 + 黑底 Tabbar）
import ExpressIndex from '../views/express/Index.vue'
import ExpressHome from '../views/express/Home.vue'
import ExpressPublish from '../views/express/Publish.vue'
import ExpressSearch from '../views/express/Search.vue'
import ExpressDetail from '../views/express/Detail.vue'
// 卖室友/交友模块（对应后端 Dating）
import DatingHome from '../views/dating/Home.vue'
import DatingPublish from '../views/dating/Publish.vue'
import DatingDetail from '../views/dating/Detail.vue'
import DatingCenter from '../views/dating/Center.vue'
// 校园话题模块（现代化社交卡片风格）
import TopicIndex from '../views/topic/Index.vue'
import TopicHome from '../views/topic/Home.vue'
import TopicPublish from '../views/topic/Publish.vue'
import TopicSearch from '../views/topic/Search.vue'
// 拍好校园模块
import PhotographHome from '../views/photograph/Home.vue'
import PhotographDetail from '../views/photograph/Detail.vue'
import PhotographPublish from '../views/photograph/Publish.vue'
// 全民快递/校园跑腿模块（现代化O2O任务卡片风格）
import DeliveryIndex from '../views/delivery/Index.vue'
import DeliveryHome from '../views/delivery/Home.vue'
import DeliveryPublish from '../views/delivery/Publish.vue'
import DeliveryDetail from '../views/delivery/Detail.vue'
import DeliveryMine from '../views/delivery/Mine.vue'

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
    path: '/cet',
    name: 'Cet',
    component: Cet
  },
  {
    path: '/cet/save',
    name: 'CetSave',
    component: CetSave
  },
  {
    path: '/collection',
    name: 'CollectionSearch',
    component: CollectionSearch
  },
  {
    path: '/collection/list',
    name: 'CollectionList',
    component: CollectionList
  },
  {
    path: '/collection/detail/:id',
    name: 'CollectionDetail',
    component: CollectionDetail
  },
  {
    path: '/book',
    name: 'Book',
    component: Book
  },
  {
    path: '/card',
    name: 'CardSearch',
    component: CardSearch
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
    name: 'SpareSearch',
    component: SpareSearch
  },
  {
    path: '/spare/list',
    name: 'SpareList',
    component: SpareList
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
  // 校园公众号列表（后端 /wechataccount）
  {
    path: '/wechataccount',
    name: 'WechatAccountList',
    component: WechatAccountList
  },
  // 用户相关页面
  {
    path: '/user/privacy',
    name: 'UserPrivacy',
    component: UserPrivacy
  },
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
    path: '/user/login-history',
    name: 'UserLoginHistory',
    component: UserLoginHistory
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
    path: '/user/avatar',
    name: 'UserAvatar',
    component: UserAvatar
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
  // About 相关静态页面路由
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
  // 二手：详情/分类/搜索为独立路由，进入时不显示底部 Tabbar
  {
    path: '/ershou/detail/:id',
    name: 'ErshouDetail',
    component: ErshouDetail
  },
  {
    path: '/ershou/type',
    name: 'ErshouType',
    component: ErshouType
  },
  {
    path: '/ershou/search',
    name: 'ErshouSearch',
    component: ErshouSearch
  },
  {
    path: '/ershou',
    component: ErshouIndex,
    redirect: '/ershou/home',
    children: [
      { path: 'home', name: 'ErshouHome', component: ErshouHome },
      { path: 'publish', name: 'ErshouPublish', component: ErshouPublish },
      { path: 'profile', name: 'ErshouProfile', component: ErshouProfile }
    ]
  },
  // 失物招领：详情为独立路由，进入时不显示底部 Tabbar
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
  // 校园表白墙：详情页为独立路由（不显示底部 Tabbar）
  {
    path: '/express/detail/:id',
    name: 'ExpressDetail',
    component: ExpressDetail
  },
  // 校园表白墙：专属黑底 Tabbar
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
  // 校园话题模块：专属亮绿色 Tabbar
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
  // 全民快递/校园跑腿模块（现代化O2O任务卡片风格）
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
  // 校园树洞：所有页面都是独立路由（无底部 Tabbar）
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
        path: 'schedule',
        name: 'Schedule',
        component: Schedule
      },
      {
        path: 'discover',
        name: 'Discover',
        component: Discover
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

// 固定浏览器标题，禁止根据路由动态修改
const FIXED_TITLE = '广东第二师范学院校园助手系统'
router.afterEach(() => {
  document.title = FIXED_TITLE
})

export default router
