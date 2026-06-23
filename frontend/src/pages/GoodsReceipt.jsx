import { useEffect, useState } from 'react'
import {
  Card, Form, DatePicker, Select, Input, Button, Table, InputNumber, Space,
  Typography, message, Row, Col, Result,
} from 'antd'
import { PlusOutlined, DeleteOutlined, SaveOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography

export default function GoodsReceipt() {
  const [materials, setMaterials] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [lines, setLines] = useState([{ key: 1, materialId: null, quantity: 0, unitPrice: 0 }])
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)
  const [done, setDone] = useState(null)

  useEffect(() => {
    client.get('/api/materials').then((r) => setMaterials(r.data))
    client.get('/api/suppliers').then((r) => setSuppliers(r.data)).catch(() => {})
  }, [])

  const addLine = () => setLines((p) => [...p, { key: Date.now(), materialId: null, quantity: 0, unitPrice: 0 }])
  const removeLine = (key) => setLines((p) => p.filter((l) => l.key !== key))
  const updateLine = (key, f, v) => setLines((p) => p.map((l) => l.key === key ? { ...l, [f]: v } : l))

  const total = lines.reduce((s, l) => s + Number(l.quantity || 0) * Number(l.unitPrice || 0), 0)

  const submit = async () => {
    const v = await form.validateFields()
    const payload = {
      receiptDate: v.receiptDate.format('YYYY-MM-DD'),
      supplierId: v.supplierId || null,
      note: v.note || null,
      lines: lines.filter((l) => l.materialId && Number(l.quantity) > 0)
        .map((l) => ({ materialId: l.materialId, quantity: l.quantity, unitPrice: l.unitPrice })),
    }
    if (payload.lines.length === 0) { message.warning('Thêm ít nhất 1 dòng nguyên liệu'); return }
    setSaving(true)
    try {
      const res = await client.post('/api/goods-receipts', payload)
      setDone(res.data)
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi khi lưu') }
    finally { setSaving(false) }
  }

  const reset = () => {
    setDone(null); form.resetFields()
    form.setFieldsValue({ receiptDate: dayjs() })
    setLines([{ key: 1, materialId: null, quantity: 0, unitPrice: 0 }])
  }

  if (done) {
    return (
      <Result status="success"
        title={`Đã tạo phiếu nhập ${done.receiptNo}`}
        subTitle={`Tổng tiền: ${fmt(done.totalAmount)} đ — tồn kho & giá vốn đã được cập nhật tự động.`}
        extra={<Button type="primary" onClick={reset}>Tạo phiếu mới</Button>} />
    )
  }

  const columns = [
    { title: 'Nguyên vật liệu', dataIndex: 'materialId', render: (v, r) => (
      <Select showSearch optionFilterProp="label" style={{ width: '100%' }} value={v} placeholder="Chọn"
              onChange={(val) => updateLine(r.key, 'materialId', val)}
              options={materials.map((m) => ({ value: m.id, label: `${m.materialName} (${m.unit})` }))} />
    )},
    { title: 'Số lượng', dataIndex: 'quantity', width: 150, render: (v, r) => (
      <InputNumber style={{ width: '100%' }} min={0} value={v} onChange={(val) => updateLine(r.key, 'quantity', val)} />
    )},
    { title: 'Đơn giá', dataIndex: 'unitPrice', width: 150, render: (v, r) => (
      <InputNumber style={{ width: '100%' }} min={0} value={v} onChange={(val) => updateLine(r.key, 'unitPrice', val)} />
    )},
    { title: 'Thành tiền', width: 140, align: 'right', render: (_, r) => <b>{fmt(Number(r.quantity || 0) * Number(r.unitPrice || 0))}</b> },
    { title: '', width: 50, render: (_, r) => <Button size="small" danger icon={<DeleteOutlined />} onClick={() => removeLine(r.key)} /> },
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}>Phiếu nhập kho</Title>
      <Card>
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
                   <Table.Summary.Cell colSpan={3} index={0}><b>Tổng cộng</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={1} align="right"><b style={{ color: '#a0522d' }}>{fmt(total)} đ</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={2} />
                 </Table.Summary.Row>
               )} />

        <Space style={{ marginTop: 16 }}>
          <Button icon={<PlusOutlined />} onClick={addLine}>Thêm dòng</Button>
          <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={submit}>Lưu phiếu nhập</Button>
        </Space>
      </Card>
    </>
  )
}
