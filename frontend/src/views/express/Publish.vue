<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

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

function showLoading(text = '正在发布...') {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') weui.loading(text)
}

function hideLoading() {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.hideLoading === 'function') weui.hideLoading()
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

  const mapGender = (v) => { if (v === 'male') return 0; if (v === 'female') return 1; return 2 }
  const payload = {
    nickname: formData.value.nickname.trim(),
    realname: formData.value.realName?.trim() || '',
    selfGender: mapGender(formData.value.myGender),
    name: formData.value.receiverName.trim(),
    personGender: mapGender(formData.value.receiverGender),
    content: formData.value.content.trim()
  }
  submitting.value = true
  showLoading('正在发布...')
  request.post('/express', payload)
    .then(() => {
      hideLoading()
      const weui = typeof window !== 'undefined' && window.weui
      if (weui && typeof weui.toast === 'function') weui.toast('发布成功', { duration: 1500 })
      setTimeout(() => router.push('/express/home'), 1500)
    })
    .catch(() => {
      submitting.value = false
      hideLoading()
    })
}
</script>

<template>
  <div class="express-publish">
    <CommunityHeader title="发布表白" moduleColor="#f43f5e" backTo="/express/home" />

    <!-- 浅粉色粗体标题 -->
    <h2 class="express-main-title">广东第二师范学院表白墙</h2>

    <!-- 手账风表单：蓝色虚线表单框 -->
    <div class="express-form">
      <!-- 第一个 form-section：你的信息 -->
      <div class="form-section">
        <span class="form-section-title">你的信息</span>
        <div class="form-cells">
          <div class="form-cell">
            <div class="form-cell__hd"><label class="form-label">昵称</label></div>
            <div class="form-cell__bd">
              <input
                class="form-input"
                type="text"
                placeholder="请输入昵称"
                v-model="formData.nickname"
              />
            </div>
          </div>
          <div class="form-cell">
            <div class="form-cell__hd"><label class="form-label">真名</label></div>
            <div class="form-cell__bd">
              <input
                class="form-input"
                type="text"
                placeholder="请输入真名"
                v-model="formData.realName"
              />
              <div class="hint-text">注：真实姓名可选填，默认保密不显示！填写即可参加紧张刺激的猜名字游戏！</div>
            </div>
          </div>
          <div class="form-cell">
            <div class="form-cell__hd"><label class="form-label">性别</label></div>
            <div class="form-cell__bd">
              <select class="form-select" v-model="formData.myGender">
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
        <div class="form-cells">
          <div class="form-cell">
            <div class="form-cell__hd"><label class="form-label">名字</label></div>
            <div class="form-cell__bd">
              <input
                class="form-input"
                type="text"
                placeholder="请输入TA的名字"
                v-model="formData.receiverName"
              />
            </div>
          </div>
          <div class="form-cell">
            <div class="form-cell__hd"><label class="form-label">性别</label></div>
            <div class="form-cell__bd">
              <select class="form-select" v-model="formData.receiverGender">
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
        <div class="form-cells">
          <div class="form-cell">
            <div class="form-cell__bd">
              <textarea
                class="form-textarea"
                placeholder="写下你想对TA说的话..."
                rows="4"
                v-model="formData.content"
              ></textarea>
            </div>
          </div>
        </div>
      </div>

      <div class="express-submit-wrap">
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
      <div class="community-dialog-mask" @click="dialogVisible = false"></div>
      <div class="community-dialog">
        <div class="community-dialog__title">提示</div>
        <div class="community-dialog__body">{{ dialogMessage }}</div>
        <div class="community-dialog__footer">
          <button class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.express-publish {
  background: var(--c-bg);
  padding-bottom: 80px;
}

.express-main-title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #ffb3ba;
  margin: var(--space-lg) var(--space-lg) var(--space-xl);
  padding: 0;
  line-height: 1.3;
}

/* 手账风：蓝色虚线表单框 */
.form-section {
  border: 2px dashed #81d4fa;
  border-radius: var(--radius-md);
  padding: 25px var(--space-lg) var(--space-lg);
  margin: 25px var(--space-lg) var(--space-lg);
  position: relative;
  background-color: var(--c-card);
  box-shadow: var(--shadow-sm);
}
.form-section-title {
  position: absolute;
  top: -12px;
  left: -2px;
  background-color: #4fc3f7;
  color: white;
  padding: 4px 10px;
  border-radius: 2px;
  font-size: var(--font-base);
  font-weight: 500;
}

/* 表单输入框 */
.form-cells {
  margin-top: 0;
  background-color: transparent;
}
.form-cell {
  display: flex;
  align-items: flex-start;
  padding: var(--space-md) 0;
  border-bottom: 1px solid var(--c-divider);
}
.form-cell:last-child {
  border-bottom: none;
}
.form-label {
  width: 70px;
  font-size: var(--font-base);
  color: var(--c-text-1);
  line-height: 2;
}
.form-cell__bd {
  flex: 1;
}
.form-input,
.form-select,
.form-textarea {
  font-size: var(--font-base);
  color: var(--c-text-1);
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  box-sizing: border-box;
}
.form-input {
  height: 28px;
}
.form-textarea {
  min-height: 80px;
  padding: var(--space-sm) 0;
  resize: vertical;
}
.form-select {
  width: 100%;
  appearance: none;
  -webkit-appearance: none;
}

.express-submit-wrap {
  padding: var(--space-xl) var(--space-lg) 40px;
}
.btn-submit {
  width: 100%;
  height: 44px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f43f5e;
  color: #fff;
  border-radius: var(--radius-full);
  font-size: var(--font-lg);
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn-submit:active {
  opacity: 0.85;
}
.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hint-text {
  font-size: var(--font-sm);
  color: var(--c-text-3);
  margin-top: 5px;
  line-height: 1.4;
}
</style>
