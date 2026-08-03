# SPEC 005 — Documentos e Submissão do Processo

## Objetivo

Centralizar solicitações e versões de documentos sem expor arquivos publicamente e fechar a submissão atômica do rascunho quando workflow e checklist estiverem disponíveis.

## Domínio

Modelar:

- `DocumentType`;
- `ChecklistTemplate` mínimo;
- `DocumentRequest` como agregado ou limite consistente;
- `DocumentVersion`/`Document`;
- status: solicitado, enviado, em análise, aprovado, recusado, reenvio solicitado, cancelado, expirado;
- motivo padronizado ou textual de recusa;
- remetente e destinatário;
- validade quando aplicável;
- `UploadIntent` com estados pendente, enviado ao storage, concluído, expirado, recusado e abortado.

## Regras

- recusa exige motivo;
- reenvio cria nova versão;
- versão anterior nunca é sobrescrita;
- somente participantes autorizados enviam ou visualizam;
- metadado de arquivo fica no banco; binário fica em armazenamento privado;
- nenhum arquivo possui URL pública permanente;
- extensão, tamanho e tipo básico são validados;
- falha de upload não pode deixar estado inconsistente;
- upload usa intenção, envio direto ao storage privado e confirmação por metadados consultados no storage;
- objetos e intenções órfãos são limpos por job idempotente e observável;
- chaves de objetos não contêm CPF, nome ou outro dado sensível;
- submissão de rascunho valida dados, inicializa workflow, gera checklist, define próxima ação e muda para `EM_TRIAGEM` em uma única transação;
- falha em qualquer parte da submissão reverte toda a operação.

## Casos de uso

- gerar checklist inicial;
- submeter processo rascunho;
- solicitar documento;
- criar intenção de upload autorizado e URL temporária;
- registrar upload concluído;
- enviar nova versão;
- marcar em análise;
- aprovar documento;
- recusar e solicitar reenvio;
- listar documentos e pendências documentais;
- baixar arquivo por autorização temporária;
- expirar intenções e remover objetos órfãos com repetição segura.

## Interfaces externas

- endpoints de checklist e solicitação;
- `POST /processes/{id}/submission` para submeter o rascunho atomicamente;
- endpoint/fluxo de upload privado;
- `POST /document-requests/{id}/uploads` para criar intenção;
- `POST /uploads/{uploadId}/complete` para confirmar por metadados do storage;
- análise documental por analista;
- download protegido.

## Testes obrigatórios

- aprovação e recusa;
- recusa sem motivo;
- reenvio e preservação de versão;
- acesso de corretor, cliente e terceiro não vinculado;
- falha de armazenamento;
- arquivo inválido;
- segregação por empresa;
- rollback integral quando a submissão falha ao inicializar workflow ou checklist;
- confirmação duplicada de upload e limpeza idempotente de órfãos.

## Critérios de aceite

- documento pode percorrer o ciclo completo;
- rascunho válido pode ser submetido atomicamente e entra em `EM_TRIAGEM` completamente inicializado;
- histórico de versões é consultável;
- nenhum arquivo é servido sem autorização.
