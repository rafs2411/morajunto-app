import { useEffect, useState } from 'react'
import { api, baixarArquivo } from '../api'
import { useAuth } from '../hooks/useAuth'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { Avatar, Btn, Card, Field, Inp, Modal, PageHeader, Sel } from '../components/ui'

function Linha({ icone, titulo, descricao, onClick }) {
    return (
        <button onClick={onClick} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 16, padding: '16px 20px', background: '#fff', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-card)', boxShadow: 'var(--shadow-card)', textAlign: 'left' }}>
            <div style={{ width: 46, height: 46, borderRadius: 12, background: 'var(--color-primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20, flexShrink: 0 }}>{icone}</div>
            <div style={{ flex: 1 }}>
                <p style={{ fontWeight: 600, fontSize: 16 }}>{titulo}</p>
                <p style={{ fontSize: 14, color: 'var(--color-muted)', marginTop: 2 }}>{descricao}</p>
            </div>
            <span style={{ color: 'var(--color-muted)', fontSize: 18 }}>›</span>
        </button>
    )
}

function Interruptor({ ligado, onChange, titulo, descricao }) {
    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <div style={{ flex: 1 }}>
                <p style={{ fontWeight: 600, fontSize: 15 }}>{titulo}</p>
                <p style={{ fontSize: 13, color: 'var(--color-muted)' }}>{descricao}</p>
            </div>
            <button
                type="button"
                onClick={() => onChange(!ligado)}
                style={{ width: 46, height: 26, borderRadius: 99, border: 'none', background: ligado ? 'var(--color-primary)' : 'var(--color-border)', position: 'relative', transition: 'background .15s', flexShrink: 0 }}
            >
                <span style={{ position: 'absolute', top: 3, left: ligado ? 23 : 3, width: 20, height: 20, borderRadius: '50%', background: '#fff', boxShadow: '0 1px 3px rgba(0,0,0,.2)', transition: 'left .15s' }} />
            </button>
        </div>
    )
}

function Botoes({ onCancelar, salvando, texto = 'Salvar' }) {
    return (
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', marginTop: 4 }}>
            <Btn type="button" variant="ghost" onClick={onCancelar}>Cancelar</Btn>
            <Btn type="submit" disabled={salvando}>{salvando ? 'Aguarde...' : texto}</Btn>
        </div>
    )
}

function ErroGeral({ texto }) {
    return texto ? <p style={{ fontSize: 13, color: 'var(--color-danger)' }}>{texto}</p> : null
}

function FotoPerfil({ usuario, tamanho }) {
    const [falhou, setFalhou] = useState(false)
    if (usuario.fotoUrl && !falhou) {
        return <img src={usuario.fotoUrl} alt="" onError={() => setFalhou(true)} style={{ width: tamanho, height: tamanho, borderRadius: '50%', objectFit: 'cover' }} />
    }
    return <Avatar nome={usuario.nome} cor="#6C63FF" size={tamanho} />
}

function PerfilForm({ onFechar }) {
    const { usuario, setUsuario } = useAuth()
    const { recarregar } = useMoradia()
    const toast = useToast()
    const [form, setForm] = useState({ nome: usuario.nome, email: usuario.email, fotoUrl: usuario.fotoUrl ?? '' })
    const [erros, setErros] = useState({})
    const [erroGeral, setErroGeral] = useState('')
    const [salvando, setSalvando] = useState(false)

    async function enviar(evento) {
        evento.preventDefault()
        setErros({})
        setErroGeral('')
        setSalvando(true)
        try {
            const atualizado = await api('/api/usuarios/me', {
                metodo: 'PUT',
                corpo: { nome: form.nome.trim(), email: form.email.trim(), fotoUrl: form.fotoUrl.trim() }
            })
            setUsuario(atualizado)
            await recarregar()
            toast('Perfil atualizado')
            onFechar()
        } catch (erro) {
            const errosCampos = erro.erros ?? {}
            setErros(errosCampos)
            setErroGeral(Object.keys(errosCampos).length === 0 ? erro.message : '')
            setSalvando(false)
        }
    }

    return (
        <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <FotoPerfil key={form.fotoUrl} usuario={{ nome: form.nome || '?', fotoUrl: form.fotoUrl }} tamanho={56} />
                <span style={{ fontSize: 13, color: 'var(--color-muted)' }}>Prévia da foto</span>
            </div>
            <Field label="Nome completo *" erro={erros.nome}>
                <Inp value={form.nome} onChange={e => setForm(atual => ({ ...atual, nome: e.target.value }))} />
            </Field>
            <Field label="E-mail *" erro={erros.email}>
                <Inp type="email" value={form.email} onChange={e => setForm(atual => ({ ...atual, email: e.target.value }))} />
            </Field>
            <Field label="Link da foto" erro={erros.fotoUrl} hint="Opcional. Cole o endereço de uma imagem (começando com https://).">
                <Inp placeholder="https://..." value={form.fotoUrl} onChange={e => setForm(atual => ({ ...atual, fotoUrl: e.target.value }))} />
            </Field>
            <ErroGeral texto={erroGeral} />
            <Botoes onCancelar={onFechar} salvando={salvando} />
        </form>
    )
}

