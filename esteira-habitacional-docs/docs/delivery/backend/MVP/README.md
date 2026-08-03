# Backend MVP — Ordem de Execução

## Princípio

Toda entrega segue a skill:

`docs/skills/backend/backend-feature-delivery/SKILL.md`

Fluxo obrigatório:

```text
Domínio → Casos de uso → Portas → Adapters → Infraestrutura → Testes → Documentação
```

## Sequência

| Ordem | Especificação | Resultado principal |
|---:|---|---|
| 000 | Fundação do backend | Projeto executável e baseline arquitetural |
| 001 | Identidade, empresa e autorização | Segregação multiempresa e contexto do usuário |
| 002 | Participantes e parceiros | Clientes, corretores e vínculos mínimos |
| 003 | Processo de financiamento | Criar, editar, listar e consultar rascunhos |
| 004 | Fluxo, etapas e próxima ação | Preparar fluxo versionado e transições operacionais |
| 005 | Documentos e submissão | Checklist, submissão atômica, upload privado, versão e análise |
| 006 | Pendências | Criar, atribuir, resolver e vencer pendências |
| 007 | Histórico e auditoria | Linha do tempo imutável e rastreabilidade |
| 008 | Filas e visões por perfil | Consultas do analista, corretor e cliente |
| 009 | Encerramento e ciclo de vida | Suspender, cancelar, concluir e reabrir |
| 010 | Estabilização do MVP | Segurança, testes integrados, contrato e qualidade |

## Regra de escopo

Nenhum prompt pode introduzir:

- microsserviços;
- mensageria externa;
- integração com sistemas bancários;
- OCR;
- assinatura eletrônica;
- aplicativo móvel nativo;
- automação oficial do WhatsApp;
- decisão automática de crédito;
- relatórios avançados;
- billing ou comissionamento.
