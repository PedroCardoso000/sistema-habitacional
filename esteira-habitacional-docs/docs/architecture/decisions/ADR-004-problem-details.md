# ADR-004 — RFC 9457 para erros HTTP

## Status

Aceita

## Contexto

Frontend e integrações precisam de um contrato de erro estável, seguro e correlacionável.

## Decisão

Responder erros HTTP conhecidos como `application/problem+json`, seguindo RFC 9457, com os campos padrão `type`, `title`, `status`, `detail` e `instance` e as extensões:

- `code`: código estável consumível pelo cliente;
- `traceId`: correlação técnica;
- `timestamp`: instante UTC;
- `violations`: lista opcional com `field`, `code` e `message`.

Um único handler global traduz exceções conhecidas. Controllers não montam erros. Respostas nunca expõem stack trace, SQL, tokens, segredos ou dados pessoais.

Mapeamento inicial: entrada inválida `400`, não autenticado `401`, proibido `403`, inexistente `404`, conflito/duplicidade `409`, regra não satisfeita `422`, tamanho de upload `413`, mídia não suportada `415` e erro inesperado `500`.

## Alternativas consideradas

- envelope proprietário: rejeitado por duplicar um padrão estabelecido.
- usar apenas status HTTP: insuficiente para validações e códigos estáveis de negócio.

## Consequências positivas

Frontend e integrações recebem formato previsível, códigos estáveis e correlação técnica.

## Consequências negativas

Cada problem type público precisa de URI, documentação e semântica estáveis.

## Riscos

Detalhes mal redigidos podem expor informações internas. Status HTTP e código de domínio não podem ser tratados como equivalentes.

## Evidências e critérios de revisão

- [RFC 9457 — Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457.html)
- revisar somente se o contrato público exigir compatibilidade diferente.

## Data

2026-08-03
