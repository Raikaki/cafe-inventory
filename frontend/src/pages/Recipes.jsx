import { useEffect, useState } from 'react'
import {
  Card, Select, Button, Form, InputNumber, Space, Typography, message, Row, Col,
  Tag, Table, Empty, Statistic,
} from 'antd'
import { PlusOutlined, DeleteOutlined, SaveOutlined } from '@ant-design/icons'
import { useSearchParams } from 'react-router-dom'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography

export default function Recipes() {
  const [products, setProducts] = useState([])
  const [materials, setMaterials] = useState([])
  const [recipe, setRecipe] = useState(null)
  const [productId, setProductId] = useState(null)
  const [lines, setLines] = useState([])
  const [saving, setSaving] = useState(false)
  const [params, setParams] = useSearchParams()

  useEffect(() => {
    client.get('/api/products').then((r) => setProducts(r.data))
    client.get('/api/materials').then((r) => setMaterials(r.data))
    const pid = params.get('productId')
    if (pid) selectProduct(Number(pid))
  }, [])

  const selectProduct = (pid) => {
    setProductId(pid)
    setParams({ productId: pid })
    client.get(`/api/recipes/product/${pid}`).then((r) => {
      setRecipe(r.data)
      setLines((r.data.lines || []).map((l, i) => ({ key: i, materialId: l.materialId, standardQty: Number(l.standardQty) })))
    })
  }

  const addLine = () => setLines((p) => [...p, { key: Date.now(), materialId: null, standardQty: 0 }])
  const removeLine = (key) => setLines((p) => p.filter((l) => l.key !== key))
  const updateLine = (key, field, val) => setLines((p) => p.map((l) => l.key === key ? { ...l, [field]: val } : l))

  const matCost = (id) => Number(materials.find((m) => m.id === id)?.averageCost || 0)
  const totalCost = lines.reduce((s, l) => s + matCost(l.materialId) * Number(l.standardQty || 0), 0)

  const save = async () => {
    const payload = {
      productId,
      lines: lines.filter((l) => l.materialId && Number(l.standardQty) > 0)
        .map((l) => ({ materialId: l.materialId, standardQty: l.standardQty })),
    }
    setSaving(true)
    try {
      await client.post('/api/recipes', payload)
      message.success('Đã lưu công thức')
      selectProduct(productId)
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi khi lưu') }
    finally { setSaving(false) }
  }

  const columns = [
    { title: 'Nguyên vật liệu', dataIndex: 'materialId', render: (v, r) => (
      <Select showSearch optionFilterProp="label" style={{ width: '100%' }} value={v}
              placeholder="Chọn nguyên liệu" onChange={(val) => updateLine(r.key, 'materialId', val)}
              options={materials.map((m) => ({ value: m.id, label: `${m.materialName} (${m.unit})` }))} />
    )},
    { title: 'Định mức / 1 ly', dataIndex: 'standardQty', width: 180, render: (v, r) => (
      <InputNumber style={{ width: '100%' }} min={0} value={v} onChange={(val) => updateLine(r.key, 'standardQty', val)} />
    )},
    { title: 'Đơn giá', width: 120, align: 'right', render: (_, r) => fmt(matCost(r.materialId)) },
    { title: 'Thành tiền', width: 130, align: 'right',
      render: (_, r) => <b>{fmt(matCost(r.materialId) * Number(r.standardQty || 0))}</b> },
    { title: '', width: 50, render: (_, r) => (
      <Button size="small" danger icon={<DeleteOutlined />} onClick={() => removeLine(r.key)} />
    )},
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}>Công thức / Định mức (BOM)</Title>
      <Row gutter={16}>
        <Col xs={24} md={8} lg={6}>
          <Card title="Chọn sản phẩm">
            <Select style={{ width: '100%' }} showSearch optionFilterProp="label"
                    placeholder="Chọn sản phẩm" value={productId} onChange={selectProduct}
                    options={products.map((p) => ({ value: p.id, label: `${p.productName} (${p.productCode})` }))} />
          </Card>
        </Col>
        <Col xs={24} md={16} lg={18}>
          {!recipe ? <Card><Empty description="Chọn một sản phẩm để định nghĩa công thức" /></Card> : (
            <Card
              title={`Công thức: ${recipe.productName}`}
              extra={<Statistic title="Giá vốn / ly" value={fmt(totalCost)} suffix="đ" valueStyle={{ fontSize: 18, color: '#a0522d' }} />}
            >
              <Table rowKey="key" dataSource={lines} columns={columns} pagination={false} size="small"
                     locale={{ emptyText: 'Chưa có nguyên liệu' }} />
              <Space style={{ marginTop: 16 }}>
                <Button icon={<PlusOutlined />} onClick={addLine}>Thêm nguyên liệu</Button>
                <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={save}>Lưu công thức</Button>
              </Space>
            </Card>
          )}
        </Col>
      </Row>
    </>
  )
}
