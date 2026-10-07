import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const getPhotographById = (id, config = {}) => request.get(`/photograph/id/${id}`, config)
export const postPhotographByIdComment = (id, data, config = {}) => request.post(`/photograph/id/${id}/comment`, data, config)
export const postPhotographByIdLike = (id, data, config = {}) => request.post(`/photograph/id/${id}/like`, data, config)
export const getPhotographTypePage = (type, start, size, config = {}) => request.get(`/photograph/type/${type}/start/${start}/size/${size}`, config)
export const postPhotograph = (data, config = {}) => request.post("/photograph", data, config)
