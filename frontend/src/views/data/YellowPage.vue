<template>
  <div class="yellowpage-page">
    <div class="weui-cells__title" @click="router.back()">返回</div>
    <div class="hd">
      <h2 class="page-title">黄页查询</h2>
    </div>

    <div v-if="loading" class="weui-loading">
      <div class="weui-loading__dot"></div>
      <p class="weui-loading__content">加载中</p>
    </div>

    <div v-else id="result">
      <div
        v-for="(group, groupIndex) in groupedData"
        :key="groupIndex"
        class="weui-cell-container"
      >
        <div class="weui-cells__title">{{ group.typeName }}</div>
        <div class="weui-cells">
          <a
            v-for="(item, index) in group.items"
            :key="index"
            class="weui-cell weui-cell_access"
            :href="item.majorPhone ? `tel:${item.majorPhone}` : 'javascript:'"
          >
            <div class="weui-cell__bd">
              <p>{{ item.section }}</p>
            </div>
            <div class="weui-cell__ft"></div>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const loading = ref(false)
const yellowPageData = ref({ type: [], data: [] })

const groupedData = computed(() => {
  const groups = {}
  yellowPageData.value.type.forEach((type) => {
    groups[type.typeCode] = {
      typeCode: type.typeCode,
      typeName: type.typeName,
      items: []
    }
  })
  yellowPageData.value.data.forEach((item) => {
    if (groups[item.typeCode]) {
      groups[item.typeCode].items.push(item)
    }
  })
  return Object.values(groups).filter((g) => g.items.length > 0)
})

function loadYellowPage() {
  loading.value = true
  request
    .get('/api/data/yellowpage')
    .then((res) => {
      loading.value = false
      if (res.success && res.data) {
        yellowPageData.value = res.data
      }
    })
    .catch(() => {
      loading.value = false
    })
}

onMounted(() => {
  loadYellowPage()
})
</script>

<style scoped>
.yellowpage-page {
  min-height: 100vh;
  background-color: #fff;
}
.page-title {
  text-align: center;
  color: #09bb07;
  padding: 10px 0;
  margin: 0;
  font-size: 34px;
  font-weight: 400;
}
.weui-loading {
  text-align: center;
  padding: 40px 0;
}
.weui-loading__dot {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #09bb07;
  border-top-color: transparent;
  border-radius: 50%;
  animation: weui-loading 1s linear infinite;
}
.weui-loading__content {
  margin-top: 10px;
  color: #999;
  font-size: 14px;
}
@keyframes weui-loading {
  to {
    transform: rotate(360deg);
  }
}
</style>
