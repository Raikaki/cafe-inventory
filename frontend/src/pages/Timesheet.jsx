import { useEffect, useState } from 'react'
import { Card, Table, Typography, DatePicker, Button, Space, Row, message, Upload, Alert, Tag } from 'antd'
import { UploadOutlined, SyncOutlined, EyeOutlined, CalendarOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'

const { Title, Text } = Typography

const WD = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7']
const hhmm = (t) => (t ? String(t).substring(0, 5) : null)
const hoursOf = (ci, co) => {
  if (!ci || !co) return null
  const [h1, m1] = ci.split(':').map(Number)
  const [h2, m2] = co.split(':').map(Number)
  const diff = (h2 * 60 + m2) - (h1 * 60 + m1)
  return diff > 0 ? (diff / 60).toFixed(1) : null
}

export default function Timesheet() {
  const [month, setMonth] = useState(dayjs())
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)

  const ym = () => ({ year: month.year(), month: month.month() + 1 })

  const load = () => {
    setLoading(true)
    const p = ym()
    client.get(`/api/attendance/timesheet?year=${p.year}&month=${p.month}`)
      .then((r) => setRecords(r.data)).finally(() => setLoading(false))
  }
  useEffect(load, [month])

  const sync = () => {
    const p = ym()
    setLoading(true)
    client.post(`/api/attendance/timesheet/sync?year=${p.year}&month=${p.month}`)
      .then((r) => { message.success(`Đã lấy ${r.data.synced} bản ghi từ dữ liệu QR`); load() })
      .catch((e) => { message.error(e.response?.data?.message || 'Lỗi'); setLoading(false) })
  }

  const upload = async ({ file, onSuccess, onError }) => {
    setUploading(true)
    const fd = new FormData(); fd.append('file', file)
    try {
      const r = await client.post('/api/attendance/timesheet/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      message.success(`Đã nhập ${r.data.imported} dòng`); onSuccess(); load()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi import'); onError(e) }
    finally { setUploading(false) }
  }

  // pivot: map[name][day] = record
  const daysInMonth = month.daysInMonth()
  const monthStr = month.format('YYYY-MM')
  const employees = [...new Set(records.map((r) => r.employeeName))].sort((a, b) => a.localeCompare(b, 'vi'))
  const byKey = {}
  records.forEach((r) => { byKey[`${r.employeeName}|${r.workDate}`] = r })

  const dataSource = employees.map((name) => {
    let total = 0
    for (let d = 1; d <= daysInMonth; d++) {
      const rec = byKey[`${name}|${monthStr}-${String(d).padStart(2, '0')}`]
      const h = rec ? hoursOf(rec.checkIn, rec.checkOut) : null
      if (h) total += Number(h)
    }
    return { key: name, employeeName: name, totalHours: total.toFixed(1) }
  })

  const dayColumns = Array.from({ length: daysInMonth }, (_, i) => {
    const d = i + 1
    const dateStr = `${monthStr}-${String(d).padStart(2, '0')}`
    const wd = WD[dayjs(dateStr).day()]
    const weekend = wd === 'CN' || wd === 'T7'
    return {
      title: <div style={{ lineHeight: 1.1 }}><div>{d}</div><div style={{ fontSize: 10, color: weekend ? '#cf1322' : '#999' }}>{wd}</div></div>,
      width: 58, align: 'center',
      onHeaderCell: () => ({ style: { background: weekend ? '#fff7e6' : undefined, padding: '6px 2px' } }),
      render: (_, row) => {
        const rec = byKey[`${row.employeeName}|${dateStr}`]
        if (!rec) return null
        const h = hoursOf(rec.checkIn, rec.checkOut)
        return (
          <div style={{ fontSize: 11, lineHeight: 1.25 }}>
            <div style={{ color: '#389e0d' }}>{hhmm(rec.checkIn) || '--'}</div>
            <div style={{ color: '#1677ff' }}>{hhmm(rec.checkOut) || '--'}</div>
            {h && <div style={{ color: '#a0522d', fontWeight: 600 }}>{h}h</div>}
          </div>
        )
      },
    }
  })

  const columns = [
    { title: 'Nhân viên', dataIndex: 'employeeName', fixed: 'left', width: 160,
      render: (v) => <b>{v}</b> },
    ...dayColumns,
    { title: 'Tổng giờ', dataIndex: 'totalHours', fixed: 'right', width: 90, align: 'right',
      render: (v) => <b style={{ color: '#a0522d' }}>{v} h</b> },
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}><CalendarOutlined /> Bảng công tháng</Title>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <span>Tháng:</span>
          <DatePicker picker="month" value={month} onChange={(v) => v && setMonth(v)} format="MM/YYYY" allowClear={false} />
          <Button icon={<EyeOutlined />} onClick={load} loading={loading}>Xem</Button>
          <Button icon={<SyncOutlined />} onClick={sync}>Lấy từ dữ liệu QR</Button>
          <Upload accept=".xlsx,.csv" customRequest={upload} showUploadList={false}>
            <Button type="primary" icon={<UploadOutlined />} loading={uploading}>Upload Excel/CSV</Button>
          </Upload>
        </Space>
        <Alert style={{ marginTop: 12 }} type="info" showIcon
               message="Định dạng file Excel/CSV"
               description={<span>4 cột: <Text code>Tên nhân viên</Text> | <Text code>Ngày</Text> (yyyy-MM-dd hoặc dd/MM/yyyy) | <Text code>Giờ vào</Text> (HH:mm) | <Text code>Giờ ra</Text>. Ví dụ: <Text code>Võ Văn Hải, 2026-06-01, 07:30, 17:00</Text>. Ảnh chụp không đọc tự động được — hãy xuất ra Excel.</span>} />
      </Card>

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="key" loading={loading} dataSource={dataSource} columns={columns}
               size="small" bordered scroll={{ x: 'max-content', y: 560 }} pagination={false}
               locale={{ emptyText: 'Chưa có dữ liệu — Upload Excel hoặc bấm "Lấy từ dữ liệu QR"' }} />
      </Card>
      <Text type="secondary" style={{ fontSize: 12 }}>
        Mỗi ô: <span style={{ color: '#389e0d' }}>giờ vào</span> / <span style={{ color: '#1677ff' }}>giờ ra</span> / <span style={{ color: '#a0522d' }}>số giờ làm</span>.
      </Text>
    </>
  )
}
