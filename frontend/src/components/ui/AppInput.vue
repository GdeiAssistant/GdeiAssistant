<script setup>
defineProps({
  label: {
    type: String,
    default: '',
  },
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: 'text',
  },
  error: {
    type: String,
    default: '',
  },
})

defineEmits(['update:modelValue'])
</script>

<template>
  <div>
    <label
      v-if="label"
      class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block"
    >
      {{ label }}
    </label>
    <input
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm bg-[var(--c-surface)] text-[var(--c-text-1)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <p
      v-if="error"
      class="text-xs text-[var(--c-danger)] mt-1"
    >
      {{ error }}
    </p>
  </div>
</template>
