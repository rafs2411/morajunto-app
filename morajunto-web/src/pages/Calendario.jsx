import { useEffect, useState } from 'react'
import { api } from '../api'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { DIAS_CURTOS, MESES, horaCurta, hojeISO, mesISO, nomeDoMes, paraISO, primeiroNome, somarMes } from '../utils/formato'
import SemMoradia from '../components/SemMoradia'
import { Card, Carregando, PageHeader, Pill } from '../components/ui'

function somarDias(iso, dias) {
    const data = new Date(`${iso}T00:00:00`)
    data.setDate(data.getDate() + dias)
    return paraISO(data)
}

function inicioDaSemana(iso) {
    const data = new Date(`${iso}T00:00:00`)
    return somarDias(iso, -data.getDay())
}

function tituloSemana(inicio) {
    const fim = somarDias(inicio, 6)
    const [, mesInicio, diaInicio] = inicio.split('-').map(Number)
    const [anoFim, mesFim, diaFim] = fim.split('-').map(Number)
    const nomeMesFim = MESES[mesFim - 1].slice(0, 3).toLowerCase()
    if (mesInicio === mesFim) {
        return `${diaInicio} – ${diaFim} ${nomeMesFim} ${anoFim}`
    }
    return `${diaInicio} ${MESES[mesInicio - 1].slice(0, 3).toLowerCase()} – ${diaFim} ${nomeMesFim} ${anoFim}`
}

function ItemCalendario({ item, compacto, ocupado, onClick }) {
    return (
        <button
            onClick={onClick}
            disabled={ocupado}
            title={`${item.nome} · ${item.responsavelNome}${item.concluida ? ' · feita' : ''}`}
            style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 4, padding: compacto ? '3px 6px' : '6px 8px', borderRadius: 6, border: 'none', background: item.concluida ? 'var(--color-bg)' : item.responsavelCor + '18', color: item.concluida ? 'var(--color-muted)' : item.responsavelCor, fontSize: compacto ? 11 : 12, fontWeight: 600, textAlign: 'left', marginBottom: 3 }}
        >
            {item.concluida && <span>✓</span>}
            {!compacto && item.horario && <span style={{ opacity: .8 }}>{horaCurta(item.horario)}</span>}
            <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', textDecoration: item.concluida ? 'line-through' : 'none' }}>{item.nome}</span>
            <span style={{ width: 6, height: 6, borderRadius: '50%', background: item.concluida ? 'var(--color-muted)' : item.responsavelCor, flexShrink: 0 }} />
        </button>
    )
}

function BotaoSeta({ onClick, children }) {
    return (
        <button onClick={onClick} style={{ width: 36, height: 36, borderRadius: 10, border: '1px solid var(--color-border)', background: '#fff', fontSize: 16 }}>
            {children}
        </button>
    )
}

