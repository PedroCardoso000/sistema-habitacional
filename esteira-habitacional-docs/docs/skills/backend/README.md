# Skills do Backend

Este diretório contém instruções reutilizáveis para orientar agentes de IA e desenvolvedores na implementação e revisão de funcionalidades do backend da Esteira Habitacional.

## Objetivo

Reduzir repetição de contexto e consumo de tokens, mantendo uma forma consistente de trabalho:

```text
Domínio → Aplicação/Casos de uso → Portas → Adaptadores → Infraestrutura → Testes → Documentação
```

A ordem é obrigatória. A implementação parte do negócio e termina na validação completa da funcionalidade.

## Skills disponíveis

| Skill | Finalidade |
|---|---|
| [`backend-feature-delivery`](./backend-feature-delivery/SKILL.md) | Implementar uma feature backend de ponta a ponta, iniciando pelo domínio e concluindo com infraestrutura, testes e documentação. |
| [`backend-feature-review`](./backend-feature-review/SKILL.md) | Revisar uma feature existente e verificar se toda a esteira arquitetural foi cumprida sem atalhos. |

## Documentação obrigatória consultada pelas skills

As skills devem usar como fonte de regras:

- [`../../backend/01-arquitetura-backend.md`](../../backend/01-arquitetura-backend.md)
- [`../../backend/02-organizacao-de-pacotes.md`](../../backend/02-organizacao-de-pacotes.md)
- [`../../backend/03-boas-praticas-java.md`](../../backend/03-boas-praticas-java.md)
- [`../../backend/04-padroes-de-codigo.md`](../../backend/04-padroes-de-codigo.md)
- [`../../backend/05-testes-backend.md`](../../backend/05-testes-backend.md)
- [`../../architecture/02-modulos-do-dominio.md`](../../architecture/02-modulos-do-dominio.md)
- [`../../architecture/03-dependencias-e-limites.md`](../../architecture/03-dependencias-e-limites.md)
- [`../../quality/01-seguranca-privacidade.md`](../../quality/01-seguranca-privacidade.md)
- [`../../quality/03-definition-of-done.md`](../../quality/03-definition-of-done.md)

## Regra de economia de contexto

Prompts de feature não devem repetir arquitetura, padrões, convenções ou estratégia de testes. Devem informar apenas:

1. objetivo da feature;
2. ator envolvido;
3. regras de negócio específicas;
4. entradas e saídas esperadas;
5. critérios de aceite;
6. exceções conhecidas.

As demais regras são herdadas destas skills e da documentação referenciada.
