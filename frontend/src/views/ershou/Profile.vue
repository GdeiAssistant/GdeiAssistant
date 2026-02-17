<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
// Mock 用户信息（对接 /api/avatar、/api/profile、/api/introduction）
const avatar = ref('/img/avatar/default.png')
const nickname = ref('二手用户')
const introduction = ref('这个人很懒，什么都没写_(:3 」∠)_')

const activeStat = ref('doing') // doing | sold | off
const doingList = ref([])   // 正在出售 Mock
const soldList = ref([])    // 已下架 Mock
const offList = ref([])     // 已出售 Mock

const currentList = computed(() => {
  if (activeStat.value === 'doing') return doingList.value
  if (activeStat.value === 'sold') return soldList.value
  return offList.value
})

function setStat(stat) {
  activeStat.value = stat
}

function goDetail(id) {
  router.push(`/ershou/detail/${id}`)
}
</script>

<template>
  <div class="ershou-profile-page">
    <!-- 统一顶部导航栏：左侧返回到应用首页，标题「个人中心」，无右侧按钮 -->
    <div class="ershou-header unified-header">
      <span class="ershou-header__back" @click="router.push('/')">返回</span>
      <h1 class="ershou-header__title">个人中心</h1>
      <span class="ershou-header__placeholder"></span>
    </div>
    <!-- 个人资料：原版 ershouPersonal.jsp profile 区块 -->
    <section class="profile">
      <i class="avt"><img class="avatar-img" :src="avatar" alt="头像"></i>
      <span class="nm">{{ nickname }}</span>
      <span class="introduction">
        <p class="intro-p">{{ introduction }}</p>
      </span>
    </section>

    <!-- 原版 ershouPersonal.jsp 仅有 profile + status（无「我发布的/我卖出的/我的收藏」列表） -->
    <!-- 状态 Tab：正在出售 / 已下架 / 已出售 -->
    <section class="status">
      <ul class="tabs">
        <li class="tab" :class="{ on: activeStat === 'doing' }" @click="setStat('doing')">正在出售<i class="line"></i></li>
        <li class="tab" :class="{ on: activeStat === 'sold' }" @click="setStat('sold')">已下架<i class="line"></i></li>
        <li class="tab" :class="{ on: activeStat === 'off' }" @click="setStat('off')">已出售<i class="line"></i></li>
      </ul>

      <div class="statlists">
        <div v-show="activeStat === 'doing'" class="statlist">
          <div v-for="item in doingList" :key="item.id" class="stat">
            <div class="info" @click="goDetail(item.id)">
              <i class="img"><img :src="item.preview" :alt="item.name"></i>
              <h5 class="tit">{{ item.name }}</h5>
              <em class="price">￥{{ item.price }}</em>
              <p class="tm">{{ item.publishTime }}</p>
            </div>
            <p class="btns">
              <a class="btn" href="javascript:;"><b>编辑</b></a>
              <a class="btn" href="javascript:;"><b>下架</b></a>
              <a class="btn" href="javascript:;"><b>确认售出</b></a>
            </p>
          </div>
          <p v-if="doingList.length === 0" class="nostatus-tip">暂无正在出售的商品</p>
        </div>
        <div v-show="activeStat === 'sold'" class="statlist">
          <div v-for="item in soldList" :key="item.id" class="stat">
            <div class="info" @click="goDetail(item.id)">
              <i class="img"><img :src="item.preview" :alt="item.name"></i>
              <h5 class="tit">{{ item.name }}</h5>
              <em class="price">￥{{ item.price }}</em>
              <p class="tm">{{ item.publishTime }}</p>
            </div>
            <p class="btns">
              <a class="btn" href="javascript:;"><b>编辑</b></a>
              <a class="btn" href="javascript:;"><b>上架</b></a>
            </p>
          </div>
          <p v-if="soldList.length === 0" class="nostatus-tip">暂无已下架的商品</p>
        </div>
        <div v-show="activeStat === 'off'" class="statlist">
          <div v-for="item in offList" :key="item.id" class="stat">
            <div class="info" @click="goDetail(item.id)">
              <i class="img"><img :src="item.preview" :alt="item.name"></i>
              <h5 class="tit">{{ item.name }}</h5>
              <em class="price">￥{{ item.price }}</em>
              <p class="tm">{{ item.publishTime }}</p>
            </div>
          </div>
          <p v-if="offList.length === 0" class="nostatus-tip">暂无已出售的商品</p>
        </div>
      </div>
    </section>

    <div style="height: 4rem;"></div>
  </div>
</template>

<style scoped>
.ershou-profile-page {
  min-height: 100vh;
  background: #e3eeec;
  padding-bottom: 60px;
}

.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.ershou-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
}
.ershou-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.ershou-header__placeholder {
  min-width: 48px;
  text-align: right;
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
  background: #44c1a5;
  border-top: 1px solid #39b89d;
  border-bottom: 4px solid #3cab93;
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
  font-size: 20px;
  color: #fff;
  display: block;
  margin-bottom: 5px;
}
.profile .introduction {
  color: #fff;
  line-height: 21px;
  font-size: 12px;
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
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 0 2px #d6e0de;
}
.status .tab {
  flex: 1;
  display: block;
  position: relative;
  color: #44c1a5;
  text-align: center;
  height: 32px;
  line-height: 32px;
  font-size: 14px;
  cursor: pointer;
}
.status .tab .line {
  width: 28px;
  height: 1px;
  background: #44c1a5;
  position: absolute;
  left: 50%;
  margin-left: -14px;
  bottom: 0;
  display: none;
}
.status .tabs .on .line { display: block; }
.status .tabs .on { font-weight: 600; }

.status .stat {
  background: #fff;
  margin-bottom: 8px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 0 2px #d6e0de;
}
.status .stat .info {
  position: relative;
  padding: 8px 8px 8px 75px;
  min-height: 60px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
.status .info .img {
  position: absolute;
  left: 8px;
  width: 60px;
  height: 60px;
  overflow: hidden;
  top: 8px;
}
.status .info img { width: 100%; height: 100%; object-fit: cover; }
.status .info h5 { color: #1c826c; font-size: 14px; line-height: 20px; margin: 0 0 4px; }
.status .info .price { color: #eb5055; line-height: 18px; font-style: normal; }
.status .info .tm { color: #999; font-size: 12px; margin: 0; }
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
  color: #999;
  border-left: 1px solid #ddd;
  line-height: 16px;
  height: 16px;
  font-weight: normal;
}
.status .btns .btn:first-child b { border-left: none; }

.nostatus-tip {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 24px;
  margin: 0;
  background: #fff;
  border-radius: 4px;
}
</style>
