<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const actions = [
  {
    id: 'info',
    title: '卡片信息',
    description: '查看姓名、卡号、余额和挂失状态',
    path: '/card/info',
    badge: '信息'
  },
  {
    id: 'records',
    title: '消费记录',
    description: '选择日期后查看当天校园卡消费流水',
    path: '/card/records',
    badge: '流水'
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
  <div class="card-page">
    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>

    <div class="hero-card">
      <div class="hero-chip">校园卡</div>
      <h1 class="hero-title">校园卡</h1>
      <p class="hero-subtitle">校园卡信息、消费记录和挂失能力统一放在一个模块里，首页不再拆成多个一级入口。</p>
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
.card-page {
  min-height: 100vh;
  padding: 0 16px 28px;
  background:
    radial-gradient(circle at top left, rgba(13, 148, 136, 0.16), transparent 34%),
    linear-gradient(180deg, #f4fbfb 0%, #ffffff 42%);
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
  color: #5f6b6b;
  cursor: pointer;
}

.hero-card {
  margin-top: 8px;
  padding: 24px 20px;
  border-radius: 24px;
  background: linear-gradient(160deg, #e8fbf8 0%, #ffffff 100%);
  box-shadow: 0 18px 50px rgba(20, 62, 58, 0.08);
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(13, 148, 136, 0.12);
  color: #0f766e;
  font-size: 13px;
  font-weight: 600;
}

.hero-title {
  margin: 18px 0 8px;
  font-size: 34px;
  line-height: 1.1;
  color: #102120;
}

.hero-subtitle {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: #5f6b6b;
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
  border: 1px solid rgba(15, 118, 110, 0.12);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 36px rgba(20, 62, 58, 0.06);
  text-align: left;
}

.action-card__badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.1);
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
}

.action-card__title {
  margin-top: 18px;
  font-size: 22px;
  line-height: 1.2;
  color: #102120;
  font-weight: 700;
}

.action-card__desc {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.7;
  color: #667170;
}

@media (max-width: 640px) {
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
