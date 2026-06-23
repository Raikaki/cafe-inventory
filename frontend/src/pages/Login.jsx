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
      <Card style={{ width: 400, boxShadow: '0 10px 40px rgba(0,0,0,.25)' }} bordered={false}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <CoffeeOutlined style={{ fontSize: 44, color: '#a0522d' }} />
          <Title level={3} style={{ marginBottom: 0, marginTop: 8 }}>Cafe Inventory</Title>
          <Text type="secondary">Hệ thống quản lý kho nguyên vật liệu</Text>
        </div>

        {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />}

        <Form layout="vertical" onFinish={onFinish} initialValues={{ username: 'admin', password: 'admin123' }}>
          <Form.Item name="username" label="Tài khoản" rules={[{ required: true }]}>
            <Input prefix={<UserOutlined />} size="large" placeholder="admin" />
          </Form.Item>
          <Form.Item name="password" label="Mật khẩu" rules={[{ required: true }]}>
            <Input.Password prefix={<LockOutlined />} size="large" placeholder="••••••" />
          </Form.Item>
          <Button type="primary" htmlType="submit" size="large" block loading={loading}>
            Đăng nhập
          </Button>
        </Form>
        <div style={{ textAlign: 'center', marginTop: 16 }}>
          <Text type="secondary" style={{ fontSize: 12 }}>Mặc định: admin / admin123</Text>
        </div>
      </Card>
    </div>
  )
}
