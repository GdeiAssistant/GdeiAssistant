<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const { loading: showLoadingToast, hideLoading } = useToast()
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
  showLoadingToast(isEditMode.value ? '正在保存...' : '正在上传...')
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
  <div class="min-h-screen bg-[var(--c-bg)]">
    <CommunityHeader
      :title="isEditMode ? '编辑信息' : '发布信息'"
      moduleColor="#3b82f6"
      @back="goBack()"
      backTo=""
    >
      <template #right>
        <a href="javascript:;" class="text-sm text-blue-500 no-underline font-medium" @click.prevent="submit">
          {{ submitting ? '提交中' : (isEditMode ? '保存' : '完成') }}
        </a>
      </template>
    </CommunityHeader>

    <div class="p-0">
      <p v-if="pageLoading" class="mt-4 text-center text-[var(--c-text-3)] text-sm">正在加载信息...</p>

      <!-- 寻找类型 -->
      <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)] mx-2.5">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">寻找类型</p>
        <div class="pt-[18px] leading-[34px] text-base w-full">
          <label class="inline-block mr-8 cursor-pointer">
            <input type="radio" name="lostType" :value="0" v-model="formData.type" class="mr-1.5 align-middle w-auto h-auto" />寻物
          </label>
          <label class="inline-block mr-8 cursor-pointer">
            <input type="radio" name="lostType" :value="1" v-model="formData.type" class="mr-1.5 align-middle w-auto h-auto" />寻主
          </label>
        </div>
      </div>

      <!-- 物品分类 -->
      <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)] mx-2.5">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">物品分类</p>
        <div class="h-[70px] flex items-center cursor-pointer relative pt-0" @click="openItemTypePicker">
          <span class="text-[var(--c-text-1)] text-base">{{ itemTypeDisplay || '请选择' }}</span>
          <i class="absolute right-2.5 top-[29px] w-2 h-3 bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20256%20512%27%20fill=%27%233b82f6%27%3E%3Cpath%20d=%27M224.3%20273l-136%20136c-9.4%209.4-24.6%209.4-33.9%200l-22.6-22.6c-9.4-9.4-9.4-24.6%200-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6%200-33.9L54.3%20103c9.4-9.4%2024.6-9.4%2033.9%200l136%20136c9.5%209.4%209.5%2024.6.1%2034z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
        </div>
      </div>

      <!-- 物品名称 -->
      <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)] mx-2.5">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">物品名称</p>
        <div class="pt-[18px]">
          <input type="text" placeholder="最多25个字" v-model="formData.title" maxlength="25" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
        </div>
      </div>

      <!-- 物品描述 -->
      <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)] mx-2.5">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">物品描述</p>
        <div class="pt-[18px]">
          <input type="text" v-model="formData.desc" maxlength="100" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
        </div>
      </div>

      <!-- 地点 -->
      <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)] mx-2.5">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ formData.type === 0 ? '丢失地点' : '捡到地点' }}</p>
        <div class="pt-[18px]">
          <input type="text" v-model="formData.location" maxlength="30" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
        </div>
      </div>

      <!-- Contact tip -->
      <div class="mt-5 text-center text-[#f6383a] text-sm py-2.5">QQ号/微信/手机号任填其中一项即可</div>

      <!-- QQ -->
      <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)] mx-2.5">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">QQ号</p>
        <div class="pt-[18px]">
          <input type="text" v-model="formData.contact.qq" maxlength="20" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
        </div>
      </div>

      <!-- 微信 -->
      <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)] mx-2.5">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">微信</p>
        <div class="pt-[18px]">
          <input type="text" v-model="formData.contact.wechat" maxlength="20" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
        </div>
      </div>

      <!-- 手机号 -->
      <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)] mx-2.5">
        <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">手机号</p>
        <div class="pt-[18px]">
          <input type="tel" v-model="formData.contact.phone" maxlength="11" class="text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
        </div>
      </div>

      <!-- 图片上传区 -->
      <section class="bg-blue-500 border-t border-blue-600 mt-5">
        <div class="px-4 pt-6 mb-1.5">
          <div v-for="(img, index) in formData.images" :key="index" class="w-[70px] h-[70px] relative inline-block mx-1.5 mb-2.5 align-top">
            <a v-if="!isEditMode" href="javascript:;">
              <i class="absolute top-0 right-0 w-[18px] h-[18px] bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20352%20512%27%20fill=%27%23fff%27%3E%3Cpath%20d=%27M242.7%20256l100.1-100.1c12.3-12.3%2012.3-32.2%200-44.5l-22.2-22.2c-12.3-12.3-32.2-12.3-44.5%200L176%20189.3%2075.9%2089.2c-12.3-12.3-32.2-12.3-44.5%200L9.2%20111.4c-12.3%2012.3-12.3%2032.2%200%2044.5L109.3%20256%209.2%20356.1c-12.3%2012.3-12.3%2032.2%200%2044.5l22.2%2022.2c12.3%2012.3%2032.2%2012.3%2044.5%200L176%20322.7l100.1%20100.1c12.3%2012.3%2032.2%2012.3%2044.5%200l22.2-22.2c12.3-12.3%2012.3-32.2%200-44.5L242.7%20256z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain cursor-pointer z-[1]" @click="removeImage(index)"></i>
            </a>
            <i class="w-[70px] h-[70px] overflow-hidden block">
              <img :src="img.previewUrl" alt="预览" class="w-full h-full object-cover" />
            </i>
          </div>
          <span v-if="!isEditMode && formData.images.length < 4" class="w-[68px] h-[68px] border-2 border-white inline-block mx-1.5 mb-2.5 align-top relative">
            <i class="absolute w-6 h-6 top-1/2 left-1/2 -mt-3 -ml-3">
              <i class="w-full h-0.5 absolute top-[11px] bg-white block"></i>
              <i class="h-full w-0.5 absolute left-[11px] bg-white block"></i>
            </i>
            <input type="file" accept="image/*" id="file_input" @change="onFileChange" class="absolute top-0 left-0 w-[68px] h-[68px] opacity-0 z-[1] cursor-pointer" />
          </span>
        </div>
        <p class="h-6 leading-6 text-center text-white text-sm pb-1">{{ isEditMode ? '编辑模式暂不支持修改图片' : '最多可上传4张图片' }}</p>
      </section>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[var(--c-surface)] rounded-xl w-[280px] z-[1001] shadow-lg overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] pt-5 pb-2">提示</div>
        <div class="text-center text-sm text-[var(--c-text-2)] px-5 pb-5">{{ dialogMessage }}</div>
        <div class="border-t border-[var(--c-border)]">
          <button class="w-full py-3 text-center text-blue-500 font-medium text-base border-none bg-transparent cursor-pointer" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>

    <!-- Item Type Picker -->
    <div v-if="itemTypePickerVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="closeItemTypePicker"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[var(--c-surface)] rounded-xl w-[280px] max-w-[320px] z-[1001] shadow-lg overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] pt-5 pb-2">选择物品分类</div>
        <div class="max-h-[280px] overflow-y-auto p-0 text-left">
          <div
            v-for="(label, index) in itemTypeNames"
            :key="label"
            class="py-3.5 px-5 border-t border-[var(--c-border)] text-[var(--c-text-1)] cursor-pointer first:border-t-0 hover:bg-[var(--c-bg)]"
            @click="selectItemType(index)"
          >
            {{ label }}
          </div>
        </div>
        <div class="border-t border-[var(--c-border)]">
          <button class="w-full py-3 text-center text-[var(--c-text-3)] font-medium text-base border-none bg-transparent cursor-pointer" @click="closeItemTypePicker">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>
