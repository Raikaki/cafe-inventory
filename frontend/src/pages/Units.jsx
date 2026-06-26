import { useEffect, useState } from 'react'
import { Card, Table, Button, Modal, Form, Input, Space, Typography, message, Row, Popconfirm, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import client from '../api/client'

const { Title } = Typography

export default function Units() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    client.get('/api/units').then((r) => setData(r.data)).finally(() => setLoading(false))
  }
  useEffect(load, [])

  const openNew = () => { setEditing(null); form.resetFields(); setOpen(true) }
  const openEdit = (r) => { setEditing(r); form.setFieldsValue(r); setOpen(true) }

  const submit = async () => {
    const v = await form.validateFields()
    try {
      if (editing) await client.put(`/api/units/${editing.id}`, { ...v, activeFlag: true })
      else await client.post('/api/units', v)
      message.success('Đã lưu'); setOpen(false); load()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const remove = async (r) => {
    try { await client.delete(`/api/units/${r.id}`); message.success('Đã xoá'); load() }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const columns = [
    { title: 'Mã đơn vị', dataIndex: 'code', width: 160, render: (v) => <Tag color="purple">{v}</Tag> },
    { title: 'Tên', dataIndex: 'name' },
    { title: '', width: 100, render: (_, r) => (
      <Space>
        <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)} />
        <Popconfirm title="Xoá đơn vị này?" onConfirm={() => remove(r)}>
          <Button size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      </Space>
    )},
  ]

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>Đơn vị tính</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openNew}>Thêm đơn vị</Button>
      </Row>
      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={data} columns={columns} pagination={{ pageSize: 12 }} />
      </Card>

      <Modal title={editing ? 'Sửa đơn vị' : 'Thêm đơn vị'} open={open} onOk={submit} onCancel={() => setOpen(false)}
             okText="Lưu" cancelText="Huỷ" destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="Mã đơn vị" rules={[{ required: true }]}><Input placeholder="kg, g, thùng..." /></Form.Item>
          <Form.Item name="name" label="Tên"><Input placeholder="Kilogram..." /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
