import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../hooks/useAuth'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { dinheiro, horaCurta, hojeISO, nomeDoMes, primeiroNome } from '../utils/formato'
import SemMoradia from '../components/SemMoradia'
import { Avatar, Btn, Card, Carregando, Pill } from '../components/ui'

function saudacao() {
    const hora = new Date().getHours()
    if (hora < 12) {
        return 'Bom dia'
    }
    if (hora < 18) {
        return 'Boa tarde'
    }
    return 'Boa noite'
}

function CartaoNumero({ label, valor, detalhe, cor, onClick }) {
    return (
        <button
            onClick={onClick}
            style={{ background: '#fff', borderRadius: 'var(--radius-card)', border: '1px solid var(--color-border)', padding: '16px 20px', textAlign: 'left', width: '100%', boxShadow: 'var(--shadow-card)', transition: 'box-shadow .15s' }}
            onMouseEnter={e => { e.currentTarget.style.boxShadow = '0 4px 20px rgba(108,99,255,.15)' }}
            onMouseLeave={e => { e.currentTarget.style.boxShadow = 'var(--shadow-card)' }}
        >
            <p style={{ fontSize: 12, fontWeight: 600, color: 'var(--color-muted)', marginBottom: 8 }}>{label}</p>
            <p style={{ fontSize: 24, fontWeight: 700, color: cor, fontFamily: 'var(--font-display)', marginBottom: 2 }}>{valor}</p>
            <p style={{ fontSize: 12, color: 'var(--color-muted)' }}>{detalhe}</p>
        </button>
    )
}

function CabecalhoCard({ titulo, textoBotao, onClick }) {
    return (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
            <h3 style={{ fontWeight: 700, fontSize: 15 }}>{titulo}</h3>
            <Btn size="sm" variant="secondary" onClick={onClick}>{textoBotao}</Btn>
        </div>
    )
}

