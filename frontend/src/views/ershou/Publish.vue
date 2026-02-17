<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const name = ref('')
const description = ref('')
const price = ref('')
const location = ref('')
const typeId = ref(-1)
const qq = ref('')
const phone = ref('')
const images = ref([]) // 图片 Base64 或 File 列表，最多 4 张
const typePickerVisible = ref(false)
const frmErrors = ref({}) // 表单项错误状态
const dialogVisible = ref(false)
const dialogMessage = ref('')

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const typeNames = ['校园代步', '手机', '电脑', '数码配件', '数码', '电器', '运动健身', '衣物伞帽', '图书教材', '租赁', '生活娱乐', '其他']
const typeNameDisplay = computed(() => typeId.value >= 0 && typeId.value <= 11 ? typeNames[typeId.value] : '')

const MAX_IMAGES = 4
const MAX_SIZE = 5 * 1024 * 1024
const ALLOW_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

function triggerFileInput() {
  if (images.value.length >= MAX_IMAGES) return
  document.getElementById('publish_file_input').click()
}

function onFileChange(e) {
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
    showDialog('最多只能选择四张图片！')
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

function submit() {
  if (images.value.length < 1) {
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
  const formData = new FormData()
  formData.append('name', name.value.trim())
  formData.append('description', description.value.trim())
  formData.append('price', price.value)
  formData.append('location', location.value.trim())
  formData.append('type', typeId.value)
  formData.append('qq', qq.value.trim())
  formData.append('phone', phone.value.trim())
  images.value.forEach((item, i) => {
    if (item.file) formData.append('file[]', item.file)
  })
  // 提交时请求 POST /api/ershou/item
  showDialog('发布成功（演示）')
  setTimeout(() => router.push('/ershou/home'), 1500)
}
</script>

<template>
  <div class="ershou-publish body1">
    <!-- 与 Home 一致的灰底统一头部，右侧绿色「完成」按钮 -->
    <div class="unified-header">
      <span class="unified-header__back" @click="router.push('/')">返回</span>
      <h1 class="unified-header__title">发布二手商品</h1>
      <a href="javascript:;" class="unified-header__submit" @click.prevent="submit">完成</a>
    </div>

    <section class="picture">
      <div class="images">
        <div v-for="(img, index) in images" :key="index" class="image">
          <a href="javascript:;" @click.prevent="removeImage(index)"><i class="i iclose"></i></a>
          <i class="img"><img :src="img.dataUrl" alt=""></i>
        </div>
        <span v-if="images.length < MAX_IMAGES" class="addimg" @click="triggerFileInput">
          <i class="i iadd"><i class="i i1"></i><i class="i i2"></i></i>
          <input type="file" accept="image/*" id="publish_file_input" @change="onFileChange">
        </span>
      </div>
      <p class="tip">最多可上传4张图片</p>
    </section>

    <section class="form">
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
.body1 { background: #fff; }
/* 与 Home.vue 一致的灰底统一头部，右侧绿色「完成」 */
.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.unified-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
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
  min-width: 48px;
  text-align: right;
  font-size: 14px;
  color: #3cb395;
  text-decoration: none;
  font-weight: 500;
}
.unified-header__submit:hover {
  color: #2a9d82;
}

.picture {
  background: #3cc2a5;
  border-top: 1px solid #39b89d;
}
.picture .images { padding: 25px 16px 0; margin-bottom: 5px; }
.images .image {
  width: 70px; height: 70px;
  position: relative;
  display: inline-block;
  margin: 0 6px 10px;
  vertical-align: top;
}
.images .image .img { width: 70px; height: 70px; overflow: hidden; display: block; }
.images .image img { width: 100%; display: block; }
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
  background: url(/img/ershou/close.png) no-repeat;
  background-size: 18px;
}
.images .addimg {
  width: 68px; height: 68px;
  border: 2px solid #fff;
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
.picture .tip { height: 24px; line-height: 24px; text-align: center; color: #fff; font-size: 14px; padding-bottom: 3px; }

.form { padding: 0 20px; }
.form .frm {
  position: relative;
  padding-left: 90px;
  font-size: 16px;
  border-bottom: 2px solid #3cc2a5;
  min-height: 70px;
  padding-bottom: 10px;
}
.form .frm .frm-input { resize: none; border: none; background: none; width: 100%; font-size: 16px; color: #202020; padding: 0; margin-top: 14px; }
.form .frmt { position: absolute; left: 0; color: #202020; height: 70px; line-height: 70px; }
.form .frmc { padding-top: 18px; }
.form .frmc input { color: #202020; background: none; height: 34px; line-height: 34px; font-size: 16px; width: 100%; border: none; }
.form .frmc b { height: 34px; line-height: 34px; font-size: 16px; width: 100%; color: #202020; display: block; position: relative; cursor: pointer; }
.form .frmc b .iarrow {
  background: url(/img/ershou/arrow.png) no-repeat;
  width: 8px; height: 12px; background-size: 8px;
  position: absolute; right: 10px; top: 10px;
}
.form .frmtip { position: absolute; bottom: 0; left: 0; background: #fe6a7c; height: 15px; line-height: 15px; font-size: 12px; color: #fff; padding: 0 3px; display: none; }
.form .frmerr { border-color: #fe6a7c; }
.form .frmerr .frmtip { display: block; }

.sky { width: 100%; height: 100%; position: fixed; top: 0; left: 0; display: none; z-index: 9; pointer-events: none; }
.sky.show { display: block; pointer-events: auto; }
.sky .mark { background: #000; opacity: .5; width: 100%; height: 100%; position: absolute; top: 0; display: none; z-index: 99; }
.sky .mark.show { display: block; }
.sky .mw { width: 225px; position: fixed; background: #fff; left: 50%; margin-left: -112px; top: 50%; margin-top: -210px; display: none; z-index: 999; pointer-events: auto; border-radius: 4px; overflow: hidden; }
.sky .mw.show { display: block; }
.sky .mwt { border-top: 4px solid #3cc3a5; height: 40px; border-bottom: 1px solid #eee; position: relative; line-height: 40px; font-size: 16px; color: #333; text-align: center; }
.sky .mwt .mwclose { position: absolute; right: 6px; top: 0; display: block; padding: 10px; }
.sky .mwt .imwclose { background: url(/img/ershou/mwclose.png) no-repeat; background-size: 11px; width: 11px; height: 13px; display: block; }
.sky .mwc ul { padding: 10px 0; }
.sky .mwc li { line-height: 30px; height: 30px; text-align: center; color: #666; font-size: 14px; }
.sky .mwc li a { color: #666; display: block; text-decoration: none; }

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
