import { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Table, Spin, Typography, Tag, Empty } from 'antd'
import {
  CoffeeOutlined, InboxOutlined, DollarOutlined, ShoppingOutlined, WarningOutlined,
} from '@ant-design/icons'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography

const kpiStyle = (from, to) => ({ background: `linear-gradient(135deg, ${from}, ${to})`, border: 'none' })

export default function Dashboard() {
  const [data, setData] = useState(null)

  useEffect(() => {
    client.get('/api/dashboard').then((res) => setData(res.data))
  }, [])

  if (!data) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>

  const chartData = (data.topProducts || []).map((p) => ({ name: p.productName, qty: Number(p.quantity) }))

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}>Dashboard</Title>

      <Row gutter={[16, 16]}>
        <Col xs={12} md={8} lg={4}>
          <Card className="kpi-card" style={kpiStyle('#1677ff', '#4096ff')}>
            <Statistic title="Sản phẩm" value={data.totalProducts} prefix={<CoffeeOutlined />} />
          </Card>
        </Col>
        <Col xs={12} md={8} lg={5}>
          <Card className="kpi-card" style={kpiStyle('#13c2c2', '#36cfc9')}>
            <Statistic title="Nguyên vật liệu" value={data.totalMaterials} prefix={<InboxOutlined />} />
          </Card>
        </Col>
        <Col xs={12} md={8} lg={5}>
          <Card className="kpi-card" style={kpiStyle('#52c41a', '#73d13d')}>
            <Statistic title="Giá trị tồn kho" value={fmt(data.inventoryValue)} prefix={<DollarOutlined />} suffix="đ" />
          </Card>
        </Col>
        <Col xs={12} md={12} lg={6}>
          <Card className="kpi-card" style={kpiStyle('#fa8c16', '#ffa940')}>
            <Statistic title="Doanh thu hôm nay" value={fmt(data.todaySales)} prefix={<ShoppingOutlined />} suffix="đ" />
          </Card>
        </Col>
        <Col xs={24} md={12} lg={4}>
          <Card className="kpi-card" style={kpiStyle('#cf1322', '#ff4d4f')}>
            <Statistic title="Sắp hết hàng" value={data.lowStockCount} prefix={<WarningOutlined />} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={14}>
          <Card title="Top sản phẩm bán chạy">
            {chartData.length === 0 ? <Empty description="Chưa có dữ liệu bán hàng" /> : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 8 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="name" fontSize={12} />
                  <YAxis allowDecimals={false} fontSize={12} />
                  <Tooltip />
                  <Bar dataKey="qty" name="Số lượng bán" fill="#7C3AED" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title={<span><WarningOutlined style={{ color: '#cf1322' }} /> Nguyên vật liệu dưới định mức</span>}>
            <Table
              size="small" rowKey="id" pagination={false}
              dataSource={data.lowStockMaterials}
              locale={{ emptyText: <Empty description="Không có vật liệu thiếu" /> }}
              columns={[
                { title: 'Mã', dataIndex: 'materialCode', width: 90 },
                { title: 'Tên', dataIndex: 'materialName' },
                { title: 'Tồn', dataIndex: 'currentQty', align: 'right',
                  render: (v) => <Tag color="red">{fmt(v)}</Tag> },
                { title: 'Tối thiểu', dataIndex: 'minimumQty', align: 'right', render: fmt },
              ]}
            />
          </Card>
        </Col>
      </Row>
    </>
  )
}
