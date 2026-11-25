import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthProvider'
import { MoradiaProvider } from './contexts/MoradiaProvider'
import { ToastProvider } from './contexts/ToastProvider'
import { RotaProtegida } from './components/RotaProtegida'
import Layout from './components/Layout'
import EntradaMoradia from './components/EntradaMoradia'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Moradias from './pages/Moradias'
import Moradores from './pages/Moradores'
import Despesas from './pages/Despesas'
import Tarefas from './pages/Tarefas'
import Calendario from './pages/Calendario'
import Compras from './pages/Compras'
import Configuracoes from './pages/Configuracoes'

function AreaLogada() {
  return (
    <RotaProtegida>
      <MoradiaProvider>
        <Layout />
      </MoradiaProvider>
    </RotaProtegida>
  )
}

function PaginaMoradias() {
  return (
    <>
      <Moradias />
      <EntradaMoradia />
    </>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<AreaLogada />}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="moradias" element={<PaginaMoradias />} />
              <Route path="moradores" element={<Moradores />} />
              <Route path="despesas" element={<Despesas />} />
              <Route path="tarefas" element={<Tarefas />} />
              <Route path="calendario" element={<Calendario />} />
              <Route path="compras" element={<Compras />} />
              <Route path="configuracoes" element={<Configuracoes />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </ToastProvider>
    </AuthProvider>
  )
}