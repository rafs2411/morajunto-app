import { useEffect } from 'react'
import { iniciais } from '../utils/formato'

const inputCss = {
    border: '1.5px solid var(--color-border)',
    borderRadius: 'var(--radius-btn)',
    padding: '9px 12px',
    fontSize: 14,
    outline: 'none',
    background: '#fff',
    color: 'var(--color-text)',
    width: '100%',
    transition: 'border-color .15s'
}

export function Avatar({ nome, cor, size = 36 }) {
    return (
        <div style={{ width: size, height: size, borderRadius: '50%', background: cor, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, fontSize: size * 0.38, fontWeight: 700, color: '#fff' }}>
            {iniciais(nome)}
        </div>
    )
}

export function Pill({ label, cor, size = 'sm' }) {
    return (
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, padding: size === 'xs' ? '2px 8px' : '3px 10px', borderRadius: 99, background: cor + '1A', color: cor, fontSize: size === 'xs' ? 11 : 12, fontWeight: 600, whiteSpace: 'nowrap' }}>
            <span style={{ width: 6, height: 6, borderRadius: '50%', background: cor, flexShrink: 0 }} />
            {label}
        </span>
    )
}

export function Badge({ label, cor }) {
    return (
        <span style={{ padding: '2px 8px', borderRadius: 99, background: cor + '20', color: cor, fontSize: 11, fontWeight: 700 }}>
            {label}
        </span>
    )
}

export function Btn({ children, variant = 'primary', size = 'md', full = false, style, ...props }) {
    const base = { display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 6, borderRadius: 'var(--radius-btn)', fontWeight: 600, border: 'none', transition: 'all .15s', width: full ? '100%' : undefined }
    const tamanhos = {
        sm: { padding: '6px 14px', fontSize: 13 },
        md: { padding: '9px 20px', fontSize: 14 },
        lg: { padding: '13px 28px', fontSize: 15 }
    }
    const variantes = {
        primary: { background: 'var(--color-primary)', color: '#fff' },
        secondary: { background: 'var(--color-primary-light)', color: 'var(--color-primary)' },
        ghost: { background: 'transparent', color: 'var(--color-muted)' },
        danger: { background: 'var(--color-danger-light)', color: 'var(--color-danger)' },
        white: { background: '#fff', color: 'var(--color-text)', boxShadow: '0 1px 4px rgba(0,0,0,.1)' }
    }
    return (
        <button {...props} style={{ ...base, ...tamanhos[size], ...variantes[variant], ...style }}>
            {children}
        </button>
    )
}

export function Field({ label, children, hint, erro }) {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--color-muted)' }}>{label}</label>
            {children}
            {hint && !erro && <p style={{ fontSize: 12, color: 'var(--color-muted)' }}>{hint}</p>}
            {erro && <p style={{ fontSize: 12, color: 'var(--color-danger)', fontWeight: 500 }}>{erro}</p>}
        </div>
    )
}

export function Inp({ style, ref, ...props }) {
    return <input ref={ref} {...props} className="mj-input" style={{ ...inputCss, ...style }} />
}

export function Sel({ children, style, ...props }) {
    return (
        <select {...props} className="mj-input" style={{ ...inputCss, ...style }}>
            {children}
        </select>
    )
}

export function Textarea({ style, ...props }) {
    return <textarea rows={3} {...props} className="mj-input" style={{ ...inputCss, resize: 'vertical', ...style }} />
}

export function Card({ children, style, ...props }) {
    return (
        <div {...props} style={{ background: 'var(--color-surface)', borderRadius: 'var(--radius-card)', boxShadow: 'var(--shadow-card)', border: '1px solid var(--color-border)', ...style }}>
            {children}
        </div>
    )
}

export function EmptyState({ icon, title, subtitle, action }) {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '48px 24px', gap: 12, textAlign: 'center' }}>
            <div style={{ fontSize: 40 }}>{icon}</div>
            <p style={{ fontWeight: 700, fontSize: 16 }}>{title}</p>
            {subtitle && <p style={{ fontSize: 14, color: 'var(--color-muted)', maxWidth: 300 }}>{subtitle}</p>}
            {action}
        </div>
    )
}

export function Carregando() {
    return <p style={{ padding: 24, color: 'var(--color-muted)', fontSize: 14 }}>Carregando...</p>
}

export function PageHeader({ titulo, subtitulo, acao }) {
    return (
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12, marginBottom: 24, flexWrap: 'wrap' }}>
            <div>
                <h1 style={{ fontFamily: 'var(--font-display)', fontSize: 28, fontWeight: 600, marginBottom: 4 }}>{titulo}</h1>
                {subtitulo && <p style={{ color: 'var(--color-muted)', fontSize: 15 }}>{subtitulo}</p>}
            </div>
            {acao}
        </div>
    )
}

export function Modal({ title, onClose, children, wide }) {
    useEffect(() => {
        const fecharNoEsc = evento => {
            if (evento.key === 'Escape') {
                onClose()
            }
        }
        document.addEventListener('keydown', fecharNoEsc)
        return () => document.removeEventListener('keydown', fecharNoEsc)
    }, [onClose])

    return (
        <div style={{ position: 'fixed', inset: 0, zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 16, background: 'rgba(0,0,0,.45)', backdropFilter: 'blur(3px)' }} onClick={onClose}>
            <div style={{ background: '#fff', borderRadius: '1.25rem', width: '100%', maxWidth: wide ? 680 : 520, maxHeight: '90vh', overflow: 'auto', boxShadow: 'var(--shadow-modal)' }} onClick={evento => evento.stopPropagation()}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '20px 24px 16px', borderBottom: '1px solid var(--color-border)' }}>
                    <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 20, fontWeight: 600 }}>{title}</h2>
                    <button onClick={onClose} style={{ width: 32, height: 32, borderRadius: '50%', border: 'none', background: 'var(--color-border)', fontSize: 16, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-muted)' }}>×</button>
                </div>
                <div style={{ padding: '20px 24px 24px' }}>{children}</div>
            </div>
        </div>
    )
}

export function ConfirmDialog({ titulo = 'Confirmar exclusão', msg, textoConfirmar = 'Excluir', onConfirm, onCancel }) {
    return (
        <div style={{ position: 'fixed', inset: 0, zIndex: 1100, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 16, background: 'rgba(0,0,0,.5)', backdropFilter: 'blur(3px)' }}>
            <Card style={{ padding: 24, maxWidth: 360, width: '100%' }}>
                <p style={{ fontSize: 16, fontWeight: 600, marginBottom: 8 }}>{titulo}</p>
                <p style={{ fontSize: 14, color: 'var(--color-muted)', marginBottom: 20 }}>{msg}</p>
                <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                    <Btn variant="ghost" size="sm" onClick={onCancel}>Cancelar</Btn>
                    <Btn variant="danger" size="sm" onClick={onConfirm}>{textoConfirmar}</Btn>
                </div>
            </Card>
        </div>
    )
}