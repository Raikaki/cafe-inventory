import { useState } from 'react'
import { Card, Input, Button, Typography, Result, Alert, Space, Spin } from 'antd'
import { CoffeeOutlined, EnvironmentOutlined, ClockCircleOutlined } from '@ant-design/icons'
import { useSearchParams } from 'react-router-dom'
import dayjs from 'dayjs'
import client from '../api/client'

const { Title, Text } = Typography

export default function Checkin() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const [name, setName] = useState('')
  const [loading, setLoading] = useState(false)
  const [done, setDone] = useState(null)
  const [error, setError] = useState(null)

  const submit = () => {
    if (!token) { setError('Thiếu mã QR. Vui lòng quét lại mã.'); return }
    if (!name.trim()) { setError('Vui lòng nhập họ tên'); return }
    setError(null); setLoading(true)

    const send = (lat, lng) => {
      client.post('/api/attendance/checkin', {
        token, employeeName: name.trim(), latitude: lat, longitude: lng,
      })
        .then((r) => setDone(r.data))
        .catch((e) => setError(e.response?.data?.message || 'Chấm công thất bại'))
        .finally(() => setLoading(false))
    }

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => send(pos.coords.latitude, pos.coords.longitude),
        () => send(null, null),           // user denied or unavailable -> still check in without location
        { enableHighAccuracy: true, timeout: 8000 }
      )
    } else {
      send(null, null)
    }
  }

  return (
    <div className="login-bg" style={{ padding: 16 }}>
      <Card style={{ width: 400, maxWidth: '100%', boxShadow: '0 10px 40px rgba(0,0,0,.25)' }} bordered={false}>
        <div style={{ textAlign: 'center', marginBottom: 20 }}>
          <CoffeeOutlined style={{ fontSize: 40, color: '#a0522d' }} />
          <Title level={3} style={{ marginBottom: 0, marginTop: 8 }}>Chấm công</Title>
          <Text type="secondary">Cafe Inventory</Text>
        </div>

        {done ? (
          <Result status="success"
            title={`Đã chấm công ${done.checkType === 'RA' ? 'GIỜ RA' : 'GIỜ VÀO'} thành công!`}
            subTitle={
              <Space direction="vertical" size={4} style={{ textAlign: 'left' }}>
                <span><b>{done.employeeName}</b> — {done.checkType === 'RA' ? 'Giờ ra' : 'Giờ vào'}</span>
                <span><ClockCircleOutlined /> {dayjs(done.scanTime).format('DD/MM/YYYY HH:mm:ss')}</span>
                <span>IP: {done.ipAddress}</span>
                {done.latitude != null
                  ? <span><EnvironmentOutlined /> {Number(done.latitude).toFixed(5)}, {Number(done.longitude).toFixed(5)}</span>
                  : <span><EnvironmentOutlined /> Không lấy được vị trí</span>}
              </Space>
            } />
        ) : (
          <>
            {!token && <Alert type="error" showIcon style={{ marginBottom: 12 }}
                              message="Thiếu mã QR — vui lòng quét lại mã của hôm nay." />}
            {error && <Alert type="error" showIcon style={{ marginBottom: 12 }} message={error} />}
            <Spin spinning={loading} tip="Đang lấy vị trí & gửi...">
              <Input size="large" placeholder="Họ và tên nhân viên" value={name}
                     onChange={(e) => setName(e.target.value)} onPressEnter={submit}
                     style={{ marginBottom: 12 }} />
              <Button type="primary" size="large" block onClick={submit} disabled={!token}>
                Chấm công ngay
              </Button>
              <Text type="secondary" style={{ display: 'block', textAlign: 'center', marginTop: 12, fontSize: 12 }}>
                Trình duyệt sẽ hỏi quyền truy cập vị trí — hãy cho phép để ghi nhận nơi chấm công.
              </Text>
            </Spin>
          </>
        )}
      </Card>
    </div>
  )
}
