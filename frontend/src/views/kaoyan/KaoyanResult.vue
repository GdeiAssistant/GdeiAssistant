<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const route = useRoute()
const scoreData = ref({})
const hasData = ref(false)
const isLoading = ref(true)

function goBack() {
  router.back()
}

function reQuery() {
  router.back()
}

onMounted(() => {
  const { name, candidateNo, idNo } = route.query
  if (!name || !candidateNo || !idNo) {
    isLoading.value = false
    return
  }
  isLoading.value = true
  request.get('/kaoyan/query', { params: route.query })
    .then((res) => {
      const targetData = res?.data?.data ?? res?.data ?? res
      if (targetData && targetData.totalScore !== undefined) {
        scoreData.value = targetData
        hasData.value = true
      } else {
        hasData.value = false
      }
    })
    .catch(() => {
      hasData.value = false
    })
    .finally(() => {
      isLoading.value = false
    })
})
</script>

<template>
  <div class="kaoyan-result-page">
    <template v-if="isLoading">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="加载中"></span>
          <p class="weui-toast__content">加载中</p>
        </div>
      </div>
    </template>

    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <div class="page-header">
      <h1 class="page-title-green">考研成绩查询</h1>
    </div>

    <template v-if="!isLoading && hasData">
      <div class="kaoyan-total">初试总分</div>
      <div class="kaoyan-total-value">{{ scoreData.totalScore }}</div>

      <div class="weui-form-preview kaoyan-preview">
        <div class="weui-form-preview__bd">
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">姓名</label>
            <span class="weui-form-preview__value">{{ scoreData.name ?? '—' }}</span>
          </div>
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">考号</label>
            <span class="weui-form-preview__value">{{ scoreData.candidateNo ?? '—' }}</span>
          </div>
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">思想政治理论</label>
            <span class="weui-form-preview__value">{{ scoreData.politics ?? '—' }}</span>
          </div>
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">外国语</label>
            <span class="weui-form-preview__value">{{ scoreData.foreignLanguage ?? '—' }}</span>
          </div>
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">业务课一</label>
            <span class="weui-form-preview__value">{{ scoreData.business1 ?? '—' }}</span>
          </div>
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">业务课二</label>
            <span class="weui-form-preview__value">{{ scoreData.business2 ?? '—' }}</span>
          </div>
        </div>
      </div>

      <div class="weui-btn_area">
        <button type="button" class="weui-btn weui-btn_primary" @click="reQuery">重新查询</button>
      </div>
    </template>

    <div v-if="!isLoading && !hasData" class="kaoyan-empty">
      暂无成绩数据
    </div>
  </div>
</template>

<style scoped>
.kaoyan-result-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}

.top-nav-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: #fff;
  box-sizing: border-box;
}

.nav-btn-back {
  font-size: 16px;
  line-height: 24px;
  color: #888;
  cursor: pointer;
}

.page-header {
  text-align: center;
  padding: 0 0 20px;
  background-color: #fff;
}

.page-title-green {
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 0 0 20px 0;
  line-height: 1.2;
}

.kaoyan-total {
  text-align: center;
  font-size: 16px;
  color: #09bb07;
  margin-bottom: 4px;
}

.kaoyan-total-value {
  text-align: center;
  font-size: 28px;
  font-weight: 500;
  color: #09bb07;
  margin-bottom: 24px;
}

.kaoyan-preview {
  margin: 0 15px 24px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  overflow: hidden;
}

.kaoyan-preview .weui-form-preview__item {
  padding: 10px 15px;
}

.kaoyan-preview .weui-form-preview__label {
  color: #999;
}

.kaoyan-preview .weui-form-preview__value {
  color: #333;
}

.kaoyan-result-page .weui-btn_area {
  padding: 0 15px;
}

.kaoyan-result-page .weui-btn_area .weui-btn {
  width: 100%;
}

.kaoyan-empty {
  text-align: center;
  padding: 40px 15px;
  font-size: 14px;
  color: #999;
}
</style>
