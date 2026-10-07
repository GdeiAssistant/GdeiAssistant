import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const getIpPage020 = (config = {}) => request.get("/ip/start/0/size/20", config)
