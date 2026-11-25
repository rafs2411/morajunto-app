import { Navigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { Carregando } from './ui'

export function RotaProtegida({ children }) {
    const { usuario, carregando } = useAuth()

    if (carregando) {
        return <Carregando />
    }
    if (!usuario) {
        return <Navigate to="/login" replace />
    }
    return children
}