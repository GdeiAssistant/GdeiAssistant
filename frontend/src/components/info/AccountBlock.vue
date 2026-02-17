<template>
  <div v-if="accounts && accounts.length" class="modern-card">
    <div class="card-title">校园公众号</div>
    <div class="account-list">
      <a
        v-for="(acc, idx) in displayAccounts"
        :key="idx"
        :href="acc.biz ? `https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=${acc.biz}&scene=123#wechat_redirect` : 'javascript:;'"
        class="account-item"
        :class="{ 'account-item-last': idx === displayAccounts.length - 1 }"
      >
        <img :src="acc.avatar" alt="" class="account-avatar" />
        <div class="account-info">
          <div class="account-name">{{ acc.name }}</div>
          <div class="account-desc">{{ acc.description }}</div>
        </div>
        <button type="button" class="account-btn">查看</button>
      </a>
    </div>
    <div
      class="account-view-all"
      @click="goToAllAccounts"
    >
      点击查看所有热门校园公众号
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  accounts: {
    type: Array,
    default: () => []
  }
})

defineEmits(['view-all-accounts'])

const router = useRouter()

/** 卡片内只展示前 3 条，与原版 JSP begin="0" end="2" 一致 */
const displayAccounts = computed(() => (props.accounts || []).slice(0, 3))

function goToAllAccounts() {
  router.push('/wechataccount')
}
</script>

<style scoped>
.modern-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
.card-title::before {
  content: "";
  display: block;
  width: 4px;
  height: 16px;
  background: #09bb07;
  border-radius: 2px;
  margin-right: 8px;
}
.account-list {
  display: flex;
  flex-direction: column;
}
.account-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  text-decoration: none;
  color: inherit;
  border-bottom: 1px solid #f0f0f0;
}
.account-item-last {
  border-bottom: none;
}
.account-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  margin-right: 12px;
}
.account-info {
  flex: 1;
  min-width: 0;
}
.account-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}
.account-desc {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.account-btn {
  background: #e1f5e2;
  color: #09bb07;
  border: none;
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 12px;
  flex-shrink: 0;
  cursor: pointer;
}
.account-view-all {
  text-align: center;
  font-size: 13px;
  color: #576b95;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #f0f0f0;
  cursor: pointer;
}
</style>
