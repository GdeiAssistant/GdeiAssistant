<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import AppDialog from '@/components/ui/AppDialog.vue'
import {
  deleteCampusCredential,
  getCampusCredentialStatus,
  revokeCampusCredentialConsent,
  updateCampusQuickAuth
} from '@/api/campusCredential'

const router = useRouter()
const { success: toastSuccess } = useToast()

const loading = ref(false)
const submitting = ref(false)
const status = ref(null)
const statusSupported = ref(true)
const statusError = ref('')
const confirmAction = ref('')
const confirmOpen = ref(false)

const revokeDescription = '撤回授权将同时删除已保存的校园凭证，后续使用相关功能需要重新登录或重新授权。'
const deleteDescription = '删除后，课表、成绩、校园卡、图书馆等查询可能需要重新登录。已保存的校园凭证和快速认证状态将一并清除。'

const hasLiveStatus = computed(() => !!status.value && statusSupported.value)
const canEnableQuickAuth = computed(() => !!status.value?.hasActiveConsent && !!status.value?.hasSavedCredential)

const detailRows = computed(() => {
  if (!status.value) return []
  return [
    { label: '校园账号', value: status.value.maskedCampusAccount || '未提供' },
    { label: '授权时间', value: formatDate(status.value.consentedAt) },
    { label: '撤回时间', value: formatDate(status.value.revokedAt) }
  ]
})

