<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const formData = ref({
  type: 0,
  itemType: -1,
  title: '',
  desc: '',
  location: '',
  contact: {
    qq: '',
    wechat: '',
    phone: ''
  },
  images: []
})
const dialogVisible = ref(false)
const dialogMessage = ref('')
const submitting = ref(false)
const itemTypePickerVisible = ref(false)
const pageLoading = ref(false)

const editItemId = computed(() => {
  const value = Number(route.query.id)
  return Number.isInteger(value) && value > 0 ? value : null
})
const isEditMode = computed(() => route.query.edit === '1' && editItemId.value !== null)
const itemTypeNames = ['手机', '校园卡', '身份证', '银行卡', '书', '钥匙', '包包', '衣帽', '校园代步', '运动健身', '数码配件', '其他']
const itemTypeDisplay = computed(() => formData.value.itemType >= 0 && formData.value.itemType <= 11
  ? itemTypeNames[formData.value.itemType]
  : '')

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function showLoading(text = '正在上传...') {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') weui.loading(text)
}

function hideLoading() {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.hideLoading === 'function') weui.hideLoading()
}

function goBack() {
  router.push(isEditMode.value ? '/lostandfound/profile' : '/lostandfound/home')
}

function openItemTypePicker() {
  itemTypePickerVisible.value = true
}

function closeItemTypePicker() {
  itemTypePickerVisible.value = false
}

function selectItemType(index) {
  formData.value.itemType = index
  closeItemTypePicker()
}

function onFileChange(e) {
  if (isEditMode.value) {
    e.target.value = ''
    return
  }
  const files = Array.from(e.target.files)
  if (files.length + formData.value.images.length > 4) {
    showDialog('最多只能上传4张图片')
    return
  }
  files.forEach(file => {
    if (!file.type.startsWith('image/')) {
      showDialog('只能上传图片文件')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      showDialog('图片大小不能超过5MB')
      return
    }
    const reader = new FileReader()
    reader.onload = (event) => {
      formData.value.images.push({
        previewUrl: event.target.result,
        file
      })
    }
    reader.readAsDataURL(file)
  })
  e.target.value = ''
}

function removeImage(index) {
  if (isEditMode.value) {
    showDialog('编辑模式暂不支持修改图片')
    return
  }
  formData.value.images.splice(index, 1)
}

function populateForm(item) {
  formData.value.type = Number.isInteger(item?.lostType) ? item.lostType : 0
  formData.value.itemType = Number.isInteger(item?.itemType) ? item.itemType : -1
  formData.value.title = item?.name || ''
  formData.value.desc = item?.description || ''
  formData.value.location = item?.location || ''
  formData.value.contact.qq = item?.qq || ''
  formData.value.contact.wechat = item?.wechat || ''
  formData.value.contact.phone = item?.phone || ''
  formData.value.images = Array.isArray(item?.pictureURL) ? item.pictureURL.map((url) => ({
    previewUrl: url,
    readonly: true
  })) : []
}

async function loadEditItem() {
  if (!isEditMode.value) return
  pageLoading.value = true
  try {
    const res = await request.get('/lostandfound/profile')
    const data = res?.data || {}
    const list = []
      .concat(Array.isArray(data.lost) ? data.lost : [])
      .concat(Array.isArray(data.found) ? data.found : [])
      .concat(Array.isArray(data.didfound) ? data.didfound : [])
    const item = list.find((entry) => Number(entry.id) === editItemId.value)
    if (!item) {
      showDialog('未找到要编辑的信息')
      return
    }
    populateForm(item)
  } finally {
    pageLoading.value = false
  }
}

function buildPayload() {
  const payload = new FormData()
  payload.append('name', formData.value.title.trim())
  payload.append('description', formData.value.desc.trim())
  payload.append('location', formData.value.location.trim())
  payload.append('lostType', String(formData.value.type))
  payload.append('itemType', String(formData.value.itemType))
  if (formData.value.contact.qq) payload.append('qq', formData.value.contact.qq.trim())
  if (formData.value.contact.wechat) payload.append('wechat', formData.value.contact.wechat.trim())
  if (formData.value.contact.phone) payload.append('phone', formData.value.contact.phone.trim())
  return payload
}

