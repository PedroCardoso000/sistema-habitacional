# ADR-011 — Upload em duas fases e limpeza de órfãos

## Status

Aceita

## Contexto

Enviar binários pelo fluxo transacional da API aumenta carga e não resolve atomicidade entre banco e armazenamento de objetos.

## Decisão

O upload usa intenção e confirmação:

1. `POST /document-requests/{id}/uploads` autoriza, cria `UploadIntent`, chave privada e URL temporária com limites;
2. cliente envia diretamente ao armazenamento privado;
3. `POST /uploads/{uploadId}/complete` consulta metadados, valida existência, tamanho, media type e checksum disponível e então cria a versão documental.

Estados: `PENDING_UPLOAD`, `UPLOADED`, `COMPLETED`, `EXPIRED`, `REJECTED` e `ABORTED`.

Um job idempotente identifica intenções expiradas/canceladas e objetos sem confirmação, solicita remoção, registra resultado, repete falhas com segurança e publica métricas. Chaves usam apenas IDs não sensíveis, por exemplo `organizations/{organizationId}/uploads/{uploadId}/{randomId}`.

## Alternativas consideradas

- upload integral pela API: rejeitado como padrão para armazenamento de objetos.
- URL pública permanente: proibida por segurança.

## Consequências positivas

Binários não atravessam a API de domínio e a confirmação não confia em metadados enviados pelo cliente.

## Consequências negativas

O sistema aceita consistência temporária controlada e precisa de estados, job e métricas de limpeza.

## Riscos

Falhas na expiração ou remoção acumulam objetos e custo; chaves ou logs mal definidos podem vazar dados sensíveis.

## Evidências e critérios de revisão

Revisar se o armazenamento escolhido não suportar URL assinada ou consulta segura de metadados.

## Data

2026-08-03
