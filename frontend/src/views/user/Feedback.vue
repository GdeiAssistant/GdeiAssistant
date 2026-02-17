<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const activeTab = ref('faq')

// 常见问题列表（根据旧版 faq.jsp 精选 & 总结）
const faqList = ref([
  {
    title: '登录应用的用户名和密码是什么？',
    content:
      '用户名和密码与校园网上网账号、学校教务系统账号保持一致。如果不清楚自己的账号信息，可以在“关于应用-校园网络账号说明”中查看详细指引。'
  },
  {
    title: '应用账号是否支持修改密码？',
    content:
      '请前往学校统一身份认证系统修改密码。修改完成后，重新登录广东二师助手即可同步最新密码，本应用不会单独保存你的登录密码。'
  },
  {
    title: '为什么成绩/课表查询会提示系统异常？',
    content:
      '教务查询依赖学校官网和教务系统的稳定性。当源站维护或更新时，可能出现查询异常或暂时不可用的情况，一般会在24小时内恢复。如果长时间异常，可以通过本页的“意见反馈”提交故障工单。'
  },
  {
    title: '什么是教务数据缓存？开启后是否安全？',
    content:
      '教务数据缓存用于在获得你授权的前提下，定期从学校教务系统同步成绩和课表，以提升查询速度和稳定性。数据仅用于教务展示，不会对外泄露，你可以在隐私设置中随时关闭该功能。'
  },
  {
    title: '如何注销账户？',
    content:
      '你可以在个人中心-删除账号中发起注销申请，系统会校验密码并判断是否满足注销条件。如果无法自助注销，可以在此处提交工单或发送邮件至 support@gdeiassistant.cn 联系管理员协助处理。'
  }
])

const openedFaqIndex = ref(0)

function toggleFaq(index) {
  openedFaqIndex.value = openedFaqIndex.value === index ? null : index
}

// 反馈表单
const feedbackType = ref('应用功能异常')
const feedbackTypes = [
  '闪退、卡顿或界面错位',
  '用户登录和密码设置',
  '个人资料和隐私设置',
  '实名认证和手机绑定',
  '账号绑定、管理和注销',
  '应用功能异常',
  '其它'
]

const content = ref('')
const contact = ref('')
const maxLength = 500

const screenshots = ref([])
const screenshotPreviews = ref([])
const maxScreenshots = 3

const contentLength = computed(() => content.value.length)

function handleSelectType(type) {
  feedbackType.value = type
}

function handleContentInput(e) {
  const target = e.target
  if (target.value.length > maxLength) {
    content.value = target.value.slice(0, maxLength)
  } else {
    content.value = target.value
  }
}

function handleScreenshotChange(e) {
  const input = e.target
  const files = input.files
  if (!files || !files.length) return

  const remain = maxScreenshots - screenshots.value.length
  const list = Array.from(files).slice(0, remain)

  list.forEach((file) => {
    if (!file.type.startsWith('image/')) return
    screenshots.value.push(file)
    screenshotPreviews.value.push(URL.createObjectURL(file))
  })

  // 重置 input，方便重复选择相同文件
  input.value = ''
}

function removeScreenshot(index) {
  screenshots.value.splice(index, 1)
  const url = screenshotPreviews.value.splice(index, 1)[0]
  if (url) URL.revokeObjectURL(url)
}

function showToast(message) {
  const toast = document.createElement('div')
  toast.style.cssText =
    'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.75);color:#fff;padding:12px 22px;border-radius:6px;z-index:9999;font-size:14px;max-width:80%;text-align:center;'
  toast.textContent = message
  document.body.appendChild(toast)
  setTimeout(() => {
    if (toast.parentNode) document.body.removeChild(toast)
  }, 2000)
}

const submitting = ref(false)

