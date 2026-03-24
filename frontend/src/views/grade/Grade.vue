<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGrade, updateGradeCache } from '@/api/grade'
import { useToast } from '@/composables/useToast'
import AppCard from '@/components/ui/AppCard.vue'
import AppSkeleton from '@/components/ui/AppSkeleton.vue'

const router = useRouter()
const { success: toastSuccess, loading: showLoading, hideLoading } = useToast()

const activeYear = ref(0)
const loading = ref(false)
const gradeResult = ref(null)
const showActionSheet = ref(false)

const yearTabs = [
  { value: 0, label: '大一' },
  { value: 1, label: '大二' },
  { value: 2, label: '大三' },
  { value: 3, label: '大四' }
]

async function fetchGrade() {
  loading.value = true
  gradeResult.value = null
  try {
    const res = await getGrade(activeYear.value)
    if (res && res.success && res.data != null) {
      gradeResult.value = res.data
    }
  } catch (e) {
    // 报错 Toast 由 request.js 全局拦截器处理
  } finally {
    loading.value = false
  }
}

function switchYear(year) {
  if (activeYear.value === year) return
  activeYear.value = year
  fetchGrade()
}

function goBack() {
  router.back()
}

/** 显示更多菜单（与旧版 grade.js showOptionMenu 一致：管理缓存配置、更新缓存数据 + 取消） */
function showOptionMenu() {
  showActionSheet.value = true
}

function closeActionSheet() {
  showActionSheet.value = false
}

/** 管理缓存配置：跳转到隐私设置页 */
function onManageCache() {
  closeActionSheet()
  router.push('/user/privacy-setting')
}

/** 更新缓存数据：调用后端 /api/grade/update，成功后静默刷新当前学年成绩 */
async function handleUpdateCache() {
  closeActionSheet()
  showLoading('正在同步教务系统...')
  try {
    const res = await updateGradeCache()
    if (res && res.success) {
      hideLoading()
      toastSuccess('更新成功')
      await fetchGrade()
    }
  } catch (e) {
    // 错误文案（含测试账号受限）由全局拦截器统一提示
  } finally {
    hideLoading()
  }
}

function scoreColorClass(score) {
  const num = parseFloat(score)
  if (isNaN(num)) return 'text-[var(--c-text-1)]'
  if (num < 60) return 'text-[var(--c-danger)]'
  return 'text-[var(--c-text-1)]'
}

