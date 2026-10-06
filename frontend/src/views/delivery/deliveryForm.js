/** Pure form-to-contract mapping; validation and transport do not depend on rendering. */
export function buildDeliveryPayload(form, taskName) {
  const price = String(form.reward ?? '').trim()
  if (!/^(?:0|[1-9]\d{0,3})(?:\.\d{1,2})?$/.test(price) || Number(price) < 0.01 || Number(price) > 9999.99) {
    throw new Error('rewardInvalid')
  }
  return {
    taskName,
    pickupCode: String(form.pickupCode || '').trim(),
    contactPhone: String(form.contactPhone || '').trim(),
    price,
    pickupLocation: String(form.pickupAddress || '').trim(),
    deliveryAddress: String(form.deliveryAddress || '').trim(),
    remarks: String(form.description || '').trim()
  }
}
