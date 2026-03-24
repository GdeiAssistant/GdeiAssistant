<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { submitFeedback } from '@/api/feedback'
import { showErrorTopTips } from '@/utils/toast.js'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success: toastSuccess } = useToast()

const activeTab = ref('faq')

// 常见问题列表（根据旧版 faq.jsp 精选 & 总结）
const faqList = ref([
  {
    title: '登录应用的用户名和密码是什么？',
    content:
      '用户名和密码与校园网上网账号、学校教务系统账号保持一致。如果不清楚自己的账号信息，可以在"关于应用-校园网络账号说明"中查看详细指引。'
  },
  {
    title: '应用账号是否支持修改密码？',
    content:
      '请前往学校统一身份认证系统修改密码。修改完成后，重新登录广东二师助手即可同步最新密码，本应用不会单独保存你的登录密码。'
  },
  {
    title: '为什么成绩/课表查询会提示系统异常？',
    content:
      '教务查询依赖学校官网和教务系统的稳定性。当源站维护或更新时，可能出现查询异常或暂时不可用的情况，一般会在24小时内恢复。如果长时间异常，可以通过本页的"意见反馈"提交故障工单。'
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

const submitting = ref(false)

async function handleSubmit() {
  const trimmedContent = content.value.trim()
  if (!trimmedContent) {
    showErrorTopTips('请填写反馈内容')
    return
  }

  submitting.value = true
  try {
    await submitFeedback({
      content: trimmedContent,
      contact: contact.value?.trim() || undefined,
      type: feedbackType.value || undefined
    })
    toastSuccess('提交成功')
    setTimeout(() => {
      content.value = ''
      contact.value = ''
      screenshots.value = []
      screenshotPreviews.value.forEach((url) => URL.revokeObjectURL(url))
      screenshotPreviews.value = []
      router.back()
    }, 1500)
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-6">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">返回</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">帮助与反馈</h1>
      <div class="w-15"></div>
    </div>

    <!-- Tabs -->
    <div class="flex bg-white border-b border-gray-200">
      <button
        type="button"
        class="flex-1 text-center py-3 text-[15px] cursor-pointer relative bg-transparent transition-colors"
        :class="activeTab === 'faq' ? 'text-emerald-500 font-semibold after:content-[\'\'] after:absolute after:left-1/2 after:-translate-x-1/2 after:bottom-0 after:w-2/5 after:h-[3px] after:rounded-full after:bg-emerald-500' : 'text-gray-400'"
        @click="activeTab = 'faq'"
      >常见问题</button>
      <button
        type="button"
        class="flex-1 text-center py-3 text-[15px] cursor-pointer relative bg-transparent transition-colors"
        :class="activeTab === 'form' ? 'text-emerald-500 font-semibold after:content-[\'\'] after:absolute after:left-1/2 after:-translate-x-1/2 after:bottom-0 after:w-2/5 after:h-[3px] after:rounded-full after:bg-emerald-500' : 'text-gray-400'"
        @click="activeTab = 'form'"
      >意见反馈</button>
    </div>

    <div class="max-w-lg mx-auto px-4 py-4">
      <!-- FAQ section -->
      <section v-if="activeTab === 'faq'" class="space-y-3">
        <div
          v-for="(item, index) in faqList"
          :key="item.title"
          class="bg-white rounded-xl shadow-sm overflow-hidden"
        >
          <button
            type="button"
            class="w-full px-4 py-3.5 bg-white flex items-center justify-between cursor-pointer border-none"
            @click="toggleFaq(index)"
          >
            <span class="text-[15px] text-gray-900 text-left">{{ item.title }}</span>
            <span
              class="text-sm text-gray-400 transition-transform duration-200"
              :class="{ 'rotate-180': openedFaqIndex === index }"
            >&#9662;</span>
          </button>
          <div
            v-if="openedFaqIndex === index"
            class="px-4 py-3 bg-gray-50 border-t border-gray-100"
          >
            <p class="m-0 text-sm leading-relaxed text-gray-600">{{ item.content }}</p>
          </div>
        </div>

        <p class="mt-2 text-[13px] text-gray-400 text-center">
          以上未能解决您的问题？请切换到
          <span class="text-emerald-500 font-semibold">「意见反馈」</span>
          告诉我们您的疑问和建议。
        </p>
      </section>

      <!-- Feedback form section -->
      <section v-else class="space-y-3">
        <!-- Type selection -->
        <div class="bg-white rounded-xl shadow-sm p-3.5">
          <h2 class="text-[15px] font-semibold text-gray-900 mb-2.5">选择反馈类型</h2>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="type in feedbackTypes"
              :key="type"
              type="button"
              class="rounded-full px-3.5 py-1.5 text-[13px] border cursor-pointer transition-colors"
              :class="feedbackType === type
                ? 'border-emerald-500 bg-emerald-50 text-emerald-700 font-medium'
                : 'border-gray-200 bg-gray-50 text-gray-600'"
              @click="handleSelectType(type)"
            >{{ type }}</button>
          </div>
        </div>

        <!-- Content -->
        <div class="bg-white rounded-xl shadow-sm p-3.5">
          <h2 class="text-[15px] font-semibold text-gray-900 mb-2.5">反馈内容</h2>
          <div class="relative">
            <textarea
              placeholder="请详细描述您遇到的问题或建议..."
              :value="content"
              @input="handleContentInput"
              rows="5"
              class="w-full rounded-xl border border-gray-200 bg-gray-50 p-3 text-sm text-gray-900 resize-none min-h-[120px] box-border focus:outline-none focus:border-emerald-500 focus:bg-white"
            ></textarea>
            <div
              class="absolute right-2.5 bottom-2 text-xs"
              :class="contentLength > maxLength ? 'text-orange-500' : 'text-gray-400'"
            >{{ contentLength }}/{{ maxLength }}</div>
          </div>
        </div>

        <!-- Contact -->
        <div class="bg-white rounded-xl shadow-sm p-3.5">
          <h2 class="text-[15px] font-semibold text-gray-900 mb-2.5">联系方式（选填）</h2>
          <input
            type="text"
            v-model="contact"
            placeholder="请留下您的QQ/微信或手机号，方便我们联系您（选填）"
            class="w-full rounded-full border border-gray-200 py-2.5 px-3.5 text-sm text-gray-900 box-border placeholder-gray-400 focus:outline-none focus:border-emerald-500 focus:bg-white"
          />
        </div>

        <!-- Screenshots -->
        <div class="bg-white rounded-xl shadow-sm p-3.5">
          <h2 class="text-[15px] font-semibold text-gray-900 mb-2.5">
            截图上传
            <span class="ml-1 text-xs text-gray-400 font-normal">（可选，最多 {{ maxScreenshots }} 张）</span>
          </h2>
          <div class="flex gap-2.5 flex-wrap">
            <div
              v-for="(url, index) in screenshotPreviews"
              :key="url"
              class="relative w-20 h-20 rounded-lg overflow-hidden bg-gray-100"
            >
              <img :src="url" alt="截图预览" class="w-full h-full object-cover" />
              <button
                type="button"
                class="absolute top-1 right-1 w-5 h-5 rounded-full bg-black/50 text-white text-sm leading-none border-none cursor-pointer"
                @click="removeScreenshot(index)"
              >&times;</button>
            </div>
            <label
              v-if="screenshots.length < maxScreenshots"
              class="w-20 h-20 rounded-lg border border-dashed border-gray-300 bg-gray-50 flex flex-col items-center justify-center text-gray-400 text-xs cursor-pointer"
            >
              <span class="text-[22px] leading-none mb-0.5">+</span>
              <span>添加图片</span>
              <input
                type="file"
                accept="image/*"
                multiple
                class="hidden"
                @change="handleScreenshotChange"
              />
            </label>
          </div>
        </div>

        <!-- Submit -->
        <div class="mt-3">
          <button
            type="button"
            class="w-full h-[46px] rounded-full border-none bg-gradient-to-br from-emerald-500 to-emerald-600 text-white text-base font-semibold shadow-lg shadow-emerald-500/35 cursor-pointer transition-opacity disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="submitting"
            @click="handleSubmit"
          >{{ submitting ? '提交中...' : '提交反馈' }}</button>
        </div>
      </section>
    </div>
  </div>
</template>
