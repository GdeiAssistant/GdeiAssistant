<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { getCurrentUserProfile } from '../../api/user.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const route = useRoute()
const activeStat = ref('lost')
const avatar = ref('/img/avatar/default.png')
const nickname = ref('用户')
const introduction = ref('这个人很懒，什么都没写_(:3 」∠)_')
const lostList = ref([])
const foundList = ref([])
const didFoundList = ref([])
const loading = ref(false)
const actionLoading = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function showDialog(message) {
  dialogMessage.value = message
  dialogVisible.value = true
}

function getQueryTab() {
  const value = route.query.tab
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return typeof value === 'string' ? value : ''
}

function normalizeStat(stat) {
  if (stat === 'found' || stat === 'didfound') {
    return stat
  }
  return 'lost'
}

function applyRouteState() {
  activeStat.value = normalizeStat(getQueryTab())
}

function switchStat(stat) {
  const normalized = normalizeStat(stat)
  if (activeStat.value === normalized && getQueryTab() === normalized) {
    return
  }
  router.replace({
    path: '/lostandfound/profile',
    query: {
      ...route.query,
      tab: normalized
    }
  })
}

function mapItem(item) {
  return {
    id: item.id,
    name: item.name || '',
    publishTime: item.publishTime || '',
    image: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : '/img/avatar/default.png'
  }
}

async function loadUserInfo() {
  const res = await getCurrentUserProfile()
  const data = res?.data || {}
  avatar.value = data.avatar || '/img/avatar/default.png'
  nickname.value = data.nickname || data.username || '用户'
  introduction.value = data.introduction || '这个人很懒，什么都没写_(:3 」∠)_'
}

async function loadItems() {
  const res = await request.get('/lostandfound/profile')
  const data = res?.data || {}
  lostList.value = Array.isArray(data.lost) ? data.lost.map(mapItem) : []
  foundList.value = Array.isArray(data.found) ? data.found.map(mapItem) : []
  didFoundList.value = Array.isArray(data.didfound) ? data.didfound.map(mapItem) : []
}

async function loadPage() {
  loading.value = true
  await Promise.allSettled([loadUserInfo(), loadItems()])
  loading.value = false
}

function editItem(id) {
  router.push({ path: '/lostandfound/publish', query: { edit: '1', id: String(id) } })
}

function goDetail(id) {
  router.push(`/lostandfound/detail/${id}`)
}

async function confirmDidFound(id) {
  if (actionLoading.value) return
  if (!window.confirm('确定标记为确认寻回吗？')) return
  actionLoading.value = true
  try {
    await request.post(`/lostandfound/item/id/${id}/didfound`)
    await loadItems()
    showDialog('已标记为确认寻回')
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  applyRouteState()
  loadPage()
})

watch(() => route.fullPath, () => {
  applyRouteState()
})
</script>

<template>
  <div class="lostandfound-profile">
    <CommunityHeader title="个人中心" moduleColor="#3b82f6" backTo="/lostandfound/home" />

    <section class="profile-section">
      <i class="avt">
        <img :src="avatar" alt="头像" />
      </i>
      <span class="nm">{{ nickname }}</span>
      <span class="introduction">
        <p>{{ introduction }}</p>
      </span>
    </section>

    <section class="status-section">
      <ul class="tabs">
        <li class="tab" :class="{ on: activeStat === 'lost' }" @click="switchStat('lost')" data-stat="lost">
          寻物<i class="line"></i>
        </li>
        <li class="tab" :class="{ on: activeStat === 'found' }" @click="switchStat('found')" data-stat="found">
          招领<i class="line"></i>
        </li>
        <li class="tab" :class="{ on: activeStat === 'didfound' }" @click="switchStat('didfound')" data-stat="didfound">
          已找回<i class="line"></i>
        </li>
      </ul>

      <div class="statlists">
        <div class="statlist" :class="{ nodis: activeStat !== 'lost' }" data-statlist="lost">
          <div v-if="loading" class="nostatus"><p class="tip">加载中...</p></div>
          <template v-else>
            <div v-if="lostList.length === 0" class="nostatus">
              <p class="tip">暂无寻物信息</p>
            </div>
            <div v-for="item in lostList" :key="item.id" class="stat community-card">
              <div class="info" @click="goDetail(item.id)">
                <i class="img">
                  <img :src="item.image" alt="" />
                </i>
                <h5 class="tit">{{ item.name }}</h5>
                <p class="tm">{{ item.publishTime }}</p>
              </div>
              <p class="btns">
                <a class="btn" href="javascript:;" @click.prevent="editItem(item.id)"><b>编辑</b></a>
                <a class="btn saled" href="javascript:;" @click.prevent="confirmDidFound(item.id)"><b>确认寻回</b></a>
              </p>
            </div>
          </template>
        </div>

        <div class="statlist" :class="{ nodis: activeStat !== 'found' }" data-statlist="found">
          <div v-if="loading" class="nostatus"><p class="tip">加载中...</p></div>
          <template v-else>
            <div v-if="foundList.length === 0" class="nostatus">
              <p class="tip">暂无招领信息</p>
            </div>
            <div v-for="item in foundList" :key="item.id" class="stat community-card">
              <div class="info" @click="goDetail(item.id)">
                <i class="img">
                  <img :src="item.image" alt="" />
                </i>
                <h5 class="tit">{{ item.name }}</h5>
                <p class="tm">{{ item.publishTime }}</p>
              </div>
              <p class="btns">
                <a class="btn" href="javascript:;" @click.prevent="editItem(item.id)"><b>编辑</b></a>
                <a class="btn saled" href="javascript:;" @click.prevent="confirmDidFound(item.id)"><b>确认寻回</b></a>
              </p>
            </div>
          </template>
        </div>

        <div class="statlist" :class="{ nodis: activeStat !== 'didfound' }" data-statlist="didfound">
          <div v-if="loading" class="nostatus"><p class="tip">加载中...</p></div>
          <template v-else>
            <div v-if="didFoundList.length === 0" class="nostatus">
              <p class="tip">暂无已找回信息</p>
            </div>
            <div v-for="item in didFoundList" :key="item.id" class="stat community-card">
              <div class="info">
                <i class="img">
                  <img :src="item.image" alt="" />
                </i>
                <h5 class="tit">{{ item.name }}</h5>
                <p class="tm">{{ item.publishTime }}</p>
              </div>
            </div>
          </template>
        </div>
      </div>
    </section>

    <div v-if="dialogVisible">
      <div class="community-dialog-mask" @click="dialogVisible = false"></div>
      <div class="community-dialog" style="--module-color: #3b82f6">
        <div class="community-dialog__title">提示</div>
        <div class="community-dialog__body">{{ dialogMessage }}</div>
        <div class="community-dialog__footer">
          <button class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lostandfound-profile {
  min-height: 100vh;
  background: var(--c-bg);
}

