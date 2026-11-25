export const PALETA = [
    { hex: '#FF5A7E', nome: 'Rosa' },
    { hex: '#3B82F6', nome: 'Azul' },
    { hex: '#10B981', nome: 'Verde' },
    { hex: '#F59E0B', nome: 'Âmbar' },
    { hex: '#8B5CF6', nome: 'Violeta' },
    { hex: '#EF4444', nome: 'Vermelho' },
    { hex: '#14B8A6', nome: 'Turquesa' },
    { hex: '#F97316', nome: 'Laranja' },
    { hex: '#EC4899', nome: 'Pink' },
    { hex: '#6366F1', nome: 'Índigo' }
]

export function nomeDaCor(hex) {
    return PALETA.find(cor => cor.hex.toLowerCase() === hex?.toLowerCase())?.nome ?? hex
}