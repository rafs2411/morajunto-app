import { useCallback, useState } from 'react'
import { ToastContext } from './toast-context'

const CORES = { success: '#10B981', error: '#EF4444', info: '#6C63FF' }
const ICONES = { success: '✓', error: '✕', info: 'ℹ' }

export function ToastProvider({ children }) {
    const [avisos, setAvisos] = useState([])

    const remover = useCallback(id => {
        setAvisos(atuais => atuais.filter(aviso => aviso.id !== id))
    }, [])

    const toast = useCallback((mensagem, tipo = 'success') => {
        const id = crypto.randomUUID()
        setAvisos(atuais => [...atuais, { id, mensagem, tipo }])
        setTimeout(() => remover(id), 3500)
    }, [remover])

    return (
        <ToastContext.Provider value={toast}>
            {children}
            <div style={{ position: 'fixed', bottom: 80, right: 16, zIndex: 2000, display: 'flex', flexDirection: 'column', gap: 8 }}>
                {avisos.map(aviso => (
                    <div
                        key={aviso.id}
                        onClick={() => remover(aviso.id)}
                        style={{ background: CORES[aviso.tipo], color: '#fff', padding: '10px 16px', borderRadius: 10, fontSize: 14, fontWeight: 500, boxShadow: '0 4px 16px rgba(0,0,0,.15)', cursor: 'pointer', minWidth: 220, maxWidth: 360 }}
                    >
                        {ICONES[aviso.tipo]} {aviso.mensagem}
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    )
}