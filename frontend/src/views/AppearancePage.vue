<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { setLocale as setI18nLocale } from '@/i18n'
import { getThemeMode, setThemeMode, getFontScaleStep, setFontScaleStep } from '@/theme'

const { t, locale } = useI18n()

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

const selectedLocale = computed(() => locale.value)

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
  setI18nLocale(code)
}
</script>

<template>
  <div class="appearance-page">
    <div class="appearance-header">
      <button type="button" class="back-link" @click="$router.back()">←</button>
      <h2>{{ t('appearance.title') }}</h2>
    </div>

    <div class="appearance-section">
      <h3 class="section-title">{{ t('appearance.theme.label') }}</h3>
      <div class="option-list">
        <button
          v-for="opt in themeOptions"
          :key="opt.value"
          type="button"
          class="option-item"
          @click="onThemeChange(opt.value)"
        >
          <span>{{ t(opt.labelKey) }}</span>
          <span v-if="theme === opt.value" class="check-icon">✓</span>
        </button>
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
        <button
          v-for="loc in locales"
          :key="loc.code"
          type="button"
          class="option-item"
          @click="onLocaleChange(loc.code)"
        >
          <span>{{ loc.label }}</span>
          <span v-if="selectedLocale === loc.code" class="check-icon">✓</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.appearance-page {
  min-height: 100vh;
  padding: 22px clamp(14px, 4vw, 42px) 48px;
  color: var(--c-text-1);
  background:
    radial-gradient(circle at 18% 10%, rgba(175, 225, 255, 0.42), transparent 28%),
    radial-gradient(circle at 82% 8%, rgba(183, 238, 207, 0.34), transparent 24%),
    var(--c-bg);
}

.appearance-header {
  display: flex;
  max-width: 760px;
  align-items: center;
  gap: 12px;
  margin: 0 auto 18px;
}

.appearance-header h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 24px;
  font-weight: 900;
  letter-spacing: -0.03em;
}

.back-link {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--c-primary);
  cursor: pointer;
  font: inherit;
  font-size: 1.2rem;
  text-decoration: none;
}

.appearance-section {
  max-width: 760px;
  margin: 14px auto 0;
  border: 1px solid rgba(205, 222, 226, 0.78);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 16px 38px rgba(32, 69, 78, 0.07);
  overflow: hidden;
  backdrop-filter: blur(14px);
}

.section-title {
  margin: 0;
  padding: 16px 18px 10px;
  color: var(--c-text-2);
  font-size: 13px;
  font-weight: 860;
}

.option-list {
  padding: 0 10px 10px;
}

.option-item {
  display: flex;
  width: 100%;
  min-height: 48px;
  align-items: center;
  justify-content: space-between;
  border: 0;
  border-radius: 16px;
  background: transparent;
  color: var(--c-text-1);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 720;
  padding: 0 12px;
  text-align: left;
}

.option-item:hover {
  background: var(--c-primary-50);
}

.check-icon {
  color: var(--c-primary);
  font-weight: 900;
}

.font-card {
  padding: 4px 18px 18px;
}

.font-slider {
  width: 100%;
  accent-color: var(--c-primary);
}

.font-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  color: var(--c-text-2);
  font-size: 12px;
}

.font-preview {
  margin: 16px 0 0;
  border: 1px solid var(--c-border);
  border-radius: 18px;
  background: rgba(246, 251, 255, 0.78);
  color: var(--c-text-1);
  padding: 16px;
}

[data-theme="dark"] .appearance-page {
  background: var(--c-bg);
}

[data-theme="dark"] .appearance-section,
[data-theme="dark"] .back-link,
[data-theme="dark"] .font-preview {
  border-color: rgba(45, 58, 73, 0.86);
  background: rgba(20, 27, 37, 0.86);
}
</style>
