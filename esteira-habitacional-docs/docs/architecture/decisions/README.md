# Registro de Decisões Arquiteturais

As decisões abaixo são normativas para o backend MVP. Em caso de conflito, o ADR aceito prevalece sobre documentos anteriores, que devem ser corrigidos na mesma alteração.

| ADR | Decisão | Status |
|---|---|---|
| [ADR-001](./ADR-001-build-maven.md) | Maven e build reproduzível | Aceita |
| [ADR-002](./ADR-002-java-spring-boot.md) | Java 25 e Spring Boot 4.1 | Aceita |
| [ADR-003](./ADR-003-flyway.md) | Flyway e migrations SQL globais | Aceita |
| [ADR-004](./ADR-004-problem-details.md) | RFC 9457 para erros HTTP | Aceita |
| [ADR-005](./ADR-005-modularidade.md) | Spring Modulith e ArchUnit | Aceita |
| [ADR-006](./ADR-006-eventos-de-dominio.md) | Eventos internos desde a fundação | Aceita |
| [ADR-007](./ADR-007-modulo-parties.md) | Módulo proprietário `parties` | Aceita |
| [ADR-008](./ADR-008-ciclo-inicial-processo.md) | `RASCUNHO` antes de `EM_TRIAGEM` | Aceita |
| [ADR-009](./ADR-009-submissao-atomica.md) | Submissão e inicialização atômicas | Aceita |
| [ADR-010](./ADR-010-vencimento-pendencia.md) | Vencimento como condição calculada | Aceita |
| [ADR-011](./ADR-011-upload-duas-fases.md) | Upload em duas fases e limpeza de órfãos | Aceita |
| [ADR-012](./ADR-012-bootstrap-plataforma.md) | Provisionamento inicial privilegiado | Aceita |

Novas decisões relevantes devem usar [`../../templates/adr-template.md`](../../templates/adr-template.md).
