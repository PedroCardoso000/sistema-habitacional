---
name: backend-feature-delivery
description: Implementa uma funcionalidade Java de ponta a ponta na Esteira Habitacional, começando pelo domínio e finalizando com adapters, infraestrutura, testes e documentação.
version: 1.0.0
language: pt-BR
---

# Skill: Entrega Completa de Feature Backend

## 1. Propósito

Ensinar o agente a implementar qualquer funcionalidade do backend de forma consistente, completa e econômica em tokens.

Toda feature deve nascer no domínio. Nenhum controller, entidade JPA, endpoint ou detalhe de framework deve ser criado antes de compreender e modelar o comportamento de negócio.

## 2. Quando aplicar

Usar esta skill quando a solicitação envolver:

- criação de nova funcionalidade;
- ampliação de comportamento existente;
- novo fluxo de domínio;
- novo caso de uso;
- nova regra de processo, documento, etapa, pendência, participante ou notificação;
- novo endpoint que represente comportamento de negócio.

Não usar esta skill isoladamente para:

- correção puramente visual;
- atualização de dependência sem mudança funcional;
- ajuste exclusivo de infraestrutura;
- refatoração técnica sem alteração de comportamento.

## 3. Fontes obrigatórias

Antes de implementar, ler somente as partes necessárias de:

- `docs/backend/01-arquitetura-backend.md`
- `docs/backend/02-organizacao-de-pacotes.md`
- `docs/backend/03-boas-praticas-java.md`
- `docs/backend/04-padroes-de-codigo.md`
- `docs/backend/05-testes-backend.md`
- `docs/architecture/02-modulos-do-dominio.md`
- `docs/architecture/03-dependencias-e-limites.md`
- `docs/quality/01-seguranca-privacidade.md`
- `docs/quality/03-definition-of-done.md`

Também consultar a especificação funcional da feature e o código diretamente relacionado.

Não ler o repositório inteiro sem necessidade.

## 4. Princípio central

A implementação deve seguir esta ordem:

```text
1. Entender a regra de negócio
2. Modelar o domínio
3. Criar testes de domínio
4. Criar portas e casos de uso
5. Criar testes de aplicação
6. Criar adapters de entrada e saída
7. Implementar infraestrutura
8. Criar testes de integração e API
9. Executar validações
10. Atualizar documentação
```

A ordem pode ter pequenos ciclos de ida e volta, mas não pode começar pela tecnologia externa.

## 5. Esteira obrigatória da feature

### Etapa 0 — Delimitação

Antes de alterar código, registrar de forma curta:

- problema de negócio;
- ator que executa a ação;
- resultado esperado;
- módulo proprietário da regra;
- agregado afetado;
- invariantes envolvidas;
- dados externos realmente necessários;
- critérios de aceite.

Se a regra não estiver clara, interromper e sinalizar a lacuna. Não inventar comportamento.

### Etapa 1 — Domínio

Identificar ou criar:

- agregado responsável;
- entidades;
- objetos de valor;
- enums de domínio;
- políticas ou specifications;
- eventos de domínio;
- exceções conhecidas.

Regras:

- comportamento deve ficar próximo do estado que protege;
- domínio não depende de Spring, JPA, HTTP, JSON, banco, mensageria ou SDK externo;
- mudanças de estado devem ocorrer por métodos de negócio;
- evitar setters públicos e entidades anêmicas;
- invariantes devem ser protegidas dentro do domínio;
- nomes devem representar a linguagem do negócio;
- usar objetos de valor para conceitos relevantes;
- usar factory apenas quando a criação exigir regras reais.

Exemplo de intenção:

```java
process.rejectDocument(documentId, reason, analystId, occurredAt);
```

Não:

```java
process.setDocumentStatus(REJECTED);
```

#### Saída obrigatória da etapa

- modelo de domínio alterado;
- regras e invariantes explícitas;
- eventos ou exceções necessários;
- testes unitários de domínio positivos e negativos.

