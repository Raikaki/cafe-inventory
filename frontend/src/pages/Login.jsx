import { useEffect, useState } from 'react'
import { Card, Form, Input, Button, Typography, message, Alert } from 'antd'
import { UserOutlined, LockOutlined, CoffeeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const { Title, Text } = Typography

export default function Login() {
  const { login, user } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (user) navigate('/dashboard', { replace: true })
  }, [user])

  const onFinish = async (values) => {
    setLoading(true); setError(null)
    try {
      await login(values.username, values.password)
      message.success('Đăng nhập thành công')
      navigate('/dashboard', { replace: true })
    } catch (e) {
      setError('Sai tài khoản hoặc mật khẩu')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-bg">
      <Card className="login-card" bordered={false} bodyStyle={{ padding: '34px 36px' }}>
        <div style={{ textAlign: 'center', marginBottom: 26 }}>
          <div style={{
            width: 64, height: 64, margin: '0 auto 14px', borderRadius: 18,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            background: 'linear-gradient(135deg,#d6a861,#a0522d)', boxShadow: '0 10px 24px rgba(160,82,45,.4)',
          }}>
            <CoffeeOutlined style={{ fontSize: 32, color: '#fff' }} />
          </div>
          <Title level={3} style={{ marginBottom: 2, marginTop: 0 }}>Cafe Inventory</Title>
          <Text type="secondary">Hệ thống quản lý kho nguyên vật liệu</Text>
        </div>

        {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />}

        <Form layout="vertical" onFinish={onFinish} initialValues={{ username: 'admin', password: 'admin123' }} requiredMark={false}>
          <Form.Item name="username" label="Tài khoản" rules={[{ required: true }]}>
            <Input prefix={<UserOutlined style={{ color: '#bbb' }} />} size="large" placeholder="admin" />
          </Form.Item>
          <Form.Item name="password" label="Mật khẩu" rules={[{ required: true }]}>
            <Input.Password prefix={<LockOutlined style={{ color: '#bbb' }} />} size="large" placeholder="••••••" />
          </Form.Item>
          <Button type="primary" htmlType="submit" size="large" block loading={loading}
                  style={{ height: 44, fontWeight: 600, marginTop: 4 }}>
            Đăng nhập
          </Button>
        </Form>
        <div style={{ textAlign: 'center', marginTop: 18 }}>
          <Text type="secondary" style={{ fontSize: 12 }}>Mặc định: admin / admin123</Text>
        </div>
      </Card>
    </div>
  )
}
