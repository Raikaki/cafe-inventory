export const fmt = (v) => {
  if (v === null || v === undefined || v === '') return '0'
  const n = Number(v)
  if (Number.isNaN(n)) return v
  return n.toLocaleString('vi-VN', { maximumFractionDigits: 3 })
}

export const money = (v) => fmt(v) + ' đ'
