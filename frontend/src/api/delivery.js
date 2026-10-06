import request from '../utils/request'

export const getDeliveryOrders = (start, size) => request.get(`/delivery/order/start/${start}/size/${size}`)
export const getDeliveryMine = () => request.get('/delivery/mine')
export const getDeliveryDetail = (id) => request.get(`/delivery/order/id/${id}`)
export const acceptDeliveryOrder = (orderId) => request.post('/delivery/acceptorder', null, { params: { orderId } })
export const finishDeliveryTrade = (id) => request.post(`/delivery/trade/id/${id}/finishtrade`)
export const publishDeliveryOrder = (payload) => request.post('/delivery/order', payload)
