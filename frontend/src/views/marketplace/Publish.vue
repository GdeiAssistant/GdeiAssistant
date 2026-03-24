<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const { toast, loading: showLoadingToast, hideLoading } = useToast()
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
  showLoadingToast(isEditMode.value ? '正在保存...' : '正在上传...')
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
  <div class="bg-[var(--c-surface)] min-h-screen">
    <CommunityHeader :title="isEditMode ? '编辑二手商品' : '发布二手商品'" moduleColor="#10b981" :showBack="true" :backTo="isEditMode ? '/marketplace/profile' : '/marketplace/home'">
      <template #right>
        <a href="javascript:;" class="text-sm text-emerald-500 no-underline font-medium hover:text-emerald-600" @click.prevent="submit">
          {{ submitting ? '提交中' : (isEditMode ? '保存' : '完成') }}
        </a>
      </template>
    </CommunityHeader>

    <!-- 图片上传区 -->
    <section class="bg-emerald-500 border-t border-emerald-600">
      <div class="px-4 pt-6 mb-1.5">
        <div v-for="(img, index) in images" :key="index" class="w-[70px] h-[70px] relative inline-block mx-1.5 mb-2.5 align-top">
          <a v-if="!isEditMode" href="javascript:;" class="absolute -bottom-3.5 left-1/2 -ml-[19px] w-[18px] h-[18px] p-2.5 block" @click.prevent="removeImage(index)">
            <i class="w-[18px] h-[18px] block bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20352%20512%27%20fill=%27%23fff%27%3E%3Cpath%20d=%27M242.7%20256l100.1-100.1c12.3-12.3%2012.3-32.2%200-44.5l-22.2-22.2c-12.3-12.3-32.2-12.3-44.5%200L176%20189.3%2075.9%2089.2c-12.3-12.3-32.2-12.3-44.5%200L9.2%20111.4c-12.3%2012.3-12.3%2032.2%200%2044.5L109.3%20256%209.2%20356.1c-12.3%2012.3-12.3%2032.2%200%2044.5l22.2%2022.2c12.3%2012.3%2032.2%2012.3%2044.5%200L176%20322.7l100.1%20100.1c12.3%2012.3%2032.2%2012.3%2044.5%200l22.2-22.2c12.3-12.3%2012.3-32.2%200-44.5L242.7%20256z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
          </a>
          <i class="w-[70px] h-[70px] overflow-hidden block rounded"><img :src="img.dataUrl" alt="" class="w-full h-full object-cover block"></i>
        </div>
        <span v-if="!isEditMode && images.length < MAX_IMAGES" class="w-[68px] h-[68px] border-2 border-white rounded inline-block mx-1.5 mb-2.5 align-top relative cursor-pointer" @click="triggerFileInput">
          <i class="absolute w-6 h-6 top-1/2 left-1/2 -mt-3 -ml-3">
            <i class="w-full h-0.5 absolute top-[11px] bg-white block"></i>
            <i class="h-full w-0.5 absolute left-[11px] bg-white block"></i>
          </i>
          <input type="file" accept="image/*" id="publish_file_input" @change="onFileChange" class="absolute top-0 left-0 w-[68px] h-[68px] opacity-0 z-[1] cursor-pointer">
        </span>
      </div>
      <p class="h-6 leading-6 text-center text-white text-sm pb-1">{{ isEditMode ? '编辑模式暂不支持修改商品图片' : '最多可上传4张图片' }}</p>
    </section>

    <!-- 表单 -->
    <section class="px-5">
      <p v-if="pageLoading" class="mt-4 text-[var(--c-text-3)] text-sm text-center">正在加载商品信息...</p>

      <div class="relative pl-[90px] text-base min-h-[70px] pb-2.5" :class="[frmErrors.name ? 'border-b-2 border-[#fe6a7c]' : 'border-b-2 border-emerald-500']">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">商品名称</p>
        <div class="pt-[18px]">
          <input v-model="name" type="text" placeholder="最多25个字" maxlength="25" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
        </div>
        <p class="absolute bottom-0 left-0 bg-[#fe6a7c] h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.name ? 'block' : 'hidden'">商品名称不能为空</p>
      </div>

      <div class="relative pl-[90px] text-base min-h-[70px] pb-2.5" :class="[frmErrors.description ? 'border-b-2 border-[#fe6a7c]' : 'border-b-2 border-emerald-500']">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">商品描述</p>
        <div class="pt-[18px]">
          <textarea v-model="description" name="description" placeholder="填写商品用途、新旧程度" maxlength="100" rows="3" class="resize-none border-none bg-transparent w-full text-base text-[var(--c-text-1)] p-0 mt-3.5 outline-none"></textarea>
        </div>
        <p class="absolute bottom-0 left-0 bg-[#fe6a7c] h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.description ? 'block' : 'hidden'">商品描述不能为空</p>
      </div>

      <div class="relative pl-[90px] text-base min-h-[70px] pb-2.5" :class="[frmErrors.price ? 'border-b-2 border-[#fe6a7c]' : 'border-b-2 border-emerald-500']">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">商品价格</p>
        <div class="pt-[18px]">
          <input v-model="price" type="number" placeholder="金额不能超过9999.99元" step="0.01" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
        </div>
        <p class="absolute bottom-0 left-0 bg-[#fe6a7c] h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.price ? 'block' : 'hidden'">商品价格金额不合法</p>
      </div>

      <div class="relative pl-[90px] text-base min-h-[70px] pb-2.5" :class="[frmErrors.location ? 'border-b-2 border-[#fe6a7c]' : 'border-b-2 border-emerald-500']">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">交易地点</p>
        <div class="pt-[18px]">
          <input v-model="location" type="text" maxlength="30" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
        </div>
        <p class="absolute bottom-0 left-0 bg-[#fe6a7c] h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.location ? 'block' : 'hidden'">交易地点不能为空</p>
      </div>

      <div class="relative pl-[90px] text-base min-h-[70px] pb-2.5" :class="[frmErrors.type ? 'border-b-2 border-[#fe6a7c]' : 'border-b-2 border-emerald-500']">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">选择分类</p>
        <div class="pt-[18px]">
          <b class="h-[34px] leading-[34px] text-base w-full text-[var(--c-text-1)] block relative cursor-pointer" @click="openTypePicker">
            <span>{{ typeNameDisplay || '请选择' }}</span>
            <i class="absolute right-2.5 top-2.5 w-2 h-3 bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20256%20512%27%20fill=%27%2310b981%27%3E%3Cpath%20d=%27M224.3%20273l-136%20136c-9.4%209.4-24.6%209.4-33.9%200l-22.6-22.6c-9.4-9.4-9.4-24.6%200-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6%200-33.9L54.3%20103c9.4-9.4%2024.6-9.4%2033.9%200l136%20136c9.5%209.4%209.5%2024.6.1%2034z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
          </b>
        </div>
        <p class="absolute bottom-0 left-0 bg-[#fe6a7c] h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.type ? 'block' : 'hidden'">分类未选择</p>
      </div>

      <div class="relative pl-[90px] text-base min-h-[70px] pb-2.5" :class="[frmErrors.qq ? 'border-b-2 border-[#fe6a7c]' : 'border-b-2 border-emerald-500']">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">QQ号</p>
        <div class="pt-[18px]">
          <input v-model="qq" type="text" maxlength="20" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
        </div>
        <p class="absolute bottom-0 left-0 bg-[#fe6a7c] h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.qq ? 'block' : 'hidden'">QQ号不能为空</p>
      </div>

      <div class="relative pl-[90px] text-base min-h-[70px] pb-2.5 border-b-2 border-emerald-500">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">手机号</p>
        <div class="pt-[18px]">
          <input v-model="phone" type="text" placeholder="选填" maxlength="11" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
        </div>
      </div>
    </section>

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[var(--c-surface)] rounded-xl w-[280px] z-[1001] shadow-lg overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] pt-5 pb-2">提示</div>
        <div class="text-center text-sm text-[var(--c-text-2)] px-5 pb-5">{{ dialogMessage }}</div>
        <div class="border-t border-[var(--c-border)]">
          <button class="w-full py-3 text-center text-emerald-500 font-medium text-base border-none bg-transparent cursor-pointer" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>

    <!-- Type Picker -->
    <div v-if="typePickerVisible" class="fixed inset-0 z-[900]" @click.self="closeTypePicker">
      <div class="absolute inset-0 bg-black/50 z-[99]" @click="closeTypePicker"></div>
      <div class="w-[225px] fixed bg-[var(--c-surface)] left-1/2 -ml-[112px] top-1/2 -mt-[210px] z-[999] rounded-lg overflow-hidden shadow-lg">
        <div class="border-t-4 border-emerald-500 h-10 border-b border-[var(--c-border)] relative leading-10 text-base text-[var(--c-text-1)] text-center">
          <a href="javascript:;" class="absolute right-1.5 top-0 block p-2.5" @click.prevent="closeTypePicker">
            <i class="block w-[11px] h-[13px] bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20352%20512%27%20fill=%27%23999%27%3E%3Cpath%20d=%27M242.7%20256l100.1-100.1c12.3-12.3%2012.3-32.2%200-44.5l-22.2-22.2c-12.3-12.3-32.2-12.3-44.5%200L176%20189.3%2075.9%2089.2c-12.3-12.3-32.2-12.3-44.5%200L9.2%20111.4c-12.3%2012.3-12.3%2032.2%200%2044.5L109.3%20256%209.2%20356.1c-12.3%2012.3-12.3%2032.2%200%2044.5l22.2%2022.2c12.3%2012.3%2032.2%2012.3%2044.5%200L176%20322.7l100.1%20100.1c12.3%2012.3%2032.2%2012.3%2044.5%200l22.2-22.2c12.3-12.3%2012.3-32.2%200-44.5L242.7%20256z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
          </a>
          <p>选择分类</p>
        </div>
        <div class="py-2.5">
          <ul>
            <li v-for="(label, id) in typeNames" :key="id" class="leading-[30px] h-[30px] text-center text-[var(--c-text-2)] text-sm">
              <a href="javascript:;" class="text-[var(--c-text-2)] block no-underline" @click.prevent="selectType(id)">{{ label }}</a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
