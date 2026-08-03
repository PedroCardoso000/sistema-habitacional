# Backend — Esteira Habitacional

Fundação Java do monólito modular da Esteira Habitacional, implementada conforme a SPEC 000.

## Requisitos

- Java 25 LTS em uma distribuição OpenJDK compatível;
- Docker com suporte a Compose e Testcontainers;
- nenhum Maven global é necessário.

## Validação completa

No Windows:

```powershell
.\mvnw.cmd clean verify
```

Em Linux ou macOS:

```bash
./mvnw clean verify
```

Esse é o comando oficial e executa compilação, testes unitários, testes de integração com PostgreSQL real, verificação de migrations, Spring Modulith, ArchUnit, Checkstyle e empacotamento.

## Execução local

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

Endpoints técnicos:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /v3/api-docs`

## Configuração

| Variável | Padrão local |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/esteira_habitacional` |
| `DATABASE_USERNAME` | `esteira` |
| `DATABASE_PASSWORD` | `esteira` |

Credenciais padrão existem somente para desenvolvimento local. Ambientes reais devem injetar segredos externamente.

## Limites desta entrega

Não existem autenticação real, entidades de negócio, storage de documentos, notificações ou endpoints funcionais do produto. O Event Publication Registry garante entrega aos listeners transacionais registrados; não é event store nem timeline.

