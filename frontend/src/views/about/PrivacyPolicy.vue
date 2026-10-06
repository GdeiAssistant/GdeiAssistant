<script setup>
import { ChevronLeft } from 'lucide-vue-next'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const isNonChinese = computed(() => !locale.value.startsWith('zh'))

const sensitiveInfoRows = [
  {
    type: '校园账号、密码、验证码、登录状态、会话令牌',
    scenario: '校园统一认证、快速认证、会话同步、异常登录排查',
    sensitive: '通常是',
    necessity: '校园认证与相关查询功能通常必需',
    control: '可拒绝保存；拒绝后相关快速认证或部分查询功能可能不可用，可申请删除或停止使用'
  },
  {
    type: '成绩、课表、考试、校园卡消费、图书借阅等校园数据',
    scenario: '学习生活查询、缓存展示、问题排查',
    sensitive: '可能是',
    necessity: '仅在您主动触发查询或为实现功能所必需时处理',
    control: '可停止继续查询；历史记录、审计或争议处理所需部分信息可能无法立即删除'
  },
  {
    type: '手机号、邮箱、验证码',
    scenario: '绑定、验证、安全提醒、通知',
    sensitive: '可能是',
    necessity: '绑定与验证功能通常必需',
    control: '可解绑或申请删除；法律法规、安全审计或纠纷处理所需信息可能继续保留'
  },
  {
    type: '头像、图片、昵称、主页信息',
    scenario: '资料展示、社区发布、投诉处理',
    sensitive: '视内容而定',
    necessity: '资料展示与图片上传功能所必需',
    control: '可修改、删除或关闭公开范围；已被他人合法获取的副本不受平台单方控制'
  },
  {
    type: '二手交易信息、联系方式',
    scenario: '发布、搜索、沟通、纠纷处理',
    sensitive: '可能是',
    necessity: '交易信息发布通常必需',
    control: '可删除或下架；涉及投诉、风控或纠纷的记录可能在必要期限内保留'
  },
  {
    type: '失物招领信息、证件/学生卡/照片',
    scenario: '发布、认领核验、防冒领、投诉处理',
    sensitive: '通常是',
    necessity: '相关场景功能通常必需',
    control: '可删除或申请遮盖；涉及权属争议、投诉或安全事项的记录可能继续保留'
  },
  {
    type: '跑腿/快递取件码、地址、收件人、联系方式',
    scenario: '订单发布、履约沟通、异常处理',
    sensitive: '通常是',
    necessity: '相关场景功能通常必需',
    control: '可在完成后删除或申请处理；涉及审计、纠纷或安全事项的部分记录可能继续保留'
  },
  {
    type: '安全日志、登录记录、设备和网络信息',
    scenario: '风险识别、故障排查、审计追溯',
    sensitive: '可能是',
    necessity: '平台安全运行通常必需',
    control: '一般不支持立即逐条删除；可依法申请查阅、更正或说明'
  }
]

