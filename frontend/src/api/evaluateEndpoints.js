import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postEvaluateSubmit = (data, config = {}) => request.post("/evaluate/submit", data, config)
