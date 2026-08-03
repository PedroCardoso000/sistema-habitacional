# Relatório de Entrega — Identidade, Empresa e Autorização

## Resumo

Foram implementados empresa, usuário tenant-scoped, papéis, permissões e estados de acesso ativo, suspenso e revogado. Gestores e administradores autorizados podem cadastrar usuários internos, alterar papel, suspender e revogar acesso dentro da própria empresa. A administração da plataforma pode criar empresas quando a função está habilitada. O primeiro provisionamento ocorre por comando não HTTP, protegido por ambiente, segredo, ausência de organizações e lock transacional.

## Fluxo implementado

```text
Organization/User → casos de uso → portas → HTTP/JDBC/bootstrap → PostgreSQL → testes
```

## Arquivos alterados

### Domínio

- `Organization`, `User`, `Email`, `Role`, `Permission` e `AccessStatus`.
- Comportamentos explícitos para atribuir papel interno, suspender, revogar e verificar permissão.

### Aplicação

- Casos de uso de cadastro interno, atribuição de papel, suspensão, revogação e contexto atual.
- Autorização contextual por empresa, status e permissão.
- Orquestração privilegiada para bootstrap e criação de empresa.

### Adapters

- Controller de identidade e acesso.
- Controller de administração da plataforma.
- Contexto temporário de desenvolvimento pelos headers `X-User-Id` e `X-Organization-Id`.
- Repositórios JDBC com tenant explícito em toda consulta de usuário.
- Runner de bootstrap não exposto por HTTP.

### Infraestrutura

- Migration `V002__create_organizations_and_identity_access.sql`.
- Configurações de bootstrap e habilitação da criação de empresas por variáveis de ambiente.
- Transações compostas na configuração técnica, preservando a camada de aplicação sem Spring.

### Testes

- Domínio: criação, papéis internos, suspensão, revogação irreversível e permissões.
- Aplicação: autorização, papel insuficiente, manipulação de tenant e bootstrap.
- Integração/API: PostgreSQL real, isolamento entre empresas, acesso revogado, autoria, criação autorizada e bootstrap não autorizado/repetido.

### Documentação

- README do backend atualizado com endpoints, headers temporários, variáveis e operação do bootstrap.
- Este relatório de entrega.

## Regras protegidas

- Nenhuma busca de usuário ocorre sem `organizationId` explícito.
- Empresa recebida externamente precisa coincidir com a empresa do ator autenticado.
- Usuário suspenso ou revogado não recebe permissões.
- Acesso revogado não pode ser reativado ou alterado e o registro do usuário não é apagado.
- Papéis cadastráveis como usuário interno ficam limitados a gestor e analista.
- Administrador da plataforma não recebe permissão documental implícita.
- Ações críticas registram empresa, ator, alvo, instante, resultado, correlation ID e origem técnica.
- Bootstrap exige ambiente e segredo, não usa tenant falso, não é HTTP e falha após o primeiro uso.

## Segurança e autorização

O contexto por headers é apenas um mecanismo temporário de autenticação para desenvolvimento. A autorização permanece no caso de uso e consulta o vínculo persistido. Segredos de bootstrap não possuem padrão válido e devem vir do ambiente. Erros conhecidos usam Problem Details sem expor detalhes internos.

## Testes executados

| Comando | Resultado |
|---|---|
| `.\mvnw.cmd test` | Sucesso: 21 testes unitários e arquiteturais |
| `.\mvnw.cmd clean verify` | Sucesso: 21 testes unitários/arquiteturais e 8 testes de integração |
| Flyway/Testcontainers | V001 e V002 aplicadas em PostgreSQL 18 real |
| Spring Modulith e ArchUnit | Módulos, ciclos e direção de dependências aprovados |
| Checkstyle | 0 violações |

## Pendências ou riscos comprovados

- O contexto por headers não é autenticação de produção e precisa ser substituído por provedor real em entrega futura.
- Dependências de teste mantêm o aviso não bloqueante de autoanexação Mockito/Byte Buddy no Java 25.
- A revisão humana permanece necessária após o commit; nenhum segundo agente foi acionado porque a execução paralela não foi solicitada.

## Confirmação de escopo

Não houve implementação de participantes, processos, documentos ou autenticação federada. O Prompt 002 não foi antecipado.
