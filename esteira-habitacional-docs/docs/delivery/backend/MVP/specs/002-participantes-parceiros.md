# SPEC 002 — Participantes e Parceiros

## Objetivo

Cadastrar os participantes mínimos usados nos processos de financiamento.

O módulo proprietário é `parties`. `identityaccess` trata credenciais e papéis; `financingprocess` referencia participantes somente por IDs, sem associações JPA entre módulos.

## Domínio

Modelar:

- `Client`;
- `Broker`;
- `RealEstateAgency`;
- contatos essenciais;
- status ativo/inativo;
- identificadores de negócio somente quando necessários;
- prevenção de duplicidade evidente por empresa.

Não armazenar dados bancários ou documentos pessoais que ainda não sejam necessários ao MVP.

## Casos de uso

- cadastrar cliente;
- localizar cliente por identificador permitido;
- atualizar contatos;
- cadastrar corretor;
- associar corretor a imobiliária;
- ativar ou inativar parceiro;
- listar participantes da empresa com paginação.

## Regras

- todos os registros pertencem a uma empresa;
- CPF/CNPJ, quando armazenado, deve ser tratado como dado sensível de acesso restrito;
- corretor inativo não pode originar novo processo;
- alterações relevantes geram evento histórico quando já vinculadas a processo.

## Interfaces externas

- endpoints internos para cliente e corretor;
- respostas não expõem campos sem finalidade;
- paginação e busca simples.

## Testes obrigatórios

- duplicidade dentro da mesma empresa;
- mesmo identificador em empresas diferentes;
- corretor inativo;
- minimização de dados em respostas.

## Critérios de aceite

- cliente e corretor podem ser recuperados para criação de processo;
- nenhuma consulta cruza empresas;
- contratos HTTP são pequenos e estáveis.
