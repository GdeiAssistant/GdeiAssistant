import { Home, Bell, Settings, Info } from 'lucide-vue-next'

export function createNavItems(t) {
  return [
    { label: t('tab.home'), icon: Home, path: '/home' },
    { label: t('tab.info'), icon: Bell, path: '/info' }
  ]
}

export function createFooterItems(t) {
  return [
    { label: t('sidebar.settings'), icon: Settings, path: '/settings' },
    { label: t('sidebar.about'), icon: Info, path: '/about' }
  ]
}

export function resolveRouteTitle(route, t) {
  if (route.meta?.titleKey) {
    return t(route.meta.titleKey)
  }
  return route.meta?.title || route.name?.toString() || ''
}
