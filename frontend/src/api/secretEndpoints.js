import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postSecretByIdLike = (id, data, config = {}) => request.post(`/secret/id/${id}/like`, data, config)
export const postSecretByIdComment = (id, data, config = {}) => request.post(`/secret/id/${id}/comment`, data, config)
export const getSecretById = (id, config = {}) => request.get(`/secret/id/${id}`, config)
export const getSecretByIdComments = (id, config = {}) => request.get(`/secret/id/${id}/comments`, config)
export const getSecretInfoPage = (start, size, config = {}) => request.get(`/secret/info/start/${start}/size/${size}`, config)
export const getSecretProfilePage0 = (size, config = {}) => request.get(`/secret/profile/start/0/size/${size}`, config)
export const getSecretProfilePage = (start, size, config = {}) => request.get(`/secret/profile/start/${start}/size/${size}`, config)
export const postSecretInfo = (data, config = {}) => request.post("/secret/info", data, config)
