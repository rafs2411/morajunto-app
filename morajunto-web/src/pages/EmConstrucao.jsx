import { Btn, Card, PageHeader } from '../components/ui'
import { useMoradia } from '../hooks/useMoradia'
import { useToast } from '../hooks/useToast'

export default function EmConstrucao({ titulo }) {
    const { ativa, ehDono } = useMoradia()
    const toast = useToast()

    return (
        <div>
            <PageHeader
                titulo={titulo}
                subtitulo={ativa ? `Moradia ativa: ${ativa.nome}${ehDono ? ' (você é o dono)' : ''}` : 'Nenhuma moradia ativa'}
            />
            <Card style={{ padding: 24 }}>
                <p style={{ color: 'var(--color-muted)', marginBottom: 16 }}>Esta tela será feita nos próximos passos.</p>
                <Btn variant="secondary" onClick={() => toast('Aviso de teste funcionando!')}>Testar aviso</Btn>
            </Card>
        </div>
    )
}