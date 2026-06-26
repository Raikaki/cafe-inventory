import { useEffect, useState } from 'react'
import {
  Card, Table, Button, Modal, Form, Input, InputNumber, Space, Tag, Popconfirm,
  Typography, message, Row, Col, Tooltip, AutoComplete,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, SlidersOutlined } from '@ant-design/icons'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title, Text } = Typography

export default function Materials() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form] = Form.useForm()

  const [adjOpen, setAdjOpen] = useState(false)
  const [adjMat, setAdjMat] = useState(null)
  const [adjForm] = Form.useForm()

  const [units, setUnits] = useState([])
  const load = () => {
    setLoading(true)
    client.get('/api/materials').then((res) => setData(res.data)).finally(() => setLoading(false))
  }
  useEffect(() => {
    load()
    client.get('/api/units').then((r) => setUnits(r.data.map((u) => ({ value: u.code }))))
  }, [])

  const openNew = () => { setEditing(null); form.resetFields(); form.setFieldsValue({ currentQty: 0, minimumQty: 0, maximumQty: 0, averageCost: 0 }); setOpen(true) }
  const openEdit = (r) => { setEditing(r); form.setFieldsValue(r); setOpen(true) }

  const submit = async () => {
    const v = await form.validateFields()
    const body = { ...v, activeFlag: true }
    try {
      if (editing) await client.put(`/api/materials/${editing.id}`, body)
      else await client.post('/api/materials', body)
      message.success('Đã lưu')
      setOpen(false); load()
    } catch (e) {
      message.error(e.response?.data?.message || 'Lỗi khi lưu')
    }
  }

  const remove = async (r) => {
    try { await client.delete(`/api/materials/${r.id}`); message.success('Đã ngừng sử dụng'); load() }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const openAdjust = (r) => { setAdjMat(r); adjForm.resetFields(); adjForm.setFieldsValue({ quantity: 0 }); setAdjOpen(true) }
  const submitAdjust = async () => {
    const v = await adjForm.validateFields()
    if (Number(v.quantity) === 0) { message.warning('Nhập số lượng điều chỉnh khác 0'); return }
    try {
      await client.post('/api/inventory/adjust', { materialId: adjMat.id, quantity: v.quantity, reason: v.reason })
      message.success('Đã điều chỉnh tồn (ghi nhận giao dịch)')
      setAdjOpen(false); load()
    } catch (e) {
      message.error(e.response?.data?.message || 'Lỗi điều chỉnh')
    }
  }

  const columns = [
    { title: 'Mã', dataIndex: 'materialCode', width: 110, fixed: 'left' },
    { title: 'Tên nguyên vật liệu', dataIndex: 'materialName' },
    { title: 'ĐVT', dataIndex: 'unit', width: 80, align: 'center' },
    { title: 'Tồn kho', dataIndex: 'currentQty', align: 'right', width: 120,
      render: (v, r) => <b style={{ color: Number(v) < Number(r.minimumQty) ? '#cf1322' : undefined }}>{fmt(v)}</b> },
    { title: 'Tối thiểu', dataIndex: 'minimumQty', align: 'right', width: 110, render: fmt },
    { title: 'Tối đa', dataIndex: 'maximumQty', align: 'right', width: 110, render: fmt },
    { title: 'Giá vốn TB', dataIndex: 'averageCost', align: 'right', width: 120, render: fmt },
    { title: 'Trạng thái', dataIndex: 'activeFlag', width: 110, align: 'center',
      render: (v) => v ? <Tag color="green">Hoạt động</Tag> : <Tag>Ngừng</Tag> },
    { title: '', width: 140, fixed: 'right', render: (_, r) => (
      <Space>
        <Tooltip title="Điều chỉnh tồn">
          <Button size="small" icon={<SlidersOutlined />} onClick={() => openAdjust(r)} />
        </Tooltip>
        <Tooltip title="Sửa thông tin">
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)} />
        </Tooltip>
        <Popconfirm title="Ngừng sử dụng vật liệu này?" onConfirm={() => remove(r)}>
          <Button size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      </Space>
    )},
  ]

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>Nguyên vật liệu</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={load}>Tải lại</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openNew}>Thêm</Button>
        </Space>
      </Row>

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={data} columns={columns}
               scroll={{ x: 1050 }} pagination={{ pageSize: 12, showSizeChanger: false }} />
      </Card>

      {/* Create / edit */}
      <Modal title={editing ? 'Sửa nguyên vật liệu' : 'Thêm nguyên vật liệu'} open={open}
             onOk={submit} onCancel={() => setOpen(false)} okText="Lưu" cancelText="Huỷ" width={560} destroyOnClose>
        <Form form={form} layout="vertical">
          <Row gutter={12}>
            <Col span={12}><Form.Item name="materialCode" label="Mã" rules={[{ required: true }]}><Input /></Form.Item></Col>
            <Col span={12}><Form.Item name="unit" label="Đơn vị tính (gốc)" rules={[{ required: true }]}>
              <AutoComplete options={units} placeholder="chọn/gõ: g, ml, cái"
                            filterOption={(i, o) => o.value.toLowerCase().includes(i.toLowerCase())} /></Form.Item></Col>
          </Row>
          <Form.Item name="materialName" label="Tên" rules={[{ required: true }]}><Input /></Form.Item>
          <Row gutter={12}>
            <Col span={12}><Form.Item name="purchaseUnit" label="Đơn vị mua" extra="vd. mua theo kg/thùng">
              <AutoComplete options={units} placeholder="chọn/gõ: kg, thùng"
                            filterOption={(i, o) => o.value.toLowerCase().includes(i.toLowerCase())} /></Form.Item></Col>
            <Col span={12}><Form.Item name="conversionFactor" label="Quy đổi (1 ĐV mua = ? ĐVT)" extra="vd. 1 kg = 1000 g">
              <InputNumber style={{ width: '100%' }} min={0} placeholder="1" /></Form.Item></Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item name="currentQty" label={editing ? 'Tồn kho hiện tại' : 'Tồn kho đầu kỳ'}
                         extra={editing
                           ? 'Tồn chỉ thay đổi qua Nhập kho / Bán hàng / Điều chỉnh'
                           : 'Sẽ được ghi nhận thành giao dịch tồn đầu kỳ'}>
                <InputNumber style={{ width: '100%' }} min={0} disabled={!!editing} />
              </Form.Item>
            </Col>
            <Col span={12}><Form.Item name="averageCost" label="Giá vốn TB"><InputNumber style={{ width: '100%' }} min={0} /></Form.Item></Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}><Form.Item name="minimumQty" label="Tồn tối thiểu"><InputNumber style={{ width: '100%' }} min={0} /></Form.Item></Col>
            <Col span={12}><Form.Item name="maximumQty" label="Tồn tối đa"><InputNumber style={{ width: '100%' }} min={0} /></Form.Item></Col>
          </Row>
        </Form>
      </Modal>

      {/* Adjust stock */}
      <Modal title={`Điều chỉnh tồn: ${adjMat?.materialName || ''}`} open={adjOpen}
             onOk={submitAdjust} onCancel={() => setAdjOpen(false)} okText="Ghi nhận" cancelText="Huỷ" destroyOnClose>
        <Text type="secondary">Tồn hiện tại: <b>{fmt(adjMat?.currentQty)} {adjMat?.unit}</b></Text>
        <Form form={adjForm} layout="vertical" style={{ marginTop: 12 }}>
          <Form.Item name="quantity" label="Số lượng điều chỉnh (dương = tăng, âm = giảm)" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="reason" label="Lý do">
            <Input placeholder="Hỏng, hết hạn, kiểm kê, hao hụt..." />
          </Form.Item>
        </Form>
      </Modal>
    </>
  )
}
