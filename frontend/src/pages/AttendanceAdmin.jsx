import { useEffect, useState } from 'react'
import { Card, Row, Col, Typography, Table, Button, DatePicker, Space, Tag, message, Alert, InputNumber, Switch } from 'antd'
import { ReloadOutlined, QrcodeOutlined, EnvironmentOutlined, AimOutlined, SaveOutlined } from '@ant-design/icons'
import { QRCodeSVG } from 'qrcode.react'
import dayjs from 'dayjs'
import client from '../api/client'

const { Title, Text, Paragraph } = Typography

export default function AttendanceAdmin() {
  const [qr, setQr] = useState(null)
  const [date, setDate] = useState(dayjs())
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(false)

  const [loc, setLoc] = useState({ latitude: null, longitude: null, radiusMeters: 200, activeFlag: false })
  const [savingLoc, setSavingLoc] = useState(false)

  const loadQr = () => client.get('/api/attendance/qr/today').then((r) => setQr(r.data))
  const loadLoc = () => client.get('/api/attendance/location').then((r) => r.data && setLoc({
    latitude: r.data.latitude, longitude: r.data.longitude,
    radiusMeters: r.data.radiusMeters ?? 200, activeFlag: !!r.data.activeFlag,
  }))

  const useCurrentPosition = () => {
    if (!navigator.geolocation) { message.error('Trình duyệt không hỗ trợ định vị'); return }
    navigator.geolocation.getCurrentPosition(
      (pos) => { setLoc((p) => ({ ...p, latitude: pos.coords.latitude, longitude: pos.coords.longitude })); message.success('Đã lấy vị trí hiện tại') },
      () => message.error('Không lấy được vị trí (bạn cần cho phép định vị)'),
      { enableHighAccuracy: true, timeout: 8000 }
    )
  }

  const saveLoc = () => {
    setSavingLoc(true)
    client.put('/api/attendance/location', loc)
      .then(() => message.success('Đã lưu cài đặt vị trí quán'))
      .catch((e) => message.error(e.response?.data?.message || 'Lỗi lưu'))
      .finally(() => setSavingLoc(false))
  }
  const loadLogs = () => {
    setLoading(true)
    client.get(`/api/attendance/logs?date=${date.format('YYYY-MM-DD')}`)
      .then((r) => setLogs(r.data)).finally(() => setLoading(false))
  }
  useEffect(() => { loadQr(); loadLoc() }, [])
  useEffect(() => { loadLogs() }, [date])

  const checkinUrl = qr ? `${window.location.origin}${qr.checkinPath}` : ''

  // Group logs per employee -> giờ vào = lần quét sớm nhất (min), giờ ra = muộn nhất (max)
  const summary = Object.values(logs.reduce((acc, l) => {
    const k = l.employeeName.toLowerCase()
    if (!acc[k]) acc[k] = { key: k, employeeName: l.employeeName, min: l.scanTime, max: l.scanTime, count: 0 }
    if (dayjs(l.scanTime).isBefore(acc[k].min)) acc[k].min = l.scanTime
    if (dayjs(l.scanTime).isAfter(acc[k].max)) acc[k].max = l.scanTime
    acc[k].count++
    return acc
  }, {})).map((r) => ({
    key: r.key,
    employeeName: r.employeeName,
    scans: r.count,
    vao: r.min,
    ra: r.count >= 2 ? r.max : null,
    hours: r.count >= 2 ? (dayjs(r.max).diff(dayjs(r.min), 'minute') / 60).toFixed(2) : null,
  }))

  const summaryColumns = [
    { title: 'Nhân viên', dataIndex: 'employeeName' },
    { title: 'Số lần quét', dataIndex: 'scans', width: 100, align: 'center' },
    { title: 'Giờ vào', dataIndex: 'vao', width: 110, render: (v) => v ? dayjs(v).format('HH:mm:ss') : '—' },
    { title: 'Giờ ra', dataIndex: 'ra', width: 110, render: (v) => v ? dayjs(v).format('HH:mm:ss') : <Tag color="orange">Chưa ra</Tag> },
    { title: 'Số giờ làm', dataIndex: 'hours', width: 110, align: 'right',
      render: (v) => v ? <b>{v} h</b> : '—' },
  ]

  const columns = [
    { title: 'Nhân viên', dataIndex: 'employeeName' },
    { title: 'Loại', dataIndex: 'checkType', width: 90, align: 'center',
      render: (v) => v === 'RA' ? <Tag color="blue">Giờ ra</Tag> : <Tag color="green">Giờ vào</Tag> },
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

          <Card title={<span><AimOutlined /> Vị trí quán (chống chấm công từ xa)</span>} style={{ marginTop: 16 }}>
            <Space direction="vertical" style={{ width: '100%' }} size={10}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span>Bật giới hạn vị trí</span>
                <Switch checked={loc.activeFlag} onChange={(v) => setLoc((p) => ({ ...p, activeFlag: v }))} />
              </div>
              <Row gutter={8}>
                <Col span={12}>
                  <InputNumber style={{ width: '100%' }} placeholder="Vĩ độ (lat)" value={loc.latitude}
                               onChange={(v) => setLoc((p) => ({ ...p, latitude: v }))} />
                </Col>
                <Col span={12}>
                  <InputNumber style={{ width: '100%' }} placeholder="Kinh độ (lng)" value={loc.longitude}
                               onChange={(v) => setLoc((p) => ({ ...p, longitude: v }))} />
                </Col>
              </Row>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span>Bán kính (m):</span>
                <InputNumber min={10} max={5000} value={loc.radiusMeters}
                             onChange={(v) => setLoc((p) => ({ ...p, radiusMeters: v }))} />
              </div>
              <Button icon={<AimOutlined />} onClick={useCurrentPosition} block>Dùng vị trí hiện tại của tôi</Button>
              <Button type="primary" icon={<SaveOutlined />} loading={savingLoc} onClick={saveLoc} block>Lưu cài đặt</Button>
              <Alert type="warning" showIcon
                     message={loc.activeFlag
                       ? `Chỉ chấm công được trong bán kính ${loc.radiusMeters}m quanh quán.`
                       : 'Đang TẮT — nhân viên chấm công được ở bất kỳ đâu. Bật công tắc để giới hạn theo vị trí quán.'} />
            </Space>
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

      <Card title="Tổng hợp giờ vào / giờ ra theo nhân viên" style={{ marginTop: 16 }} bodyStyle={{ padding: 0 }}>
        <Table rowKey="key" loading={loading} dataSource={summary} columns={summaryColumns}
               pagination={false} size="small"
               locale={{ emptyText: 'Chưa có dữ liệu' }} />
      </Card>
    </>
  )
}
