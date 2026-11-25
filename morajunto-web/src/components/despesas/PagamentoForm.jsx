import { useState } from 'react'
import { centavos, lerValor } from '../../utils/formato'
import ChipMorador from '../ChipMorador'
import { Btn, Field, Inp } from '../ui'

export default function PagamentoForm({ moradores, inicial, onCancelar, onSalvar }) {
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

        const valor = lerValor(form.valor)
        if (!form.pagadorId || !form.recebedorId) {
            setErroGeral('Escolha quem pagou e quem recebeu')
            return
        }
        if (form.pagadorId === form.recebedorId) {
            setErroGeral('Quem paga e quem recebe precisam ser pessoas diferentes')
            return
        }
        if (!Number.isFinite(valor) || valor <= 0) {
            setErros({ valor: 'Informe um valor maior que zero' })
            return
        }

        setSalvando(true)
        try {
            await onSalvar({
                pagadorId: form.pagadorId,
                recebedorId: form.recebedorId,
                valor: centavos(valor) / 100,
                dataPagamento: form.dataPagamento,
                observacao: form.observacao.trim() || null
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
            <Field label="Quem pagou">
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {moradores.map(morador => (
                        <ChipMorador key={morador.id} morador={morador} selecionado={form.pagadorId === morador.id} onClick={() => mudar('pagadorId', morador.id)} />
                    ))}
                </div>
            </Field>
            <Field label="Quem recebeu">
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {moradores.map(morador => (
                        <ChipMorador key={morador.id} morador={morador} selecionado={form.recebedorId === morador.id} onClick={() => mudar('recebedorId', morador.id)} />
                    ))}
                </div>
            </Field>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16 }}>
                <Field label="Valor (R$) *" erro={erros.valor}>
                    <Inp inputMode="decimal" placeholder="0,00" value={form.valor} onChange={e => mudar('valor', e.target.value)} />
                </Field>
                <Field label="Data" erro={erros.dataPagamento}>
                    <Inp type="date" value={form.dataPagamento} onChange={e => mudar('dataPagamento', e.target.value)} />
                </Field>
            </div>
            <Field label="Observação" erro={erros.observacao}>
                <Inp placeholder="Pix do aluguel" value={form.observacao} onChange={e => mudar('observacao', e.target.value)} />
            </Field>

            {erroGeral && <p style={{ fontSize: 13, color: 'var(--color-danger)' }}>{erroGeral}</p>}

            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <Btn type="button" variant="ghost" onClick={onCancelar}>Cancelar</Btn>
                <Btn type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Registrar pagamento'}</Btn>
            </div>
        </form>
    )
}