export const DIAS_CURTOS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']
export const MESES = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro']

export function dinheiro(valor) {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(valor ?? 0))
}

export function lerValor(texto) {
    const valor = String(texto ?? '').trim()
    if (!valor) {
        return NaN
    }
    const normalizado = valor.includes(',') ? valor.replace(/\./g, '').replace(',', '.') : valor
    return Number(normalizado)
}

export function centavos(valor) {
    return Math.round(Number(valor) * 100)
}

export function paraISO(data) {
    const ano = data.getFullYear()
    const mes = String(data.getMonth() + 1).padStart(2, '0')
    const dia = String(data.getDate()).padStart(2, '0')
    return `${ano}-${mes}-${dia}`
}

export function hojeISO() {
    return paraISO(new Date())
}

export function mesISO(data = new Date()) {
    return paraISO(data).slice(0, 7)
}

export function somarMes(mes, quantidade) {
    const [ano, numero] = mes.split('-').map(Number)
    return mesISO(new Date(ano, numero - 1 + quantidade, 1))
}

export function nomeDoMes(mes) {
    const [ano, numero] = mes.split('-').map(Number)
    return `${MESES[numero - 1]} ${ano}`
}

export function dataBR(iso) {
    if (!iso) {
        return ''
    }
    const [ano, mes, dia] = iso.slice(0, 10).split('-')
    return `${dia}/${mes}/${ano}`
}

export function horaCurta(horario) {
    return horario ? horario.slice(0, 5) : ''
}

export function iniciais(nome = '') {
    return nome.trim().split(/\s+/).slice(0, 2).map(parte => parte[0]).join('').toUpperCase()
}

export function primeiroNome(nome = '') {
    return nome.trim().split(/\s+/)[0]
}