function SenhaForm({ onFechar }) {
    const toast = useToast()
    const [form, setForm] = useState({ senhaAtual: '', novaSenha: '', confirmacao: '' })
    const [erros, setErros] = useState({})
    const [erroGeral, setErroGeral] = useState('')
    const [salvando, setSalvando] = useState(false)

    async function enviar(evento) {
        evento.preventDefault()
        setErros({})
        setErroGeral('')
        if (form.novaSenha !== form.confirmacao) {
            setErros({ confirmacao: 'As senhas não são iguais' })
            return
        }
        setSalvando(true)
        try {
            await api('/api/usuarios/me/senha', { metodo: 'PUT', corpo: { senhaAtual: form.senhaAtual, novaSenha: form.novaSenha } })
            toast('Senha alterada')
            onFechar()
        } catch (erro) {
            const errosCampos = erro.erros ?? {}
            setErros(errosCampos)
            setErroGeral(Object.keys(errosCampos).length === 0 ? erro.message : '')
            setSalvando(false)
        }
    }

    return (
        <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <Field label="Senha atual" erro={erros.senhaAtual}>
                <Inp type="password" value={form.senhaAtual} onChange={e => setForm(atual => ({ ...atual, senhaAtual: e.target.value }))} />
            </Field>
            <Field label="Nova senha" erro={erros.novaSenha} hint="Mínimo de 8 caracteres">
                <Inp type="password" value={form.novaSenha} onChange={e => setForm(atual => ({ ...atual, novaSenha: e.target.value }))} />
            </Field>
            <Field label="Repita a nova senha" erro={erros.confirmacao}>
                <Inp type="password" value={form.confirmacao} onChange={e => setForm(atual => ({ ...atual, confirmacao: e.target.value }))} />
            </Field>
            <ErroGeral texto={erroGeral} />
            <Botoes onCancelar={onFechar} salvando={salvando} texto="Trocar senha" />
        </form>
    )
}

function PreferenciasForm({ preferencias, tipo, onFechar, onSalvar }) {
    const [form, setForm] = useState(preferencias)
    const [erroGeral, setErroGeral] = useState('')
    const [salvando, setSalvando] = useState(false)

    async function enviar(evento) {
        evento.preventDefault()
        setErroGeral('')
        setSalvando(true)
        try {
            await onSalvar(form)
            onFechar()
        } catch (erro) {
            setErroGeral(erro.message)
            setSalvando(false)
        }
    }

    return (
        <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            {tipo === 'notificacoes' ? (
                <>
                    <Interruptor titulo="Lembretes de tarefas" descricao="Avisos sobre as tarefas do dia" ligado={form.notificarTarefas} onChange={valor => setForm(atual => ({ ...atual, notificarTarefas: valor }))} />
                    <Interruptor titulo="Avisos de despesas" descricao="Quando alguém lançar uma despesa com você" ligado={form.notificarDespesas} onChange={valor => setForm(atual => ({ ...atual, notificarDespesas: valor }))} />
                </>
            ) : (
                <>
                    <Field label="Tema">
                        <Sel value={form.tema} onChange={e => setForm(atual => ({ ...atual, tema: e.target.value }))}>
                            <option value="CLARO">Claro</option>
                            <option value="ESCURO">Escuro</option>
                        </Sel>
                    </Field>
                    <Field label="Idioma">
                        <Sel value={form.idioma} onChange={e => setForm(atual => ({ ...atual, idioma: e.target.value }))}>
                            <option value="pt-BR">Português (Brasil)</option>
                            <option value="en-US">English</option>
                            <option value="es-ES">Español</option>
                        </Sel>
                    </Field>
                    <p style={{ fontSize: 13, color: 'var(--color-muted)', background: 'var(--color-bg)', padding: '10px 12px', borderRadius: 8 }}>
                        Sua escolha fica salva na sua conta. Por enquanto o app só tem o tema claro e o português; os outros vão ser aplicados numa próxima versão.
                    </p>
                </>
            )}
            <ErroGeral texto={erroGeral} />
            <Botoes onCancelar={onFechar} salvando={salvando} />
        </form>
    )
}

