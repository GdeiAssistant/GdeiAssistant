<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { DialogRoot, DialogOverlay, DialogContent, DialogPortal } from 'radix-vue'
import {
  Search, Star, Calendar, CreditCard, FileText, GraduationCap,
  BookOpen, DoorOpen, Dumbbell, PenLine, Database, ShoppingCart,
  Search as SearchIcon, MessageCircle, Eye, Users, Heart, Camera,
  Truck, CornerDownLeft, ArrowUp, ArrowDown,
} from 'lucide-vue-next'
import { ALL_FEATURES } from '@/constants/features'

const props = defineProps({
  open: Boolean,
})

const emit = defineEmits(['close'])

const router = useRouter()
const query = ref('')
const selectedIndex = ref(0)
const inputRef = ref(null)

const iconMap = {
  grade: Star, schedule: Calendar, card: CreditCard, cet: FileText,
  kaoyan: GraduationCap, collection: BookOpen, spare: DoorOpen,
  pe: Dumbbell, evaluate: PenLine, data: Database,
  ershou: ShoppingCart, lostandfound: SearchIcon, express: Heart,
  secret: Eye, dating: Users, topic: MessageCircle,
  photograph: Camera, delivery: Truck,
}

const academicIds = ['grade', 'schedule', 'card', 'cet', 'kaoyan', 'collection', 'spare', 'pe', 'evaluate', 'data']

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) return ALL_FEATURES
  return ALL_FEATURES.filter(f => f.name.toLowerCase().includes(q))
})

const academicItems = computed(() => filtered.value.filter(f => academicIds.includes(f.id)))
const communityItems = computed(() => filtered.value.filter(f => !academicIds.includes(f.id)))

const flatList = computed(() => [...academicItems.value, ...communityItems.value])

watch(() => props.open, (val) => {
  if (val) {
    query.value = ''
    selectedIndex.value = 0
    nextTick(() => inputRef.value?.focus())
  }
})

watch(query, () => {
  selectedIndex.value = 0
})

function navigate(item) {
  router.push(item.path)
  emit('close')
}

function onKeydown(e) {
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    selectedIndex.value = (selectedIndex.value + 1) % flatList.value.length
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    selectedIndex.value = (selectedIndex.value - 1 + flatList.value.length) % flatList.value.length
  } else if (e.key === 'Enter') {
    e.preventDefault()
    const item = flatList.value[selectedIndex.value]
    if (item) navigate(item)
  }
}

function handleGlobalShortcut(e) {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    window.dispatchEvent(new CustomEvent('open-command-palette'))
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleGlobalShortcut)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalShortcut)
})

function getFlatIndex(item) {
  return flatList.value.indexOf(item)
}
</script>

