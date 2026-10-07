import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postCloseSubmit = (data, config = {}) => request.post("/close/submit", data, config)
