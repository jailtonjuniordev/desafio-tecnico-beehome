# BeeHome Backend — API de Gerenciamento de Tarefas

API REST do desafio técnico BeeHome: autenticação JWT e CRUD de tarefas com filtros, paginação e regras de negócio.

## Stack

- Java 21
- Spring Boot 4.1
- Spring Security (JWT / OAuth2 Resource Server)
- Spring Data JPA + Hibernate
- MySQL 8 + Flyway
- springdoc-openapi (Swagger UI)
- BCrypt (strength 12)

## Como rodar

### 1. Variáveis de ambiente

Copie `.env.example` para `.env` (ou exporte as variáveis):

```bash
cp .env.example .env
```

Variáveis usadas pela aplicação:

| Variável | Descrição |
|----------|-----------|
| `MYSQL_PORT` | Porta do MySQL (ex.: 3306) |
| `MYSQL_DATABASE` | Nome do banco |
| `MYSQL_USER` / `MYSQL_PASSWORD` | Credenciais |
| `JWT_SECRET` | Segredo HS256 (min. ~32 bytes) |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas (frontend) |

### 2. Banco de dados e ambiente Docker

Para subir banco, backend e frontend, veja [Subir o ambiente com Docker](../README.md#subir-o-ambiente-com-docker) no README da raiz.

As migrations Flyway (`V1__users`, `V2__tasks`) rodam na subida da API.

### 3. Aplicação

```bash
# com as variáveis do .env carregadas
set -a && source .env && set +a
./mvnw spring-boot:run
```

API: `http://localhost:8080`

## Documentação interativa

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Postman:** [`postman/BeeHome-Desafio-Tecnico.postman_collection.json`](postman/BeeHome-Desafio-Tecnico.postman_collection.json)

No Swagger, use **Authorize** com o token retornado pelo login (`Bearer <token>`).

## Autenticação

1. Registre um usuário em `POST /api/v1/users` (público).
2. Faça login em `POST /api/v1/auth/login` (público) e guarde `token` + `tokenType`.
3. Nas demais rotas, envie:

```http
Authorization: Bearer <accessToken>
```

## Catálogo de endpoints

| Método | Path | Auth | Resumo |
|--------|------|------|--------|
| `POST` | `/api/v1/users` | Não | Registrar usuário |
| `GET` | `/api/v1/users/me` | Sim | Perfil do usuário logado |
| `PUT` | `/api/v1/users/me` | Sim | Atualizar perfil |
| `POST` | `/api/v1/auth/login` | Não | Login e emissão de JWT |
| `POST` | `/api/v1/tasks` | Sim | Criar tarefa |
| `GET` | `/api/v1/tasks` | Sim | Listar e filtrar (paginado; query params opcionais) |
| `GET` | `/api/v1/tasks/{id}` | Sim | Buscar por id |
| `PUT` | `/api/v1/tasks/{id}` | Sim | Atualizar |
| `DELETE` | `/api/v1/tasks/{id}` | Sim | Excluir (hard delete) |

Detalhes de request/response e status de erro estão no Swagger.

## Regras de negócio

### Users

- `email` deve ser único.
- Senha obrigatória no create (min. 8); no update é opcional e, se enviada, é re-hasheada com BCrypt.
- `GET/PUT /me` usam o `sub` do JWT como id do usuário.

### Auth

- Login valida email + senha.
- Credenciais inválidas (usuário inexistente ou senha errada) retornam **400** com mensagem genérica (`Email or password incorrect`), sem vazar qual campo falhou.
- Token JWT inclui `sub` (userId) e `email`.

### Tasks

- Toda tarefa pertence ao usuário autenticado (`assignedTo` = id do JWT).
- Usuário só acessa/altera/exclui as próprias tarefas; tarefa de outro usuário ou inexistente → **404** (`Task not found`).
- Título único **por usuário** → conflito **409**.
- `deadline` não pode ser no passado no create/update → **400**.
- `DELETE` remove a linha fisicamente (hard delete).
- Listagem usa `dynamicSearchFilters` com:
  - `assignedTo` (sempre)
  - `status` (igualdade, opcional)
  - `title` (LIKE parcial case-insensitive, opcional)
  - `deadlineStart` / `deadlineEnd` (range inclusivo no campo `deadline`, opcional)
- Se `deadlineStart` > `deadlineEnd` → **400**.
- Paginação Spring (`page`, `size`, `sort`); padrão `sort=deadline,asc`.
- Status: `PENDING`, `IN_PROGRESS`, `COMPLETED`.
- `createdAt` / `updatedAt` vêm do `BaseEntity` (equivalente ao `createdOn` do enunciado).

## Exemplos (curl)

### Registrar

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"Jailton Junior","email":"jailton@email.com","password":"senha1234"}'
```

### Login

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jailton@email.com","password":"senha1234"}'
```

### Criar tarefa

```bash
curl -s -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"title":"Estudar Spring Boot","description":"JPA e Security","status":"PENDING","deadline":"2026-12-31T23:59:59-03:00"}'
```

### Listar com filtros

```bash
curl -s "http://localhost:8080/api/v1/tasks?page=0&size=10&sort=deadline,asc&status=PENDING&title=Spring&deadlineStart=2026-07-01T00:00:00-03:00&deadlineEnd=2026-12-31T23:59:59-03:00" \
  -H "Authorization: Bearer <TOKEN>"
```

## Estrutura do código

Módulos DDD sob `com.jjdev.beehome_tecnico`:

- `auth` — usuários, login, JWT, security
- `task` — CRUD e filtros de tarefas
- `shared` — BaseEntity/BaseModel, exceptions, BaseRepository, OpenAPI

## Testes

```bash
set -a && source .env && set +a
./mvnw test
```

Testes unitários Mockito cobrem `AuthServiceImpl`, `UserServiceImpl` e `TaskServiceImpl`.
