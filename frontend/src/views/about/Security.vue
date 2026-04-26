<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t, locale } = useI18n()
const isNonChinese = computed(() => !locale.value.startsWith('zh'))
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="router.back()" class="text-[var(--c-primary)] text-sm font-medium">← {{ t('about.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('about.securityTitle') }}</span>
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
            [&_strong]:font-semibold">
          <div v-if="isNonChinese" class="bg-amber-50 text-amber-800 border border-amber-300 rounded-lg px-4 py-3 mb-4 text-sm leading-relaxed">
            {{ t('about.chineseOnlyNotice') }}
          </div>

          <h3 class="!mt-0 text-center">《广东二师助手安全说明》</h3>
          <p class="text-center">发布日期：2019年1月31日</p>
          <p class="text-center">更新日期：2026年4月25日</p>
          <p class="text-center">生效日期：2026年5月11日</p>

          <div class="bg-slate-50 text-slate-800 border border-slate-200 rounded-lg px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>阅读提示：</strong>本页面用于说明平台通常采取或建议采取的安全措施，具体实现仍以实际部署、版本和配置为准。</p>
            <p class="!mb-0"><strong>本次修订说明：</strong>本次修订内容将自 2026 年 5 月 11 日起适用；生效日前仍适用修订前版本。</p>
          </div>

          <h3>第一条 安全目标与适用范围</h3>
          <p><strong>1.1</strong> 平台通常从账号安全、校园凭证保护、服务可用性、日志审计、最小权限、密钥管理、异常监测和问题追溯等方面开展安全设计和持续改进。</p>
          <p><strong>1.2</strong> 本说明适用于 Web 端、接口服务、对象存储上传、校园系统对接、短信/邮件/AI OCR 等能力的常见安全要求，但不构成对全部技术细节的穷尽披露。</p>

          <h3>第二条 登录态、令牌与会话安全</h3>
          <p><strong>2.1</strong> Web 端登录状态通常主要依赖浏览器本地存储中的令牌与 `Authorization` 请求头维持，而不是仅依赖浏览器 HttpOnly Cookie 作为唯一或主要的用户登录载体。</p>
          <p><strong>2.2</strong> 结合不同功能场景，平台也可能使用浏览器本地存储、请求头、Cookie、服务端会话、同步凭证或其他安全机制保存或传递登录状态；对接学校相关系统时，服务端还可能基于同步会话或服务端 Cookie 完成校园系统访问。</p>
          <p><strong>2.3</strong> 对高敏令牌、校园凭证和同步会话，平台应尽量采用更安全的存储、有效期、刷新、失效、访问控制、异常检测和 XSS 防护措施。</p>
          <p><strong>2.4</strong> 浏览器本地存储存在 XSS、恶意扩展、共享设备、浏览器同步和误操作带来的风险。请避免在公共设备长期登录，并在完成使用后退出登录、清理浏览器数据或注销账号。</p>

          <h3>第三条 凭证、敏感字段与数据保护</h3>
          <p><strong>3.1</strong> 对校园账号凭证、验证码、手机号、邮箱、取件码、地址、对象存储访问凭证和其他高敏信息，平台应按照最小必要原则处理，并尽量在传输和存储环节采用加密、脱敏、权限隔离和日志审计措施。</p>
          <p><strong>3.2</strong> 生产环境应启用敏感字段加密。未启用前，不应以“已充分加密保护”作绝对承诺。</p>
          <p><strong>3.3</strong> 日志、监控和错误追踪中不应记录明文密码、验证码、取件码、完整手机号、完整邮箱、完整校园账号、完整证件号、完整地址、完整令牌或其他不必要的高敏数据。</p>
          <p><strong>3.4</strong> 对图片、附件和对象存储上传，应控制文件类型、大小、有效期、对象访问权限和临时上传凭证范围，并尽量避免在可公开地址中暴露无关元数据。</p>

          <h3>第四条 开发与部署安全建议</h3>
          <ul>
            <li>不得将真实密钥、真实账号、真实密码、真实短信模板密钥、邮件密钥、AI API Key、对象存储密钥、数据库连接串等提交到仓库。</li>
            <li>`JWT_SECRET`、`ENCRYPT_PRIVATE_KEY`、短信、邮件、AI、对象存储等密钥应通过环境变量、部署平台 Secret 或其他专门的密钥管理机制配置。</li>
            <li>生产环境应启用敏感字段加密、合理的访问控制、速率限制、异常告警、日志脱敏和必要的备份恢复策略。</li>
            <li>调用外部 AI/OCR、短信、邮件、对象存储或其他第三方服务时，应仅传输实现功能所必需的最少信息。</li>
            <li>上传、查询、交易、跑腿和投诉相关接口应结合身份校验、参数校验、权限控制和风控策略限制滥用。</li>
          </ul>

          <h3>第五条 常见风险与用户自我保护</h3>
          <ul>
            <li>不要在非本人设备、共享浏览器或来源不明的页面中输入校园账号、密码、验证码或其他敏感凭证。</li>
            <li>谨慎上传头像、证件、学生卡、取件码截图、物流凭证和包含住址或联系方式的图片。</li>
            <li>不要向他人转发验证码、下载链接、校园凭证、登录令牌或含敏感信息的截图。</li>
            <li>发现账号异常、内容被冒用、取件码泄露、设备遗失或疑似数据泄露时，应尽快修改密码并通过反馈渠道申请处理。</li>
          </ul>

          <h3>第六条 漏洞处理与安全事件响应</h3>
          <p><strong>6.1</strong> 如您发现安全漏洞、越权访问、敏感信息暴露或其他安全风险，可通过应用内“帮助与反馈”或页面公示的其他安全反馈渠道提交。涉及敏感漏洞时，请避免在公开页面披露可直接利用的细节。</p>
          <p><strong>6.2</strong> 平台通常会在合理期限内评估、修复或缓解已确认的安全问题，但不承诺固定处理时限、奖金或公开披露计划。</p>
          <p><strong>6.3</strong> 当发生或可能发生安全事件时，平台会在适用规则要求下采取失效处理、访问限制、日志留存、补丁发布、配置调整、范围排查和合理通知等措施。</p>

          <h3>第七条 说明更新</h3>
          <p><strong>7.1</strong> 平台可根据法律法规、部署形态、产品能力、第三方服务和安全策略变化，对本说明进行修订。</p>
          <p><strong>7.2</strong> 涉及登录状态保存机制、校园凭证处理、安全风险提示或用户重要权益变化时，平台通常会通过页面公告、弹窗、站内通知或其他合理方式提示您阅读。</p>
        </div>
      </div>
    </div>
  </div>
</template>
