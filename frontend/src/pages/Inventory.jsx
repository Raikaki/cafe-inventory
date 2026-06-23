import { useEffect, useState } from 'react'
import { Card, Table, Typography, Tag, Select, Space } from 'antd'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography

const typeMeta = {
  RECEIPT: { color: 'green', label: 'Nhập kho' },
  SALE_CONSUMPTION: { color: 'volcano', label: 'Bán hàng' },
  ADJUSTMENT: { color: 'blue', label: 'Điều chỉnh' },
  STOCK_COUNT: { color: 'purple', label: 'Kiểm kê' },
}

export default function Inventory() {
  const [txns, setTxns] = useState([])
  const [matMap, setMatMap] = useState({})
  const [materials, setMaterials] = useState([])
  const [filterMat, setFilterMat] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    client.get('/api/materials').then((r) => {
      setMaterials(r.data)
      setMatMap(Object.fromEntries(r.data.map((m) => [m.id, m])))
    })
    load(null)
  }, [])

  const load = (matId) => {
    setLoading(true)
    const url = matId ? `/api/inventory/transactions/material/${matId}` : '/api/inventory/transactions'
    client.get(url).then((r) => setTxns(r.data)).finally(() => setLoading(false))
  }

  const onFilter = (v) => { setFilterMat(v); load(v) }

  const columns = [
    { title: 'Thời gian', dataIndex: 'txnDate', width: 160, render: (v) => dayjs(v).format('DD/MM/YYYY HH:mm') },
    { title: 'Loại', dataIndex: 'txnType', width: 120,
      render: (v) => <Tag color={typeMeta[v]?.color}>{typeMeta[v]?.label || v}</Tag> },
    { title: 'Chứng từ', dataIndex: 'referenceNo', width: 150 },
    { title: 'Nguyên vật liệu', dataIndex: 'materialId',
      render: (v) => matMap[v] ? `${matMap[v].materialName} (${matMap[v].materialCode})` : v },
    { title: 'Biến động', dataIndex: 'quantity', align: 'right', width: 120,
      render: (v) => <b style={{ color: Number(v) < 0 ? '#cf1322' : '#52c41a' }}>{Number(v) > 0 ? '+' : ''}{fmt(v)}</b> },
    { title: 'Tồn trước', dataIndex: 'beforeQty', align: 'right', width: 110, render: fmt },
    { title: 'Tồn sau', dataIndex: 'afterQty', align: 'right', width: 110, render: (v) => <b>{fmt(v)}</b> },
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}>Sổ kho — Lịch sử biến động</Title>
      <Card bodyStyle={{ paddingTop: 16 }}>
        <Space style={{ marginBottom: 16 }}>
          <span>Lọc theo nguyên vật liệu:</span>
          <Select allowClear showSearch optionFilterProp="label" style={{ width: 280 }} placeholder="Tất cả"
                  value={filterMat} onChange={onFilter}
                  options={materials.map((m) => ({ value: m.id, label: `${m.materialName} (${m.materialCode})` }))} />
        </Space>
        <Table rowKey="id" loading={loading} dataSource={txns} columns={columns}
               scroll={{ x: 900 }} pagination={{ pageSize: 15 }} />
      </Card>
    </>
  )
}
