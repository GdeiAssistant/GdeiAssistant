import { getCurrentScope, onScopeDispose } from 'vue'

/** Only the latest request may commit state, including loading and errors. */
export function useLatestRequest() {
  let generation = 0
  let controller
  let disposed = false
  const invalidate = () => { generation++; controller?.abort() }
  if (getCurrentScope()) onScopeDispose(() => { disposed = true; invalidate() })
  return {
    begin() {
      invalidate()
      controller = new AbortController()
      const id = generation
      return { signal: controller.signal, isCurrent: () => !disposed && id === generation }
    },
    invalidate
  }
}
