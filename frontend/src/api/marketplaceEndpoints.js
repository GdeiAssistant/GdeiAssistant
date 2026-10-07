import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const getMarketplaceItemById = (id, config = {}) => request.get(`/marketplace/item/id/${id}`, config)
export const getMarketplaceItemPage = (start, config = {}) => request.get(`/marketplace/item/start/${start}`, config)
export const getMarketplaceProfile = (config = {}) => request.get("/marketplace/profile", config)
export const postMarketplaceItemStateById = (id, data, config = {}) => request.post(`/marketplace/item/state/id/${id}`, data, config)
export const postMarketplaceItemById = (id, data, config = {}) => request.post(`/marketplace/item/id/${id}`, data, config)
export const postMarketplaceItem = (data, config = {}) => request.post("/marketplace/item", data, config)
export const getMarketplaceKeywordPage = (keyword, start, config = {}) => request.get(`/marketplace/keyword/${encodeURIComponent(keyword)}/start/${start}`, config)
export const getMarketplaceItemTypePage = (type, start, config = {}) => request.get(`/marketplace/item/type/${type}/start/${start}`, config)