### Etapa 2 — Aplicação e casos de uso

Criar ou alterar o caso de uso responsável pela coordenação.

Cada caso de uso deve:

1. receber comando ou consulta explícita;
2. validar pré-condições de aplicação;
3. validar autorização contextual;
4. recuperar agregados por portas de saída;
5. delegar comportamento ao domínio;
6. persistir mudanças;
7. publicar eventos ou acionar efeitos secundários por portas;
8. retornar resultado enxuto.

Regras:

- caso de uso não deve conter regra que pertença à entidade;
- caso de uso não conhece classes JPA, HTTP ou SDKs;
- interfaces devem ser pequenas e específicas;
- comandos não devem carregar entidades técnicas;
- consultas podem usar modelos de leitura próprios;
- transações são coordenadas na aplicação ou na composição técnica, nunca no domínio.

#### Portas de entrada

Exemplos:

- `RejectDocumentUseCase`
- `ChangeProcessStageUseCase`
- `CreatePendingActionUseCase`

#### Portas de saída

Exemplos:

- `FinancingProcessRepository`
- `DocumentStorage`
- `NotificationGateway`
- `AuditLog`
- `CurrentUserProvider`
- `Clock`

#### Saída obrigatória da etapa

- contrato de entrada;
- comando ou consulta;
- implementação do caso de uso;
- portas de saída necessárias;
- testes do caso de uso com fakes ou mocks objetivos.

### Etapa 3 — Interface de entrada

Implementar o adapter que expõe a funcionalidade ao meio externo.

Para HTTP, o controller deve apenas:

1. receber request;
2. validar formato e campos obrigatórios;
3. mapear request para comando;
4. invocar a porta de entrada;
5. mapear resultado para response;
6. converter erros conhecidos para resposta padronizada.

Proibido no controller:

- regra de negócio;
- acesso direto a repositório;
- alteração direta de entidade;
- transação de negócio;
- montagem complexa de fluxo;
- captura genérica e silenciosa de exceções.

#### Saída obrigatória da etapa

- request/response mínimos;
- mapper explícito;
- controller ou outro adapter de entrada;
- validação sintática;
- contrato de erro consistente.

### Etapa 4 — Adapters de saída e infraestrutura

Implementar as portas necessárias com tecnologia concreta.

Pode incluir:

- persistência JPA/PostgreSQL;
- armazenamento privado de documentos;
- notificação;
- auditoria;
- autenticação e autorização técnica;
- relógio e geração de identificadores;
- integrações externas.

Regras:

- entidades JPA não devem substituir o modelo de domínio;
- mapeamento persistence ↔ domain deve ser explícito;
- um repositório deve representar um agregado;
- migrations devem ser versionadas;
- queries devem respeitar segregação por empresa;
- arquivos não podem ter URL pública permanente;
- falhas externas devem ser traduzidas para erros da aplicação ou infraestrutura;
- detalhes externos não podem vazar para o domínio.

#### Saída obrigatória da etapa

- adapter de saída;
- mapeadores;
- configuração Spring mínima;
- migration, índice ou configuração quando necessário;
- tratamento de erro e observabilidade compatíveis.

### Etapa 5 — Testes completos

A feature só está completa quando possuir a combinação adequada de testes.

#### Testes de domínio

Cobrir:

- invariantes;
- transições permitidas;
- transições proibidas;
- casos positivos;
- casos negativos;
- preservação de histórico e versões quando aplicável.

Sem Spring e sem banco.

#### Testes de aplicação

Cobrir:

- coordenação entre portas;
- autorização;
- agregado inexistente;
- persistência;
- eventos e efeitos secundários;
- erros conhecidos.

#### Testes de integração

Adicionar quando houver:

- persistência;
- migrations;
- queries;
- isolamento multiempresa;
- segurança HTTP;
- serialização;
- armazenamento de arquivos.

Preferir Testcontainers para dependências reais.

#### Testes de API/contrato

