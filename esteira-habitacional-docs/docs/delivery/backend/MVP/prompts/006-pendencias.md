# PROMPT 006 — Pendências

Implemente a especificação:

`docs/delivery/backend/MVP/specs/006-pendencias.md`

## Skill obrigatória

Aplique integralmente:

`docs/skills/backend/backend-feature-delivery/SKILL.md`

## Leitura permitida

Leia somente:

- a especificação acima;
- os documentos obrigatórios indicados pela skill;
- código e testes diretamente relacionados à entrega;
- estado atual de build, diff e migrações.

Não leia o repositório inteiro sem necessidade.

## Regras de execução

1. Comece pelo domínio e pelas invariantes.
2. Crie testes de domínio antes ou junto do comportamento.
3. Implemente casos de uso e portas pequenas.
4. Adicione adapters de entrada e saída somente após o núcleo estar definido.
5. Mantenha Spring, JPA, HTTP e armazenamento fora do domínio.
6. Preserve segregação por empresa em toda consulta e alteração.
7. Não implemente itens fora do escopo da especificação.
8. Não antecipe features futuras.
9. Não introduza microsserviços, mensageria externa ou abstrações genéricas sem necessidade comprovada.
10. Use nomes de negócio claros e código legível.

## Validação

Execute os comandos reais disponíveis no projeto para:

- compilar;
- executar testes unitários;
- executar testes de integração;
- validar migrações;
- validar formatação/análise estática;
- verificar o diff.

Não invente comandos. Se o projeto ainda não possuir algum comando, documente a lacuna e crie somente o mínimo necessário quando fizer parte desta especificação.

## Entrega

Ao final, informe objetivamente:

- comportamento implementado;
- decisões tomadas;
- arquivos principais alterados;
- migrations criadas;
- testes adicionados e executados;
- comandos executados e resultados;
- pendências reais;
- confirmação de que não houve expansão de escopo.
