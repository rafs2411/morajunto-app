import { useEffect, useState } from 'react'
import { api } from '../api'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { DIAS_CURTOS, dataBR, horaCurta, hojeISO, paraISO, primeiroNome } from '../utils/formato'
import SemMoradia from '../components/SemMoradia'
import TarefaForm from '../components/tarefas/TarefaForm'
import { Badge, Btn, Card, Carregando, ConfirmDialog, EmptyState, Modal, PageHeader, Pill } from '../components/ui'

const ABAS = [['TODAS', 'Todas'], ['PENDENTES', 'Pendentes hoje'], ['CONCLUIDAS', 'Concluídas hoje']]

function proximaData(tarefa, hoje) {
    if (tarefa.recorrencia === 'UNICA') {
        return tarefa.dataInicio >= hoje ? tarefa.dataInicio : null
    }
    const dia = new Date(`${tarefa.dataInicio > hoje ? tarefa.dataInicio : hoje}T00:00:00`)
    for (let i = 0; i < 7; i++) {
        const iso = paraISO(dia)
        if (tarefa.dataFim && iso > tarefa.dataFim) {
            return null
        }
        if (tarefa.diasSemana.includes(dia.getDay())) {
            return iso
        }
        dia.setDate(dia.getDate() + 1)
    }
    return null
}

function textoProxima(data, hoje) {
    if (!data) {
        return 'Encerrada'
    }
    if (data === hoje) {
        return 'Hoje'
    }
    const diaSemana = DIAS_CURTOS[new Date(`${data}T00:00:00`).getDay()]
    return `Próxima: ${diaSemana}, ${dataBR(data).slice(0, 5)}`
}

function TarefaItem({ tarefa, deHoje, hoje, ocupado, onConcluir, onEditar, onExcluir }) {
    const feita = deHoje?.concluida
    const proxima = textoProxima(proximaData(tarefa, hoje), hoje)

    return (
        <Card style={{ padding: '16px 20px', display: 'flex', alignItems: 'flex-start', gap: 14, borderLeft: `4px solid ${tarefa.responsavelCor}`, opacity: feita ? .7 : 1 }}>
            {deHoje ? (
                <button
                    onClick={onConcluir}
                    disabled={ocupado}
                    title={feita ? 'Desmarcar' : 'Marcar como feita hoje'}
                    style={{ width: 26, height: 26, borderRadius: '50%', border: `2px solid ${feita ? 'var(--color-success)' : tarefa.responsavelCor}`, background: feita ? 'var(--color-success)' : '#fff', color: '#fff', fontSize: 14, flexShrink: 0, marginTop: 2 }}
                >
                    {feita ? '✓' : ''}
                </button>
            ) : (
                <div style={{ width: 26, flexShrink: 0 }} />
            )}

            <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontWeight: 600, fontSize: 16, textDecoration: feita ? 'line-through' : 'none', color: feita ? 'var(--color-muted)' : 'var(--color-text)' }}>{tarefa.nome}</p>
                {tarefa.descricao && <p style={{ fontSize: 13, color: 'var(--color-muted)', marginTop: 2 }}>{tarefa.descricao}</p>}
                <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 10, marginTop: 8 }}>
                    <Pill label={primeiroNome(tarefa.responsavelNome)} cor={tarefa.responsavelCor} size="xs" />
                    {tarefa.horario && <span style={{ fontSize: 13, fontWeight: 600 }}>{horaCurta(tarefa.horario)}</span>}
                    {tarefa.recorrencia === 'SEMANAL' ? (
                        <>
                            <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--color-primary)' }}>{tarefa.diasSemana.map(dia => DIAS_CURTOS[dia]).join(', ')}</span>
                            <Badge label="Semanal" cor="#10B981" />
                        </>
                    ) : (
                        <Badge label={`Única · ${dataBR(tarefa.dataInicio).slice(0, 5)}`} cor="#8B5CF6" />
                    )}
                    <span style={{ fontSize: 12, color: 'var(--color-muted)' }}>{proxima}</span>
                    {feita && deHoje.concluidaPorNome && <span style={{ fontSize: 12, color: 'var(--color-success)' }}>Feita por {primeiroNome(deHoje.concluidaPorNome)}</span>}
                </div>
            </div>

            <div style={{ display: 'flex', gap: 6 }}>
                <Btn size="sm" variant="ghost" onClick={onEditar} title="Editar">✏️</Btn>
                <Btn size="sm" variant="danger" onClick={onExcluir} title="Excluir">🗑️</Btn>
            </div>
        </Card>
    )
}