export default function Calendario() {
    const { ativa } = useMoradia()
    const toast = useToast()
    const hoje = hojeISO()
    const [visao, setVisao] = useState('MES')
    const [mes, setMes] = useState(mesISO())
    const [semana, setSemana] = useState(inicioDaSemana(hoje))
    const [itens, setItens] = useState(null)
    const [moradores, setMoradores] = useState([])
    const [filtro, setFiltro] = useState(null)
    const [versao, setVersao] = useState(0)
    const [ocupado, setOcupado] = useState(false)
    const ativaId = ativa?.id

    useEffect(() => {
        if (!ativaId) {
            return
        }
        let ativo = true
        const busca = visao === 'MES'
            ? api('/api/tarefas/calendario/mes', { params: { mes } })
            : api('/api/tarefas/calendario', { params: { inicio: semana, fim: somarDias(semana, 6) } })

        Promise.all([busca, api('/api/moradores')])
            .then(([lista, listaMoradores]) => {
                if (ativo) {
                    setItens(lista)
                    setMoradores(listaMoradores)
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
    }, [ativaId, visao, mes, semana, versao, toast])

    if (!ativa) {
        return <SemMoradia titulo="Calendário" />
    }
    if (!itens) {
        return <Carregando />
    }

    const visiveis = filtro ? itens.filter(item => item.responsavelId === filtro) : itens
    const porDia = visiveis.reduce((mapa, item) => {
        mapa[item.data] = [...(mapa[item.data] ?? []), item]
        return mapa
    }, {})

    async function alternar(item) {
        setOcupado(true)
        try {
            await api(`/api/tarefas/${item.tarefaId}/concluir`, { metodo: item.concluida ? 'DELETE' : 'POST', params: { data: item.data } })
            toast(item.concluida ? 'Tarefa desmarcada' : 'Tarefa concluída!')
            setVersao(atual => atual + 1)
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setOcupado(false)
        }
    }

    function navegar(direcao) {
        if (visao === 'MES') {
            setMes(somarMes(mes, direcao))
        } else {
            setSemana(somarDias(semana, direcao * 7))
        }
    }

    function irParaHoje() {
        setMes(mesISO())
        setSemana(inicioDaSemana(hoje))
    }

    const [ano, numeroMes] = mes.split('-').map(Number)
    const primeiroDia = new Date(ano, numeroMes - 1, 1)
    const diasNoMes = new Date(ano, numeroMes, 0).getDate()
    const celulasMes = [
        ...Array(primeiroDia.getDay()).fill(null),
        ...Array.from({ length: diasNoMes }, (_, indice) => paraISO(new Date(ano, numeroMes - 1, indice + 1)))
    ]
    const diasSemana = Array.from({ length: 7 }, (_, indice) => somarDias(semana, indice))

    return (
        <div>
            <PageHeader
                titulo="Calendário"
                subtitulo="Clique numa tarefa para marcar ou desmarcar naquele dia."
                acao={
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                        <div style={{ display: 'flex', gap: 4, background: 'var(--color-border)', borderRadius: 10, padding: 4 }}>
                            {[['MES', 'Mensal'], ['SEMANA', 'Semanal']].map(([valor, texto]) => (
                                <button
                                    key={valor}
                                    onClick={() => setVisao(valor)}
                                    style={{ padding: '7px 16px', borderRadius: 7, border: 'none', background: visao === valor ? '#fff' : 'transparent', fontWeight: 600, fontSize: 14, color: visao === valor ? 'var(--color-text)' : 'var(--color-muted)', boxShadow: visao === valor ? '0 1px 4px rgba(0,0,0,.1)' : undefined }}
                                >
                                    {texto}
                                </button>
                            ))}
                        </div>
                        <BotaoSeta onClick={() => navegar(-1)}>‹</BotaoSeta>
                        <button onClick={irParaHoje} title="Voltar para hoje" style={{ border: 'none', background: 'none', fontWeight: 600, fontSize: 15, minWidth: 150 }}>
                            {visao === 'MES' ? nomeDoMes(mes) : tituloSemana(semana)}
                        </button>
                        <BotaoSeta onClick={() => navegar(1)}>›</BotaoSeta>
                    </div>
                }
            />

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 16 }}>
                {moradores.map(morador => (
                    <button
                        key={morador.id}
                        onClick={() => setFiltro(filtro === morador.id ? null : morador.id)}
                        style={{ border: 'none', background: 'none', padding: 0, opacity: filtro && filtro !== morador.id ? .35 : 1 }}
                    >
                        <Pill label={primeiroNome(morador.nome)} cor={morador.cor} />
                    </button>
                ))}
            </div>

            <Card style={{ overflow: 'hidden' }}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, minmax(0, 1fr))', borderBottom: '1px solid var(--color-border)' }}>
                    {DIAS_CURTOS.map(dia => (
                        <div key={dia} style={{ padding: '12px 4px', textAlign: 'center', fontSize: 13, fontWeight: 600, color: 'var(--color-muted)' }}>{dia}</div>
                    ))}
                </div>

                {visao === 'MES' ? (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, minmax(0, 1fr))' }}>
                        {celulasMes.map((data, indice) => {
                            const doDia = data ? porDia[data] ?? [] : []
                            const ehHoje = data === hoje
                            return (
                                <div key={data ?? `vazio-${indice}`} style={{ minHeight: 96, padding: 6, borderRight: (indice + 1) % 7 === 0 ? 'none' : '1px solid var(--color-border)', borderBottom: '1px solid var(--color-border)', background: ehHoje ? 'var(--color-primary-light)' : 'transparent' }}>
                                    {data && (
                                        <>
                                            <div style={{ width: 24, height: 24, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 600, marginBottom: 4, background: ehHoje ? 'var(--color-primary)' : 'transparent', color: ehHoje ? '#fff' : 'var(--color-text)' }}>
                                                {Number(data.slice(8))}
                                            </div>
                                            {doDia.slice(0, 3).map(item => (
                                                <ItemCalendario key={item.tarefaId} item={item} compacto ocupado={ocupado} onClick={() => alternar(item)} />
                                            ))}
                                            {doDia.length > 3 && (
                                                <button
                                                    onClick={() => { setSemana(inicioDaSemana(data)); setVisao('SEMANA') }}
                                                    style={{ border: 'none', background: 'none', fontSize: 11, fontWeight: 600, color: 'var(--color-primary)', padding: '0 6px' }}
                                                >
                                                    +{doDia.length - 3}
                                                </button>
                                            )}
                                        </>
                                    )}
                                </div>
                            )
                        })}
                    </div>
                ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, minmax(0, 1fr))' }}>
                        {diasSemana.map((data, indice) => {
                            const doDia = porDia[data] ?? []
                            const ehHoje = data === hoje
                            return (
                                <div key={data} style={{ minHeight: 320, padding: 8, borderRight: indice === 6 ? 'none' : '1px solid var(--color-border)', background: ehHoje ? 'var(--color-primary-light)' : 'transparent' }}>
                                    <div style={{ textAlign: 'center', marginBottom: 10 }}>
                                        <span style={{ display: 'inline-flex', width: 30, height: 30, borderRadius: '50%', alignItems: 'center', justifyContent: 'center', fontWeight: 700, background: ehHoje ? 'var(--color-primary)' : 'transparent', color: ehHoje ? '#fff' : 'var(--color-text)' }}>
                                            {Number(data.slice(8))}
                                        </span>
                                    </div>
                                    {doDia.length === 0 && <p style={{ fontSize: 11, color: 'var(--color-muted)', textAlign: 'center' }}>—</p>}
                                    {doDia.map(item => (
                                        <ItemCalendario key={item.tarefaId} item={item} ocupado={ocupado} onClick={() => alternar(item)} />
                                    ))}
                                </div>
                            )
                        })}
                    </div>
                )}
            </Card>
        </div>
    )
}