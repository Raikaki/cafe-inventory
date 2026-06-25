import { useEffect, useState } from 'react'
import {
  Card, Table, Button, Modal, Form, Input, Space, Tag, Popconfirm,
  Typography, message, Row,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import client from '../api/client'

const { Title } = Typography

export default function Suppliers() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    client.get('/api/suppliers').then((res) => setData(res.data)).finally(() => setLoading(false))
  }
  useEffect(load, [])

  const openNew = () => { setEditing(null); form.resetFields(); setOpen(true) }
  const openEdit = (r) => { setEditing(r); form.setFieldsValue(r); setOpen(true) }

  const submit = async () => {
    const v = await form.validateFields()
    const body = { ...v, activeFlag: true }
    try {
      if (editing) await client.put(`/api/suppliers/${editing.id}`, body)
      else await client.post('/api/suppliers', body)
      message.success('Đã lưu'); setOpen(false); load()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi khi lưu') }
  }

  const remove = async (r) => {
    try { await client.delete(`/api/suppliers/${r.id}`); message.success('Đã ngừng sử dụng'); load() }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const columns = [
    { title: 'Mã', dataIndex: 'supplierCode', width: 120 },
    { title: 'Tên nhà cung cấp', dataIndex: 'supplierName' },
    { title: 'Điện thoại', dataIndex: 'phone', width: 140 },
    { title: 'Email', dataIndex: 'email', width: 200 },
    { title: 'Địa chỉ', dataIndex: 'address' },
    { title: 'Trạng thái', dataIndex: 'activeFlag', width: 120, align: 'center',
      render: (v) => v ? <Tag color="green">Hoạt động</Tag> : <Tag>Ngừng</Tag> },
    { title: '', width: 100, render: (_, r) => (
      <Space>
        <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)} />
        <Popconfirm title="Ngừng sử dụng nhà cung cấp này?" onConfirm={() => remove(r)}>
          <Button size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      </Space>
    )},
  ]

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>Nhà cung cấp</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openNew}>Đăng ký NCC</Button>
      </Row>

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={data} columns={columns}
               pagination={{ pageSize: 12, showSizeChanger: false }} />
      </Card>

      <Modal title={editing ? 'Sửa nhà cung cấp' : 'Đăng ký nhà cung cấp'} open={open}
             onOk={submit} onCancel={() => setOpen(false)} okText="Lưu" cancelText="Huỷ" destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="supplierCode" label="Mã" rules={[{ required: true }]}><Input placeholder="VD: SUP002" /></Form.Item>
          <Form.Item name="supplierName" label="Tên nhà cung cấp" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="phone" label="Điện thoại"><Input /></Form.Item>
          <Form.Item name="email" label="Email"><Input type="email" /></Form.Item>
          <Form.Item name="address" label="Địa chỉ"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
