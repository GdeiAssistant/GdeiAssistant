import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postEmailVerificationemail = (value1, data, config = {}) => request.post(`/email/verification?email=${encodeURIComponent(value1)}`, data, config)
export const postEmailBindemailrandomCode = (value1, value2, data, config = {}) => request.post(`/email/bind?email=${encodeURIComponent(value1)}&randomCode=${encodeURIComponent(value2)}`, data, config)
export const postEmailUnbind = (data, config = {}) => request.post("/email/unbind", data, config)
export const getEmailStatus = (config = {}) => request.get("/email/status", config)
