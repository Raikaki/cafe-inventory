import { useEffect, useState } from 'react'
import {
  Card, Table, Button, Modal, Form, Input, InputNumber, Space, Tag, Popconfirm,
  Typography, message, Row,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ExperimentOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography

export default function Products() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const load = () => {
    setLoading(true)
    client.get('/api/products').then((res) => setData(res.data)).finally(() => setLoading(false))
  }
  useEffect(load, [])

  const openNew = () => { setEditing(null); form.resetFields(); form.setFieldsValue({ salePrice: 0 }); setOpen(true) }
  const openEdit = (r) => { setEditing(r); form.setFieldsValue(r); setOpen(true) }

  const submit = async () => {
    const v = await form.validateFields()
    const body = { ...v, activeFlag: true }
    try {
      if (editing) await client.put(`/api/products/${editing.id}`, body)
      else await client.post('/api/products', body)
      message.success('Đã lưu'); setOpen(false); load()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi khi lưu') }
  }

  const remove = async (r) => {
    try { await client.delete(`/api/products/${r.id}`); message.success('Đã ngừng sử dụng'); load() }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const columns = [
    { title: 'Mã', dataIndex: 'productCode', width: 130 },
    { title: 'Tên sản phẩm', dataIndex: 'productName' },
    { title: 'Giá bán', dataIndex: 'salePrice', align: 'right', width: 140, render: (v) => fmt(v) + ' đ' },
    { title: 'Trạng thái', dataIndex: 'activeFlag', width: 120, align: 'center',
      render: (v) => v ? <Tag color="green">Hoạt động</Tag> : <Tag>Ngừng</Tag> },
    { title: '', width: 150, render: (_, r) => (
      <Space>
        <Button size="small" icon={<ExperimentOutlined />} onClick={() => navigate(`/recipes?productId=${r.id}`)}>Công thức</Button>
        <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)} />
        <Popconfirm title="Ngừng sử dụng sản phẩm này?" onConfirm={() => remove(r)}>
          <Button size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      </Space>
    )},
  ]

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>Sản phẩm</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openNew}>Thêm</Button>
      </Row>

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={data} columns={columns}
               pagination={{ pageSize: 12, showSizeChanger: false }} />
      </Card>

      <Modal title={editing ? 'Sửa sản phẩm' : 'Thêm sản phẩm'} open={open}
             onOk={submit} onCancel={() => setOpen(false)} okText="Lưu" cancelText="Huỷ" destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="productCode" label="Mã" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="productName" label="Tên" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="salePrice" label="Giá bán" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0} formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  )
}
