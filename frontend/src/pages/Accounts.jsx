import { useEffect, useState } from 'react'
import { Card, Table, Button, Modal, Form, Input, Space, Typography, message, Row, Popconfirm, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import client from '../api/client'

const { Title } = Typography

export default function Accounts() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    client.get('/api/accounts').then((r) => setData(r.data)).finally(() => setLoading(false))
  }
  useEffect(load, [])

  const openNew = () => { setEditing(null); form.resetFields(); setOpen(true) }
  const openEdit = (r) => { setEditing(r); form.setFieldsValue(r); setOpen(true) }

  const submit = async () => {
    const v = await form.validateFields()
    try {
      if (editing) await client.put(`/api/accounts/${editing.id}`, { ...v, activeFlag: true })
      else await client.post('/api/accounts', v)
      message.success('Đã lưu'); setOpen(false); load()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const remove = async (r) => {
    try { await client.delete(`/api/accounts/${r.id}`); message.success('Đã xoá'); load() }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const columns = [
    { title: 'Số hiệu TK', dataIndex: 'accountCode', width: 150, render: (v) => <Tag color="purple">{v}</Tag> },
    { title: 'Tên tài khoản', dataIndex: 'accountName' },
    { title: '', width: 100, render: (_, r) => (
      <Space>
        <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)} />
        <Popconfirm title="Xoá tài khoản này?" onConfirm={() => remove(r)}>
          <Button size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      </Space>
    )},
  ]

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>Hệ thống tài khoản kế toán</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openNew}>Thêm tài khoản</Button>
      </Row>
      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={data} columns={columns} pagination={{ pageSize: 20 }} size="small" />
      </Card>

      <Modal title={editing ? 'Sửa tài khoản' : 'Thêm tài khoản'} open={open} onOk={submit} onCancel={() => setOpen(false)}
             okText="Lưu" cancelText="Huỷ" destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="accountCode" label="Số hiệu TK" rules={[{ required: true }]}><Input placeholder="111, 152, 511..." /></Form.Item>
          <Form.Item name="accountName" label="Tên tài khoản"><Input placeholder="Tiền mặt..." /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
