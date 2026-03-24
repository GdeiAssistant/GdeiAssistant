<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const { success: toastSuccess, loading: toastLoading, hideLoading } = useToast()
const formData = ref({
  nickname: '',
  realName: '',
  myGender: '',
  receiverName: '',
  receiverGender: '',
  content: ''
})

const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function submit() {
  const nickname = (formData.value.nickname || '').trim()
  const realname = (formData.value.realName || '').trim()
  const receiverName = (formData.value.receiverName || '').trim()
  const content = (formData.value.content || '').trim()

  if (!nickname) { showDialog('请输入昵称'); return }
  if (nickname.length > 10) { showDialog('昵称不能超过 10 个字'); return }
  if (realname.length > 10) { showDialog('真实姓名不能超过 10 个字'); return }
  if (!formData.value.myGender) { showDialog('请选择你的性别'); return }
  if (!receiverName) { showDialog('请输入TA的名字'); return }
  if (receiverName.length > 10) { showDialog('TA的名字不能超过 10 个字'); return }
  if (!formData.value.receiverGender) { showDialog('请选择TA的性别'); return }
  if (!content) { showDialog('请填写表白内容'); return }
  if (content.length > 250) { showDialog('表白内容不能超过 250 个字'); return }

  const mapGender = (v) => { if (v === 'male') return 0; if (v === 'female') return 1; return 2 }
  const payload = {
    nickname,
    realname,
    selfGender: mapGender(formData.value.myGender),
    name: receiverName,
    personGender: mapGender(formData.value.receiverGender),
    content
  }
  submitting.value = true
  toastLoading('正在发布...')
  request.post('/express', payload)
    .then(() => {
      hideLoading()
      toastSuccess('发布成功')
      setTimeout(() => router.push('/express/home'), 1500)
    })
    .catch(() => {
      submitting.value = false
      hideLoading()
    })
}
</script>

<template>
  <div class="bg-[var(--c-bg)] pb-20">
    <CommunityHeader title="发布表白" moduleColor="#f43f5e" backTo="/express/home" />

    <!-- 浅粉色粗体标题 -->
    <h2 class="text-center text-[22px] font-bold text-[#ffb3ba] mx-4 mt-4 mb-5 leading-tight">广东第二师范学院表白墙</h2>

    <!-- 手账风表单 -->
    <div>
      <!-- 第一个 form-section：你的信息 -->
      <div class="border-2 border-dashed border-[#81d4fa] rounded-lg pt-6 px-4 pb-4 mx-4 mt-6 mb-4 relative bg-[var(--c-surface)] shadow-sm">
        <span class="absolute -top-3 -left-0.5 bg-[#4fc3f7] text-white px-2.5 py-1 rounded-sm text-sm font-medium">你的信息</span>
        <div>
          <div class="flex items-start py-3 border-b border-[var(--c-border)]">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">昵称</label>
            <div class="flex-1">
              <input
                class="w-full h-7 text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent"
                type="text"
                placeholder="请输入昵称"
                v-model="formData.nickname"
              />
            </div>
          </div>
          <div class="flex items-start py-3 border-b border-[var(--c-border)]">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">真名</label>
            <div class="flex-1">
              <input
                class="w-full h-7 text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent"
                type="text"
                placeholder="请输入真名"
                v-model="formData.realName"
              />
              <div class="text-xs text-[var(--c-text-3)] mt-1 leading-snug">注：真实姓名可选填，默认保密不显示！填写即可参加紧张刺激的猜名字游戏！</div>
            </div>
          </div>
          <div class="flex items-start py-3">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">性别</label>
            <div class="flex-1">
              <select class="w-full text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent appearance-none" v-model="formData.myGender">
                <option value="">请选择</option>
                <option value="male">男</option>
                <option value="female">女</option>
                <option value="secret">其他或保密</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 第二个 form-section：TA的信息 -->
      <div class="border-2 border-dashed border-[#81d4fa] rounded-lg pt-6 px-4 pb-4 mx-4 mt-6 mb-4 relative bg-[var(--c-surface)] shadow-sm">
        <span class="absolute -top-3 -left-0.5 bg-[#4fc3f7] text-white px-2.5 py-1 rounded-sm text-sm font-medium">TA的信息</span>
        <div>
          <div class="flex items-start py-3 border-b border-[var(--c-border)]">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">名字</label>
            <div class="flex-1">
              <input
                class="w-full h-7 text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent"
                type="text"
                placeholder="请输入TA的名字"
                v-model="formData.receiverName"
              />
            </div>
          </div>
          <div class="flex items-start py-3">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">性别</label>
            <div class="flex-1">
              <select class="w-full text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent appearance-none" v-model="formData.receiverGender">
                <option value="">请选择</option>
                <option value="male">男</option>
                <option value="female">女</option>
                <option value="secret">其他或保密</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 表白内容 -->
      <div class="border-2 border-dashed border-[#81d4fa] rounded-lg pt-6 px-4 pb-4 mx-4 mt-6 mb-4 relative bg-[var(--c-surface)] shadow-sm">
        <span class="absolute -top-3 -left-0.5 bg-[#4fc3f7] text-white px-2.5 py-1 rounded-sm text-sm font-medium">表白内容</span>
        <div>
          <div class="py-3">
            <textarea
              class="w-full min-h-[80px] text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent py-1 resize-y"
              placeholder="写下你想对TA说的话..."
              rows="4"
              v-model="formData.content"
            ></textarea>
          </div>
        </div>
      </div>

      <div class="px-4 pt-5 pb-10">
        <button
          type="button"
          class="w-full h-11 flex justify-center items-center bg-[#f43f5e] text-white rounded-full text-base font-medium border-none cursor-pointer transition-opacity duration-200 active:opacity-85 disabled:opacity-60 disabled:cursor-not-allowed"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? '提交中...' : '发布表白' }}
        </button>
      </div>
    </div>

    <!-- 提示对话框 -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">提示</div>
        <div class="px-5 pb-4 text-sm text-[var(--c-text-1)] text-center">{{ dialogMessage }}</div>
        <div class="flex border-t border-[var(--c-border)]">
          <button class="flex-1 py-3 text-center text-sm text-[#f43f5e] font-semibold bg-transparent border-none cursor-pointer" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>
