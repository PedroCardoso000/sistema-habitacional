# ADR-012 — Provisionamento inicial privilegiado

## Status

Aceita

## Contexto

A primeira organização não pode depender de um contexto organizacional inexistente, mas um bypass permanente abriria uma falha estrutural de segurança.

## Decisão

Criar a fronteira `platformadministration` e o caso de uso `ProvisionFirstOrganization`, separado das operações normais.

Ele só executa quando não existe organização, o ambiente permite bootstrap, um segredo está configurado e a operação ainda não ocorreu. Em uma transação, cria organização, usuário inicial, papel de administrador, vínculo e registro/evento de provisionamento, marcando o bootstrap como concluído.

No MVP será exposto como comando controlado de inicialização, não endpoint HTTP público. Depois de executado, torna-se inutilizável e o segredo deve ser removido. `PlatformContext` e `OrganizationContext` são tipos distintos; não existe organização falsa.

## Alternativas consideradas

- endpoint público: rejeitado por ampliar superfície de ataque.
- bypass se a tabela estiver vazia: rejeitado por criar exceção permanente e difícil de auditar.
- seed com credenciais fixas: rejeitado por segurança.

## Consequências positivas

O primeiro provisionamento não cria bypass permanente nem organização fictícia.

## Consequências negativas

Deploy precisa executar e auditar uma etapa única adicional.

## Riscos

Segredo persistente ou comando reexecutável amplia a superfície de ataque. Administração da plataforma não pode herdar acesso documental implicitamente.

## Evidências e critérios de revisão

Revisar quando um plano de controle administrativo autenticado for necessário.

## Data

2026-08-03
