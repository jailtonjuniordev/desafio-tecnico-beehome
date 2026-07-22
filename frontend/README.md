# BeeHome Frontend

Aplicação Angular 19 para gestão de tarefas (desafio técnico BeeHome).

## Requisitos

- **Ambiente completo:** Docker e Docker Compose (veja o README da raiz)
- **Desenvolvimento local:** Node.js **20 LTS** ou **22 LTS** e API em `http://localhost:8080`

## Como executar

### Ambiente completo (recomendado)

Para subir banco, backend e frontend, veja [Subir o ambiente com Docker](../README.md#subir-o-ambiente-com-docker) no README da raiz.

Frontend: `http://localhost:4200`

### Desenvolvimento local (opcional)

```bash
nvm use 22   # ou 20
npm install
npm start
```

A aplicação sobe em `http://localhost:4200`. A API precisa estar em `http://localhost:8080`.

## Build de produção

```bash
npm run build
```

Artefatos em `dist/beehome`.

## Estrutura

```text
src/app/
  core/           # auth guard, interceptor JWT, AuthService
  features/
    auth/         # login, registro e perfil
    tasks/        # listagem, filtros, modal CRUD
  shared/         # dialog de confirmação, snackbar e header
  environments/   # apiUrl
```

## Contrato da API

Base URL: `http://localhost:8080/api/v1` (configurável em `src/environments/environment.ts`).

### Auth

| Método | Path | Body / resposta |
|--------|------|-----------------|
| POST | `/api/v1/users` | `{ username, email, password }` (registro; senha min. 8) |
| POST | `/api/v1/auth/login` | `{ email, password }` -> `{ token, tokenType, expiration }` |
| GET | `/api/v1/users/me` | Perfil do usuário logado (JWT) |
| PUT | `/api/v1/users/me` | Atualizar perfil (`username`, `email`, `password` opcional) |

Após o registro, o frontend faz login automático para obter o JWT.

Nas rotas protegidas, envie:

```http
Authorization: Bearer <token>
```

### Tasks (requer JWT)

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/v1/tasks?page=&size=&sort=deadline,asc\|desc&status=` | Listagem paginada |
| GET | `/api/v1/tasks/{id}` | Detalhe |
| POST | `/api/v1/tasks` | Criar `{ title, description, status, deadline }` |
| PUT | `/api/v1/tasks/{id}` | Atualizar |
| DELETE | `/api/v1/tasks/{id}` | Excluir |

- Status: `PENDING` \| `IN_PROGRESS` \| `COMPLETED`
- `deadline`: ISO-8601 datetime (ex.: `2026-12-31T23:59:59.000Z`)
- Resposta inclui `createdAt` (não `createdOn`)

Resposta de listagem esperada (estilo Spring Page):

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 10,
  "number": 0
}
```

## Funcionalidades

- Login e registro com validação e feedback de erro
- Edição de dados do usuário em `/profile`
- CRUD de tarefas com tabela Material, paginação, filtro por status e ordenação por prazo (deadline)
- Modal reutilizável para criar/editar (deadline não pode ser no passado)
- SnackBar para sucesso e erros
- Rotas `/tasks` e `/profile` protegidas por `authGuard`
