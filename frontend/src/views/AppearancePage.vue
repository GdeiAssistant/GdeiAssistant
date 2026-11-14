<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { setLocale as setI18nLocale } from '@/i18n'
import {
  getThemeMode,
  setThemeMode,
  getThemeAccent,
  setThemeAccent,
  getFontScaleStep,
  setFontScaleStep
} from '@/theme'
import { LOCALE_OPTIONS, resolveSupportedLocale } from '@/constants/localeOptions'
import { Check, ChevronLeft } from 'lucide-vue-next'

const { t, locale } = useI18n()

const theme = ref(getThemeMode())
const accent = ref(getThemeAccent())
const fontStep = ref(getFontScaleStep())

const themeOptions = [
  { value: 'system', labelKey: 'appearance.theme.system' },
  { value: 'light', labelKey: 'appearance.theme.light' },
  { value: 'dark', labelKey: 'appearance.theme.dark' },
]

const accentOptions = [
  { value: 'banyan', labelKey: 'appearance.accent.banyan', lightHex: '#0E7C61', darkHex: '#2DD4BF', softHex: '#E4F3EE' },
  { value: 'kapok', labelKey: 'appearance.accent.kapok', lightHex: '#C85A32', darkHex: '#FB923C', softHex: '#FBECE6' },
  { value: 'haizhu', labelKey: 'appearance.accent.haizhu', lightHex: '#1D4ED8', darkHex: '#60A5FA', softHex: '#E8EFFD' },
  { value: 'bauhinia', labelKey: 'appearance.accent.bauhinia', lightHex: '#6D28D9', darkHex: '#A78BFA', softHex: '#F1EAFF' },
]

const fontLabels = [
  'appearance.font.small',
  'appearance.font.standard',
  'appearance.font.large',
  'appearance.font.xlarge',
]

const fontScales = [0.85, 1.0, 1.15, 1.3]

const locales = LOCALE_OPTIONS

const selectedLocale = computed(() => resolveSupportedLocale(locale.value))

function onThemeChange(value) {
  theme.value = value
  setThemeMode(value)
}

function onAccentChange(value) {
  accent.value = setThemeAccent(value)
}

function onFontChange(e) {
  const step = Number(e.target.value)
  fontStep.value = step
  setFontScaleStep(step)
}

function onLocaleChange(code) {
  setI18nLocale(code)
}

function isThemeSelected(value) {
  return theme.value === value
}

function isAccentSelected(value) {
  return accent.value === value
}

function isLocaleSelected(code) {
  return selectedLocale.value === code
}
</script>

<template>
  <div class="appearance-page subpage">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="$router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <h1 class="subpage-bar__title">{{ t('appearance.title') }}</h1>
      <span aria-hidden="true"></span>
    </div>

    <div class="appearance-body">
      <div class="appearance-heading" aria-hidden="true">{{ t('appearance.title') }}</div>

      <section class="appearance-section">
        <h3 class="section-title">{{ t('appearance.theme.label') }}</h3>
        <div class="theme-grid" role="radiogroup" :aria-label="t('appearance.theme.label')">
          <button
            v-for="opt in themeOptions"
            :key="opt.value"
            type="button"
            role="radio"
            :aria-checked="isThemeSelected(opt.value)"
            class="option-item theme-option"
            :class="{ 'option-item--active': isThemeSelected(opt.value) }"
            @click="onThemeChange(opt.value)"
          >
            <span class="theme-swatch" :class="`theme-swatch--${opt.value}`" aria-hidden="true">
              <i></i><i></i><i></i>
            </span>
            <span class="theme-option__label">
              <span>{{ t(opt.labelKey) }}</span>
              <Check v-if="isThemeSelected(opt.value)" class="check-icon" :size="16" aria-hidden="true" />
            </span>
          </button>
        </div>
      </section>

      <section class="appearance-section">
        <h3 class="section-title">{{ t('appearance.accent.label') }}</h3>
        <div class="accent-grid" role="radiogroup" :aria-label="t('appearance.accent.label')">
          <button
            v-for="opt in accentOptions"
            :key="opt.value"
            type="button"
            role="radio"
            :aria-checked="isAccentSelected(opt.value)"
            class="option-item accent-option"
            :class="{ 'option-item--active': isAccentSelected(opt.value) }"
            @click="onAccentChange(opt.value)"
          >
            <span class="accent-option__meta">
              <span
                class="accent-swatch"
                :style="{ backgroundColor: opt.lightHex, '--swatch-dark': opt.darkHex, '--swatch-soft': opt.softHex }"
                aria-hidden="true"
              ></span>
              <span class="accent-option__name">{{ t(opt.labelKey) }}</span>
            </span>
            <Check v-if="isAccentSelected(opt.value)" class="check-icon" :size="16" aria-hidden="true" />
          </button>
        </div>
      </section>

      <section class="appearance-section">
        <h3 class="section-title">{{ t('appearance.font.label') }}</h3>
        <div class="font-card">
          <p class="font-preview" :style="{ fontSize: (16 * fontScales[fontStep]) + 'px' }">
            {{ t('appearance.font.preview') }}
          </p>
          <input
            type="range"
            min="0"
            max="3"
            step="1"
            :value="fontStep"
            class="font-slider"
            :style="{ '--fill': (fontStep / 3) * 100 + '%' }"
            @input="onFontChange"
          />
          <div class="font-labels">
            <span v-for="(lbl, i) in fontLabels" :key="i" :class="{ 'is-active': i === fontStep }">{{ t(lbl) }}</span>
          </div>
        </div>
      </section>

      <section class="appearance-section">
        <h3 class="section-title">{{ t('appearance.language.label') }}</h3>
        <div class="option-list">
          <button
            v-for="loc in locales"
            :key="loc.code"
            type="button"
            class="option-item"
            :class="{ 'option-item--active': isLocaleSelected(loc.code) }"
            @click="onLocaleChange(loc.code)"
          >
            <span>{{ loc.label }}</span>
            <Check v-if="isLocaleSelected(loc.code)" class="check-icon" :size="18" aria-hidden="true" />
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.subpage-bar__title {
  margin: 0;
}

