import { useEffect, useState } from 'react'
import {
  Card, Table, Typography, Button, Space, Row, Col, message, Tag, Modal, Form, Select, InputNumber, Input, DatePicker, Alert,
} from 'antd'
import { PlusOutlined, ReloadOutlined, WarningOutlined, MinusCircleOutlined, ExperimentOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography

const statusMeta = {
  EXPIRED: { color: 'red', label: 'Hết hạn' },
  EXPIRING: { color: 'orange', label: 'Sắp hết hạn' },
  OK: { color: 'green', label: 'Còn hạn' },
  NO_EXPIRY: { color: 'default', label: 'Không HSD' },
}

export default function Batches() {
  const [data, setData] = useState([])
  const [materials, setMaterials] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [form] = Form.useForm()
  const [disp, setDisp] = useState(null)
  const [dispForm] = Form.useForm()

  const load = () => {
    setLoading(true)
    client.get('/api/batches').then((r) => setData(r.data)).finally(() => setLoading(false))
  }
  useEffect(() => {
    load()
    client.get('/api/materials').then((r) => setMaterials(r.data.filter((m) => m.activeFlag !== false)))
    client.get('/api/suppliers').then((r) => setSuppliers(r.data))
  }, [])

  const openNew = () => { form.resetFields(); form.setFieldsValue({ receivedDate: dayjs() }); setOpen(true) }
  const submit = async () => {
    const v = await form.validateFields()
    const payload = {
      ...v,
      receivedDate: v.receivedDate ? v.receivedDate.format('YYYY-MM-DD') : null,
      expiryDate: v.expiryDate ? v.expiryDate.format('YYYY-MM-DD') : null,
    }
    try { await client.post('/api/batches', payload); message.success('Đã thêm lô'); setOpen(false); load() }
    catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const submitDispose = async () => {
    const v = await dispForm.validateFields()
    try {
      await client.post(`/api/batches/${disp.id}/dispose?qty=${v.qty}&reason=${encodeURIComponent(v.reason || '')}`)
      message.success('Đã ghi nhận'); setDisp(null); load()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
  }

  const expired = data.filter((b) => b.status === 'EXPIRED').length
  const expiring = data.filter((b) => b.status === 'EXPIRING').length

  const columns = [
    { title: 'Mã', dataIndex: 'materialCode', width: 100 },
    { title: 'Nguyên vật liệu', dataIndex: 'materialName' },
    { title: 'Lô', dataIndex: 'batchNo', width: 110, render: (v) => v || '—' },
    { title: 'NCC', dataIndex: 'supplierName', width: 140, render: (v) => v || '—' },
    { title: 'Ngày nhập', dataIndex: 'receivedDate', width: 110, render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    { title: 'HSD', dataIndex: 'expiryDate', width: 110, render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    { title: 'Còn lại', dataIndex: 'remainingQty', align: 'right', width: 110,
      render: (v, r) => `${fmt(v)} ${r.unit}` },
    { title: 'Trạng thái', dataIndex: 'status', width: 150, align: 'center',
      render: (v, r) => (
        <Tag color={statusMeta[v]?.color}>
          {statusMeta[v]?.label}{r.daysToExpiry != null && v !== 'OK' && v !== 'NO_EXPIRY' ? ` (${r.daysToExpiry}d)` : ''}
        </Tag>
      ) },
    { title: '', width: 100, render: (_, r) => (
      <Button size="small" icon={<MinusCircleOutlined />} onClick={() => { setDisp(r); dispForm.resetFields() }}>Huỷ/dùng</Button>
    )},
  ]

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}><ExperimentOutlined /> Lô &amp; Hạn sử dụng</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={load}>Tải lại</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openNew}>Thêm lô</Button>
        </Space>
      </Row>

      {(expired > 0 || expiring > 0) && (
        <Alert style={{ marginBottom: 16 }} type={expired > 0 ? 'error' : 'warning'} showIcon icon={<WarningOutlined />}
               message={`Cảnh báo: ${expired} lô đã hết hạn, ${expiring} lô sắp hết hạn (≤7 ngày)`}
               description="Ưu tiên dùng lô gần hết hạn trước (FEFO); huỷ lô đã hết hạn để trừ tồn." />
      )}

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={data} columns={columns}
               pagination={{ pageSize: 15 }} size="small" scroll={{ x: 1000 }}
               rowClassName={(r) => r.status === 'EXPIRED' ? 'row-critical' : ''}
               locale={{ emptyText: 'Chưa có lô nào — bấm "Thêm lô"' }} />
      </Card>

      <Modal title="Thêm lô hàng" open={open} onOk={submit} onCancel={() => setOpen(false)}
             okText="Lưu" cancelText="Huỷ" destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="materialId" label="Nguyên vật liệu" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label"
                    options={materials.map((m) => ({ value: m.id, label: `${m.materialName} (${m.unit})` }))} />
          </Form.Item>
          <Row gutter={12}>
            <Col span={12}><Form.Item name="batchNo" label="Số lô"><Input /></Form.Item></Col>
            <Col span={12}><Form.Item name="receivedQty" label="Số lượng" rules={[{ required: true }]}>
              <InputNumber style={{ width: '100%' }} min={0} /></Form.Item></Col>
          </Row>
          <Form.Item name="supplierId" label="Nhà cung cấp">
            <Select allowClear options={suppliers.map((s) => ({ value: s.id, label: s.supplierName }))} />
          </Form.Item>
          <Row gutter={12}>
            <Col span={12}><Form.Item name="receivedDate" label="Ngày nhập"><DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" /></Form.Item></Col>
            <Col span={12}><Form.Item name="expiryDate" label="Hạn sử dụng"><DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" /></Form.Item></Col>
          </Row>
          <Form.Item name="note" label="Ghi chú"><Input /></Form.Item>
        </Form>
      </Modal>

      <Modal title={disp ? `Huỷ/dùng lô: ${disp.materialName}` : ''} open={!!disp}
             onOk={submitDispose} onCancel={() => setDisp(null)} okText="Ghi nhận" cancelText="Huỷ" destroyOnClose>
        {disp && <div style={{ marginBottom: 8 }}>Còn lại: <b>{fmt(disp.remainingQty)} {disp.unit}</b></div>}
        <Form form={dispForm} layout="vertical">
          <Form.Item name="qty" label="Số lượng huỷ/dùng" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0} max={disp?.remainingQty} />
          </Form.Item>
          <Form.Item name="reason" label="Lý do"><Input placeholder="Hết hạn, hỏng, đã dùng..." /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
