# SPEC 010 — Estabilização do Backend MVP

## Objetivo

Validar o backend completo antes de iniciar o frontend.

## Escopo

- revisão arquitetural de todos os módulos;
- consistência dos contratos HTTP;
- OpenAPI atualizado;
- migrações limpas e reproduzíveis;
- testes de domínio, aplicação, integração e API;
- testes multiempresa e de autorização;
- testes de concorrência nas alterações críticas;
- testes de upload e falhas recuperáveis;
- índices das consultas operacionais;
- logs e correlação de requisições;
- health/readiness sem executar conteúdo de negócio;
- documentação para execução local;
- massa de dados de demonstração sem dados reais;
- relatório de lacunas e itens deliberadamente adiados.

## Validações obrigatórias

- build limpo;
- todos os testes passam;
- análise estática sem falha bloqueadora;
- migrations aplicadas em banco vazio;
- API iniciada com dependências locais;
- principais fluxos executados de ponta a ponta;
- acesso cruzado entre empresas bloqueado;
- documentos nunca públicos;
- erros conhecidos retornam contrato padronizado;
- nenhum endpoint contém regra de negócio.

## Cenários ponta a ponta

1. criar empresa e analista;
2. cadastrar corretor e cliente;
3. criar processo por corretor;
4. iniciar fluxo e próxima ação;
5. solicitar e enviar documento;
6. recusar e reenviar nova versão;
7. criar e resolver pendência;
8. avançar etapas;
9. consultar visão do corretor e do cliente;
10. concluir processo;
11. confirmar linha do tempo completa.

## Critérios de aceite

- backend MVP pode ser consumido pelo frontend;
- contrato OpenAPI representa a implementação;
- riscos e exclusões estão documentados;
- pasta `features-completas` continua sem implementação antecipada.