.appearance-page {
  min-height: 100vh;
  color: var(--c-text-1);
  background: var(--c-bg);
}

.appearance-body {
  width: min(640px, 100%);
  margin: 0 auto;
  padding: 20px 16px 56px;
}

.appearance-heading {
  display: none;
  margin: 0 0 24px;
  font-family: var(--font-serif);
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.015em;
}

.appearance-section + .appearance-section {
  margin-top: 28px;
}

.section-title {
  margin: 0 0 10px 2px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-text-3);
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.accent-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.accent-option {
  min-height: 52px;
  padding: 12px 14px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
}

.accent-option:hover {
  border-color: color-mix(in srgb, var(--c-primary) 40%, var(--c-border));
}

.accent-option.option-item--active {
  border-color: var(--c-primary);
  background: var(--c-primary-soft);
  box-shadow: 0 0 0 1px var(--c-primary);
}

.accent-option__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.accent-option__name {
  font-size: 14px;
  font-weight: 500;
}

.accent-swatch {
  position: relative;
  flex: none;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--c-text-1) 14%, transparent);
  box-shadow: inset 0 0 0 2px var(--c-surface);
}

[data-theme="dark"] .accent-swatch {
  background-color: var(--swatch-dark) !important;
}

.option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 52px;
  padding: 0 16px;
  border: 0;
  background: transparent;
  color: var(--c-text-1);
  font: inherit;
  font-size: 15px;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.16s ease, border-color 0.16s ease;
}

.option-item:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: -2px;
}

.theme-option {
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
  padding: 10px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
}

.theme-option:hover {
  border-color: color-mix(in srgb, var(--c-primary) 40%, var(--c-border));
}

.theme-option.option-item--active {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 1px var(--c-primary);
}

.theme-option__label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  min-height: 20px;
  padding: 0 2px;
  font-size: 14px;
  font-weight: 500;
}

.theme-swatch {
  position: relative;
  display: block;
  height: 64px;
  overflow: hidden;
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-control);
  background: var(--c-preview-bg);
}

.theme-swatch i {
  position: absolute;
  left: 10px;
  height: 8px;
  border-radius: 4px;
  background: var(--c-preview-line);
}

.theme-swatch i:nth-child(1) { top: 12px; width: 44%; background: var(--c-preview-brand); }
.theme-swatch i:nth-child(2) { top: 28px; width: 70%; }
.theme-swatch i:nth-child(3) { top: 42px; width: 56%; }

.theme-swatch--dark { background: var(--c-preview-dark-bg); }
.theme-swatch--dark i { background: var(--c-preview-dark-surface); }
.theme-swatch--dark i:nth-child(1) { background: var(--c-preview-brand-dark); }

.theme-swatch--system {
  background: linear-gradient(90deg, var(--c-preview-bg) 50%, var(--c-preview-dark-bg) 50%);
}

.theme-swatch--system i { background: var(--c-preview-muted); }
.theme-swatch--system i:nth-child(1) { background: var(--c-preview-brand); }

.check-icon {
  flex: none;
  color: var(--c-primary);
}

.font-card,
.option-list {
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
}

.font-card {
  padding: 20px 16px 16px;
}

.font-preview {
  min-height: 56px;
  margin: 0 0 18px;
  padding: 12px 14px;
  border-radius: var(--radius-control);
  background: var(--c-fill-2);
  color: var(--c-text-1);
  line-height: 1.6;
}

.font-slider {
  width: 100%;
  height: 4px;
  margin: 8px 0;
  border-radius: 2px;
  background: linear-gradient(90deg, var(--c-primary) var(--fill, 33%), var(--c-fill-3) var(--fill, 33%));
  accent-color: var(--c-primary);
  appearance: none;
  cursor: pointer;
}

.font-slider::-webkit-slider-thumb {
  width: 22px;
  height: 22px;
  border: 2px solid var(--c-primary);
  border-radius: 50%;
  background: var(--c-surface);
  appearance: none;
}

.font-slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border: 2px solid var(--c-primary);
  border-radius: 50%;
  background: var(--c-surface);
}

.font-slider:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 6px;
}

.font-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: var(--c-text-3);
}

.font-labels .is-active {
  color: var(--c-primary);
  font-weight: 600;
}

.option-list .option-item + .option-item {
  border-top: 1px solid var(--c-divider);
}

.option-list .option-item:hover {
  background: var(--c-surface-hover);
}

.option-list .option-item--active {
  font-weight: 600;
}

@media (min-width: 1024px) {
  .appearance-body {
    padding: 40px 0 72px;
  }

  .appearance-heading {
    display: block;
  }
}

@media (max-width: 380px) {
  .theme-swatch {
    height: 52px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .option-item {
    transition: none;
  }
}
</style>
