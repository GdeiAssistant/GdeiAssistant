<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-[var(--color-surface)]">
    <!-- Sticky header -->
    <div class="sticky top-0 left-0 right-0 h-[50px] bg-[var(--color-surface)] shadow-md flex items-center px-4 z-[1000]">
      <span class="text-[var(--color-primary)] text-[15px] cursor-pointer mr-4" @click="router.back()">返回</span>
      <h2 class="flex-1 text-center text-lg font-medium text-[var(--c-text-1)] m-0">Cookie政策</h2>
      <span class="w-10"></span>
    </div>

    <!-- Content -->
    <div class="max-w-2xl mx-auto px-4 py-6">
      <div class="bg-[var(--color-surface)] rounded-xl p-4 shadow-sm">
        <div class="text-sm leading-relaxed text-[var(--c-text-2)] [&_p]:my-3 [&_strong]:text-base [&_strong]:font-bold [&_strong]:text-[var(--c-text-1)]">
          <p class="text-center">《广东二师助手Cookie政策》</p>
          <p class="text-center">更新日期：2019年3月26日</p>
          <p class="text-right"><br/></p>
          <p></p>
          <div class="mb-6">
            <p><strong>Cookie政策</strong></p>
            <p class="indent-7">
              Cookie是一种可让网站服务器将数据存储于客户端或从客户端中读取数据的中立技术。Cookie文件是存储了一些与用户访问网站有关信息的文件，格式为.txt。当您访问网站时会被储存在你的电脑里。如果您使用同样的浏览器再次浏览我们的网站，这些文件将被检索。Cookie内容通常均经过加密。安全的Cookie技术会为用户在上网时提供更为便捷的服务。
            </p>
          </div>
          <div class="mb-6">
            <p><strong>我们如何使用Cookies</strong></p>
            <p class="indent-7">
              Cookie对提升用户的网络使用体验十分重要，广东二师助手使用Cookie一般出于以下目的：
            </p>
            <div class="overflow-x-auto mt-8">
              <table class="w-full border-collapse">
                <tr>
                  <td class="border border-gray-400 px-6 py-4 text-center w-20 font-bold">目的分类</td>
                  <td class="border border-gray-400 px-6 py-4 text-center font-bold">说明&示例</td>
                </tr>
                <tr>
                  <td class="border border-gray-400 px-6 py-4 text-center">验证身份</td>
                  <td class="border border-gray-400 px-6 py-4">
                    <p class="text-left">Cookie可在您接入我们的服务时通知我们，以使我们可验证您的身份信息，以确保您的账号安全。</p>
                    <p class="text-left">例如，Cookie技术可在您登入广东二师助手服务时通知我们，因此，我们能够在您访问所有广东二师助手服务的站点时，识别是否您本人安全登录，向您显示与您相关的信息。</p>
                  </td>
                </tr>
                <tr>
                  <td class="border border-gray-400 px-6 py-4 text-center">偏好设置</td>
                  <td class="border border-gray-400 px-6 py-4">
                    <p class="text-left">Cookie可帮助我们按照您所希望的服务样式、外观的个性化设置为您提供服务。</p>
                    <p class="text-left">
                        例如，Cookie技术可记录您是否已经阅读过一些提示性的展现，防止重复提示对您造成骚扰；一些阅读展现（如字体大小等）方面您本人偏好的设定，我们也会存放于Cookie中，在您访问时自动为您调整到您上次设定的值。</p>
                  </td>
                </tr>
                <tr>
                  <td class="border border-gray-400 px-6 py-4 text-center">安全</td>
                  <td class="border border-gray-400 px-6 py-4">
                    <p class="text-left">Cookie可帮助我们保障数据和服务的安全性，排查针对我们的产品和服务的作弊、黑客、欺诈行为。</p>
                    <p class="text-left">例如，Cookie可以存放票据信息，能够由服务器验证是否是您自主在本站点的正常登录，通过票据中的加密信息，阻止多种类型的攻击，防止跨站信息窃取访问，防止身份冒充访问。</p>
                  </td>
                </tr>
                <tr>
                  <td class="border border-gray-400 px-6 py-4 text-center">功能与服务</td>
                  <td class="border border-gray-400 px-6 py-4">
                    <p class="text-left">Cookie可帮助我们为用户提供更好的产品和服务。</p>
                    <p class="text-left">例如，Cookie可以在您进行登录时，通过存储的信息，帮您填入您最后一次登入的账户名称，提高您的操作效率。</p>
                  </td>
                </tr>
                <tr>
                  <td class="border border-gray-400 px-6 py-4 text-center">效率</td>
                  <td class="border border-gray-400 px-6 py-4">
                    <p class="text-left">Cookie可以避免不必要的服务器负载，提高服务效率，节省资源、能源。</p>
                    <p class="text-left">例如，Cookie可帮助我们优化在服务器之间路由流量，并且了解不同用户加载我们服务时的速度，有时，我们或会利用Cookie让您正在使用时加载及响应得更快。</p>
                  </td>
                </tr>
                <tr>
                  <td class="border border-gray-400 px-6 py-4 text-center">分析与研究</td>
                  <td class="border border-gray-400 px-6 py-4">
                    <p class="text-left">我们使用这些Cookie来了解、改善我们的产品和服务，为新产品或功能的研发提供参考数据。</p>
                    <p class="text-left">例如，我们会使用Cookie来了解您使用我们服务的方式，以求改善用户体验。例如您在接入我们服务时，在各个步骤所耗费的时间，我们会收集此信息用以分析研究能够进一步优化和改善的方面。</p>
                  </td>
                </tr>
              </table>
            </div>
          </div>
          <div class="mb-6">
            <p><strong>用户如何管理Cookie</strong></p>
            <p class="indent-7">
              大多数浏览器均为用户提供了管理Cookie的功能。当用户在进行浏览器的Cookie设置时，可以允许自己信任的网站启用Cookie功能，禁止其他网站使用Cookie功能。例如，您可在Chrome浏览器设置中进行Cookie的管理。同时，您还可以开启"禁止跟踪（DNT）"功能。但请注意，如果停用Cookie，您可能无法享受最佳的服务体验，某些服务也可能无法正常使用。
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
