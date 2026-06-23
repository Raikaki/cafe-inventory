import { useEffect, useState } from 'react'
import { Card, Row, Col, Typography, Table, Button, DatePicker, Space, Tag, message, Alert } from 'antd'
import { ReloadOutlined, QrcodeOutlined, EnvironmentOutlined } from '@ant-design/icons'
import { QRCodeSVG } from 'qrcode.react'
import dayjs from 'dayjs'
import client from '../api/client'

const { Title, Text, Paragraph } = Typography

export default function AttendanceAdmin() {
  const [qr, setQr] = useState(null)
  const [date, setDate] = useState(dayjs())
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(false)

  const loadQr = () => client.get('/api/attendance/qr/today').then((r) => setQr(r.data))
  const loadLogs = () => {
    setLoading(true)
    client.get(`/api/attendance/logs?date=${date.format('YYYY-MM-DD')}`)
      .then((r) => setLogs(r.data)).finally(() => setLoading(false))
  }
  useEffect(() => { loadQr() }, [])
  useEffect(() => { loadLogs() }, [date])

  const checkinUrl = qr ? `${window.location.origin}${qr.checkinPath}` : ''

  const columns = [
    { title: 'Nhân viên', dataIndex: 'employeeName' },
    { title: 'Thời gian', dataIndex: 'scanTime', width: 170,
      render: (v) => dayjs(v).format('DD/MM/YYYY HH:mm:ss') },
    { title: 'IP thiết bị', dataIndex: 'ipAddress', width: 150 },
    { title: 'Vị trí', width: 160, render: (_, r) => (r.latitude != null && r.longitude != null)
        ? <a href={`https://maps.google.com/?q=${r.latitude},${r.longitude}`} target="_blank" rel="noreferrer">
            <EnvironmentOutlined /> {Number(r.latitude).toFixed(5)}, {Number(r.longitude).toFixed(5)}</a>
        : <Tag>Không có</Tag> },
  ]

  return (
    <>
      <Title level={3} style={{ marginTop: 0 }}><QrcodeOutlined /> Chấm công bằng QR</Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card title="Mã QR hôm nay" extra={<Button size="small" icon={<ReloadOutlined />} onClick={loadQr} />}
                style={{ textAlign: 'center' }}>
            {qr && (
              <>
                <div style={{ background: '#fff', padding: 16, display: 'inline-block', borderRadius: 8 }}>
                  <QRCodeSVG value={checkinUrl} size={220} level="M" />
                </div>
                <Paragraph type="secondary" style={{ marginTop: 12, marginBottom: 4 }}>
                  Ngày {dayjs(qr.date).format('DD/MM/YYYY')} — nhân viên quét mã để chấm công
                </Paragraph>
                <Text copyable style={{ fontSize: 12 }}>{checkinUrl}</Text>
                <Alert style={{ marginTop: 12, textAlign: 'left' }} type="info" showIcon
                       message="Mã đổi mới mỗi ngày; mã của ngày hôm trước sẽ không dùng được." />
              </>
            )}
          </Card>
        </Col>

        <Col xs={24} lg={16}>
          <Card title="Lịch sử chấm công"
                extra={<Space>
                  <DatePicker value={date} onChange={(v) => v && setDate(v)} format="DD/MM/YYYY" allowClear={false} />
                  <Button icon={<ReloadOutlined />} onClick={loadLogs} />
                </Space>}
                bodyStyle={{ padding: 0 }}>
            <Table rowKey="id" loading={loading} dataSource={logs} columns={columns}
                   scroll={{ x: 700 }} pagination={{ pageSize: 10 }} size="small"
                   locale={{ emptyText: 'Chưa có ai chấm công ngày này' }} />
          </Card>
        </Col>
      </Row>
    </>
  )
}
