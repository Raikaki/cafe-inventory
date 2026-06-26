import { useEffect, useState } from 'react'
import {
  Card, Table, Typography, Button, Space, Row, message, Tag, Modal, Form, Select, InputNumber, Input, Popconfirm, Empty,
} from 'antd'
import { PlusOutlined, ReloadOutlined, CrownOutlined, DeleteOutlined, DollarOutlined } from '@ant-design/icons'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title, Text } = Typography

export default function PriceComparison() {
  const [data, setData] = useState([])
  const [materials, setMaterials] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    client.get('/api/price-comparison').then((r) => setData(r.data)).finally(() => setLoading(false))
  }
  useEffect(() => {
    load()
    client.get('/api/materials').then((r) => setMaterials(r.data))
    client.get('/api/suppliers').then((r) => setSuppliers(r.data))
  }, [])

  const openNew = (materialId) => { form.resetFields(); if (materialId) form.setFieldsValue({ materialId }); setOpen(true) }

  const submit = async () => {
    const v = await form.validateFields()
    try { await client.post('/api/price-comparison/quote', v); message.success('Đã lưu báo giá'); setOpen(false); load() }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const delQuote = async (id) => {
    try { await client.delete(`/api/price-comparison/quote/${id}`); message.success('Đã xoá báo giá'); load() }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const columns = [
    { title: 'Mã', dataIndex: 'materialCode', width: 110 },
    { title: 'Nguyên vật liệu', dataIndex: 'materialName' },
    { title: 'ĐVT', dataIndex: 'unit', width: 70, align: 'center' },
    { title: 'Tồn', dataIndex: 'currentQty', align: 'right', width: 100, render: fmt },
    { title: 'NCC đề xuất (rẻ nhất)', dataIndex: 'cheapestSupplierName', width: 200,
      render: (v) => v ? <Tag color="gold" icon={<CrownOutlined />}>{v}</Tag> : <Text type="secondary">Chưa có báo giá</Text> },
    { title: 'Giá rẻ nhất', dataIndex: 'cheapestPrice', align: 'right', width: 130,
      render: (v) => v != null ? <b style={{ color: '#52c41a' }}>{fmt(v)} đ</b> : '—' },
    { title: 'Số NCC', align: 'center', width: 80, render: (_, r) => r.options.length },
    { title: '', width: 110, render: (_, r) => (
      <Button size="small" icon={<PlusOutlined />} onClick={() => openNew(r.materialId)}>Báo giá</Button>
    )},
  ]

  const expanded = (row) => {
    if (!row.options.length) return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có giá NCC nào — bấm 'Báo giá' để thêm" />
    const optCols = [
      { title: 'Nhà cung cấp', dataIndex: 'supplierName',
        render: (v, o) => o.supplierId === row.cheapestSupplierId
          ? <span><Tag color="gold" icon={<CrownOutlined />}>Rẻ nhất</Tag> {v}</span> : v },
      { title: 'Giá', dataIndex: 'price', align: 'right', width: 140,
        render: (v, o) => <b style={{ color: o.supplierId === row.cheapestSupplierId ? '#52c41a' : undefined }}>{fmt(v)} đ</b> },
      { title: 'Nguồn', dataIndex: 'source', width: 130, align: 'center',
        render: (v) => v === 'QUOTE' ? <Tag color="blue">Báo giá</Tag> : <Tag>Lịch sử nhập</Tag> },
      { title: '', width: 60, render: (_, o) => o.quoteId
        ? <Popconfirm title="Xoá báo giá này?" onConfirm={() => delQuote(o.quoteId)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm> : null },
    ]
    return <Table rowKey={(o) => o.supplierId + '-' + o.source} dataSource={row.options} columns={optCols}
                  pagination={false} size="small" />
  }

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}><DollarOutlined /> So sánh giá nhà cung cấp</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={load}>Tải lại</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => openNew(null)}>Thêm báo giá</Button>
        </Space>
      </Row>

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="materialId" loading={loading} dataSource={data} columns={columns}
               expandable={{ expandedRowRender: expanded }}
               pagination={{ pageSize: 15 }} size="small" scroll={{ x: 900 }} />
      </Card>
      <Text type="secondary" style={{ fontSize: 12 }}>
        Giá lấy từ <Tag color="blue">Báo giá</Tag> bạn nhập và <Tag>Lịch sử nhập</Tag> thực tế. Hệ thống đề xuất NCC giá thấp nhất.
      </Text>

      <Modal title="Thêm / sửa báo giá nhà cung cấp" open={open} onOk={submit} onCancel={() => setOpen(false)}
             okText="Lưu" cancelText="Huỷ" destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="materialId" label="Nguyên vật liệu" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label"
                    options={materials.map((m) => ({ value: m.id, label: `${m.materialName} (${m.unit})` }))} />
          </Form.Item>
          <Form.Item name="supplierId" label="Nhà cung cấp" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label"
                    options={suppliers.filter((s) => s.activeFlag !== false).map((s) => ({ value: s.id, label: s.supplierName }))} />
          </Form.Item>
          <Form.Item name="price" label="Giá chào (đ / đơn vị)" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0}
                         formatter={(x) => `${x}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                         parser={(x) => `${x}`.replace(/,/g, '')} />
          </Form.Item>
          <Form.Item name="note" label="Ghi chú"><Input /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
