<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const formData = ref({
  type: 0, // 0: 寻物, 1: 招领
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

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function onFileChange(e) {
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
    reader.onload = (e) => {
      formData.value.images.push(e.target.result)
    }
    reader.readAsDataURL(file)
  })
}

function removeImage(index) {
  formData.value.images.splice(index, 1)
}

function submit() {
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
  if (formData.value.images.length === 0) {
    showDialog('请至少上传一张图片')
    return
  }
  if (!formData.value.contact.qq && !formData.value.contact.wechat && !formData.value.contact.phone) {
    showDialog('联系方式至少需要填写一项')
    return
  }

  // TODO: 提交表单
  console.log('提交表单', formData.value)
  showDialog('发布成功')
  setTimeout(() => {
    router.push('/lostandfound/home')
  }, 1500)
}
</script>

<template>
  <div class="lostandfound-publish">
    <!-- 统一顶部导航栏 -->
    <div class="unified-header">
      <span class="unified-header__back" @click="router.push('/lostandfound/home')">返回</span>
      <h1 class="unified-header__title">发布信息</h1>
      <a href="javascript:;" class="unified-header__submit" @click.prevent="submit">完成</a>
    </div>

    <div class="publish-form">
      <!-- 类型选择：参考原版 publish.jsp 的 .which 结构 -->
      <div class="form-frm">
        <p class="form-frmt">寻找类型</p>
        <div class="form-which">
          <label>
            <input type="radio" name="lostType" value="0" v-model="formData.type" />寻物
          </label>
          <label>
            <input type="radio" name="lostType" value="1" v-model="formData.type" />寻主
          </label>
        </div>
      </div>

      <!-- 物品名称 -->
      <div class="form-frm">
        <p class="form-frmt">物品名称</p>
        <div class="form-frmc">
          <input type="text" placeholder="最多25个字" v-model="formData.title" maxlength="25" />
        </div>
      </div>

      <!-- 物品描述 -->
      <div class="form-frm">
        <p class="form-frmt">物品描述</p>
        <div class="form-frmc">
          <input type="text" v-model="formData.desc" maxlength="100" />
        </div>
      </div>

      <!-- 地点 -->
      <div class="form-frm">
        <p class="form-frmt place">{{ formData.type === 0 ? '丢失地点' : '捡到地点' }}</p>
        <div class="form-frmc">
          <input type="text" v-model="formData.location" maxlength="30" />
        </div>
      </div>

      <!-- 联系方式提示 -->
      <div class="contact-tip">QQ号/微信/手机号任填其中一项即可</div>

      <!-- QQ号 -->
      <div class="form-frm">
        <p class="form-frmt">QQ号</p>
        <div class="form-frmc">
          <input type="text" v-model="formData.contact.qq" maxlength="20" />
        </div>
      </div>

      <!-- 微信 -->
      <div class="form-frm">
        <p class="form-frmt">微信</p>
        <div class="form-frmc">
          <input type="text" v-model="formData.contact.wechat" maxlength="20" />
        </div>
      </div>

      <!-- 手机号 -->
      <div class="form-frm">
        <p class="form-frmt">手机号</p>
        <div class="form-frmc">
          <input type="tel" v-model="formData.contact.phone" maxlength="11" />
        </div>
      </div>

      <!-- 图片上传：参考原版 publish.jsp 的 .picture 结构 -->
      <section class="picture-section">
        <div class="picture-images">
          <div v-for="(img, index) in formData.images" :key="index" class="picture-image">
            <a href="#">
              <i class="i iclose" :id="index" @click="removeImage(index)"></i>
            </a>
            <i class="img">
              <img :src="img" alt="预览" />
            </i>
          </div>
          <span v-if="formData.images.length < 4" class="addimg">
            <i class="i iadd">
              <i class="i i1"></i>
              <i class="i i2"></i>
            </i>
            <input type="file" accept="image/*" id="file_input" @change="onFileChange" />
          </span>
        </div>
        <p class="picture-tip">最多可上传4张图片</p>
      </section>
    </div>

    <!-- WEUI Dialog 对话框 -->
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

/* 表单样式：参考原版 publish.css */
.form-frm {
  position: relative;
  padding-left: 90px;
  font-size: 16px;
  border-bottom: 2px solid #3cc2a5;
  height: 70px;
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

/* 单选框样式：参考原版 .which */
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

/* 图片上传样式：参考原版 publish.css .picture */
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

/* WEUI Dialog 样式 */
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
