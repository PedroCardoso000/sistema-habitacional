# SPEC 001 — Identidade, Empresa e Autorização

## Objetivo

Garantir que toda operação ocorra dentro de uma empresa correspondente e com autorização contextual.

## Atores

- gestor;
- analista;
- corretor;
- cliente;
- vendedor;
- administrador da plataforma.

## Domínio

Modelar, no mínimo:

- `Organization`;
- `User`;
- `Role`;
- `Permission` quando necessário;
- vínculo do usuário com a empresa;
- status de acesso ativo, suspenso e revogado.

A autenticação técnica pode ser simulada ou adaptada no MVP, mas a autorização de negócio pertence ao sistema.

## Casos de uso

- provisionar a primeira empresa e seu administrador por comando privilegiado executado uma vez;
- criar empresa por administração de plataforma autorizada, quando habilitado;
- convidar/cadastrar usuário interno;
- atribuir papel;
- suspender ou revogar acesso;
- recuperar contexto do usuário atual;
- verificar autorização por empresa e papel.

## Regras de negócio

- usuário sem vínculo não acessa dados da empresa;
- usuário revogado não executa novas ações;
- revogação não apaga autoria histórica;
- administrador técnico não lê documentos por padrão;
- toda consulta persistente inclui `organizationId` explícito.
- bootstrap não usa organização falsa nem bypass de tenant;
- provisionamento inicial só executa sem organizações existentes, com ambiente e segredo de bootstrap habilitados;
- o comando de bootstrap fica inutilizável após o primeiro provisionamento e não é endpoint HTTP público.

## Interfaces externas

- endpoints mínimos para empresa e usuários internos;
- mecanismo temporário de contexto autenticado adequado ao desenvolvimento;
- contrato preparado para substituição futura por provedor real.
- comando controlado de bootstrap na fronteira `platformadministration`.

## Testes obrigatórios

- isolamento entre empresas;
- acesso revogado;
- papel insuficiente;
- autoria preservada;
- tentativa de manipular `organizationId` externo.
- repetição ou execução não autorizada do bootstrap.

## Critérios de aceite

- nenhum repositório consulta dado de negócio sem escopo de empresa;
- controllers não decidem autorização de negócio;
- testes de integração provam segregação multiempresa.
