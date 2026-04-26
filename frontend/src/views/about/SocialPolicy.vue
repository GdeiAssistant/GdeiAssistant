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
      <span class="flex-1 text-center text-sm font-bold">{{ t('about.menuCommunityGuidelines') }}</span>
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

          <h3 class="!mt-0 text-center">《广东二师助手社区准则》</h3>
          <p class="text-center">发布日期：2026年2月17日</p>
          <p class="text-center">更新日期：2026年4月25日</p>
          <p class="text-center">生效日期：2026年5月11日</p>

          <div class="bg-red-50 text-red-800 border border-red-200 rounded-lg px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>适用范围：</strong>本准则适用于普通社区内容、二手交易、失物招领、校园跑腿/全民快递、评论互动、资料页展示以及投诉举报、申诉等相关行为。</p>
            <p class="!mb-2"><strong>隐私提醒：</strong>证件、学生卡、取件码、地址、手机号、邮箱、聊天记录和其他个人信息应最小化展示，通常建议通过私密渠道沟通，不要在公开页面发布完整信息。</p>
            <p class="!mb-0"><strong>本次修订说明：</strong>本次修订内容将自生效日期起适用；生效日前仍适用修订前版本。生效后继续使用社区或相关功能的，视为您已阅读并接受修订后的规则；法律法规另有规定或平台另行要求单独同意的，从其规定或以您的单独同意为准。</p>
          </div>

          <h3>第一条 社区定位与专项规则</h3>
          <p><strong>1.1</strong> 广东二师助手致力于提供真实、友善、克制、对校园生活有帮助的交流环境，但平台不承诺对所有内容进行事前人工审核，也不当然对用户间线下行为承担保证责任。</p>
          <p><strong>1.2</strong> 二手交易、失物招领、校园跑腿/全民快递另有专项规则，分别详见 <a href="/policy/secondhand">《二手交易规则》</a>、<a href="/policy/lostandfound">《失物招领规则》</a>、<a href="/policy/errand">《校园跑腿/全民快递规则》</a>。</p>

          <h3>第二条 倡导内容</h3>
          <ul>
            <li>真实分享校园学习、生活、就业、公益、互助、失物提醒和闲置转让信息；</li>
            <li>理性讨论、就事论事、尊重他人，不进行人身攻击、羞辱、煽动对立或恶意起哄；</li>
            <li>在交易、认领、跑腿和线下交接时保持审慎，尽量核验身份、场所和关键信息。</li>
          </ul>

          <h3>第三条 禁止内容与禁止行为</h3>
          <p><strong>3.1</strong> 您不得利用平台发布、传播、展示、导流、组织或实施以下内容和行为：</p>
          <ul>
            <li>违法违规、暴力、淫秽色情、低俗擦边、歧视侮辱、煽动仇恨、造谣传谣、侵犯隐私或其他不适宜校园场景的信息；</li>
            <li>公开他人证件、学生卡、照片、联系方式、聊天记录、取件码、住址、宿舍信息、门禁信息、消费记录或其他个人信息；</li>
            <li>虚假交易、钓鱼链接、刷单引流、诈骗、诱导转账、冒充官方、冒领失物、伪造身份、伪造截图或误导性宣传；</li>
            <li>发布、求购、寄递或转让管制器具、危险品、药品/处方药、烟酒、违禁品、非法账号、虚拟货币诈骗、侵权盗版、考试作弊材料、代写代考等内容；</li>
            <li>通过脚本、爬虫、批量请求、逆向、撞库、恶意举报或其他方式干扰平台、学校系统或第三方服务的正常运行；</li>
            <li>借助 AI、深度合成、换脸、伪造截图或伪造聊天记录冒充他人、误导交易相对方或制造“官方授权”“学校认可”等混淆表述。</li>
          </ul>

          <h3>第四条 普通社区内容要求</h3>
          <p><strong>4.1</strong> 昵称、头像、签名、帖子、评论、图片和外链，应具备合法来源，并且不侵犯他人的著作权、商标权、肖像权、名誉权、隐私权、商业秘密或其他合法权益。</p>
          <p><strong>4.2</strong> 涉及他人个人经历、感情纠纷、学业表现、处罚情况或线下冲突时，请避免发布足以识别特定个人的信息，避免“挂人”、网络暴力或未经核实的指控。</p>

          <h3>第五条 二手交易、失物招领与校园跑腿/全民快递特别要求</h3>
          <p><strong>5.1</strong> 二手交易信息应真实说明商品状态、价格、图片、地点和联系方式，不得隐瞒关键瑕疵或以低价引流、虚假成交、先付款后拉黑等方式实施欺诈。</p>
          <p><strong>5.2</strong> 失物招领信息应尽量对证件号、学生卡号、二维码、照片和联系方式做打码或最小化展示，并注意防范冒领。</p>
          <p><strong>5.3</strong> 校园跑腿/全民快递信息中的取件码、地址、手机号、姓名等属于高敏信息，通常应仅向必要的相对方展示；不得承接违法违规、危险、贵重或明显超出自身能力范围的任务。</p>
          <p><strong>5.4</strong> 用户应自行核验对方身份、商品真伪、认领依据、交接场所和履约条件。平台提供信息展示、沟通、撮合或辅助管理能力，不当然成为交易、认领、运输或保管主体。</p>

          <h3>第六条 联系方式与私下交易风险</h3>
          <p><strong>6.1</strong> 请谨慎公开手机号、微信号、QQ 号、邮箱、宿舍地址、收件地址、门牌信息和支付信息。若确需交换联系方式，建议在确认对方身份和需求后再通过私密渠道发送。</p>
          <p><strong>6.2</strong> 对于线下见面、当面交易、失物认领、取件交接和跑腿履约，请尽量选择校园内相对安全、明亮、人流较多的地点，并自行判断是否继续进行。</p>

          <h3>第七条 平台处理措施</h3>
          <p><strong>7.1</strong> 对涉嫌违反本准则的内容或行为，平台可根据情节和风险采取以下措施：</p>
          <ul>
            <li>对内容进行折叠、隐藏、下架、删除、断开链接或限制展示；</li>
            <li>对账号采取限制发布、限制评论、限制私信、限制接单、禁言、冻结、封禁等措施；</li>
            <li>保留必要证据、页面快照、日志、订单状态、投诉材料和处理记录；</li>
            <li>配合投诉举报、争议处理、侵权通知与反通知、学校管理和安全审计；</li>
            <li>涉嫌违法时，依法配合有权机关或依法有权的争议解决机构处理。</li>
          </ul>
          <p><strong>7.2</strong> 平台可根据投诉、举报、风控、安全、合规、传播范围、历史违规记录和用户配合整改情况决定处置层级，不承诺对所有申诉作出有利于任一方的结论。</p>

          <h3>第八条 投诉举报与申诉</h3>
          <p><strong>8.1</strong> 如您发现违法违规、诈骗、骚扰、隐私泄露、冒领失物、虚假交易或其他不当内容，可通过应用内“帮助与反馈”或页面公示的其他渠道提交举报。</p>
          <p><strong>8.2</strong> 涉及侵权投诉与反通知的，请参阅 <a href="/policy/intellectualproperty">《侵权投诉与反通知规则》</a>。</p>
          <p><strong>8.3</strong> 如您对平台依据本准则作出的处理结果存在异议，可提交申诉材料。平台将在合理期限内结合证据情况处理，但不承诺必然恢复内容或账号权限。</p>

          <h3>第九条 规则更新</h3>
          <p><strong>9.1</strong> 平台可根据法律法规、学校管理要求、产品能力、风险控制需要或专项规则变化，对本准则进行修订。</p>
          <p><strong>9.2</strong> 涉及用户重要权益、交易/跑腿/失物规则、敏感信息保护或违规处理方式的重要变化时，平台通常会通过页面公告、弹窗、站内通知或其他合理方式提示您阅读。</p>
        </div>
      </div>
    </div>
  </div>
</template>
