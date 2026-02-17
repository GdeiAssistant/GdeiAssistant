<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const formData = ref({
  type: 'express',
  pickupAddress: '',
  pickupCode: '',
  pickupImage: '',
  deliveryAddress: '',
  contactPhone: '',
  size: 'small',
  description: '',
  reward: ''
})
const pickupImagePreview = ref('')
const pickupImageFile = ref(null)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

const sizeOptions = [
  { label: '小件(外卖/文件)', value: 'small' },
  { label: '中件(鞋服)', value: 'medium' },
  { label: '大件(重物)', value: 'large' }
]

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function onPickupImageChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showDialog('只能上传图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showDialog('图片大小不能超过5MB')
    return
  }
  pickupImageFile.value = file
  pickupImagePreview.value = URL.createObjectURL(file)
}

function removePickupImage() {
  pickupImagePreview.value = ''
  pickupImageFile.value = null
}

function selectSize(size) {
  formData.value.size = size
}

function submit() {
  if (!formData.value.pickupAddress || !formData.value.pickupAddress.trim()) {
    showDialog('请输入取件地点')
    return
  }
  if (!formData.value.deliveryAddress || !formData.value.deliveryAddress.trim()) {
    showDialog('请输入送达地址')
    return
  }
  if (!formData.value.contactPhone || !formData.value.contactPhone.trim()) {
    showDialog('请输入联系人电话')
    return
  }
  const reward = parseFloat(formData.value.reward)
  if (!reward || reward <= 0 || reward > 99) {
    showDialog('请输入有效的跑腿费（0-99元）')
    return
  }

  submitting.value = true
  const payload = new FormData()
  payload.append('type', formData.value.type)
  payload.append('pickupAddress', formData.value.pickupAddress.trim())
  if (formData.value.pickupCode) payload.append('pickupCode', formData.value.pickupCode.trim())
  if (pickupImageFile.value) payload.append('pickupImage', pickupImageFile.value)
  payload.append('deliveryAddress', formData.value.deliveryAddress.trim())
  payload.append('contactPhone', formData.value.contactPhone.trim())
  payload.append('size', formData.value.size)
  if (formData.value.description) payload.append('description', formData.value.description.trim())
  payload.append('reward', reward.toFixed(2))

  request.post('/delivery/publish', payload)
    .then(() => {
      showDialog('发布成功')
      setTimeout(() => router.push('/delivery/home'), 1500)
    })
    .catch(() => { submitting.value = false })
}
</script>

