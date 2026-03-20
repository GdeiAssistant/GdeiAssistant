<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const name = ref('')
const description = ref('')
const price = ref('')
const location = ref('')
const typeId = ref(-1)
const qq = ref('')
const phone = ref('')
const images = ref([])
const typePickerVisible = ref(false)
const frmErrors = ref({})
const dialogVisible = ref(false)
const dialogMessage = ref('')
const submitting = ref(false)
const pageLoading = ref(false)

const editItemId = computed(() => {
  const value = Number(route.query.id)
  return Number.isInteger(value) && value > 0 ? value : null
})
const isEditMode = computed(() => route.query.edit === '1' && editItemId.value !== null)

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

const typeNames = ['校园代步', '手机', '电脑', '数码配件', '数码', '电器', '运动健身', '衣物伞帽', '图书教材', '租赁', '生活娱乐', '其他']
const typeNameDisplay = computed(() => typeId.value >= 0 && typeId.value <= 11 ? typeNames[typeId.value] : '')

const MAX_IMAGES = 4
const MAX_SIZE = 5 * 1024 * 1024
const ALLOW_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

function goBack() {
  router.push(isEditMode.value ? '/marketplace/profile' : '/marketplace/home')
}

function triggerFileInput() {
  if (isEditMode.value) {
    showDialog('编辑模式暂不支持修改商品图片')
    return
  }
  if (images.value.length >= MAX_IMAGES) return
  const input = document.getElementById('publish_file_input')
  if (input) input.click()
}

