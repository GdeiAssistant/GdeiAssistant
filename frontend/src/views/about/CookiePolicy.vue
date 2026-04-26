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
            [&_ul]:mb-3 [&_ul]:list-disc [&_ul]:pl-5
            [&_li]:mb-2
            [&_a]:text-[var(--c-primary)] [&_a]:underline
            [&_strong]:font-semibold">
          <div v-if="isNonChinese" class="bg-amber-50 text-amber-800 border border-amber-300 rounded-lg px-4 py-3 mb-4 text-sm leading-relaxed">
            {{ t('about.chineseOnlyNotice') }}
          </div>

          <h3 class="!mt-0 text-center">《广东二师助手 Cookie 与本地存储说明》</h3>
          <p class="text-center">发布日期：2026年2月17日</p>
          <p class="text-center">更新日期：2026年4月25日</p>
          <p class="text-center">生效日期：2026年5月11日</p>

          <div class="bg-amber-50 text-amber-900 border border-amber-200 rounded-lg px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>使用方式说明：</strong>平台通常会通过浏览器本地存储、请求头或其他必要的站点数据维持登录状态和界面偏好；是否使用浏览器 Cookie 以及具体方式，取决于终端、版本和实际部署。</p>
            <p class="!mb-0"><strong>风险提示：</strong>浏览器本地存储可能受 XSS、恶意扩展、共享设备、浏览器同步或误操作影响。请避免在公共设备长期登录，并在退出登录、清理浏览器数据或注销账号后及时关闭会话。</p>
          </div>

          <h3>第一条 我们使用哪些技术</h3>
          <p><strong>1.1</strong> Cookie 是网站写入浏览器的小型文本数据。本说明中的“Cookie 与同类技术”还包括 localStorage、sessionStorage、浏览器缓存以及其他用于保存本地状态、偏好或临时会话信息的技术。</p>
          <p><strong>1.2</strong> 当前 Web 端通常会读取或保存以下状态：</p>
          <ul>
            <li>登录令牌或会话标识，用于维持登录状态并向接口附加必要凭证；</li>
            <li>语言、主题、字体缩放、数据源模式等界面偏好；</li>
            <li>Cookie 横幅关闭状态、部分页面配置和临时缓存；</li>
            <li>退出登录、安全处理或页面恢复所需的会话级临时状态。</li>
          </ul>
          <p><strong>1.3</strong> 平台与学校相关系统或第三方系统交互时，服务端可能根据实际实现使用请求头、Cookie、服务端会话、同步凭证或其他安全机制；这不等同于您浏览器侧一定使用 HttpOnly Cookie 保存用户登录状态。</p>

          <h3>第二条 这些技术的用途</h3>
          <p><strong>2.1</strong> 识别您是否已登录、附加必要凭证、在令牌失效或退出登录时清理状态。</p>
          <p><strong>2.2</strong> 记住语言、主题、字体大小、数据源模式和其他界面偏好，减少重复设置。</p>
          <p><strong>2.3</strong> 辅助异常状态恢复、故障排查、风控和必要的合规留痕。</p>
          <p><strong>2.4</strong> 当前版本未将定向广告、跨站画像或独立广告网络跟踪作为既有能力写入本说明；若后续新增相关能力，平台会按照适用规则另行告知。</p>

          <h3>第三条 本地存储与 Cookie 的风险</h3>
          <p><strong>3.1</strong> 浏览器本地存储中的数据通常可被同一设备上的浏览器环境读取。若设备存在恶意扩展、恶意脚本、共享账号、自动同步或长时间无人管理的登录状态，相关信息可能被他人利用。</p>
          <p><strong>3.2</strong> 为降低风险，建议您：</p>
          <ul>
            <li>不要在公共设备、共享设备或不可信浏览器中长期保持登录；</li>
            <li>使用完成后主动退出登录，并视需要清理浏览器缓存、Cookie、本地存储和下载记录；</li>
            <li>发现异常登录、设备遗失、令牌疑似泄露或账号被盗用时，及时修改密码并申请失效处理。</li>
          </ul>

          <h3>第四条 您如何管理这些技术</h3>
          <p><strong>4.1</strong> 您可以通过浏览器设置清理 Cookie、站点数据、本地存储、缓存或站点权限，也可以通过退出登录、清空应用数据、删除账号等方式减少残留信息。</p>
          <p><strong>4.2</strong> 清理相关数据后，您可能需要重新登录、重新设置界面偏好、重新关闭提示横幅，或者导致部分缓存结果失效。</p>
          <p><strong>4.3</strong> 如您阻止浏览器保存必要状态、禁用相关存储或清理关键数据，登录保持、页面体验、偏好记忆和部分功能可能无法正常工作。</p>

          <h3>第五条 与隐私政策和安全说明的关系</h3>
          <p><strong>5.1</strong> 本说明是 <a href="/policy/privacy">《隐私政策》</a> 的补充文件，主要解释 Cookie、本地存储和同类技术的使用方式。</p>
          <p><strong>5.2</strong> 如 Cookie、本地存储或同类技术涉及个人信息处理，其处理目的、保存期限、用户权利和第三方服务规则，以隐私政策及相关页面提示为准。</p>
          <p><strong>5.3</strong> 关于更完整的技术和部署安全建议，请同时参阅 <a href="/about/security">《安全说明》</a>。</p>

          <h3>第六条 更新与生效</h3>
          <p><strong>6.1</strong> 平台可根据法律法规、前端技术方案、登录状态保存机制或安全策略变化，对本说明进行更新。</p>
          <p><strong>6.2</strong> 本次修订内容将自 2026 年 5 月 11 日起适用；生效日前仍适用修订前版本。</p>
        </div>
      </div>
    </div>
  </div>
</template>
