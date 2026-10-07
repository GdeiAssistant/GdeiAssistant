import { describe, expect, it } from 'vitest'
import { effectScope } from 'vue'
import { useScrollLoad } from './useScrollLoad'
import { useLatestRequest } from './useLatestRequest'
const deferred = () => { let resolve, reject; const promise = new Promise((yes, no) => { resolve = yes; reject = no }); return { promise, resolve, reject } }
const page = (id, hasMore = true) => ({ list: [{ id }], hasMore })
describe('request lifecycles', () => {
  it('never appends a stale page after refresh', async () => {
    const calls = [], list = useScrollLoad(() => { const task = deferred(); calls.push(task); return task.promise })
    const first = list.loadData(); calls[0].resolve(page('first')); await first
    const oldPage = list.loadData(), refresh = list.loadData(true)
    calls[2].resolve(page('fresh')); await refresh; calls[1].resolve(page('stale', false)); await oldPage
    expect(list.items.value).toEqual([{ id: 'fresh' }]); expect(list.finished.value).toBe(false)
  })
  it('only the latest refresh can replace data or clear its loading flag', async () => {
    const calls = [], list = useScrollLoad(() => { const task = deferred(); calls.push(task); return task.promise })
    const old = list.loadData(true), current = list.loadData(true)
    calls[0].resolve(page('old')); await old; expect(list.refreshing.value).toBe(true)
    calls[1].resolve(page('new')); await current
    expect(list.items.value).toEqual([{ id: 'new' }]); expect(list.refreshing.value).toBe(false)
  })
  it('preserves data and exposes errors instead of treating failure as empty', async () => {
    const calls = [], list = useScrollLoad(() => { const task = deferred(); calls.push(task); return task.promise })
    const first = list.loadData(); calls[0].resolve(page('saved')); await first
    const refresh = list.loadData(true); calls[1].reject(new Error('synthetic outage')); await refresh
    expect(list.items.value).toEqual([{ id: 'saved' }]); expect(list.error.value.message).toBe('synthetic outage')
  })
  it('invalidates prior requests and cannot commit after its scope ends', () => {
    const scope = effectScope(), gate = scope.run(() => useLatestRequest())
    const old = gate.begin(), current = gate.begin()
    expect(old.signal.aborted).toBe(true); expect(old.isCurrent()).toBe(false); expect(current.isCurrent()).toBe(true)
    scope.stop(); expect(current.signal.aborted).toBe(true); expect(current.isCurrent()).toBe(false)
  })
  it('discards list responses after disposal', async () => {
    const scope = effectScope(), pending = deferred(), list = scope.run(() => useScrollLoad(() => pending.promise))
    const request = list.loadData(); scope.stop(); pending.resolve(page('late')); await request; expect(list.items.value).toEqual([])
  })
  it('scroll loads only near the end and blocks duplicate in-flight loads', async () => {
    const calls = [], list = useScrollLoad(() => { const task = deferred(); calls.push(task); return task.promise })
    list.handleScroll({ target: { scrollTop: 0, clientHeight: 50, scrollHeight: 500 } })
    expect(calls).toHaveLength(0)
    list.handleScroll({ target: { scrollTop: 440, clientHeight: 50, scrollHeight: 500 } })
    list.handleScroll({ target: { scrollTop: 440, clientHeight: 50, scrollHeight: 500 } })
    expect(calls).toHaveLength(1)
    calls[0].resolve(page('end', false)); await Promise.resolve(); await Promise.resolve()
    list.handleScroll({ target: { scrollTop: 440, clientHeight: 50, scrollHeight: 500 } })
    expect(calls).toHaveLength(1); expect(list.finished.value).toBe(true)
  })
  it('pull refresh obeys the top boundary and clears loading on empty or failed pages', async () => {
    const calls = [], list = useScrollLoad(() => { const task = deferred(); calls.push(task); return task.promise })
    const prevented = { count: 0, preventDefault() { this.count++ } }
    list.handleTouchStart({ touches: [{ clientY: 0 }] })
    list.handleTouchMove({ touches: [{ clientY: 200 }], preventDefault: () => prevented.preventDefault() }, { value: { scrollTop: 20 } })
    expect(list.pullY.value).toBe(0)
    list.handleTouchMove({ touches: [{ clientY: -5 }], preventDefault: () => prevented.preventDefault() }, { scrollTop: 0 })
    expect(prevented.count).toBe(0)
    list.handleTouchMove({ touches: [{ clientY: 80 }], preventDefault: () => prevented.preventDefault() }, { scrollTop: 0 })
    list.handleTouchEnd(); expect(calls).toHaveLength(0); expect(list.pullY.value).toBe(0)
    list.handleTouchMove({ touches: [{ clientY: 300 }], preventDefault: () => prevented.preventDefault() }, { scrollTop: 0 })
    list.handleTouchEnd(); expect(calls).toHaveLength(1); expect(list.refreshing.value).toBe(true)
    list.handleTouchEnd(); expect(calls).toHaveLength(1)
    calls[0].resolve({ list: [], hasMore: false }); await Promise.resolve(); await Promise.resolve()
    expect(list.finished.value).toBe(true); expect(list.pullY.value).toBe(0)
    const refresh=list.loadData(true);calls[1].resolve(page('new'));await refresh
    const more=list.loadData();calls[2].reject(new Error('outage'));await more
    expect(list.loading.value).toBe(false);expect(list.items.value).toEqual([{ id:'new' }])
  })
  it('rejects malformed pagination and ignores disposed loads and stale failures', async () => {
    const calls=[],scope=effectScope(),list=scope.run(() => useScrollLoad(() => { const task=deferred();calls.push(task);return task.promise }))
    const bad=list.loadData();calls[0].resolve({list:[]});await bad;expect(list.error.value).toBeInstanceOf(TypeError)
    const old=list.loadData(true),current=list.loadData(true);calls[1].reject(new Error('old'));await old
    expect(list.error.value).toBeNull();expect(list.refreshing.value).toBe(true)
    calls[2].resolve({list:[],hasMore:false});await current;scope.stop();await list.loadData();expect(calls).toHaveLength(3)
  })

})
