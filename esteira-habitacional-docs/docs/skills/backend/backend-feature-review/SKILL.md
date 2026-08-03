---
name: backend-feature-review
description: Audita uma feature Java da Esteira Habitacional para verificar aderência ao fluxo domínio-first, arquitetura hexagonal, segurança, testes e completude ponta a ponta.
version: 1.0.0
language: pt-BR
---

# Skill: Revisão de Feature Backend

## 1. Propósito

Revisar uma funcionalidade já implementada e identificar falhas reais de arquitetura, domínio, segurança, testes e completude.

A revisão não deve reescrever a feature por preferência pessoal. Deve apontar somente problemas comprováveis.

## 2. Fontes obrigatórias

Consultar:

- documentação da feature;
- código e testes alterados;
- `docs/backend/01-arquitetura-backend.md` até `05-testes-backend.md`;
- `docs/architecture/03-dependencias-e-limites.md`;
- `docs/quality/01-seguranca-privacidade.md`;
- `docs/quality/03-definition-of-done.md`;
- `docs/skills/backend/backend-feature-delivery/SKILL.md`.

## 3. Ordem da auditoria

### 3.1 Domínio

Verificar:

- a regra nasceu no agregado correto;
- não há entidade anêmica;
- invariantes estão protegidas;
- métodos expressam linguagem do negócio;
- domínio não depende de framework;
- objetos de valor foram usados quando justificáveis;
- transições inválidas são impedidas;
- histórico e versionamento são preservados.

### 3.2 Aplicação

Verificar:

- existe caso de uso explícito;
- controller não coordena regra;
- portas são pequenas e específicas;
- autorização contextual é aplicada;
- caso de uso não conhece JPA/HTTP;
- efeitos secundários usam portas;
- resultado é enxuto.

### 3.3 Adapters e infraestrutura

Verificar:

- mapeamentos são explícitos;
- domínio não foi substituído por entidade JPA;
- persistência respeita agregado;
- migrations e índices existem quando necessários;
- erros externos não vazam;
- segregação por empresa está aplicada;
- documentos permanecem privados;
- observabilidade existe para falhas relevantes.

### 3.4 Testes

Verificar:

- testes de domínio positivos e negativos;
- testes de caso de uso;
- integração real quando aplicável;
- segurança e isolamento multiempresa;
- contrato HTTP;
- regressão para bugs encontrados;
- ausência de testes frágeis acoplados à implementação.

### 3.5 Completude

Confirmar a trilha:

```text
Domínio → Aplicação → Porta → Adapter → Infraestrutura → Testes → Documentação
```

Se uma etapa aplicável estiver ausente, a feature não está completa.

## 4. Classificação dos achados

### Bloqueador

- quebra de regra de negócio;
- acesso indevido entre empresas ou participantes;
- perda ou exposição de documento;
- ausência de teste para regra crítica;
- alteração de estado por atalho fora do domínio;
- migration incompatível ou perda de dados.

### Importante

- caso de uso acoplado à infraestrutura;
- controller com regra;
- ausência de histórico obrigatório;
- erro não padronizado;
- consulta insegura;
- teste de integração ausente em comportamento persistente crítico.

### Melhoria

- nome pouco expressivo;
- duplicação pequena;
- método longo;
- abstração ou mapper que pode ser simplificado.

Não classificar preferência estética como defeito.

## 5. Saída esperada

### Veredito

- Aprovada;
- Aprovada com ajustes;
- Reprovada.

### Esteira verificada

Tabela curta com cada camada e status.

### Achados

Para cada achado:

- severidade;
- evidência concreta;
- impacto;
- correção mínima recomendada.

### Testes e comandos

Informar o que foi executado e o resultado real.

### Lacunas de informação

Indicar o que não foi possível validar sem presumir.
