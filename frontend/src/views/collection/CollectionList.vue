<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { searchBooks } from '@/api/collection'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const { loading: showLoading, hideLoading } = useToast()

const list = ref([])
const loading = ref(false)
const currentPage = ref(1)
const keyword = computed(() => (route.query.keyword || '').trim())

function goBack() {
  router.back()
}

function openDetail(item) {
  if (!item?.detailURL) return
  router.push({ path: '/library/detail', query: { detailURL: item.detailURL } })
}

function fetchList() {
  loading.value = true
  showLoading('加载中')
  searchBooks(keyword.value, currentPage.value)
    .then((res) => {
      const result = res?.data
      list.value = Array.isArray(result?.collectionList) ? result.collectionList : []
    })
    .catch(() => {
      list.value = []
    })
    .finally(() => {
      loading.value = false
      hideLoading()
    })
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--
    fetchList()
  }
}

function nextPage() {
  currentPage.value++
  fetchList()
}

const showNoResult = computed(() => !loading.value && list.value.length === 0 && !!keyword.value)

onMounted(() => {
  if (!keyword.value) {
    router.replace('/library')
    return
  }
  fetchList()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <template v-if="keyword">
      <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
        <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
        <span class="flex-1 text-center text-sm font-bold">馆藏检索</span>
        <div class="w-10"></div>
      </div>

      <div class="max-w-lg mx-auto px-4 py-6">
        <p class="text-center text-sm text-[var(--c-text-2)] mb-5">图书馆馆藏检索结果</p>

        <h3 class="text-xs font-semibold text-[var(--c-text-2)] uppercase tracking-wide mb-2">馆藏检索结果</h3>

        <div class="bg-[var(--c-surface)] rounded-xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
          <a
            v-for="item in list"
            :key="item.detailURL || item.bookname"
            href="javascript:"
            class="block px-4 py-3.5 hover:bg-[var(--c-surface-hover)] transition-colors no-underline"
            @click.prevent="openDetail(item)"
          >
            <h4 class="text-base font-medium text-[var(--c-text)] break-all leading-snug">{{ item.bookname || '—' }}</h4>
            <p class="mt-1 text-sm text-[var(--c-text-2)]">
              <span class="text-[var(--c-text-3)]">著者: </span>{{ item.author || '—' }}
            </p>
            <p class="text-sm text-[var(--c-text-2)]">
              <span class="text-[var(--c-text-3)]">出版者: </span>{{ item.publishingHouse || '—' }}
            </p>
          </a>
        </div>

        <!-- Pagination -->
        <div v-if="list.length > 0" class="flex items-center justify-center gap-4 mt-5">
          <button
            class="px-4 py-1.5 text-sm border border-[var(--c-border)] rounded-lg bg-[var(--c-surface)] hover:bg-[var(--c-surface-hover)] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            :disabled="currentPage <= 1"
            @click="prevPage"
          >上一页</button>
          <span class="text-sm text-[var(--c-text-2)]">第 {{ currentPage }} 页</span>
          <button
            class="px-4 py-1.5 text-sm border border-[var(--c-border)] rounded-lg bg-[var(--c-surface)] hover:bg-[var(--c-surface-hover)] transition-colors"
            @click="nextPage"
          >下一页</button>
        </div>

        <div v-if="showNoResult" class="flex items-center justify-center min-h-[120px] py-10 text-sm text-[var(--c-text-3)]">
          暂无馆藏结果
        </div>
      </div>
    </template>
  </div>
</template>
