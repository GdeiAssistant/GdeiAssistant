<template>
  <div class="social-image-wrap" :style="{ aspectRatio: image.width / image.height }">
    <button v-if="displaySrc" type="button" class="social-image-open" :aria-label="$t('social.viewImage')" @click="open">
      <img :src="displaySrc" :alt="$t('social.imageMessage')" class="social-image" />
    </button>
    <button v-else-if="failed" type="button" class="social-image-reload" @click="reload">{{ $t('social.imageLoadError') }} · {{ $t('common.retry') }}</button>
    <span v-else class="social-image-loading">{{ $t('common.loading') }}</span>
  </div>
</template>
<script setup>
import { computed, toRef } from 'vue'
import { useChatImage } from '../../composables/useChatImage.js'
const props = defineProps({ image: { type: Object, required: true }, localPreview: { type: String, default: '' } })
const emit = defineEmits(['open'])
const url = computed(() => props.image.url || '')
const { src, failed, reload } = useChatImage(toRef(url))
const displaySrc = computed(() => /^blob:/.test(props.localPreview) ? props.localPreview : src.value)
function open() { if (displaySrc.value) emit('open', displaySrc.value) }
</script>