function formatDate(value) {
  if (!value) return '暂无'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '暂无'
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function hasLoginToken() {
  try {
    return !!localStorage.getItem('token')
  } catch (_) {
    return false
  }
}

async function loadStatus() {
  if (!hasLoginToken()) {
    status.value = null
    statusSupported.value = false
    statusError.value = '请先登录后查看校园凭证管理状态。'
    return
  }

  loading.value = true
  statusError.value = ''
  try {
    status.value = await getCampusCredentialStatus()
    statusSupported.value = true
  } catch (_) {
    statusSupported.value = false
    statusError.value = '当前暂时无法获取校园凭证管理状态，请优先通过“帮助与反馈”申请处理。'
  } finally {
    loading.value = false
  }
}

function openConfirm(action) {
  confirmAction.value = action
  confirmOpen.value = true
}

function closeConfirm() {
  confirmOpen.value = false
  confirmAction.value = ''
}

async function performConfirmedAction() {
  if (!confirmAction.value) return
  submitting.value = true
  try {
    if (confirmAction.value === 'revoke') {
      status.value = await revokeCampusCredentialConsent()
      toastSuccess('校园账号凭证授权已撤回')
      forceRelogin()
      return
    }
    if (confirmAction.value === 'delete') {
      status.value = await deleteCampusCredential()
      toastSuccess('已删除保存的校园凭证')
      forceRelogin()
      return
    }
  } finally {
    submitting.value = false
    closeConfirm()
  }
}

async function toggleQuickAuth(enabled) {
  submitting.value = true
  try {
    status.value = await updateCampusQuickAuth(enabled)
    toastSuccess(enabled ? '快速认证已开启' : '快速认证已关闭')
  } finally {
    submitting.value = false
  }
}

function forceRelogin() {
  localStorage.removeItem('token')
  sessionStorage.clear()
  setTimeout(() => router.replace('/login'), 700)
}

onMounted(() => {
  loadStatus()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--color-surface)]">
    <div class="sticky top-0 left-0 right-0 h-[50px] bg-[var(--color-surface)] shadow-md flex items-center px-4 z-[1000]">
      <span class="text-[var(--color-primary)] text-[15px] cursor-pointer mr-4" @click="router.back()">返回</span>
      <h2 class="flex-1 text-center text-lg font-medium text-[var(--c-text-1)] m-0">校园账号与凭证管理</h2>
      <span class="w-10"></span>
    </div>

    <div class="max-w-2xl mx-auto px-4 py-6">
      <section class="bg-[var(--color-surface)] rounded-xl p-4 shadow-sm">
        <div class="flex flex-col gap-1">
          <h3 class="m-0 text-base font-semibold text-[var(--c-text-1)]">校园账号凭证与快速认证管理</h3>
          <p class="m-0 text-sm leading-6 text-[var(--c-text-2)]">查看授权状态、已保存凭证和快速认证；撤回授权或删除凭证后，相关校园查询可能需要重新登录。</p>
        </div>

        <div class="mt-4 border-t border-[var(--c-border-light)] pt-4">
          <div class="flex items-center justify-between gap-3">
            <p class="m-0 text-sm font-medium text-[var(--c-text-1)]">当前管理状态</p>
            <button
              type="button"
              class="shrink-0 rounded-full border border-[var(--c-border)] bg-transparent px-3 py-1 text-xs text-[var(--c-text-2)] cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="loading || submitting"
              @click="loadStatus"
            >
              刷新
            </button>
          </div>
          <p v-if="loading" class="mt-3 text-sm text-[var(--c-text-3)]">正在获取状态...</p>
          <template v-else-if="hasLiveStatus">
            <div class="mt-3 grid gap-2 sm:grid-cols-3">
              <div class="rounded-lg bg-[var(--c-bg)] px-3 py-3">
                <div class="text-xs text-[var(--c-text-3)]">授权状态</div>
                <div
                  class="mt-1 text-sm font-medium"
                  :class="status.hasActiveConsent ? 'text-emerald-600' : 'text-amber-700'"
                >
                  {{ status.hasActiveConsent ? '已授权' : '未授权' }}
                </div>
              </div>
              <div class="rounded-lg bg-[var(--c-bg)] px-3 py-3">
                <div class="text-xs text-[var(--c-text-3)]">已保存凭证</div>
                <div
                  class="mt-1 text-sm font-medium"
                  :class="status.hasSavedCredential ? 'text-emerald-600' : 'text-[var(--c-text-2)]'"
                >
                  {{ status.hasSavedCredential ? '是' : '否' }}
                </div>
              </div>
              <div class="rounded-lg bg-[var(--c-bg)] px-3 py-3">
                <div class="text-xs text-[var(--c-text-3)]">快速认证</div>
                <div
                  class="mt-1 text-sm font-medium"
                  :class="status.quickAuthEnabled ? 'text-emerald-600' : 'text-[var(--c-text-2)]'"
                >
                  {{ status.quickAuthEnabled ? '已开启' : '已关闭' }}
                </div>
              </div>
            </div>

            <dl class="mt-4 grid gap-2 text-sm">
              <div
                v-for="row in detailRows"
                :key="row.label"
                class="flex items-start justify-between gap-4 border-t border-[var(--c-border-light)] pt-2"
              >
                <dt class="text-[var(--c-text-3)]">{{ row.label }}</dt>
                <dd class="m-0 max-w-[60%] text-right text-[var(--c-text-1)] break-words">{{ row.value }}</dd>
              </div>
            </dl>

            <div class="mt-4 flex flex-col gap-2">
              <button
                v-if="status.quickAuthEnabled"
                type="button"
                class="w-full rounded-lg border border-[var(--c-border)] bg-transparent px-4 py-2.5 text-sm text-[var(--c-text-2)] cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="submitting"
                @click="toggleQuickAuth(false)"
              >
                关闭快速认证
              </button>
              <button
                v-else
                type="button"
                class="w-full rounded-lg border border-[var(--c-primary)] bg-[var(--c-primary)] px-4 py-2.5 text-sm text-white cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="submitting || !canEnableQuickAuth"
                @click="toggleQuickAuth(true)"
              >
                开启快速认证
              </button>
              <p
                v-if="!status.quickAuthEnabled && !canEnableQuickAuth"
                class="m-0 text-xs leading-5 text-[var(--c-text-3)]"
              >
                需要先存在有效授权记录，并保存可复用的校园凭证后，才可重新开启快速认证。
              </p>
            </div>

            <div class="mt-5 border-t border-[var(--c-border-light)] pt-4">
              <p class="m-0 text-xs font-medium text-[var(--c-text-3)]">危险操作</p>
              <div class="mt-3 grid gap-3 md:grid-cols-2">
                <button
                  type="button"
                  class="w-full rounded-lg border border-amber-200 bg-amber-50 px-4 py-2.5 text-sm text-amber-700 cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                  :disabled="submitting"
                  @click="openConfirm('revoke')"
                >
                  撤回授权
                </button>
                <button
                  type="button"
                  class="w-full rounded-lg border border-red-200 bg-red-50 px-4 py-2.5 text-sm text-red-600 cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                  :disabled="submitting"
                  @click="openConfirm('delete')"
                >
                  删除已保存凭证
                </button>
              </div>
            </div>
          </template>
          <template v-else>
            <p class="mt-3 text-sm text-[var(--c-text-3)]">{{ statusError }}</p>
          </template>
        </div>

        <div class="mt-4 grid gap-3 md:grid-cols-2">
            <button
              type="button"
              class="w-full rounded-lg border border-[var(--c-primary)] bg-[var(--c-primary)] px-4 py-2.5 text-sm text-white cursor-pointer"
              @click="router.push('/user/feedback')"
            >
              通过帮助与反馈申请处理
            </button>
            <button
              type="button"
              class="w-full rounded-lg border border-[var(--c-border)] bg-transparent px-4 py-2.5 text-sm text-[var(--c-text-2)] cursor-pointer"
              @click="router.push('/policy/privacy')"
            >
              查看隐私政策中的校园凭证说明
            </button>
          </div>
      </section>

      <section class="mt-4 bg-[var(--color-surface)] rounded-xl p-4 shadow-sm">
        <div class="text-sm leading-relaxed text-[var(--c-text-2)] [&_p]:my-3 [&_img]:max-w-full [&_img]:h-auto [&_img]:my-4">
          <p class="text-center font-medium text-[var(--c-text-1)]">广东二师助手校园网络账号说明</p>
          <p class="text-center">更新日期：2022年6月28日</p>
          <p class="indent-8">广东第二师范学院的同学们，欢迎使用广东第二师范学院校园助手系统。<span class="text-red-500">若你为新生或首次使用本系统，建议你先阅读本使用说明</span>。</p>
          <p class="indent-8">使用本系统，需要使用校园网账号的用户名与密码进行登录。<span class="text-red-500">值得注意的是，用户名并不是学号</span>。此外，<span class="text-red-500">新生一般需要一周的时间等待网络中心激活学院应用信息系统账号，若你无法登录系统，建议暂时耐心等待</span>。</p>
          <p class="indent-8">查询校园网账号信息的方式如下：</p>
          <p class="text-center">
            <img class="w-full h-auto" src="/img/about/application/account_1.png">
          </p>
          <p class="text-left indent-8">打开学校主页：www.gdei.edu.cn，在主页右方的导航栏找到"内网登录"，点击进入。</p>
          <p class="text-center">
            <img class="w-full h-auto" src="/img/about/application/account_2.png">
          </p>
          <p class="text-left indent-8">在统一身份证认证系统中，点击"查询账号"选项。</p>
          <p class="text-center">
            <img class="w-full h-auto" src="/img/about/application/account_3.png">
          </p>
          <p class="text-left indent-8">若姓名、学工号、身份证号和验证码信息正确无误，页面将显示你的校园网账号信息。其中，<span class="text-red-500">用户名则是使用本系统需要填写的账号用户名，2015级后的学生初始密码为身份证后六位数字。由于使用初始密码存在风险，建议你及时修改账号密码。</span></p>
        </div>
      </section>
      </div>

    <AppDialog
      :open="confirmOpen"
      :title="confirmAction === 'revoke' ? '撤回校园账号凭证授权' : '删除已保存的校园凭证'"
      :description="confirmAction === 'revoke' ? revokeDescription : deleteDescription"
      @close="closeConfirm"
      @confirm="performConfirmedAction"
    />
  </div>
</template>
