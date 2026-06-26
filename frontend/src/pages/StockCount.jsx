import { useEffect, useState } from 'react'
import { Card, Table, Typography, DatePicker, Input, Button, Space, message, InputNumber, Result, Tag } from 'antd'
import { SaveOutlined, AuditOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography

export default function StockCount() {
  const [rows, setRows] = useState([])
  const [date, setDate] = useState(dayjs())
  const [note, setNote] = useState('')
  const [saving, setSaving] = useState(false)
  const [done, setDone] = useState(null)

  const load = () => {
    client.get('/api/materials').then((r) => {
      const active = r.data.filter((m) => m.activeFlag !== false)
      setRows(active.map((m) => ({
        materialId: m.id, materialCode: m.materialCode, materialName: m.materialName, unit: m.unit,
        systemQty: Number(m.currentQty), actualQty: Number(m.currentQty),
      })))
    })
  }
  useEffect(load, [])

  const update = (id, val) => setRows((p) => p.map((r) => r.materialId === id ? { ...r, actualQty: val } : r))

  const submit = () => {
    const items = rows.map((r) => ({ materialId: r.materialId, actualQty: Number(r.actualQty || 0) }))
    setSaving(true)
    client.post('/api/stock-counts', { countDate: date.format('YYYY-MM-DD'), note, items })
      .then((res) => { setDone(res.data); message.success('Đã lưu kiểm kê & điều chỉnh tồn') })
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi'))
      .finally(() => setSaving(false))
  }

  if (done) {
    return (
      <Result status="success"
        title={`Đã kiểm kê: ${done.countNo}`}
        subTitle={`${done.adjustedCount} nguyên vật liệu được điều chỉnh tồn theo thực tế.`}
        extra={<Button type="primary" onClick={() => { setDone(null); load() }}>Kiểm kê phiếu mới</Button>} />
    )
  }

  const columns = [
    { title: 'Mã', dataIndex: 'materialCode', width: 110 },
    { title: 'Nguyên vật liệu', dataIndex: 'materialName' },
    { title: 'ĐVT', dataIndex: 'unit', width: 70, align: 'center' },
    { title: 'Tồn sổ', dataIndex: 'systemQty', align: 'right', width: 120, render: fmt },
    { title: 'Tồn thực tế', dataIndex: 'actualQty', width: 150, render: (v, r) => (
      <InputNumber style={{ width: '100%' }} min={0} value={v} onChange={(val) => update(r.materialId, val)} />
    )},
    { title: 'Chênh lệch', width: 130, align: 'right', render: (_, r) => {
      const diff = Number(r.actualQty || 0) - Number(r.systemQty || 0)
      if (diff === 0) return <span style={{ color: '#999' }}>0</span>
      return <b style={{ color: diff > 0 ? '#52c41a' : '#cf1322' }}>{diff > 0 ? '+' : ''}{fmt(diff)}</b>
    }},
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}><AuditOutlined /> Kiểm kê kho</Title>
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <span>Ngày kiểm kê:</span>
          <DatePicker value={date} onChange={(v) => v && setDate(v)} format="DD/MM/YYYY" allowClear={false} />
          <Input placeholder="Ghi chú" value={note} onChange={(e) => setNote(e.target.value)} style={{ width: 280 }} />
          <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={submit}>Lưu & điều chỉnh tồn</Button>
        </Space>
        <div style={{ marginTop: 8, fontSize: 12, color: '#888' }}>
          Nhập <b>tồn thực tế đếm được</b>; hệ thống tự tạo điều chỉnh cho phần chênh lệch (thừa/thiếu).
        </div>
      </Card>
      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="materialId" dataSource={rows} columns={columns} pagination={false} size="small"
               scroll={{ y: 520 }} />
      </Card>
    </>
  )
}
