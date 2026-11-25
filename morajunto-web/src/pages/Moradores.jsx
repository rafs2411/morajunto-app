import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../hooks/useAuth'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { PALETA, nomeDaCor } from '../utils/cores'
import SemMoradia from '../components/SemMoradia'
import { Avatar, Btn, Card, Carregando, ConfirmDialog, Field, Inp, Modal, PageHeader } from '../components/ui'

function SeletorCor({ valor, usadas, onChange }) {
    return (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
            {PALETA.map(cor => {
                const usada = usadas.includes(cor.hex)
                const escolhida = valor === cor.hex
                return (
                    <button
                        key={cor.hex}
                        type="button"
                        title={usada ? `${cor.nome} (em uso)` : cor.nome}
                        disabled={usada}
                        onClick={() => onChange(cor.hex)}
                        style={{ width: 34, height: 34, borderRadius: '50%', background: cor.hex, border: escolhida ? '3px solid var(--color-text)' : '3px solid transparent', outline: escolhida ? '2px solid #fff' : 'none', outlineOffset: -5, opacity: usada ? .25 : 1 }}
                    />
                )
            })}
        </div>
    )
}

function MoradorForm({ inicial, usadas, onCancelar, onSalvar }) {
    const [form, setForm] = useState(inicial)
    const [erros, setErros] = useState({})
    const [erroGeral, setErroGeral] = useState('')
    const [salvando, setSalvando] = useState(false)

    function mudar(campo, valor) {
        setForm(atual => ({ ...atual, [campo]: valor }))
    }

    async function enviar(evento) {
        evento.preventDefault()
        setErros({})
        setErroGeral('')
        setSalvando(true)
        try {
            await onSalvar({ nome: form.nome.trim(), email: form.email.trim() || null, cor: form.cor })
        } catch (erro) {
            const errosCampos = erro.erros ?? {}
            setErros(errosCampos)
            setErroGeral(Object.keys(errosCampos).length === 0 ? erro.message : '')
            setSalvando(false)
        }
    }

    return (
        <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <Field label="Nome completo *" erro={erros.nome}>
                <Inp placeholder="Maria Santos" value={form.nome} onChange={e => mudar('nome', e.target.value)} />
            </Field>
            <Field label="E-mail" erro={erros.email} hint="Opcional. Se a pessoa criar uma conta com esse e-mail depois, ela é ligada a este cadastro.">
                <Inp type="email" placeholder="maria@email.com" value={form.email} onChange={e => mudar('email', e.target.value)} />
            </Field>
            <Field label="Cor do morador" erro={erros.cor}>
                <SeletorCor valor={form.cor} usadas={usadas} onChange={cor => mudar('cor', cor)} />
            </Field>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Avatar nome={form.nome || '?'} cor={form.cor} size={40} />
                <span style={{ fontSize: 14, color: 'var(--color-muted)' }}>Prévia do avatar</span>
            </div>

            {erroGeral && <p style={{ fontSize: 13, color: 'var(--color-danger)' }}>{erroGeral}</p>}

            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <Btn type="button" variant="ghost" onClick={onCancelar}>Cancelar</Btn>
                <Btn type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar'}</Btn>
            </div>
        </form>
    )
}

function ConviteForm({ onCancelar, onEnviar }) {
    const [email, setEmail] = useState('')
    const [erro, setErro] = useState('')
    const [enviando, setEnviando] = useState(false)

    async function enviar(evento) {
        evento.preventDefault()
        setErro('')
        setEnviando(true)
        try {
            await onEnviar(email.trim())
        } catch (falha) {
            setErro(falha.erros?.email ?? falha.message)
            setEnviando(false)
        }
    }

    return (
        <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <p style={{ fontSize: 14, color: 'var(--color-muted)' }}>
                A pessoa vê o convite na tela de Moradias quando entrar com este e-mail, mesmo que ainda não tenha conta. O convite vale por 7 dias.
            </p>
            <Field label="E-mail da pessoa *" erro={erro}>
                <Inp type="email" placeholder="amigo@email.com" value={email} onChange={e => setEmail(e.target.value)} />
            </Field>
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <Btn type="button" variant="ghost" onClick={onCancelar}>Cancelar</Btn>
                <Btn type="submit" disabled={enviando}>{enviando ? 'Enviando...' : 'Enviar convite'}</Btn>
            </div>
        </form>
    )
}

