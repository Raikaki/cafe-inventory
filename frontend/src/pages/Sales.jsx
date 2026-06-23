import { useEffect, useState } from 'react'
import {
  Card, Tabs, Form, DatePicker, Select, Button, Table, InputNumber, Space,
  Typography, message, Upload, Alert, Tag,
} from 'antd'
import { PlusOutlined, DeleteOutlined, ShoppingCartOutlined, UploadOutlined, InboxOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title, Text } = Typography
const { Dragger } = Upload

function ResultView({ result }) {
  if (!result) return null
  return (
    <Card style={{ marginTop: 16 }} title={<span>Kết quả lô <Tag color="blue">{result.batchNo}</Tag> — {result.salesLines} dòng bán</span>}>
      {result.warnings?.length > 0 && (
        <Alert type="warning" showIcon style={{ marginBottom: 12 }}
               message="Cảnh báo" description={<ul style={{ margin: 0 }}>{result.warnings.map((w, i) => <li key={i}>{w}</li>)}</ul>} />
      )}
      <Table rowKey="materialId" size="small" pagination={false} dataSource={result.consumption}
             columns={[
               { title: 'Mã', dataIndex: 'materialCode', width: 100 },
               { title: 'Nguyên vật liệu', dataIndex: 'materialName' },
               { title: 'ĐVT', dataIndex: 'unit', width: 70, align: 'center' },
               { title: 'Đã dùng', dataIndex: 'consumedQty', align: 'right', render: (v) => <b>{fmt(v)}</b> },
               { title: 'Tồn trước', dataIndex: 'beforeQty', align: 'right', render: fmt },
               { title: 'Tồn sau', dataIndex: 'afterQty', align: 'right',
                 render: (v) => <span style={{ color: Number(v) < 0 ? '#cf1322' : '#52c41a', fontWeight: 600 }}>{fmt(v)}</span> },
             ]} />
    </Card>
  )
}

function ManualTab({ products, onResult }) {
  const [form] = Form.useForm()
  const [lines, setLines] = useState([{ key: 1, productCode: null, quantity: 0 }])
  const [saving, setSaving] = useState(false)

  const addLine = () => setLines((p) => [...p, { key: Date.now(), productCode: null, quantity: 0 }])
  const removeLine = (k) => setLines((p) => p.filter((l) => l.key !== k))
  const updateLine = (k, f, v) => setLines((p) => p.map((l) => l.key === k ? { ...l, [f]: v } : l))

  const submit = async () => {
    const v = await form.validateFields()
    const payload = {
      saleDate: v.saleDate.format('YYYY-MM-DD'),
      lines: lines.filter((l) => l.productCode && Number(l.quantity) > 0)
        .map((l) => ({ productCode: l.productCode, quantity: l.quantity })),
    }
    if (payload.lines.length === 0) { message.warning('Thêm ít nhất 1 sản phẩm'); return }
    setSaving(true)
    try {
      const res = await client.post('/api/sales', payload)
      message.success('Đã ghi nhận & trừ kho')
      onResult(res.data)
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi') }
    finally { setSaving(false) }
  }

  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productCode', render: (v, r) => (
      <Select showSearch optionFilterProp="label" style={{ width: '100%' }} value={v} placeholder="Chọn sản phẩm"
              onChange={(val) => updateLine(r.key, 'productCode', val)}
              options={products.map((p) => ({ value: p.productCode, label: `${p.productName} (${p.productCode})` }))} />
    )},
    { title: 'Số lượng', dataIndex: 'quantity', width: 160, render: (v, r) => (
      <InputNumber style={{ width: '100%' }} min={0} value={v} onChange={(val) => updateLine(r.key, 'quantity', val)} />
    )},
    { title: '', width: 50, render: (_, r) => <Button size="small" danger icon={<DeleteOutlined />} onClick={() => removeLine(r.key)} /> },
  ]

  return (
    <>
      <Form form={form} layout="inline" initialValues={{ saleDate: dayjs() }} style={{ marginBottom: 16 }}>
        <Form.Item name="saleDate" label="Ngày bán" rules={[{ required: true }]}>
          <DatePicker format="DD/MM/YYYY" />
        </Form.Item>
      </Form>
      <Table rowKey="key" dataSource={lines} columns={columns} pagination={false} size="small" />
      <Space style={{ marginTop: 16 }}>
        <Button icon={<PlusOutlined />} onClick={addLine}>Thêm sản phẩm</Button>
        <Button type="primary" icon={<ShoppingCartOutlined />} loading={saving} onClick={submit}>Ghi nhận & trừ kho</Button>
      </Space>
    </>
  )
}

function ImportTab({ onResult }) {
  const [uploading, setUploading] = useState(false)

  const customRequest = async ({ file, onSuccess, onError }) => {
    setUploading(true)
    const fd = new FormData()
    fd.append('file', file)
    try {
      const res = await client.post('/api/sales/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      message.success('Import thành công & đã trừ kho')
      onResult(res.data); onSuccess()
    } catch (e) {
      message.error(e.response?.data?.message || 'Lỗi import')
      onError(e)
    } finally { setUploading(false) }
  }

  return (
    <>
      <Alert style={{ marginBottom: 16 }} type="info" showIcon
             message="Định dạng file"
             description={<span>Cột: <Text code>Sale Date</Text> | <Text code>Product Code</Text> | <Text code>Quantity</Text>. Ví dụ: <Text code>2026-01-01, CF002, 50</Text>. Hỗ trợ .xlsx và .csv (có/không header).</span>} />
      <Dragger accept=".xlsx,.csv" customRequest={customRequest} showUploadList={false} disabled={uploading}>
        <p className="ant-upload-drag-icon"><InboxOutlined /></p>
        <p className="ant-upload-text">Kéo thả hoặc bấm để chọn file Excel/CSV</p>
        <p className="ant-upload-hint">Hệ thống sẽ nổ công thức và tự động trừ kho</p>
      </Dragger>
    </>
  )
}

export default function Sales() {
  const [products, setProducts] = useState([])
  const [result, setResult] = useState(null)

  useEffect(() => { client.get('/api/products').then((r) => setProducts(r.data)) }, [])

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}>Bán hàng → Tự động trừ kho</Title>
      <Card>
        <Tabs items={[
          { key: 'manual', label: <span><ShoppingCartOutlined /> Nhập tay</span>, children: <ManualTab products={products} onResult={setResult} /> },
          { key: 'import', label: <span><UploadOutlined /> Import Excel/CSV</span>, children: <ImportTab onResult={setResult} /> },
        ]} />
      </Card>
      <ResultView result={result} />
    </>
  )
}