async function handleSubmit() {
  const trimmedContent = content.value.trim()
  if (!trimmedContent) {
    showToast('请填写反馈内容')
    return
  }

  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('type', feedbackType.value)
    formData.append('content', trimmedContent)
    if (contact.value && contact.value.trim()) {
      formData.append('contact', contact.value.trim())
    }
    screenshots.value.forEach((file, index) => {
      formData.append(`screenshot${index + 1}`, file)
    })

    await request.post('/user/feedback', formData)
    showToast('感谢您的反馈！')

    // 清空表单
    content.value = ''
    contact.value = ''
    screenshots.value = []
    screenshotPreviews.value.forEach((url) => URL.revokeObjectURL(url))
    screenshotPreviews.value = []
  } catch (e) {
    // 错误已由全局拦截器提示
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="feedback-page">
    <!-- 统一头部 -->
    <div class="feedback-header unified-header">
      <span class="feedback-header__back" @click="router.back()">返回</span>
      <h1 class="feedback-header__title">帮助与反馈</h1>
      <span class="feedback-header__placeholder"></span>
    </div>

    <!-- 顶部 Tab -->
    <div class="feedback-tabs">
      <div
        :class="['feedback-tab', { active: activeTab === 'faq' }]"
        @click="activeTab = 'faq'"
      >
        常见问题
      </div>
      <div
        :class="['feedback-tab', { active: activeTab === 'form' }]"
        @click="activeTab = 'form'"
      >
        意见反馈
      </div>
    </div>

    <div class="feedback-content">
      <!-- 常见问题 -->
      <section v-if="activeTab === 'faq'" class="feedback-section">
        <div class="faq-card" v-for="(item, index) in faqList" :key="item.title">
          <button
            type="button"
            class="faq-header"
            @click="toggleFaq(index)"
          >
            <span class="faq-title">{{ item.title }}</span>
            <span
              class="faq-arrow"
              :class="{ open: openedFaqIndex === index }"
            >
              ▾
            </span>
          </button>
          <div
            v-if="openedFaqIndex === index"
            class="faq-body"
          >
            <p>{{ item.content }}</p>
          </div>
        </div>

        <p class="faq-tip">
          以上未能解决您的问题？请切换到
          <span class="faq-tip__strong">「意见反馈」</span>
          告诉我们您的疑问和建议。
        </p>
      </section>

      <!-- 意见反馈表单 -->
      <section v-else class="feedback-section">
        <div class="card">
          <h2 class="card-title">选择反馈类型</h2>
          <div class="tag-group">
            <button
              v-for="type in feedbackTypes"
              :key="type"
              type="button"
              class="tag"
              :class="{ active: feedbackType === type }"
              @click="handleSelectType(type)"
            >
              {{ type }}
            </button>
          </div>
        </div>

        <div class="card">
          <h2 class="card-title">反馈内容</h2>
          <div class="textarea-wrap">
            <textarea
              class="textarea"
              :placeholder="'请详细描述您遇到的问题或建议...'"
              :value="content"
              @input="handleContentInput"
              rows="5"
            ></textarea>
            <div
              class="textarea-counter"
              :class="{ danger: contentLength > maxLength }"
            >
              {{ contentLength }}/{{ maxLength }}
            </div>
          </div>
        </div>

        <div class="card">
          <h2 class="card-title">联系方式（选填）</h2>
          <input
            class="input"
            type="text"
            v-model="contact"
            placeholder="请留下您的QQ/微信或手机号，方便我们联系您（选填）"
          />
        </div>

        <div class="card">
          <h2 class="card-title">
            截图上传
            <span class="card-subtitle">（可选，最多 {{ maxScreenshots }} 张）</span>
          </h2>
          <div class="upload-grid">
            <div
              v-for="(url, index) in screenshotPreviews"
              :key="url"
              class="upload-item"
            >
              <img :src="url" alt="截图预览" />
              <button
                type="button"
                class="upload-remove"
                @click="removeScreenshot(index)"
              >
                ×
              </button>
            </div>
            <label
              v-if="screenshots.length < maxScreenshots"
              class="upload-add"
            >
              <span class="upload-plus">＋</span>
              <span class="upload-text">添加图片</span>
              <input
                type="file"
                accept="image/*"
                multiple
                class="upload-input"
                @change="handleScreenshotChange"
              />
            </label>
          </div>
        </div>

        <div class="submit-wrap">
          <button
            type="button"
            class="submit-btn"
            :disabled="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '提交反馈' }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.feedback-page {
  background: #f5f5f7;
  min-height: 100vh;
  padding-bottom: 24px;
}

.feedback-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}
.feedback-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.feedback-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.feedback-header__placeholder {
  min-width: 48px;
}

.feedback-tabs {
  display: flex;
  background: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}
.feedback-tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 15px;
  color: #777;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}
.feedback-tab.active {
  color: #10b981;
  font-weight: 600;
}
.feedback-tab.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 40%;
  height: 3px;
  border-radius: 999px;
  background: #10b981;
}

.feedback-content {
  padding: 16px;
}

.feedback-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
  overflow: hidden;
}
.faq-header {
  width: 100%;
  padding: 14px 16px;
  background: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}
.faq-title {
  font-size: 15px;
  color: #111827;
  text-align: left;
}
.faq-arrow {
  font-size: 14px;
  color: #9ca3af;
  transition: transform 0.2s ease;
}
.faq-arrow.open {
  transform: rotate(180deg);
}
.faq-body {
  padding: 12px 16px 14px;
  background: #f9fafb;
  border-top: 1px solid #f3f4f6;
}
.faq-body p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #4b5563;
}

.faq-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
  text-align: center;
}
.faq-tip__strong {
  color: #10b981;
  font-weight: 600;
}

.card {
  background: #ffffff;
  border-radius: 12px;
  padding: 14px 14px 16px;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 10px;
}
.card-subtitle {
  margin-left: 4px;
  font-size: 12px;
  color: #9ca3af;
  font-weight: 400;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag {
  border-radius: 999px;
  padding: 6px 14px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  font-size: 13px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s ease;
}
.tag.active {
  border-color: #10b981;
  background: #ecfdf5;
  color: #047857;
  font-weight: 500;
}

.textarea-wrap {
  position: relative;
}
.textarea {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  padding: 12px;
  font-size: 14px;
  color: #111827;
  resize: none;
  min-height: 120px;
  box-sizing: border-box;
}
.textarea:focus {
  outline: none;
  border-color: #10b981;
  background: #ffffff;
}
.textarea-counter {
  position: absolute;
  right: 10px;
  bottom: 8px;
  font-size: 12px;
  color: #9ca3af;
}
.textarea-counter.danger {
  color: #f97316;
}

.input {
  width: 100%;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  padding: 10px 14px;
  font-size: 14px;
  color: #111827;
  box-sizing: border-box;
}
.input::placeholder {
  color: #9ca3af;
}
.input:focus {
  outline: none;
  border-color: #10b981;
  background: #ffffff;
}

.upload-grid {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.upload-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 10px;
  overflow: hidden;
  background: #f3f4f6;
}
.upload-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.upload-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 999px;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
}
.upload-add {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  border: 1px dashed #d1d5db;
  background: #f9fafb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 12px;
  cursor: pointer;
}
.upload-plus {
  font-size: 22px;
  line-height: 1;
  margin-bottom: 2px;
}
.upload-input {
  display: none;
}

.submit-wrap {
  margin-top: 12px;
}
.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.35);
  cursor: pointer;
  transition: opacity 0.2s ease;
}
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

