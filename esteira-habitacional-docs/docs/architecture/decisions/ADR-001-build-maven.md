# ADR-001 — Maven e build reproduzível

## Status

Aceita

## Contexto

O backend precisa de um build único, auditável e reproduzível localmente e no CI, sem depender de ferramenta instalada globalmente.

## Decisão

- usar Apache Maven 3.9.16;
- versionar Maven Wrapper e executar oficialmente por `./mvnw` ou `mvnw.cmd`;
- manter um `pom.xml` central para o monólito modular;
- usar o parent/BOM do Spring Boot para versões gerenciadas;
- usar Maven Enforcer para validar Java, Maven e convergência de dependências;
- declarar explicitamente versões de plugins não geridos pelo parent.

## Alternativas consideradas

- Gradle: rejeitado por não oferecer vantagem suficiente para compensar outra linguagem e maior liberdade de configuração.
- Maven instalado globalmente: rejeitado por prejudicar reprodutibilidade.

## Consequências positivas

O build fica previsível, reproduzível e simples de auditar.

## Consequências negativas

Atualizações do Maven Wrapper e de plugins exigem mudanças explícitas de infraestrutura.

## Riscos

Plugins fora do gerenciamento podem divergir se o Enforcer e a atualização de dependências forem negligenciados.

## Evidências e critérios de revisão

- [Apache Maven 3.9.16 — release notes](https://maven.apache.org/docs/3.9.16/release-notes.html)
- revisar ao trocar a linha principal do Spring Boot ou se o Maven deixar de atender ao build.

## Data

2026-08-03
