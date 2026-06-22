const localStorageDescriptor = Object.getOwnPropertyDescriptor(globalThis, 'localStorage')

if (!localStorageDescriptor || typeof localStorageDescriptor.get === 'function') {
  const store = {}
  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    writable: true,
    value: {
      getItem: (key) => store[key] ?? null,
      setItem: (key, value) => { store[key] = String(value) },
      removeItem: (key) => { delete store[key] },
      clear: () => { Object.keys(store).forEach(k => delete store[k]) },
      get length() { return Object.keys(store).length },
      key: (i) => Object.keys(store)[i] ?? null
    }
  })
}
