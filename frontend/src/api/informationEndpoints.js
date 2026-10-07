import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postInformationMessageByIdRead = (id, data, config = {}) => request.post(`/information/message/id/${id}/read`, data, config)
export const postInformationMessageReadall = (data, config = {}) => request.post("/information/message/readall", data, config)
export const getInformationAnnouncementPage05 = (config = {}) => request.get("/information/announcement/start/0/size/5", config)
export const getInformationOverview = (config = {}) => request.get("/information/overview", config)
export const getInformationMessageInteractionPage020 = (config = {}) => request.get("/information/message/interaction/start/0/size/20", config)
export const getInformationMessageUnread = (config = {}) => request.get("/information/message/unread", config)
export const getInformationMessageInteractionPage = (start, size, config = {}) => request.get(`/information/message/interaction/start/${start}/size/${size}`, config)
export const getInformationAnnouncementById = (id, config = {}) => request.get(`/information/announcement/id/${id}`, config)
export const getInformationAnnouncementPage = (start, size, config = {}) => request.get(`/information/announcement/start/${start}/size/${size}`, config)
export const getInformationNewsById = (id, config = {}) => request.get(`/information/news/id/${id}`, config)
export const getInformationNewsTypePage = (type, start, size, config = {}) => request.get(`/information/news/type/${type}/start/${start}/size/${size}`, config)
