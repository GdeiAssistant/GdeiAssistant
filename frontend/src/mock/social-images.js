// Demo's private media store: binary images stay out of localStorage and DTOs.
const memory = new Map()
let database
async function open() {
  if (!globalThis.indexedDB) return null
  database ||= new Promise((resolve, reject) => {
    const request = indexedDB.open('gdei-social-demo-media', 1)
    request.onupgradeneeded = () => request.result.createObjectStore('images')
    request.onsuccess = () => resolve(request.result)
    request.onerror = () => { database = null; reject(request.error) }
  })
  return database
}
async function operation(key, value, remove = false) {
  const db = await open()
  if (!db) {
    if (remove) return memory.delete(key)
    if (value !== undefined) return memory.set(key, value)
    return memory.get(key)
  }
  return new Promise((resolve, reject) => {
    const transaction = db.transaction('images', value !== undefined || remove ? 'readwrite' : 'readonly')
    const store = transaction.objectStore('images')
    const request = remove ? store.delete(key) : value !== undefined ? store.put(value, key) : store.get(key)
    transaction.oncomplete = () => resolve(request.result)
    transaction.onerror = () => reject(transaction.error)
    transaction.onabort = () => reject(transaction.error || new Error('image storage failed'))
  })
}
export const saveDemoImage = (key, blob) => operation(key, blob)
export const readDemoImage = key => operation(key)
export const deleteDemoImage = key => operation(key, undefined, true)
