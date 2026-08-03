# Componentização e Reutilização

## 1. Meta correta

A meta não é “100% de reutilização”. Isso costuma gerar componentes genéricos demais e acoplados.

A meta é:

> Reutilizar componentes quando eles representam o mesmo conceito visual ou comportamental.

Duplicação pequena e local pode ser melhor que abstração prematura.

## 2. Níveis de componentes

### Primitivos

- Button
- Input
- Select
- Badge
- Card
- Modal
- Tabs
- Tooltip

### Compostos

- FilterBar
- ProcessStatusBadge
- DocumentStatusBadge
- UserAvatar
- EmptyState
- ConfirmationDialog

### Componentes de domínio

- ProcessQueueItem
- FinancingJourney
- DocumentChecklist
- PendingActionCard
- ProcessTimeline
- ProcessSummary

### Páginas

- AnalystQueuePage
- ProcessDetailsPage
- BrokerDashboardPage
- ClientProcessPage

## 3. Regras

- props pequenas e semânticas;
- não passar objetos gigantes por conveniência;
- composição em vez de muitos booleanos;
- estados de loading, erro e vazio obrigatórios;
- acessibilidade embutida;
- comportamento testável;
- variantes controladas pelo design system.
