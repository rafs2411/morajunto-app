import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../hooks/useAuth'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { dataBR, dinheiro, hojeISO, mesISO, nomeDoMes, primeiroNome, somarMes } from '../utils/formato'
import SemMoradia from '../components/SemMoradia'
import DespesaForm from '../components/despesas/DespesaForm'
import PagamentoForm from '../components/despesas/PagamentoForm'
import { Avatar, Btn, Card, Carregando, ConfirmDialog, EmptyState, Modal, PageHeader } from '../components/ui'

const ICONES = { 'Aluguel': '🏠', 'Alimentação': '🛒', 'Energia': '⚡', 'Água': '💧', 'Internet': '📶', 'Gás': '🔥', 'Limpeza': '🧹', 'Outros': '📦' }

function NavegacaoMes({ mes, onMudar }) {
    const estilo = { width: 36, height: 36, borderRadius: 10, border: '1px solid var(--color-border)', background: '#fff', fontSize: 16 }
    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <button style={estilo} onClick={() => onMudar(somarMes(mes, -1))}>‹</button>
            <span style={{ fontWeight: 600, fontSize: 15, minWidth: 140, textAlign: 'center' }}>{nomeDoMes(mes)}</span>
            <button style={estilo} onClick={() => onMudar(somarMes(mes, 1))}>›</button>
        </div>
    )
}

