import { Routes, Route, Navigate } from 'react-router-dom'
import { Spin } from 'antd'
import { useAuth } from './context/AuthContext'
import AppLayout from './components/AppLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Materials from './pages/Materials'
import Products from './pages/Products'
import Recipes from './pages/Recipes'
import GoodsReceipt from './pages/GoodsReceipt'
import Sales from './pages/Sales'
import Inventory from './pages/Inventory'
import LowStock from './pages/LowStock'
import Forecast from './pages/Forecast'
import InventoryBalance from './pages/InventoryBalance'
import StockSummary from './pages/StockSummary'
import Timesheet from './pages/Timesheet'
import AttendanceAdmin from './pages/AttendanceAdmin'
import Checkin from './pages/Checkin'

function Protected({ children }) {
  const { user, loading } = useAuth()
  if (loading) {
    return <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center' }}><Spin size="large" /></div>
  }
  return user ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/checkin" element={<Checkin />} />
      <Route element={<Protected><AppLayout /></Protected>}>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/materials" element={<Materials />} />
        <Route path="/products" element={<Products />} />
        <Route path="/recipes" element={<Recipes />} />
        <Route path="/goods-receipts" element={<GoodsReceipt />} />
        <Route path="/sales" element={<Sales />} />
        <Route path="/inventory" element={<Inventory />} />
        <Route path="/low-stock" element={<LowStock />} />
        <Route path="/inventory-balance" element={<InventoryBalance />} />
        <Route path="/stock-summary" element={<StockSummary />} />
        <Route path="/timesheet" element={<Timesheet />} />
        <Route path="/forecast" element={<Forecast />} />
        <Route path="/attendance" element={<AttendanceAdmin />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
