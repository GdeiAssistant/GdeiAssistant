<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-[var(--color-surface)]">
    <!-- Sticky header -->
    <div class="sticky top-0 left-0 right-0 h-[50px] bg-[var(--color-surface)] shadow-md flex items-center px-4 z-[1000]">
      <span class="text-[var(--color-primary)] text-[15px] cursor-pointer mr-4" @click="router.back()">返回</span>
      <h2 class="flex-1 text-center text-lg font-medium text-[var(--c-text-1)] m-0">校园网络账号说明</h2>
      <span class="w-10"></span>
    </div>

    <!-- Content -->
    <div class="max-w-2xl mx-auto px-4 py-6">
      <div class="bg-[var(--color-surface)] rounded-xl p-4 shadow-sm">
        <div class="text-sm leading-relaxed text-[var(--c-text-2)] [&_p]:my-3 [&_img]:max-w-full [&_img]:h-auto [&_img]:my-4">
          <p class="text-center">《广东二师助手校园网络账号说明》</p>
          <p class="text-center">更新日期：2022年6月28日</p>
          <p class="indent-8">广东第二师范学院的同学们，欢迎使用广东第二师范学院校园助手系统。<span class="text-red-500">若你为新生或首次使用本系统，建议你先阅读本使用说明</span>。
          </p>
          <p class="indent-8">使用本系统，需要使用校园网账号的用户名与密码进行登录。<span class="text-red-500">值得注意的是，用户名并不是学号</span>。此外，<span
                  class="text-red-500">新生一般需要一周的时间等待网络中心激活学院应用信息系统账号，若你无法登录系统，建议暂时耐心等待</span>。</p>
          <p class="indent-8">查询校园网账号信息的方式如下：</p>
          <p class="text-center">
            <img class="w-full h-auto"
                 src="/img/about/application/account_1.png">
          </p>
          <p class="text-left indent-8">打开学校主页：www.gdei.edu.cn
            ，在主页右方的导航栏找到"内网登录"，点击进入。</p>
          <p class="text-center">
            <img class="w-full h-auto"
                 src="/img/about/application/account_2.png"></p>
          <p class="text-left indent-8">在统一身份证认证系统中，点击"查询账号"选项。<br>
          </p>
          <p class="text-center">
            <img class="w-full h-auto"
                 src="/img/about/application/account_3.png">
          </p>
          <p class="text-left indent-8">若姓名、学工号、身份证号和验证码信息正确无误，页面将显示你的校园网账号信息。其中，<span
                  class="text-red-500">用户名则是使用本系统需要填写的账号用户名，2015级后的学生初始密码为身份证后六位数字。由于使用初始密码存在风险，建议你及时修改账号密码。</span>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
