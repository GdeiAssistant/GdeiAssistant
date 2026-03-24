<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '选择地区' },
  // tree: [{ code, name, stateMap: { code: { code, name, cityMap: { code: { code, name } } } } }]
  tree: { type: Array, default: () => [] },
})

const emit = defineEmits(['close', 'confirm'])

const selectedRegion = ref(null)
const selectedState = ref(null)
const selectedCity = ref(null)

// reset when opened
watch(() => props.open, (val) => {
  if (val) {
    selectedRegion.value = null
    selectedState.value = null
    selectedCity.value = null
  }
})

const states = computed(() => {
  if (!selectedRegion.value) return []
  return Object.values(selectedRegion.value.stateMap || {})
})

const cities = computed(() => {
  if (!selectedState.value) return []
  return Object.values(selectedState.value.cityMap || {})
})

function selectRegion(region) {
  selectedRegion.value = region
  selectedState.value = null
  selectedCity.value = null
}

function selectState(state) {
  selectedState.value = state
  selectedCity.value = null
}

function selectCity(city) {
  selectedCity.value = city
}

function confirm() {
  if (!selectedRegion.value) return
  emit('confirm', {
    region: selectedRegion.value,
    state: selectedState.value,
    city: selectedCity.value,
  })
}

function close() {
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="fixed inset-0 z-[1000] flex items-end justify-center sm:items-center">
      <div class="absolute inset-0 bg-black/60" @click="close" />
      <div class="relative z-10 bg-[var(--c-surface)] rounded-t-2xl sm:rounded-2xl w-full sm:w-[420px] overflow-hidden shadow-xl flex flex-col max-h-[80vh]">
        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-4 border-b border-[var(--c-border-light)] shrink-0">
          <button type="button" class="text-sm text-[var(--c-text-tertiary)] bg-transparent border-0 cursor-pointer font-inherit" @click="close">
            {{ $t('common.cancel') }}
          </button>
          <span class="text-base font-semibold text-[var(--c-text-primary)]">{{ title }}</span>
          <button type="button"
            class="text-sm font-medium bg-transparent border-0 cursor-pointer font-inherit"
            :class="selectedRegion ? 'text-[var(--c-primary)]' : 'text-[var(--c-text-quaternary)]'"
            :disabled="!selectedRegion"
            @click="confirm">
            {{ $t('common.confirm') }}
          </button>
        </div>

        <!-- Columns -->
        <div class="flex flex-1 overflow-hidden">
          <!-- Region -->
          <div class="flex-1 overflow-y-auto border-r border-[var(--c-border-light)]">
            <button type="button"
              v-for="region in tree" :key="region.code"
              class="w-full px-3 py-3 text-left text-sm border-0 cursor-pointer font-inherit truncate transition-colors"
              :class="selectedRegion?.code === region.code
                ? 'bg-[var(--c-primary-light,#e8f0fe)] text-[var(--c-primary)] font-medium'
                : 'bg-transparent text-[var(--c-text-primary)] hover:bg-[var(--c-surface-hover)]'"
              @click="selectRegion(region)">
              {{ region.name }}
            </button>
          </div>

          <!-- State -->
          <div class="flex-1 overflow-y-auto border-r border-[var(--c-border-light)]">
            <div v-if="!selectedRegion" class="px-3 py-3 text-sm text-[var(--c-text-quaternary)]">请先选择</div>
            <button type="button"
              v-for="state in states" :key="state.code"
              class="w-full px-3 py-3 text-left text-sm border-0 cursor-pointer font-inherit truncate transition-colors"
              :class="selectedState?.code === state.code
                ? 'bg-[var(--c-primary-light,#e8f0fe)] text-[var(--c-primary)] font-medium'
                : 'bg-transparent text-[var(--c-text-primary)] hover:bg-[var(--c-surface-hover)]'"
              @click="selectState(state)">
              {{ state.name }}
            </button>
          </div>

          <!-- City -->
          <div class="flex-1 overflow-y-auto">
            <div v-if="!selectedState" class="px-3 py-3 text-sm text-[var(--c-text-quaternary)]">请先选择</div>
            <button type="button"
              v-for="city in cities" :key="city.code"
              class="w-full px-3 py-3 text-left text-sm border-0 cursor-pointer font-inherit truncate transition-colors"
              :class="selectedCity?.code === city.code
                ? 'bg-[var(--c-primary-light,#e8f0fe)] text-[var(--c-primary)] font-medium'
                : 'bg-transparent text-[var(--c-text-primary)] hover:bg-[var(--c-surface-hover)]'"
              @click="selectCity(city)">
              {{ city.name }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
