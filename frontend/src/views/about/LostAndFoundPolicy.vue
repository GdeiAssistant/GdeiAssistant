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
      <span class="flex-1 text-center text-sm font-bold">失物招领规则</span>
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

          <h3 class="!mt-0 text-center">《失物招领规则》</h3>
          <p class="text-center">发布日期：2026年4月25日</p>
          <p class="text-center">更新日期：2026年4月25日</p>
          <p class="text-center">生效日期：2026年5月11日</p>

          <div class="bg-blue-50 text-blue-900 border border-blue-200 rounded-lg px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>平台角色：</strong>平台主要提供失物信息发布、沟通、认领辅助和必要的记录留存能力，不当然成为失物保管、权属认定或赔偿主体。</p>
            <p class="!mb-0"><strong>生效说明：</strong>本规则自 2026 年 5 月 11 日起生效；生效日前仍适用修订前相关规则。</p>
          </div>

          <h3>第一条 发布要求</h3>
          <ul>
            <li>请如实填写拾获或遗失时间、地点、物品类别、特征描述和联系信息，不得虚构失物、冒用他人失物或借机引流、收费、诈骗。</li>
            <li>证件、学生卡、银行卡、车票、快递面单、聊天记录截图等敏感内容应尽量打码或最小化展示。</li>
            <li>请避免公开完整证件号、学生卡号、二维码、手机号、住址和其他足以导致冒领的信息。</li>
          </ul>

          <h3>第二条 认领核验与防冒领</h3>
          <p><strong>2.1</strong> 认领人应提供足以证明权属的信息，如物品特征、遗失时间地点、内含物、购买记录或其他合理证明。</p>
          <p><strong>2.2</strong> 拾获人和认领人都应注意防冒领、防钓鱼和防诈骗，不要轻易公开完整高敏信息或在不安全地点交接。</p>
          <p><strong>2.3</strong> 涉及证件、学生卡、银行卡等高风险物品时，通常建议优先移交学校管理部门、失物招领点、保卫部门或其他有权处理机构。</p>

          <h3>第三条 线下交接与安全提示</h3>
          <ul>
            <li>尽量选择校园内相对安全、明亮、人流较多的地点交接，并自行判断是否需要同伴陪同。</li>
            <li>如对方要求提供与认领无关的验证码、账号密码、支付信息或其他明显超出必要范围的信息，请立即停止沟通。</li>
            <li>若涉及贵重物品、证件原件或权属争议较大的物品，建议通过学校管理部门或其他适当渠道处理。</li>
          </ul>

          <h3>第四条 权属争议、投诉与处理</h3>
          <p><strong>4.1</strong> 因权属争议、冒领、描述不实、隐私泄露或线下交接纠纷引起的问题，原则上由相关用户自行协商处理。</p>
          <p><strong>4.2</strong> 平台可根据投诉、举报、风险控制和安全需要，对相关内容采取隐藏、下架、删除、限制账号、保留必要记录或协助处理等措施。</p>
          <p><strong>4.3</strong> 如涉嫌冒领、诈骗、侵权或其他违法行为，平台可依法配合有权机关处理。</p>
        </div>
      </div>
    </div>
  </div>
</template>
