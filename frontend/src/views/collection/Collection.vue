<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const actions = [
  {
    id: 'search',
    title: '馆藏检索',
    description: '按书名、作者和出版社检索馆藏图书',
    path: '/library/search',
    badge: '检索'
  },
  {
    id: 'borrow',
    title: '我的借阅',
    description: '输入图书馆密码后查看借阅记录并继续续借',
    path: '/library/borrow',
    badge: '借阅'
  }
]

function goBack() {
  router.back()
}

function openAction(path) {
  if (path) {
    router.push(path)
  }
}
</script>

<template>
  <div class="library-page">
    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>

    <div class="hero-card">
      <div class="hero-chip">图书馆</div>
      <h1 class="hero-title">图书馆</h1>
      <p class="hero-subtitle">馆藏检索和我的借阅统一放在一个模块里，首页不再拆成两个平级入口。</p>
    </div>

    <div class="action-grid">
      <button
        v-for="item in actions"
        :key="item.id"
        type="button"
        class="action-card"
        @click="openAction(item.path)"
      >
        <div class="action-card__badge">{{ item.badge }}</div>
        <div class="action-card__title">{{ item.title }}</div>
        <div class="action-card__desc">{{ item.description }}</div>
      </button>
    </div>
  </div>
</template>

<style scoped>
.library-page {
  min-height: 100vh;
  padding: 0 16px 28px;
  background:
    radial-gradient(circle at top right, rgba(9, 187, 7, 0.14), transparent 34%),
    linear-gradient(180deg, #f7fbf7 0%, #ffffff 42%);
  box-sizing: border-box;
}

.top-nav-bar {
  display: flex;
  align-items: center;
  min-height: 44px;
  padding: 12px 0 4px;
}

.nav-btn-back {
  font-size: 16px;
  line-height: 24px;
  color: #5f6b5f;
  cursor: pointer;
}

.hero-card {
  margin-top: 8px;
  padding: 24px 20px;
  border-radius: 24px;
  background: linear-gradient(160deg, #eaf8ea 0%, #ffffff 100%);
  box-shadow: 0 18px 50px rgba(23, 55, 30, 0.08);
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(9, 187, 7, 0.12);
  color: #137333;
  font-size: 13px;
  font-weight: 600;
}

.hero-title {
  margin: 18px 0 8px;
  font-size: 34px;
  line-height: 1.1;
  color: #102110;
}

.hero-subtitle {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: #5f6b5f;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-height: 180px;
  padding: 20px 18px;
  border: 1px solid rgba(19, 115, 51, 0.12);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 36px rgba(26, 52, 32, 0.06);
  text-align: left;
}

.action-card__badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(19, 115, 51, 0.1);
  color: #137333;
  font-size: 12px;
  font-weight: 700;
}

.action-card__title {
  margin-top: 18px;
  font-size: 22px;
  line-height: 1.2;
  color: #102110;
  font-weight: 700;
}

.action-card__desc {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.7;
  color: #6a756a;
}

@media (max-width: 640px) {
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
