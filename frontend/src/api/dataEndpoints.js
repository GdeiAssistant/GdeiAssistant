import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const postDataElectricfees = (data, config = {}) => request.post("/data/electricfees", data, config)
export const getDataYellowpage = (config = {}) => request.get("/data/yellowpage", config)
