# ADR-006 — Eventos internos desde a fundação

## Status

Aceita

## Contexto

Features anteriores à timeline já produzem fatos relevantes. Adiar o contrato de eventos criaria acoplamento e retrabalho.

## Decisão

- definir `DomainEvent` puro e uma porta `DomainEventPublisher` desde a fundação;
- agregados produzem eventos imutáveis com IDs, organização, ator e instante quando aplicável;
- aplicação coleta e publica eventos somente após executar o comportamento de domínio;
- adapter de infraestrutura usa eventos do Spring/Spring Modulith;
- usar Event Publication Registry para entrega durável aos listeners transacionais registrados;
- não usar Kafka, RabbitMQ, broker externo ou event sourcing no MVP;
- a entrega 007 adiciona projeção append-only para timeline e regras de visibilidade.

O Event Publication Registry não é event store: ele registra uma publicação por listener transacional elegível. Sem listener, não existe histórico durável. Portanto, nenhum ambiente com dados relevantes pode operar antes de a projeção da entrega 007 estar instalada, e a timeline nunca deve ser reconstruída a partir do log técnico do registry.

O schema do registry é criado por migration Flyway versionada; inicialização automática de schema pelo Spring Modulith permanece desabilitada.

## Alternativas consideradas

- esperar a entrega 007: rejeitado porque obrigaria features anteriores a inventar acoplamentos temporários.
- event sourcing: rejeitado por complexidade e por não ser requisito.

## Consequências positivas

Módulos reagem a fatos sem acoplamento direto, mantendo eventos, timeline e logs como conceitos separados.

## Consequências negativas

Listeners precisam ser idempotentes, observáveis e acompanhados de migrations Flyway.

## Riscos

Confundir o registry com event store causaria perda de histórico. Eventos sem listener transacional não ganham registro durável automaticamente.

## Evidências e critérios de revisão

- [Spring Modulith — eventos e Event Publication Registry](https://docs.spring.io/spring-modulith/reference/events.html)
- revisar se houver integração externa real ou necessidade comprovada de broker.

## Data

2026-08-03
