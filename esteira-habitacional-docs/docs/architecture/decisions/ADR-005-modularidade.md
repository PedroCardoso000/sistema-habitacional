# ADR-005 — Spring Modulith e ArchUnit

## Status

Aceita

## Contexto

Pacotes bem nomeados não impedem ciclos, acessos indevidos entre módulos ou contaminação das camadas internas.

## Decisão

- usar Spring Modulith 2.1.x, inicialmente 2.1.0, para declarar, verificar, testar e documentar módulos de negócio;
- usar ArchUnit para regras internas: domínio sem Spring/JPA/HTTP, controllers sem repositórios, aplicação sem adapters concretos e entidades JPA restritas à infraestrutura;
- módulos expõem somente APIs deliberadas e não fazem associações JPA cruzadas;
- executar verificações em testes e pipeline;
- não habilitar verificação estrutural no startup de produção inicialmente;
- importar o BOM compatível do Spring Modulith e validar compatibilidade no build da fundação.

## Alternativas consideradas

- apenas ArchUnit: insuficiente para expressar e documentar o modelo modular completo.
- apenas Spring Modulith: insuficiente para todas as regras internas específicas.
- verificação em runtime: adiada por adicionar risco operacional sem benefício atual.

## Consequências positivas

Violações arquiteturais falham cedo. A estrutura de packages vira contrato executável.

## Consequências negativas

Mudanças legítimas de fronteira exigem atualizar declarações, APIs públicas e testes arquiteturais.

## Riscos

Testes superficiais podem criar falsa confiança; regras de autorização e domínio continuam exigindo testes próprios.

## Evidências e critérios de revisão

- [Spring Modulith — verificação de módulos](https://docs.spring.io/spring-modulith/reference/verification.html)
- [Spring Modulith 2.1.0](https://docs.spring.io/spring-modulith/reference/)
- revisar se as ferramentas impedirem uma fronteira de negócio comprovadamente necessária.

## Data

2026-08-03
