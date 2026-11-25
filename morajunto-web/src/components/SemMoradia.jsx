import { useNavigate } from 'react-router-dom'
import { Btn, Card, EmptyState, PageHeader } from './ui'

export default function SemMoradia({ titulo }) {
    const navigate = useNavigate()

    return (
        <div>
            <PageHeader titulo={titulo} />
            <Card>
                <EmptyState
                    icon="🏠"
                    title="Nenhuma moradia ativa"
                    subtitle="Crie uma moradia, aceite um convite ou peça para entrar em uma moradia aberta."
                    action={<Btn onClick={() => navigate('/moradias')}>Ir para Moradias</Btn>}
                />
            </Card>
        </div>
    )
}