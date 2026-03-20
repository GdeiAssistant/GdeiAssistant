<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { getCurrentUserProfile } from '../../api/user.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const route = useRoute()

const avatar = ref('/img/avatar/default.png')
const nickname = ref('二手用户')
const introduction = ref('这个人很懒，什么都没写_(:3 」∠)_')
const activeStat = ref('doing')
const doingList = ref([])
const soldList = ref([])
const offList = ref([])
const loading = ref(false)
const actionLoading = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

const emptyText = computed(() => {
  if (activeStat.value === 'doing') return '暂无正在出售的商品'
  if (activeStat.value === 'sold') return '暂无已售出的商品'
  return '暂无已下架的商品'
})

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
  if (stat === 'sold' || stat === 'off') {
    return stat
  }
  return 'doing'
}

function applyRouteState() {
  activeStat.value = normalizeStat(getQueryTab())
}

function setStat(stat) {
  const normalized = normalizeStat(stat)
  if (activeStat.value === normalized && getQueryTab() === normalized) {
    return
  }
  router.replace({
    path: '/ershou/profile',
    query: {
      ...route.query,
      tab: normalized
    }
  })
}

function mapProfileItem(item) {
  return {
    id: item.id,
    name: item.name || '',
    price: item.price || 0,
    publishTime: item.publishTime || '',
    preview: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : '/img/avatar/default.png'
  }
}

async function loadUserInfo() {
  const res = await getCurrentUserProfile()
  const data = res?.data || {}
  avatar.value = data.avatar || '/img/avatar/default.png'
  nickname.value = data.nickname || data.username || '二手用户'
  introduction.value = data.introduction || '这个人很懒，什么都没写_(:3 」∠)_'
}

async function loadItems() {
  const res = await request.get('/ershou/profile')
  const data = res?.data || {}
  doingList.value = Array.isArray(data.doing) ? data.doing.map(mapProfileItem) : []
  soldList.value = Array.isArray(data.sold) ? data.sold.map(mapProfileItem) : []
  offList.value = Array.isArray(data.off) ? data.off.map(mapProfileItem) : []
}

async function loadPage() {
  loading.value = true
  await Promise.allSettled([loadUserInfo(), loadItems()])
  loading.value = false
}

function goDetail(id) {
  router.push(`/ershou/detail/${id}`)
}

function editItem(id) {
  router.push({ path: '/ershou/publish', query: { edit: '1', id: String(id) } })
}

