import { useEffect, useState } from 'react'
import { Card, Table, Typography, Tag, Progress, Empty } from 'antd'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography

export default function LowStock() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    client.get('/api/inventory/low-stock').then((r) => setData(r.data)).finally(() => setLoading(false))
  }, [])

  const columns = [
    { title: 'Mã', dataIndex: 'materialCode', width: 120 },
    { title: 'Tên nguyên vật liệu', dataIndex: 'materialName' },
    { title: 'ĐVT', dataIndex: 'unit', width: 80, align: 'center' },
    { title: 'Tồn hiện tại', dataIndex: 'currentQty', align: 'right', width: 130,
      render: (v) => <Tag color="red">{fmt(v)}</Tag> },
    { title: 'Định mức tối thiểu', dataIndex: 'minimumQty', align: 'right', width: 150, render: fmt },
    { title: 'Mức độ', width: 200, render: (_, r) => {
      const pct = Number(r.minimumQty) > 0 ? Math.min(100, Math.round(Number(r.currentQty) / Number(r.minimumQty) * 100)) : 0
      return <Progress percent={pct} size="small" status="exception" />
    }},
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}>Cảnh báo tồn kho thấp</Title>
      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={data} columns={columns}
               pagination={false}
               locale={{ emptyText: <Empty description="Không có nguyên vật liệu nào dưới định mức 🎉" /> }} />
      </Card>
    </>
  )
}
