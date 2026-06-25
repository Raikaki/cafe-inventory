import { useState } from 'react'
import { Card, Table, Typography, DatePicker, Button, Space, Row, Col, message, Statistic, Empty } from 'antd'
import { SearchOutlined, ShoppingOutlined } from '@ant-design/icons'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography
const { RangePicker } = DatePicker

export default function ProductSales() {
  const [range, setRange] = useState([dayjs().startOf('month'), dayjs()])
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)

  const query = () => {
    if (!range?.[0] || !range?.[1]) { message.warning('Chọn khoảng ngày'); return }
    setLoading(true)
    const from = range[0].format('YYYY-MM-DD')
    const to = range[1].format('YYYY-MM-DD')
    client.get(`/api/reports/product-sales?from=${from}&to=${to}`)
      .then((r) => setData(r.data))
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi'))
      .finally(() => setLoading(false))
  }

  const columns = [
    { title: 'Mã', dataIndex: 'productCode', width: 120 },
    { title: 'Tên thành phẩm', dataIndex: 'productName' },
    { title: 'Giá bán', dataIndex: 'salePrice', align: 'right', width: 140, render: (v) => fmt(v) + ' đ' },
    { title: 'Số lượng bán', dataIndex: 'quantitySold', align: 'right', width: 140,
      render: (v) => <b>{fmt(v)}</b> },
    { title: 'Doanh thu', dataIndex: 'revenue', align: 'right', width: 160,
      render: (v) => <b style={{ color: '#a0522d' }}>{fmt(v)} đ</b> },
  ]

  const chartData = (data?.rows || []).slice(0, 8).map((r) => ({ name: r.productName, qty: Number(r.quantitySold) }))

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}><ShoppingOutlined /> Thành phẩm đã bán</Title>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <span>Từ ngày → đến ngày:</span>
          <RangePicker value={range} onChange={setRange} format="DD/MM/YYYY" allowClear={false}
                       presets={[
                         { label: 'Hôm nay', value: [dayjs(), dayjs()] },
                         { label: 'Tháng này', value: [dayjs().startOf('month'), dayjs()] },
                         { label: '30 ngày qua', value: [dayjs().subtract(30, 'day'), dayjs()] },
                       ]} />
          <Button type="primary" icon={<SearchOutlined />} loading={loading} onClick={query}>Truy vấn</Button>
        </Space>
      </Card>

      {data && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col xs={12} md={6}>
            <Card><Statistic title="Tổng số lượng bán" value={fmt(data.totalQuantity)} /></Card>
          </Col>
          <Col xs={12} md={8}>
            <Card><Statistic title="Tổng doanh thu" value={fmt(data.totalRevenue)} suffix="đ" valueStyle={{ color: '#a0522d' }} /></Card>
          </Col>
        </Row>
      )}

      {chartData.length > 0 && (
        <Card title="Top thành phẩm bán chạy" style={{ marginBottom: 16 }}>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" fontSize={12} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip />
              <Bar dataKey="qty" name="Số lượng bán" fill="#a0522d" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>
      )}

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="productCode" loading={loading} dataSource={data?.rows || []} columns={columns}
               pagination={false} size="small"
               locale={{ emptyText: data ? <Empty description="Không có đơn bán trong khoảng này" /> : 'Chọn kỳ rồi bấm Truy vấn' }}
               summary={() => data ? (
                 <Table.Summary.Row>
                   <Table.Summary.Cell index={0} colSpan={3}><b>Tổng cộng</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={1} align="right"><b>{fmt(data.totalQuantity)}</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={2} align="right"><b style={{ color: '#a0522d' }}>{fmt(data.totalRevenue)} đ</b></Table.Summary.Cell>
                 </Table.Summary.Row>
               ) : null} />
      </Card>
    </>
  )
}
