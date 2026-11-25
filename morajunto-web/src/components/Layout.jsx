import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useMoradia } from '../hooks/useMoradia'
import { Avatar } from './ui'

const NAV_ITEMS = [
    { caminho: '/dashboard', label: 'Dashboard', icon: '⊞' },
    { caminho: '/moradias', label: 'Moradias', icon: '🏠' },
    { caminho: '/moradores', label: 'Moradores', icon: '👥' },
    { caminho: '/despesas', label: 'Despesas', icon: '💰' },
    { caminho: '/tarefas', label: 'Tarefas', icon: '✓' },
    { caminho: '/calendario', label: 'Calendário', icon: '📅' },
    { caminho: '/compras', label: 'Compras', icon: '🛒' },
    { caminho: '/configuracoes', label: 'Config.', icon: '⚙' }
]

const BOTTOM_NAV = NAV_ITEMS.filter(item => ['/dashboard', '/despesas', '/tarefas', '/compras', '/moradores'].includes(item.caminho))
const DO_MENU_MAIS = ['/moradias', '/calendario', '/configuracoes']

function Sidebar({ aoNavegar }) {
    const { usuario } = useAuth()
    const { ativa } = useMoradia()

    return (
        <>
            <div style={{ padding: '24px 20px 16px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ width: 32, height: 32, borderRadius: 8, background: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16 }}>🏡</div>
                    <span style={{ fontFamily: 'var(--font-display)', fontSize: 20, fontWeight: 700, color: 'var(--color-primary)' }}>MoraJunto</span>
                </div>
                {ativa ? (
                    <div style={{ marginTop: 12, padding: '8px 10px', background: 'var(--color-primary-light)', borderRadius: 8 }}>
                        <p style={{ fontSize: 11, fontWeight: 600, color: 'var(--color-primary)', opacity: .7, marginBottom: 1 }}>MORADIA ATIVA</p>
                        <p style={{ fontSize: 13, fontWeight: 700, color: 'var(--color-primary)' }}>{ativa.nome}</p>
                        <p style={{ fontSize: 11, color: 'var(--color-primary)', opacity: .7 }}>{ativa.endereco}</p>
                    </div>
                ) : (
                    <div style={{ marginTop: 12, padding: '8px 10px', background: 'var(--color-bg)', borderRadius: 8 }}>
                        <p style={{ fontSize: 12, color: 'var(--color-muted)' }}>Nenhuma moradia ativa</p>
                    </div>
                )}
            </div>

            <nav style={{ flex: 1, padding: '4px 12px', overflowY: 'auto' }}>
                {NAV_ITEMS.map(item => (
                    <NavLink
                        key={item.caminho}
                        to={item.caminho}
                        onClick={aoNavegar}
                        style={({ isActive }) => ({ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 12px', borderRadius: 8, background: isActive ? 'var(--color-primary-light)' : 'transparent', color: isActive ? 'var(--color-primary)' : 'var(--color-muted)', fontWeight: isActive ? 700 : 500, fontSize: 14, marginBottom: 2, textDecoration: 'none', transition: 'all .15s' })}
                    >
                        {({ isActive }) => (
                            <>
                                <span style={{ fontSize: 16, width: 20, textAlign: 'center' }}>{item.icon}</span>
                                {item.label}
                                {isActive && <div style={{ marginLeft: 'auto', width: 4, height: 16, borderRadius: 99, background: 'var(--color-primary)' }} />}
                            </>
                        )}
                    </NavLink>
                ))}
            </nav>

            <div style={{ padding: '16px 20px 24px', borderTop: '1px solid var(--color-border)', display: 'flex', alignItems: 'center', gap: 10 }}>
                <Avatar nome={usuario.nome} cor="#6C63FF" size={30} />
                <div style={{ minWidth: 0 }}>
                    <p style={{ fontSize: 13, fontWeight: 600, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{usuario.nome}</p>
                    <p style={{ fontSize: 11, color: 'var(--color-muted)' }}>v1.0.0 · MoraJunto</p>
                </div>
            </div>
        </>
    )
}

export default function Layout() {
    const [menuAberto, setMenuAberto] = useState(false)
    const { ativa } = useMoradia()
    const { pathname } = useLocation()

    return (
        <div style={{ minHeight: '100vh', display: 'flex' }}>
            <aside className="mj-sidebar" style={{ width: 240, background: '#fff', borderRight: '1px solid var(--color-border)', flexDirection: 'column', position: 'fixed', top: 0, left: 0, bottom: 0, zIndex: 100 }}>
                <Sidebar />
            </aside>

            {menuAberto && (
                <div style={{ position: 'fixed', inset: 0, zIndex: 500, display: 'flex' }}>
                    <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,.4)' }} onClick={() => setMenuAberto(false)} />
                    <aside style={{ width: 260, background: '#fff', position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', boxShadow: '4px 0 20px rgba(0,0,0,.15)' }}>
                        <Sidebar aoNavegar={() => setMenuAberto(false)} />
                    </aside>
                </div>
            )}

            <main className="mj-main" style={{ flex: 1, paddingBottom: 72, minHeight: '100vh', display: 'flex', flexDirection: 'column', minWidth: 0 }}>
                <div className="mj-topbar" style={{ background: '#fff', borderBottom: '1px solid var(--color-border)', padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 12, position: 'sticky', top: 0, zIndex: 50 }}>
                    <button onClick={() => setMenuAberto(true)} style={{ border: 'none', background: 'none', fontSize: 20, color: 'var(--color-text)', padding: 4 }}>☰</button>
                    <span style={{ fontFamily: 'var(--font-display)', fontSize: 18, fontWeight: 700, color: 'var(--color-primary)' }}>MoraJunto</span>
                    {ativa && <span style={{ marginLeft: 'auto', fontSize: 12, color: 'var(--color-muted)', fontWeight: 500 }}>{ativa.nome}</span>}
                </div>

                <div className="mj-conteudo" style={{ flex: 1, padding: '24px 16px', maxWidth: 1060, width: '100%', margin: '0 auto' }}>
                    <Outlet />
                </div>
            </main>

            <nav className="mj-bottomnav" style={{ position: 'fixed', bottom: 0, left: 0, right: 0, background: '#fff', borderTop: '1px solid var(--color-border)', display: 'flex', zIndex: 200 }}>
                {BOTTOM_NAV.map(item => (
                    <NavLink
                        key={item.caminho}
                        to={item.caminho}
                        style={({ isActive }) => ({ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, padding: '8px 4px 6px', color: isActive ? 'var(--color-primary)' : 'var(--color-muted)', textDecoration: 'none' })}
                    >
                        <span style={{ fontSize: 18 }}>{item.icon}</span>
                        <span style={{ fontSize: 10, fontWeight: 600 }}>{item.label}</span>
                    </NavLink>
                ))}
                <button
                    onClick={() => setMenuAberto(true)}
                    style={{ flex: 1, border: 'none', background: 'none', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, padding: '8px 4px 6px', color: DO_MENU_MAIS.includes(pathname) ? 'var(--color-primary)' : 'var(--color-muted)' }}
                >
                    <span style={{ fontSize: 18 }}>⋯</span>
                    <span style={{ fontSize: 10, fontWeight: 600 }}>Mais</span>
                </button>
            </nav>

            <style>{`
        .mj-sidebar { display: none; }
        @media (min-width: 768px) {
          .mj-sidebar { display: flex; }
          .mj-main { margin-left: 240px; padding-bottom: 0 !important; }
          .mj-topbar { display: none !important; }
          .mj-bottomnav { display: none !important; }
          .mj-conteudo { padding: 32px !important; }
        }
      `}</style>
        </div>
    )
}