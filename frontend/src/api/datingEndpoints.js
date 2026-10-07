import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const getDatingPickMyReceived = (config = {}) => request.get("/dating/pick/my/received", config)
export const getDatingPickMySent = (config = {}) => request.get("/dating/pick/my/sent", config)
export const getDatingProfileMy = (config = {}) => request.get("/dating/profile/my", config)
export const postDatingPickById = (id, data, config = {}) => request.post(`/dating/pick/id/${id}`, data, config)
export const postDatingProfileByIdState = (id, data, config = {}) => request.post(`/dating/profile/id/${id}/state`, data, config)
export const postDatingPick = (data, config = {}) => request.post("/dating/pick", data, config)
export const getDatingProfileById = (id, config = {}) => request.get(`/dating/profile/id/${id}`, config)
export const getDatingProfileAreaPage = (area, start, config = {}) => request.get(`/dating/profile/area/${area}/start/${start}`, config)
export const postDatingProfile = (data, config = {}) => request.post("/dating/profile", data, config)
