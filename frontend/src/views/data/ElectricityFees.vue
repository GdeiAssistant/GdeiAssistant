<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import request from '../../utils/request'

const router = useRouter()
const { error: showError, loading: showLoading, hideLoading } = useToast()

const form = ref({
  year: new Date().getFullYear(),
  name: '',
  number: ''
})

const isLoading = ref(false)
const isQueried = ref(false)
const result = ref({})

// 生成年份选项（2016-当前年份）
const years = computed(() => {
  const currentYear = new Date().getFullYear()
  const yearList = []
  for (let i = 2016; i <= currentYear; i++) {
    yearList.push(i)
  }
  return yearList.reverse()
})

const submitQuery = () => {
  console.log('submitQuery 被调用', form.value)

  // 依次检查所有必填字段，显示具体错误
  if (!form.value.year) {
    showError('请输入年份')
    return
  }

  const name = String(form.value.name || '').trim()
  const number = String(form.value.number || '').trim()

  if (!name) {
    showError('请输入姓名')
    return
  }

  if (!number) {
    showError('请输入学号')
    return
  }

  // 学号校验（11位数字）
  if (!/^\d{11}$/.test(number)) {
    showError('请输入正确的学号（11位数字）')
    return
  }

  // 年份校验
  const year = Number(form.value.year)
  if (year < 2016 || year > 2050) {
    showError('请选择正确的年份')
    return
  }

  // 开始查询
  console.log('开始发送请求', { name, number, year })
  isLoading.value = true
  showLoading('正在查询')

  request
    .post('/data/electricfees', {
      name: name,
      number: number,
      year: year
    })
    .then((res) => {
      console.log('请求成功', res)
      isLoading.value = false
      hideLoading()
      if (res && res.success && res.data) {
        result.value = res.data
        isQueried.value = true
      } else {
        showError(res?.message || '查询失败')
      }
    })
    .catch((err) => {
      console.error('请求失败', err)
      isLoading.value = false
      hideLoading()
      // 错误由 request.js 全局拦截器统一提示
    })
}

const resetQuery = () => {
  isQueried.value = false
  form.value = {
    year: new Date().getFullYear(),
    name: '',
    number: ''
  }
  result.value = {}
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
      <span class="flex-1 text-center text-sm font-bold">电费查询</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Query form -->
      <div v-if="!isQueried" class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <form class="space-y-4" @submit.prevent="submitQuery">
          <!-- 年份 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">年份</label>
            <select
              v-model="form.year"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)] appearance-none"
            >
              <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
            </select>
          </div>

          <!-- 姓名 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">姓名</label>
            <input
              v-model="form.name"
              type="text"
              maxlength="10"
              placeholder="请输入你的姓名"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            />
          </div>

          <!-- 学号 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">学号</label>
            <input
              v-model="form.number"
              type="text"
              maxlength="11"
              placeholder="请输入你的学号"
              inputmode="numeric"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            />
          </div>
        </form>

        <button
          type="button"
          class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          @click="submitQuery"
        >查询</button>
      </div>

      <!-- Query result -->
      <div v-if="isQueried">
        <h3 class="text-xs font-semibold text-[var(--c-text-2)] uppercase tracking-wide mb-2">电费信息</h3>

        <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
          <div class="divide-y divide-[var(--c-border-light)]">
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">年份</span>
              <span class="text-sm font-semibold">{{ result.year }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">宿舍</span>
              <span class="text-sm font-semibold">{{ result.buildingNumber }}{{ result.roomNumber }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">入住人数</span>
              <span class="text-sm font-semibold">{{ result.peopleNumber }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">用电数额</span>
              <span class="text-sm font-semibold">{{ result.usedElectricAmount }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">免费电额</span>
              <span class="text-sm font-semibold">{{ result.freeElectricAmount }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">计费电数</span>
              <span class="text-sm font-semibold">{{ result.feeBasedElectricAmount }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">电价</span>
              <span class="text-sm font-semibold">{{ result.electricPrice }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">总电费</span>
              <span class="text-sm font-semibold">{{ result.totalElectricBill }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">平均电费</span>
              <span class="text-sm font-semibold">{{ result.averageElectricBill }}</span>
            </div>
          </div>
        </div>

        <button
          type="button"
          class="w-full mt-5 bg-[var(--c-surface)] text-[var(--c-text)] border border-[var(--c-border)] rounded-lg py-2.5 font-semibold transition-opacity hover:opacity-80"
          @click="resetQuery"
        >重新查询</button>
      </div>
    </div>
  </div>
</template>
