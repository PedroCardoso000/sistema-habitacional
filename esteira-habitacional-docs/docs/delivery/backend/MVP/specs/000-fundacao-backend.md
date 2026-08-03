# SPEC 000 — Fundação do Backend

## Objetivo

Criar o backend Java executável do MVP e estabelecer a estrutura arquitetural que todas as features seguintes deverão respeitar.

## Escopo

- aplicação Spring Boot em Java;
- Java 25 LTS e Spring Boot 4.1.x;
- Maven 3.9.16 com Wrapper, Enforcer e build reprodutível;
- monólito modular;
- Spring Modulith e ArchUnit para proteção dos limites;
- pacotes por módulo de negócio;
- separação entre domínio, aplicação, adapters e configuração;
- PostgreSQL como banco relacional;
- Flyway com migrations SQL em sequência global;
- Testcontainers preparado para integração;
- tratamento de erros em RFC 9457 Problem Details;
- healthcheck técnico;
- OpenAPI gerado a partir dos endpoints;
- logging estruturado básico;
- relógio e geração de identificadores abstraídos por portas;
- contrato puro de eventos de domínio e porta `DomainEventPublisher`;
- Event Publication Registry do Spring Modulith preparado para listeners transacionais, sem broker externo.

## Módulos iniciais

- `identityaccess`
- `organizations`
- `parties`
- `financingprocess`
- `workflow`
- `documents`
- `pendingactions`
- `timelineaudit`
- `reporting`
- `platformadministration`
- `shared`

Os módulos podem começar vazios, mas a direção das dependências deve estar protegida.

## Regras

- domínio sem dependência de Spring, JPA, HTTP ou JSON;
- entidades de persistência separadas das entidades de domínio;
- nenhum framework de mapeamento automático é obrigatório;
- `shared` mínimo;
- sem abstrações genéricas prematuras;
- sem microsserviços;
- Event Publication Registry não é event store nem substitui a timeline da entrega 007.
- schemas técnicos, inclusive o registry de eventos, são criados por Flyway; inicialização automática por bibliotecas fica desabilitada.

## Critérios de aceite

- aplicação compila e inicia;
- endpoint de health responde sem acessar regra de negócio;
- migrações executam em PostgreSQL real via Testcontainers;
- teste de arquitetura impede dependência do domínio para camadas externas;
- teste do Spring Modulith impede ciclos e dependências não permitidas entre módulos;
- erros HTTP possuem estrutura consistente;
- pipeline local de testes executa com um único comando documentado.

## Fora do escopo

- autenticação real;
- entidades de negócio completas;
- endpoints funcionais do produto.
