const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
const CHAVE_TOKEN = 'morajunto.token'

export class ApiError extends Error {
    constructor(status, mensagem, erros = {}) {
        super(mensagem)
        this.status = status
        this.erros = erros
    }
}

export function lerToken() {
    return localStorage.getItem(CHAVE_TOKEN)
}

export function salvarToken(token) {
    localStorage.setItem(CHAVE_TOKEN, token)
}

export function apagarToken() {
    localStorage.removeItem(CHAVE_TOKEN)
}

function montarUrl(caminho, params) {
    const url = new URL(BASE_URL + caminho)
    if (params) {
        Object.entries(params).forEach(([chave, valor]) => {
            if (valor !== undefined && valor !== null && valor !== '') {
                url.searchParams.set(chave, valor)
            }
        })
    }
    return url
}

async function requisitar(caminho, { metodo = 'GET', corpo, params } = {}) {
    const token = lerToken()
    const headers = {}
    if (token) {
        headers.Authorization = `Bearer ${token}`
    }
    if (corpo !== undefined) {
        headers['Content-Type'] = 'application/json'
    }

    try {
        const resposta = await fetch(montarUrl(caminho, params), {
            method: metodo,
            headers,
            body: corpo !== undefined ? JSON.stringify(corpo) : undefined
        })

        if (resposta.status === 401 && token) {
            apagarToken()
            window.dispatchEvent(new Event('morajunto:sessao-expirada'))
            throw new ApiError(401, 'Sua sessão expirou. Entre novamente.')
        }

        return resposta
    } catch (erro) {
        if (erro instanceof ApiError) {
            throw erro
        }
        throw new ApiError(0, 'Não foi possível conectar à API. Confira se o backend está rodando.')
    }
}

export async function api(caminho, opcoes) {
    const resposta = await requisitar(caminho, opcoes)

    if (resposta.status === 204) {
        return null
    }

    const texto = await resposta.text()
    const dados = texto ? JSON.parse(texto) : null

    if (!resposta.ok) {
        throw new ApiError(resposta.status, dados?.mensagem ?? 'Algo deu errado. Tente novamente.', dados?.erros ?? {})
    }
    return dados
}

export async function baixarArquivo(caminho, params, nomeArquivo) {
    const resposta = await requisitar(caminho, { params })

    if (!resposta.ok) {
        const dados = await resposta.json().catch(() => null)
        throw new ApiError(resposta.status, dados?.mensagem ?? 'Não foi possível baixar o arquivo.')
    }

    const arquivo = await resposta.blob()
    const link = document.createElement('a')
    link.href = URL.createObjectURL(arquivo)
    link.download = nomeArquivo
    link.click()
    URL.revokeObjectURL(link.href)
}