onMounted(() => {
  fetchGrade()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Header bar -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">← 返回</button>
      <span class="flex-1 text-center text-sm font-bold">成绩查询</span>
      <button @click="showOptionMenu" class="text-[var(--c-primary)] text-sm font-medium w-10 text-right">更多</button>
    </div>

    <!-- Year/term selector pills -->
    <div class="flex gap-2 px-5 py-4 overflow-x-auto no-scrollbar">
      <button
        v-for="tab in yearTabs"
        :key="tab.value"
        @click="switchYear(tab.value)"
        :class="[
          'shrink-0 rounded-full px-4 py-1.5 text-sm font-medium transition-all whitespace-nowrap',
          activeYear === tab.value
            ? 'bg-[var(--c-primary)] text-white'
            : 'bg-[var(--c-surface)] text-[var(--c-text-2)] border border-[var(--c-border)]'
        ]"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="px-5 space-y-4">
      <AppCard>
        <div class="px-5 py-4 space-y-3">
          <AppSkeleton class="h-5 w-24 mx-auto" />
          <AppSkeleton class="h-4 w-40 mx-auto" />
          <div class="space-y-2 pt-2">
            <AppSkeleton class="h-8 w-full" />
            <AppSkeleton class="h-8 w-full" />
            <AppSkeleton class="h-8 w-full" />
            <AppSkeleton class="h-8 w-full" />
          </div>
        </div>
      </AppCard>
      <AppCard>
        <div class="px-5 py-4 space-y-3">
          <AppSkeleton class="h-5 w-24 mx-auto" />
          <AppSkeleton class="h-4 w-40 mx-auto" />
          <div class="space-y-2 pt-2">
            <AppSkeleton class="h-8 w-full" />
            <AppSkeleton class="h-8 w-full" />
            <AppSkeleton class="h-8 w-full" />
          </div>
        </div>
      </AppCard>
    </div>

    <!-- Grade data -->
    <template v-else-if="gradeResult">
      <div class="px-5 space-y-4 pb-8">
        <!-- 第一学期 -->
        <AppCard>
          <template #header>
            <div class="flex items-center justify-between w-full">
              <span class="text-sm font-semibold text-[var(--c-text-1)]">第一学期</span>
              <span class="text-xs text-[var(--c-text-3)]">
                GPA: {{ gradeResult.firstTermGPA != null ? gradeResult.firstTermGPA : '—' }}
              </span>
            </div>
          </template>

          <template v-if="(gradeResult.firstTermGradeList || []).length > 0">
            <!-- Table header -->
            <div class="grid grid-cols-[1fr_56px_64px] text-[11px] font-semibold uppercase tracking-wide text-[var(--c-text-3)] bg-[var(--c-bg)] px-5 py-2.5">
              <span>课程</span>
              <span class="text-center">学分</span>
              <span class="text-right">成绩</span>
            </div>
            <!-- Table rows -->
            <div
              v-for="(item, index) in gradeResult.firstTermGradeList"
              :key="'1-' + index"
              class="grid grid-cols-[1fr_56px_64px] items-center px-5 py-3 text-sm border-b border-[var(--c-border-light)] last:border-b-0"
            >
              <div>
                <span class="font-semibold text-[var(--c-text-1)]">{{ item.gradeName }}</span>
                <span
                  v-if="item.gradeType"
                  class="ml-1.5 inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium bg-[var(--c-border-light)] text-[var(--c-text-2)]"
                >{{ item.gradeType }}</span>
              </div>
              <span class="text-center text-xs text-[var(--c-text-3)]">{{ item.gradeCredit }}</span>
              <span
                :class="['text-right font-semibold text-base', scoreColorClass(item.gradeScore)]"
                style="font-family: var(--font-mono, ui-monospace, monospace)"
              >{{ item.gradeScore }}</span>
            </div>
          </template>
          <div v-else class="px-5 py-8 text-center text-sm text-[var(--c-text-3)]">暂无成绩数据</div>
        </AppCard>

        <!-- 第二学期 -->
        <AppCard>
          <template #header>
            <div class="flex items-center justify-between w-full">
              <span class="text-sm font-semibold text-[var(--c-text-1)]">第二学期</span>
              <span class="text-xs text-[var(--c-text-3)]">
                GPA: {{ gradeResult.secondTermGPA != null ? gradeResult.secondTermGPA : '—' }}
              </span>
            </div>
          </template>

          <template v-if="(gradeResult.secondTermGradeList || []).length > 0">
            <!-- Table header -->
            <div class="grid grid-cols-[1fr_56px_64px] text-[11px] font-semibold uppercase tracking-wide text-[var(--c-text-3)] bg-[var(--c-bg)] px-5 py-2.5">
              <span>课程</span>
              <span class="text-center">学分</span>
              <span class="text-right">成绩</span>
            </div>
            <!-- Table rows -->
            <div
              v-for="(item, index) in gradeResult.secondTermGradeList"
              :key="'2-' + index"
              class="grid grid-cols-[1fr_56px_64px] items-center px-5 py-3 text-sm border-b border-[var(--c-border-light)] last:border-b-0"
            >
              <div>
                <span class="font-semibold text-[var(--c-text-1)]">{{ item.gradeName }}</span>
                <span
                  v-if="item.gradeType"
                  class="ml-1.5 inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium bg-[var(--c-border-light)] text-[var(--c-text-2)]"
                >{{ item.gradeType }}</span>
              </div>
              <span class="text-center text-xs text-[var(--c-text-3)]">{{ item.gradeCredit }}</span>
              <span
                :class="['text-right font-semibold text-base', scoreColorClass(item.gradeScore)]"
                style="font-family: var(--font-mono, ui-monospace, monospace)"
              >{{ item.gradeScore }}</span>
            </div>
          </template>
          <div v-else class="px-5 py-8 text-center text-sm text-[var(--c-text-3)]">暂无成绩数据</div>
        </AppCard>
      </div>
    </template>

    <!-- Empty state (not loading, no data) -->
    <div v-else class="flex flex-col items-center justify-center py-24 text-[var(--c-text-3)]">
      <svg class="w-16 h-16 mb-4 opacity-30" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <p class="text-sm">暂无成绩数据</p>
    </div>

    <!-- Action Sheet (bottom drawer) -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showActionSheet"
          class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm"
          @click="closeActionSheet"
        />
      </Transition>
      <Transition name="slide-up">
        <div
          v-if="showActionSheet"
          class="fixed bottom-0 left-0 right-0 z-50 pb-[env(safe-area-inset-bottom)] bg-[var(--c-surface)] rounded-t-2xl"
        >
          <div class="flex flex-col">
            <button
              @click="onManageCache"
              class="px-5 py-4 text-sm font-medium text-[var(--c-text-1)] text-center border-b border-[var(--c-border-light)] active:bg-[var(--c-bg)] transition"
            >管理缓存配置</button>
            <button
              @click="handleUpdateCache"
              class="px-5 py-4 text-sm font-medium text-[var(--c-text-1)] text-center border-b border-[var(--c-border-light)] active:bg-[var(--c-bg)] transition"
            >更新缓存数据</button>
          </div>
          <div class="mt-2 border-t border-[var(--c-border)]">
            <button
              @click="closeActionSheet"
              class="w-full px-5 py-4 text-sm font-medium text-[var(--c-danger)] text-center active:bg-[var(--c-bg)] transition"
            >取消</button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style>
/* Action sheet transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}

/* Hide scrollbar for pill container */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
