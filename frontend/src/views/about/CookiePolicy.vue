<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const isNonChinese = computed(() => !locale.value.startsWith('zh'))
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">← {{ t('about.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('about.menuCookiePolicy') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-3xl mx-auto px-4 py-6">
      <div class="rounded-xl bg-[var(--c-surface)] border border-[var(--c-border)] p-5 md:p-6">
        <div
          class="text-sm leading-7 text-[var(--c-text-2)]
            [&_p]:mb-3
            [&_h3]:mt-7 [&_h3]:mb-3 [&_h3]:text-base [&_h3]:font-bold [&_h3]:text-[var(--c-text-primary)]
            [&_h4]:mt-5 [&_h4]:mb-2 [&_h4]:text-sm [&_h4]:font-semibold [&_h4]:text-[var(--c-text-primary)]
            [&_ul]:mb-3 [&_ul]:list-disc [&_ul]:pl-5
            [&_ol]:mb-3 [&_ol]:list-decimal [&_ol]:pl-5
            [&_li]:mb-2
            [&_strong]:font-semibold">
          <div v-if="isNonChinese" class="bg-amber-50 text-amber-800 border border-amber-300 rounded-lg px-4 py-3 mb-4 text-sm leading-relaxed">
            {{ t('about.chineseOnlyNotice') }}
          </div>

          <h3 class="!mt-0 text-center">《广东二师助手Cookie与本地存储说明》</h3>
          <p class="text-center">更新日期：2026年4月15日</p>
          <p class="text-center">生效日期：2026年4月15日</p>

          <p><strong>特别提示：</strong>本说明用于说明广东二师助手在 Web 端如何使用 Cookie 以及与其功能类似的客户端存储技术。结合当前版本的真实实现，我们主要使用浏览器本地存储（如 Local Storage）保存登录状态、界面偏好、数据源模式和提示关闭状态；传统 Cookie 并不是当前 Web 端的主要登录鉴权载体。</p>

          <h3>第一条 什么是 Cookie 及同类技术</h3>
          <p><strong>1.1</strong> Cookie 是网站在浏览器或设备中写入并读取的小型文本数据，可用于识别访问状态、保存设置或辅助安全控制。</p>
          <p><strong>1.2</strong> 与 Cookie 功能类似的技术，还包括浏览器本地存储（Local Storage）、会话存储（Session Storage）以及其他用于保存本地状态、配置或临时会话信息的客户端存储机制。</p>
          <p><strong>1.3</strong> 本说明所称“Cookie 及同类技术”，即对前述技术的合称。不同技术的保存期限、作用范围和清理方式可能不同，但都可能影响您访问页面时的登录状态、显示偏好和使用体验。</p>

          <h3>第二条 我们当前使用哪些技术</h3>
          <p><strong>2.1</strong> 就当前 Web 端实现而言，我们不以传统 Cookie 作为主要登录鉴权方式。前后端鉴权主要依赖浏览器本地存储中的登录令牌以及请求头中的 Authorization 信息。</p>
          <p><strong>2.2</strong> 当前版本会在浏览器本地存储中保存或读取部分必要状态，例如：</p>
          <ul>
            <li>登录令牌，用于维持登录状态并发起受保护请求；</li>
            <li>语言偏好（如 locale），用于记住您的界面语言；</li>
            <li>主题或显示偏好（如 theme、font_scale_step），用于记住深浅色模式和字体缩放设置；</li>
            <li>数据源模式（如 gdei_data_source_mode），用于记住您选择的数据源开关；</li>
            <li>提示关闭状态（如 cookieAccepted），用于记住 About 页 Cookie 横幅是否已被关闭；</li>
            <li>部分界面开关或功能展示配置，用于改善页面可用性和减少重复提示。</li>
          </ul>
          <p><strong>2.3</strong> 当前 Web 端也会在退出登录、删除账号或安全处理时清理 sessionStorage 等会话级存储，以减少浏览器残留的临时状态对后续使用的影响。</p>
          <p><strong>2.4</strong> 根据当前代码实现，我们未独立部署面向定向广告的第三方 Cookie、跨站画像类跟踪技术，也未单独接入第三方广告网络或独立统计埋点 SDK。若后续新增相关能力，我们会依据法律法规另行告知，并在必要时取得您的同意。</p>
          <p><strong>2.5</strong> 当前公开 Web 端未单独启用以离线持久化为目的的 service worker 或 IndexedDB 持久化方案。后续如技术方案发生变化，我们会同步更新本说明。</p>

          <h3>第三条 这些技术的用途</h3>
          <p><strong>3.1【严格必要 / 安全与登录状态】</strong> 用于识别您是否已登录、为请求附加必要凭证、在退出登录或令牌失效时清理状态，并减少未经授权访问带来的风险。</p>
          <p><strong>3.2【功能偏好 / 界面设置记忆】</strong> 用于记住您的语言、主题、字体缩放、数据源模式及部分界面配置，避免您每次访问时重复设置。</p>
          <p><strong>3.3【风险控制 / 防滥用 / 故障排查】</strong> 我们可能结合本地状态和服务端鉴权结果进行异常登录识别、请求校验、状态恢复、页面调试和问题排查，但不会因此向您承诺绝对安全或绝不出错。</p>
          <p><strong>3.4【法律合规记录】</strong> 当前 About 页存在一个简化的 Cookie 横幅。您关闭该提示后，系统会通过本地状态记住关闭结果，以避免重复打扰并保留必要的告知记录。</p>
          <p><strong>3.5</strong> 除前述与当前实现相符的用途外，我们当前不独立部署用于跨站定向营销、联盟广告画像或商业化广告投放的 Cookie 体系。</p>

          <h3>第四条 是否存在第三方 Cookie、第三方存储或第三方统计</h3>
          <p><strong>4.1</strong> 结合当前项目实现，平台未独立部署面向定向广告的第三方 Cookie，也未提供站内“同意/拒绝全部广告 Cookie”的单独管理中心，因为当前版本并未使用这类复杂广告跟踪体系。</p>
          <p><strong>4.2</strong> 当前版本如涉及学校官方系统、对象存储、验证码、短信邮件或其他第三方基础设施服务，其数据交互规则以对应功能的实际调用和《广东二师助手隐私政策》为准；本说明不将未实际接入的第三方广告、A/B 测试或跨站画像能力虚构为既有功能。</p>

          <h3>第五条 您如何管理或清除 Cookie 及同类技术</h3>
          <p><strong>5.1</strong> 您可以通过浏览器设置管理 Cookie、站点权限、缓存或清除站点数据；也可以在浏览器开发者工具或站点设置中清除 Local Storage、Session Storage 等本地存储内容。</p>
          <p><strong>5.2</strong> 您清除本地站点数据、退出登录或更换浏览器/设备后，登录状态、语言偏好、主题设置、字体缩放、数据源模式、Cookie 横幅关闭状态等可能失效或恢复默认值。</p>
          <p><strong>5.3</strong> 如您禁用必要的 Cookie 或同类技术，或者阻止浏览器保存必要的本地状态，登录保持、偏好记忆、部分页面展示或相关功能可能无法正常使用。</p>
          <p><strong>5.4</strong> 当前版本未提供站内逐项勾选、逐项关闭所有 Cookie 的专门设置中心；如后续上线相关能力，我们会结合实际功能另行说明。</p>

          <h3>第六条 禁用后的影响</h3>
          <p><strong>6.1</strong> 禁用或清除必要类本地状态后，您可能需要重新登录、重新选择语言和主题、重新设置数据源模式，并再次看到 Cookie 横幅提示。</p>
          <p><strong>6.2</strong> 如浏览器、插件或系统策略阻止本地状态读写，部分依赖登录态和偏好记忆的页面可能出现重复跳转、设置丢失、展示不一致或请求失败等情况。</p>

          <h3>第七条 本说明与隐私政策的关系</h3>
          <p><strong>7.1</strong> 本说明是《广东二师助手隐私政策》的补充说明，主要解释 Cookie、Local Storage、Session Storage 及其他同类技术的使用方式。</p>
          <p><strong>7.2</strong> 当 Cookie 或同类技术涉及个人信息处理时，相关个人信息保护规则、共享规则、保存期限和用户权利，以《广东二师助手隐私政策》及相关页面提示为准。</p>
          <p><strong>7.3</strong> 如未来确有第三方接收与 Cookie 或同类技术有关的信息，平台将按照实际场景在隐私政策、功能页提示或单独授权页中予以说明。</p>

          <h3>第八条 更新与生效</h3>
          <p><strong>8.1</strong> 我们可能因法律法规、监管要求、产品功能变化、前端存储机制调整或安全能力升级，对本说明进行更新。</p>
          <p><strong>8.2</strong> 更新后的说明将通过页面公示、弹窗或其他合理方式提示，并自页面载明日期起生效。您在更新生效后继续使用本服务的，视为您已阅读并接受更新后的说明。</p>
        </div>
      </div>
    </div>
  </div>
</template>
