import { useState } from 'react'
import { DIAS_CURTOS, hojeISO } from '../../utils/formato'
import { Btn, Field, Inp, Sel, Textarea } from '../ui'

function diaDaSemana(iso) {
    return new Date(`${iso}T00:00:00`).getDay()
}

export default function TarefaForm({ inicial, moradores, onCancelar, onSalvar }) {
    const [form, setForm] = useState(inicial ?? {
        nome: '',
        descricao: '',
        responsavelId: moradores[0]?.id ?? '',
        horario: '',
        dataInicio: hojeISO(),
        dataFim: '',
        repetir: false,
        diasSemana: []
    })
    const [erros, setErros] = useState({})
    const [erroGeral, setErroGeral] = useState('')
    const [salvando, setSalvando] = useState(false)

    function mudar(campo, valor) {
        setForm(atual => ({ ...atual, [campo]: valor }))
    }

    function alternarRepetir(marcado) {
        setForm(atual => ({
            ...atual,
            repetir: marcado,
            diasSemana: marcado && atual.diasSemana.length === 0 ? [diaDaSemana(atual.dataInicio)] : atual.diasSemana
        }))
    }

    function alternarDia(dia) {
        mudar('diasSemana', form.diasSemana.includes(dia) ? form.diasSemana.filter(atual => atual !== dia) : [...form.diasSemana, dia].sort())
    }

    async function enviar(evento) {
        evento.preventDefault()
        setErros({})
        setErroGeral('')

        if (form.repetir && form.diasSemana.length === 0) {
            setErroGeral('Escolha pelo menos um dia da semana')
            return
        }

        setSalvando(true)
        try {
            await onSalvar({
                nome: form.nome.trim(),
                descricao: form.descricao.trim() || null,
                responsavelId: Number(form.responsavelId),
                recorrencia: form.repetir ? 'SEMANAL' : 'UNICA',
                dataInicio: form.dataInicio,
                dataFim: form.repetir && form.dataFim ? form.dataFim : null,
                horario: form.horario || null,
                diasSemana: form.repetir ? form.diasSemana : []
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
            <Field label="Nome da tarefa *" erro={erros.nome}>
                <Inp placeholder="Limpar cozinha" value={form.nome} onChange={e => mudar('nome', e.target.value)} />
            </Field>
            <Field label="Descrição" erro={erros.descricao}>
                <Textarea placeholder="Detalhes sobre a tarefa..." value={form.descricao} onChange={e => mudar('descricao', e.target.value)} />
            </Field>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16 }}>
                <Field label="Responsável" erro={erros.responsavelId}>
                    <Sel value={form.responsavelId} onChange={e => mudar('responsavelId', e.target.value)}>
                        {moradores.map(morador => <option key={morador.id} value={morador.id}>{morador.nome}</option>)}
                    </Sel>
                </Field>
                <Field label="Horário" erro={erros.horario} hint="Opcional">
                    <Inp type="time" value={form.horario} onChange={e => mudar('horario', e.target.value)} />
                </Field>
            </div>

            <label style={{ display: 'flex', alignItems: 'center', gap: 10, fontSize: 14, cursor: 'pointer' }}>
                <input type="checkbox" checked={form.repetir} onChange={e => alternarRepetir(e.target.checked)} style={{ width: 18, height: 18, accentColor: 'var(--color-primary)' }} />
                Repetir semanalmente
            </label>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16 }}>
                <Field label={form.repetir ? 'Começa em' : 'Data'} erro={erros.dataInicio}>
                    <Inp type="date" value={form.dataInicio} onChange={e => mudar('dataInicio', e.target.value)} />
                </Field>
                {form.repetir && (
                    <Field label="Termina em" erro={erros.dataFim} hint="Opcional. Vazio = sem data para acabar.">
                        <Inp type="date" value={form.dataFim} onChange={e => mudar('dataFim', e.target.value)} />
                    </Field>
                )}
            </div>

            {form.repetir && (
                <Field label="Dias da semana" erro={erros.diasSemana}>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                        {DIAS_CURTOS.map((nome, dia) => {
                            const ativo = form.diasSemana.includes(dia)
                            return (
                                <button
                                    key={nome}
                                    type="button"
                                    onClick={() => alternarDia(dia)}
                                    style={{ padding: '7px 12px', borderRadius: 8, border: `1.5px solid ${ativo ? 'var(--color-primary)' : 'var(--color-border)'}`, background: ativo ? 'var(--color-primary-light)' : '#fff', color: ativo ? 'var(--color-primary)' : 'var(--color-muted)', fontWeight: 600, fontSize: 13 }}
                                >
                                    {nome}
                                </button>
                            )
                        })}
                    </div>
                </Field>
            )}

            {erroGeral && <p style={{ fontSize: 13, color: 'var(--color-danger)' }}>{erroGeral}</p>}

            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                <Btn type="button" variant="ghost" onClick={onCancelar}>Cancelar</Btn>
                <Btn type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar'}</Btn>
            </div>
        </form>
    )
}