import request from '@/utils/request'

export function getCampusCredentialStatus() {
  return request.get('/campus-credential/status').then((res) => res.data)
}

export function revokeCampusCredentialConsent() {
  return request.post('/campus-credential/revoke').then((res) => res.data)
}

export function deleteCampusCredential() {
  return request.delete('/campus-credential').then((res) => res.data)
}

export function updateCampusQuickAuth(enabled) {
  return request.post('/campus-credential/quick-auth', { enabled }).then((res) => res.data)
}
