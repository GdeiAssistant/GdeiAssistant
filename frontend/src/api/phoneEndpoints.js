import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postPhoneVerificationcodephone = (value1, value2, data, config = {}) => request.post(`/phone/verification?code=${encodeURIComponent(value1)}&phone=${encodeURIComponent(value2)}`, data, config)
export const postPhoneAttachcodephonerandomCode = (value1, value2, value3, data, config = {}) => request.post(`/phone/attach?code=${encodeURIComponent(value1)}&phone=${encodeURIComponent(value2)}&randomCode=${encodeURIComponent(value3)}`, data, config)
export const postPhoneUnattach = (data, config = {}) => request.post("/phone/unattach", data, config)
export const getPhoneStatus = (config = {}) => request.get("/phone/status", config)
