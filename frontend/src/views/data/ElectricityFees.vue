<template>
  <div class="electricity-page">
    <!-- 顶部错误提示 -->
    <div class="weui-toptips weui-toptips_warn" :style="{ display: showTopTips ? 'block' : 'none' }">
      {{ errorMsg }}
    </div>

    <div class="weui-cells__title" @click="router.back()">返回</div>
    <div class="hd">
      <h2 class="page-title">电费查询</h2>
    </div>

    <!-- 查询表单 -->
    <div v-if="!isQueried" id="edit">
      <div class="weui-cells weui-cells_form">
        <form @submit.prevent="submitQuery">
          <div class="weui-cell weui-cell_select weui-cell_select-after">
            <div class="weui-cell__hd">
              <label class="weui-label">年份</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
              <select v-model="form.year" class="weui-select" id="year" name="year">
                <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
              </select>
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__hd">
              <label class="weui-label">姓名</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
              <input
                v-model="form.name"
                id="name"
                class="weui-input"
                type="text"
                maxlength="10"
                name="name"
                placeholder="请输入你的姓名"
              />
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__hd">
              <label class="weui-label">学号</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
              <input
                v-model="form.number"
                id="number"
                class="weui-input"
                type="text"
                maxlength="11"
                name="number"
                placeholder="请输入你的学号"
                inputmode="numeric"
              />
            </div>
          </div>
        </form>
      </div>

      <!-- 查询按钮 -->
      <div class="weui-btn_area">
        <a href="javascript:;" class="weui-btn weui-btn_primary" @click="submitQuery">查询</a>
      </div>

      <!-- 查询中弹框 -->
      <div id="loadingToast" v-if="isLoading">
        <div class="weui-mask_transparent"></div>
        <div class="weui-toast">
          <i class="weui-loading weui-icon_toast"></i>
          <p class="weui-toast__content">正在查询</p>
        </div>
      </div>
    </div>

    <!-- 查询结果 -->
    <div v-if="isQueried" id="result">
      <div class="weui-cells__title">电费信息</div>
      <div class="weui-cells">
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>年份</p>
          </div>
          <div class="weui-cell__ft">{{ result.year }}</div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>宿舍</p>
          </div>
          <div class="weui-cell__ft">{{ result.buildingNumber }}{{ result.roomNumber }}</div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>入住人数</p>
          </div>
          <div class="weui-cell__ft">{{ result.peopleNumber }}</div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>用电数额</p>
          </div>
          <div class="weui-cell__ft">{{ result.usedElectricAmount }}</div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>免费电额</p>
          </div>
          <div class="weui-cell__ft">{{ result.freeElectricAmount }}</div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>计费电数</p>
          </div>
          <div class="weui-cell__ft">{{ result.feeBasedElectricAmount }}</div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>电价</p>
          </div>
          <div class="weui-cell__ft">{{ result.electricPrice }}</div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>总电费</p>
          </div>
          <div class="weui-cell__ft">{{ result.totalElectricBill }}</div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__bd">
            <p>平均电费</p>
          </div>
          <div class="weui-cell__ft">{{ result.averageElectricBill }}</div>
        </div>
      </div>
      <div class="weui-btn_area">
        <a class="weui-btn weui-btn_default" href="javascript:" @click="resetQuery">重新查询</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const form = ref({
  year: new Date().getFullYear(),
  name: '',
  number: ''
})

const isLoading = ref(false)
const errorMsg = ref('')
const showTopTips = ref(false)
const isQueried = ref(false)
const result = ref({})

// 生成年份选项（2016-当前年份）
const years = computed(() => {
  const currentYear = new Date().getFullYear()
  const yearList = []
  for (let i = 2016; i <= currentYear; i++) {
    yearList.push(i)
  }
  return yearList.reverse()
})

const showError = (msg) => {
  errorMsg.value = msg
  showTopTips.value = true
  setTimeout(() => {
    showTopTips.value = false
  }, 2000)
}

const submitQuery = () => {
  console.log('submitQuery 被调用', form.value)
  
  // 依次检查所有必填字段，显示具体错误
  if (!form.value.year) {
    showError('请输入年份')
    return
  }
  
  const name = String(form.value.name || '').trim()
  const number = String(form.value.number || '').trim()
  
  if (!name) {
    showError('请输入姓名')
    return
  }
  
  if (!number) {
    showError('请输入学号')
    return
  }

  // 学号校验（11位数字）
  if (!/^\d{11}$/.test(number)) {
    showError('请输入正确的学号（11位数字）')
    return
  }

  // 年份校验
  const year = Number(form.value.year)
  if (year < 2016 || year > 2050) {
    showError('请选择正确的年份')
    return
  }

  // 开始查询
  console.log('开始发送请求', { name, number, year })
  isLoading.value = true
  
  request
    .post('/api/data/electricfees', {
      name: name,
      number: number,
      year: year
    })
    .then((res) => {
      console.log('请求成功', res)
      isLoading.value = false
      if (res && res.success && res.data) {
        result.value = res.data
        isQueried.value = true
      } else {
        showError(res?.message || '查询失败')
      }
    })
    .catch((err) => {
      console.error('请求失败', err)
      isLoading.value = false
      const errorMsg = err?.response?.data?.message || err?.message || '网络连接异常，请检查并重试'
      showError(errorMsg)
    })
}

const resetQuery = () => {
  isQueried.value = false
  form.value = {
    year: new Date().getFullYear(),
    name: '',
    number: ''
  }
  result.value = {}
}
</script>

<style scoped>
.electricity-page {
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
#loadingToast {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}
.weui-toptips {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10000;
  text-align: center;
  padding: 8px;
  background-color: #f43530;
  color: #fff;
  font-size: 14px;
}
</style>
