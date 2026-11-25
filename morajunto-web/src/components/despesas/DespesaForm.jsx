import { useState } from 'react'
import { centavos, dinheiro, hojeISO, lerValor, primeiroNome } from '../../utils/formato'
import ChipMorador from '../ChipMorador'
import { Avatar, Btn, Field, Inp, Sel, Textarea } from '../ui'

function dividirIgual(totalCentavos, ids) {
    const ordenados = [...ids].sort((a, b) => a - b)
    const base = Math.floor(totalCentavos / ordenados.length)
    const sobra = totalCentavos - base * ordenados.length
    return Object.fromEntries(ordenados.map((id, indice) => [id, (base + (indice < sobra ? 1 : 0)) / 100]))
}

function BotaoAba({ ativo, onClick, children }) {
    return (
        <button
            type="button"
            onClick={onClick}
            style={{ flex: 1, padding: 8, borderRadius: 7, border: 'none', background: ativo ? '#fff' : 'transparent', fontWeight: 600, fontSize: 13, color: ativo ? 'var(--color-text)' : 'var(--color-muted)', boxShadow: ativo ? '0 1px 4px rgba(0,0,0,.1)' : undefined }}
        >
            {children}
        </button>
    )
}

export default function DespesaForm({ categorias, moradores, meuMoradorId, onCancelar, onSalvar }) {
    const categoriaPadrao = categorias.find(categoria => categoria.nome === 'Outros') ?? categorias[0]
    const [form, setForm] = useState({
        nome: '',
        categoriaId: categoriaPadrao?.id ?? '',
        valor: '',
        dataDespesa: hojeISO(),
        descricao: '',
        pagoPorMoradorId: meuMoradorId ?? moradores[0]?.id,
        divididos: moradores.map(morador => morador.id),
        tipoDivisao: 'IGUAL',
        personalizados: {}
    })
    const [erros, setErros] = useState({})
    const [erroGeral, setErroGeral] = useState('')
    const [salvando, setSalvando] = useState(false)

    function mudar(campo, valor) {
        setForm(atual => ({ ...atual, [campo]: valor }))
    }

    function alternarDividido(id) {
        mudar('divididos', form.divididos.includes(id) ? form.divididos.filter(atual => atual !== id) : [...form.divididos, id])
    }

    function mudarPersonalizado(id, valor) {
        setForm(atual => ({ ...atual, personalizados: { ...atual.personalizados, [id]: valor } }))
    }

    const total = lerValor(form.valor)
    const totalValido = Number.isFinite(total) && total > 0
    const selecionados = moradores.filter(morador => form.divididos.includes(morador.id))
    const partesIguais = totalValido && selecionados.length > 0 ? dividirIgual(centavos(total), selecionados.map(morador => morador.id)) : {}
    const somaPersonalizada = selecionados.reduce((soma, morador) => soma + (centavos(lerValor(form.personalizados[morador.id])) || 0), 0) / 100
    const diferenca = totalValido ? (centavos(total) - centavos(somaPersonalizada)) / 100 : 0

    async function enviar(evento) {
        evento.preventDefault()
        setErros({})
        setErroGeral('')

        if (!totalValido) {
            setErros({ valorTotal: 'Informe um valor maior que zero' })
            return
        }
        if (selecionados.length === 0) {
            setErroGeral('Escolha pelo menos uma pessoa para dividir a despesa')
            return
        }
        if (form.tipoDivisao === 'PERSONALIZADA' && diferenca !== 0) {
            setErroGeral('A soma das partes precisa ser igual ao valor total')
            return
        }

        const divisoes = selecionados.map(morador => form.tipoDivisao === 'IGUAL'
            ? { moradorId: morador.id }
            : { moradorId: morador.id, valor: lerValor(form.personalizados[morador.id]) || 0 })

        setSalvando(true)
        try {
            await onSalvar({
                nome: form.nome.trim(),
                categoriaId: Number(form.categoriaId),
                valorTotal: centavos(total) / 100,
                dataDespesa: form.dataDespesa,
                descricao: form.descricao.trim() || null,
                pagoPorMoradorId: form.pagoPorMoradorId,
                tipoDivisao: form.tipoDivisao,
                divisoes
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
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16 }}>
                <Field label="Nome *" erro={erros.nome}>
                    <Inp placeholder="Conta de Luz" value={form.nome} onChange={e => mudar('nome', e.target.value)} />
                </Field>
                <Field label="Categoria" erro={erros.categoriaId}>
                    <Sel value={form.categoriaId} onChange={e => mudar('categoriaId', e.target.value)}>
                        {categorias.map(categoria => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}
                    </Sel>
                </Field>
                <Field label="Valor total (R$) *" erro={erros.valorTotal}>
                    <Inp inputMode="decimal" placeholder="0,00" value={form.valor} onChange={e => mudar('valor', e.target.value)} />
                </Field>
                <Field label="Data" erro={erros.dataDespesa}>
                    <Inp type="date" value={form.dataDespesa} onChange={e => mudar('dataDespesa', e.target.value)} />
                </Field>
            </div>

            <Field label="Descrição" erro={erros.descricao}>
                <Textarea placeholder="Detalhes opcionais..." value={form.descricao} onChange={e => mudar('descricao', e.target.value)} />
            </Field>

            <Field label="Pago por" erro={erros.pagoPorMoradorId}>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {moradores.map(morador => (
                        <ChipMorador key={morador.id} morador={morador} selecionado={form.pagoPorMoradorId === morador.id} onClick={() => mudar('pagoPorMoradorId', morador.id)} />
                    ))}
                </div>
            </Field>

            <Field label="Dividir entre">
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {moradores.map(morador => (
                        <ChipMorador key={morador.id} morador={morador} selecionado={form.divididos.includes(morador.id)} onClick={() => alternarDividido(morador.id)} />
                    ))}
                </div>
            </Field>

            <div style={{ display: 'flex', gap: 4, background: 'var(--color-border)', borderRadius: 10, padding: 4 }}>
                <BotaoAba ativo={form.tipoDivisao === 'IGUAL'} onClick={() => mudar('tipoDivisao', 'IGUAL')}>Dividir igual</BotaoAba>
                <BotaoAba ativo={form.tipoDivisao === 'PERSONALIZADA'} onClick={() => mudar('tipoDivisao', 'PERSONALIZADA')}>Valores personalizados</BotaoAba>
            </div>

            {selecionados.length > 0 && (
                <div style={{ background: 'var(--color-bg)', borderRadius: 10, padding: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {selecionados.map(morador => (
                        <div key={morador.id} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <Avatar nome={morador.nome} cor={morador.cor} size={26} />
                            <span style={{ flex: 1, fontSize: 14, fontWeight: 500 }}>{primeiroNome(morador.nome)}</span>
                            {form.tipoDivisao === 'IGUAL' ? (
                                <span style={{ fontSize: 14, fontWeight: 700 }}>{totalValido ? dinheiro(partesIguais[morador.id]) : '—'}</span>
                            ) : (
                                <Inp inputMode="decimal" placeholder="0,00" value={form.personalizados[morador.id] ?? ''} onChange={e => mudarPersonalizado(morador.id, e.target.value)} style={{ width: 120, textAlign: 'right' }} />
                            )}
                        </div>
                    ))}
                    {form.tipoDivisao === 'PERSONALIZADA' && totalValido && (
                        <p style={{ fontSize: 13, fontWeight: 600, textAlign: 'right', color: diferenca === 0 ? 'var(--color-success)' : 'var(--color-warning)' }}>
                            {diferenca === 0 ? '✓ A soma bate com o total' : diferenca > 0 ? `Faltam ${dinheiro(diferenca)}` : `Passou ${dinheiro(-diferenca)} do total`}
                        </p>
                    )}
                </div>
            )}

            {erroGeral && <p style={{ fontSize: 13, color: 'var(--color-danger)' }}>{erroGeral}</p>}

            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <Btn type="button" variant="ghost" onClick={onCancelar}>Cancelar</Btn>
                <Btn type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar'}</Btn>
            </div>
        </form>
    )
}