function ExportarForm({ onFechar }) {
    const toast = useToast()
    const [periodo, setPeriodo] = useState({ inicio: '', fim: '' })
    const [baixando, setBaixando] = useState(false)
    const [erro, setErro] = useState('')

    async function enviar(evento) {
        evento.preventDefault()
        setErro('')
        setBaixando(true)
        try {
            await baixarArquivo('/api/exportacao/historico', periodo, 'historico-morajunto.csv')
            toast('Arquivo baixado')
            onFechar()
        } catch (falha) {
            setErro(falha.message)
            setBaixando(false)
        }
    }

    return (
        <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <p style={{ fontSize: 14, color: 'var(--color-muted)' }}>
                Baixa as despesas e os pagamentos da moradia ativa num arquivo CSV que abre no Excel. Deixe as datas vazias para exportar tudo.
            </p>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16 }}>
                <Field label="De">
                    <Inp type="date" value={periodo.inicio} onChange={e => setPeriodo(atual => ({ ...atual, inicio: e.target.value }))} />
                </Field>
                <Field label="Até">
                    <Inp type="date" value={periodo.fim} onChange={e => setPeriodo(atual => ({ ...atual, fim: e.target.value }))} />
                </Field>
            </div>
            <ErroGeral texto={erro} />
            <Botoes onCancelar={onFechar} salvando={baixando} texto="Baixar CSV" />
        </form>
    )
}

export default function Configuracoes() {
    const { usuario, sair } = useAuth()
    const { ativa } = useMoradia()
    const toast = useToast()
    const [aberto, setAberto] = useState(null)
    const [preferencias, setPreferencias] = useState(null)

    useEffect(() => {
        let ativo = true
        api('/api/usuarios/me/preferencias')
            .then(resposta => {
                if (ativo) {
                    setPreferencias(resposta)
                }
            })
            .catch(erro => {
                if (ativo) {
                    toast(erro.message, 'error')
                }
            })
        return () => {
            ativo = false
        }
    }, [toast])

    async function salvarPreferencias(novas) {
        const salvas = await api('/api/usuarios/me/preferencias', { metodo: 'PUT', corpo: novas })
        setPreferencias(salvas)
        toast('Preferências salvas')
    }

    function abrir(opcao) {
        if ((opcao === 'notificacoes' || opcao === 'aparencia') && !preferencias) {
            toast('Carregando suas preferências, tente de novo em instantes', 'info')
            return
        }
        if (opcao === 'exportar' && !ativa) {
            toast('Selecione uma moradia para exportar o histórico', 'info')
            return
        }
        setAberto(opcao)
    }

    const fechar = () => setAberto(null)
    const titulos = { perfil: 'Perfil', notificacoes: 'Notificações', seguranca: 'Trocar senha', aparencia: 'Aparência', exportar: 'Exportar dados' }

    return (
        <div>
            <PageHeader titulo="Configurações" subtitulo="Ajustes da conta e preferências." />

            <Card style={{ padding: 20, display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
                <FotoPerfil key={usuario.fotoUrl ?? 'sem-foto'} usuario={usuario} tamanho={56} />
                <div>
                    <p style={{ fontWeight: 700, fontSize: 17 }}>{usuario.nome}</p>
                    <p style={{ fontSize: 14, color: 'var(--color-muted)' }}>{usuario.email}</p>
                </div>
            </Card>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <Linha icone="👤" titulo="Perfil" descricao="Nome, e-mail, foto" onClick={() => abrir('perfil')} />
                <Linha icone="🔔" titulo="Notificações" descricao="Lembretes de tarefas e despesas" onClick={() => abrir('notificacoes')} />
                <Linha icone="🔒" titulo="Segurança" descricao="Trocar a senha" onClick={() => abrir('seguranca')} />
                <Linha icone="🎨" titulo="Aparência" descricao="Tema e idioma" onClick={() => abrir('aparencia')} />
                <Linha icone="📄" titulo="Exportar dados" descricao="Baixar histórico em CSV" onClick={() => abrir('exportar')} />
            </div>

            <button onClick={sair} style={{ width: '100%', marginTop: 20, padding: 16, borderRadius: 'var(--radius-card)', border: 'none', background: 'var(--color-danger-light)', color: 'var(--color-danger)', fontWeight: 600, fontSize: 16 }}>
                Sair da conta
            </button>
            <p style={{ textAlign: 'center', fontSize: 13, color: 'var(--color-muted)', marginTop: 16 }}>MoraJunto v1.0.0 · Feito com ♥</p>

            {aberto && (
                <Modal title={titulos[aberto]} onClose={fechar}>
                    {aberto === 'perfil' && <PerfilForm onFechar={fechar} />}
                    {aberto === 'seguranca' && <SenhaForm onFechar={fechar} />}
                    {(aberto === 'notificacoes' || aberto === 'aparencia') && (
                        <PreferenciasForm preferencias={preferencias} tipo={aberto} onFechar={fechar} onSalvar={salvarPreferencias} />
                    )}
                    {aberto === 'exportar' && <ExportarForm onFechar={fechar} />}
                </Modal>
            )}
        </div>
    )
}