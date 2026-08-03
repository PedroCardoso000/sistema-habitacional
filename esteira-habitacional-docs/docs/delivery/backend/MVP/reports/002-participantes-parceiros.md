# Relatório de Entrega — Participantes e Parceiros

## Resumo

Foram implementados clientes, corretores e imobiliárias pertencentes a uma empresa. Gestores e analistas ativos podem cadastrar, buscar e atualizar participantes, associar corretor a imobiliária, ativar ou inativar parceiros e listar projeções paginadas. O mesmo CPF/CNPJ pode existir em empresas diferentes, mas duplicidades dentro da mesma empresa são bloqueadas.

## Fluxo implementado

```text
Client/Broker/RealEstateAgency → casos de uso → autorização/portas → HTTP/JDBC → PostgreSQL → testes
```

## Arquivos alterados

### Domínio

- `Client`, `Broker`, `RealEstateAgency`, `ContactInfo`, `Cpf`, `Cnpj`, `PartyStatus`.
- Validação de CPF/CNPJ, normalização de contatos, associação com imobiliária e status de parceiro.
- Regra que impede corretor inativo de originar novo processo.

### Aplicação

- Casos de uso de cadastro, busca, atualização de contatos, associação, status e paginação.
- Contratos públicos tenant-scoped para recuperar cliente e corretor ativo pelo futuro módulo de processo.
- Permissões `MANAGE_PARTIES` e `VIEW_PARTIES` integradas ao módulo de identidade.

### Adapters

- Controller interno de participantes e parceiros.
- Repositórios JDBC separados para cliente, corretor e imobiliária.
- Projeção JDBC paginada e adapter de auditoria.

### Infraestrutura

- Migration `V003__create_parties_and_partners.sql`.
- Constraints únicas compostas por empresa e identificador.
- FKs compostas para impedir associação de corretor com imobiliária de outra empresa.
- Índices de listagem por empresa e nome.

### Testes

- Domínio: documentos fiscais, contatos, status, associação e corretor inativo.
- Aplicação: duplicidade, persistência/auditoria e referência de corretor.
- Integração/API: isolamento, duplicidade por tenant, busca, minimização, parceiros, contatos, paginação e auditoria.

### Documentação

- README atualizado com endpoints e tratamento de CPF/CNPJ.
- Este relatório de entrega.

## Regras protegidas

- Toda consulta e alteração recebe `organizationId` explícito.
- O tenant da URL precisa coincidir com o contexto autorizado do ator.
- CPF/CNPJ são únicos somente dentro da empresa correspondente.
- Associação corretor–imobiliária é protegida por empresa na aplicação e no banco.
- Corretor inativo é recusado pelo contrato usado para originar processo.
- Respostas e listagens não expõem CPF/CNPJ; listagens também omitem contatos.
- Ações relevantes registram empresa, ator, alvo, resultado, instante e correlation ID, sem documento fiscal.
- Paginação aceita páginas não negativas e tamanho entre 1 e 100.

## Segurança e autorização

Somente gestores e analistas ativos possuem permissões de participantes. Administrador técnico, corretor, cliente e vendedor não recebem acesso implícito. A busca por CPF usa corpo da requisição, evitando o identificador sensível no caminho da URL. Nenhuma resposta ou auditoria inclui CPF/CNPJ.

## Testes executados

| Comando | Resultado |
|---|---|
| `.\mvnw.cmd test` | Sucesso: 30 testes unitários e arquiteturais |
| `.\mvnw.cmd clean verify` | Sucesso: 30 testes unitários/arquiteturais e 13 testes de integração |
| Flyway/Testcontainers | V001, V002 e V003 aplicadas em PostgreSQL 18 real |
| Spring Modulith e ArchUnit | Limites do módulo `parties` e direção de dependências aprovados |
| Checkstyle | 0 violações |

## Pendências ou riscos comprovados

- CPF/CNPJ permanecem persistidos como dados restritos para busca e unicidade. Criptografia de coluna ou tokenização não foi exigida pela especificação e deve ser decidida antes de elevar o modelo de ameaça ou conceder acesso operacional direto ao banco.
- O contexto por headers continua sendo mecanismo temporário de desenvolvimento, conforme Prompt 001.
- Dependências de teste mantêm o aviso não bloqueante de autoanexação Mockito/Byte Buddy no Java 25.
- A revisão humana continua necessária após o commit; nenhum segundo agente foi acionado porque execução paralela não foi solicitada.

## Confirmação de escopo

Não foram criados processos, vínculos participante–processo, eventos históricos de processo, documentos pessoais adicionais ou dados bancários. O Prompt 003 não foi antecipado.