<template>
  <div class="delivery-publish">
    <div class="delivery-publish__header">
      <span class="delivery-publish__cancel" @click="router.back()">取消</span>
      <h1 class="delivery-publish__title">发布任务</h1>
      <button type="button" class="delivery-publish__submit" :disabled="submitting" @click="submit">
        {{ submitting ? '提交中...' : '确认发布' }}
      </button>
    </div>

    <div class="delivery-publish__content">
      <!-- 取件信息区 -->
      <div class="form-block">
        <div class="form-block__title">取件信息</div>
        <div class="form-item">
          <label class="form-label">取件地点</label>
          <input
            type="text"
            class="form-input"
            placeholder="如：菜鸟驿站、南门快递点"
            v-model="formData.pickupAddress"
          />
        </div>
        <div class="form-item">
          <label class="form-label">取件码/凭证（可选）</label>
          <input
            type="text"
            class="form-input"
            placeholder="如：1-2-3456"
            v-model="formData.pickupCode"
          />
        </div>
        <div class="form-item">
          <label class="form-label">取件凭证图片（可选）</label>
          <div v-if="pickupImagePreview" class="image-preview">
            <img :src="pickupImagePreview" />
            <button type="button" class="image-remove" @click="removePickupImage">×</button>
          </div>
          <div v-else class="image-upload-btn" @click="$refs.pickupImageInput.click()">
            <input
              ref="pickupImageInput"
              type="file"
              accept="image/*"
              @change="onPickupImageChange"
              style="display: none;"
            />
            <span class="upload-icon">+</span>
            <span class="upload-text">上传图片</span>
          </div>
        </div>
      </div>

      <!-- 送达信息区 -->
      <div class="form-block">
        <div class="form-block__title">送达信息</div>
        <div class="form-item">
          <label class="form-label">送达地址</label>
          <input
            type="text"
            class="form-input"
            placeholder="如：南苑1栋301"
            v-model="formData.deliveryAddress"
          />
        </div>
        <div class="form-item">
          <label class="form-label">联系人电话</label>
          <input
            type="tel"
            class="form-input"
            placeholder="请输入手机号"
            v-model="formData.contactPhone"
            maxlength="11"
          />
        </div>
      </div>

      <!-- 物品描述区 -->
      <div class="form-block">
        <div class="form-block__title">物品描述</div>
        <div class="form-item">
          <label class="form-label">重量/体积</label>
          <div class="size-tags">
            <button
              v-for="opt in sizeOptions"
              :key="opt.value"
              type="button"
              class="size-tag"
              :class="{ active: formData.size === opt.value }"
              @click="selectSize(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
        <div class="form-item">
          <label class="form-label">备注说明（可选）</label>
          <textarea
            class="form-textarea"
            placeholder="补充说明..."
            v-model="formData.description"
            rows="3"
            maxlength="100"
          ></textarea>
        </div>
      </div>
    </div>

    <!-- 悬浮底部提交区 -->
    <div class="delivery-publish__footer">
      <div class="footer-reward">
        <span class="reward-label">跑腿费：</span>
        <span class="reward-symbol">￥</span>
        <input
          type="number"
          class="reward-input"
          placeholder="0.00"
          v-model="formData.reward"
          step="0.01"
          min="0"
          max="99"
        />
      </div>
      <button type="button" class="footer-submit" :disabled="submitting" @click="submit">
        {{ submitting ? '提交中...' : '确认发布' }}
      </button>
    </div>

    <!-- 提示对话框 -->
    <div v-if="dialogVisible" class="weui-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="weui-dialog">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
      <div class="weui-dialog__bd">{{ dialogMessage }}</div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.delivery-publish {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 80px;
}

.delivery-publish__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 15px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.delivery-publish__cancel {
  color: #666;
  font-size: 14px;
  cursor: pointer;
}
.delivery-publish__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.delivery-publish__submit {
  padding: 6px 20px;
  background: #fa8231;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.3s;
}
.delivery-publish__submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.delivery-publish__content {
  padding: 15px;
}

.form-block {
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
}
.form-block__title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}
.form-item {
  margin-bottom: 15px;
}
.form-item:last-child {
  margin-bottom: 0;
}
.form-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}
.form-input,
.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  font-size: 15px;
  color: #333;
  outline: none;
  box-sizing: border-box;
}
.form-input:focus,
.form-textarea:focus {
  border-color: #fa8231;
}
.form-textarea {
  resize: none;
  min-height: 80px;
}

.image-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f0f0;
}
.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  border: none;
  border-radius: 50%;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #fafafa;
}
.upload-icon {
  font-size: 32px;
  color: #999;
  margin-bottom: 4px;
}
.upload-text {
  font-size: 12px;
  color: #999;
}

.size-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.size-tag {
  padding: 8px 16px;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  background: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}
.size-tag.active {
  background: #fa8231;
  color: #fff;
  border-color: #fa8231;
}

.delivery-publish__footer {
  position: fixed;
  bottom: 60px;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 12px 15px;
  background: #fff;
  border-top: 1px solid #e5e5e5;
  z-index: 100;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.04);
}
.footer-reward {
  display: flex;
  align-items: center;
  margin-right: 15px;
}
.reward-label {
  font-size: 14px;
  color: #666;
  margin-right: 4px;
}
.reward-symbol {
  font-size: 16px;
  color: #ff5252;
  font-weight: bold;
}
.reward-input {
  width: 80px;
  padding: 6px 8px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  font-size: 16px;
  color: #ff5252;
  font-weight: bold;
  outline: none;
  margin-left: 4px;
}
.reward-input:focus {
  border-color: #fa8231;
}
.footer-submit {
  flex: 1;
  height: 44px;
  background: #fa8231;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}
.footer-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.weui-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
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
  padding: 16px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
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
  border-top: 1px solid #eee;
}
.weui-dialog__btn {
  flex: 1;
  padding: 14px;
  text-align: center;
  color: #fa8231;
  text-decoration: none;
  font-weight: 500;
}
</style>
