import { useEffect, useState } from 'react'
import { Card, Table, Typography, Tag, Select, Space, Row, Col, Spin, Button } from 'antd'
import { RobotOutlined, ReloadOutlined, ThunderboltOutlined } from '@ant-design/icons'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title, Paragraph, Text } = Typography

const statusMeta = {
  CRITICAL: { color: 'red', label: 'Khẩn cấp' },
  WARNING: { color: 'orange', label: 'Cảnh báo' },
  OK: { color: 'green', label: 'An toàn' },
  IDLE: { color: 'default', label: 'Ít dùng' },
}

export default function Forecast() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [lookback, setLookback] = useState(30)
  const [horizon, setHorizon] = useState(14)

  const load = () => {
    setLoading(true)
    client.get(`/api/forecast?lookbackDays=${lookback}&horizonDays=${horizon}&ai=true`)
      .then((r) => setData(r.data)).finally(() => setLoading(false))
  }
  useEffect(load, [lookback, horizon])

  const columns = [
    { title: 'Mã', dataIndex: 'materialCode', width: 100 },
    { title: 'Nguyên vật liệu', dataIndex: 'materialName' },
    { title: 'Tồn hiện tại', dataIndex: 'currentQty', align: 'right', width: 120,
      render: (v, r) => `${fmt(v)} ${r.unit}` },
    { title: 'Dùng TB/ngày', dataIndex: 'avgDailyUsage', align: 'right', width: 120, render: fmt },
    { title: 'Hết sau (ngày)', dataIndex: 'daysToStockout', align: 'right', width: 130,
      render: (v) => v == null ? <Text type="secondary">∞</Text>
        : <b style={{ color: v <= 7 ? '#cf1322' : v <= 14 ? '#fa8c16' : '#52c41a' }}>{v}</b> },
    { title: `Dự báo dùng (${data?.horizonDays || 0}n)`, dataIndex: 'projectedUsage', align: 'right', width: 130, render: fmt },
    { title: 'Đề xuất nhập', dataIndex: 'recommendedReorderQty', align: 'right', width: 130,
      render: (v, r) => Number(v) > 0 ? <Tag color="blue">{fmt(v)} {r.unit}</Tag> : <Text type="secondary">—</Text> },
    { title: 'Trạng thái', dataIndex: 'status', width: 110, align: 'center',
      render: (v) => <Tag color={statusMeta[v]?.color}>{statusMeta[v]?.label || v}</Tag> },
  ]

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}><ThunderboltOutlined /> Dự báo tồn kho (AI)</Title>
        <Space>
          <span>Lịch sử:</span>
          <Select value={lookback} onChange={setLookback} style={{ width: 110 }}
                  options={[{ value: 7, label: '7 ngày' }, { value: 30, label: '30 ngày' }, { value: 90, label: '90 ngày' }]} />
          <span>Tầm nhìn:</span>
          <Select value={horizon} onChange={setHorizon} style={{ width: 110 }}
                  options={[{ value: 7, label: '7 ngày' }, { value: 14, label: '14 ngày' }, { value: 30, label: '30 ngày' }]} />
          <Button icon={<ReloadOutlined />} onClick={load}>Làm mới</Button>
        </Space>
      </Row>

      {loading || !data ? <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div> : (
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={8}>
            <Card title={<span><RobotOutlined /> Khuyến nghị từ AI</span>}
                  extra={<Tag color={data.aiEnabled ? 'purple' : 'default'}>{data.aiEnabled ? 'Gemini AI' : 'Rule-based'}</Tag>}
                  style={{ height: '100%' }}>
              <Paragraph style={{ whiteSpace: 'pre-wrap', marginBottom: 0 }}>{data.aiAdvice}</Paragraph>
            </Card>
          </Col>
          <Col xs={24} lg={16}>
            <Card title="Bảng dự báo theo nguyên vật liệu" bodyStyle={{ padding: 0 }}>
              <Table rowKey="materialId" dataSource={data.items} columns={columns}
                     scroll={{ x: 900 }} pagination={{ pageSize: 12 }} size="small"
                     rowClassName={(r) => r.status === 'CRITICAL' ? 'row-critical' : ''} />
            </Card>
          </Col>
        </Row>
      )}
    </>
  )
}
