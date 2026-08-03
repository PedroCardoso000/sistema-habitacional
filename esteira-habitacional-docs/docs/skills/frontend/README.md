# Skills do Frontend

Este diretório contém as instruções reutilizáveis para implementação de funcionalidades no frontend da Esteira Habitacional.

## Skill principal

- [`frontend-feature-delivery/SKILL.md`](./frontend-feature-delivery/SKILL.md): orienta a implementação completa de uma feature de interface, desde a leitura dos contratos e estados da experiência até componentes, integração, responsividade, acessibilidade e testes.

## Biblioteca e base visual adotadas

A base padrão do frontend será:

- **React com TypeScript**;
- **Next.js**, salvo decisão arquitetural posterior em contrário;
- **Tailwind CSS** para tokens, layout e estilos;
- **shadcn/ui** como coleção-base de componentes com código pertencente ao projeto;
- **Radix UI Primitives** para comportamento acessível dos componentes interativos usados pelo shadcn/ui;
- **Lucide React** para iconografia consistente;
- **TanStack Query** para estado do servidor e sincronização com a API;
- **React Hook Form + Zod** para formulários e validação de entrada.

## Por que shadcn/ui em vez de PrimeReact

A interface definida para o produto possui identidade visual própria, tema escuro e componentes de domínio específicos. O shadcn/ui entrega uma base acessível e editável diretamente no repositório, evitando dependência visual rígida e permitindo que o design system seja realmente do produto.

PrimeReact é válido e possui muitos componentes prontos, mas sua amplitude cria maior risco de dependência da biblioteca, estilos genéricos e customização excessiva. Ele não será a biblioteca padrão do MVP. Uma exceção poderá ser registrada por ADR caso surja necessidade concreta de um componente complexo que não justifique implementação local.

## Princípio central

> A página compõe a feature; a feature coordena comportamento; componentes apresentam estado; clientes de API comunicam com o backend.

Nenhum prompt de implementação deve repetir todas as regras arquiteturais. Ele deve referenciar esta skill e informar apenas o objetivo, os contratos, os critérios de aceite e o escopo da feature.
