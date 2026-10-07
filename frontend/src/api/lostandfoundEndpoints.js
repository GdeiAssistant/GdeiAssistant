import request from '../utils/request'

/** Module endpoints; URL construction and transport options stay outside views. */
export const getLostFoundItemById = (id, config = {}) => request.get(`/lostandfound/item/id/${id}`, config)
export const getLostFoundProfile = (config = {}) => request.get("/lostandfound/profile", config)
export const postLostFoundItemByIdDidfound = (id, data, config = {}) => request.post(`/lostandfound/item/id/${id}/didfound`, data, config)
export const postLostFoundItemById = (id, data, config = {}) => request.post(`/lostandfound/item/id/${id}`, data, config)
export const postLostFoundItem = (data, config = {}) => request.post("/lostandfound/item", data, config)

export const getLostFoundFeed = (type, start, config = {}) => request.get(`/lostandfound/${type === 0 ? 'lostitem' : 'founditem'}/start/${start}`, config)
