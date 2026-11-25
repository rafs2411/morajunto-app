import { useEffect, useState } from 'react'
import { api } from '../api'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'
import { dataBR } from '../utils/formato'
import { Badge, Btn, Card } from './ui'

const STATUS = {
    PENDENTE: { texto: 'Aguardando', cor: '#F59E0B' },
    ACEITA: { texto: 'Aceito', cor: '#10B981' },
    RECUSADA: { texto: 'Recusado', cor: '#EF4444' },
    EXPIRADA: { texto: 'Expirado', cor: '#6B7280' }
}

function Secao({ titulo, subtitulo, children }) {
    return (
        <section style={{ marginTop: 32 }}>
            <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 20, fontWeight: 600, marginBottom: 4 }}>{titulo}</h2>
            {subtitulo && <p style={{ fontSize: 14, color: 'var(--color-muted)', marginBottom: 12 }}>{subtitulo}</p>}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: subtitulo ? 0 : 12 }}>{children}</div>
        </section>
    )
}

function Linha({ titulo, detalhe, children }) {
    return (
        <Card style={{ padding: '14px 18px', display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
            <div style={{ flex: 1, minWidth: 200 }}>
                <p style={{ fontWeight: 600, fontSize: 15 }}>{titulo}</p>
                <p style={{ fontSize: 13, color: 'var(--color-muted)', marginTop: 2 }}>{detalhe}</p>
            </div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>{children}</div>
        </Card>
    )
}

function TextoVazio({ children }) {
    return <p style={{ fontSize: 14, color: 'var(--color-muted)' }}>{children}</p>
}

export default function EntradaMoradia() {
    const { ativa, ehDono, recarregar } = useMoradia()
    const toast = useToast()
    const [dados, setDados] = useState({ convites: [], disponiveis: [], enviados: [], recebidos: [] })
    const [versao, setVersao] = useState(0)
    const [ocupado, setOcupado] = useState(false)
    const ativaId = ativa?.id

    useEffect(() => {
        let ativo = true
        Promise.all([
            api('/api/convites/recebidos'),
            api('/api/pedidos/moradias-disponiveis'),
            api('/api/pedidos/enviados'),
            ehDono ? api('/api/pedidos/recebidos') : Promise.resolve([])
        ])
            .then(([convites, disponiveis, enviados, recebidos]) => {
                if (ativo) {
                    setDados({ convites, disponiveis, enviados, recebidos })
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
    }, [versao, ehDono, ativaId, toast])

    async function executar(acao, mensagem, atualizarMoradias = false) {
        setOcupado(true)
        try {
            await acao()
            if (atualizarMoradias) {
                await recarregar()
            }
            toast(mensagem)
            setVersao(atual => atual + 1)
        } catch (erro) {
            toast(erro.message, 'error')
        } finally {
            setOcupado(false)
        }
    }

    return (
        <div>
            {dados.convites.length > 0 && (
                <Secao titulo="Convites recebidos" subtitulo="Alguém te convidou para morar junto.">
                    {dados.convites.map(convite => (
                        <Linha key={convite.id} titulo={convite.moradiaNome} detalhe={`${convite.moradiaEndereco} · vale até ${dataBR(convite.expiraEm)}`}>
                            <Btn size="sm" variant="ghost" disabled={ocupado}
                                onClick={() => executar(() => api(`/api/convites/${convite.id}/recusar`, { metodo: 'POST' }), 'Convite recusado')}>
                                Recusar
                            </Btn>
                            <Btn size="sm" disabled={ocupado}
                                onClick={() => executar(() => api(`/api/convites/${convite.id}/aceitar`, { metodo: 'POST' }), `Você entrou em ${convite.moradiaNome}`, true)}>
                                Aceitar
                            </Btn>
                        </Linha>
                    ))}
                </Secao>
            )}

            {ehDono && ativa && (
                <Secao titulo={`Pedidos para entrar em ${ativa.nome}`}>
                    {dados.recebidos.length === 0 ? (
                        <TextoVazio>
                            {ativa.disponivelParaEntrada
                                ? 'Nenhum pedido pendente.'
                                : 'Esta moradia está fechada para novos moradores. Para receber pedidos, edite a moradia e marque "Aberta para novos moradores".'}
                        </TextoVazio>
                    ) : dados.recebidos.map(pedido => (
                        <Linha key={pedido.id} titulo={pedido.usuarioNome} detalhe={`${pedido.usuarioEmail} · pediu em ${dataBR(pedido.criadaEm)}`}>
                            <Btn size="sm" variant="ghost" disabled={ocupado}
                                onClick={() => executar(() => api(`/api/pedidos/${pedido.id}/recusar`, { metodo: 'POST' }), 'Pedido recusado')}>
                                Recusar
                            </Btn>
                            <Btn size="sm" disabled={ocupado}
                                onClick={() => executar(() => api(`/api/pedidos/${pedido.id}/aceitar`, { metodo: 'POST' }), `${pedido.usuarioNome} agora mora aqui`, true)}>
                                Aceitar
                            </Btn>
                        </Linha>
                    ))}
                </Secao>
            )}

            <Secao titulo="Encontrar moradias" subtitulo="Moradias abertas para novos moradores.">
                {dados.disponiveis.length === 0 ? (
                    <TextoVazio>Nenhuma moradia aberta no momento.</TextoVazio>
                ) : dados.disponiveis.map(moradia => (
                    <Linha
                        key={moradia.id}
                        titulo={moradia.nome}
                        detalhe={`${moradia.endereco} · ${moradia.totalMoradores} moradores · ${moradia.vagas === null ? 'sem limite de vagas' : `${moradia.vagas} ${moradia.vagas === 1 ? 'vaga' : 'vagas'}`}`}
                    >
                        {moradia.pedidoPendente ? (
                            <Badge label="Pedido enviado" cor="#F59E0B" />
                        ) : (
                            <Btn size="sm" variant="secondary" disabled={ocupado}
                                onClick={() => executar(() => api('/api/pedidos', { metodo: 'POST', corpo: { moradiaId: moradia.id } }), 'Pedido enviado ao dono da moradia')}>
                                Pedir para entrar
                            </Btn>
                        )}
                    </Linha>
                ))}
            </Secao>

            {dados.enviados.length > 0 && (
                <Secao titulo="Meus pedidos">
                    {dados.enviados.map(pedido => (
                        <Linha key={pedido.id} titulo={pedido.moradiaNome} detalhe={`Enviado em ${dataBR(pedido.criadaEm)}`}>
                            <Badge label={STATUS[pedido.status].texto} cor={STATUS[pedido.status].cor} />
                        </Linha>
                    ))}
                </Secao>
            )}
        </div>
    )
}