export function createElectricityResultFields(t) {
  return [
    { key: 'year', label: t('electricityFees.result.year') },
    { key: 'dormitory', label: t('electricityFees.result.dormitory') },
    { key: 'peopleNumber', label: t('electricityFees.result.peopleNumber') },
    { key: 'usedElectricAmount', label: t('electricityFees.result.usedElectricAmount') },
    { key: 'freeElectricAmount', label: t('electricityFees.result.freeElectricAmount') },
    { key: 'feeBasedElectricAmount', label: t('electricityFees.result.feeBasedElectricAmount') },
    { key: 'electricPrice', label: t('electricityFees.result.electricPrice') },
    { key: 'totalElectricBill', label: t('electricityFees.result.totalElectricBill') },
    { key: 'averageElectricBill', label: t('electricityFees.result.averageElectricBill') }
  ]
}
