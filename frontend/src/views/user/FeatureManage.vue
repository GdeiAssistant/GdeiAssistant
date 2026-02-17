<template>
  <div class="page-container">
    <div class="unified-header">
      <div class="header-left" @click="goBack">返回</div>
      <div class="header-title">功能管理</div>
      <div class="header-right"></div>
    </div>

    <div class="weui-cells__title">
      首页功能模块展示设置（关闭后将不在首页显示）
    </div>

    <div class="weui-cells weui-cells_form">
      <div
        class="weui-cell weui-cell_active weui-cell_switch"
        v-for="item in featureList"
        :key="item.key"
      >
        <div class="weui-cell__bd">{{ item.name }}</div>
        <div class="weui-cell__ft">
          <input
            class="weui-switch"
            type="checkbox"
            v-model="item.visible"
            @change="handleToggle(item)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const featureList = ref([
  // 学习相关功能
  { key: 'grade', name: '成绩查询', visible: true },
  { key: 'schedule', name: '课表查询', visible: true },
  { key: 'cet', name: '四六级查询', visible: true },
  { key: 'collection', name: '馆藏查询', visible: true },
  { key: 'book', name: '我的图书馆', visible: true },
  { key: 'evaluate', name: '教学评价', visible: true },
  { key: 'spare', name: '教室查询', visible: true },
  { key: 'kaoyan', name: '考研查询', visible: true },
  { key: 'pe', name: '体测查询', visible: true },
  // 生活服务功能
  { key: 'card', name: '消费查询', visible: true },
  { key: 'cardInfo', name: '我的饭卡', visible: true },
  { key: 'news', name: '新闻通知', visible: true },
  { key: 'data', name: '信息查询', visible: true },
  // 社交功能模块
  { key: 'ershou', name: '二手交易', visible: true },
  { key: 'lostandfound', name: '失物招领', visible: true },
  { key: 'secret', name: '校园树洞', visible: true },
  { key: 'photograph', name: '拍好校园', visible: true },
  { key: 'express', name: '表白墙', visible: true },
  { key: 'dating', name: '卖室友', visible: true },
  { key: 'topic', name: '话题', visible: true },
  { key: 'delivery', name: '全民快递', visible: true },
  // 外部链接功能
  { key: 'calendar', name: '学期校历', visible: true },
  { key: 'government', name: '政务服务', visible: true },
  { key: 'student', name: '学信网', visible: true },
  { key: 'volunteer', name: 'i志愿', visible: true },
  { key: 'healthcode', name: '粤康码', visible: true },
  { key: 'travelcode', name: '通信行程码', visible: true },
  { key: 'ncov', name: '疫情动态', visible: true },
])

const STORAGE_PREFIX = 'gdei_feature_'

const loadFromStorage = () => {
  featureList.value.forEach((item) => {
    const savedStatus = localStorage.getItem(`${STORAGE_PREFIX}${item.key}`)
    if (savedStatus !== null) {
      item.visible = savedStatus === 'true'
    } else {
      item.visible = true
    }
  })
}

const handleToggle = (item) => {
  localStorage.setItem(`${STORAGE_PREFIX}${item.key}`, String(item.visible))
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadFromStorage()
})
</script>

<style scoped>
.page-container {
  background-color: #f8f8f8;
  min-height: 100vh;
  box-sizing: border-box;
}

.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  background-color: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 0 16px;
}

.header-left {
  font-size: 16px;
  color: #333;
  cursor: pointer;
  width: 60px;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #000;
  flex: 1;
  text-align: center;
  margin: 0;
  padding: 0;
}

.header-right {
  width: 60px;
}

.weui-cells__title {
  margin-top: 16px;
  margin-bottom: 8px;
  color: #999;
  font-size: 14px;
  padding: 0 16px;
}

.weui-cells {
  margin-top: 0;
  background-color: #fff;
}

.weui-cell_switch {
  padding: 12px 16px;
}

.weui-cell__bd {
  font-size: 16px;
  color: #333;
}
</style>

