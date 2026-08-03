# SPEC 009 — Encerramento e Ciclo de Vida

## Objetivo

Controlar estados finais e interrupções do processo sem perder rastreabilidade.

## Estados gerais

- rascunho;
- em triagem;
- ativo;
- aguardando terceiro;
- bloqueado;
- suspenso;
- cancelado;
- não aprovado;
- concluído;
- arquivado.

`RASCUNHO` é o estado inicial anterior à submissão. `EM_TRIAGEM` começa apenas após inicialização atômica de workflow, checklist e próxima ação.

## Casos de uso

- suspender processo;
- cancelar por desistência ou outro motivo;
- registrar não aprovação com exposição controlada;
- concluir após assinatura/resultado final;
- reabrir por usuário autorizado;
- arquivar segundo política mínima.

## Regras

- suspensão, cancelamento, não aprovação e reabertura exigem motivo;
- conclusão exige pendências obrigatórias resolvidas ou exceção autorizada;
- processo concluído fica somente leitura para perfis comuns;
- reabertura gera histórico e restaura ações compatíveis;
- informação de não aprovação não expõe justificativa além do permitido;
- retenção definitiva permanece questão configurável e não deve ser inventada.

## Testes obrigatórios

- conclusão com pendência aberta;
- cancelamento sem motivo;
- reabertura sem autorização;
- escrita em processo concluído;
- visibilidade de não aprovação;
- histórico de todas as transições.

## Critérios de aceite

- ciclo de vida é consistente em domínio e API;
- estados finais aparecem corretamente nas consultas;
- nenhuma exclusão física acontece como efeito do encerramento.
