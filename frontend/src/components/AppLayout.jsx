import { useState } from 'react'
import { Layout, Menu, Avatar, Dropdown, Tag, Grid, theme as antdTheme } from 'antd'
import {
  DashboardOutlined, InboxOutlined, CoffeeOutlined, ExperimentOutlined,
  ImportOutlined, ShoppingCartOutlined, SwapOutlined, WarningOutlined,
  LogoutOutlined, UserOutlined, MenuFoldOutlined, MenuUnfoldOutlined,
} from '@ant-design/icons'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

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
    { key: '/low-stock', icon: <WarningOutlined />, label: 'Cảnh báo tồn thấp' },
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
