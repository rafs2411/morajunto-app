import { useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { Btn, Field, Inp } from '../components/ui'

const VANTAGENS = ['Divida despesas sem drama', 'Organize tarefas domésticas', 'Gerencie compras em grupo']

export default function Login() {
    const { usuario, entrar, cadastrar } = useAuth()
    const [aba, setAba] = useState('login')
    const [form, setForm] = useState({ nome: '', email: '', senha: '' })
    const [erros, setErros] = useState({})
    const [erroGeral, setErroGeral] = useState('')
    const [enviando, setEnviando] = useState(false)

    if (usuario) {
        return <Navigate to="/" replace />
    }

    function mudar(campo, valor) {
        setForm(atual => ({ ...atual, [campo]: valor }))
    }

    function trocarAba(novaAba) {
        setAba(novaAba)
        setErros({})
        setErroGeral('')
    }

    async function enviar(evento) {
        evento.preventDefault()
        setErros({})
        setErroGeral('')
        setEnviando(true)
        try {
            const email = form.email.trim()
            if (aba === 'login') {
                await entrar(email, form.senha)
            } else {
                await cadastrar(form.nome.trim(), email, form.senha)
            }
        } catch (erro) {
            setErros(erro.erros ?? {})
            setErroGeral(erro.message)
        } finally {
            setEnviando(false)
        }
    }

    return (
        <div className="login-grid" style={{ minHeight: '100vh', display: 'grid' }}>
            <div className="login-left" style={{ background: 'var(--color-primary)', padding: 48, flexDirection: 'column', justifyContent: 'center', color: '#fff' }}>
                <div style={{ fontFamily: 'var(--font-display)', fontSize: 36, fontWeight: 700, marginBottom: 8 }}>MoraJunto</div>
                <p style={{ fontSize: 18, opacity: .85, marginBottom: 48, lineHeight: 1.5 }}>
                    Gestão descomplicada<br />para moradias compartilhadas.
                </p>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                    {VANTAGENS.map(texto => (
                        <div key={texto} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <div style={{ width: 24, height: 24, borderRadius: '50%', background: 'rgba(255,255,255,.25)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>✓</div>
                            <span style={{ opacity: .9, fontSize: 15 }}>{texto}</span>
                        </div>
                    ))}
                </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 32, background: 'var(--color-bg)' }}>
                <div style={{ width: '100%', maxWidth: 380 }}>
                    <div style={{ display: 'flex', gap: 4, marginBottom: 28, background: 'var(--color-border)', borderRadius: 10, padding: 4 }}>
                        {[['login', 'Entrar'], ['cadastro', 'Cadastrar']].map(([valor, texto]) => (
                            <button
                                key={valor}
                                type="button"
                                onClick={() => trocarAba(valor)}
                                style={{ flex: 1, padding: 8, borderRadius: 7, border: 'none', background: aba === valor ? '#fff' : 'transparent', fontWeight: 600, fontSize: 14, color: aba === valor ? 'var(--color-text)' : 'var(--color-muted)', boxShadow: aba === valor ? '0 1px 4px rgba(0,0,0,.1)' : undefined, transition: 'all .15s' }}
                            >
                                {texto}
                            </button>
                        ))}
                    </div>

                    <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                        {aba === 'cadastro' && (
                            <Field label="Nome completo" erro={erros.nome}>
                                <Inp placeholder="Maria Santos" value={form.nome} onChange={e => mudar('nome', e.target.value)} />
                            </Field>
                        )}
                        <Field label="E-mail" erro={erros.email}>
                            <Inp type="email" placeholder="voce@email.com" value={form.email} onChange={e => mudar('email', e.target.value)} />
                        </Field>
                        <Field label="Senha" erro={erros.senha} hint={aba === 'cadastro' ? 'Mínimo de 8 caracteres' : undefined}>
                            <Inp type="password" value={form.senha} onChange={e => mudar('senha', e.target.value)} />
                        </Field>

                        {erroGeral && Object.keys(erros).length === 0 && (
                            <p style={{ fontSize: 13, color: 'var(--color-danger)', background: 'var(--color-danger-light)', padding: '10px 12px', borderRadius: 8, fontWeight: 500 }}>
                                {erroGeral}
                            </p>
                        )}

                        <Btn type="submit" size="lg" full disabled={enviando}>
                            {enviando ? 'Aguarde...' : aba === 'login' ? 'Entrar na conta' : 'Criar conta'}
                        </Btn>
                    </form>
                </div>
            </div>

            <style>{`
        .login-grid { grid-template-columns: 1fr 1fr; }
        .login-left { display: flex; }
        @media (max-width: 640px) {
          .login-grid { grid-template-columns: 1fr; }
          .login-left { display: none; }
        }
      `}</style>
        </div>
    )
}