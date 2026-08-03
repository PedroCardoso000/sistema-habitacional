# Segurança e Privacidade

## 1. Contexto

A plataforma manipula dados pessoais, financeiros e documentos sensíveis ao processo de financiamento.

## 2. Regras mínimas

- tráfego criptografado;
- segregação por empresa;
- autorização por papel e vínculo com processo;
- arquivos privados;
- URLs temporárias;
- logs sem conteúdo sensível;
- histórico de acesso e ações críticas;
- política de retenção;
- revogação imediata de acesso;
- validação de extensão, tamanho e conteúdo de arquivo;
- proteção contra upload malicioso;
- segredos fora do código.

## 3. Multi-tenancy

Toda consulta deve considerar a empresa do usuário.

Nunca confiar apenas em filtro recebido do cliente.

## 4. Princípio do menor privilégio

- gestor não precisa ler todo documento por padrão;
- corretor acessa somente processos vinculados;
- cliente acessa apenas seu processo;
- vendedor acessa apenas solicitações sob sua responsabilidade;
- administrador técnico não lê documentos por padrão.

## 5. Auditoria

Registrar:

- usuário;
- empresa;
- ação;
- objeto;
- data/hora;
- resultado;
- correlation ID;
- origem técnica quando necessário.
