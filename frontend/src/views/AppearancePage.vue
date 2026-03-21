<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { setLocale as setI18nLocale } from '@/i18n'
import { getThemeMode, setThemeMode, getFontScaleStep, setFontScaleStep } from '@/theme'

const { t } = useI18n()

const theme = ref(getThemeMode())
const fontStep = ref(getFontScaleStep())

const themeOptions = [
  { value: 'system', labelKey: 'appearance.theme.system' },
  { value: 'light', labelKey: 'appearance.theme.light' },
  { value: 'dark', labelKey: 'appearance.theme.dark' },
]

const fontLabels = [
  'appearance.font.small',
  'appearance.font.standard',
  'appearance.font.large',
  'appearance.font.xlarge',
]

const fontScales = [0.85, 1.0, 1.15, 1.3]

const locales = [
  { code: 'zh-CN', label: '简体中文' },
  { code: 'zh-HK', label: '繁體中文（香港）' },
  { code: 'zh-TW', label: '繁體中文（台灣）' },
  { code: 'en', label: 'English' },
  { code: 'ja', label: '日本語' },
  { code: 'ko', label: '한국어' },
]

const selectedLocale = ref(localStorage.getItem('locale') || 'zh-CN')

function onThemeChange(value) {
  theme.value = value
  setThemeMode(value)
}

function onFontChange(e) {
  const step = Number(e.target.value)
  fontStep.value = step
  setFontScaleStep(step)
}

function onLocaleChange(code) {
  selectedLocale.value = code
  setI18nLocale(code)
}
</script>

<template>
  <div class="appearance-page">
    <div class="appearance-header">
      <a class="back-link" @click="$router.back()">←</a>
      <h2>{{ t('appearance.title') }}</h2>
    </div>

    <div class="appearance-section">
      <h3 class="section-title">{{ t('appearance.theme.label') }}</h3>
      <div class="option-list">
        <div
          v-for="opt in themeOptions"
          :key="opt.value"
          class="option-item"
          @click="onThemeChange(opt.value)"
        >
          <span>{{ t(opt.labelKey) }}</span>
          <span v-if="theme === opt.value" class="check-icon">✓</span>
        </div>
      </div>
    </div>

    <div class="appearance-section">
      <h3 class="section-title">{{ t('appearance.font.label') }}</h3>
      <div class="font-card">
        <input
          type="range"
          min="0"
          max="3"
          step="1"
          :value="fontStep"
          class="font-slider"
          @input="onFontChange"
        />
        <div class="font-labels">
          <span v-for="(lbl, i) in fontLabels" :key="i">{{ t(lbl) }}</span>
        </div>
        <p class="font-preview" :style="{ fontSize: (16 * fontScales[fontStep]) + 'px' }">
          {{ t('appearance.font.preview') }}
        </p>
      </div>
    </div>

    <div class="appearance-section">
      <h3 class="section-title">{{ t('appearance.language.label') }}</h3>
      <div class="option-list">
        <div
          v-for="loc in locales"
          :key="loc.code"
          class="option-item"
          @click="onLocaleChange(loc.code)"
        >
          <span>{{ loc.label }}</span>
          <span v-if="selectedLocale === loc.code" class="check-icon">✓</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.appearance-page {
  min-height: 100vh;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
}
.appearance-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-divider);
}
.appearance-header h2 { margin: 0; font-size: 1.1rem; }
.back-link {
  font-size: 1.2rem;
  cursor: pointer;
  color: var(--color-primary);
  text-decoration: none;
}
.section-title {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  padding: 16px 16px 8px;
  margin: 0;
}
.option-list {
  background: var(--color-surface);
}
.option-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  border-bottom: 1px solid var(--color-divider);
}
.option-item:hover {
  background: var(--color-bg-tertiary);
}
.check-icon {
  color: var(--color-primary);
  font-weight: 600;
}
.font-card {
  background: var(--color-surface);
  padding: 16px;
}
.font-slider {
  width: 100%;
  accent-color: var(--color-primary);
}
.font-labels {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  margin-top: 4px;
}
.font-preview {
  margin-top: 12px;
  color: var(--color-text-primary);
}
</style>