export default function Dashboard() {
    const { usuario } = useAuth()
    const { ativa } = useMoradia()
    const toast = useToast()
    const navigate = useNavigate()
    const [dados, setDados] = useState(null)
    const [versao, setVersao] = useState(0)
    const [ocupado, setOcupado] = useState(false)
    const ativaId = ativa?.id

    useEffect(() => {
        if (!ativaId) {
            return
        }
        let ativo = true
        Promise.all([api('/api/dashboard'), api('/api/compras')])
            .then(([painel, compras]) => {
                if (ativo) {
                    setDados({ ...painel, jaComprados: compras.comprados.length })
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
        return <SemMoradia titulo={`${saudacao()}, ${primeiroNome(usuario.nome)}! 👋`} />
    }
    if (!dados) {
        return <Carregando />
    }

    const maiorSaldo = Math.max(1, ...dados.balanco.saldos.map(saldo => Math.abs(Number(saldo.saldo))))

    async function alternarTarefa(item) {
        setOcupado(true)
        try {
            await api(`/api/tarefas/${item.tarefaId}/concluir`, { metodo: item.concluida ? 'DELETE' : 'POST', params: { data: hojeISO() } })
            toast(item.concluida ? 'Tarefa desmarcada' : 'Tarefa concluída!')
            setVersao(atual => atual + 1)
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setOcupado(false)
        }
    }

    return (
        <div>
            <div style={{ marginBottom: 24 }}>
                <h1 style={{ fontFamily: 'var(--font-display)', fontSize: 30, fontWeight: 600, marginBottom: 4 }}>
                    {saudacao()}, {primeiroNome(usuario.nome)}! 👋
                </h1>
                <p style={{ color: 'var(--color-muted)', fontSize: 15 }}>Aqui está o resumo da sua moradia em {nomeDoMes(dados.mes).toLowerCase()}.</p>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(190px, 1fr))', gap: 12, marginBottom: 24 }}>
                <CartaoNumero label="DESPESAS DO MÊS" valor={dinheiro(dados.despesasMes)} detalhe={`${dados.lancamentosMes} ${dados.lancamentosMes === 1 ? 'lançamento' : 'lançamentos'}`} cor="var(--color-primary)" onClick={() => navigate('/despesas')} />
                <CartaoNumero label="TAREFAS PENDENTES" valor={dados.tarefasPendentes} detalhe="no mês" cor="#F59E0B" onClick={() => navigate('/tarefas')} />
                <CartaoNumero label="ITENS PARA COMPRAR" valor={dados.itensParaComprar} detalhe={`${dados.jaComprados} já ${dados.jaComprados === 1 ? 'comprado' : 'comprados'}`} cor="#10B981" onClick={() => navigate('/compras')} />
                <CartaoNumero label="MORADORES" valor={dados.moradores} detalhe="na moradia ativa" cor="#6C63FF" onClick={() => navigate('/moradores')} />
            </div>

            <div className="dash-grid" style={{ display: 'grid', gap: 16, marginBottom: 16 }}>
                <Card style={{ padding: 20 }}>
                    <CabecalhoCard titulo="Balanço por morador" textoBotao="Ver tudo" onClick={() => navigate('/despesas')} />
                    {dados.balanco.saldos.map(saldo => {
                        const valor = Number(saldo.saldo)
                        const cor = valor >= 0 ? '#10B981' : '#EF4444'
                        return (
                            <div key={saldo.moradorId} style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
                                <Avatar nome={saldo.nome} cor={saldo.cor} size={32} />
                                <div style={{ flex: 1, minWidth: 0 }}>
                                    <p style={{ fontWeight: 600, fontSize: 13 }}>{primeiroNome(saldo.nome)}</p>
                                    <div style={{ height: 4, background: 'var(--color-border)', borderRadius: 99, marginTop: 3 }}>
                                        <div style={{ height: '100%', width: `${Math.abs(valor) / maiorSaldo * 100}%`, background: cor, borderRadius: 99 }} />
                                    </div>
                                </div>
                                <span style={{ fontSize: 13, fontWeight: 700, color: valor === 0 ? 'var(--color-muted)' : cor, whiteSpace: 'nowrap' }}>
                                    {valor > 0 ? '+' : ''}{dinheiro(valor)}
                                </span>
                            </div>
                        )
                    })}
                </Card>

                <Card style={{ padding: 20 }}>
                    <CabecalhoCard titulo="Tarefas de hoje" textoBotao="Ver tudo" onClick={() => navigate('/tarefas')} />
                    {dados.tarefasHoje.length === 0 ? (
                        <p style={{ fontSize: 13, color: 'var(--color-muted)' }}>Nenhuma tarefa para hoje. 😌</p>
                    ) : dados.tarefasHoje.map(item => (
                        <button
                            key={item.tarefaId}
                            onClick={() => alternarTarefa(item)}
                            disabled={ocupado}
                            title={item.concluida ? 'Desmarcar' : 'Marcar como feita'}
                            style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10, border: 'none', background: 'none', padding: 0, textAlign: 'left' }}
                        >
                            <div style={{ width: 10, height: 10, borderRadius: '50%', background: item.concluida ? '#10B981' : item.responsavelCor, flexShrink: 0 }} />
                            <p style={{ fontSize: 14, flex: 1, fontWeight: 500, textDecoration: item.concluida ? 'line-through' : 'none', color: item.concluida ? 'var(--color-muted)' : 'var(--color-text)' }}>
                                {item.nome}
                                {item.horario && <span style={{ color: 'var(--color-muted)', fontWeight: 400 }}> · {horaCurta(item.horario)}</span>}
                            </p>
                            <Pill label={primeiroNome(item.responsavelNome)} cor={item.responsavelCor} size="xs" />
                        </button>
                    ))}
                </Card>
            </div>

            <Card style={{ padding: 20 }}>
                <CabecalhoCard titulo="Lista de compras" textoBotao="Ver lista completa" onClick={() => navigate('/compras')} />
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {dados.previaCompras.length === 0 ? (
                        <p style={{ fontSize: 13, color: 'var(--color-muted)' }}>Tudo comprado! 🎉</p>
                    ) : dados.previaCompras.map(item => (
                        <span key={item.id} style={{ padding: '6px 14px', borderRadius: 99, background: 'var(--color-bg)', border: '1px solid var(--color-border)', fontSize: 14, fontWeight: 500 }}>
                            {item.nome}
                        </span>
                    ))}
                </div>
            </Card>

            <div style={{ marginTop: 20 }}>
                <p style={{ fontSize: 12, fontWeight: 700, color: 'var(--color-muted)', marginBottom: 8 }}>AÇÕES RÁPIDAS</p>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {[
                        ['💰 Nova despesa', '/despesas'],
                        ['✓ Nova tarefa', '/tarefas'],
                        ['🛒 Adicionar à lista', '/compras'],
                        ['📅 Ver calendário', '/calendario']
                    ].map(([texto, caminho]) => (
                        <Btn key={caminho} variant="white" size="sm" onClick={() => navigate(caminho)}>{texto}</Btn>
                    ))}
                </div>
            </div>

            <style>{`
        .dash-grid { grid-template-columns: 1fr 1fr; }
        @media (max-width: 700px) { .dash-grid { grid-template-columns: 1fr; } }
      `}</style>
        </div>
    )
}