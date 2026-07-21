# desafio-tecnico-beehome

Sistema de gerenciamento de tarefas (desafio tecnico BeeHome): backend Spring Boot + frontend Angular.

## Repositorios / pastas

| Pasta | Descricao |
|-------|-----------|
| [`backend/`](backend/) | API REST (Java 21, Spring Boot, MySQL, JWT) |
| [`frontend/`](frontend/) | Interface Angular |
| [`docker-compose.yml`](docker-compose.yml) | Stack completa para testes (banco, backend e frontend) |

## Backend

Documentacao completa da API (endpoints, regras de negocio, Swagger e exemplos):

- [backend/README.md](backend/README.md)
- Swagger UI (com a API no ar): http://localhost:8080/swagger-ui.html
- Collection Postman: [backend/postman/BeeHome-Desafio-Tecnico.postman_collection.json](backend/postman/BeeHome-Desafio-Tecnico.postman_collection.json)

## Subir o ambiente com Docker

O `docker-compose.yml` sobe os servicos juntos para facilitar os testes. Configure o `.env` na raiz (ou use o de `backend/.env.example` como base).

**Banco + Backend + Frontend** (padrao):

```bash
docker compose up -d --build
```

- MySQL: `localhost:3306`
- API: http://localhost:8080
- Frontend: http://localhost:4200