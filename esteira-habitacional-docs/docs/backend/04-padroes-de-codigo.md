# Padrões de Código Backend

## 1. Nomenclatura

- classes: substantivos claros;
- casos de uso: verbo + objeto;
- métodos: comportamento de negócio;
- booleanos: `is`, `has`, `can`, `should`;
- interfaces de saída: sufixo funcional, não técnico quando possível.

Exemplos:

- `ApproveDocumentUseCase`
- `DocumentStorage`
- `FinancingProcessRepository`
- `ProcessAccessPolicy`

## 2. Métodos

- um nível de abstração por método;
- evitar parâmetros booleanos;
- evitar métodos com muitos parâmetros;
- preferir comandos e objetos de valor;
- retornos previsíveis;
- sem efeitos colaterais escondidos.

## 3. Comentários

Comentários devem explicar **por quê**, não repetir o código.

## 4. Mapeamento

Mapeamentos devem ser explícitos:

- request -> command;
- domain -> response;
- persistence entity <-> domain.

Evitar reflection mágica em pontos críticos.

## 5. APIs

- recursos e ações coerentes;
- erros padronizados;
- validação de entrada;
- idempotência em operações sensíveis;
- paginação;
- filtros explícitos;
- versionamento somente quando necessário.

## 6. Persistência

- repositório por agregado;
- evitar lógica de domínio em query SQL ou callback JPA;
- migrations versionadas;
- índices baseados em consultas reais;
- exclusão lógica apenas quando houver justificativa de negócio;
- histórico imutável.

## 7. Pull requests

Cada PR deve:

- ter objetivo único;
- manter arquitetura;
- incluir testes;
- atualizar documentação quando necessário;
- não introduzir dependência sem justificativa;
- passar formatação, análise estática e build.
