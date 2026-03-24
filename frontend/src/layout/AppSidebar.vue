<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  Home,
  Bell,
  Star,
  Calendar,
  CreditCard,
  FileText,
  GraduationCap,
  BookOpen,
  DoorOpen,
  Dumbbell,
  PenLine,
  Database,
  ShoppingCart,
  Search,
  MessageCircle,
  Eye,
  Users,
  Heart,
  Camera,
  Truck,
  Settings,
  Info,
  ChevronRight,
} from 'lucide-vue-next'
import { ALL_FEATURES } from '@/constants/features'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const iconMap = {
  grade: Star,
  schedule: Calendar,
  card: CreditCard,
  cet: FileText,
  kaoyan: GraduationCap,
  collection: BookOpen,
  spare: DoorOpen,
  pe: Dumbbell,
  evaluate: PenLine,
  data: Database,
  ershou: ShoppingCart,
  lostandfound: Search,
  express: Heart,
  secret: Eye,
  dating: Users,
  topic: MessageCircle,
  photograph: Camera,
  delivery: Truck,
}

const academicIds = ['grade', 'schedule', 'card', 'cet', 'kaoyan', 'collection', 'spare', 'pe', 'evaluate', 'data']
const communityIds = ['ershou', 'lostandfound', 'express', 'secret', 'dating', 'topic', 'photograph', 'delivery']

const academicItems = computed(() =>
  academicIds
    .map((id) => ALL_FEATURES.find((f) => f.id === id))
    .filter(Boolean)
    .map((f) => ({ ...f, icon: iconMap[f.id] })),
)

const communityItems = computed(() =>
  communityIds
    .map((id) => ALL_FEATURES.find((f) => f.id === id))
    .filter(Boolean)
    .map((f) => ({ ...f, icon: iconMap[f.id] })),
)

function isActive(path) {
  return route.path.startsWith(path)
}

function navigate(path) {
  router.push(path)
}
</script>

<template>
  <aside class="fixed left-0 top-0 h-full w-[220px] bg-white border-r border-gray-200 flex flex-col z-40">
    <!-- Brand header -->
    <div class="flex items-center gap-2.5 px-4 py-5">
      <div
        class="w-8 h-8 rounded-lg bg-gradient-to-br from-green-400 to-green-600 flex items-center justify-center text-white font-bold text-sm shrink-0"
      >
        G
      </div>
      <span class="text-base font-semibold text-gray-900 truncate">广东二师助手</span>
    </div>

    <!-- Scrollable nav area -->
    <nav class="flex-1 overflow-y-auto px-3 pb-4 space-y-5">
      <!-- 概览 -->
      <div>
        <p class="text-[10px] font-semibold uppercase tracking-wider text-gray-400 px-2.5 mb-1.5">概览</p>
        <ul class="space-y-0.5">
          <li>
            <button
              class="flex items-center gap-2.5 w-full rounded-lg px-2.5 py-1.5 text-sm font-medium transition-colors"
              :class="
                isActive('/home')
                  ? 'bg-[var(--c-primary-50)] text-[var(--c-primary)] font-semibold'
                  : 'text-gray-500 hover:bg-gray-50'
              "
              @click="navigate('/home')"
            >
              <Home class="w-4 h-4 shrink-0" />
              <span>首页</span>
            </button>
          </li>
          <li>
            <button
              class="flex items-center gap-2.5 w-full rounded-lg px-2.5 py-1.5 text-sm font-medium transition-colors"
              :class="
                isActive('/info')
                  ? 'bg-[var(--c-primary-50)] text-[var(--c-primary)] font-semibold'
                  : 'text-gray-500 hover:bg-gray-50'
              "
              @click="navigate('/info')"
            >
              <Bell class="w-4 h-4 shrink-0" />
              <span class="flex-1 text-left">消息通知</span>
              <span class="bg-red-500 text-white text-[10px] px-1.5 rounded-full leading-4">3</span>
            </button>
          </li>
        </ul>
      </div>

      <!-- 教务服务 -->
      <div>
        <p class="text-[10px] font-semibold uppercase tracking-wider text-gray-400 px-2.5 mb-1.5">教务服务</p>
        <ul class="space-y-0.5">
          <li v-for="item in academicItems" :key="item.id">
            <button
              class="flex items-center gap-2.5 w-full rounded-lg px-2.5 py-1.5 text-sm font-medium transition-colors"
              :class="
                isActive(item.path)
                  ? 'bg-[var(--c-primary-50)] text-[var(--c-primary)] font-semibold'
                  : 'text-gray-500 hover:bg-gray-50'
              "
              @click="navigate(item.path)"
            >
              <component :is="item.icon" class="w-4 h-4 shrink-0" />
              <span>{{ item.name }}</span>
            </button>
          </li>
        </ul>
      </div>

      <!-- 校园社区 -->
      <div>
        <p class="text-[10px] font-semibold uppercase tracking-wider text-gray-400 px-2.5 mb-1.5">校园社区</p>
        <ul class="space-y-0.5">
          <li v-for="item in communityItems" :key="item.id">
            <button
              class="flex items-center gap-2.5 w-full rounded-lg px-2.5 py-1.5 text-sm font-medium transition-colors"
              :class="
                isActive(item.path)
                  ? 'bg-[var(--c-primary-50)] text-[var(--c-primary)] font-semibold'
                  : 'text-gray-500 hover:bg-gray-50'
              "
              @click="navigate(item.path)"
            >
              <component :is="item.icon" class="w-4 h-4 shrink-0" />
              <span>{{ item.name }}</span>
            </button>
          </li>
        </ul>
      </div>
    </nav>

    <!-- Footer -->
    <div class="border-t border-gray-200 px-3 py-3 space-y-1">
      <button
        class="flex items-center gap-2.5 w-full rounded-lg px-2.5 py-1.5 text-sm font-medium text-gray-500 hover:bg-gray-50 transition-colors"
        @click="navigate('/settings')"
      >
        <Settings class="w-4 h-4 shrink-0" />
        <span>设置</span>
      </button>
      <button
        class="flex items-center gap-2.5 w-full rounded-lg px-2.5 py-1.5 text-sm font-medium text-gray-500 hover:bg-gray-50 transition-colors"
        @click="navigate('/about')"
      >
        <Info class="w-4 h-4 shrink-0" />
        <span>关于</span>
      </button>

      <!-- User avatar card -->
      <div
        class="flex items-center gap-2.5 mt-3 px-2.5 py-2 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
      >
        <div
          class="w-8 h-8 rounded-full bg-gradient-to-br from-green-400 to-green-600 flex items-center justify-center text-white text-xs font-semibold shrink-0"
        >
          李
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-semibold text-gray-900 truncate">李同学</p>
          <p class="text-[11px] text-gray-400 truncate">计科 · 2023级</p>
        </div>
        <ChevronRight class="w-4 h-4 text-gray-300 shrink-0" />
      </div>
    </div>
  </aside>
</template>
