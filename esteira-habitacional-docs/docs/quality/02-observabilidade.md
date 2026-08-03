# Observabilidade

## 1. Objetivo

Permitir diagnóstico sem depender de reprodução manual ou acesso ao banco.

## 2. Logs estruturados

Campos mínimos:

- timestamp;
- severity;
- service;
- environment;
- correlationId;
- userId quando permitido;
- tenantId/organizationId;
- useCase;
- outcome;
- errorType.

Não registrar documentos, CPF completo, tokens ou dados financeiros.

## 3. Métricas

- latência por endpoint;
- taxa de erro;
- falhas de upload;
- documentos processados;
- jobs executados;
- notificações enviadas/falhas;
- conexões de banco;
- uso de armazenamento.

## 4. Tracing

Preparar instrumentação para rastrear:

- requisição HTTP;
- caso de uso;
- persistência;
- armazenamento;
- serviço externo.

## 5. Health checks

Separar:

- liveness;
- readiness;
- dependências críticas.
