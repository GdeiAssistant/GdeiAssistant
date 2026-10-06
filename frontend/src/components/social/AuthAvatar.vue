<template>
  <img
    v-if="src"
    :src="src"
    :alt="alt"
    class="object-cover"
    :class="imgClass"
  />
  <div
    v-else
    class="bg-[var(--c-bg)] text-[var(--c-text-tertiary)] flex items-center justify-center text-xs"
    :class="imgClass"
  >{{ placeholder }}</div>
</template>

<script setup>
import { toRef } from 'vue'
import { useAuthAvatar } from '../../composables/useAuthAvatar.js'

const props = defineProps({
  url: { type: String, default: '' },
  alt: { type: String, default: '' },
  imgClass: { type: String, default: 'w-10 h-10 rounded-full' },
  placeholder: { type: String, default: '?' }
})

const { src } = useAuthAvatar(toRef(props, 'url'))
</script>
