# Arquitetura do Backend

## 1. Objetivo

Construir um backend Java cujo centro seja o domínio de financiamento habitacional, mantendo regras de negócio independentes de tecnologia.

## 2. Estrutura conceitual

```text
Domain
  ↑
Application
  ↑
Adapters
  ↑
Infrastructure / Bootstrap
```

## 3. Camada de domínio

Contém:

- entidades;
- agregados;
- objetos de valor;
- enums de domínio;
- serviços de domínio;
- políticas;
- eventos de domínio;
- exceções de domínio.

Exemplos:

- `FinancingProcess`
- `ProcessStage`
- `DocumentRequest`
- `PendingAction`
- `ProcessStatus`
- `DocumentStatus`
- `ProcessParticipant`

O comportamento deve ficar próximo dos dados que protege.

Evitar entidades anêmicas com apenas getters e setters.

## 4. Camada de aplicação

Organiza casos de uso.

Exemplos:

- `CreateFinancingProcess`
- `RequestDocument`
- `SubmitDocument`
- `ApproveDocument`
- `RejectDocument`
- `ChangeProcessStage`
- `CreatePendingAction`
- `ResolvePendingAction`
- `CompleteFinancingProcess`

Cada caso de uso deve:

1. receber comando ou consulta explícita;
2. verificar autorização e pré-condições;
3. recuperar o estado necessário;
4. executar comportamento de domínio;
5. persistir mudanças;
6. publicar eventos;
7. retornar resultado enxuto.

## 5. Adaptadores de entrada

Controllers devem:

- receber HTTP;
- validar formato;
- mapear request para comando;
- chamar caso de uso;
- mapear resultado para response;
- converter erros conhecidos para respostas HTTP.

Controllers não devem conter regra de negócio.

## 6. Adaptadores de saída

Implementam portas para:

- persistência;
- armazenamento de arquivo;
- envio de notificação;
- autenticação;
- auditoria;
- relógio;
- geração de identificadores.

Eventos de domínio são objetos puros. A aplicação os entrega pela porta `DomainEventPublisher`; o adapter Spring Modulith fornece publicação interna e registro durável apenas para listeners transacionais registrados. Esse registro não substitui histórico de negócio nem event store.

## 7. Bootstrap

Responsável por:

- configuração Spring;
- composição de dependências;
- filtros;
- segurança técnica;
- observabilidade;
- inicialização.

## 8. CQRS pragmático

Não é necessário adotar CQRS completo.

Pode-se separar:

- comandos com comportamento de domínio;
- consultas otimizadas para telas e relatórios.

Consultas de leitura podem usar projeções próprias, desde que não permitam alterar regras de negócio por atalhos.
