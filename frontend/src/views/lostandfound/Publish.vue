<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'

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
    <div class="unified-header">
      <span class="unified-header__back" @click="goBack">返回</span>
      <h1 class="unified-header__title">{{ isEditMode ? '编辑信息' : '发布信息' }}</h1>
      <a href="javascript:;" class="unified-header__submit" @click.prevent="submit">
        {{ submitting ? '提交中' : (isEditMode ? '保存' : '完成') }}
      </a>
    </div>

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
      <div class="weui-mask" @click="dialogVisible = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
        <div class="weui-dialog__bd">{{ dialogMessage }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>

    <div v-if="itemTypePickerVisible">
      <div class="weui-mask" @click="closeItemTypePicker"></div>
      <div class="weui-dialog weui-dialog--list">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">选择物品分类</strong></div>
        <div class="weui-dialog__bd weui-dialog__bd--scroll">
          <div v-for="(label, index) in itemTypeNames" :key="label" class="weui-dialog__item" @click="selectItemType(index)">
            {{ label }}
          </div>
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="closeItemTypePicker">取消</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lostandfound-publish {
  min-height: 100vh;
  background: #e3eeec;
}

.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.unified-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.unified-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.unified-header__submit {
  font-size: 14px;
  color: #3cb395;
  text-decoration: none;
  min-width: 48px;
  text-align: right;
}

.publish-form {
  padding: 0;
  background: #e3eeec;
}

.page-loading {
  margin: 16px 0 0;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.form-frm {
  position: relative;
  padding-left: 90px;
  font-size: 16px;
  border-bottom: 2px solid #3cc2a5;
  min-height: 70px;
  background: #fff;
  margin: 0 10px;
  border-radius: 0;
}
.form-frmt {
  position: absolute;
  left: 0;
  color: #202020;
  height: 70px;
  line-height: 70px;
  padding-left: 15px;
  font-size: 16px;
}
.form-frmc {
  padding-top: 18px;
}
.form-frmc input {
  color: #202020;
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
  color: #202020;
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
  background: #3cc2a5;
  border-top: 1px solid #39b89d;
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
.weui-dialog--list {
  max-width: 320px;
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
  word-wrap: break-word;
  word-break: break-all;
}
.weui-dialog__bd--scroll {
  max-height: 280px;
  overflow-y: auto;
  padding: 0;
}
.weui-dialog__item {
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
  color: #333;
  cursor: pointer;
}
.weui-dialog__item:first-child {
  border-top: none;
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
  color: #3cc395;
  text-decoration: none;
  border-right: 1px solid #d9d9d9;
}
.weui-dialog__btn:last-child {
  border-right: none;
}
.weui-dialog__btn_primary {
  color: #3cc395;
  font-weight: 500;
}
</style>
