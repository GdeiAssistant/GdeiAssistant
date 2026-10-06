import { describe, expect, it } from 'vitest'
import { buildDeliveryPayload } from './deliveryForm'

describe('delivery publish contract', () => {
  it('preserves the actual pickup code and decimal text without unused controls', () => {
    expect(buildDeliveryPayload({ pickupCode: ' A-123 ', contactPhone: '13000000000', pickupAddress: ' Pickup ', deliveryAddress: ' Room ', reward: '0.10' }, 'Task')).toEqual({
      taskName: 'Task', pickupCode: 'A-123', contactPhone: '13000000000', price: '0.10', pickupLocation: 'Pickup', deliveryAddress: 'Room', remarks: ''
    })
  })
  it.each(['1.001', 'NaN', '10000', '0', '-1', '1junk'])('rejects an invalid amount %s', (reward) => {
    expect(() => buildDeliveryPayload({ reward }, 'Task')).toThrow('rewardInvalid')
  })
})