<template>
  <DialogRoot :open="open">
    <DialogPortal>
      <Transition name="cp-overlay">
        <DialogOverlay
          v-if="open"
          class="fixed inset-0 bg-black/35 z-[500] flex items-start justify-center pt-[20vh]"
          @click.self="emit('close')"
        >
          <Transition name="cp-content" appear>
            <DialogContent
              class="w-[480px] max-w-[90vw] bg-[var(--c-surface)] rounded-2xl shadow-2xl overflow-hidden"
              @keydown="onKeydown"
              @escape-key-down="emit('close')"
            >
              <!-- Search input -->
              <div class="flex items-center gap-2.5 px-4 py-3 border-b border-[var(--c-border-light)]">
                <Search class="w-4 h-4 text-[var(--c-text-3)] shrink-0" />
                <input
                  ref="inputRef"
                  v-model="query"
                  placeholder="搜索功能..."
                  class="flex-1 bg-transparent outline-none text-sm"
                />
                <span
                  class="text-[10px] bg-[var(--c-bg)] border border-[var(--c-border)] rounded px-1.5 py-0.5 text-[var(--c-text-3)] cursor-pointer select-none"
                  @click="emit('close')"
                >ESC</span>
              </div>

              <!-- Results -->
              <div class="max-h-[340px] overflow-y-auto overscroll-contain">
                <!-- Academic group -->
                <template v-if="academicItems.length">
                  <div class="text-[10px] font-semibold uppercase tracking-wider text-[var(--c-text-3)] px-4 pt-2.5 pb-1">
                    学业服务
                  </div>
                  <div
                    v-for="item in academicItems"
                    :key="item.id"
                    class="flex items-center gap-2.5 px-4 py-2 cursor-pointer"
                    :class="{ 'bg-[var(--c-primary-50)]': getFlatIndex(item) === selectedIndex }"
                    @click="navigate(item)"
                    @mouseenter="selectedIndex = getFlatIndex(item)"
                  >
                    <component
                      :is="iconMap[item.id]"
                      v-if="iconMap[item.id]"
                      class="w-5 text-center text-[var(--c-text-3)]"
                      :size="16"
                    />
                    <span class="text-sm font-medium">{{ item.name }}</span>
                    <span class="ml-auto text-[11px] text-[var(--c-text-3)]">{{ item.path }}</span>
                  </div>
                </template>

                <!-- Community group -->
                <template v-if="communityItems.length">
                  <div class="text-[10px] font-semibold uppercase tracking-wider text-[var(--c-text-3)] px-4 pt-2.5 pb-1">
                    社区服务
                  </div>
                  <div
                    v-for="item in communityItems"
                    :key="item.id"
                    class="flex items-center gap-2.5 px-4 py-2 cursor-pointer"
                    :class="{ 'bg-[var(--c-primary-50)]': getFlatIndex(item) === selectedIndex }"
                    @click="navigate(item)"
                    @mouseenter="selectedIndex = getFlatIndex(item)"
                  >
                    <component
                      :is="iconMap[item.id]"
                      v-if="iconMap[item.id]"
                      class="w-5 text-center text-[var(--c-text-3)]"
                      :size="16"
                    />
                    <span class="text-sm font-medium">{{ item.name }}</span>
                    <span class="ml-auto text-[11px] text-[var(--c-text-3)]">{{ item.path }}</span>
                  </div>
                </template>

                <!-- Empty state -->
                <div v-if="!flatList.length" class="px-4 py-8 text-center text-sm text-[var(--c-text-3)]">
                  没有匹配的功能
                </div>
              </div>

              <!-- Footer -->
              <div class="px-4 py-2 border-t border-[var(--c-border-light)] flex gap-4 text-[10px] text-[var(--c-text-3)]">
                <span class="flex items-center gap-1">
                  <kbd class="bg-[var(--c-bg)] border border-[var(--c-border)] rounded px-1 py-0.5"><ArrowUp :size="10" /></kbd>
                  <kbd class="bg-[var(--c-bg)] border border-[var(--c-border)] rounded px-1 py-0.5"><ArrowDown :size="10" /></kbd>
                  导航
                </span>
                <span class="flex items-center gap-1">
                  <kbd class="bg-[var(--c-bg)] border border-[var(--c-border)] rounded px-1 py-0.5"><CornerDownLeft :size="10" /></kbd>
                  跳转
                </span>
                <span class="flex items-center gap-1">
                  <kbd class="bg-[var(--c-bg)] border border-[var(--c-border)] rounded px-1 py-0.5 text-[10px]">ESC</kbd>
                  关闭
                </span>
              </div>
            </DialogContent>
          </Transition>
        </DialogOverlay>
      </Transition>
    </DialogPortal>
  </DialogRoot>
</template>

<style scoped>
.cp-overlay-enter-active,
.cp-overlay-leave-active {
  transition: opacity 0.2s ease;
}
.cp-overlay-enter-from,
.cp-overlay-leave-to {
  opacity: 0;
}

.cp-content-enter-active {
  transition: all 0.2s ease-out;
}
.cp-content-leave-active {
  transition: all 0.15s ease-in;
}
.cp-content-enter-from {
  opacity: 0;
  transform: scale(0.96);
}
.cp-content-leave-to {
  opacity: 0;
  transform: scale(0.96);
}
</style>