async function updateItemState(id, state, confirmText, successText) {
  if (actionLoading.value) return
  if (confirmText && !window.confirm(confirmText)) return
  actionLoading.value = true
  try {
    await request.post(`/ershou/item/state/id/${id}`, null, { params: { state } })
    await loadItems()
    showDialog(successText)
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
  <div class="ershou-profile-page">
    <CommunityHeader title="个人中心" moduleColor="#10b981" backTo="/ershou/home" />

    <section class="profile">
      <i class="avt"><img class="avatar-img" :src="avatar" alt="头像"></i>
      <span class="nm">{{ nickname }}</span>
      <span class="introduction">
        <p class="intro-p">{{ introduction }}</p>
      </span>
    </section>

    <section class="status">
      <ul class="tabs">
        <li class="tab" :class="{ on: activeStat === 'doing' }" @click="setStat('doing')">正在出售<i class="line"></i></li>
        <li class="tab" :class="{ on: activeStat === 'sold' }" @click="setStat('sold')">已售出<i class="line"></i></li>
        <li class="tab" :class="{ on: activeStat === 'off' }" @click="setStat('off')">已下架<i class="line"></i></li>
      </ul>

      <div class="statlists">
        <div v-show="activeStat === 'doing'" class="statlist">
          <p v-if="loading" class="nostatus-tip">加载中...</p>
          <template v-else>
            <div v-for="item in doingList" :key="item.id" class="stat community-card">
              <div class="info" @click="goDetail(item.id)">
                <i class="img"><img :src="item.preview" :alt="item.name"></i>
                <h5 class="tit">{{ item.name }}</h5>
                <em class="price">￥{{ item.price }}</em>
                <p class="tm">{{ item.publishTime }}</p>
              </div>
              <p class="btns">
                <a class="btn" href="javascript:;" @click.prevent="editItem(item.id)"><b>编辑</b></a>
                <a class="btn" href="javascript:;" @click.prevent="updateItemState(item.id, 0, '确定下架这件商品吗？', '已下架')"><b>下架</b></a>
                <a class="btn" href="javascript:;" @click.prevent="updateItemState(item.id, 2, '确定标记为已出售吗？', '已标记为已出售')"><b>确认售出</b></a>
              </p>
            </div>
            <p v-if="doingList.length === 0" class="nostatus-tip">{{ emptyText }}</p>
          </template>
        </div>

        <div v-show="activeStat === 'sold'" class="statlist">
          <p v-if="loading" class="nostatus-tip">加载中...</p>
          <template v-else>
            <div v-for="item in soldList" :key="item.id" class="stat community-card">
              <div class="info">
                <i class="img"><img :src="item.preview" :alt="item.name"></i>
                <h5 class="tit">{{ item.name }}</h5>
                <em class="price">￥{{ item.price }}</em>
                <p class="tm">{{ item.publishTime }}</p>
              </div>
              <p class="btns">
                <span class="btn"><b class="readonly-hint">已售出的商品不再支持编辑或重新上架</b></span>
              </p>
            </div>
            <p v-if="soldList.length === 0" class="nostatus-tip">{{ emptyText }}</p>
          </template>
        </div>

        <div v-show="activeStat === 'off'" class="statlist">
          <p v-if="loading" class="nostatus-tip">加载中...</p>
          <template v-else>
            <div v-for="item in offList" :key="item.id" class="stat community-card">
              <div class="info" @click="goDetail(item.id)">
                <i class="img"><img :src="item.preview" :alt="item.name"></i>
                <h5 class="tit">{{ item.name }}</h5>
                <em class="price">￥{{ item.price }}</em>
                <p class="tm">{{ item.publishTime }}</p>
              </div>
              <p class="btns">
                <a class="btn" href="javascript:;" @click.prevent="editItem(item.id)"><b>编辑</b></a>
                <a class="btn" href="javascript:;" @click.prevent="updateItemState(item.id, 1, '', '已重新上架')"><b>重新上架</b></a>
              </p>
            </div>
            <p v-if="offList.length === 0" class="nostatus-tip">{{ emptyText }}</p>
          </template>
        </div>
      </div>
    </section>

    <div v-if="dialogVisible">
      <div class="community-dialog-mask" @click="dialogVisible = false"></div>
      <div class="community-dialog">
        <div class="community-dialog__title">提示</div>
        <div class="community-dialog__body">{{ dialogMessage }}</div>
        <div class="community-dialog__footer">
          <button class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>

    <div style="height: 4rem;"></div>
  </div>
</template>

<style scoped>
.ershou-profile-page {
  min-height: 100vh;
  background: var(--c-bg);
  padding-bottom: 60px;
}

.avatar-img {
  width: 64px !important;
  height: 64px !important;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.profile {
  position: relative;
  background: linear-gradient(135deg, #10b981, #059669);
  padding: 30px 20px 20px 140px;
  min-height: 90px;
}
.profile .avt {
  position: absolute;
  left: 25px;
  top: 25px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
}
.profile .avt img,
.profile .avt .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
.profile .nm {
  font-size: var(--font-2xl);
  color: #fff;
  display: block;
  margin-bottom: 5px;
}
.profile .introduction {
  color: #fff;
  line-height: 21px;
  font-size: var(--font-sm);
  display: block;
}
.profile .intro-p {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status { margin: 10px; }
.status .tabs {
  display: flex;
  margin-bottom: 10px;
  background: var(--c-card);
  border-radius: var(--radius-sm);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.status .tab {
  flex: 1;
  display: block;
  position: relative;
  color: var(--c-ershou);
  text-align: center;
  height: 32px;
  line-height: 32px;
  font-size: var(--font-base);
  cursor: pointer;
}
.status .tab .line {
  width: 28px;
  height: 2px;
  background: var(--c-ershou);
  position: absolute;
  left: 50%;
  margin-left: -14px;
  bottom: 0;
  display: none;
  border-radius: 1px;
}
.status .tabs .on .line { display: block; }
.status .tabs .on { font-weight: 600; }

.status .stat {
  margin-bottom: 8px;
  overflow: hidden;
}
.status .stat .info {
  position: relative;
  padding: 8px 8px 8px 75px;
  min-height: 60px;
  border-bottom: 1px solid var(--c-border);
}
.status .info .img {
  position: absolute;
  left: 8px;
  width: 60px;
  height: 60px;
  overflow: hidden;
  top: 8px;
  border-radius: var(--radius-sm);
}
.status .info img { width: 100%; height: 100%; object-fit: cover; }
.status .info h5 { color: var(--c-ershou); font-size: var(--font-base); line-height: 20px; margin: 0 0 4px; }
.status .info .price { color: #eb5055; line-height: 18px; font-style: normal; }
.status .info .tm { color: var(--c-text-3); font-size: var(--font-sm); margin: 0; }
.status .stat .btns {
  display: flex;
}
.status .btns .btn {
  flex: 1;
  display: block;
  padding: 7px 0;
  text-align: center;
  text-decoration: none;
  cursor: pointer;
}
.status .btns .btn b {
  display: block;
  color: var(--c-text-3);
  border-left: 1px solid var(--c-border);
  line-height: 16px;
  height: 16px;
  font-weight: normal;
}
.status .btns .btn:first-child b { border-left: none; }
.readonly-hint {
  color: var(--c-text-3);
  font-weight: normal;
  font-size: var(--font-sm);
  border-left: none;
}

.nostatus-tip {
  text-align: center;
  color: var(--c-text-3);
  font-size: var(--font-base);
  padding: 24px;
  margin: 0;
  background: var(--c-card);
  border-radius: var(--radius-sm);
}
</style>
