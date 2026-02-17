<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({
  title: '',
  type: 1,
  content: ''
})

const mainImageUrl = ref('')
const subImages = ref(['', '', ''])
const dialogVisible = ref(false)
const dialogMessage = ref('')
const toastVisible = ref(false)

const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const showToast = (msg) => {
  dialogMessage.value = msg
  toastVisible.value = true
  setTimeout(() => {
    toastVisible.value = false
  }, 1200)
}

const goBack = () => {
  router.back()
}

const onMainImageChange = (e) => {
  const file = e.target.files?.[0]
  if (file) mainImageUrl.value = URL.createObjectURL(file)
  e.target.value = ''
}

const onSubImageChange = (e, idx) => {
  const file = e.target.files?.[0]
  if (file) subImages.value[idx] = URL.createObjectURL(file)
  e.target.value = ''
}

const clearImages = () => {
  mainImageUrl.value = ''
  subImages.value = ['', '', '']
}

const submit = () => {
  const title = form.value.title?.trim()
  if (!title || !mainImageUrl.value) {
    showDialog('标题和主图不能为空')
    return
  }
  showToast('发布成功')
  setTimeout(() => {
    router.push('/photograph/home')
  }, 1200)
}
</script>

<template>
  <div class="photograph-publish">
    <!-- 统一顶部导航栏 -->
    <div class="unified-header">
      <div class="header-left" @click="goBack">‹</div>
      <div class="header-title">拍好校园</div>
      <div class="header-right"></div>
    </div>

    <!-- 上传表单：参考 upload.jsp 结构 -->
    <section id="upload">
      <div class="am-form-group">
        <label>标题/名字<span class="red-text">*</span> </label>
        <input
          type="text"
          maxlength="25"
          id="title"
          class="am-form-field am-round"
          placeholder="输入照片标题或你的名字"
          v-model="form.title"
        />
      </div>
      <div class="am-form-group">
        <label>照片类型</label>
        <label>
          <input type="radio" name="type" value="1" v-model="form.type" />
          生活照
        </label>
        <label>
          <input type="radio" name="type" value="2" v-model="form.type" />
          校园照
        </label>
      </div>

      <div style="margin-bottom: 10px">
        <i class="am-icon-picture-o"></i>选择主图<span class="red-text">*</span>
      </div>
      <div class="main-upload-box">
        <input type="file" accept="image/*" class="hidden-file-input" @change="onMainImageChange" />
        <div v-if="!mainImageUrl" class="upload-plus">+</div>
        <img v-else :src="mainImageUrl" class="preview-img" alt="主图预览" />
      </div>

      <br />

      <div style="margin-bottom: 10px">
        <i class="am-icon-picture-o"></i>选择副图(选填，可多选，最多三张)
      </div>
      <div class="sub-upload-list">
        <div v-for="(_, idx) in 3" :key="idx" class="sub-upload-box">
          <input type="file" accept="image/*" class="hidden-file-input" @change="onSubImageChange($event, idx)" />
          <div v-if="!subImages[idx]" class="upload-plus">+</div>
          <img v-else :src="subImages[idx]" class="preview-img sub-preview" alt="副图预览" />
        </div>
      </div>

      <br />

      <button class="weui-btn weui-btn_default clear-button" type="button" @click="clearImages">
        <i class="am-icon-trash"></i>清空图片
      </button>

      <div class="am-form-group" id="say-something">
        <label>说点什么吧</label>
        <textarea
          class="am-form-field am-radius"
          rows="4"
          id="content"
          placeholder="选填，可以填写感慨/对大学的期待..."
          v-model="form.content"
        ></textarea>
        <span id="word-count">{{ (form.content || '').length }}/150字</span>
      </div>

      <br />

      <button type="button" class="weui-btn weui-btn_primary submit-button" @click="submit">确认提交</button>
    </section>

    <!-- WEUI 对话框 -->
    <div v-if="dialogVisible">
      <div class="weui-mask" @click="dialogVisible = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd">
          <strong class="weui-dialog__title">提示</strong>
        </div>
        <div class="weui-dialog__bd">{{ dialogMessage }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>

    <!-- WEUI Toast -->
    <div v-if="toastVisible" class="weui-toast-container">
      <div class="weui-mask_transparent"></div>
      <div class="weui-toast">
        <i class="weui-icon-success-no-circle weui-icon_toast"></i>
        <p class="weui-toast__content">{{ dialogMessage }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.photograph-publish {
  min-height: 100vh;
  background: #f8f8f8;
}

/* 统一顶部导航栏 */
.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.header-left {
  padding: 0 10px;
  display: flex;
  align-items: center;
  cursor: pointer;
  min-width: 48px;
  font-size: 24px;
  font-weight: 300 !important;
  color: #333;
}
.header-left i,
.header-left svg,
.header-left img {
  font-size: 24px !important;
  width: 24px !important;
  height: 24px !important;
  font-weight: 300 !important;
  transform: scale(1.4);
  transform-origin: center center;
}
.header-left svg {
  stroke-width: 1 !important;
}
.header-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}
.header-right {
  width: 48px;
  text-align: center;
}

#upload {
  padding: 20px;
}

.am-form-group {
  margin-bottom: 15px;
}
.am-form-field {
  width: 100%;
  box-sizing: border-box;
}
.red-text {
  color: red;
  font-weight: bold;
}

.main-upload-box,
.sub-upload-box {
  position: relative;
  overflow: hidden;
}
.main-upload-box {
  border: 2px dashed #4fc3f7;
  border-radius: 4px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  cursor: pointer;
}
.hidden-file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 10;
}
.upload-plus {
  font-size: 40px;
  color: #ccc;
  z-index: 1;
  pointer-events: none;
}
.main-upload-box .preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 5;
  position: absolute;
  inset: 0;
}

.sub-upload-list {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.sub-upload-box {
  width: 31%;
  aspect-ratio: 1 / 1;
  border: 2px dashed #aed581;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.sub-upload-box .upload-plus {
  font-size: 24px;
}
.sub-upload-box .preview-img.sub-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 5;
  position: absolute;
  inset: 0;
}

.clear-button {
  width: 100%;
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  font-size: 14px;
  cursor: pointer;
}
.clear-button:hover {
  background-color: #e8e8e8;
}

#say-something {
  margin-top: 20px;
}
#word-count {
  float: right;
  font-size: 14px;
  color: #777;
}

.submit-button {
  display: block;
  margin: 0 auto;
  width: 100%;
  background-color: #27ae60;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 12px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}
.submit-button:hover {
  background-color: #229954;
}
.submit-button:active {
  background-color: #1e8449;
}

/* WEUI 对话框样式（简化版） */
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
}
.weui-dialog__hd {
  padding: 1.2em 1.6em 0.5em;
  text-align: center;
}
.weui-dialog__title {
  font-weight: 400;
  font-size: 18px;
}
.weui-dialog__bd {
  padding: 0 1.6em 0.8em;
  min-height: 40px;
  font-size: 15px;
  line-height: 1.5;
  color: #999;
  text-align: center;
}
.weui-dialog__ft {
  position: relative;
  line-height: 42px;
  display: flex;
  border-top: 1px solid #d5d5d6;
}
.weui-dialog__btn {
  flex: 1;
  text-align: center;
  text-decoration: none;
  color: #3cc51f;
  font-size: 17px;
}
.weui-dialog__btn_primary {
  color: #0bb20c;
}
</style>

