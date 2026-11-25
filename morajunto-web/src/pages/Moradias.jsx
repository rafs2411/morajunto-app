import { useState } from 'react'
import { api } from '../api'
import { useAuth } from '../hooks/useAuth'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { Badge, Btn, Card, ConfirmDialog, EmptyState, Field, Inp, Modal, PageHeader } from '../components/ui'

const FORM_VAZIO = { nome: '', endereco: '', disponivelParaEntrada: false, vagas: '' }

function MoradiaForm({ inicial, onCancelar, onSalvar }) {
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
            await onSalvar({
                nome: form.nome.trim(),
                endereco: form.endereco.trim(),
                disponivelParaEntrada: form.disponivelParaEntrada,
                vagas: form.disponivelParaEntrada && form.vagas !== '' ? Number(form.vagas) : null
            })
        } catch (erro) {
            const errosCampos = erro.erros ?? {}
            setErros(errosCampos)
            setErroGeral(Object.keys(errosCampos).length === 0 ? erro.message : '')
            setSalvando(false)
        }
    }

    return (
        <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <Field label="Nome da moradia *" erro={erros.nome}>
                <Inp placeholder="Casa da Galera" value={form.nome} onChange={e => mudar('nome', e.target.value)} />
            </Field>
            <Field label="Endereço *" erro={erros.endereco}>
                <Inp placeholder="Rua das Flores, 42 – Bairro" value={form.endereco} onChange={e => mudar('endereco', e.target.value)} />
            </Field>

            <label style={{ display: 'flex', alignItems: 'center', gap: 10, fontSize: 14, cursor: 'pointer' }}>
                <input
                    type="checkbox"
                    checked={form.disponivelParaEntrada}
                    onChange={e => mudar('disponivelParaEntrada', e.target.checked)}
                    style={{ width: 18, height: 18, accentColor: 'var(--color-primary)' }}
                />
                Aberta para novos moradores
            </label>

            {form.disponivelParaEntrada && (
                <Field label="Vagas" erro={erros.vagas} hint="Quantas pessoas ainda podem entrar. Deixe vazio para não ter limite.">
                    <Inp type="number" min="1" max="50" placeholder="Sem limite" value={form.vagas} onChange={e => mudar('vagas', e.target.value)} />
                </Field>
            )}

            {erroGeral && <p style={{ fontSize: 13, color: 'var(--color-danger)' }}>{erroGeral}</p>}

            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', marginTop: 4 }}>
                <Btn type="button" variant="ghost" onClick={onCancelar}>Cancelar</Btn>
                <Btn type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar'}</Btn>
            </div>
        </form>
    )
}

function MoradiaCard({ moradia, ehDono, onSelecionar, onEditar, onExcluir }) {
    return (
        <Card style={{ padding: 20, border: moradia.ativa ? '2px solid var(--color-primary)' : '1px solid var(--color-border)', display: 'flex', flexDirection: 'column', gap: 12 }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 8 }}>
                <div style={{ minWidth: 0 }}>
                    <h3 style={{ fontFamily: 'var(--font-display)', fontSize: 20, fontWeight: 600, marginBottom: 4 }}>{moradia.nome}</h3>
                    <p style={{ fontSize: 14, color: 'var(--color-muted)' }}>{moradia.endereco}</p>
                </div>
                {moradia.ativa && <span style={{ fontSize: 12, fontWeight: 700, color: 'var(--color-primary)' }}>Ativa</span>}
            </div>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, alignItems: 'center' }}>
                <span style={{ fontSize: 14, color: 'var(--color-muted)', marginRight: 4 }}>
                    {moradia.totalMoradores} {moradia.totalMoradores === 1 ? 'morador' : 'moradores'}
                </span>
                <Badge label={ehDono ? 'Dono' : 'Morador'} cor={ehDono ? '#6C63FF' : '#6B7280'} />
                {moradia.disponivelParaEntrada && (
                    <Badge label={moradia.vagas === null ? 'Aberta · sem limite' : `Aberta · ${moradia.vagas} ${moradia.vagas === 1 ? 'vaga' : 'vagas'}`} cor="#10B981" />
                )}
            </div>

            <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 4 }}>
                <Btn full variant={moradia.ativa ? 'primary' : 'secondary'} disabled={moradia.ativa} onClick={onSelecionar} style={{ opacity: 1, cursor: moradia.ativa ? 'default' : 'pointer' }}>
                    {moradia.ativa ? '✓ Selecionada' : 'Selecionar'}
                </Btn>
                {ehDono && (
                    <>
                        <Btn variant="ghost" onClick={onEditar} title="Editar">✏️</Btn>
                        <Btn variant="danger" onClick={onExcluir} title="Excluir">🗑️</Btn>
                    </>
                )}
            </div>
        </Card>
    )
}