function onFileChange(e) {
  if (isEditMode.value) {
    e.target.value = ''
    return
  }
  const files = e.target.files
  if (!files || files.length === 0) return
  const file = files[0]
  if (ALLOW_TYPES.indexOf(file.type) === -1) {
    showDialog('不合法的图片文件类型')
    return
  }
  if (file.size > MAX_SIZE) {
    showDialog('图片文件不能超过5MB')
    return
  }
  if (images.value.length >= MAX_IMAGES) {
    showDialog('最多只能选择四张图片')
    return
  }
  const reader = new FileReader()
  reader.onload = (ev) => {
    images.value = [...images.value, { dataUrl: ev.target.result, file }]
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

function removeImage(index) {
  if (isEditMode.value) {
    showDialog('编辑模式暂不支持修改商品图片')
    return
  }
  images.value = images.value.filter((_, i) => i !== index)
}

function openTypePicker() {
  typePickerVisible.value = true
}

function closeTypePicker() {
  typePickerVisible.value = false
}

function selectType(id) {
  typeId.value = id
  closeTypePicker()
}

function validate() {
  const err = {}
  if (!name.value.trim()) err.name = true
  if (!description.value.trim()) err.description = true
  if (!price.value || parseFloat(price.value) <= 0 || parseFloat(price.value) > 9999.99) err.price = true
  if (!location.value.trim()) err.location = true
  if (typeId.value < 0 || typeId.value > 11) err.type = true
  if (!qq.value.trim()) err.qq = true
  if (phone.value.length > 11) err.phone = true
  frmErrors.value = err
  return Object.keys(err).length === 0
}

function buildPayload() {
  const formData = new FormData()
  formData.append('name', name.value.trim())
  formData.append('description', description.value.trim())
  formData.append('price', price.value)
  formData.append('location', location.value.trim())
  formData.append('type', String(typeId.value))
  formData.append('qq', qq.value.trim())
  formData.append('phone', phone.value.trim())
  return formData
}

function populateForm(item) {
  name.value = item?.name || ''
  description.value = item?.description || ''
  price.value = item?.price != null ? String(item.price) : ''
  location.value = item?.location || ''
  typeId.value = Number.isInteger(item?.type) ? item.type : -1
  qq.value = item?.qq || ''
  phone.value = item?.phone || ''
  images.value = Array.isArray(item?.pictureURL) ? item.pictureURL.map((url) => ({ dataUrl: url, readonly: true })) : []
}

async function loadEditItem() {
  if (!isEditMode.value) return
  pageLoading.value = true
  try {
    const res = await request.get('/marketplace/profile')
    const data = res?.data || {}
    const list = []
      .concat(Array.isArray(data.doing) ? data.doing : [])
      .concat(Array.isArray(data.sold) ? data.sold : [])
      .concat(Array.isArray(data.off) ? data.off : [])
    const item = list.find((entry) => Number(entry.id) === editItemId.value)
    if (!item) {
      showDialog('未找到要编辑的商品')
      return
    }
    populateForm(item)
  } finally {
    pageLoading.value = false
  }
}

async function submit() {
  if (submitting.value || pageLoading.value) return
  if (!isEditMode.value && images.value.length < 1) {
    showDialog('请至少选择一张图片')
    return
  }
  if (!name.value || name.value.trim() === '') {
    showDialog('请填写商品名称')
    return
  }
  if (!description.value || description.value.trim() === '') {
    showDialog('请填写商品描述')
    return
  }
  if (!price.value || parseFloat(price.value) <= 0 || parseFloat(price.value) > 9999.99) {
    showDialog('请填写有效的商品价格（0-9999.99元）')
    return
  }
  if (!location.value || location.value.trim() === '') {
    showDialog('请填写交易地点')
    return
  }
  if (typeId.value === null || typeId.value < 0 || typeId.value > 11) {
    showDialog('请选择商品分类')
    return
  }
  if (!qq.value || qq.value.trim() === '') {
    showDialog('请填写QQ号')
    return
  }
  if (!validate()) {
    showDialog('请检查表单内容')
    return
  }
  submitting.value = true
  showLoading(isEditMode.value ? '正在保存...' : '正在上传...')
  try {
    const formData = buildPayload()
    if (isEditMode.value) {
      await request.post(`/marketplace/item/id/${editItemId.value}`, formData)
      hideLoading()
      showDialog('保存成功')
      setTimeout(() => router.push('/marketplace/profile'), 1200)
      return
    }
    const imageKeys = await uploadFilesByPresignedUrl(images.value.map(item => item.file).filter(Boolean))
    imageKeys.forEach((imageKey) => formData.append('imageKeys', imageKey))
    await request.post('/marketplace/item', formData)
    hideLoading()
    showDialog('发布成功')
    setTimeout(() => router.push('/marketplace/home'), 1500)
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
  <div class="ershou-publish body1">
    <CommunityHeader :title="isEditMode ? '编辑二手商品' : '发布二手商品'" moduleColor="#10b981" :showBack="true" :backTo="isEditMode ? '/marketplace/profile' : '/marketplace/home'">
      <template #right>
        <a href="javascript:;" class="header-submit" @click.prevent="submit">
          {{ submitting ? '提交中' : (isEditMode ? '保存' : '完成') }}
        </a>
      </template>
    </CommunityHeader>

    <section class="picture">
      <div class="images">
        <div v-for="(img, index) in images" :key="index" class="image">
          <a v-if="!isEditMode" href="javascript:;" @click.prevent="removeImage(index)"><i class="i iclose"></i></a>
          <i class="img"><img :src="img.dataUrl" alt=""></i>
        </div>
        <span v-if="!isEditMode && images.length < MAX_IMAGES" class="addimg" @click="triggerFileInput">
          <i class="i iadd"><i class="i i1"></i><i class="i i2"></i></i>
          <input type="file" accept="image/*" id="publish_file_input" @change="onFileChange">
        </span>
      </div>
      <p class="tip">{{ isEditMode ? '编辑模式暂不支持修改商品图片' : '最多可上传4张图片' }}</p>
    </section>

    <section class="form">
      <p v-if="pageLoading" class="form-loading">正在加载商品信息...</p>
      <div class="frm" :class="{ frmerr: frmErrors.name }">
        <p class="frmt">商品名称</p>
        <div class="frmc">
          <input v-model="name" type="text" placeholder="最多25个字" maxlength="25">
        </div>
        <p class="frmtip">商品名称不能为空</p>
      </div>
      <div class="frm" :class="{ frmerr: frmErrors.description }">
        <p class="frmt">商品描述</p>
        <div class="frmc">
          <textarea v-model="description" name="description" placeholder="填写商品用途、新旧程度" maxlength="100" rows="3" class="frm-input"></textarea>
        </div>
        <p class="frmtip">商品描述不能为空</p>
      </div>
      <div class="frm" :class="{ frmerr: frmErrors.price }">
        <p class="frmt">商品价格</p>
        <div class="frmc">
          <input v-model="price" type="number" placeholder="金额不能超过9999.99元" step="0.01">
        </div>
        <p class="frmtip">商品价格金额不合法</p>
      </div>
      <div class="frm" :class="{ frmerr: frmErrors.location }">
        <p class="frmt">交易地点</p>
        <div class="frmc">
          <input v-model="location" type="text" maxlength="30">
        </div>
        <p class="frmtip">交易地点不能为空</p>
      </div>
      <div class="frm" :class="{ frmerr: frmErrors.type }">
        <p class="frmt select">选择分类</p>
        <div class="frmc">
          <b id="selectType" @click="openTypePicker">
            <span class="selectvalue">{{ typeNameDisplay || '请选择' }}</span>
            <i class="i iarrow"></i>
          </b>
        </div>
        <p class="frmtip">分类未选择</p>
      </div>
      <div class="frm" :class="{ frmerr: frmErrors.qq }">
        <p class="frmt">QQ号</p>
        <div class="frmc">
          <input v-model="qq" type="text" maxlength="20">
        </div>
        <p class="frmtip">QQ号不能为空</p>
      </div>
      <div class="frm" :class="{ frmerr: frmErrors.phone }">
        <p class="frmt">手机号</p>
        <div class="frmc">
          <input v-model="phone" type="text" placeholder="选填" maxlength="11">
        </div>
      </div>
    </section>

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

    <div class="sky" :class="{ show: typePickerVisible }" @click.self="closeTypePicker">
      <div class="mark" :class="{ show: typePickerVisible }" @click="closeTypePicker"></div>
      <div class="mw typemw" :class="{ show: typePickerVisible }">
        <div class="mwt">
          <a href="javascript:;" class="mwclose" @click.prevent="closeTypePicker"><i class="i imwclose"></i></a>
          <p>选择分类</p>
        </div>
        <div class="mwc">
          <ul>
            <li v-for="(label, id) in typeNames" :key="id"><a href="javascript:;" @click.prevent="selectType(id)">{{ label }}</a></li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.body1 { background: var(--c-card); }

.header-submit {
  min-width: 48px;
  text-align: right;
  font-size: var(--font-base);
  color: var(--c-ershou);
  text-decoration: none;
  font-weight: 500;
}
.header-submit:hover {
  color: #059669;
}

.picture {
  background: #10b981;
  border-top: 1px solid #0d9668;
}
.picture .images { padding: 25px 16px 0; margin-bottom: 5px; }
.images .image {
  width: 70px; height: 70px;
  position: relative;
  display: inline-block;
  margin: 0 6px 10px;
  vertical-align: top;
}
.images .image .img { width: 70px; height: 70px; overflow: hidden; display: block; border-radius: var(--radius-sm); }
.images .image img { width: 100%; height: 100%; object-fit: cover; display: block; }
.images .image a {
  display: block;
  position: absolute;
  bottom: -14px; left: 50%;
  margin-left: -19px;
  width: 18px; height: 18px;
  padding: 10px;
}
.images .image .iclose {
  width: 18px; height: 18px;
  background: url(/img/marketplace/close.png) no-repeat;
  background-size: 18px;
}
.images .addimg {
  width: 68px; height: 68px;
  border: 2px solid #fff;
  border-radius: var(--radius-sm);
  display: inline-block;
  margin: 0 6px 10px;
  vertical-align: top;
  position: relative;
  cursor: pointer;
}
#publish_file_input {
  position: absolute;
  top: 0; left: 0;
  width: 68px; height: 68px;
  opacity: 0;
  z-index: 1;
  cursor: pointer;
}
.images .addimg .iadd {
  position: absolute;
  width: 24px; height: 24px;
  top: 50%; left: 50%;
  margin: -12px 0 0 -12px;
}
.images .addimg .iadd .i1 { width: 100%; height: 2px; position: absolute; top: 11px; background: #fff; }
.images .addimg .iadd .i2 { height: 100%; width: 2px; position: absolute; left: 11px; background: #fff; }
.picture .tip { height: 24px; line-height: 24px; text-align: center; color: #fff; font-size: var(--font-base); padding-bottom: 3px; }

.form { padding: 0 20px; }
.form-loading {
  margin: 16px 0 0;
  color: var(--c-text-3);
  font-size: var(--font-base);
  text-align: center;
}
.form .frm {
  position: relative;
  padding-left: 90px;
  font-size: var(--font-lg);
  border-bottom: 2px solid var(--c-ershou);
  min-height: 70px;
  padding-bottom: 10px;
}
.form .frm .frm-input { resize: none; border: none; background: none; width: 100%; font-size: var(--font-lg); color: var(--c-text-1); padding: 0; margin-top: 14px; }
.form .frmt { position: absolute; left: 0; color: var(--c-text-1); height: 70px; line-height: 70px; }
.form .frmc { padding-top: 18px; }
.form .frmc input { color: var(--c-text-1); background: none; height: 34px; line-height: 34px; font-size: var(--font-lg); width: 100%; border: none; }
.form .frmc b { height: 34px; line-height: 34px; font-size: var(--font-lg); width: 100%; color: var(--c-text-1); display: block; position: relative; cursor: pointer; }
.form .frmc b .iarrow {
  background: url(/img/marketplace/arrow.png) no-repeat;
  width: 8px; height: 12px; background-size: 8px;
  position: absolute; right: 10px; top: 10px;
}
.form .frmtip { position: absolute; bottom: 0; left: 0; background: #fe6a7c; height: 15px; line-height: 15px; font-size: var(--font-sm); color: #fff; padding: 0 3px; display: none; }
.form .frmerr { border-color: #fe6a7c; }
.form .frmerr .frmtip { display: block; }

.sky { width: 100%; height: 100%; position: fixed; top: 0; left: 0; display: none; z-index: 9; pointer-events: none; }
.sky.show { display: block; pointer-events: auto; }
.sky .mark { background: #000; opacity: .5; width: 100%; height: 100%; position: absolute; top: 0; display: none; z-index: 99; }
.sky .mark.show { display: block; }
.sky .mw { width: 225px; position: fixed; background: var(--c-card); left: 50%; margin-left: -112px; top: 50%; margin-top: -210px; display: none; z-index: 999; pointer-events: auto; border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-lg); }
.sky .mw.show { display: block; }
.sky .mwt { border-top: 4px solid var(--c-ershou); height: 40px; border-bottom: 1px solid var(--c-divider); position: relative; line-height: 40px; font-size: var(--font-lg); color: var(--c-text-1); text-align: center; }
.sky .mwt .mwclose { position: absolute; right: 6px; top: 0; display: block; padding: 10px; }
.sky .mwt .imwclose { background: url(/img/marketplace/mwclose.png) no-repeat; background-size: 11px; width: 11px; height: 13px; display: block; }
.sky .mwc ul { padding: 10px 0; }
.sky .mwc li { line-height: 30px; height: 30px; text-align: center; color: var(--c-text-2); font-size: var(--font-base); }
.sky .mwc li a { color: var(--c-text-2); display: block; text-decoration: none; }
</style>