.profile-section {
  position: relative;
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  padding: 30px 20px 20px 140px;
  min-height: 90px;
}
.profile-section .avt {
  position: absolute;
  left: 25px;
  top: 25px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  display: block;
}
.profile-section .avt img {
  width: 64px !important;
  height: 64px !important;
  border-radius: 50%;
  border: 2px solid #fff;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.profile-section .nm {
  font-size: 20px;
  color: #fff;
  display: block;
  margin-bottom: 5px;
}
.profile-section .introduction {
  color: rgba(255, 255, 255, 0.9);
  line-height: 21px;
  font-size: 12px;
  display: block;
}
.profile-section .introduction p {
  margin: 0;
  padding: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-section {
  margin: 10px;
}
.status-section .tabs {
  display: flex;
  list-style: none;
  padding: 0;
  margin: 10px 0;
}
.status-section .tab {
  flex: 1;
  display: block;
  position: relative;
  color: var(--c-lostandfound);
  text-align: center;
  height: 32px;
  line-height: 32px;
  font-size: 14px;
  cursor: pointer;
  background: var(--c-card);
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  margin-right: 5px;
}
.status-section .tab:last-child {
  margin-right: 0;
}
.status-section .tab .line {
  width: 28px;
  height: 1px;
  background: var(--c-lostandfound);
  position: absolute;
  left: 50%;
  margin-left: -14px;
  bottom: 0;
  display: none;
}
.status-section .tab.on {
  background: var(--c-lostandfound);
  color: #fff;
}
.status-section .tab.on .line {
  display: block;
  background: #fff;
}

.statlists {
  position: relative;
}
.statlist {
  display: block;
}
.statlist.nodis {
  display: none !important;
}

.stat {
  margin-bottom: 8px;
  border-radius: var(--radius-md);
}
.stat .info {
  position: relative;
  padding: 8px 8px 8px 75px;
  min-height: 60px;
  border-bottom: 1px solid var(--c-border);
  cursor: pointer;
}
.stat .info .img {
  position: absolute;
  left: 8px;
  width: 60px;
  height: 60px;
  overflow: hidden;
  display: block;
  border-radius: var(--radius-sm);
}
.stat .info .img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.stat .info h5.tit {
  color: var(--c-lostandfound);
  font-size: 14px;
  line-height: 20px;
  margin: 0 0 4px 0;
  padding: 0;
}
.stat .info .tm {
  color: var(--c-text-3);
  font-size: 12px;
  margin: 0;
  padding: 0;
}
.stat .btns {
  display: flex;
}
.stat .btns .btn {
  flex: 1;
  display: block;
  padding: 7px 0;
  text-decoration: none;
}
.stat .btns .btn b {
  display: block;
  color: var(--c-text-3);
  border-left: 1px solid var(--c-divider);
  text-align: center;
  line-height: 16px;
  height: 16px;
}
.stat .btns .btn:first-child b {
  border-left: none;
}

.nostatus {
  position: relative;
  padding-top: 170px;
  min-height: 200px;
  text-align: center;
}
.nostatus::before {
  content: '';
  position: absolute;
  top: 44px;
  left: 50%;
  width: 88px;
  height: 88px;
  transform: translateX(-50%);
  border-radius: 28px;
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
  box-shadow: inset 0 0 0 1px #bfdbfe;
}
.nostatus::after {
  content: '';
  position: absolute;
  top: 77px;
  left: 50%;
  width: 30px;
  height: 30px;
  transform: translateX(-50%) rotate(45deg);
  border-right: 3px solid rgba(59, 130, 246, 0.6);
  border-bottom: 3px solid rgba(59, 130, 246, 0.6);
}
.nostatus .tip {
  text-align: center;
  color: var(--c-lostandfound);
  margin-bottom: 25px;
  font-size: 14px;
}
</style>
