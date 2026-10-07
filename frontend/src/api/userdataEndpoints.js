import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const getUserdataState = (config = {}) => request.get("/userdata/state", config)
export const postUserdataExport = (data, config = {}) => request.post("/userdata/export", data, config)
export const postUserdataDownload = (data, config = {}) => request.post("/userdata/download", data, config)
