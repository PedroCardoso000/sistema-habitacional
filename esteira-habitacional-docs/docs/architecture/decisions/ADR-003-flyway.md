# ADR-003 — Flyway e migrations SQL globais

## Status

Aceita

## Contexto

O esquema PostgreSQL precisa evoluir por mudanças explícitas, incrementais e reproduzíveis desde banco vazio.

## Decisão

- usar Flyway com migrations SQL em `src/main/resources/db/migration`;
- manter sequência global no MVP: `V001__descricao.sql`, `V002__descricao.sql` e assim por diante;
- nunca alterar migration já aplicada; correções usam nova versão;
- separar DDL e carga/ajuste de dados quando isso melhorar segurança operacional;
- validar a sequência completa em PostgreSQL real com Testcontainers;
- desabilitar inicialização automática de schemas por bibliotecas; inclusive as tabelas do Event Publication Registry são criadas por Flyway;
- não usar migrations específicas por módulo do Spring Modulith inicialmente;
- usar dependências de Flyway geridas pelo Spring Boot, incluindo o módulo PostgreSQL exigido pela versão adotada.

## Alternativas consideradas

- Liquibase: rejeitado por adicionar abstração sem benefício necessário ao MVP.
- migrations por módulo: adiadas até que os limites estejam estabilizados.

## Consequências positivas

O histórico do banco é linear, explícito e fácil de inspecionar.

## Consequências negativas

A sequência global exige coordenação de numeração entre features.

## Riscos

Alterar migration aplicada ou permitir criação automática de schema destrói a reprodutibilidade e deve falhar na pipeline.

## Evidências e critérios de revisão

- [Flyway migrate](https://documentation.red-gate.com/flyway/reference/commands/migrate)
- [Integração Flyway com Spring Boot](https://documentation.red-gate.com/flyway/reference/usage/community-plugins-and-integrations/community-plugins-and-integrations-spring-boot)
- revisar se a sequência global causar conflito operacional real.

## Data

2026-08-03