const thirdPartyRows = [
  {
    type: '学校相关系统',
    providers: '根据实际接入的学校认证、教务、图书馆、校园卡等系统而定',
    purpose: '校园认证、会话同步、校园数据查询',
    data: '校园账号凭证、会话信息、查询参数、查询结果',
    disable: '相关功能通常不可单独关闭；可停止使用对应功能或申请删除已保存凭证',
    crossBorder: '通常以相关系统实际部署为准',
    note: '属于实现校园功能的核心依赖'
  },
  {
    type: '邮件服务 / SMTP',
    providers: '根据实际部署选择的邮件服务商或自建邮件服务',
    purpose: '邮箱验证码、反馈回执、通知',
    data: '邮箱地址、邮件内容、发送状态、发送记录',
    disable: '邮箱功能可不启用；启用后可解绑',
    crossBorder: '视服务商区域、节点和配置而定',
    note: '不代表平台一定启用全部邮件能力'
  },
  {
    type: '短信服务',
    providers: '可能包括腾讯云、阿里云、华为云或其他服务商',
    purpose: '短信验证码、安全通知',
    data: '手机号、区号、验证码发送记录、发送状态',
    disable: '短信能力可根据部署选择启用或关闭',
    crossBorder: '视服务商区域、节点和配置而定',
    note: '如未启用短信能力，相应场景不会发生传输'
  },
  {
    type: '对象存储',
    providers: '可能包括 Cloudflare R2 或其他对象存储服务',
    purpose: '头像、图片、附件上传与访问',
    data: '文件内容、文件名、对象键、访问日志、请求元数据',
    disable: '上传能力可按场景关闭；关闭后相关图片/附件功能不可用',
    crossBorder: '视实际部署、服务商区域和配置而定',
    note: '平台仅上传实现功能所必需的内容'
  },
  {
    type: 'AI OCR / 验证码识别服务',
    providers: '境内服务商（如豆包、DeepSeek 等）及境外服务商（如 OpenAI、Gemini、Claude 等），以实际部署启用的服务为准',
    purpose: '验证码、数字或图片文字识别',
    data: '验证码图片、识别请求、必要的请求元数据',
    disable: '视配置而定；未启用时不发生该类传输',
    crossBorder: '涉及境外服务商的，构成向境外提供个人信息；平台仅传输完成识别所必需的最小内容，并将依法履行告知义务、开展个人信息保护影响评估、通过标准合同等法定途径，并就向境外提供敏感个人信息单独取得您的同意。您可拒绝使用涉及境外识别服务的功能',
    note: '优先使用境内服务；仅在境内服务无法满足识别需求时使用境外服务，且仅应在最小必要范围内传输'
  },
  {
    type: '云服务器、数据库、缓存、日志、监控、备份',
    providers: '根据实际部署选择的云服务商或自建基础设施',
    purpose: '服务运行、安全审计、故障排查、备份恢复',
    data: '账号数据、配置数据、运行日志、缓存数据、备份文件',
    disable: '通常不可完全关闭，但可根据部署形态调整',
    crossBorder: '视实际部署、服务商区域和配置而定',
    note: '平台通常会通过访问控制、加密或脱敏降低风险'
  }
]

