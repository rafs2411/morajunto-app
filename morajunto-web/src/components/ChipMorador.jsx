import { primeiroNome } from '../utils/formato'
import { Avatar } from './ui'

export default function ChipMorador({ morador, selecionado, onClick }) {
    return (
        <button
            type="button"
            onClick={onClick}
            style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '5px 12px 5px 5px', borderRadius: 99, border: `1.5px solid ${selecionado ? morador.cor : 'var(--color-border)'}`, background: selecionado ? morador.cor + '14' : '#fff', color: selecionado ? morador.cor : 'var(--color-text)', fontWeight: 600, fontSize: 14, transition: 'all .15s' }}
        >
            <Avatar nome={morador.nome} cor={morador.cor} size={22} />
            {primeiroNome(morador.nome)}
        </button>
    )
}