<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

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
  const payload = {
    name: '代收',
    number: '00000000000',
    phone: formData.value.contactPhone.trim(),
    price: reward,
    company: formData.value.pickupAddress.trim() || '代取快递',
    address: formData.value.deliveryAddress.trim(),
    remarks: formData.value.description?.trim() || ''
  }
  request.post('/delivery/order', payload)
    .then(() => {
      const weui = typeof window !== 'undefined' && window.weui
      if (weui && typeof weui.toast === 'function') weui.toast('发布成功', { duration: 1500 })
      setTimeout(() => router.push('/delivery/home'), 1500)
    })
    .catch(() => { submitting.value = false })
}
</script>

<template>
  <div class="delivery-publish" style="--module-color: #f59e0b">
    <CommunityHeader title="发布任务" moduleColor="#f59e0b" :showBack="false">
      <template #right>
        <button type="button" class="delivery-publish__submit" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中...' : '确认发布' }}
        </button>
      </template>
    </CommunityHeader>

    <div class="delivery-publish__content">
      <!-- 取件信息区 -->
      <div class="form-block community-card">
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
            <button type="button" class="image-remove" @click="removePickupImage">x</button>
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
      <div class="form-block community-card">
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
      <div class="form-block community-card">
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
        <span class="reward-symbol">&#xffe5;</span>
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
    <div v-if="dialogVisible" class="community-dialog-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="community-dialog">
      <div class="community-dialog__title">提示</div>
      <div class="community-dialog__body">{{ dialogMessage }}</div>
      <div class="community-dialog__footer">
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.delivery-publish {
  background: var(--c-bg);
  min-height: 100vh;
  padding-bottom: 80px;
}

.delivery-publish__submit {
  padding: 6px 20px;
  background: var(--c-delivery);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--font-base);
  cursor: pointer;
  transition: opacity 0.3s;
}
.delivery-publish__submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.delivery-publish__content {
  padding: var(--space-lg);
  animation: community-slide-up 0.4s ease both;
}

.form-block {
  padding: var(--space-lg);
  margin-bottom: var(--space-lg);
}
.form-block__title {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--c-text-1);
  margin-bottom: var(--space-lg);
  padding-bottom: 10px;
  border-bottom: 1px solid var(--c-border);
}
.form-item {
  margin-bottom: var(--space-lg);
}
.form-item:last-child {
  margin-bottom: 0;
}
.form-label {
  display: block;
  font-size: var(--font-base);
  color: var(--c-text-2);
  margin-bottom: var(--space-sm);
}
.form-input,
.form-textarea {
  width: 100%;
  padding: var(--space-md);
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-sm);
  font-size: var(--font-md);
  color: var(--c-text-1);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.form-input:focus,
.form-textarea:focus {
  border-color: var(--c-delivery);
}
.form-textarea {
  resize: none;
  min-height: 80px;
}

.image-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--c-border);
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
  border-radius: var(--radius-full);
  font-size: var(--font-xl);
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed var(--c-divider);
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: var(--c-bg);
  transition: border-color 0.2s;
}
.image-upload-btn:active {
  border-color: var(--c-delivery);
}
.upload-icon {
  font-size: 32px;
  color: var(--c-text-3);
  margin-bottom: var(--space-xs);
}
.upload-text {
  font-size: var(--font-sm);
  color: var(--c-text-3);
}

.size-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.size-tag {
  padding: var(--space-sm) var(--space-lg);
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-full);
  background: var(--c-card);
  color: var(--c-text-2);
  font-size: var(--font-base);
  cursor: pointer;
  transition: all 0.3s;
}
.size-tag.active {
  background: var(--c-delivery);
  color: #fff;
  border-color: var(--c-delivery);
}

.delivery-publish__footer {
  position: fixed;
  bottom: 56px;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: var(--space-md) var(--space-lg);
  background: var(--c-card);
  border-top: 1px solid var(--c-border);
  z-index: 100;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.04);
}
.footer-reward {
  display: flex;
  align-items: center;
  margin-right: var(--space-lg);
}
.reward-label {
  font-size: var(--font-base);
  color: var(--c-text-2);
  margin-right: var(--space-xs);
}
.reward-symbol {
  font-size: var(--font-lg);
  color: #ef4444;
  font-weight: bold;
}
.reward-input {
  width: 80px;
  padding: 6px var(--space-sm);
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-sm);
  font-size: var(--font-lg);
  color: #ef4444;
  font-weight: bold;
  outline: none;
  margin-left: var(--space-xs);
  transition: border-color 0.2s;
}
.reward-input:focus {
  border-color: var(--c-delivery);
}
.footer-submit {
  flex: 1;
  height: 44px;
  background: var(--c-delivery);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-lg);
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}
.footer-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
