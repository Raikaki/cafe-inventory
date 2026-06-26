import { useState, useEffect } from 'react'
import { Layout, Menu, Avatar, Dropdown, Tag, Grid, notification, theme as antdTheme } from 'antd'
import {
  DashboardOutlined, InboxOutlined, CoffeeOutlined, ExperimentOutlined,
  ImportOutlined, ShoppingCartOutlined, SwapOutlined, WarningOutlined,
  LogoutOutlined, UserOutlined, MenuFoldOutlined, MenuUnfoldOutlined, ThunderboltOutlined,
  FileTextOutlined, DatabaseOutlined, QrcodeOutlined, AppstoreOutlined, TeamOutlined, ShopOutlined,
  SnippetsOutlined, DollarOutlined,
} from '@ant-design/icons'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import client from '../api/client'

const { Header, Sider, Content } = Layout
const { useBreakpoint } = Grid

const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: 'cat', icon: <AppstoreOutlined />, label: 'Danh mục', children: [
    { key: '/materials', icon: <InboxOutlined />, label: 'Nguyên vật liệu' },
    { key: '/products', icon: <CoffeeOutlined />, label: 'Sản phẩm' },
    { key: '/recipes', icon: <ExperimentOutlined />, label: 'Công thức (BOM)' },
    { key: '/suppliers', icon: <ShopOutlined />, label: 'Nhà cung cấp' },
  ]},
  { key: 'ops', icon: <SwapOutlined />, label: 'Nghiệp vụ', children: [
    { key: '/goods-receipts', icon: <ImportOutlined />, label: 'Nhập kho' },
    { key: '/sales', icon: <ShoppingCartOutlined />, label: 'Bán hàng' },
  ]},
  { key: 'wh', icon: <DatabaseOutlined />, label: 'Kho & Báo cáo', children: [
    { key: '/inventory', icon: <SwapOutlined />, label: 'Sổ kho' },
    { key: '/inventory-balance', icon: <FileTextOutlined />, label: 'Tổng hợp theo kỳ' },
    { key: '/stock-summary', icon: <DatabaseOutlined />, label: 'Chốt tồn & giá' },
    { key: '/low-stock', icon: <WarningOutlined />, label: 'Cảnh báo tồn thấp' },
    { key: '/product-sales', icon: <ShoppingCartOutlined />, label: 'Thành phẩm đã bán' },
    { key: '/price-comparison', icon: <DollarOutlined />, label: 'So sánh giá NCC' },
    { key: '/forecast', icon: <ThunderboltOutlined />, label: 'Dự báo tồn (AI)' },
  ]},
  { key: 'hr', icon: <TeamOutlined />, label: 'Nhân sự', children: [
    { key: '/attendance', icon: <QrcodeOutlined />, label: 'Chấm công QR' },
    { key: '/timesheet', icon: <FileTextOutlined />, label: 'Bảng chấm công' },
  ]},
  { key: 'acc', icon: <SnippetsOutlined />, label: 'Kế toán', children: [
    { key: '/vouchers', icon: <FileTextOutlined />, label: 'Chứng từ' },
  ]},
]

const ROOT_KEYS = ['cat', 'ops', 'wh', 'hr', 'acc']
const PATH_PARENT = {
  '/materials': 'cat', '/products': 'cat', '/recipes': 'cat', '/suppliers': 'cat',
  '/goods-receipts': 'ops', '/sales': 'ops',
  '/inventory': 'wh', '/inventory-balance': 'wh', '/stock-summary': 'wh', '/low-stock': 'wh', '/forecast': 'wh', '/product-sales': 'wh', '/price-comparison': 'wh',
  '/attendance': 'hr', '/timesheet': 'hr', '/vouchers': 'acc',
}
const TITLES = {
  '/dashboard': 'Dashboard', '/materials': 'Nguyên vật liệu', '/products': 'Sản phẩm',
  '/recipes': 'Công thức (BOM)', '/suppliers': 'Nhà cung cấp', '/goods-receipts': 'Nhập kho', '/sales': 'Bán hàng',
  '/inventory': 'Sổ kho', '/inventory-balance': 'Tổng hợp tồn theo kỳ', '/stock-summary': 'Chốt tồn & giá theo tháng',
  '/low-stock': 'Cảnh báo tồn thấp', '/forecast': 'Dự báo tồn (AI)', '/attendance': 'Chấm công QR',
  '/timesheet': 'Bảng chấm công', '/product-sales': 'Thành phẩm đã bán',
  '/price-comparison': 'So sánh giá nhà cung cấp', '/vouchers': 'Chứng từ kế toán',
}

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

  const [openKeys, setOpenKeys] = useState(PATH_PARENT[location.pathname] ? [PATH_PARENT[location.pathname]] : ['cat'])

  // keep the parent of the active route open
  useEffect(() => {
    const parent = PATH_PARENT[location.pathname]
    if (parent && !openKeys.includes(parent)) setOpenKeys([parent])
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname])

  // accordion: only one parent open at a time
  const onOpenChange = (keys) => {
    const latest = keys.find((k) => !openKeys.includes(k))
    if (latest && ROOT_KEYS.includes(latest)) setOpenKeys([latest])
    else setOpenKeys(keys.filter((k) => ROOT_KEYS.includes(k)))
  }

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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user])

  const userMenu = {
    items: [
      { key: 'role', disabled: true, label: <span>Vai trò: <Tag color={roleColor(user?.role)}>{(user?.role || '').replace('ROLE_', '')}</Tag></span> },
      { type: 'divider' },
      { key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất', danger: true },
    ],
    onClick: ({ key }) => { if (key === 'logout') logout() },
  }

  const pageTitle = TITLES[location.pathname] || 'Cafe Inventory'

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {notiCtx}
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed} trigger={null}
             breakpoint="lg" collapsedWidth={screens.xs ? 0 : 80} width={250} theme="dark">
        <div className="brand-logo">
          <span className="brand-badge"><CoffeeOutlined /></span>
          {!collapsed && <span className="brand-text">Cafe Inventory</span>}
        </div>
        <Menu theme="dark" mode="inline"
              selectedKeys={[location.pathname]}
              openKeys={collapsed ? undefined : openKeys}
              onOpenChange={onOpenChange}
              onClick={({ key }) => { if (!ROOT_KEYS.includes(key)) navigate(key) }}
              items={menuItems} style={{ borderInlineEnd: 'none' }} />
      </Sider>

      <Layout>
        <Header className="app-header">
          <div className="app-header-left">
            <span className="collapse-btn" onClick={() => setCollapsed(!collapsed)}>
              {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            </span>
            <span className="page-title">{pageTitle}</span>
          </div>
          <Dropdown menu={userMenu} placement="bottomRight" arrow>
            <div className="user-chip">
              <Avatar size={32} style={{ backgroundColor: token.colorPrimary }} icon={<UserOutlined />} />
              <div className="user-meta">
                <span className="user-name">{user?.username}</span>
                <span className="user-role">{(user?.role || '').replace('ROLE_', '')}</span>
              </div>
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