export default function Moradias() {
    const { usuario } = useAuth()
    const { moradias, recarregar, selecionar } = useMoradia()
    const toast = useToast()
    const [modal, setModal] = useState(null)
    const [excluindo, setExcluindo] = useState(null)

    async function salvar(dados) {
        if (modal.moradia) {
            await api(`/api/moradias/${modal.moradia.id}`, { metodo: 'PUT', corpo: dados })
            toast('Moradia atualizada')
        } else {
            await api('/api/moradias', { metodo: 'POST', corpo: dados })
            toast('Moradia criada e selecionada')
        }
        await recarregar()
        setModal(null)
    }

    async function trocar(moradia) {
        try {
            await selecionar(moradia.id)
            toast(`Agora a moradia ativa é ${moradia.nome}`)
        } catch (erro) {
            toast(erro.message, 'error')
        }
    }

    async function excluir() {
        try {
            await api(`/api/moradias/${excluindo.id}`, { metodo: 'DELETE' })
            toast('Moradia excluída')
            await recarregar()
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setExcluindo(null)
        }
    }

    function abrirEdicao(moradia) {
        setModal({
            moradia,
            inicial: {
                nome: moradia.nome,
                endereco: moradia.endereco,
                disponivelParaEntrada: moradia.disponivelParaEntrada,
                vagas: moradia.vagas ?? ''
            }
        })
    }

    return (
        <div>
            <PageHeader
                titulo="Minhas Moradias"
                subtitulo="Gerencie todas as suas moradias compartilhadas."
                acao={<Btn onClick={() => setModal({ moradia: null, inicial: FORM_VAZIO })}>+ Nova Moradia</Btn>}
            />

            {moradias.length === 0 ? (
                <Card>
                    <EmptyState
                        icon="🏠"
                        title="Você ainda não faz parte de nenhuma moradia"
                        subtitle="Crie a sua moradia ou peça para o dono de uma moradia te convidar pelo seu e-mail."
                        action={<Btn onClick={() => setModal({ moradia: null, inicial: FORM_VAZIO })}>Criar minha moradia</Btn>}
                    />
                </Card>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 16 }}>
                    {moradias.map(moradia => (
                        <MoradiaCard
                            key={moradia.id}
                            moradia={moradia}
                            ehDono={moradia.donoId === usuario.id}
                            onSelecionar={() => trocar(moradia)}
                            onEditar={() => abrirEdicao(moradia)}
                            onExcluir={() => setExcluindo(moradia)}
                        />
                    ))}
                </div>
            )}

            {modal && (
                <Modal title={modal.moradia ? 'Editar Moradia' : 'Nova Moradia'} onClose={() => setModal(null)}>
                    <MoradiaForm inicial={modal.inicial} onCancelar={() => setModal(null)} onSalvar={salvar} />
                </Modal>
            )}

            {excluindo && (
                <ConfirmDialog
                    msg={`Excluir "${excluindo.nome}"? Todas as despesas, tarefas e compras dela também serão apagadas.`}
                    onConfirm={excluir}
                    onCancel={() => setExcluindo(null)}
                />
            )}
        </div>
    )
}