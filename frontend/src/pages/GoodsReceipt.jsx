import { useEffect, useState } from 'react'
import {
  Card, Form, DatePicker, Select, Input, Button, Table, InputNumber, Space,
  Typography, message, Row, Col, Result, Tabs, Upload, Alert,
} from 'antd'
import { PlusOutlined, DeleteOutlined, SaveOutlined, ImportOutlined, InboxOutlined, EditOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title, Text } = Typography
const { Dragger } = Upload

function ManualReceipt({ materials, suppliers, onDone }) {
  const [lines, setLines] = useState([{ key: 1, materialId: null, quantity: 0, amount: 0 }])
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  const addLine = () => setLines((p) => [...p, { key: Date.now(), materialId: null, quantity: 0, amount: 0 }])
  const removeLine = (key) => setLines((p) => p.filter((l) => l.key !== key))
  const updateLine = (key, f, v) => setLines((p) => p.map((l) => l.key === key ? { ...l, [f]: v } : l))
  const total = lines.reduce((s, l) => s + Number(l.amount || 0), 0)

  const submit = async () => {
    const v = await form.validateFields()
    const payload = {
      receiptDate: v.receiptDate.format('YYYY-MM-DD'),
      supplierId: v.supplierId || null,
      note: v.note || null,
      lines: lines.filter((l) => l.materialId && Number(l.quantity) > 0)
        .map((l) => ({ materialId: l.materialId, quantity: l.quantity, amount: l.amount })),
    }
    if (payload.lines.length === 0) { message.warning('Thêm ít nhất 1 dòng nguyên liệu'); return }
    setSaving(true)
    try { const res = await client.post('/api/goods-receipts', payload); onDone(res.data) }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi khi lưu') }
    finally { setSaving(false) }
  }

  const columns = [
    { title: 'Nguyên vật liệu', dataIndex: 'materialId', render: (v, r) => (
      <Select showSearch optionFilterProp="label" style={{ width: '100%' }} value={v} placeholder="Chọn"
              onChange={(val) => updateLine(r.key, 'materialId', val)}
              options={materials.map((m) => ({ value: m.id, label: `${m.materialName} (${m.unit})` }))} />
    )},
    { title: 'Số lượng', dataIndex: 'quantity', width: 140, render: (v, r) => (
      <InputNumber style={{ width: '100%' }} min={0} value={v} onChange={(val) => updateLine(r.key, 'quantity', val)} />
    )},
    { title: 'Thành tiền (đ)', dataIndex: 'amount', width: 160, render: (v, r) => (
      <InputNumber style={{ width: '100%' }} min={0} value={v} onChange={(val) => updateLine(r.key, 'amount', val)}
                   formatter={(x) => `${x}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                   parser={(x) => `${x}`.replace(/,/g, '')} />
    )},
    { title: 'Đơn giá (tự tính)', width: 140, align: 'right',
      render: (_, r) => <span style={{ color: '#1677ff' }}>{Number(r.quantity) > 0 ? fmt(Number(r.amount || 0) / Number(r.quantity)) : '—'}</span> },
    { title: '', width: 50, render: (_, r) => <Button size="small" danger icon={<DeleteOutlined />} onClick={() => removeLine(r.key)} /> },
  ]

  return (
    <>
      <Form form={form} layout="vertical" initialValues={{ receiptDate: dayjs() }}>
        <Row gutter={16}>
          <Col xs={24} md={6}><Form.Item name="receiptDate" label="Ngày nhập" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" /></Form.Item></Col>
          <Col xs={24} md={8}><Form.Item name="supplierId" label="Nhà cung cấp">
            <Select allowClear placeholder="Chọn nhà cung cấp"
                    options={suppliers.map((s) => ({ value: s.id, label: s.supplierName }))} /></Form.Item></Col>
          <Col xs={24} md={10}><Form.Item name="note" label="Ghi chú"><Input /></Form.Item></Col>
        </Row>
      </Form>
      <Table rowKey="key" dataSource={lines} columns={columns} pagination={false} size="small"
             summary={() => (
               <Table.Summary.Row>
                 <Table.Summary.Cell colSpan={2} index={0}><b>Tổng cộng</b></Table.Summary.Cell>
                 <Table.Summary.Cell index={1} align="right"><b style={{ color: '#a0522d' }}>{fmt(total)} đ</b></Table.Summary.Cell>
                 <Table.Summary.Cell colSpan={2} index={2} />
               </Table.Summary.Row>
             )} />
      <Space style={{ marginTop: 16 }}>
        <Button icon={<PlusOutlined />} onClick={addLine}>Thêm dòng</Button>
        <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={submit}>Lưu phiếu nhập</Button>
      </Space>
    </>
  )
}

function ImportReceipt({ onDone }) {
  const [uploading, setUploading] = useState(false)
  const customRequest = async ({ file, onSuccess, onError }) => {
    setUploading(true)
    const fd = new FormData()
    fd.append('file', file)
    try {
      const res = await client.post('/api/goods-receipts/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      message.success('Import thành công & đã tăng tồn kho')
      onDone(res.data); onSuccess()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi import'); onError(e) }
    finally { setUploading(false) }
  }
  return (
    <>
      <Alert style={{ marginBottom: 16 }} type="info" showIcon message="Định dạng file"
             description={<span>Cột: <Text code>Material Code</Text> | <Text code>Quantity</Text> | <Text code>Thành tiền</Text>. Ví dụ: <Text code>MAT001, 5000, 1500000</Text> (đơn giá tự tính = tiền ÷ SL). Hỗ trợ .xlsx và .csv.</span>} />
      <Dragger accept=".xlsx,.csv" customRequest={customRequest} showUploadList={false} disabled={uploading}>
        <p className="ant-upload-drag-icon"><InboxOutlined /></p>
        <p className="ant-upload-text">Kéo thả hoặc bấm để chọn file Excel/CSV</p>
        <p className="ant-upload-hint">Tạo 1 phiếu nhập (ngày hôm nay) và tăng tồn kho tự động</p>
      </Dragger>
    </>
  )
}

export default function GoodsReceipt() {
  const [materials, setMaterials] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [done, setDone] = useState(null)

  useEffect(() => {
    client.get('/api/materials').then((r) => setMaterials(r.data))
    client.get('/api/suppliers').then((r) => setSuppliers(r.data)).catch(() => {})
  }, [])

  if (done) {
    return (
      <Result status="success"
        title={`Đã tạo phiếu nhập ${done.receiptNo}`}
        subTitle={`Tổng tiền: ${fmt(done.totalAmount)} đ — tồn kho & giá vốn đã được cập nhật tự động.`}
        extra={<Button type="primary" onClick={() => setDone(null)}>Tạo phiếu mới</Button>} />
    )
  }

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}>Phiếu nhập kho</Title>
      <Card>
        <Tabs items={[
          { key: 'manual', label: <span><EditOutlined /> Nhập tay</span>,
            children: <ManualReceipt materials={materials} suppliers={suppliers} onDone={setDone} /> },
          { key: 'import', label: <span><ImportOutlined /> Import Excel/CSV</span>,
            children: <ImportReceipt onDone={setDone} /> },
        ]} />
      </Card>
    </>
  )
}
