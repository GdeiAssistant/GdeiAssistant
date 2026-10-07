import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postProfileAvatar = (data, config = {}) => request.post("/profile/avatar", data, config)
export const deleteProfileAvatar = (config = {}) => request.delete("/profile/avatar", config)
export const getProfileAvatar = (config = {}) => request.get("/profile/avatar", config)
