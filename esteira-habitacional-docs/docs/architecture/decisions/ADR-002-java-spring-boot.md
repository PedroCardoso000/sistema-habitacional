# ADR-002 — Java 25 e Spring Boot 4.1

## Status

Aceita

## Contexto

Um produto novo deve partir de uma linha LTS atual, sem se vincular a uma distribuição comercial específica do JDK.

## Decisão

- Java 25 LTS como versão major do projeto;
- Spring Boot 4.1.x, iniciando em 4.1.0;
- Spring Framework e dependências Spring geridos pelo Spring Boot;
- usar distribuição OpenJDK compatível, como Temurin, Corretto ou Red Hat OpenJDK;
- não usar recursos preview do Java no MVP;
- Maven Enforcer e compilador devem rejeitar versão incompatível.

## Alternativas consideradas

- Java 21: tecnicamente válido, mas é o LTS anterior e não oferece vantagem concreta para este greenfield.
- Oracle JDK obrigatório: rejeitado para evitar dependência de fornecedor e licenciamento.

## Consequências positivas

O projeto começa em uma linha LTS atual e independente de fornecedor de JDK.

## Consequências negativas

Ambientes locais e CI precisam oferecer Java 25. Atualizações patch de Boot exigem build e testes completos.

## Riscos

Ferramentas periféricas podem demorar a suportar a nova linha; incompatibilidades comprovadas devem bloquear a fundação, não ser contornadas silenciosamente.

## Evidências e critérios de revisão

- [Spring Boot 4.1.0 — documentação](https://docs.spring.io/spring-boot/reference/index.html)
- [Oracle Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)
- revisar ao fim do suporte da linha ou diante de incompatibilidade comprovada de ferramenta crítica.

## Data

2026-08-03
