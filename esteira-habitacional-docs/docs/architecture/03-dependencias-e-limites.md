# Dependências e Limites Arquiteturais

## 1. Regras obrigatórias

1. O domínio não depende de frameworks.
2. Casos de uso dependem de interfaces, nunca de implementações concretas.
3. Controllers não acessam repositórios diretamente.
4. Entidades JPA não devem contaminar o domínio.
5. DTOs HTTP não devem ser reutilizados como objetos de domínio.
6. Regras de autorização de negócio não ficam apenas no frontend.
7. Nenhum módulo pode consultar tabelas de outro módulo diretamente sem contrato explícito.
8. Operações críticas geram histórico e auditoria.

## 2. Comunicação interna

Preferência:

- chamadas diretas por interfaces de aplicação;
- eventos internos para desacoplamento secundário;
- transações locais;
- consistência forte dentro do agregado.

Evitar no MVP:

- broker de mensagens sem necessidade;
- sagas distribuídas;
- chamadas HTTP entre módulos do mesmo backend;
- banco separado por módulo;
- duplicação de modelos sem motivo.

## 3. Interfaces de entrada e saída

Exemplo conceitual:

```java
public interface CreateFinancingProcessUseCase {
    FinancingProcessResult execute(CreateFinancingProcessCommand command);
}

public interface FinancingProcessRepository {
    Optional<FinancingProcess> findById(FinancingProcessId id);
    void save(FinancingProcess process);
}
```

A aplicação conhece a interface. A infraestrutura conhece a implementação.

## 4. Transações

- Transação começa na aplicação.
- Um caso de uso deve formar uma unidade clara de trabalho.
- Efeitos externos devem ser idempotentes quando possível.
- Upload de arquivo e gravação de metadados exigem estratégia explícita para falhas parciais.

## 5. Critério para extrair microsserviço

A extração só será considerada quando ao menos um destes fatores for real:

- necessidade de escala independente;
- fronteira de segurança distinta;
- ciclo de implantação autônomo;
- equipe dedicada;
- indisponibilidade aceitável diferente;
- carga técnica muito distinta;
- dependência externa de alta latência que justifique isolamento.

Sem isso, a extração é complexidade sem retorno.
