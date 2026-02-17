<template>
  <div class="page-container">
    <div class="unified-header">
      <div class="header-left" @click="goBack">返回</div>
      <div class="header-title">实名认证</div>
      <div class="header-right"></div>
    </div>

    <div class="weui-cells__title">请选择您的实名认证证件类型</div>
    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click="handleAuthClick('idcard')">
        <div class="weui-cell__bd"><p>中国大陆居民身份证</p></div>
        <div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click="handleAuthClick('hk_macau')">
        <div class="weui-cell__bd"><p>港澳居民来往内地通行证 (回乡证)</p></div>
        <div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click="handleAuthClick('taiwan')">
        <div class="weui-cell__bd"><p>台湾居民来往大陆通行证 (台胞证)</p></div>
        <div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click="handleAuthClick('passport')">
        <div class="weui-cell__bd"><p>护照</p></div>
        <div class="weui-cell__ft"></div>
      </a>
    </div>

    <div v-if="showDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
        <div class="weui-dialog__bd">为了提供更安全的校园服务，实名认证系统正在接入公安数据接口中，暂未对外开放，敬请期待！</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="showDialog = false">确定</a>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 控制弹窗显示的变量，默认是 false (隐藏)
const showDialog = ref(false);

const goBack = () => {
  router.back();
};

// 点击认证类型时显示提示弹窗
const handleAuthClick = (type) => {
  showDialog.value = true;
};
</script>

<style scoped>
/* 页面基础底色，确保铺满全屏 */
.page-container {
  background-color: #f8f8f8;
  min-height: 100vh;
  box-sizing: border-box;
  overflow: hidden; /* 防止 margin 塌陷 */
}

/* 标准顶部导航栏 */
.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  background-color: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 0 16px;
}

.header-left {
  font-size: 16px;
  color: #333;
  cursor: pointer;
  width: 60px; /* 固定宽度，配合 right 保证中间标题绝对居中 */
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #000;
  flex: 1;
  text-align: center;
  margin: 0;
  padding: 0;
}

.header-right {
  width: 60px; /* 占位用，保持天平平衡 */
}

/* 列表间距微调 */
.weui-cells__title {
  margin-top: 16px;
  margin-bottom: 8px;
  color: #999;
  font-size: 14px;
}

.weui-cells {
  margin-top: 0; /* 消除默认的超大顶部间距 */
  background-color: #fff;
}

/* 补充 WEUI 弹窗的基础样式（以防你的全局 CSS 里漏了这部分） */
.weui-mask {
  position: fixed;
  z-index: 1000;
  top: 0;
  right: 0;
  left: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
}
.weui-dialog {
  position: fixed;
  z-index: 5000;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #fff;
  text-align: center;
  border-radius: 12px;
  overflow: hidden;
  width: 320px;
  max-width: 80%;
}
.weui-dialog__hd {
  padding: 32px 24px 16px;
}
.weui-dialog__title {
  font-weight: 700;
  font-size: 17px;
  line-height: 1.4;
}
.weui-dialog__bd {
  padding: 0 24px 32px;
  font-size: 15px;
  line-height: 1.4;
  color: #666;
}
.weui-dialog__ft {
  position: relative;
  line-height: 56px;
  font-size: 17px;
  display: flex;
}
.weui-dialog__ft:after {
  content: " ";
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  height: 1px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  color: rgba(0, 0, 0, 0.1);
}
.weui-dialog__btn {
  display: block;
  flex: 1;
  color: #07c160;
  text-decoration: none;
  cursor: pointer;
}
.weui-dialog__btn:active {
  background-color: #ececec;
}
</style>