import { useState } from 'react'
import { Card, Table, Typography, DatePicker, Button, Space, message } from 'antd'
import { SearchOutlined, BankOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography
const { RangePicker } = DatePicker

export default function TrialBalance() {
  const [range, setRange] = useState([dayjs().startOf('month'), dayjs()])
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)

  const query = () => {
    if (!range?.[0] || !range?.[1]) { message.warning('Chọn khoảng ngày'); return }
    setLoading(true)
    const from = range[0].format('YYYY-MM-DD')
    const to = range[1].format('YYYY-MM-DD')
    client.get(`/api/reports/trial-balance?from=${from}&to=${to}`)
      .then((r) => setRows(r.data.rows))
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi'))
      .finally(() => setLoading(false))
  }

  const num = (v) => Number(v) !== 0 ? fmt(v) : ''
  const sum = (k) => rows.reduce((s, r) => s + Number(r[k] || 0), 0)

  const columns = [
    { title: 'Số hiệu TK', dataIndex: 'accountCode', width: 110, fixed: 'left' },
    { title: 'Tên tài khoản', dataIndex: 'accountName', width: 220 },
    { title: 'Dư đầu kỳ', children: [
      { title: 'Nợ', dataIndex: 'openingDebit', align: 'right', width: 130, render: num },
      { title: 'Có', dataIndex: 'openingCredit', align: 'right', width: 130, render: num },
    ]},
    { title: 'Phát sinh trong kỳ', children: [
      { title: 'Nợ', dataIndex: 'periodDebit', align: 'right', width: 130, render: (v) => <span style={{ color: '#16a34a' }}>{num(v)}</span> },
      { title: 'Có', dataIndex: 'periodCredit', align: 'right', width: 130, render: (v) => <span style={{ color: '#dc2626' }}>{num(v)}</span> },
    ]},
    { title: 'Dư cuối kỳ', children: [
      { title: 'Nợ', dataIndex: 'closingDebit', align: 'right', width: 130, render: (v) => <b>{num(v)}</b> },
      { title: 'Có', dataIndex: 'closingCredit', align: 'right', width: 130, render: (v) => <b>{num(v)}</b> },
    ]},
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}><BankOutlined /> Bảng cân đối số phát sinh</Title>
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <span>Từ ngày → đến ngày:</span>
          <RangePicker value={range} onChange={setRange} format="DD/MM/YYYY" allowClear={false}
                       presets={[
                         { label: 'Tháng này', value: [dayjs().startOf('month'), dayjs()] },
                         { label: 'Năm nay', value: [dayjs().startOf('year'), dayjs()] },
                       ]} />
          <Button type="primary" icon={<SearchOutlined />} loading={loading} onClick={query}>Truy vấn</Button>
        </Space>
      </Card>
      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="accountCode" loading={loading} dataSource={rows} columns={columns}
               bordered size="small" pagination={false} scroll={{ x: 1100 }}
               locale={{ emptyText: 'Chọn kỳ rồi bấm Truy vấn' }}
               summary={() => rows.length ? (
                 <Table.Summary.Row>
                   <Table.Summary.Cell index={0} colSpan={2}><b>TỔNG CỘNG</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={2} align="right"><b>{fmt(sum('openingDebit'))}</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={3} align="right"><b>{fmt(sum('openingCredit'))}</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={4} align="right"><b>{fmt(sum('periodDebit'))}</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={5} align="right"><b>{fmt(sum('periodCredit'))}</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={6} align="right"><b>{fmt(sum('closingDebit'))}</b></Table.Summary.Cell>
                   <Table.Summary.Cell index={7} align="right"><b>{fmt(sum('closingCredit'))}</b></Table.Summary.Cell>
                 </Table.Summary.Row>
               ) : null} />
      </Card>
    </>
  )
}
