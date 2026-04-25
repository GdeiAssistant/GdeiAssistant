<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const isNonChinese = computed(() => !locale.value.startsWith('zh'))

const rows = [
  {
    type: '学校相关系统',
    providers: '根据实际接入的学校认证、教务、图书馆、校园卡等系统而定',
    purpose: '校园认证、会话同步、校园数据查询',
    data: '校园账号凭证、会话信息、查询参数、查询结果',
    disable: '通常与对应校园功能绑定',
    crossBorder: '通常以相关系统实际部署为准',
    note: '属于核心校园功能依赖'
  },
  {
    type: '邮件服务 / SMTP',
    providers: '根据实际部署选择的邮件服务商或自建邮件服务',
    purpose: '邮箱验证码、回执、通知',
    data: '邮箱地址、邮件内容、发送状态、发送记录',
    disable: '可根据部署选择是否启用',
    crossBorder: '视服务商区域和配置而定',
    note: '未启用则不会发生相应传输'
  },
  {
    type: '短信服务',
    providers: '可能包括腾讯云、阿里云、华为云或其他服务商',
    purpose: '短信验证码、安全通知',
    data: '手机号、验证码发送记录、发送状态',
    disable: '可根据部署选择是否启用',
    crossBorder: '视服务商区域和配置而定',
    note: '应以最小必要范围传输'
  },
  {
    type: '对象存储',
    providers: '可能包括 Cloudflare R2 或其他对象存储服务',
    purpose: '头像、图片、附件上传与访问',
    data: '文件内容、文件名、对象键、访问日志、必要的请求元数据',
    disable: '上传能力可按功能开关控制',
    crossBorder: '视实际部署、服务商区域和配置而定',
    note: '不应将无关高敏信息一并上传'
  },
  {
    type: 'AI OCR / 验证码识别',
    providers: '可能包括 DeepSeek、OpenAI、Gemini、Claude、豆包等',
    purpose: '验证码、数字或图片文字识别',
    data: '验证码图片、识别请求、必要的请求元数据',
    disable: '视配置而定',
    crossBorder: '视实际部署、服务商区域和配置而定；平台将按适用规则进行告知、评估或取得必要授权',
    note: '未启用时不会发生该类传输'
  },
  {
    type: '云服务器、数据库、缓存、日志、监控、备份',
    providers: '根据实际部署选择的云服务商或自建基础设施',
    purpose: '服务运行、安全审计、故障排查、备份恢复',
    data: '账号数据、配置数据、运行日志、缓存数据、备份文件',
    disable: '通常不可完全关闭',
    crossBorder: '视实际部署、服务商区域和配置而定',
    note: '应结合权限控制、加密和脱敏措施'
  }
]
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">← {{ t('about.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">第三方服务清单</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-3xl mx-auto px-4 py-6">
      <div class="rounded-xl bg-[var(--c-surface)] border border-[var(--c-border)] p-5 md:p-6">
        <div
          class="text-sm leading-7 text-[var(--c-text-2)]
            [&_p]:mb-3
            [&_h3]:mt-7 [&_h3]:mb-3 [&_h3]:text-base [&_h3]:font-bold [&_h3]:text-[var(--c-text-primary)]
            [&_a]:text-[var(--c-primary)] [&_a]:underline
            [&_table]:mb-4 [&_table]:w-full [&_table]:border-collapse [&_table]:text-left
            [&_thead]:bg-[var(--c-bg)]
            [&_th]:border [&_th]:border-[var(--c-border)] [&_th]:px-3 [&_th]:py-2 [&_th]:text-xs [&_th]:font-semibold [&_th]:text-[var(--c-text-primary)]
            [&_td]:border [&_td]:border-[var(--c-border)] [&_td]:px-3 [&_td]:py-2 [&_td]:align-top
            [&_strong]:font-semibold">
          <div v-if="isNonChinese" class="bg-amber-50 text-amber-800 border border-amber-300 rounded-lg px-4 py-3 mb-4 text-sm leading-relaxed">
            {{ t('about.chineseOnlyNotice') }}
          </div>

          <h3 class="!mt-0 text-center">《第三方服务清单》</h3>
          <p class="text-center">发布日期：2026年4月25日</p>
          <p class="text-center">更新日期：2026年4月25日</p>
          <p class="text-center">生效日期：2026年5月11日</p>

          <div class="bg-slate-50 text-slate-800 border border-slate-200 rounded-lg px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>说明：</strong>以下服务商为“可能包括”或“根据实际部署选择”的范围，不代表当前所有服务均已启用。</p>
            <p class="!mb-0"><strong>互链：</strong>本清单与 <a href="/policy/privacy">《隐私政策》</a> 配套阅读；涉及安全配置建议时，可同时参阅 <a href="/about/security">《安全说明》</a>。</p>
          </div>

          <table>
            <thead>
              <tr>
                <th>服务类型</th>
                <th>可能服务商</th>
                <th>使用目的</th>
                <th>涉及信息</th>
                <th>是否可关闭</th>
                <th>是否可能跨境或境外处理</th>
                <th>备注</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in rows" :key="row.type">
                <td>{{ row.type }}</td>
                <td>{{ row.providers }}</td>
                <td>{{ row.purpose }}</td>
                <td>{{ row.data }}</td>
                <td>{{ row.disable }}</td>
                <td>{{ row.crossBorder }}</td>
                <td>{{ row.note }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
