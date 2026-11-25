import { useEffect, useRef, useState } from 'react'
import { api } from '../api'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { dataBR, primeiroNome } from '../utils/formato'
import SemMoradia from '../components/SemMoradia'
import { Btn, Card, Carregando, EmptyState, Field, Inp, Modal, PageHeader } from '../components/ui'

function ItemForm({ item, onCancelar, onSalvar }) {
    const [nome, setNome] = useState(item.nome)
    const [quantidade, setQuantidade] = useState(item.quantidade ?? '')
    const [erros, setErros] = useState({})
    const [salvando, setSalvando] = useState(false)

    async function enviar(evento) {
        evento.preventDefault()
        setErros({})
        setSalvando(true)
        try {
            await onSalvar({ nome: nome.trim(), quantidade: quantidade.trim() || null })
        } catch (erro) {
            setErros({ ...erro.erros, geral: Object.keys(erro.erros ?? {}).length === 0 ? erro.message : '' })
            setSalvando(false)
        }
    }

    return (
        <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <Field label="Nome do item *" erro={erros.nome}>
                <Inp value={nome} onChange={e => setNome(e.target.value)} />
            </Field>
            <Field label="Quantidade" erro={erros.quantidade} hint="Texto livre, por exemplo: 2 kg, 1 pacote, 3 unid.">
                <Inp value={quantidade} onChange={e => setQuantidade(e.target.value)} />
            </Field>
            {erros.geral && <p style={{ fontSize: 13, color: 'var(--color-danger)' }}>{erros.geral}</p>}
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <Btn type="button" variant="ghost" onClick={onCancelar}>Cancelar</Btn>
                <Btn type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar'}</Btn>
            </div>
        </form>
    )
}

function Caixa({ marcada, disabled, onClick }) {
    return (
        <button
            onClick={onClick}
            disabled={disabled}
            title={marcada ? 'Voltar para a lista' : 'Marcar como comprado'}
            style={{ width: 24, height: 24, borderRadius: 6, border: `2px solid ${marcada ? 'var(--color-success)' : 'var(--color-border)'}`, background: marcada ? 'var(--color-success)' : '#fff', color: '#fff', fontSize: 13, flexShrink: 0 }}
        >
            {marcada ? '✓' : ''}
        </button>
    )
}

function Quantidade({ texto }) {
    if (!texto) {
        return null
    }
    return (
        <span style={{ fontSize: 12, padding: '3px 10px', borderRadius: 99, background: 'var(--color-bg)', border: '1px solid var(--color-border)', color: 'var(--color-muted)', whiteSpace: 'nowrap' }}>
            {texto}
        </span>
    )
}

