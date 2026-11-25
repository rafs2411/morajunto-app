import { useCallback, useEffect, useMemo, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../hooks/useAuth'
import { Carregando } from '../components/ui'
import { MoradiaContext } from './moradia-context'

export function MoradiaProvider({ children }) {
    const { usuario } = useAuth()
    const [moradias, setMoradias] = useState([])
    const [carregando, setCarregando] = useState(true)

    useEffect(() => {
        api('/api/moradias')
            .then(setMoradias)
            .catch(() => setMoradias([]))
            .finally(() => setCarregando(false))
    }, [])

    const recarregar = useCallback(async () => {
        const lista = await api('/api/moradias')
        setMoradias(lista)
        return lista
    }, [])

    const selecionar = useCallback(async moradiaId => {
        await api(`/api/moradias/${moradiaId}/selecionar`, { metodo: 'POST' })
        await recarregar()
    }, [recarregar])

    const valor = useMemo(() => {
        const ativa = moradias.find(moradia => moradia.ativa) ?? null
        const ehDono = Boolean(ativa && usuario && ativa.donoId === usuario.id)
        return { moradias, ativa, ehDono, recarregar, selecionar }
    }, [moradias, usuario, recarregar, selecionar])

    if (carregando) {
        return <Carregando />
    }

    return <MoradiaContext.Provider value={valor}>{children}</MoradiaContext.Provider>
}