const retentionRows = [
  {
    type: '账号资料',
    duration: '账号存续期间或为实现功能所必需的期限内',
    deletion: '账号注销、字段删除、匿名化或去标识化处理',
    exception: '法律法规、审计、安全或争议处理另有要求的除外'
  },
  {
    type: '登录令牌',
    duration: '通常保存至主动退出、令牌失效、设备清理或安全策略要求失效时',
    deletion: '令牌过期、吊销、覆盖或清理本地数据',
    exception: '安全日志与审计记录可能继续保留'
  },
  {
    type: '校园账号凭证',
    duration: '以实现快速认证、会话同步或当前功能所必需的最短期限为原则',
    deletion: '关闭快速认证、撤回授权、注销账号或申请删除后删除或停止使用',
    exception: '法律法规、安全审计或风险处置需要保留的例外除外'
  },
  {
    type: '校园查询结果',
    duration: '按缓存策略、用户主动操作和功能需要确定',
    deletion: '超期清理、覆盖更新、匿名化或用户申请删除',
    exception: '纠纷处理、安全审计或法律要求留存的部分记录除外'
  },
  {
    type: '头像 / 图片 / 附件',
    duration: '内容有效期内或直至用户删除、替换、注销或平台处置',
    deletion: '删除对象、断开访问、匿名化引用',
    exception: '已被他人合法获取的副本、缓存或依法需保留的记录除外'
  },
  {
    type: '社区内容',
    duration: '用户发布期间及必要的投诉处理期内',
    deletion: '删除、下架、匿名化或限制展示',
    exception: '涉及投诉举报、侵权处理、违法线索或安全事件时可延长保存'
  },
  {
    type: '二手交易、跑腿、失物招领记录',
    duration: '自交易、履约或认领处理完成之日起不少于三年；法律法规另有更长要求的，从其规定',
    deletion: '保存期届满后删除、匿名化或限制访问',
    exception: '涉及投诉、退款、纠纷、安全或法律要求时可在必要范围内延长保存'
  },
  {
    type: '投诉举报记录',
    duration: '处理完成后在合理期限内保存',
    deletion: '删除、归档、匿名化',
    exception: '涉及诉讼、仲裁、行政处理或安全事件时可继续留存'
  },
  {
    type: '安全日志',
    duration: '按安全策略、审计要求和风险级别保存',
    deletion: '超期清理、归档脱敏或去标识化',
    exception: '发生安全事件、合规调查或法律要求时可能延长'
  },
  {
    type: '数据导出文件',
    duration: '通常仅在下载所必需的临时期限内保留',
    deletion: '超期自动失效、删除链接或删除文件',
    exception: '法律法规或审计要求另有规定的除外'
  }
]
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="$router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('about.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('about.privacyPolicyTitle') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-3xl mx-auto px-4 py-6">
      <div class="ui-panel rounded-xl bg-[var(--c-surface)] border border-[var(--c-border)] p-5 md:p-6">
        <div
          class="text-sm leading-7 text-[var(--c-text-2)] [&_p]:mb-3 [&_h3]:mt-7 [&_h3]:mb-3 [&_h3]:text-base [&_h3]:font-bold [&_h3]:text-[var(--c-text-primary)] [&_h4]:mt-5 [&_h4]:mb-2 [&_h4]:text-sm [&_h4]:font-semibold [&_h4]:text-[var(--c-text-primary)] [&_ul]:mb-3 [&_ul]:list-disc [&_ul]:pl-5 [&_ol]:mb-3 [&_ol]:list-decimal [&_ol]:pl-5 [&_li]:mb-2 [&_a]:text-[var(--c-primary)] [&_a]:underline [&_table]:mb-4 [&_table]:w-full [&_table]:border-collapse [&_table]:text-left [&_table]:align-top [&_thead]:bg-[var(--c-bg)] [&_th]:border [&_th]:border-[var(--c-border)] [&_th]:px-3 [&_th]:py-2 [&_th]:text-xs [&_th]:font-semibold [&_th]:text-[var(--c-text-primary)] [&_td]:border [&_td]:border-[var(--c-border)] [&_td]:px-3 [&_td]:py-2 [&_td]:align-top [&_strong]:font-semibold">
          <div v-if="isNonChinese" class="policy-notice policy-notice--language px-4 py-3 mb-4 text-sm leading-relaxed">
            {{ t('about.chineseOnlyNotice') }}
          </div>

          <h3 class="!mt-0 text-center">《广东二师助手隐私政策》</h3>
          <p class="text-center">发布日期：2026年2月17日</p>
          <p class="text-center">更新日期：2026年10月7日</p>
          <p class="text-center">生效日期：2026年10月14日</p>

          <div class="policy-notice policy-notice--important px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>重要提示：</strong>本政策重点说明校园账号凭证、校园数据查询、社区发布、交易/跑腿/失物招领信息、第三方服务以及用户权利与操作路径。</p>
            <p class="!mb-2"><strong>本次修订说明：</strong>本次修订内容自 2026 年 10 月 14 日起适用；生效日前仍适用修订前版本。</p>
            <p class="!mb-0"><strong>重大变化提示：</strong>涉及敏感个人信息、校园账号凭证、第三方服务或用户重要权益的重大变化时，平台通常会通过页面公告、弹窗、站内通知或其他合理方式提示您阅读；必要时，会依法取得单独同意或重新同意。</p>
          </div>

          <p><strong>本政策适用于您访问、注册、登录、使用广东二师助手及相关网站、应用程序、小程序、接口和社区功能时，平台对个人信息的收集、使用、存储、共享、公开、保护和管理方式。</strong></p>
          <p><strong>广东二师助手为校园助手类服务，不代表学校官方系统。与学校官方系统、学校官方通知或相关部门说明不一致时，以学校官方系统、官方通知或相关部门说明为准。</strong></p>

          <h3>第一条 我们如何处理您的个人信息</h3>
          <p><strong>1.1</strong> 我们会在实现功能所必需的范围内处理您的个人信息，处理场景包括但不限于：账号注册与登录、校园身份认证、课表/成绩/考试（含四六级成绩）/校园卡/图书馆/体测等查询、手机号或邮箱绑定、头像与图片上传、社区内容发布（含动态、话题、树洞、校园交友/表白、约拍等场景）、二手交易、失物招领、校园跑腿/全民快递、投诉举报、帮助与反馈、数据导出、账号注销、安全运行与日志审计，以及平台后续经告知并取得必要授权后新增的功能场景。平台暂未开通实名认证等功能；如后续开通涉及身份证件号等新增个人信息的处理，将另行单独告知并取得您的同意。</p>
          <p><strong>1.2</strong> 本政策中“个人信息”“敏感个人信息”“处理”“匿名化”“去标识化”“自动化决策”等术语的含义，与《中华人民共和国个人信息保护法》及相关国家标准的现行规定一致；本政策未另行定义的术语，按照前述规定解释。</p>
          <p><strong>1.3</strong> 我们遵循合法、正当、必要、诚信原则，在实现功能、保障安全、处理投诉纠纷、履行法律义务和改进产品所必需的范围内处理您的个人信息。</p>
          <p><strong>1.4</strong> 如某项功能另有单独授权页、隐私提示或专项规则，该等说明在对应场景中优先适用。</p>

          <h3>第二条 校园账号凭证与模拟登录</h3>
          <p><strong>2.1 收集的信息类型。</strong> 为实现校园统一认证、快速认证、课表/成绩/考试/校园卡/图书馆等查询、服务端会话同步和异常登录排查，我们可能处理校园账号、密码、验证码、登录状态、会话令牌、查询参数和查询结果。</p>
          <p><strong>2.2 使用目的。</strong> 相关信息仅用于：校园身份认证、快速认证、会话同步、按您的主动请求向学校相关系统发起查询，以及识别异常登录、重复登录、接口滥用和安全风险。平台不会将校园账号凭证用于上述目的之外的用途，也不会将其用于用户画像或营销推广。</p>
          <p><strong>2.3 处理方式。</strong> 校园数据查询系平台根据您的指示和授权代您执行；平台不代表学校官方系统，学校官方系统数据及官方通知以学校官方渠道为准。</p>
          <p><strong>2.4 处理方式与单独同意。</strong> 校园账号密码属于敏感个人信息。我们仅在您通过单独界面或勾选方式明确同意保存后，方会以加密方式存储您的密码，并采取访问控制、日志审计、有效期与失效控制等安全措施；您未同意保存的，该密码仅在完成当次认证所必需的必要时间内临时使用，完成认证后即被丢弃，不作留存。相关同意以显著方式提示，且您有权随时撤回。</p>
          <p><strong>2.5 保存期限。</strong> 校园账号凭证以实现功能所必需的最短期限为原则保存。您关闭快速认证、撤回授权、注销账号或申请删除后，平台应删除或停止使用相关凭证；法律法规、安全审计或风险处置另有要求的除外。</p>
          <p><strong>2.6 用户选择权与操作路径。</strong> 您可以拒绝保存校园凭证，但相应快速认证、会话同步或部分校园查询功能可能不可用。关闭快速认证、清除已保存凭证、撤回授权的操作路径见账号设置、隐私设置、帮助与反馈或页面公示的其他渠道。</p>

          <h3>第三条 敏感个人信息处理说明</h3>
          <p><strong>3.1</strong> 平台处理的部分信息可能属于敏感个人信息或具有较高隐私风险。我们会结合功能必要性、风险等级、处理场景和法律要求采取更严格的控制措施。<strong>其中：校园账号密码的保存以您的单独同意为前提（见第二条 2.4）；涉及境外 AI 识别服务的验证码图片传输，将依法单独取得您的同意（见第四条）。</strong></p>
          <div class="policy-table-scroll">
            <table>
              <thead>
                <tr>
                  <th>信息类型</th>
                  <th>使用场景</th>
                  <th>是否可能属于敏感个人信息</th>
                  <th>必要性</th>
                  <th>用户可否拒绝或删除</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in sensitiveInfoRows" :key="row.type">
                  <td>{{ row.type }}</td>
                  <td>{{ row.scenario }}</td>
                  <td>{{ row.sensitive }}</td>
                  <td>{{ row.necessity }}</td>
                  <td>{{ row.control }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <h3>第四条 第三方服务清单</h3>
          <p><strong>4.1</strong> 为实现校园认证、消息发送、图片上传、验证码识别、服务部署、安全审计和故障排查，平台可能根据实际部署选择第三方服务。是否启用、启用哪些服务以及是否发生跨境或境外处理，通常取决于实际部署、服务商区域和配置。</p>
          <p><strong>4.2</strong> 详细清单可进一步参阅 <a href="/policy/third-party-services">《第三方服务清单》</a>。</p>
          <div class="policy-table-scroll policy-table-scroll--wide">
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
                <tr v-for="row in thirdPartyRows" :key="row.type">
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

          <h3>第五条 个人信息共享、委托处理与公开展示</h3>
          <p><strong>5.1</strong> 我们不会出售您的个人信息。</p>
          <p><strong>5.2</strong> 在实现校园查询、消息发送、对象存储上传、AI OCR 识别、服务部署、安全审计、投诉处理或法律合规所必需的范围内，我们可能委托第三方处理、向第三方提供或与第三方共享必要信息。我们区分以下三类情形处理：</p>
          <ul>
            <li><strong>受托处理：</strong>如对象存储、短信通道、邮件服务、云基础设施等，受托方仅按照我们的指示处理信息，不得用于自身目的；</li>
            <li><strong>共同处理：</strong>如与学校相关系统在会话同步场景中各自处理必要信息的，双方依法约定各自的权利义务，并通过本政策向您告知；</li>
            <li><strong>独立向其提供：</strong>如依法向行政、司法机关提供，或依本政策 5.4 条向争议相对方提供必要记录。</li>
          </ul>
          <p>具体第三方类型、涉及信息与跨境情况见第四条及 <a href="/policy/third-party-services">《第三方服务清单》</a>。</p>
          <p><strong>5.3</strong> 当您在社区（含动态、话题、树洞、校园交友/表白、约拍）、二手交易、失物招领或校园跑腿/全民快递场景中主动发布内容时，昵称、头像、标题、描述、价格、地点、图片、联系方式等必要信息可能依产品逻辑向其他用户展示；请避免公开不必要的敏感信息，交友和约拍场景请特别注意肖像与行踪信息安全。</p>
          <p><strong>5.4</strong> 如涉及投诉举报、侵权主张、争议处理、网络安全事件、学校管理事项或行政、司法机关依法提出的要求，平台可能在必要范围内提供相关记录、日志、页面快照、联系信息和处理材料。</p>

          <h3>第六条 个人信息保存期限</h3>
          <p><strong>6.1</strong> 我们仅在实现处理目的所必需的最短期限内保存个人信息。超过保存期限后，我们会根据适用法律和实际情况进行删除、匿名化、去标识化、限制处理或归档脱敏。</p>
          <div class="policy-table-scroll">
            <table>
              <thead>
                <tr>
                  <th>信息类型</th>
                  <th>保存期限</th>
                  <th>删除或匿名化方式</th>
                  <th>例外情况</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in retentionRows" :key="row.type">
                  <td>{{ row.type }}</td>
                  <td>{{ row.duration }}</td>
                  <td>{{ row.deletion }}</td>
                  <td>{{ row.exception }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <h3>第七条 您的权利与操作路径</h3>
          <p><strong>7.1</strong> 在符合法律法规规定的前提下，您可以申请查询、更正、补充、删除个人信息，关闭快速认证或申请删除校园凭证，撤回部分授权，注销账号，删除发布内容，申请导出个人信息，以及投诉举报或联系平台处理隐私问题。</p>
          <p><strong>7.2</strong> 相关路径通常包括账号设置、隐私设置、帮助与反馈以及页面公示的其他渠道，具体以产品实际提供的功能为准。</p>
          <p><strong>7.3</strong> 为保障账号与信息安全，平台在处理您的申请前，可能要求核验身份、补充必要材料或说明具体请求范围。</p>
          <p><strong>7.4</strong> 撤回同意不影响撤回前基于您同意已经开展的处理活动的合法性；对依法必须留存的信息、涉及安全审计或纠纷处理的记录，平台可能无法立即删除。</p>

          <h3>第八条 未成年人 / 儿童个人信息保护</h3>
          <p><strong>8.1</strong> 本服务主要面向高校在校用户及相关校园用户，不主动面向不满十四周岁的儿童提供服务。</p>
          <p><strong>8.2</strong> 未满十四周岁的用户，不应在未取得监护人同意的情况下注册、登录、绑定账号、发布内容或提交个人信息；如平台发现或收到有效通知确认收集了不满十四周岁儿童的个人信息，将在合理范围内删除、限制处理或要求补充监护人同意。</p>
          <p><strong>8.3</strong> 已满十四周岁不满十八周岁的未成年人注册或使用本服务的，应在取得监护人同意后进行；平台将在首次注册环节进行年龄提示，并为其提供拒绝或删除相关信息的途径。</p>
          <p><strong>8.4</strong> 未成年人（无论是否已满十四周岁）在发布联系方式、证件、地址、取件码、照片等信息时，应特别谨慎。</p>

          <h3>第九条 个人信息保护影响评估与安全措施</h3>
          <p><strong>9.1</strong> 对敏感个人信息、第三方提供、公开展示、自动化处理、委托处理、跨境或可能影响个人权益的处理活动，平台将根据适用规则开展必要评估、记录、整改和改进。</p>
          <p><strong>9.2</strong> 平台会结合实际部署采用访问控制、最小必要、加密、脱敏、日志审计、备份恢复、异常监测和权限管理等措施保护您的个人信息，但互联网并非绝对安全环境，任何措施都无法保证百分之百安全。</p>
          <p><strong>9.3</strong> 如发生或可能发生个人信息安全事件，平台将根据适用法律采取补救措施，并在需要时以合理方式向您告知。</p>

          <h3>第十条 政策更新</h3>
          <p><strong>10.1</strong> 平台可根据法律法规、监管要求、学校系统变化、产品能力、第三方服务、个人信息处理方式或安全措施变化，对本政策进行更新。</p>
          <p><strong>10.2</strong> 本次修订内容自 2026 年 10 月 14 日起适用；生效日前仍适用修订前版本。</p>
          <p><strong>10.3</strong> 涉及敏感个人信息、校园账号凭证、第三方服务或用户重要权益的重大变化时，平台通常会通过页面公告、弹窗、站内通知或其他合理方式提示您阅读；必要时，会依法取得单独同意或重新同意。</p>

          <h3>第十一条 联系与反馈</h3>
          <p><strong>11.1</strong> 如您对本政策、个人信息处理、校园凭证、账号安全、投诉举报或删除申请有疑问，可通过应用内“帮助与反馈”联系平台，或发送邮件至隐私专用邮箱：<a href="mailto:private@gdeiassistant.cn">private@gdeiassistant.cn</a>。</p>
          <p><strong>11.2</strong> 平台已指定个人信息保护负责人，统筹个人信息保护与隐私合规事项。您可以通过上述隐私专用邮箱、应用内“帮助与反馈”或其他页面公示渠道与其联系；联系方式如有变更，将以页面公示为准。</p>
          <p><strong>11.3</strong> 一般情况下，平台将在十五个工作日内对您的个人信息权利请求或隐私投诉予以响应和处理；情形复杂需延长的，将向您说明理由和预期时限。</p>
          <p><strong>11.4</strong> 如双方就本政策相关事项发生争议，适用法律和争议处理方式以《广东二师助手用户协议》及适用法律规定为准。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.policy-table-scroll {
  max-width: 100%;
  margin-bottom: 1rem;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.policy-table-scroll table {
  min-width: 680px;
  margin-bottom: 0;
}

.policy-table-scroll--wide table {
  min-width: 760px;
}
</style>
