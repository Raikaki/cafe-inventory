import { useState } from 'react'
import {
  Card, Table, Typography, DatePicker, Select, Button, Space, Row, Col, message, Tag,
  Modal, Form, Input, InputNumber, Descriptions,
} from 'antd'
import { SearchOutlined, PlusOutlined, EyeOutlined, FileTextOutlined, PrinterOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import client from '../api/client'
import { fmt } from '../utils/format'

const { Title } = Typography
const { RangePicker } = DatePicker

const TYPES = [
  { value: 'PHIEU_THU', label: 'Phiếu thu', color: 'green' },
  { value: 'PHIEU_CHI', label: 'Phiếu chi', color: 'red' },
  { value: 'PHIEU_NHAP_KHO', label: 'Phiếu nhập kho', color: 'blue' },
  { value: 'PHIEU_XUAT_KHO', label: 'Phiếu xuất kho', color: 'volcano' },
  { value: 'HOA_DON', label: 'Hóa đơn', color: 'purple' },
  { value: 'KHAC', label: 'Khác', color: 'default' },
]
const typeMeta = Object.fromEntries(TYPES.map((t) => [t.value, t]))

export default function Vouchers() {
  const [range, setRange] = useState([dayjs().startOf('month'), dayjs()])
  const [type, setType] = useState(null)
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)

  const [open, setOpen] = useState(false)
  const [form] = Form.useForm()
  const [detail, setDetail] = useState(null)

  const query = () => {
    if (!range?.[0] || !range?.[1]) { message.warning('Chọn khoảng ngày'); return }
    setLoading(true)
    const from = range[0].format('YYYY-MM-DD')
    const to = range[1].format('YYYY-MM-DD')
    const q = type ? `&type=${type}` : ''
    client.get(`/api/vouchers?from=${from}&to=${to}${q}`)
      .then((r) => setRows(r.data))
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi'))
      .finally(() => setLoading(false))
  }

  const openNew = () => { form.resetFields(); form.setFieldsValue({ voucherDate: dayjs(), voucherType: 'PHIEU_THU' }); setOpen(true) }

  const submit = async () => {
    const v = await form.validateFields()
    const payload = { ...v, voucherDate: v.voucherDate.format('YYYY-MM-DD') }
    try {
      await client.post('/api/vouchers', payload)
      message.success('Đã tạo chứng từ')
      setOpen(false); query()
    } catch (e) { message.error(e.response?.data?.message || 'Lỗi khi lưu') }
  }

  const printVoucher = (v) => {
    const formNo = { PHIEU_THU: 'Mẫu số 01 - TT', PHIEU_CHI: 'Mẫu số 02 - TT',
      PHIEU_NHAP_KHO: 'Mẫu số 01 - VT', PHIEU_XUAT_KHO: 'Mẫu số 02 - VT' }[v.voucherType] || ''
    const title = (typeMeta[v.voucherType]?.label || 'Chứng từ').toUpperCase()
    const personLabel = { PHIEU_THU: 'Họ và tên người nộp tiền', PHIEU_CHI: 'Họ và tên người nhận tiền',
      PHIEU_NHAP_KHO: 'Họ và tên người giao hàng', PHIEU_XUAT_KHO: 'Họ và tên người nhận hàng' }[v.voucherType] || 'Đối tác'
    const d = dayjs(v.voucherDate)
    const money = Number(v.amount || 0).toLocaleString('vi-VN')
    const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>${v.voucherNo}</title>
      <style>
        body{font-family:'Times New Roman',serif;font-size:14px;color:#000;padding:24px;max-width:720px;margin:auto}
        .hdr{display:flex;justify-content:space-between;font-size:13px}
        .ttl{text-align:center;margin:14px 0 2px}.ttl h2{margin:0;font-size:20px;letter-spacing:1px}
        .meta{text-align:center;font-style:italic;font-size:13px}
        .no{text-align:center;margin:6px 0 14px}
        .row{margin:6px 0}.dots{border-bottom:1px dotted #000}
        .signs{display:flex;justify-content:space-between;margin-top:36px;text-align:center;font-size:13px}
        .signs div{width:19%}.signs i{font-size:12px}
        @media print{button{display:none}}
      </style></head><body>
      <div class="hdr">
        <div><b>QUÁN CAFE</b><br/>Địa chỉ: ............................</div>
        <div style="text-align:right"><i>${formNo}</i><br/><i>(Ban hành theo TT 88/2021/TT-BTC)</i></div>
      </div>
      <div class="ttl"><h2>${title}</h2></div>
      <div class="meta">Ngày ${d.format('DD')} tháng ${d.format('MM')} năm ${d.format('YYYY')}</div>
      <div class="no">Số: <b>${v.voucherNo}</b></div>
      <div class="row">${personLabel}: <span class="dots">${v.partnerName || '.....................................'}</span></div>
      <div class="row">Địa chỉ: <span class="dots">${v.partnerAddress || '.....................................'}</span></div>
      <div class="row">Nội dung: <span class="dots">${v.content || '.....................................'}</span></div>
      <div class="row">Số tiền: <b>${money} đ</b></div>
      <div class="row">Viết bằng chữ: <i>${v.amountInWords || ''}</i></div>
      <div class="row">Kèm theo: ............ chứng từ gốc.</div>
      <div class="signs">
        <div><b>Giám đốc</b><br/><i>(Ký, họ tên)</i></div>
        <div><b>Kế toán trưởng</b><br/><i>(Ký, họ tên)</i></div>
        <div><b>Người lập phiếu</b><br/><i>(Ký, họ tên)</i><br/><br/>${v.createdBy || ''}</div>
        <div><b>Thủ quỹ</b><br/><i>(Ký, họ tên)</i></div>
        <div><b>Người ${v.voucherType === 'PHIEU_CHI' ? 'nhận' : 'nộp'} tiền</b><br/><i>(Ký, họ tên)</i></div>
      </div>
      <button onclick="window.print()" style="margin-top:24px;padding:8px 16px">In</button>
      </body></html>`
    const w = window.open('', '_blank')
    w.document.write(html); w.document.close(); w.focus()
    setTimeout(() => w.print(), 300)
  }

  const columns = [
    { title: 'Số CT', dataIndex: 'voucherNo', width: 130, fixed: 'left' },
    { title: 'Ngày', dataIndex: 'voucherDate', width: 110, render: (v) => dayjs(v).format('DD/MM/YYYY') },
    { title: 'Loại', dataIndex: 'voucherType', width: 140,
      render: (v) => <Tag color={typeMeta[v]?.color}>{typeMeta[v]?.label || v}</Tag> },
    { title: 'Đối tác', dataIndex: 'partnerName' },
    { title: 'Nội dung', dataIndex: 'content', ellipsis: true },
    { title: 'Số tiền', dataIndex: 'amount', align: 'right', width: 150,
      render: (v) => <b style={{ color: '#7C3AED' }}>{fmt(v)} đ</b> },
    { title: 'Người lập', dataIndex: 'creatorName', width: 140 },
    { title: '', width: 60, fixed: 'right', render: (_, r) => (
      <Button size="small" icon={<EyeOutlined />} onClick={() => setDetail(r)} />
    )},
  ]

  return (
    <>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}><FileTextOutlined /> Chứng từ kế toán</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openNew}>Lập chứng từ</Button>
      </Row>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <span>Từ ngày → đến ngày:</span>
          <RangePicker value={range} onChange={setRange} format="DD/MM/YYYY" allowClear={false}
                       presets={[
                         { label: 'Tháng này', value: [dayjs().startOf('month'), dayjs()] },
                         { label: '30 ngày qua', value: [dayjs().subtract(30, 'day'), dayjs()] },
                         { label: 'Năm nay', value: [dayjs().startOf('year'), dayjs()] },
                       ]} />
          <span>Loại:</span>
          <Select allowClear placeholder="Tất cả" style={{ width: 180 }} value={type} onChange={setType}
                  options={TYPES.map((t) => ({ value: t.value, label: t.label }))} />
          <Button type="primary" icon={<SearchOutlined />} loading={loading} onClick={query}>Truy vấn</Button>
        </Space>
      </Card>

      <Card bodyStyle={{ padding: 0 }}>
        <Table rowKey="id" loading={loading} dataSource={rows} columns={columns}
               scroll={{ x: 1000 }} pagination={{ pageSize: 15 }} size="small"
               locale={{ emptyText: 'Chọn kỳ rồi bấm Truy vấn' }} />
      </Card>

      {/* Create */}
      <Modal title="Lập chứng từ kế toán" open={open} onOk={submit} onCancel={() => setOpen(false)}
             okText="Lưu" cancelText="Huỷ" width={640} destroyOnClose>
        <Form form={form} layout="vertical">
          <Row gutter={12}>
            <Col span={12}><Form.Item name="voucherType" label="Loại chứng từ" rules={[{ required: true }]}>
              <Select options={TYPES.map((t) => ({ value: t.value, label: t.label }))} /></Form.Item></Col>
            <Col span={12}><Form.Item name="voucherDate" label="Ngày chứng từ" rules={[{ required: true }]}>
              <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" /></Form.Item></Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}><Form.Item name="creatorName" label="Người/đơn vị lập"><Input /></Form.Item></Col>
            <Col span={12}><Form.Item name="partnerName" label="Người/đơn vị nộp/nhận"><Input /></Form.Item></Col>
          </Row>
          <Form.Item name="content" label="Nội dung"><Input.TextArea rows={2} /></Form.Item>
          <Row gutter={12}>
            <Col span={8}><Form.Item name="quantity" label="Số lượng"><InputNumber style={{ width: '100%' }} min={0} /></Form.Item></Col>
            <Col span={8}><Form.Item name="unit" label="Đơn vị"><Input /></Form.Item></Col>
            <Col span={8}><Form.Item name="amount" label="Số tiền (đ)" rules={[{ required: true }]}>
              <InputNumber style={{ width: '100%' }} min={0}
                           formatter={(x) => `${x}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                           parser={(x) => `${x}`.replace(/,/g, '')} /></Form.Item></Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}><Form.Item name="approverName" label="Người duyệt"><Input /></Form.Item></Col>
            <Col span={12}><Form.Item name="note" label="Ghi chú"><Input /></Form.Item></Col>
          </Row>
          <span style={{ color: '#888', fontSize: 12 }}>Số chứng từ và "số tiền bằng chữ" sẽ được tạo tự động.</span>
        </Form>
      </Modal>

      {/* Detail */}
      <Modal title={detail ? `Chứng từ ${detail.voucherNo}` : ''} open={!!detail} width={640}
             onCancel={() => setDetail(null)}
             footer={detail ? [
               <Button key="print" type="primary" icon={<PrinterOutlined />} onClick={() => printVoucher(detail)}>In phiếu</Button>,
               <Button key="close" onClick={() => setDetail(null)}>Đóng</Button>,
             ] : null}>
        {detail && (
          <Descriptions bordered size="small" column={1}>
            <Descriptions.Item label="Loại"><Tag color={typeMeta[detail.voucherType]?.color}>{typeMeta[detail.voucherType]?.label}</Tag></Descriptions.Item>
            <Descriptions.Item label="Ngày">{dayjs(detail.voucherDate).format('DD/MM/YYYY')}</Descriptions.Item>
            <Descriptions.Item label="Người/ĐV lập">{detail.creatorName || '—'}</Descriptions.Item>
            <Descriptions.Item label="Người/ĐV nộp/nhận">{detail.partnerName || '—'}</Descriptions.Item>
            <Descriptions.Item label="Nội dung">{detail.content || '—'}</Descriptions.Item>
            <Descriptions.Item label="Số lượng / ĐV">{detail.quantity ? `${fmt(detail.quantity)} ${detail.unit || ''}` : '—'}</Descriptions.Item>
            <Descriptions.Item label="Số tiền"><b style={{ color: '#7C3AED' }}>{fmt(detail.amount)} đ</b></Descriptions.Item>
            <Descriptions.Item label="Bằng chữ"><i>{detail.amountInWords}</i></Descriptions.Item>
            <Descriptions.Item label="Người duyệt">{detail.approverName || '—'}</Descriptions.Item>
            <Descriptions.Item label="Ghi chú">{detail.note || '—'}</Descriptions.Item>
            <Descriptions.Item label="Người tạo">{detail.createdBy}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </>
  )
}
