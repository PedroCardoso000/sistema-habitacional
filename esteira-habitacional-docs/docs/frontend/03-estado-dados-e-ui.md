# Estado, Dados e UI

## 1. Tipos de estado

### Estado do servidor

Exemplos:

- processos;
- documentos;
- pendências;
- participantes;
- histórico.

Deve ser tratado com biblioteca de server state ou camada equivalente, com cache e invalidação explícita.

### Estado local

Exemplos:

- modal aberto;
- aba selecionada;
- campo temporário;
- filtro ainda não aplicado.

### Estado global

Somente para itens realmente transversais:

- sessão;
- tema;
- permissões resolvidas;
- preferências globais.

## 2. Contratos

- tipos de API centralizados;
- validação de respostas quando necessário;
- erros normalizados;
- datas convertidas em um ponto conhecido;
- sem espalhar strings de endpoint.

## 3. UI resiliente

Toda tela deve prever:

- loading;
- erro recuperável;
- vazio;
- sem permissão;
- dados parciais;
- ação em andamento;
- confirmação de sucesso.

## 4. Design system

A paleta inicial segue:

- fundo azul-marinho;
- superfícies em azul escuro;
- texto branco e cinza claro;
- azul vivo para ação principal;
- verde para sucesso;
- amarelo/laranja para atenção;
- vermelho para erro ou bloqueio.

Cor nunca deve ser o único indicador de estado.
