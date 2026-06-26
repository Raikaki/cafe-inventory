import { useEffect, useState } from 'react'
import { Card, Table, Typography, DatePicker, Button, Space, message, Upload, Alert, Tag } from 'antd'
import { UploadOutlined, EyeOutlined, CalendarOutlined, DownloadOutlined, FileExcelOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'

const { Title, Text } = Typography
const { RangePicker } = DatePicker

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
  const [range, setRange] = useState([dayjs().startOf('month'), dayjs()])
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)

  const load = () => {
    if (!range?.[0] || !range?.[1]) return
    setLoading(true)
    const from = range[0].format('YYYY-MM-DD')
    const to = range[1].format('YYYY-MM-DD')
    client.get(`/api/attendance/timesheet?from=${from}&to=${to}`)
      .then((r) => setRows(r.data)).finally(() => setLoading(false))
  }
  useEffect(load, [range])

  const upload = async ({ file, onSuccess, onError }) => {
    setUploading(true)
    const fd = new FormData(); fd.append('file', file)
    try {
      const r = await client.post('/api/attendance/timesheet/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      message.success(`Đã nhập ${r.data.imported} dòng`); onSuccess(); load()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi import'); onError(e) }
    finally { setUploading(false) }
  }

  const exportExcel = () => {
    if (!range?.[0] || !range?.[1]) { message.warning('Chọn khoảng ngày'); return }
    const from = range[0].format('YYYY-MM-DD')
    const to = range[1].format('YYYY-MM-DD')
    client.get(`/api/attendance/timesheet/export?from=${from}&to=${to}`, { responseType: 'blob' })
      .then((res) => {
        const url = URL.createObjectURL(new Blob([res.data]))
        const a = document.createElement('a')
        a.href = url; a.download = `cham_cong_${from}_${to}.xlsx`; a.click()
        URL.revokeObjectURL(url)
      })
      .catch(() => message.error('Lỗi xuất Excel'))
  }

  const downloadTemplate = () => {
    const sample =
      'Tên nhân viên,Ngày,Giờ vào,Giờ ra\n' +
      'Võ Văn Hải,2026-06-01,07:30,17:00\n' +
      'Bùi Đình Khánh,2026-06-01,07:35,15:32\n'
    const blob = new Blob(['﻿' + sample], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = 'mau_cham_cong.csv'; a.click()
    URL.revokeObjectURL(url)
  }

  // enumerate days in range
  const days = []
  if (range?.[0] && range?.[1]) {
    let d = range[0].startOf('day')
    const end = range[1].startOf('day')
    let guard = 0
    while ((d.isBefore(end) || d.isSame(end)) && guard < 400) { days.push(d.format('YYYY-MM-DD')); d = d.add(1, 'day'); guard++ }
  }

  const employees = [...new Set(rows.map((r) => r.employeeName))].sort((a, b) => a.localeCompare(b, 'vi'))
  const byKey = {}
  rows.forEach((r) => { byKey[`${r.employeeName}|${r.workDate}`] = r })

  const dataSource = employees.map((name) => {
    let total = 0
    days.forEach((ds) => {
      const rec = byKey[`${name}|${ds}`]
      const h = rec ? hoursOf(rec.checkIn, rec.checkOut) : null
      if (h) total += Number(h)
    })
    return { key: name, employeeName: name, totalHours: total.toFixed(1) }
  })

  const dayColumns = days.map((ds) => {
    const dd = dayjs(ds)
    const wd = WD[dd.day()]
    const weekend = wd === 'CN' || wd === 'T7'
    return {
      title: <div style={{ lineHeight: 1.1 }}><div>{dd.format('DD/MM')}</div><div style={{ fontSize: 10, color: weekend ? '#cf1322' : '#999' }}>{wd}</div></div>,
      width: 62, align: 'center',
      onHeaderCell: () => ({ style: { background: weekend ? '#fff7e6' : undefined, padding: '6px 2px' } }),
      render: (_, row) => {
        const rec = byKey[`${row.employeeName}|${ds}`]
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
    { title: 'Nhân viên', dataIndex: 'employeeName', fixed: 'left', width: 160, render: (v) => <b>{v}</b> },
    ...dayColumns,
    { title: 'Tổng giờ', dataIndex: 'totalHours', fixed: 'right', width: 90, align: 'right',
      render: (v) => <b style={{ color: '#a0522d' }}>{v} h</b> },
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}><CalendarOutlined /> Bảng chấm công</Title>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <span>Từ ngày → đến ngày:</span>
          <RangePicker value={range} onChange={setRange} format="DD/MM/YYYY" allowClear={false}
                       presets={[
                         { label: 'Tháng này', value: [dayjs().startOf('month'), dayjs()] },
                         { label: 'Tháng trước', value: [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')] },
                         { label: '7 ngày qua', value: [dayjs().subtract(6, 'day'), dayjs()] },
                       ]} />
          <Button icon={<EyeOutlined />} onClick={load} loading={loading}>Truy vấn</Button>
          <Button icon={<FileExcelOutlined />} style={{ color: '#1d6f42', borderColor: '#1d6f42' }} onClick={exportExcel}>Xuất Excel</Button>
          <Button icon={<DownloadOutlined />} onClick={downloadTemplate}>Tải file mẫu</Button>
          <Upload accept=".xlsx,.csv" customRequest={upload} showUploadList={false}>
            <Button type="primary" icon={<UploadOutlined />} loading={uploading}>Upload Excel/CSV</Button>
          </Upload>
        </Space>
        <Alert style={{ marginTop: 12 }} type="info" showIcon
               message="Dữ liệu tự lấy từ chấm công QR hàng ngày trong khoảng đã chọn"
               description={<span>Có thể bổ sung bằng file Excel/CSV (4 cột: <Text code>Tên nhân viên | Ngày | Giờ vào | Giờ ra</Text>). Mỗi ô: <span style={{ color: '#389e0d' }}>vào</span> / <span style={{ color: '#1677ff' }}>ra</span> / <span style={{ color: '#a0522d' }}>số giờ</span>.</span>} />
      </Card>

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="key" loading={loading} dataSource={dataSource} columns={columns}
               size="small" bordered scroll={{ x: 'max-content', y: 560 }} pagination={false}
               locale={{ emptyText: 'Chưa có dữ liệu chấm công trong khoảng này' }} />
      </Card>
    </>
  )
}