export default function Tarefas() {
    const { ativa } = useMoradia()
    const toast = useToast()
    const [dados, setDados] = useState(null)
    const [versao, setVersao] = useState(0)
    const [aba, setAba] = useState('TODAS')
    const [filtroMorador, setFiltroMorador] = useState(null)
    const [modal, setModal] = useState(null)
    const [excluindo, setExcluindo] = useState(null)
    const [ocupado, setOcupado] = useState(false)
    const ativaId = ativa?.id
    const hoje = hojeISO()

    useEffect(() => {
        if (!ativaId) {
            return
        }
        let ativo = true
        Promise.all([
            api('/api/tarefas'),
            api('/api/moradores'),
            api('/api/tarefas/calendario', { params: { inicio: hoje, fim: hoje } })
        ])
            .then(([tarefas, moradores, deHoje]) => {
                if (ativo) {
                    setDados({ tarefas, moradores, deHoje: Object.fromEntries(deHoje.map(item => [item.tarefaId, item])) })
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
    }, [ativaId, versao, hoje, toast])

    if (!ativa) {
        return <SemMoradia titulo="Tarefas Domésticas" />
    }
    if (!dados) {
        return <Carregando />
    }

    const pendentesHoje = Object.values(dados.deHoje).filter(item => !item.concluida).length
    const visiveis = dados.tarefas.filter(tarefa => {
        const deHoje = dados.deHoje[tarefa.id]
        if (filtroMorador && tarefa.responsavelId !== filtroMorador) {
            return false
        }
        if (aba === 'PENDENTES') {
            return deHoje && !deHoje.concluida
        }
        if (aba === 'CONCLUIDAS') {
            return deHoje && deHoje.concluida
        }
        return true
    })

    async function alternarConclusao(tarefa) {
        const feita = dados.deHoje[tarefa.id]?.concluida
        setOcupado(true)
        try {
            await api(`/api/tarefas/${tarefa.id}/concluir`, { metodo: feita ? 'DELETE' : 'POST', params: { data: hoje } })
            toast(feita ? 'Tarefa desmarcada' : 'Tarefa concluída!')
            setVersao(atual => atual + 1)
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setOcupado(false)
        }
    }

    async function salvar(corpo) {
        if (modal.tarefa) {
            await api(`/api/tarefas/${modal.tarefa.id}`, { metodo: 'PUT', corpo })
            toast('Tarefa atualizada')
        } else {
            await api('/api/tarefas', { metodo: 'POST', corpo })
            toast('Tarefa criada')
        }
        setModal(null)
        setVersao(atual => atual + 1)
    }

    async function excluir() {
        try {
            await api(`/api/tarefas/${excluindo.id}`, { metodo: 'DELETE' })
            toast('Tarefa excluída')
            setVersao(atual => atual + 1)
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setExcluindo(null)
        }
    }

    function abrirEdicao(tarefa) {
        setModal({
            tarefa,
            inicial: {
                nome: tarefa.nome,
                descricao: tarefa.descricao ?? '',
                responsavelId: tarefa.responsavelId,
                horario: horaCurta(tarefa.horario),
                dataInicio: tarefa.dataInicio,
                dataFim: tarefa.recorrencia === 'SEMANAL' ? tarefa.dataFim ?? '' : '',
                repetir: tarefa.recorrencia === 'SEMANAL',
                diasSemana: tarefa.diasSemana
            }
        })
    }

    return (
        <div>
            <PageHeader
                titulo="Tarefas Domésticas"
                subtitulo={pendentesHoje === 0 ? 'Nada pendente para hoje 🎉' : `${pendentesHoje} ${pendentesHoje === 1 ? 'pendente' : 'pendentes'} hoje`}
                acao={<Btn onClick={() => setModal({ tarefa: null, inicial: null })}>+ Nova Tarefa</Btn>}
            />

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 16 }}>
                {dados.moradores.map(morador => (
                    <button
                        key={morador.id}
                        onClick={() => setFiltroMorador(filtroMorador === morador.id ? null : morador.id)}
                        style={{ border: 'none', background: 'none', padding: 0, opacity: filtroMorador && filtroMorador !== morador.id ? .35 : 1 }}
                    >
                        <Pill label={primeiroNome(morador.nome)} cor={morador.cor} />
                    </button>
                ))}
            </div>

            <div style={{ display: 'inline-flex', gap: 4, background: 'var(--color-border)', borderRadius: 10, padding: 4, marginBottom: 20, flexWrap: 'wrap' }}>
                {ABAS.map(([valor, texto]) => (
                    <button
                        key={valor}
                        onClick={() => setAba(valor)}
                        style={{ padding: '8px 16px', borderRadius: 7, border: 'none', background: aba === valor ? '#fff' : 'transparent', fontWeight: 600, fontSize: 14, color: aba === valor ? 'var(--color-text)' : 'var(--color-muted)', boxShadow: aba === valor ? '0 1px 4px rgba(0,0,0,.1)' : undefined }}
                    >
                        {texto}
                    </button>
                ))}
            </div>

            {visiveis.length === 0 ? (
                <Card>
                    <EmptyState icon="✓" title="Nenhuma tarefa por aqui" subtitle={aba === 'TODAS' ? 'Crie a primeira tarefa da casa.' : 'Nenhuma tarefa de hoje com esse filtro.'} />
                </Card>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                    {visiveis.map(tarefa => (
                        <TarefaItem
                            key={tarefa.id}
                            tarefa={tarefa}
                            deHoje={dados.deHoje[tarefa.id]}
                            hoje={hoje}
                            ocupado={ocupado}
                            onConcluir={() => alternarConclusao(tarefa)}
                            onEditar={() => abrirEdicao(tarefa)}
                            onExcluir={() => setExcluindo(tarefa)}
                        />
                    ))}
                </div>
            )}

            {modal && (
                <Modal title={modal.tarefa ? 'Editar Tarefa' : 'Nova Tarefa'} onClose={() => setModal(null)}>
                    <TarefaForm inicial={modal.inicial} moradores={dados.moradores} onCancelar={() => setModal(null)} onSalvar={salvar} />
                </Modal>
            )}

            {excluindo && (
                <ConfirmDialog
                    msg={`Excluir a tarefa "${excluindo.nome}"? Ela some da lista e do calendário.`}
                    onConfirm={excluir}
                    onCancel={() => setExcluindo(null)}
                />
            )}
        </div>
    )
}