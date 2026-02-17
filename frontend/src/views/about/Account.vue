<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="article-page">
    <div class="article-header">
      <span class="back-btn" @click="router.back()">返回</span>
      <h2 class="article-title">校园网络账号说明</h2>
    </div>
    <div class="weui-article">
      <p style="text-align:center">《广东二师助手校园网络账号说明》</p>
      <p style="text-align:center">更新日期：2022年6月28日</p>
      <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;广东第二师范学院的同学们，欢迎使用广东第二师范学院校园助手系统。<span style="color: rgb(255, 0, 0);">若你为新生或首次使用本系统，建议你先阅读本使用说明</span>。
      </p>
      <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;使用本系统，需要使用校园网账号的用户名与密码进行登录。<span style="color: rgb(255, 0, 0);">值得注意的是，用户名并不是学号</span>。此外，<span
              style="color: rgb(255, 0, 0);">新生一般需要一周的时间等待网络中心激活学院应用信息系统账号，若你无法登录系统，建议暂时耐心等待</span>。</p>
      <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;查询校园网账号信息的方式如下：</p>
      <p style="text-align: center;">
        <img style="width: 100% !important; height: auto !important; visibility: visible !important;"
             src="/img/about/application/account_1.png">
      </p>
      <p style="text-align: left;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;打开学校主页：www.gdei.edu.cn
        ，在主页右方的导航栏找到"内网登录"，点击进入。</p>
      <p style="text-align: center;">
        <img style="width: 100% !important; height: auto !important; visibility: visible !important;"
             src="/img/about/application/account_2.png"></p>
      <p style="text-align: left;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;在统一身份证认证系统中，点击"查询账号"选项。<br>
      </p>
      <p style="text-align: center;">
        <img style="width: 100% !important; height: auto !important; visibility: visible !important;"
             src="/img/about/application/account_3.png">
      </p>
      <p style="text-align: left;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;若姓名、学工号、身份证号和验证码信息正确无误，页面将显示你的校园网账号信息。其中，<span
              style="color: rgb(255, 0, 0);">用户名则是使用本系统需要填写的账号用户名，2015级后的学生初始密码为身份证后六位数字。由于使用初始密码存在风险，建议你及时修改账号密码。</span>
      </p>
    </div>
  </div>
</template>

<style scoped>
.article-page {
  min-height: 100vh;
  background-color: #fff;
}

.article-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 0 16px;
  z-index: 1000;
}

.back-btn {
  color: #09bb07;
  font-size: 15px;
  cursor: pointer;
  margin-right: 16px;
}

.article-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.weui-article {
  margin-top: 50px;
  padding: 20px 16px;
  line-height: 1.8;
  color: #333;
  font-size: 14px;
}

.weui-article p {
  margin: 12px 0;
}

.weui-article img {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
}
</style>
