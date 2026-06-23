import { useState } from 'react'
import { Card, Table, Typography, DatePicker, Button, Space, Row, message, Tag } from 'antd'
import { SearchOutlined, FileTextOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography
const { RangePicker } = DatePicker

export default function InventoryBalance() {
  const [range, setRange] = useState([dayjs().startOf('month'), dayjs()])
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [queried, setQueried] = useState(false)

  const query = () => {
    if (!range || !range[0] || !range[1]) { message.warning('Chọn khoảng ngày'); return }
    setLoading(true)
    const from = range[0].format('YYYY-MM-DD')
    const to = range[1].format('YYYY-MM-DD')
    client.get(`/api/reports/inventory-balance?from=${from}&to=${to}`)
      .then((r) => { setRows(r.data.rows); setQueried(true) })
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi truy vấn'))
      .finally(() => setLoading(false))
  }

  const columns = [
    { title: 'Mã', dataIndex: 'materialCode', width: 100, fixed: 'left' },
    { title: 'Nguyên vật liệu', dataIndex: 'materialName' },
    { title: 'ĐVT', dataIndex: 'unit', width: 70, align: 'center' },
    { title: 'Tồn đầu kỳ', dataIndex: 'openingQty', align: 'right', width: 120, render: fmt },
    { title: 'Nhập', dataIndex: 'receiptQty', align: 'right', width: 110,
      render: (v) => Number(v) > 0 ? <span style={{ color: '#52c41a' }}>+{fmt(v)}</span> : fmt(v) },
    { title: 'Tiêu thụ', dataIndex: 'consumptionQty', align: 'right', width: 110,
      render: (v) => Number(v) > 0 ? <span style={{ color: '#cf1322' }}>-{fmt(v)}</span> : fmt(v) },
    { title: 'Điều chỉnh', dataIndex: 'adjustmentQty', align: 'right', width: 110,
      render: (v) => Number(v) !== 0 ? <span style={{ color: '#1677ff' }}>{Number(v) > 0 ? '+' : ''}{fmt(v)}</span> : fmt(v) },
    { title: 'Tồn cuối kỳ', dataIndex: 'closingQty', align: 'right', width: 130,
      render: (v) => <b>{fmt(v)}</b> },
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}><FileTextOutlined /> Tổng hợp tồn kho theo kỳ</Title>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <span>Từ ngày → đến ngày:</span>
          <RangePicker value={range} onChange={setRange} format="DD/MM/YYYY" allowClear={false}
                       presets={[
                         { label: 'Tháng này', value: [dayjs().startOf('month'), dayjs()] },
                         { label: '30 ngày qua', value: [dayjs().subtract(30, 'day'), dayjs()] },
                         { label: 'Năm nay', value: [dayjs().startOf('year'), dayjs()] },
                       ]} />
          <Button type="primary" icon={<SearchOutlined />} loading={loading} onClick={query}>Truy vấn</Button>
        </Space>
      </Card>

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="materialId" loading={loading} dataSource={rows} columns={columns}
               scroll={{ x: 920 }} pagination={false} size="small"
               locale={{ emptyText: queried ? 'Không có dữ liệu' : 'Chọn kỳ và bấm Truy vấn' }}
               rowClassName={(r) => (Number(r.receiptQty) > 0 || Number(r.consumptionQty) > 0 || Number(r.adjustmentQty) !== 0) ? '' : 'row-idle'} />
      </Card>
    </>
  )
}
