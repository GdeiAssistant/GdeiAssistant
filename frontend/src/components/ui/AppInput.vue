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
  margin-bottom: 6px;
  color: var(--c-text-1);
  font-size: 13px;
  font-weight: 600;
}

.app-input__control {
  width: 100%;
  min-height: 44px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-surface);
  color: var(--c-text-1);
  font-size: 14px;
  outline: none;
  padding: 0 14px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.app-input__control:focus {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--c-primary) 18%, transparent);
}

.app-input__error {
  margin-top: 6px;
  color: var(--c-danger);
  font-size: 12px;
}
</style>
