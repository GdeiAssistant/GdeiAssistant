<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { getCurrentUserProfile } from '../../api/user.js'

const router = useRouter()
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

function switchStat(stat) {
  activeStat.value = stat
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
  loadPage()
})
</script>

<template>
  <div class="lostandfound-profile">
    <div class="unified-header">
      <span class="unified-header__back" @click="router.push('/lostandfound/home')">返回</span>
      <h1 class="unified-header__title">个人中心</h1>
      <span class="unified-header__placeholder"></span>
    </div>

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
          失物<i class="line"></i>
        </li>
        <li class="tab" :class="{ on: activeStat === 'found' }" @click="switchStat('found')" data-stat="found">
          招领<i class="line"></i>
        </li>
        <li class="tab" :class="{ on: activeStat === 'didfound' }" @click="switchStat('didfound')" data-stat="didfound">
          确认寻回<i class="line"></i>
        </li>
      </ul>

      <div class="statlists">
        <div class="statlist" :class="{ nodis: activeStat !== 'lost' }" data-statlist="lost">
          <div v-if="loading" class="nostatus"><p class="tip">加载中...</p></div>
          <template v-else>
            <div v-if="lostList.length === 0" class="nostatus">
              <p class="tip">暂无失物信息</p>
            </div>
            <div v-for="item in lostList" :key="item.id" class="stat">
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
            <div v-for="item in foundList" :key="item.id" class="stat">
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
              <p class="tip">暂无确认寻回信息</p>
            </div>
            <div v-for="item in didFoundList" :key="item.id" class="stat">
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
      <div class="weui-mask" @click="dialogVisible = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
        <div class="weui-dialog__bd">{{ dialogMessage }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lostandfound-profile {
  min-height: 100vh;
  background: #e3eeec;
}

.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.unified-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.unified-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.unified-header__placeholder {
  min-width: 48px;
}

.profile-section {
  position: relative;
  background: #44c1a5;
  border-top: 1px solid #39b89d;
  border-bottom: 4px solid #3cab93;
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
  color: #fff;
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
  margin-bottom: 10px;
  list-style: none;
  padding: 0;
  margin: 10px 0;
}
.status-section .tab {
  flex: 1;
  display: block;
  position: relative;
  color: #44c1a5;
  text-align: center;
  height: 32px;
  line-height: 32px;
  font-size: 14px;
  cursor: pointer;
  background: #fff;
  border-radius: 4px 4px 0 0;
  margin-right: 5px;
}
.status-section .tab:last-child {
  margin-right: 0;
}
.status-section .tab .line {
  width: 28px;
  height: 1px;
  background: #44c1a5;
  position: absolute;
  left: 50%;
  margin-left: -14px;
  bottom: 0;
  display: none;
}
.status-section .tab.on {
  background: #44c1a5;
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
  background: #fff;
  margin-bottom: 8px;
  border-radius: 4px;
}
.stat .info {
  position: relative;
  padding: 8px 8px 8px 75px;
  min-height: 60px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
.stat .info .img {
  position: absolute;
  left: 8px;
  width: 60px;
  height: 60px;
  overflow: hidden;
  display: block;
}
.stat .info .img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.stat .info h5.tit {
  color: #1c826c;
  font-size: 14px;
  line-height: 20px;
  margin: 0 0 4px 0;
  padding: 0;
}
.stat .info .tm {
  color: #999;
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
  color: #999;
  border-left: 1px solid #ddd;
  text-align: center;
  line-height: 16px;
  height: 16px;
}
.stat .btns .btn:first-child b {
  border-left: none;
}

.nostatus {
  background: url(/img/lostandfound/nostatus.png) no-repeat center 60px;
  background-size: 88px;
  padding-top: 170px;
  min-height: 200px;
  text-align: center;
}
.nostatus .tip {
  text-align: center;
  color: #44c1a5;
  margin-bottom: 25px;
  font-size: 14px;
}

.weui-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
}
.weui-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 300px;
  background: #fff;
  border-radius: 8px;
  z-index: 1001;
  overflow: hidden;
}
.weui-dialog__hd {
  padding: 20px 20px 10px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
  font-weight: 500;
  color: #333;
}
.weui-dialog__bd {
  padding: 10px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
  word-wrap: break-word;
  word-break: break-all;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #d9d9d9;
}
.weui-dialog__btn {
  flex: 1;
  padding: 15px 0;
  text-align: center;
  font-size: 17px;
  color: #3cc395;
  text-decoration: none;
}
.weui-dialog__btn_primary {
  color: #3cc395;
  font-weight: 500;
}
</style>
