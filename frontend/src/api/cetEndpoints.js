import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const getCetCheckcode = (config = {}) => request.get("/cet/checkcode", config)