export default function Moradores() {
    const { usuario } = useAuth()
    const { ativa, ehDono, recarregar } = useMoradia()
    const toast = useToast()
    const [moradores, setMoradores] = useState([])
    const [carregando, setCarregando] = useState(true)
    const [versao, setVersao] = useState(0)
    const [modal, setModal] = useState(null)
    const [convidando, setConvidando] = useState(false)
    const [removendo, setRemovendo] = useState(null)
    const ativaId = ativa?.id

    useEffect(() => {
        if (!ativaId) {
            return
        }
        let ativo = true
        api('/api/moradores')
            .then(lista => {
                if (ativo) {
                    setMoradores(lista)
                }
            })
            .catch(erro => {
                if (ativo) {
                    toast(erro.message, 'error')
                }
            })
            .finally(() => {
                if (ativo) {
                    setCarregando(false)
                }
            })
        return () => {
            ativo = false
        }
    }, [ativaId, versao, toast])

    if (!ativa) {
        return <SemMoradia titulo="Moradores" />
    }

    function coresUsadas(idIgnorado) {
        return moradores.filter(morador => morador.id !== idIgnorado).map(morador => morador.cor.toUpperCase())
    }

    function abrirNovo() {
        const usadas = coresUsadas(null)
        const livre = PALETA.find(cor => !usadas.includes(cor.hex))?.hex ?? PALETA[0].hex
        setModal({ morador: null, inicial: { nome: '', email: '', cor: livre } })
    }

    function abrirEdicao(morador) {
        setModal({ morador, inicial: { nome: morador.nome, email: morador.email ?? '', cor: morador.cor.toUpperCase() } })
    }

    async function salvar(dados) {
        if (modal.morador) {
            await api(`/api/moradores/${modal.morador.id}`, { metodo: 'PUT', corpo: dados })
            toast('Morador atualizado')
        } else {
            await api('/api/moradores', { metodo: 'POST', corpo: dados })
            toast('Morador adicionado')
        }
        setModal(null)
        setVersao(atual => atual + 1)
        await recarregar()
    }

    async function convidar(email) {
        await api('/api/convites', { metodo: 'POST', corpo: { email } })
        setConvidando(false)
        toast(`Convite enviado para ${email}`)
    }

    async function remover() {
        try {
            await api(`/api/moradores/${removendo.id}`, { metodo: 'DELETE' })
            toast(`${removendo.nome} saiu da moradia`)
            setVersao(atual => atual + 1)
            await recarregar()
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setRemovendo(null)
        }
    }

    return (
        <div>
            <PageHeader
                titulo="Moradores"
                subtitulo={`${moradores.length} ${moradores.length === 1 ? 'morador' : 'moradores'} nesta moradia.`}
                acao={ehDono && (
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                        <Btn variant="secondary" onClick={() => setConvidando(true)}>✉️ Convidar por e-mail</Btn>
                        <Btn onClick={abrirNovo}>+ Adicionar</Btn>
                    </div>
                )}
            />

            {carregando ? <Carregando /> : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }}>
                    {moradores.map(morador => {
                        const souEu = morador.email && morador.email === usuario.email
                        return (
                            <Card key={morador.id} style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                                    <Avatar nome={morador.nome} cor={morador.cor} size={56} />
                                    <div style={{ minWidth: 0 }}>
                                        <p style={{ fontWeight: 700, fontSize: 16 }}>{morador.nome}{souEu && <span style={{ fontWeight: 500, color: 'var(--color-muted)', fontSize: 13 }}> (você)</span>}</p>
                                        <p style={{ fontSize: 13, color: 'var(--color-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{morador.email ?? 'Sem e-mail'}</p>
                                        <p style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, fontWeight: 600, color: morador.cor, marginTop: 6 }}>
                                            <span style={{ width: 8, height: 8, borderRadius: '50%', background: morador.cor }} />
                                            {nomeDaCor(morador.cor)}
                                        </p>
                                    </div>
                                </div>
                                {ehDono && (
                                    <div style={{ display: 'flex', gap: 8 }}>
                                        <Btn full variant="secondary" size="sm" onClick={() => abrirEdicao(morador)}>Editar</Btn>
                                        {!souEu && <Btn full variant="danger" size="sm" onClick={() => setRemovendo(morador)}>Remover</Btn>}
                                    </div>
                                )}
                            </Card>
                        )
                    })}
                </div>
            )}

            {modal && (
                <Modal title={modal.morador ? 'Editar Morador' : 'Novo Morador'} onClose={() => setModal(null)}>
                    <MoradorForm inicial={modal.inicial} usadas={coresUsadas(modal.morador?.id)} onCancelar={() => setModal(null)} onSalvar={salvar} />
                </Modal>
            )}

            {convidando && (
                <Modal title="Convidar por e-mail" onClose={() => setConvidando(false)}>
                    <ConviteForm onCancelar={() => setConvidando(false)} onEnviar={convidar} />
                </Modal>
            )}

            {removendo && (
                <ConfirmDialog
                    titulo="Remover morador"
                    textoConfirmar="Remover"
                    msg={`${removendo.nome} vai sair da moradia. As despesas e o histórico continuam salvos, e a cor dele fica reservada.`}
                    onConfirm={remover}
                    onCancel={() => setRemovendo(null)}
                />
            )}
        </div>
    )
}