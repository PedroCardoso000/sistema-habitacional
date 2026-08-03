# Estratégia de Testes Backend

## 1. Pirâmide pragmática

### Testes de domínio

Maior volume.

Cobrem:

- invariantes;
- transições de estado;
- aprovação e recusa documental;
- versionamento;
- conclusão e cancelamento;
- regras de pendência;
- autorização de ações de negócio.

Sem Spring e sem banco.

### Testes de casos de uso

Cobrem coordenação entre portas, autorização, persistência e eventos.

Dependências externas simuladas por fakes ou mocks objetivos.

### Testes de integração

Cobrem:

- persistência real;
- migrações;
- queries;
- isolamento por empresa;
- upload/metadados;
- segurança HTTP;
- serialização.

Usar Testcontainers quando aplicável.

### Testes de contrato/API

Cobrem endpoints, códigos HTTP, erros e compatibilidade com o frontend.

## 2. Regras

- testes devem descrever comportamento;
- não testar implementação privada;
- não buscar cobertura artificial;
- bugs corrigidos recebem teste de regressão;
- regras críticas devem ter casos positivos e negativos;
- autorização multiempresa exige testes obrigatórios.

## 3. Nomenclatura

Formato sugerido:

```text
shouldRejectDocumentWhenReasonIsMissing
shouldPreventBrokerFromAccessingUnrelatedProcess
shouldPreservePreviousDocumentVersionAfterResubmission
```
