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
      <span class="flex-1 text-center text-sm font-bold">校园跑腿 / 全民快递规则</span>
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

          <h3 class="!mt-0 text-center">《校园跑腿 / 全民快递规则》</h3>
          <p class="text-center">发布日期：2026年4月25日</p>
          <p class="text-center">更新日期：2026年4月25日</p>
          <p class="text-center">生效日期：2026年5月11日</p>

          <div class="bg-amber-50 text-amber-900 border border-amber-200 rounded-lg px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>平台角色：</strong>平台主要提供任务信息发布、接单沟通、状态展示和辅助管理能力，不当然成为快递企业、承运人、仓储保管人、代收代付机构或保险人。</p>
            <p class="!mb-0"><strong>生效说明：</strong>本规则自 2026 年 5 月 11 日起生效；生效日前仍适用修订前相关规则。</p>
          </div>

          <h3>第一条 发布、接单与履约</h3>
          <ul>
            <li>发布人应如实填写取件地点、送达地点、报酬、物品类型和必要说明，不得虚构任务、恶意放鸽子或隐藏关键风险。</li>
            <li>接单人应结合自身时间、路线、能力和风险判断是否接单，不得恶意接单后失联、冒领、拖延或转交不明第三方。</li>
            <li>平台不代收、不托管、不结算任务报酬，也不保证每个任务必然被接单、按时完成或无争议完成。</li>
          </ul>

          <h3>第二条 敏感信息保护</h3>
          <ul>
            <li>取件码、地址、手机号、姓名、门禁信息、宿舍号、物流凭证等通常属于高敏信息，应最小化展示并仅向必要相对方提供。</li>
            <li>不要在公开页面发布完整取件码、完整住址或其他无关高敏信息；如需补充细节，通常建议通过私密渠道沟通。</li>
            <li>完成履约后，双方应视需要及时删除不再必要的高敏信息或申请平台处理相关记录。</li>
          </ul>

          <h3>第三条 禁止承接或发布的物品</h3>
          <ul>
            <li>违法违规物品、危险品、易燃易爆物、毒害物、活体动物、需要特殊资质运输保管的物品；</li>
            <li>现金、有价证券、银行卡、证件原件、高价值贵重物品、保密资料或明显超出普通校园跑腿能力范围的物品；</li>
            <li>法律法规、学校管理规定或平台规则禁止寄递、转送、代取、代送的其他物品。</li>
          </ul>

          <h3>第四条 异常、取消、丢失、损坏与纠纷</h3>
          <p><strong>4.1</strong> 因取消、超时、无法取件、错取、漏送、丢失、损坏、地址错误、沟通不畅、双方失联等引起的争议，原则上由发布人和接单人自行协商处理。</p>
          <p><strong>4.2</strong> 平台可根据投诉、举报、风控或安全需要，对任务内容、账号和相关记录采取隐藏、下架、限制接单、限制发布、保留必要证据或协助处理等措施。</p>
          <p><strong>4.3</strong> 如争议涉及违法线索、人身或财产安全风险，平台可依法配合有权机关处理。</p>

          <h3>第五条 安全提示</h3>
          <ul>
            <li>尽量选择校园内相对安全、明亮、人流较多的地点交接。</li>
            <li>如对方要求提供与履约无关的验证码、账号密码、支付密码或明显超出必要范围的信息，请立即停止操作。</li>
            <li>请勿承接违法违规、危险、贵重或明显超出自身能力范围的任务。</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