async function submit() {
  if (submitting.value || pageLoading.value) return
  if (!formData.value.title || formData.value.title.trim() === '') {
    showDialog('请填写物品名称')
    return
  }
  if (formData.value.title.length > 25) {
    showDialog('物品名称不能超过25个字符')
    return
  }
  if (!formData.value.desc || formData.value.desc.trim() === '') {
    showDialog('请填写物品描述')
    return
  }
  if (formData.value.desc.length > 100) {
    showDialog('物品描述不能超过100个字符')
    return
  }
  if (!formData.value.location || formData.value.location.trim() === '') {
    showDialog('请填写地点')
    return
  }
  if (formData.value.location.length > 30) {
    showDialog('地点不能超过30个字符')
    return
  }
  if (formData.value.itemType < 0 || formData.value.itemType > 11) {
    showDialog('请选择物品分类')
    return
  }
  if (!isEditMode.value && formData.value.images.length === 0) {
    showDialog('请至少上传一张图片')
    return
  }
  if (!formData.value.contact.qq && !formData.value.contact.wechat && !formData.value.contact.phone) {
    showDialog('联系方式至少需要填写一项')
    return
  }
  submitting.value = true
  showLoading(isEditMode.value ? '正在保存...' : '正在上传...')
  try {
    const payload = buildPayload()
    if (isEditMode.value) {
      await request.post(`/lostandfound/item/id/${editItemId.value}`, payload)
      hideLoading()
      showDialog('保存成功')
      setTimeout(() => {
        router.push('/lostandfound/profile')
      }, 1200)
      return
    }
    const imageKeys = await uploadFilesByPresignedUrl(formData.value.images.map(item => item.file).filter(Boolean))
    imageKeys.forEach((imageKey) => payload.append('imageKeys', imageKey))
    await request.post('/lostandfound/item', payload)
    hideLoading()
    showDialog('发布成功')
    setTimeout(() => {
      router.push('/lostandfound/home')
    }, 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}

onMounted(() => {
  loadEditItem()
})
</script>

<template>
  <div class="lostandfound-publish">
    <CommunityHeader
      :title="isEditMode ? '编辑信息' : '发布信息'"
      moduleColor="#3b82f6"
      @back="goBack()"
      backTo=""
    >
      <template #right>
        <a href="javascript:;" class="publish-submit-btn" @click.prevent="submit">
          {{ submitting ? '提交中' : (isEditMode ? '保存' : '完成') }}
        </a>
      </template>
    </CommunityHeader>

    <div class="publish-form">
      <p v-if="pageLoading" class="page-loading">正在加载信息...</p>

      <div class="form-frm">
        <p class="form-frmt">寻找类型</p>
        <div class="form-which">
          <label>
            <input type="radio" name="lostType" :value="0" v-model="formData.type" />寻物
          </label>
          <label>
            <input type="radio" name="lostType" :value="1" v-model="formData.type" />寻主
          </label>
        </div>
      </div>

      <div class="form-frm">
        <p class="form-frmt">物品分类</p>
        <div class="form-frmc form-frmc--select" @click="openItemTypePicker">
          <span class="select-value">{{ itemTypeDisplay || '请选择' }}</span>
          <i class="select-arrow"></i>
        </div>
      </div>

      <div class="form-frm">
        <p class="form-frmt">物品名称</p>
        <div class="form-frmc">
          <input type="text" placeholder="最多25个字" v-model="formData.title" maxlength="25" />
        </div>
      </div>

      <div class="form-frm">
        <p class="form-frmt">物品描述</p>
        <div class="form-frmc">
          <input type="text" v-model="formData.desc" maxlength="100" />
        </div>
      </div>

      <div class="form-frm">
        <p class="form-frmt place">{{ formData.type === 0 ? '丢失地点' : '捡到地点' }}</p>
        <div class="form-frmc">
          <input type="text" v-model="formData.location" maxlength="30" />
        </div>
      </div>

      <div class="contact-tip">QQ号/微信/手机号任填其中一项即可</div>

      <div class="form-frm">
        <p class="form-frmt">QQ号</p>
        <div class="form-frmc">
          <input type="text" v-model="formData.contact.qq" maxlength="20" />
        </div>
      </div>

      <div class="form-frm">
        <p class="form-frmt">微信</p>
        <div class="form-frmc">
          <input type="text" v-model="formData.contact.wechat" maxlength="20" />
        </div>
      </div>

      <div class="form-frm">
        <p class="form-frmt">手机号</p>
        <div class="form-frmc">
          <input type="tel" v-model="formData.contact.phone" maxlength="11" />
        </div>
      </div>

      <section class="picture-section">
        <div class="picture-images">
          <div v-for="(img, index) in formData.images" :key="index" class="picture-image">
            <a v-if="!isEditMode" href="javascript:;">
              <i class="i iclose" :id="index" @click="removeImage(index)"></i>
            </a>
            <i class="img">
              <img :src="img.previewUrl" alt="预览" />
            </i>
          </div>
          <span v-if="!isEditMode && formData.images.length < 4" class="addimg">
            <i class="i iadd">
              <i class="i i1"></i>
              <i class="i i2"></i>
            </i>
            <input type="file" accept="image/*" id="file_input" @change="onFileChange" />
          </span>
        </div>
        <p class="picture-tip">{{ isEditMode ? '编辑模式暂不支持修改图片' : '最多可上传4张图片' }}</p>
      </section>
    </div>

    <div v-if="dialogVisible">
      <div class="community-dialog-mask" @click="dialogVisible = false"></div>
      <div class="community-dialog" style="--module-color: #3b82f6">
        <div class="community-dialog__title">提示</div>
        <div class="community-dialog__body">{{ dialogMessage }}</div>
        <div class="community-dialog__footer">
          <button class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>

    <div v-if="itemTypePickerVisible">
      <div class="community-dialog-mask" @click="closeItemTypePicker"></div>
      <div class="community-dialog community-dialog--list" style="--module-color: #3b82f6">
        <div class="community-dialog__title">选择物品分类</div>
        <div class="community-dialog__body community-dialog__body--scroll">
          <div v-for="(label, index) in itemTypeNames" :key="label" class="community-dialog__item" @click="selectItemType(index)">
            {{ label }}
          </div>
        </div>
        <div class="community-dialog__footer">
          <button class="community-dialog__btn community-dialog__btn--cancel" @click="closeItemTypePicker">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lostandfound-publish {
  min-height: 100vh;
  background: var(--c-bg);
}

.publish-submit-btn {
  font-size: 14px;
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
}

.publish-form {
  padding: 0;
}

.page-loading {
  margin: 16px 0 0;
  text-align: center;
  color: var(--c-text-3);
  font-size: 14px;
}

.form-frm {
  position: relative;
  padding-left: 90px;
  font-size: 16px;
  border-bottom: 1px solid var(--c-divider);
  min-height: 70px;
  background: var(--c-card);
  margin: 0 10px;
  border-radius: 0;
}
.form-frmt {
  position: absolute;
  left: 0;
  color: var(--c-text-1);
  height: 70px;
  line-height: 70px;
  padding-left: 15px;
  font-size: 16px;
}
.form-frmc {
  padding-top: 18px;
}
.form-frmc input {
  color: var(--c-text-1);
  background: none;
  height: 34px;
  line-height: 34px;
  font-size: 16px;
  width: 100%;
  border: none;
  outline: none;
}
.form-frmc--select {
  height: 70px;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
  padding-top: 0;
}
.select-value {
  color: var(--c-text-1);
  font-size: 16px;
}
.select-arrow {
  position: absolute;
  right: 10px;
  top: 29px;
  width: 8px;
  height: 12px;
  background: url(/img/ershou/arrow.png) no-repeat;
  background-size: 8px;
}

.form-which {
  padding-top: 18px;
  line-height: 34px;
  font-size: 16px;
  width: 100%;
}
.form-which label {
  display: inline-block;
  margin-right: 2rem;
  cursor: pointer;
}
.form-which input[type="radio"] {
  margin-right: 5px;
  width: auto;
  height: auto;
  vertical-align: middle;
}

.contact-tip {
  margin-top: 20px;
  text-align: center;
  color: #f6383a;
  font-size: 14px;
  padding: 10px;
}

.picture-section {
  background: #3b82f6;
  border-top: 1px solid #2563eb;
  margin-top: 20px;
}
.picture-images {
  padding: 25px 16px 0;
  margin-bottom: 5px;
}
.picture-image {
  width: 70px;
  height: 70px;
  position: relative;
  display: inline-block;
  margin: 0 6px 10px;
  vertical-align: top;
}
.picture-image .img {
  width: 70px;
  height: 70px;
  overflow: hidden;
  display: block;
}
.picture-image .img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.picture-image .iclose {
  position: absolute;
  top: 0;
  right: 0;
  width: 18px;
  height: 18px;
  background: url(/img/lostandfound/close.png) no-repeat;
  background-size: 18px;
  cursor: pointer;
  z-index: 1;
}
.addimg {
  width: 68px;
  height: 68px;
  border: 2px solid #fff;
  display: inline-block;
  margin: 0 6px 10px;
  vertical-align: top;
  position: relative;
}
.addimg #file_input {
  position: absolute;
  top: 0;
  left: 0;
  width: 68px;
  height: 68px;
  opacity: 0;
  z-index: 1;
  cursor: pointer;
}
.addimg .iadd {
  position: absolute;
  width: 24px;
  height: 24px;
  top: 50%;
  left: 50%;
  margin: -12px 0 0 -12px;
}
.addimg .iadd .i1 {
  width: 100%;
  height: 2px;
  position: absolute;
  top: 11px;
  background: #fff;
}
.addimg .iadd .i2 {
  height: 100%;
  width: 2px;
  position: absolute;
  left: 11px;
  background: #fff;
}
.picture-tip {
  height: 24px;
  line-height: 24px;
  text-align: center;
  color: #fff;
  font-size: 14px;
  padding-bottom: 3px;
}

/* Dialog overrides for picker list */
.community-dialog--list {
  max-width: 320px;
}
.community-dialog__body--scroll {
  max-height: 280px;
  overflow-y: auto;
  padding: 0;
  text-align: left;
}
.community-dialog__item {
  padding: 14px 20px;
  border-top: 1px solid var(--c-border);
  color: var(--c-text-1);
  cursor: pointer;
}
.community-dialog__item:first-child {
  border-top: none;
}
</style>
