import { useState, useEffect } from 'react'
import { Layout, Menu, Avatar, Dropdown, Tag, Grid, notification, theme as antdTheme } from 'antd'
import {
  DashboardOutlined, InboxOutlined, CoffeeOutlined, ExperimentOutlined,
  ImportOutlined, ShoppingCartOutlined, SwapOutlined, WarningOutlined,
  LogoutOutlined, UserOutlined, MenuFoldOutlined, MenuUnfoldOutlined, ThunderboltOutlined,
  FileTextOutlined,
} from '@ant-design/icons'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import client from '../api/client'

const { Header, Sider, Content } = Layout
const { useBreakpoint } = Grid

const items = [
  { type: 'group', label: 'TỔNG QUAN', children: [
    { key: '/dashboard', icon: <DashboardOutlined />, label: 'Dashboard' },
  ]},
  { type: 'group', label: 'DANH MỤC', children: [
    { key: '/materials', icon: <InboxOutlined />, label: 'Nguyên vật liệu' },
    { key: '/products', icon: <CoffeeOutlined />, label: 'Sản phẩm' },
    { key: '/recipes', icon: <ExperimentOutlined />, label: 'Công thức (BOM)' },
  ]},
  { type: 'group', label: 'NGHIỆP VỤ', children: [
    { key: '/goods-receipts', icon: <ImportOutlined />, label: 'Nhập kho' },
    { key: '/sales', icon: <ShoppingCartOutlined />, label: 'Bán hàng' },
  ]},
  { type: 'group', label: 'KHO & BÁO CÁO', children: [
    { key: '/inventory', icon: <SwapOutlined />, label: 'Sổ kho' },
    { key: '/inventory-balance', icon: <FileTextOutlined />, label: 'Tổng hợp tồn theo kỳ' },
    { key: '/low-stock', icon: <WarningOutlined />, label: 'Cảnh báo tồn thấp' },
    { key: '/forecast', icon: <ThunderboltOutlined />, label: 'Dự báo tồn (AI)' },
  ]},
]

const roleColor = (role) => ({
  ROLE_ADMIN: 'red', ROLE_MANAGER: 'volcano', ROLE_STAFF: 'blue', ROLE_VIEWER: 'default',
}[role] || 'default')

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuth()
  const screens = useBreakpoint()
  const { token } = antdTheme.useToken()
  const [noti, notiCtx] = notification.useNotification()

  // Low-stock warning shown once per login session
  useEffect(() => {
    if (!user) return
    if (sessionStorage.getItem('lowstock_shown')) return
    client.get('/api/inventory/low-stock').then((res) => {
      const list = res.data || []
      if (list.length > 0) {
        noti.warning({
          message: `Cảnh báo tồn kho: ${list.length} nguyên vật liệu dưới định mức`,
          description: (
            <div>
              <ul style={{ paddingLeft: 18, margin: '4px 0' }}>
                {list.slice(0, 5).map((m) => (
                  <li key={m.id}>{m.materialName}: còn <b style={{ color: '#cf1322' }}>{m.currentQty}</b> {m.unit} (tối thiểu {m.minimumQty})</li>
                ))}
              </ul>
              {list.length > 5 && <span>... và {list.length - 5} loại khác.</span>}
              <div style={{ marginTop: 8 }}>
                <a onClick={() => navigate('/low-stock')}>Xem chi tiết →</a>
              </div>
            </div>
          ),
          duration: 0,
          placement: 'topRight',
        })
      }
      sessionStorage.setItem('lowstock_shown', '1')
    }).catch(() => {})
  }, [user])

  const userMenu = {
    items: [
      { key: 'role', disabled: true, label: <span>Vai trò: <Tag color={roleColor(user?.role)}>{(user?.role || '').replace('ROLE_', '')}</Tag></span> },
      { type: 'divider' },
      { key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất', danger: true },
    ],
    onClick: ({ key }) => { if (key === 'logout') logout() },
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {notiCtx}
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed} trigger={null}
             breakpoint="lg" collapsedWidth={screens.xs ? 0 : 80} width={240} theme="dark">
        <div className="brand-logo">
          <CoffeeOutlined style={{ fontSize: 22, color: '#c8964f' }} />
          {!collapsed && <span>Cafe Inventory</span>}
        </div>
        <Menu theme="dark" mode="inline"
              selectedKeys={[location.pathname]}
              onClick={({ key }) => navigate(key)}
              items={items} />
      </Sider>

      <Layout>
        <Header style={{ padding: '0 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                         boxShadow: '0 1px 4px rgba(0,0,0,.08)' }}>
          <div onClick={() => setCollapsed(!collapsed)} style={{ cursor: 'pointer', fontSize: 18 }}>
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </div>
          <Dropdown menu={userMenu} placement="bottomRight" arrow>
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar style={{ backgroundColor: token.colorPrimary }} icon={<UserOutlined />} />
              <span style={{ fontWeight: 600 }}>{user?.username}</span>
            </div>
          </Dropdown>
        </Header>

        <Content className="site-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
