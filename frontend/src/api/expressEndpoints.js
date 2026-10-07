import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postExpressByIdLike = (id, data, config = {}) => request.post(`/express/id/${id}/like`, data, config)
export const postExpressByIdGuess = (id, data, config = {}) => request.post(`/express/id/${id}/guess`, data, config)
export const postExpressByIdComment = (id, data, config = {}) => request.post(`/express/id/${id}/comment`, data, config)
export const getExpressById = (id, config = {}) => request.get(`/express/id/${id}`, config)
export const getExpressByIdComment = (id, config = {}) => request.get(`/express/id/${id}/comment`, config)
export const getExpressPage = (start, size, config = {}) => request.get(`/express/start/${start}/size/${size}`, config)
export const postExpress = (data, config = {}) => request.post("/express", data, config)
export const getExpressKeywordPage = (keyword, start, size, config = {}) => request.get(`/express/keyword/${encodeURIComponent(keyword)}/start/${start}/size/${size}`, config)
