import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const getTopicById = (id, config = {}) => request.get(`/topic/id/${id}`, config)
export const postTopicByIdLike = (id, data, config = {}) => request.post(`/topic/id/${id}/like`, data, config)
export const getTopicPage = (start, size, config = {}) => request.get(`/topic/start/${start}/size/${size}`, config)
export const postTopic = (data, config = {}) => request.post("/topic", data, config)
export const getTopicKeywordPage050 = (keyword, config = {}) => request.get(`/topic/keyword/${encodeURIComponent(keyword)}/start/0/size/50`, config)
