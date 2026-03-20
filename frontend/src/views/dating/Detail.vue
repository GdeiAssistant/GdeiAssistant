<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

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
  const params = new URLSearchParams()
  params.append('profileId', String(route.params.id))
  params.append('content', text)
  request.post('/dating/pick', params)
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
    const res = await request.get(`/dating/profile/id/${route.params.id}`)
    const data = res?.data
    if (data && res.success !== false) {
      const profile = data.profile || {}
      item.value = {
        id: profile.profileId,
        name: profile.nickname,
        image: data.pictureURL,
        images: data.pictureURL ? [data.pictureURL] : [],
        bio: profile.content,
        content: profile.content,
        grade: profile.grade,
        faculty: profile.faculty,
        hometown: profile.hometown,
        qq: profile.qq,
        wechat: profile.wechat,
        contactVisible: data.isContactVisible === true
      }
    } else {
      item.value = null
    }
  } catch (e) {
    item.value = null
  }
})
</script>

<template>
  <div class="community-page dating-detail">
    <CommunityHeader title="卖室友" moduleColor="#ec4899" backTo="/dating/home" />

    <div v-if="item" class="community-card dating-detail__box" style="--module-color: #ec4899; animation: community-slide-up 0.4s ease both;">
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

    <div v-if="pickSuccessVisible" class="community-dialog-mask" style="background: transparent;"></div>
    <div v-if="pickSuccessVisible" class="dating-toast">
      <p class="dating-toast__content">发送成功，请耐心等待对方回复</p>
    </div>

    <div v-if="dialogVisible" class="community-dialog-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="community-dialog" style="--module-color: #ec4899">
      <div class="community-dialog__title">提示</div>
      <div class="community-dialog__body">{{ dialogMessage }}</div>
      <div class="community-dialog__footer">
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-detail { padding-bottom: 40px; }

.dating-detail__box {
  width: 90%;
  margin: var(--space-md) auto;
  padding: var(--space-lg);
  overflow: hidden;
}
.dating-detail__name {
  font-size: 22px;
  color: var(--c-dating);
  font-weight: bold;
  margin-bottom: var(--space-md);
}
.dating-detail__photo {
  width: 100%;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--c-bg);
  margin-bottom: var(--space-md);
}
.dating-detail__photo img { width: 100%; height: auto; max-height: 360px; object-fit: cover; }
.dating-detail__bio {
  padding: var(--space-md) 0;
  line-height: 1.6;
  color: var(--c-text-1);
  font-size: var(--font-md);
}
.dating-detail__info { list-style: none; margin: 0; padding: 0; width: 90%; margin: 0 auto; }
.dating-detail__info li {
  height: 44px;
  line-height: 44px;
  border-bottom: 1px dashed var(--c-divider);
  font-size: var(--font-base);
  color: var(--c-text-1);
}
.dating-detail__info li span { font-weight: bold; margin-right: var(--space-sm); color: var(--c-text-1); }
.dating-detail__pick {
  border-top: 2px dashed var(--c-divider);
  padding-top: var(--space-lg);
  margin-top: var(--space-lg);
  text-align: center;
}
.dating-textarea {
  width: 100%;
  padding: var(--space-md);
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-sm);
  font-size: var(--font-base);
  min-height: 80px;
  box-sizing: border-box;
  margin-bottom: var(--space-md);
  color: var(--c-text-1);
}
.dating-textarea::placeholder { color: var(--c-text-3); }
.circle-btn {
  padding: 10px 28px;
  background: var(--c-dating);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--font-lg);
  cursor: pointer;
  transition: opacity 0.2s;
}
.circle-btn:active { opacity: 0.85; }
.circle-btn:disabled { opacity: 0.6; }

.dating-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0,0,0,0.7);
  color: #fff;
  padding: var(--space-lg) var(--space-xl);
  border-radius: var(--radius-sm);
  z-index: 1003;
  animation: community-fade-in 0.2s ease;
}
.dating-toast__content { margin: 0; font-size: var(--font-base); }
</style>
