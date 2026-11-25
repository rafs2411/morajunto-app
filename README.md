# MoraJunto

Sistema para organizar a vida em casas compartilhadas (repúblicas, apartamentos de estudantes, casas com amigos). Cada moradia tem seus moradores, despesas, tarefas e lista de compras, tudo em um só lugar.

Projeto pessoal feito por **Rafaela Alves Sabino**, estudante de Análise e Desenvolvimento de Sistemas.

## O que o sistema faz

- **Cadastro e login** com autenticação por token JWT.
- **Moradias:** criar uma moradia, definir vagas e acompanhar quem mora nela. Quem cria é o dono; os outros são membros, com permissões diferentes.
- **Entrada na moradia:** convites com validade de 7 dias e pedidos de entrada, que o dono aceita ou recusa.
- **Moradores:** cada morador tem uma cor própria dentro da moradia.
- **Despesas:** divisão igual (o centavo que sobra vai para os primeiros moradores) ou personalizada, pagamentos e saldo de cada um (positivo = tem a receber).
- **Tarefas:** únicas ou semanais, com conclusão por dia, e calendário de até 93 dias.
- **Lista de compras:** adicionar, marcar e remover itens.
- **Dashboard:** resumo da moradia, com cards que levam para cada área.
- **Configurações:** preferências do usuário.
- **Exportação do histórico de despesas** em CSV.

## Tecnologias

**Back-end (`morajunto-api`)**
- Java 21 e Spring Boot 4.1.2
- Spring Security (stateless) com JWT e senhas com BCrypt
- Spring Data JPA / Hibernate e MySQL 8
- Bean Validation com mensagens em português
- Maven e Lombok
- Testes com JUnit 5, Mockito e AssertJ (47 testes)

**Front-end (`morajunto-web`)**
- React 19 e Vite
- JavaScript
- React Router
- ESLint

## Estrutura do repositório

```
morajunto-app/
├── morajunto-api/   API REST em Spring Boot
└── morajunto-web/   Aplicação em React
```

## Como rodar

### Pré-requisitos

- JDK 21 ou superior
- Node.js (versão LTS)
- MySQL 8

### 1. Back-end

O Hibernate está configurado em modo `validate`: ele confere o banco, mas não cria as tabelas. Por isso o banco e as tabelas precisam existir antes de iniciar a API.

Defina as variáveis de ambiente:

| Variável | Obrigatória | Para que serve |
|---|---|---|
| `DB_PASSWORD` | sim | Senha do MySQL |
| `JWT_SECRET` | sim | Chave secreta para assinar os tokens (texto longo e aleatório, com 32 caracteres ou mais) |
| `DB_URL` | não | Endereço do banco |
| `DB_USERNAME` | não | Usuário do banco |
| `JWT_EXPIRACAO_MINUTOS` | não | Tempo de validade do token |
| `CORS_ORIGENS` | não | Endereços do front autorizados a chamar a API |

No Windows (PowerShell), para a sessão atual:

```powershell
$env:DB_PASSWORD = "sua-senha"
$env:JWT_SECRET = "uma-chave-longa-e-aleatoria-com-mais-de-32-caracteres"
```

Depois, dentro da pasta `morajunto-api`:

```powershell
.\mvnw spring-boot:run
```

Ou abra o projeto no IntelliJ e rode a classe principal com as variáveis configuradas. A API sobe em `http://localhost:8080`.

Para rodar os testes:

```powershell
.\mvnw test
```

### 2. Front-end

Dentro da pasta `morajunto-web`, crie um arquivo `.env` com o endereço da API:

```
VITE_API_URL=http://localhost:8080
```

Depois:

```powershell
npm install
npm run dev
```

O front abre em `http://localhost:5173`.

## Segurança

- Nenhuma senha ou chave fica no código: tudo vem de variáveis de ambiente.
- As senhas dos usuários são guardadas com BCrypt.
- A API não guarda sessão; cada requisição leva o token JWT.

## Possíveis melhorias

- Editar uma despesa já criada
- Permitir que um morador saia da moradia
- Cancelar convites e pedidos pendentes
- Aplicar tema e idioma escolhidos nas configurações

## Autora

**Rafaela Alves Sabino**, Brasília - DF
GitHub: [rafs2411](https://github.com/rafs2411)