function CartaoSaldo({ saldo }) {
    const positivo = saldo.saldo > 0
    const zerado = Number(saldo.saldo) === 0
    return (
        <div style={{ background: saldo.cor + '12', border: `1px solid ${saldo.cor}40`, borderRadius: 'var(--radius-card)', padding: '14px 18px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                <Avatar nome={saldo.nome} cor={saldo.cor} size={26} />
                <span style={{ fontWeight: 600, color: saldo.cor }}>{primeiroNome(saldo.nome)}</span>
            </div>
            <p style={{ fontSize: 22, fontWeight: 800, color: zerado ? 'var(--color-muted)' : positivo ? 'var(--color-success)' : 'var(--color-danger)' }}>
                {positivo ? '+' : ''}{dinheiro(saldo.saldo)}
            </p>
            <p style={{ fontSize: 12, color: 'var(--color-muted)' }}>{zerado ? 'em dia' : positivo ? 'a receber' : 'a pagar'}</p>
        </div>
    )
}

function DespesaItem({ despesa, aberta, onAlternar, onExcluir }) {
    const pessoas = despesa.divisoes.length
    return (
        <Card style={{ padding: 0, overflow: 'hidden' }}>
            <button onClick={onAlternar} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 14, padding: '16px 20px', border: 'none', background: 'transparent', textAlign: 'left' }}>
                <div style={{ width: 46, height: 46, borderRadius: 12, background: 'var(--color-primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20, flexShrink: 0 }}>
                    {ICONES[despesa.categoria] ?? '📦'}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                    <p style={{ fontWeight: 600, fontSize: 16 }}>
                        {despesa.nome}
                        <span style={{ marginLeft: 10, fontSize: 12, fontWeight: 600, color: 'var(--color-primary)' }}>{despesa.categoria}</span>
                    </p>
                    <p style={{ fontSize: 13, color: 'var(--color-muted)', marginTop: 4 }}>
                        {dataBR(despesa.dataDespesa)} · Pago por <span style={{ color: despesa.pagoPorCor, fontWeight: 600 }}>{primeiroNome(despesa.pagoPorNome)}</span>
                    </p>
                </div>
                <div style={{ textAlign: 'right' }}>
                    <p style={{ fontFamily: 'var(--font-display)', fontSize: 20, fontWeight: 700 }}>{dinheiro(despesa.valorTotal)}</p>
                    <p style={{ fontSize: 12, color: 'var(--color-muted)' }}>
                        {despesa.tipoDivisao === 'IGUAL' ? `${dinheiro(despesa.valorTotal / pessoas)}/pessoa` : `dividido entre ${pessoas}`}
                    </p>
                </div>
                <span style={{ color: 'var(--color-muted)', transform: aberta ? 'rotate(180deg)' : 'none', transition: 'transform .15s' }}>▾</span>
            </button>

            {aberta && (
                <div style={{ borderTop: '1px solid var(--color-border)', padding: '14px 20px', background: 'var(--color-bg)', display: 'flex', flexDirection: 'column', gap: 10 }}>
                    {despesa.descricao && <p style={{ fontSize: 14, color: 'var(--color-muted)' }}>{despesa.descricao}</p>}
                    <p style={{ fontSize: 12, fontWeight: 700, color: 'var(--color-muted)' }}>
                        {despesa.tipoDivisao === 'IGUAL' ? 'DIVISÃO IGUAL' : 'DIVISÃO PERSONALIZADA'}
                    </p>
                    {despesa.divisoes.map(divisao => (
                        <div key={divisao.moradorId} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <Avatar nome={divisao.nome} cor={divisao.cor} size={24} />
                            <span style={{ flex: 1, fontSize: 14 }}>{divisao.nome}</span>
                            <span style={{ fontSize: 14, fontWeight: 700 }}>{dinheiro(divisao.valor)}</span>
                        </div>
                    ))}
                    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <Btn size="sm" variant="danger" onClick={onExcluir}>Excluir despesa</Btn>
                    </div>
                </div>
            )}
        </Card>
    )
}

export default function Despesas() {
    const { usuario } = useAuth()
    const { ativa } = useMoradia()
    const toast = useToast()
    const [mes, setMes] = useState(mesISO())
    const [dados, setDados] = useState(null)
    const [versao, setVersao] = useState(0)
    const [aberta, setAberta] = useState(null)
    const [novaDespesa, setNovaDespesa] = useState(false)
    const [pagamento, setPagamento] = useState(null)
    const [excluindo, setExcluindo] = useState(null)
    const ativaId = ativa?.id

    useEffect(() => {
        if (!ativaId) {
            return
        }
        let ativo = true
        Promise.all([
            api('/api/despesas', { params: { mes } }),
            api('/api/saldos'),
            api('/api/pagamentos'),
            api('/api/moradores'),
            api('/api/despesas/categorias')
        ])
            .then(([despesas, resumo, pagamentos, moradores, categorias]) => {
                if (ativo) {
                    setDados({ despesas, resumo, pagamentos, moradores, categorias })
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
    }, [ativaId, mes, versao, toast])

    if (!ativa) {
        return <SemMoradia titulo="Despesas" />
    }
    if (!dados) {
        return <Carregando />
    }

    const meuMorador = dados.moradores.find(morador => morador.email === usuario.email)
    const totalMes = dados.despesas.reduce((soma, despesa) => soma + Number(despesa.valorTotal), 0)
    const pagamentosDoMes = dados.pagamentos.filter(item => item.dataPagamento.startsWith(mes))

    function atualizar() {
        setVersao(atual => atual + 1)
    }

    async function salvarDespesa(corpo) {
        await api('/api/despesas', { metodo: 'POST', corpo })
        setNovaDespesa(false)
        toast('Despesa salva')
        if (corpo.dataDespesa.slice(0, 7) !== mes) {
            setMes(corpo.dataDespesa.slice(0, 7))
        }
        atualizar()
    }

    async function salvarPagamento(corpo) {
        await api('/api/pagamentos', { metodo: 'POST', corpo })
        setPagamento(null)
        toast('Pagamento registrado')
        atualizar()
    }

    function abrirPagamento(divida) {
        setPagamento({
            pagadorId: divida?.devedorId ?? meuMorador?.id ?? null,
            recebedorId: divida?.credorId ?? null,
            valor: divida ? String(divida.valor).replace('.', ',') : '',
            dataPagamento: hojeISO(),
            observacao: ''
        })
    }

    async function confirmarExclusao() {
        const { tipo, item } = excluindo
        try {
            await api(tipo === 'despesa' ? `/api/despesas/${item.id}` : `/api/pagamentos/${item.id}`, { metodo: 'DELETE' })
            toast(tipo === 'despesa' ? 'Despesa excluída' : 'Pagamento excluído')
            atualizar()
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setExcluindo(null)
        }
    }

    return (
        <div>
            <PageHeader
                titulo="Despesas"
                subtitulo={`Total do mês: ${dinheiro(totalMes)}`}
                acao={
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                        <Btn variant="secondary" onClick={() => abrirPagamento(null)}>Registrar pagamento</Btn>
                        <Btn onClick={() => setNovaDespesa(true)}>+ Nova Despesa</Btn>
                    </div>
                }
            />

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(190px, 1fr))', gap: 12, marginBottom: 16 }}>
                {dados.resumo.saldos.map(saldo => <CartaoSaldo key={saldo.moradorId} saldo={saldo} />)}
            </div>

            <Card style={{ padding: 20, marginBottom: 24 }}>
                <h3 style={{ fontWeight: 700, fontSize: 15, marginBottom: 12 }}>Quem deve a quem</h3>
                {dados.resumo.dividas.length === 0 ? (
                    <p style={{ fontSize: 14, color: 'var(--color-muted)' }}>Tudo certo! Ninguém deve nada. 🎉</p>
                ) : dados.resumo.dividas.map(divida => (
                    <div key={`${divida.devedorId}-${divida.credorId}`} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '8px 0', flexWrap: 'wrap' }}>
                        <Avatar nome={divida.devedorNome} cor={divida.devedorCor} size={28} />
                        <span style={{ fontSize: 14 }}>
                            <strong>{primeiroNome(divida.devedorNome)}</strong> deve <strong>{dinheiro(divida.valor)}</strong> para <strong>{primeiroNome(divida.credorNome)}</strong>
                        </span>
                        <Btn size="sm" variant="secondary" style={{ marginLeft: 'auto' }} onClick={() => abrirPagamento(divida)}>Registrar pagamento</Btn>
                    </div>
                ))}
            </Card>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12, flexWrap: 'wrap', gap: 8 }}>
                <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 20, fontWeight: 600 }}>Lançamentos</h2>
                <NavegacaoMes mes={mes} onMudar={setMes} />
            </div>

            {dados.despesas.length === 0 ? (
                <Card>
                    <EmptyState icon="💰" title="Nenhuma despesa neste mês" subtitle="Lance a primeira despesa para começar a dividir." />
                </Card>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                    {dados.despesas.map(despesa => (
                        <DespesaItem
                            key={despesa.id}
                            despesa={despesa}
                            aberta={aberta === despesa.id}
                            onAlternar={() => setAberta(aberta === despesa.id ? null : despesa.id)}
                            onExcluir={() => setExcluindo({ tipo: 'despesa', item: despesa })}
                        />
                    ))}
                </div>
            )}

            <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 20, fontWeight: 600, margin: '28px 0 12px' }}>Pagamentos do mês</h2>
            {pagamentosDoMes.length === 0 ? (
                <p style={{ fontSize: 14, color: 'var(--color-muted)' }}>Nenhum pagamento registrado neste mês.</p>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {pagamentosDoMes.map(item => (
                        <Card key={item.id} style={{ padding: '12px 18px', display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                            <Avatar nome={item.pagadorNome} cor={item.pagadorCor} size={28} />
                            <span style={{ color: 'var(--color-muted)' }}>→</span>
                            <Avatar nome={item.recebedorNome} cor={item.recebedorCor} size={28} />
                            <div style={{ flex: 1, minWidth: 160 }}>
                                <p style={{ fontSize: 14, fontWeight: 600 }}>{primeiroNome(item.pagadorNome)} pagou {primeiroNome(item.recebedorNome)}</p>
                                <p style={{ fontSize: 12, color: 'var(--color-muted)' }}>{dataBR(item.dataPagamento)}{item.observacao ? ` · ${item.observacao}` : ''}</p>
                            </div>
                            <span style={{ fontWeight: 700 }}>{dinheiro(item.valor)}</span>
                            <Btn size="sm" variant="danger" onClick={() => setExcluindo({ tipo: 'pagamento', item })}>🗑️</Btn>
                        </Card>
                    ))}
                </div>
            )}

            {novaDespesa && (
                <Modal title="Nova Despesa" onClose={() => setNovaDespesa(false)} wide>
                    <DespesaForm
                        categorias={dados.categorias}
                        moradores={dados.moradores}
                        meuMoradorId={meuMorador?.id}
                        onCancelar={() => setNovaDespesa(false)}
                        onSalvar={salvarDespesa}
                    />
                </Modal>
            )}

            {pagamento && (
                <Modal title="Registrar pagamento" onClose={() => setPagamento(null)}>
                    <PagamentoForm moradores={dados.moradores} inicial={pagamento} onCancelar={() => setPagamento(null)} onSalvar={salvarPagamento} />
                </Modal>
            )}

            {excluindo && (
                <ConfirmDialog
                    msg={excluindo.tipo === 'despesa'
                        ? `Excluir "${excluindo.item.nome}"? Os saldos vão ser recalculados.`
                        : 'Excluir este pagamento? Os saldos vão ser recalculados.'}
                    onConfirm={confirmarExclusao}
                    onCancel={() => setExcluindo(null)}
                />
            )}
        </div>
    )
}