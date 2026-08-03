# ADR-007 — Módulo proprietário `parties`

## Status

Aceita

## Contexto

Cliente, corretor e imobiliária são participantes de negócio, não identidades autenticadas nem partes internas do agregado de financiamento.

## Decisão

Criar o módulo `parties`, proprietário de:

- `Client`;
- `Broker`;
- `RealEstateAgency`;
- contatos e dados profissionais necessários.

`identityaccess` mantém usuário, papel, permissão e vínculo com organização. `financingprocess` referencia participantes apenas por IDs de domínio. Não existem associações JPA atravessando módulos.

Cada registro pertence à organização que o cadastrou. O mesmo CPF/CNPJ em empresas diferentes não produz cadastro global compartilhado no MVP.

## Alternativas consideradas

- colocar participantes em identidade: mistura pessoa de negócio com credencial.
- colocá-los no processo: impede catálogo reutilizável da organização.
- cadastro global: rejeitado por privacidade, segregação e complexidade prematura.

## Consequências positivas

Participantes ficam separados de credenciais e podem ser reutilizados por processos da mesma organização.

## Consequências negativas

Consultas entre módulos precisam de contratos explícitos e não podem usar joins JPA por conveniência.

## Riscos

Uma deduplicação global acidental violaria segregação e privacidade; duplicidade é avaliada somente dentro da organização.

## Evidências e critérios de revisão

Revisar apenas se surgir requisito comprovado de cadastro mestre multiempresa.

## Data

2026-08-03
