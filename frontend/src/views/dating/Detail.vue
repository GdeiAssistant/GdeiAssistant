<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()
const item = ref(null)
const pickContent = ref('')
const pickSubmitting = ref(false)
const pickSuccessVisible = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function getGradeText(grade) {
  const map = { 1: '大一', 2: '大二', 3: '大三', 4: '大四' }
  return map[grade] || '未知'
}

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function submitPick() {
  const text = pickContent.value && pickContent.value.trim()
  if (!text) {
    showDialog('请输入撩一下的留言信息')
    return
  }
  if (text.length > 50) {
    showDialog('撩一下输入的内容太长了')
    return
  }
  pickSubmitting.value = true
  request.post('/dating/pick', { profileId: route.params.id, content: text })
    .then(() => {
      pickSuccessVisible.value = true
      pickContent.value = ''
      pickSubmitting.value = false
      setTimeout(() => { pickSuccessVisible.value = false }, 1500)
    })
    .catch(() => { pickSubmitting.value = false })
}

onMounted(async () => {
  try {
    const res = await request.get(`/dating/item/${route.params.id}`)
    item.value = res.data || res
  } catch (e) {
    showDialog('加载失败')
  }
})
</script>

<template>
  <div class="dating-detail">
    <div class="dating-header unified-header">
      <span class="dating-header__back" @click="router.back()">返回</span>
      <h1 class="dating-header__title">卖室友</h1>
      <span class="dating-header__placeholder"></span>
    </div>

    <div v-if="item" class="dating-detail__box">
      <div class="dating-detail__name">{{ item.name }}</div>
      <div class="dating-detail__photo">
        <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" :alt="item.name" />
      </div>
      <div class="dating-detail__bio">{{ item.bio || item.content }}</div>
      <ul class="dating-detail__info">
        <li><span>年级：</span>{{ getGradeText(item.grade) }}</li>
        <li><span>专业：</span>{{ item.faculty }}</li>
        <li><span>家乡：</span>{{ item.hometown }}</li>
        <li><span>QQ：</span>{{ item.contactVisible ? item.qq : '对方接受了撩一下后才可见哦' }}</li>
        <li><span>微信：</span>{{ item.contactVisible ? item.wechat : '对方接受了撩一下后才可见哦' }}</li>
      </ul>
      <div class="dating-detail__pick">
        <textarea v-model="pickContent" class="dating-textarea" placeholder="来说点什么吧，不超过50字" rows="3"></textarea>
        <button type="button" class="circle-btn" :disabled="pickSubmitting" @click="submitPick">撩一下</button>
      </div>
    </div>

    <div v-if="pickSuccessVisible" class="weui-mask_transparent"></div>
    <div v-if="pickSuccessVisible" class="weui-toast weui-toast_text">
      <p class="weui-toast__content">发送成功，请耐心等待对方回复</p>
    </div>

    <div v-if="dialogVisible" class="weui-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="weui-dialog">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
      <div class="weui-dialog__bd">{{ dialogMessage }}</div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-detail { background: #eee; min-height: 100vh; padding-bottom: 40px; }
.dating-header.unified-header {
  display: flex; align-items: center; justify-content: space-between;
  height: 44px; padding: 0 12px;
  background: linear-gradient(180deg, #78e2d1 0%, #6dcbbd 100%);
  color: #fff;
}
.dating-header__back { color: #fff; cursor: pointer; min-width: 48px; font-size: 14px; }
.dating-header__title { flex: 1; text-align: center; font-size: 16px; margin: 0; }
.dating-header__placeholder { min-width: 48px; }

.dating-detail__box { width: 90%; margin: 10px auto; background: #fff; overflow: hidden; padding: 15px; border-radius: 8px; }
.dating-detail__name { font-size: 22px; color: #6dcbbd; font-weight: bold; margin-bottom: 12px; }
.dating-detail__photo { width: 100%; border-radius: 8px; overflow: hidden; background: #eee; margin-bottom: 12px; }
.dating-detail__photo img { width: 100%; height: auto; max-height: 360px; object-fit: cover; }
.dating-detail__bio { padding: 12px 0; line-height: 1.6; color: #333; font-size: 15px; }
.dating-detail__info { list-style: none; margin: 0; padding: 0; width: 90%; margin: 0 auto; }
.dating-detail__info li { height: 44px; line-height: 44px; border-bottom: 1px dashed #ccc; font-size: 14px; color: #333; }
.dating-detail__info li span { font-weight: bold; margin-right: 8px; color: #000; }
.dating-detail__pick { border-top: 2px dashed #eee; padding-top: 15px; margin-top: 15px; text-align: center; }
.dating-textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; min-height: 80px; box-sizing: border-box; margin-bottom: 12px; }
.circle-btn { padding: 10px 28px; background: #2ee9d0; color: #fff; border: none; border-radius: 24px; font-size: 16px; cursor: pointer; }
.circle-btn:disabled { opacity: 0.6; }

.weui-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); z-index: 1000; }
.weui-dialog { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 85%; max-width: 300px; background: #fff; border-radius: 8px; z-index: 1001; overflow: hidden; }
.weui-dialog__hd { padding: 16px; text-align: center; }
.weui-dialog__title { font-size: 17px; color: #333; }
.weui-dialog__bd { padding: 10px 20px; text-align: center; font-size: 15px; color: #666; }
.weui-dialog__ft { display: flex; border-top: 1px solid #eee; }
.weui-dialog__btn { flex: 1; padding: 14px; text-align: center; color: #6dcbbd; text-decoration: none; }
.weui-mask_transparent { position: fixed; inset: 0; z-index: 1002; }
.weui-toast { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); background: rgba(0,0,0,0.7); color: #fff; padding: 16px 24px; border-radius: 8px; z-index: 1003; }
.weui-toast__content { margin: 0; font-size: 14px; }
</style>