Cobrir:

- endpoint;
- código HTTP;
- formato de resposta;
- erro padronizado;
- acesso autorizado e negado;
- compatibilidade do contrato.

#### Regra de regressão

Todo bug encontrado durante a implementação deve resultar em teste que falhava antes da correção.

### Etapa 6 — Validação final

Executar, conforme ferramentas do projeto:

- formatação;
- compilação;
- testes unitários;
- testes de integração;
- análise estática;
- verificação de migrations;
- `git diff --check`;
- inspeção do diff da feature.

Não declarar sucesso sem informar exatamente o que foi executado e o resultado.

### Etapa 7 — Documentação

Atualizar somente quando necessário:

- especificação da feature;
- requisitos e critérios de aceite;
- contrato de API;
- ADR, quando houver decisão arquitetural relevante;
- migrations e operações necessárias;
- README do módulo.

## 6. Organização esperada

A feature deve respeitar pacote por módulo e camada dentro do módulo:

```text
com.esteirahabitacional.<module>
├── domain
│   ├── model
│   ├── event
│   ├── policy
│   └── exception
├── application
│   ├── port.in
│   ├── port.out
│   ├── command
│   ├── query
│   └── service
├── adapter
│   ├── in.web
│   └── out.persistence
└── config
```

Não criar pacotes globais por tecnologia, como `controllers`, `services`, `repositories` e `entities`, misturando todos os módulos.

## 7. Regras de legibilidade

O código deve ser human-friendly:

- nomes expressam intenção;
- métodos usam linguagem de negócio;
- uma classe possui uma responsabilidade principal;
- evitar abreviações obscuras;
- evitar parâmetros booleanos;
- preferir comandos e objetos de valor;
- não esconder efeitos colaterais;
- comentários explicam decisão, não repetem código;
- não usar pattern sem necessidade real;
- não usar `@Data` indiscriminadamente no domínio;
- não capturar exceções silenciosamente.

## 8. Regras de segurança e multiempresa

Para toda feature, verificar explicitamente:

- qual empresa é proprietária do dado;
- quem pode visualizar;
- quem pode alterar;
- se o vínculo com o processo é obrigatório;
- quais dados podem ser exibidos ao corretor, cliente ou vendedor;
- se a ação exige auditoria;
- se existem dados pessoais ou documentos;
- se a consulta impede acesso entre empresas.

Toda feature relacionada a processo ou documento deve possuir teste de isolamento quando houver risco multiempresa.

## 9. Limites contra overengineering

Para o MVP:

- manter monólito modular;
- não criar microserviço sem necessidade comprovada;
- não adicionar mensageria apenas para desacoplamento abstrato;
- não aplicar CQRS completo sem benefício concreto;
- não criar abstrações para uma única implementação previsível, salvo quando proteger o domínio de tecnologia externa;
- não criar framework interno;
- não generalizar uma regra antes de existir segunda variação real.

## 10. Formato do relatório final

Ao concluir, responder com:

### Resumo

O comportamento entregue em linguagem de negócio.

### Fluxo implementado

```text
Domínio → Caso de uso → Porta → Adapter → Infraestrutura → Testes
```

Indicar classes principais em cada etapa.

### Arquivos alterados

Lista objetiva agrupada por camada.

### Regras protegidas

Invariantes, autorização e isolamento implementados.

### Testes executados

Comandos e resultados reais.

### Pendências ou riscos

Apenas itens comprovados. Não inventar pendências.

## 11. Critério de conclusão

Uma feature não está completa se faltar qualquer item aplicável:

- comportamento de domínio;
- invariantes;
- caso de uso;
- portas;
- adapter de entrada;
- adapter de saída;
- persistência/configuração necessária;
- autorização;
- auditoria quando aplicável;
- testes de domínio;
- testes de aplicação;
- testes de integração/API quando aplicáveis;
- documentação do contrato;
- validação do build.

“Endpoint funcionando” não significa “feature completa”.
