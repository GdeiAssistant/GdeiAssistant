<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const agreed = ref(false)
const showConfirmDialog = ref(false)
const deleting = ref(false)

function showToast(message) {
  const toast = document.createElement('div')
  toast.style.cssText =
    'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.75);color:#fff;padding:12px 22px;border-radius:6px;z-index:9999;font-size:14px;max-width:80%;text-align:center;'
  toast.textContent = message
  document.body.appendChild(toast)
  setTimeout(() => {
    if (toast.parentNode) document.body.removeChild(toast)
  }, 2000)
}

function handleDeleteClick() {
  if (!agreed.value) return
  showConfirmDialog.value = true
}

function handleCancel() {
  showConfirmDialog.value = false
}

async function handleConfirmDelete() {
  showConfirmDialog.value = false
  deleting.value = true
  
  try {
    await request.post('/user/delete')
    showToast('账号已注销')
    
    // 清除登录态
    localStorage.clear()
    sessionStorage.clear()
    
    // 延迟跳转，让Toast有时间显示
    setTimeout(() => {
      router.replace('/login')
    }, 1500)
  } catch (e) {
    deleting.value = false
    showToast('注销失败，请重试')
  }
}
</script>

<template>
  <div class="delete-account-page">
    <!-- 统一头部 -->
    <div class="delete-header unified-header">
      <span class="delete-header__back" @click="router.back()">返回</span>
      <h1 class="delete-header__title">注销账户</h1>
      <span class="delete-header__placeholder"></span>
    </div>

    <div class="delete-content">
      <!-- 警告头 -->
      <div class="warning-header">
        <div class="warning-icon">
          <i class="weui-icon-warn weui-icon_msg"></i>
        </div>
        <h2 class="warning-title">将注销您的广东第二师范学院助手账号</h2>
      </div>

      <!-- 风险提示清单 -->
      <div class="risk-card">
        <p class="risk-title">注销后，以下信息将被永久删除且无法恢复：</p>
        <ul class="risk-list">
          <li>账号及所有个人资料将被清空</li>
          <li>表白墙、话题、跑腿等所有发布内容将被永久删除</li>
          <li>所有互动记录（评论、点赞等）将被清除</li>
          <li>自定义课程以及保存的四六级准考证号信息将被删除</li>
          <li>社交功能平台的用户数据和交易记录将被清空</li>
          <li>绑定的手机号、邮箱地址等身份信息将被删除</li>
        </ul>
        <p class="risk-note">注销操作不可逆，请谨慎操作。</p>
      </div>

      <!-- 阅读确认勾选框 -->
      <div class="agreement-section">
        <label class="weui-cell weui-check__label">
          <div class="weui-cell__hd">
            <input
              type="checkbox"
              class="weui-check"
              v-model="agreed"
            />
            <i class="weui-icon-checked"></i>
          </div>
          <div class="weui-cell__bd">
            <p>我已充分阅读并同意上述注销后果</p>
          </div>
        </label>
      </div>

      <!-- 底部操作按钮 -->
      <div class="action-section">
        <div class="weui-btn-area">
          <a
            href="javascript:;"
            class="weui-btn weui-btn_warn"
            :class="{ 'weui-btn_disabled': !agreed || deleting }"
            @click.prevent="handleDeleteClick"
          >
            <span v-if="deleting" class="btn-loading">
              <span class="weui-loading weui-icon_toast"></span>
              <span style="margin-left: 8px;">注销中...</span>
            </span>
            <span v-else>确认注销</span>
          </a>
        </div>
      </div>
    </div>

    <!-- 二次确认弹窗 -->
    <div v-if="showConfirmDialog" class="weui-mask" @click="handleCancel"></div>
    <div v-if="showConfirmDialog" class="weui-dialog">
      <div class="weui-dialog__hd">
        <strong class="weui-dialog__title">最后确认</strong>
      </div>
      <div class="weui-dialog__bd">
        注销操作不可逆，确定要永久删除该账号吗？
      </div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_default" @click="handleCancel">取消</a>
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="handleConfirmDelete">残忍注销</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.delete-account-page {
  background: #f8f8f8;
  min-height: 100vh;
  padding-bottom: 24px;
}

.delete-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}
.delete-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.delete-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.delete-header__placeholder {
  min-width: 48px;
}

.delete-content {
  padding: 0;
}

/* 警告头 */
.warning-header {
  text-align: center;
  padding: 40px 15px 30px;
  background: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}
.warning-icon {
  margin: 40px auto 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.warning-icon .weui-icon-warn.weui-icon_msg {
  font-size: 80px !important;
  line-height: 1;
  color: #fa5151;
  display: block;
  width: 80px;
  height: 80px;
}
.warning-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  line-height: 1.4;
}

/* 风险提示清单 */
.risk-card {
  background: #ffffff;
  padding: 20px 15px;
  margin-top: 10px;
  border-top: 1px solid #e5e5e5;
  border-bottom: 1px solid #e5e5e5;
}
.risk-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin: 0 0 16px;
}
.risk-list {
  list-style: none;
  padding: 0;
  margin: 0 0 16px;
}
.risk-list li {
  font-size: 14px;
  line-height: 1.8;
  color: #666;
  margin-bottom: 8px;
  padding-left: 20px;
  position: relative;
}
.risk-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #666;
  font-weight: bold;
}
.risk-note {
  font-size: 13px;
  color: #fa5151;
  margin: 16px 0 0;
  font-weight: 500;
}

/* 阅读确认勾选框 */
.agreement-section {
  background: #ffffff;
  margin-top: 10px;
  border-top: 1px solid #e5e5e5;
  border-bottom: 1px solid #e5e5e5;
  padding: 15px;
}
.weui-check__label {
  display: flex;
  align-items: flex-start;
  padding: 0;
}
.weui-check__label .weui-cell__hd {
  padding-right: 12px;
  position: relative;
}
.weui-check {
  position: absolute;
  left: -9999px;
}
.weui-icon-checked {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 1px solid #d9d9d9;
  border-radius: 3px;
  background-color: #ffffff;
  position: relative;
  vertical-align: middle;
}
.weui-check:checked + .weui-icon-checked {
  background-color: #07c160;
  border-color: #07c160;
}
.weui-check:checked + .weui-icon-checked::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}
.weui-check__label .weui-cell__bd p {
  font-size: 14px;
  color: #333;
  margin: 0;
  line-height: 1.5;
}

/* 底部操作按钮 */
.action-section {
  margin-top: 20px;
  padding: 0 15px;
}
.weui-btn-area {
  margin-top: 0;
  padding: 0;
}
.weui-btn_warn {
  width: 100%;
  background-color: #fa5151;
  color: #ffffff;
  border-radius: 8px;
  font-size: 17px;
  font-weight: 500;
  padding: 12px 24px;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;
  text-decoration: none;
  cursor: pointer;
}
.weui-btn_warn:active {
  background-color: #e64340;
}
.weui-btn_warn.weui-btn_disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background-color: #ccc;
}
.weui-btn_warn.weui-btn_disabled:active {
  background-color: #ccc;
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 二次确认弹窗 */
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
  background: #ffffff;
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
  padding: 10px 20px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
  line-height: 1.5;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #e5e5e5;
}
.weui-dialog__btn {
  flex: 1;
  padding: 15px 0;
  text-align: center;
  font-size: 17px;
  color: #333;
  text-decoration: none;
  border-right: 1px solid #e5e5e5;
}
.weui-dialog__btn:last-child {
  border-right: none;
}
.weui-dialog__btn_primary {
  color: #fa5151;
  font-weight: 500;
}
</style>
