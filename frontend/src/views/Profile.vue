<template>
  <div class="profile-page">
    <h1 class="page_title">个人中心</h1>

    <div class="weui-toptips weui_warn js_tooltips" style="display: none;"></div>

    <div class="weui-cells" style="margin-top: 0;">
      <div class="weui-cell weui-cell_access" @click="router.push('/user/avatar-edit')">
        <div class="weui-cell__bd"><p>头像</p></div>
        <div class="weui-cell__ft">
          <img :src="userInfo.avatar" style="width: 50px; height: 50px; border-radius: 50%; display: block;" alt="头像" />
        </div>
      </div>

      <div class="weui-cell">
        <div class="weui-cell__bd"><p>用户名</p></div>
        <div class="weui-cell__ft">{{ userInfo.username }}</div>
      </div>

      <div class="weui-cell weui-cell_access relative-cell" @click="openNicknameDialog">
        <div class="weui-cell__bd"><p>昵称</p></div>
        <div class="weui-cell__ft">{{ userInfo.nickname || '点击设置' }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openBirthdayPicker">
        <div class="weui-cell__bd"><p>生日</p></div>
        <div class="weui-cell__ft">{{ userInfo.birthday || '请选择' }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openFacultyPicker">
        <div class="weui-cell__bd"><p>院系</p></div>
        <div class="weui-cell__ft">{{ userInfo.faculty || facultyPlaceholder }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openMajorPicker">
        <div class="weui-cell__bd"><p>专业</p></div>
        <div class="weui-cell__ft">{{ userInfo.major || '请选择专业' }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openEnrollmentPicker">
        <div class="weui-cell__bd"><p>入学年份</p></div>
        <div class="weui-cell__ft">{{ userInfo.enrollment || '请选择年份' }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openLocationPicker">
        <div class="weui-cell__bd"><p>所在地</p></div>
        <div class="weui-cell__ft">{{ userInfo.location || '请选择所在地' }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openHometownPicker">
        <div class="weui-cell__bd"><p>家乡</p></div>
        <div class="weui-cell__ft">{{ userInfo.hometown || '请选择家乡' }}</div>
      </div>

      <a class="weui-cell weui-cell_access relative-cell" href="javascript:" @click.prevent="openIntroDialog">
        <div class="weui-cell__bd"><p>个人简介</p></div>
        <div class="weui-cell__ft">{{ userInfo.introduction ? '已填写' : '去填写' }}</div>
      </a>

      <div class="weui-cell">
        <div class="weui-cell__bd"><p>IP属地</p></div>
        <div class="weui-cell__ft">{{ userInfo.ipArea || '中国广东广州' }}</div>
      </div>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/privacy-setting')">
        <div class="weui-cell__bd"><p>隐私设置</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/feature-manage')">
        <div class="weui-cell__bd"><p>功能管理</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click="handlePasswordClick">
        <div class="weui-cell__bd"><p>修改密码</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/login-record')">
        <div class="weui-cell__bd"><p>登录记录</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/realname')">
        <div class="weui-cell__bd"><p>实名认证</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/bind-phone')">
        <div class="weui-cell__bd"><p>绑定手机</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/bind-email')">
        <div class="weui-cell__bd"><p>绑定邮箱</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/delete')">
        <div class="weui-cell__bd"><p>注销账号</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleReport">
        <div class="weui-cell__bd"><p>不良"校园贷"举报通道</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/download')">
        <div class="weui-cell__bd"><p>下载用户数据</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/feedback')">
        <div class="weui-cell__bd"><p>帮助与反馈</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells logout-cells" style="margin-bottom: 80px;">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleLogoutClick">
        <div class="weui-cell__bd"><p>退出账号</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div v-if="showLogoutDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showLogoutDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
        <div class="weui-dialog__bd">确定要退出账号吗？</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showLogoutDialog = false">取消</a>
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmLogout">确定退出</a>
        </div>
      </div>
    </div>

    <div v-if="showNicknameDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showNicknameDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">修改昵称</strong></div>
        <div class="weui-dialog__bd">
          <input type="text" class="custom-input" v-model="tempNickname" placeholder="请输入新昵称">
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showNicknameDialog = false">取消</a>
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmNickname">确定</a>
        </div>
      </div>
    </div>

    <div v-if="showIntroDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showIntroDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">修改个人简介</strong></div>
        <div class="weui-dialog__bd">
          <textarea class="custom-input" v-model="tempIntro" placeholder="一句话介绍自己..." rows="3"></textarea>
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showIntroDialog = false">取消</a>
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmIntro">确定</a>
        </div>
      </div>
    </div>

    <div v-if="showPwdDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showPwdDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
        <div class="weui-dialog__bd">修改密码功能暂未开放。如需修改，请前往学校统一身份认证平台或教务系统进行操作。</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="showPwdDialog = false">我知道了</a>
        </div>
      </div>
    </div>

    <!-- Fallback: 无 weui 时的日期选择（禁止未来日期） -->
    <div v-if="showDateFallback" class="dialog-wrapper">
      <div class="weui-mask" @click="showDateFallback = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">选择生日</strong></div>
        <div class="weui-dialog__bd">
          <input type="date" class="custom-input" v-model="tempDate" min="1900-01-01" :max="todayStr" style="width:100%;box-sizing:border-box;">
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showDateFallback = false">取消</a>
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmDateFallback">确定</a>
        </div>
      </div>
    </div>

    <!-- Fallback: 无 weui 时的列表选择 -->
    <div v-if="showListFallback" class="dialog-wrapper">
      <div class="weui-mask" @click="showListFallback = false"></div>
      <div class="weui-dialog weui-dialog--list">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">{{ listFallbackTitle }}</strong></div>
        <div class="weui-dialog__bd weui-dialog__bd--scroll">
          <div v-for="opt in listFallbackOptions" :key="opt" class="weui-dialog__item" @click="confirmListFallback(opt)">{{ opt }}</div>
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showListFallback = false">取消</a>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
// 全量地理数据（由 location.xml 迁移，国家 -> 省/州 -> 城市，含所有国家）
import locationTree from '../data/locationTree.js'

const router = useRouter()

// 与头像编辑页同步：从 localStorage 读取 user_avatar 更新 userInfo.avatar
const AVATAR_STORAGE_KEY = 'user_avatar'
const defaultAvatarUrl = '/img/login/qq.png'
function getStoredAvatar() {
  if (typeof localStorage === 'undefined') return defaultAvatarUrl
  return localStorage.getItem(AVATAR_STORAGE_KEY) || defaultAvatarUrl
}
function refreshAvatar() {
  userInfo.value.avatar = getStoredAvatar()
}

// === 用户核心数据 ===
const userInfo = ref({
  avatar: getStoredAvatar(),
  username: '测试用户',
  nickname: '二师小助手',
  birthday: '2002-05-15',
  faculty: '',
  major: '',
  enrollment: '2022',
  location: '',
  hometown: '',
  introduction: '',
  ipArea: '中国广东广州'
})

// === 弹窗控制与临时变量 ===
const showPwdDialog = ref(false)
const showNicknameDialog = ref(false)
const showIntroDialog = ref(false)
const showLogoutDialog = ref(false)
const tempNickname = ref('')
const tempIntro = ref('')

// ==========================================
// 院系-专业字典（与后端 OptionConstantUtils 一致）
// ==========================================
const facultyData = {
  '未选择': ['未选择'],
  '教育学院': ['未选择', '教育学', '学前教育', '小学教育', '特殊教育'],
  '政法系': ['未选择', '法学', '思想政治教育', '社会工作'],
  '中文系': ['未选择', '汉语言文学', '历史学', '秘书学'],
  '数学系': ['未选择', '数学与应用数学', '信息与计算科学', '统计学'],
  '外语系': ['未选择', '英语', '商务英语', '日语', '翻译'],
  '物理与信息工程系': ['未选择', '物理学', '电子信息工程', '通信工程'],
  '化学系': ['未选择', '化学', '应用化学', '材料化学'],
  '生物与食品工程学院': ['未选择', '生物科学', '生物技术', '食品科学与工程'],
  '体育学院': ['未选择', '体育教育', '社会体育指导与管理'],
  '美术学院': ['未选择', '美术学', '视觉传达设计', '环境设计'],
  '计算机科学系': ['未选择', '软件工程', '网络工程', '计算机科学与技术', '物联网工程'],
  '音乐系': ['未选择', '音乐学', '音乐表演', '舞蹈学'],
  '教师研修学院': ['未选择', '教育学', '教育技术学'],
  '成人教育学院': ['未选择', '汉语言文学', '学前教育', '行政管理'],
  '网络教育学院': ['未选择', '计算机科学与技术', '工商管理', '会计学'],
  '马克思主义学院': ['未选择', '思想政治教育', '马克思主义理论']
}
const facultyList = Object.keys(facultyData)
const facultyPlaceholder = '请选择院系'
const majorList = ref([])
const updateMajorListByFaculty = () => {
  majorList.value = (facultyData[userInfo.value.faculty] || ['未选择']).slice()
}
// 院系变化时立即将专业重置为「未选择」
watch(() => userInfo.value.faculty, () => {
  userInfo.value.major = '未选择'
  updateMajorListByFaculty()
})

// 年份列表
const yearList = ref([])

// 所在地/家乡使用 locationTree（见顶部 import），结构：国家 -> 省/州 -> 城市
const getLocationFlatOptions = () =>
  locationTree.flatMap(c => (c.children || []).flatMap(s => (s.children || []).map(city => `${c.label} ${s.label} ${city.label}`)))

const initYearList = () => {
  const currentYear = new Date().getFullYear()
  for (let i = 2014; i <= currentYear; i++) yearList.value.push(i)
}

const todayStr = (() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})()

// ==========================================
// 核心逻辑 3：自动保存与 Picker 打开
// ==========================================

const handleAutoSave = () => {
  const payload = { ...userInfo.value }
  if (payload.location) payload.location = String(payload.location).trim()
  if (payload.hometown) payload.hometown = String(payload.hometown).trim()
  console.log('自动保存的数据:', payload)
}

const getWeui = () => (typeof window !== 'undefined' ? window.weui : null)

// 生日：weui.datePicker，start 1900、end 今日，禁止选择未来日期
const openBirthdayPicker = () => {
  const weui = getWeui()
  if (weui && typeof weui.datePicker === 'function') {
    weui.datePicker({
      defaultValue: userInfo.value.birthday ? userInfo.value.birthday.split('-') : [],
      start: new Date(1900, 0, 1),
      end: new Date(),
      onConfirm: (result) => {
        const val = result && result.length >= 3 ? `${result[0]}-${String(result[1]).padStart(2, '0')}-${String(result[2]).padStart(2, '0')}` : ''
        if (val) {
          userInfo.value.birthday = val
          handleAutoSave()
        }
      }
    })
    return
  }
  showDateFallback.value = true
  tempDate.value = userInfo.value.birthday || ''
}

// 院系：weui.picker 单列，选中后立即将专业重置为「未选择」
const openFacultyPicker = () => {
  const weui = getWeui()
  const items = facultyList.map(label => ({ label, value: label }))
  if (weui && typeof weui.picker === 'function') {
    weui.picker(items, {
      defaultValue: [userInfo.value.faculty || facultyList[0]],
      onConfirm: (result) => {
        if (result && result[0]) {
          userInfo.value.faculty = result[0].value
          userInfo.value.major = '未选择'
          updateMajorListByFaculty()
          handleAutoSave()
        }
      }
    })
    return
  }
  openListFallback('请选择院系', facultyList, (val) => {
    userInfo.value.faculty = val
    userInfo.value.major = '未选择'
    updateMajorListByFaculty()
    handleAutoSave()
  })
}

// 专业：weui.picker 单列；未选院系时 weui.alert 提示并中止
const openMajorPicker = () => {
  const faculty = userInfo.value.faculty
  if (!faculty || faculty === '未选择') {
    const weui = getWeui()
    if (weui && typeof weui.alert === 'function') {
      weui.alert('请先选择院系')
    } else {
      alert('请先选择院系')
    }
    return
  }
  const weui = getWeui()
  const list = majorList.value.length ? majorList.value : ['未选择']
  const items = list.map(label => ({ label, value: label }))
  if (weui && typeof weui.picker === 'function') {
    weui.picker(items, {
      defaultValue: [userInfo.value.major || '未选择'],
      onConfirm: (result) => {
        if (result && result[0]) {
          userInfo.value.major = result[0].value
          handleAutoSave()
        }
      }
    })
    return
  }
  openListFallback('请选择专业', majorList.value, (val) => {
    userInfo.value.major = val
    handleAutoSave()
  })
}

// 入学年份：weui.picker 单列
const openEnrollmentPicker = () => {
  const weui = getWeui()
  const items = yearList.value.map(y => ({ label: String(y), value: String(y) }))
  if (weui && typeof weui.picker === 'function') {
    weui.picker(items, {
      defaultValue: [userInfo.value.enrollment || String(yearList.value[yearList.value.length - 1])],
      onConfirm: (result) => {
        if (result && result[0]) {
          userInfo.value.enrollment = result[0].value
          handleAutoSave()
        }
      }
    })
    return
  }
  openListFallback('请选择年份', yearList.value.map(String), (val) => {
    userInfo.value.enrollment = val
    handleAutoSave()
  })
}

// 所在地：weui.picker 三列（国家-省份-城市），结果格式「中国 天津 和平」
const openLocationPicker = () => {
  const weui = getWeui()
  if (weui && typeof weui.picker === 'function') {
    weui.picker(locationTree, {
      container: 'body',
      onConfirm: (result) => {
        if (result && result.length >= 3) {
          const str = result.map(r => r.label).join(' ')
          userInfo.value.location = str
          handleAutoSave()
        }
      }
    })
    return
  }
  openListFallback('请选择所在地', getLocationFlatOptions(), (val) => {
    userInfo.value.location = val
    handleAutoSave()
  })
}

// 家乡：同上，三列，结果「中国 省份 城市」
const openHometownPicker = () => {
  const weui = getWeui()
  if (weui && typeof weui.picker === 'function') {
    weui.picker(locationTree, {
      container: 'body',
      onConfirm: (result) => {
        if (result && result.length >= 3) {
          const str = result.map(r => r.label).join(' ')
          userInfo.value.hometown = str
          handleAutoSave()
        }
      }
    })
    return
  }
  openListFallback('请选择家乡', getLocationFlatOptions(), (val) => {
    userInfo.value.hometown = val
    handleAutoSave()
  })
}

// 无 weui 时的列表选择兜底
const showListFallback = ref(false)
const listFallbackTitle = ref('')
const listFallbackOptions = ref([])
const listFallbackCallback = ref(null)
const openListFallback = (title, options, onConfirm) => {
  listFallbackTitle.value = title
  listFallbackOptions.value = options
  listFallbackCallback.value = onConfirm
  showListFallback.value = true
}
const confirmListFallback = (val) => {
  if (listFallbackCallback.value) listFallbackCallback.value(val)
  showListFallback.value = false
}

// 无 weui 时的日期选择兜底
const showDateFallback = ref(false)
const tempDate = ref('')
const confirmDateFallback = () => {
  if (tempDate.value) {
    userInfo.value.birthday = tempDate.value
    handleAutoSave()
  }
  showDateFallback.value = false
}

// 昵称弹窗与保存
const openNicknameDialog = () => { tempNickname.value = userInfo.value.nickname; showNicknameDialog.value = true }
const confirmNickname = () => {
  if (tempNickname.value.trim()) {
    userInfo.value.nickname = tempNickname.value
    handleAutoSave() // 修改完立即保存
  }
  showNicknameDialog.value = false
}

// 简介弹窗与保存
const openIntroDialog = () => { tempIntro.value = userInfo.value.introduction; showIntroDialog.value = true }
const confirmIntro = () => {
  userInfo.value.introduction = tempIntro.value.trim()
  handleAutoSave()
  showIntroDialog.value = false
}

// ==========================================
// 底部功能入口
// ==========================================
const handleNav = (path) => { if (path) router.push(path) }
const handlePasswordClick = () => { showPwdDialog.value = true }
const handleReport = () => { window.location.href = 'https://www.wjx.top/m/47687434.aspx' }

const doLogout = () => {
  localStorage.clear()
  sessionStorage.clear()
  router.replace('/login')
}

const handleLogoutClick = () => {
  const weui = getWeui()
  if (weui && typeof weui.confirm === 'function') {
    weui.confirm('确定要退出账号吗？', {
      buttons: [
        { label: '取消', type: 'default' },
        { label: '确定退出', type: 'primary', onClick: () => doLogout() }
      ]
    })
    return
  }
  showLogoutDialog.value = true
}

const confirmLogout = () => {
  showLogoutDialog.value = false
  doLogout()
}

onMounted(() => {
  initYearList()
  updateMajorListByFaculty()
  refreshAvatar()
  window.addEventListener('avatar-changed', refreshAvatar)
})

onBeforeUnmount(() => {
  window.removeEventListener('avatar-changed', refreshAvatar)
})
</script>

<style scoped>
/* 整个页面背景纯白/微灰 */
.profile-page {
  background-color: #f8f8f8;
  min-height: 100vh;
}

/* 标题样式保持不变 */
.page_title {
  text-align: center;
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  padding: 40px 0 20px 0;
  margin: 0;
}

/* 列表区域样式 */
.profile-page .weui-cells {
  background-color: #fff;
  margin-top: 12px;
}
.profile-page .weui-cells:first-child {
  margin-top: 0;
}
.profile-page .weui-cells::before, .profile-page .weui-cells::after { border: none; }

/* 退出账号：显式顶部分割线 */
.profile-page .logout-cells .weui-cell::before {
  display: block !important;
  content: " ";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 1px;
  border-top: 1px solid #e0e0e0;
  background-color: #e0e0e0;
  z-index: 2;
}

/* 每一行的样式 */
.profile-page .weui-cell { padding: 16px 15px; position: relative; }

/* 贯穿全屏的浅横线 */
.profile-page .weui-cell::before {
  content: " "; position: absolute; left: 15px; right: 0; top: 0; height: 1px;
  border-top: 1px solid #f0f0f0; color: #f0f0f0; transform-origin: 0 0; transform: scaleY(0.5); z-index: 2;
}

/* 左右文字颜色 */
.profile-page .weui-cell__bd p { color: #333; font-size: 16px; margin: 0;}
.profile-page .weui-cell__ft { color: #999; font-size: 15px; }

/* Fallback 列表弹窗 */
.weui-dialog__bd--scroll { max-height: 280px; overflow-y: auto; }
.weui-dialog__item { padding: 12px 24px; border-bottom: 1px solid #eee; cursor: pointer; }
.weui-dialog__item:active { background: #f5f5f5; }

/* 弹窗样式 */
.custom-input { width: 100%; padding: 10px; box-sizing: border-box; border: 1px solid #e5e5e5; border-radius: 6px; font-size: 15px; outline: none; }
.weui-mask { position: fixed; z-index: 1000; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); }
.weui-dialog { position: fixed; z-index: 5000; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #fff; text-align: center; border-radius: 12px; width: 320px; overflow: hidden;}
.weui-dialog__hd { padding: 32px 24px 16px; }
.weui-dialog__title { font-weight: 700; font-size: 17px; }
.weui-dialog__bd { padding: 0 24px 32px; font-size: 15px; color: #666; text-align: left; }
.weui-dialog__ft { display: flex; border-top: 1px solid #e5e5e5; line-height: 56px; }
.weui-dialog__btn { flex: 1; color: #07c160; text-decoration: none; border-left: 1px solid #e5e5e5; cursor: pointer;}
.weui-dialog__btn:first-child { border-left: none; color: #333; }
</style>