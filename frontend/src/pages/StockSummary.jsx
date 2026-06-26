import { useState, useEffect } from 'react'
import { Card, Table, Typography, DatePicker, Button, Space, Row, message, Tag, Statistic, Popconfirm } from 'antd'
import { CalculatorOutlined, EyeOutlined, DatabaseOutlined, LockOutlined, UnlockOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title, Text } = Typography

export default function StockSummary() {
  const [month, setMonth] = useState(dayjs())
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [aggregatedAt, setAggregatedAt] = useState(null)
  const [loaded, setLoaded] = useState(false)

  const params = () => ({ year: month.year(), month: month.month() + 1 })

  const [locks, setLocks] = useState([])
  const loadLocks = () => client.get('/api/period-locks').then((r) => setLocks(r.data || [])).catch(() => {})
  useEffect(loadLocks, [])
  const locked = locks.some((l) => l.periodYear === month.year() && l.periodMonth === month.month() + 1)

  const doLock = () => {
    const p = params()
    client.post(`/api/period-locks/lock?year=${p.year}&month=${p.month}`)
      .then(() => { message.success('Đã khóa sổ kỳ này'); loadLocks() })
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi'))
  }
  const doUnlock = () => {
    const p = params()
    client.post(`/api/period-locks/unlock?year=${p.year}&month=${p.month}`)
      .then(() => { message.success('Đã mở khóa kỳ'); loadLocks() })
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi'))
  }

  const view = () => {
    setLoading(true)
    const p = params()
    client.get(`/api/reports/stock-summary?year=${p.year}&month=${p.month}`)
      .then((r) => { setRows(r.data); setLoaded(true); setAggregatedAt(r.data[0]?.createdAt || null) })
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi'))
      .finally(() => setLoading(false))
  }

  const aggregate = () => {
    setLoading(true)
    const p = params()
    client.post(`/api/reports/stock-summary/aggregate?year=${p.year}&month=${p.month}`)
      .then((r) => {
        setRows(r.data); setLoaded(true); setAggregatedAt(r.data[0]?.createdAt || dayjs().toISOString())
        message.success('Đã tổng hợp & lưu vào bảng tồn kho tháng')
      })
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi tổng hợp'))
      .finally(() => setLoading(false))
  }

  const totalValue = rows.reduce((s, r) => s + Number(r.closingValue || 0), 0)

  const columns = [
    { title: 'Mã', dataIndex: 'materialCode', width: 100, fixed: 'left' },
    { title: 'Nguyên vật liệu', dataIndex: 'materialName' },
    { title: 'ĐVT', dataIndex: 'unit', width: 70, align: 'center' },
    { title: 'Tồn đầu', dataIndex: 'openingQty', align: 'right', width: 110, render: fmt },
    { title: 'Nhập', dataIndex: 'receiptQty', align: 'right', width: 100,
      render: (v) => Number(v) > 0 ? <span style={{ color: '#52c41a' }}>+{fmt(v)}</span> : fmt(v) },
    { title: 'Tiêu thụ', dataIndex: 'consumptionQty', align: 'right', width: 100,
      render: (v) => Number(v) > 0 ? <span style={{ color: '#cf1322' }}>-{fmt(v)}</span> : fmt(v) },
    { title: 'Điều chỉnh', dataIndex: 'adjustmentQty', align: 'right', width: 100,
      render: (v) => Number(v) !== 0 ? <span style={{ color: '#1677ff' }}>{Number(v) > 0 ? '+' : ''}{fmt(v)}</span> : fmt(v) },
    { title: 'Tồn cuối', dataIndex: 'closingQty', align: 'right', width: 110, render: (v) => <b>{fmt(v)}</b> },
    { title: 'Đơn giá', dataIndex: 'unitCost', align: 'right', width: 110, render: fmt },
    { title: 'Giá trị tồn cuối', dataIndex: 'closingValue', align: 'right', width: 140,
      render: (v) => <b style={{ color: '#a0522d' }}>{fmt(v)} đ</b> },
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}><DatabaseOutlined /> Tổng hợp tồn kho & giá trị theo tháng</Title>

      <Card style={{ marginBottom: 16 }}>
        <Row justify="space-between" align="middle">
          <Space wrap>
            <span>Tháng:</span>
            <DatePicker picker="month" value={month} onChange={(v) => v && setMonth(v)} format="MM/YYYY" allowClear={false} />
            <Button icon={<EyeOutlined />} loading={loading} onClick={view}>Xem dữ liệu đã lưu</Button>
            <Button type="primary" icon={<CalculatorOutlined />} loading={loading} onClick={aggregate}>
              Tổng hợp &amp; lưu
            </Button>
            {locked
              ? <Popconfirm title="Mở khóa kỳ này? Sẽ cho phép sửa lại giao dịch trong kỳ." onConfirm={doUnlock}>
                  <Button icon={<UnlockOutlined />}>Mở khóa</Button>
                </Popconfirm>
              : <Popconfirm title="Khóa sổ kỳ này? Sẽ chặn thêm/sửa giao dịch trong kỳ." onConfirm={doLock}>
                  <Button danger icon={<LockOutlined />}>Khóa sổ kỳ</Button>
                </Popconfirm>}
            {locked && <Tag color="red" icon={<LockOutlined />}>Đã khóa sổ</Tag>}
          </Space>
          {aggregatedAt && <Text type="secondary">Chốt lúc: {dayjs(aggregatedAt).format('DD/MM/YYYY HH:mm')}</Text>}
        </Row>
      </Card>

      {rows.length > 0 && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Statistic title="Tổng giá trị tồn kho cuối kỳ" value={Math.round(totalValue)}
                     suffix="đ" valueStyle={{ color: '#a0522d' }} style={{ paddingLeft: 8 }} />
        </Row>
      )}

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={rows} columns={columns}
               scroll={{ x: 1100 }} pagination={false} size="small"
               locale={{ emptyText: loaded ? 'Chưa có dữ liệu tổng hợp cho tháng này — bấm "Tổng hợp & lưu"' : 'Chọn tháng rồi bấm Xem hoặc Tổng hợp' }}
               summary={(data) => (
                 <Table.Summary.Row>
                   <Table.Summary.Cell index={0} colSpan={9}><b>Tổng giá trị tồn cuối kỳ</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={1} align="right">
                     <b style={{ color: '#a0522d' }}>{fmt(totalValue)} đ</b>
                   </Table.Summary.Cell>
                 </Table.Summary.Row>
               )} />
      </Card>
    </>
  )
}
