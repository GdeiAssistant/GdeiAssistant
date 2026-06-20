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
  <div class="app-input">
    <label v-if="label" class="app-input__label">
      {{ label }}
    </label>
    <input
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      class="app-input__control"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <p v-if="error" class="app-input__error">
      {{ error }}
    </p>
  </div>
</template>

<style scoped>
.app-input__label {
  display: block;
  margin-bottom: 8px;
  color: var(--c-text-2);
  font-size: 13px;
  font-weight: 780;
}

.app-input__control {
  width: 100%;
  border: 1px solid var(--c-border);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.76);
  color: var(--c-text-1);
  font-size: 14px;
  outline: none;
  padding: 12px 14px;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.app-input__control:focus {
  border-color: color-mix(in srgb, var(--c-primary) 52%, var(--c-border));
  background: var(--c-surface);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--c-primary) 13%, transparent);
}

.app-input__error {
  margin-top: 6px;
  color: var(--c-danger);
  font-size: 12px;
}

[data-theme="dark"] .app-input__control {
  background: rgba(24, 32, 43, 0.76);
}
</style>
