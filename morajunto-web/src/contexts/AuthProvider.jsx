import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, apagarToken, lerToken, salvarToken } from '../api'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }) {
    const [usuario, setUsuario] = useState(null)
    const [carregando, setCarregando] = useState(() => Boolean(lerToken()))

    useEffect(() => {
        if (!lerToken()) {
            return
        }
        api('/api/usuarios/me')
            .then(setUsuario)
            .catch(() => apagarToken())
            .finally(() => setCarregando(false))
    }, [])

    useEffect(() => {
        const sessaoExpirada = () => setUsuario(null)
        window.addEventListener('morajunto:sessao-expirada', sessaoExpirada)
        return () => window.removeEventListener('morajunto:sessao-expirada', sessaoExpirada)
    }, [])

    const entrar = useCallback(async (email, senha) => {
        const resposta = await api('/api/auth/login', { metodo: 'POST', corpo: { email, senha } })
        salvarToken(resposta.token)
        setUsuario(resposta.usuario)
    }, [])

    const cadastrar = useCallback(async (nome, email, senha) => {
        await api('/api/auth/cadastro', { metodo: 'POST', corpo: { nome, email, senha } })
        await entrar(email, senha)
    }, [entrar])

    const sair = useCallback(() => {
        apagarToken()
        setUsuario(null)
    }, [])

    const valor = useMemo(
        () => ({ usuario, setUsuario, carregando, entrar, cadastrar, sair }),
        [usuario, carregando, entrar, cadastrar, sair]
    )

    return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}