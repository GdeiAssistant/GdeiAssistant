<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="article-page">
    <div class="article-header">
      <span class="back-btn" @click="router.back()">返回</span>
      <h2 class="article-title">Cookie政策</h2>
    </div>
    <div class="weui-article">
      <p style="text-align:center">《广东二师助手Cookie政策》</p>
      <p style="text-align:center">更新日期：2019年3月26日</p>
      <p style="text-align:right"><br/></p>
      <p style="text-indent:14px;line-height:150%"></p>
      <p><strong>Cookie政策</strong></p>
      <p style="text-indent:28px">
        Cookie是一种可让网站服务器将数据存储于客户端或从客户端中读取数据的中立技术。Cookie文件是存储了一些与用户访问网站有关信息的文件，格式为.txt。当您访问网站时会被储存在你的电脑里。如果您使用同样的浏览器再次浏览我们的网站，这些文件将被检索。Cookie内容通常均经过加密。安全的Cookie技术会为用户在上网时提供更为便捷的服务。
      </p>
      <p>&nbsp;</p>
      <p><strong>我们如何使用Cookies</strong></p>
      <p style="text-indent:28px">
        Cookie对提升用户的网络使用体验十分重要，广东二师助手使用Cookie一般出于以下目的：
      </p>
      <table>
        <tr>
          <td class="td1"><strong>目的分类</strong></td>
          <td><strong>说明&示例</strong></td>
        </tr>
        <tr>
          <td>验证身份</td>
          <td>
            <p>Cookie可在您接入我们的服务时通知我们，以使我们可验证您的身份信息，以确保您的账号安全。</p>
            <p>例如，Cookie技术可在您登入广东二师助手服务时通知我们，因此，我们能够在您访问所有广东二师助手服务的站点时，识别是否您本人安全登录，向您显示与您相关的信息。</p>
          </td>
        </tr>
        <tr>
          <td>偏好设置</td>
          <td>
            <p>Cookie可帮助我们按照您所希望的服务样式、外观的个性化设置为您提供服务。</p>
            <p>
                例如，Cookie技术可记录您是否已经阅读过一些提示性的展现，防止重复提示对您造成骚扰；一些阅读展现（如字体大小等）方面您本人偏好的设定，我们也会存放于Cookie中，在您访问时自动为您调整到您上次设定的值。</p>
          </td>
        </tr>
        <tr>
          <td>安全</td>
          <td>
            <p>Cookie可帮助我们保障数据和服务的安全性，排查针对我们的产品和服务的作弊、黑客、欺诈行为。</p>
            <p>例如，Cookie可以存放票据信息，能够由服务器验证是否是您自主在本站点的正常登录，通过票据中的加密信息，阻止多种类型的攻击，防止跨站信息窃取访问，防止身份冒充访问。</p>
          </td>
        </tr>
        <tr>
          <td>功能与服务</td>
          <td>
            <p>Cookie可帮助我们为用户提供更好的产品和服务。</p>
            <p>例如，Cookie可以在您进行登录时，通过存储的信息，帮您填入您最后一次登入的账户名称，提高您的操作效率。</p>
          </td>
        </tr>
        <tr>
          <td>效率</td>
          <td>
            <p>Cookie可以避免不必要的服务器负载，提高服务效率，节省资源、能源。</p>
            <p>例如，Cookie可帮助我们优化在服务器之间路由流量，并且了解不同用户加载我们服务时的速度，有时，我们或会利用Cookie让您正在使用时加载及响应得更快。</p>
          </td>
        </tr>
        <tr>
          <td>分析与研究</td>
          <td>
            <p>我们使用这些Cookie来了解、改善我们的产品和服务，为新产品或功能的研发提供参考数据。</p>
            <p>例如，我们会使用Cookie来了解您使用我们服务的方式，以求改善用户体验。例如您在接入我们服务时，在各个步骤所耗费的时间，我们会收集此信息用以分析研究能够进一步优化和改善的方面。</p>
          </td>
        </tr>
      </table>
      <p>&nbsp;</p>
      <p><strong>用户如何管理Cookie</strong></p>
      <p style="text-indent:28px">
        大多数浏览器均为用户提供了管理Cookie的功能。当用户在进行浏览器的Cookie设置时，可以允许自己信任的网站启用Cookie功能，禁止其他网站使用Cookie功能。例如，您可在Chrome浏览器设置中进行Cookie的管理。同时，您还可以开启"禁止跟踪（DNT）"功能。但请注意，如果停用Cookie，您可能无法享受最佳的服务体验，某些服务也可能无法正常使用。
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

.weui-article table {
  border-collapse: collapse;
  width: 100%;
  margin-top: 32px;
}

.weui-article table td {
  border: 1px solid #999;
  padding: 16px 25px;
  text-align: center;
}

.weui-article table td.td1 {
  width: 80px;
}

.weui-article table td p {
  text-align: left;
}

.weui-article strong {
  font-weight: 600;
}
</style>
