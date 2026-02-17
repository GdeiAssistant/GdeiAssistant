<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const formData = ref({
  nickname: '',
  realName: '',
  myGender: '',
  receiverName: '',
  receiverGender: '',
  content: ''
})

// 性别选项：['男', '女', '其他或保密']
const genderOptions = [
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
  { label: '其他或保密', value: 'secret' }
]
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function submit() {
  if (!formData.value.nickname || formData.value.nickname.trim() === '') {
    showDialog('请输入昵称')
    return
  }
  // 真名为可选，不验证
  if (!formData.value.myGender) {
    showDialog('请选择你的性别')
    return
  }
  if (!formData.value.receiverName || formData.value.receiverName.trim() === '') {
    showDialog('请输入TA的名字')
    return
  }
  if (!formData.value.receiverGender) {
    showDialog('请选择TA的性别')
    return
  }
  if (!formData.value.content || formData.value.content.trim() === '') {
    showDialog('请填写表白内容')
    return
  }

  submitting.value = true
  // 根据是否填写真实姓名，设置 canGuess 标识
  const payload = {
    ...formData.value,
    canGuess: !!formData.value.realName && formData.value.realName.trim() !== ''
  }
  request.post('/express/publish', payload)
    .then(() => {
      showDialog('发布成功')
      setTimeout(() => {
        router.push('/express/home')
      }, 1500)
    })
    .catch(() => {
      submitting.value = false
    })
}
</script>

<template>
  <div class="dating-publish">
    <!-- 标准 .unified-header：左侧返回 -->
    <div class="dating-header unified-header">
      <span class="dating-header__back" @click="router.push('/express/home')">返回</span>
      <h1 class="dating-header__title"></h1>
      <span class="dating-header__placeholder"></span>
    </div>

    <!-- 巨大的浅粉色粗体标题 -->
    <h2 class="dating-main-title">广东第二师范学院表白墙</h2>

    <!-- 手账风表单：蓝色虚线表单框 -->
    <div class="dating-form">
      <!-- 第一个 form-section：你的信息 -->
      <div class="form-section">
        <span class="form-section-title">你的信息</span>
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">昵称</label></div>
            <div class="weui-cell__bd">
              <input
                class="weui-input"
                type="text"
                placeholder="请输入昵称"
                v-model="formData.nickname"
              />
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">真名</label></div>
            <div class="weui-cell__bd">
              <input
                class="weui-input"
                type="text"
                placeholder="请输入真名"
                v-model="formData.realName"
              />
              <div class="hint-text">注：真实姓名可选填，默认保密不显示！填写即可参加紧张刺激的猜名字游戏！</div>
            </div>
          </div>
          <div class="weui-cell weui-cell_select">
            <div class="weui-cell__hd"><label class="weui-label">性别</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model="formData.myGender">
                <option value="">请选择</option>
                <option value="male">男</option>
                <option value="female">女</option>
                <option value="secret">其他或保密</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 第二个 form-section：TA的信息 -->
      <div class="form-section">
        <span class="form-section-title">TA的信息</span>
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">名字</label></div>
            <div class="weui-cell__bd">
              <input
                class="weui-input"
                type="text"
                placeholder="请输入TA的名字"
                v-model="formData.receiverName"
              />
            </div>
          </div>
          <div class="weui-cell weui-cell_select">
            <div class="weui-cell__hd"><label class="weui-label">性别</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model="formData.receiverGender">
                <option value="">请选择</option>
                <option value="male">男</option>
                <option value="female">女</option>
                <option value="secret">其他或保密</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 表白内容 -->
      <div class="form-section">
        <span class="form-section-title">表白内容</span>
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <textarea
                class="weui-textarea"
                placeholder="写下你想对TA说的话..."
                rows="4"
                v-model="formData.content"
              ></textarea>
            </div>
          </div>
        </div>
      </div>

      <div class="dating-submit-wrap">
        <button
          type="button"
          class="btn-submit"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? '提交中...' : '发布表白' }}
        </button>
      </div>
    </div>

    <!-- 提示对话框 -->
    <div v-if="dialogVisible">
      <div class="weui-mask" @click="dialogVisible = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
        <div class="weui-dialog__bd">{{ dialogMessage }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-publish {
  background: #f5f5f5;
  padding-bottom: 80px;
}

.dating-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.dating-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
}
.dating-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.dating-header__placeholder {
  min-width: 48px;
  text-align: right;
}

.dating-main-title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #ffb3ba;
  margin: 16px 15px 20px;
  padding: 0;
  line-height: 1.3;
}

/* 手账风：蓝色虚线表单框 */
.form-section {
  border: 2px dashed #81d4fa;
  border-radius: 8px;
  padding: 25px 15px 15px;
  margin: 25px 15px 15px;
  position: relative;
  background-color: #fff;
}
.form-section-title {
  position: absolute;
  top: -12px;
  left: -2px;
  background-color: #4fc3f7;
  color: white;
  padding: 4px 10px;
  border-radius: 2px;
  font-size: 14px;
  font-weight: 500;
}

/* WEUI 风格输入框 */
.weui-cells {
  margin-top: 0;
  background-color: transparent;
}
.weui-cell {
  padding: 12px 0;
  border-bottom: 1px solid #e5e5e5;
}
.weui-cell:last-child {
  border-bottom: none;
}
.weui-label {
  width: 70px;
  font-size: 14px;
  color: #333;
}
.weui-input,
.weui-select,
.weui-textarea {
  font-size: 14px;
  color: #333;
}
.weui-textarea {
  min-height: 80px;
  padding: 8px 0;
}
.weui-select {
  width: 100%;
  border: none;
  background: transparent;
  appearance: none;
  -webkit-appearance: none;
}

.dating-submit-wrap {
  padding: 20px 15px 40px;
}
.btn-submit {
  width: 100%;
  height: 44px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #74b9ff;
  color: #fff;
  border-radius: 6px;
  font-size: 16px;
  border: none;
  cursor: pointer;
}
.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hint-text {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  line-height: 1.4;
}

.weui-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
}
.weui-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 300px;
  background: #fff;
  border-radius: 8px;
  z-index: 1001;
  overflow: hidden;
}
.weui-dialog__hd {
  padding: 20px 20px 10px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
  font-weight: 500;
  color: #333;
}
.weui-dialog__bd {
  padding: 10px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #d9d9d9;
}
.weui-dialog__btn {
  flex: 1;
  padding: 15px 0;
  text-align: center;
  font-size: 17px;
  color: #4fc3f7;
  text-decoration: none;
}
.weui-dialog__btn_primary {
  font-weight: 500;
}
</style>