export default function Compras() {
    const { ativa } = useMoradia()
    const toast = useToast()
    const [lista, setLista] = useState(null)
    const [versao, setVersao] = useState(0)
    const [novo, setNovo] = useState({ nome: '', quantidade: '' })
    const [editando, setEditando] = useState(null)
    const [ocupado, setOcupado] = useState(false)
    const campoNome = useRef(null)
    const ativaId = ativa?.id

    useEffect(() => {
        if (!ativaId) {
            return
        }
        let ativo = true
        api('/api/compras')
            .then(resposta => {
                if (ativo) {
                    setLista(resposta)
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
    }, [ativaId, versao, toast])

    if (!ativa) {
        return <SemMoradia titulo="Lista de Compras" />
    }
    if (!lista) {
        return <Carregando />
    }

    async function executar(acao, mensagem) {
        setOcupado(true)
        try {
            await acao()
            if (mensagem) {
                toast(mensagem)
            }
            setVersao(atual => atual + 1)
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setOcupado(false)
        }
    }

    async function adicionar(evento) {
        evento.preventDefault()
        if (!novo.nome.trim()) {
            campoNome.current?.focus()
            return
        }
        await executar(
            () => api('/api/compras', { metodo: 'POST', corpo: { nome: novo.nome.trim(), quantidade: novo.quantidade.trim() || null } }),
            'Item adicionado'
        )
        setNovo({ nome: '', quantidade: '' })
        campoNome.current?.focus()
    }

    function alternar(item) {
        executar(() => api(`/api/compras/${item.id}/comprado`, { metodo: item.comprado ? 'DELETE' : 'POST' }))
    }

    function excluir(item) {
        executar(() => api(`/api/compras/${item.id}`, { metodo: 'DELETE' }), `"${item.nome}" removido da lista`)
    }

    async function salvarEdicao(corpo) {
        await api(`/api/compras/${editando.id}`, { metodo: 'PUT', corpo })
        setEditando(null)
        toast('Item atualizado')
        setVersao(atual => atual + 1)
    }

    const pendentes = lista.paraComprar.length

    return (
        <div>
            <PageHeader
                titulo="Lista de Compras"
                subtitulo={pendentes === 0 ? 'Nada para comprar 🎉' : `${pendentes} ${pendentes === 1 ? 'item pendente' : 'itens pendentes'}`}
            />

            <Card style={{ padding: 16, marginBottom: 24 }}>
                <form onSubmit={adicionar} style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                    <Inp ref={campoNome} placeholder="Nome do item..." value={novo.nome} onChange={e => setNovo(atual => ({ ...atual, nome: e.target.value }))} style={{ flex: 3, minWidth: 180 }} />
                    <Inp placeholder="Qtd. (ex: 2 kg)" value={novo.quantidade} onChange={e => setNovo(atual => ({ ...atual, quantidade: e.target.value }))} style={{ flex: 1, minWidth: 120 }} />
                    <Btn type="submit" disabled={ocupado}>+ Adicionar</Btn>
                </form>
            </Card>

            <p style={{ fontSize: 12, fontWeight: 700, color: 'var(--color-muted)', marginBottom: 8 }}>PARA COMPRAR — {pendentes}</p>
            {pendentes === 0 ? (
                <Card>
                    <EmptyState icon="🛒" title="Tudo comprado!" subtitle="Adicione itens acima quando faltar alguma coisa." />
                </Card>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {lista.paraComprar.map(item => (
                        <Card key={item.id} style={{ padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 12 }}>
                            <Caixa marcada={false} disabled={ocupado} onClick={() => alternar(item)} />
                            <div style={{ flex: 1, minWidth: 0 }}>
                                <p style={{ fontSize: 15, fontWeight: 500 }}>{item.nome}</p>
                                <p style={{ fontSize: 12, color: 'var(--color-muted)' }}>Adicionado por {primeiroNome(item.criadoPorNome)} em {dataBR(item.criadoEm)}</p>
                            </div>
                            <Quantidade texto={item.quantidade} />
                            <Btn size="sm" variant="ghost" onClick={() => setEditando(item)} title="Editar">✏️</Btn>
                            <Btn size="sm" variant="danger" disabled={ocupado} onClick={() => excluir(item)} title="Excluir">🗑️</Btn>
                        </Card>
                    ))}
                </div>
            )}

            {lista.comprados.length > 0 && (
                <>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12, margin: '28px 0 10px' }}>
                        <div style={{ flex: 1, height: 1, background: 'var(--color-border)' }} />
                        <span style={{ fontSize: 12, fontWeight: 700, color: 'var(--color-muted)' }}>COMPRADOS — {lista.comprados.length}</span>
                        <div style={{ flex: 1, height: 1, background: 'var(--color-border)' }} />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        {lista.comprados.map(item => (
                            <Card key={item.id} style={{ padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 12, opacity: .7 }}>
                                <Caixa marcada disabled={ocupado} onClick={() => alternar(item)} />
                                <div style={{ flex: 1, minWidth: 0 }}>
                                    <p style={{ fontSize: 15, textDecoration: 'line-through', color: 'var(--color-muted)' }}>{item.nome}</p>
                                    {item.compradoPorNome && (
                                        <p style={{ fontSize: 12, color: 'var(--color-muted)' }}>Comprado por {primeiroNome(item.compradoPorNome)} em {dataBR(item.compradoEm)}</p>
                                    )}
                                </div>
                                <Quantidade texto={item.quantidade} />
                                <Btn size="sm" variant="danger" disabled={ocupado} onClick={() => excluir(item)} title="Excluir">🗑️</Btn>
                            </Card>
                        ))}
                    </div>
                </>
            )}

            {editando && (
                <Modal title="Editar item" onClose={() => setEditando(null)}>
                    <ItemForm item={editando} onCancelar={() => setEditando(null)} onSalvar={salvarEdicao} />
                </Modal>
            )}
        </div>
    )
}