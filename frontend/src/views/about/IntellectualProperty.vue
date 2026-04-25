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
      <span class="flex-1 text-center text-sm font-bold">{{ t('about.menuIPDeclaration') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-3xl mx-auto px-4 py-6">
      <div class="rounded-xl bg-[var(--c-surface)] border border-[var(--c-border)] p-5 md:p-6">
        <div
          class="text-sm leading-7 text-[var(--c-text-2)]
            [&_p]:mb-3
            [&_h3]:mt-7 [&_h3]:mb-3 [&_h3]:text-base [&_h3]:font-bold [&_h3]:text-[var(--c-text-primary)]
            [&_ul]:mb-3 [&_ul]:list-disc [&_ul]:pl-5
            [&_ol]:mb-3 [&_ol]:list-decimal [&_ol]:pl-5
            [&_li]:mb-2
            [&_a]:text-[var(--c-primary)] [&_a]:underline
            [&_strong]:font-semibold">
          <div v-if="isNonChinese" class="bg-amber-50 text-amber-800 border border-amber-300 rounded-lg px-4 py-3 mb-4 text-sm leading-relaxed">
            {{ t('about.chineseOnlyNotice') }}
          </div>

          <h3 class="!mt-0 text-center">《侵权投诉与反通知规则》</h3>
          <p class="text-center">发布日期：2026年2月17日</p>
          <p class="text-center">更新日期：2026年4月25日</p>
          <p class="text-center">生效日期：2026年5月11日</p>

          <div class="bg-amber-50 text-amber-900 border border-amber-200 rounded-lg px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>适用范围：</strong>当您认为平台内内容侵犯著作权、商标权、肖像权、名誉权、隐私权、商业秘密或其他合法权益时，可依本规则提交投诉；被投诉用户也可提交反通知或申诉。</p>
            <p class="!mb-0"><strong>提交提醒：</strong>请尽量通过帮助与反馈、站内工单、仓库公开反馈渠道或平台后续公布的专门入口提交材料；不适合公开披露的敏感材料，请避免直接在公开页面贴出完整细节。</p>
          </div>

          <h3>第一条 用户发布内容的权利边界</h3>
          <p><strong>1.1</strong> 用户发布内容不得侵犯他人的著作权、商标权、专利权、肖像权、名誉权、隐私权、商业秘密、个人信息权益或其他合法权益。</p>
          <p><strong>1.2</strong> 图片、头像、截图、课程资料、试卷、课件、教材、付费资料、软件、Logo 等内容，在发布前应确认您具有合法权利或已取得必要授权。</p>
          <p><strong>1.3</strong> 本规则不替代行政投诉、报警、诉讼、仲裁或学校管理程序。对于权属复杂、争议重大或需要线下调查的事项，平台可要求当事人提供进一步材料，或建议向有权机关、学校管理部门或依法有权的争议解决机构处理。</p>

          <h3>第二条 侵权投诉应提交的材料</h3>
          <p><strong>2.1</strong> 投诉材料通常应包含以下内容：</p>
          <ol>
            <li>权利人或代理人的身份信息，以及必要的主体资格或授权证明；</li>
            <li>权属证明，例如作品原始底稿、授权文件、登记材料、商标注册信息、肖像或身份说明等；</li>
            <li>被投诉内容的链接、页面路径、账号昵称、发布时间、截图或其他足以定位内容的信息；</li>
            <li>侵权说明，包括侵权比对、未获授权说明、权利受损情况、传播影响等；</li>
            <li>有效联系方式，以便平台在必要时与您沟通补充材料或反馈处理结果；</li>
            <li>对材料真实性、完整性和合法性负责的声明。</li>
          </ol>
          <p><strong>2.2</strong> 如材料明显不足、无法定位内容、权属证明不清或超出平台处理能力，平台可要求补充材料，并在补充完成前暂缓处理。</p>

          <h3>第三条 平台收到投诉后的处理</h3>
          <p><strong>3.1</strong> 平台收到投诉后，会结合材料完整性、初步证据、传播风险、内容类型、用户历史情况和法律要求进行审查。</p>
          <p><strong>3.2</strong> 对明显违法、涉嫌严重侵权、泄露敏感个人信息、冒用身份、存在持续扩散风险或其他紧急情形，平台可先行采取删除、屏蔽、限制展示、断开链接、限制账号功能、保留记录等必要措施。</p>
          <p><strong>3.3</strong> 平台通常会在合理期限内处理，但不承诺固定小时数内完成，也不当然对争议实体权利归属作出最终裁判。</p>

          <h3>第四条 反通知与申诉机制</h3>
          <p><strong>4.1</strong> 被投诉用户认为内容不侵权、已获授权、属于合理使用、事实真实或投诉存在明显错误的，可提交反通知、申诉或不侵权说明。</p>
          <p><strong>4.2</strong> 反通知通常应包含：</p>
          <ol>
            <li>被投诉用户或代理人的身份信息及必要授权材料；</li>
            <li>被采取措施内容的定位信息；</li>
            <li>不侵权说明、授权证明、原创证明、合法来源说明或其他相关材料；</li>
            <li>真实性承诺和有效联系方式。</li>
          </ol>
          <p><strong>4.3</strong> 平台可根据材料将反通知转送投诉方，并结合双方材料决定恢复内容、继续限制、删除内容、要求补充材料或维持现状。</p>

          <h3>第五条 恶意投诉、恶意申诉与重复侵权</h3>
          <p><strong>5.1</strong> 恶意投诉、伪造权属、虚构事实、滥用权利、恶意申诉、伪造授权或重复发布明显侵权内容的，平台可视情况采取限制发布、限制申诉、封禁账号、保留记录并依法协助处理。</p>
          <p><strong>5.2</strong> 因通知不实、恶意投诉、恶意反通知或其他不当行为给平台、其他用户或第三方造成损失的，相关方应依法承担责任。</p>

          <h3>第六条 联系与材料提交建议</h3>
          <p><strong>6.1</strong> 若平台当前未提供专门自助入口，您可通过应用内帮助与反馈、站内工单、仓库公开反馈渠道或后续公布的专门渠道提交侵权投诉、反通知或申诉材料。</p>
          <p><strong>6.2</strong> 公开渠道不适合提交真实证件号、完整联系方式、原始密钥、原始取件码、未打码证件照或其他高敏材料；必要时请先使用打码版本说明问题，再根据平台提示补充更完整的证明。</p>
          <p><strong>6.3</strong> 本规则未尽事宜，适用 <a href="/agreement">《用户协议》</a>、<a href="/policy/social">《社区准则》</a>、<a href="/policy/privacy">《隐私政策》</a> 及适用法律规定。</p>
        </div>
      </div>
    </div>
  </div>
</template>
