# Módulos do Domínio

## 1. Princípio

Os módulos representam capacidades de negócio, não camadas técnicas.

## 2. Módulos iniciais

### 2.1 Identity & Access

Responsável por:

- usuários;
- papéis;
- permissões;
- vínculo com empresa;
- revogação de acesso;
- escopo por processo.

Observação: autenticação pode ser delegada a um provedor externo, mas autorização de negócio permanece no sistema.

### 2.2 Organizations

Responsável por:

- empresa correspondente;
- configurações da empresa;
- política de retenção;
- modelos ativos.

### 2.3 Parties

Responsável por:

- clientes;
- corretores parceiros;
- imobiliárias;
- contatos e dados profissionais mínimos;
- ativação, inativação e duplicidade dentro da empresa.

Participantes pertencem à empresa que os cadastrou. Não existe cadastro global compartilhado no MVP. Outros módulos os referenciam apenas por identificadores.

### 2.4 Financing Process

Módulo central.

Responsável por:

- criação do processo;
- origem;
- participantes;
- responsável interno;
- situação geral;
- prioridade;
- próxima ação;
- encerramento, suspensão e reabertura.

Agregado principal sugerido: `FinancingProcess`.

### 2.5 Workflow

Responsável por:

- modelo de fluxo;
- etapas modelo;
- etapas executadas;
- critérios de entrada e saída;
- avanço, retorno e exceção autorizada.

### 2.6 Documents

Responsável por:

- tipos de documento;
- checklist;
- solicitação documental;
- arquivo/documento;
- versionamento;
- análise, aprovação, recusa e reenvio;
- validade.

### 2.7 Pending Actions

Responsável por:

- pendências;
- responsável;
- prioridade;
- prazo;
- reatribuição;
- resolução;
- vencimento.

### 2.8 Timeline & Audit

Responsável por:

- eventos históricos imutáveis;
- linha do tempo;
- autoria;
- data;
- contexto;
- mudanças relevantes.

### 2.9 Notifications

Responsável por:

- eventos notificáveis;
- preferências de canal;
- mensagem;
- entrega;
- status da notificação;
- compartilhamento manual no WhatsApp no MVP.

### 2.10 Reporting

Responsável por consultas consolidadas:

- fila de trabalho;
- processos parados;
- volume por etapa;
- documentos pendentes;
- pendências vencidas;
- tempo por etapa.

### 2.11 Platform Administration

Fronteira privilegiada e sem acesso documental implícito, responsável no MVP apenas pelo provisionamento inicial controlado da primeira empresa e do primeiro administrador.

## 3. Agregados iniciais sugeridos

- `FinancingProcess`
- `DocumentRequest`
- `WorkflowModel`
- `Organization`
- `Client`
- `Broker`

Nem toda entidade precisa ser um agregado separado. O desenho deve priorizar consistência e transações curtas.

## 4. Eventos de domínio candidatos

- `FinancingProcessCreated`
- `FinancingProcessSubmitted`
- `WorkflowInitialized`
- `ProcessStageChanged`
- `NextActionDefined`
- `DocumentRequested`
- `DocumentSubmitted`
- `DocumentApproved`
- `DocumentRejected`
- `PendingActionCreated`
- `PendingActionResolved`
- `ProcessCompleted`
- `ProcessCancelled`

Eventos de domínio não significam obrigatoriamente mensageria externa. No MVP, podem ser publicados internamente na mesma aplicação.
