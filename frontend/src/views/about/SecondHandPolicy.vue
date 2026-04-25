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
      <span class="flex-1 text-center text-sm font-bold">二手交易规则</span>
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

          <h3 class="!mt-0 text-center">《二手交易规则》</h3>
          <p class="text-center">发布日期：2026年4月25日</p>
          <p class="text-center">更新日期：2026年4月25日</p>
          <p class="text-center">生效日期：2026年5月11日</p>

          <div class="bg-emerald-50 text-emerald-900 border border-emerald-200 rounded-lg px-4 py-3 mb-5 text-sm leading-relaxed">
            <p class="!mb-2"><strong>平台角色：</strong>平台主要提供信息发布、展示、搜索、沟通和辅助管理能力，不当然成为买卖、担保、支付、鉴定、仓储、运输或履约主体。</p>
            <p class="!mb-0"><strong>生效说明：</strong>本规则自 2026 年 5 月 11 日起生效；生效日前仍适用修订前相关规则。</p>
          </div>

          <h3>第一条 发布要求</h3>
          <ul>
            <li>如实填写商品名称、描述、价格、地点、图片和联系方式，不得虚构货源、状态、数量、品牌、授权或成交情况。</li>
            <li>商品图片和文字说明应尽量反映真实状态，重大瑕疵、损坏、维修史、缺件或功能受限应在发布时说明。</li>
            <li>请谨慎公开手机号、微信号、住址等联系方式，通常建议在确认对方意向后通过私密渠道进一步沟通。</li>
          </ul>

          <h3>第二条 禁售或限制发布物品</h3>
          <ul>
            <li>枪支弹药、管制器具、危险化学品、爆炸物、毒品等违法违禁物品；</li>
            <li>药品、处方药、烟酒、活体动物、现金、有价证券、证件原件、银行卡、电话卡、校园账号凭证等高风险物品；</li>
            <li>侵权盗版、假冒伪劣、破解工具、外挂、作弊材料、代写代考、非法账号、虚拟货币诈骗相关内容；</li>
            <li>其他法律法规、学校管理规定或平台规则禁止流通、展示、求购或交易的物品和服务。</li>
          </ul>

          <h3>第三条 验货、付款与线下交易安全</h3>
          <p><strong>3.1</strong> 用户应自行核验对方身份、商品状态、价格合理性、交付方式和线下见面环境。</p>
          <p><strong>3.2</strong> 建议优先选择校园内或其他相对安全、明亮、人流较多的地点当面验货，必要时保留关键聊天记录、商品照片和交易凭证。</p>
          <p><strong>3.3</strong> 平台不代收、不托管、不担保支付，不自动承担退款、退货、补差、回购、鉴定或先行赔付义务。</p>

          <h3>第四条 诈骗防范与联系方式保护</h3>
          <ul>
            <li>警惕“先付定金后拉黑”“高价回收诱导转账”“钓鱼链接”“冒充官方客服”或“脱离平台沟通”的诈骗手法。</li>
            <li>不要公开完整住址、证件、支付二维码、银行卡信息或其他与本次交易无关的高敏信息。</li>
            <li>如对方要求提供验证码、校园账号密码、完整身份证号或明显超出交易必要范围的信息，应立即停止沟通并举报。</li>
          </ul>

          <h3>第五条 纠纷、投诉与处理</h3>
          <p><strong>5.1</strong> 因商品质量、权属、价格、付款、退款、退货、交付、描述不符或线下争议引起的纠纷，原则上由交易双方自行协商处理。</p>
          <p><strong>5.2</strong> 平台可基于投诉、举报、风控、安全或合规需要，对相关内容采取隐藏、下架、删除、限制发布、限制账号、保留必要记录或协助处理纠纷等措施。</p>
          <p><strong>5.3</strong> 如涉嫌诈骗、销赃、侵权或其他违法行为，平台可依法配合有权机关处理。</p>
        </div>
      </div>
    </div>
  </div>
</template>
