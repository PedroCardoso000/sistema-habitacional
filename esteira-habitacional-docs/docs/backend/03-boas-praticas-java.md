# Boas Práticas Java

## 1. Código humano e legível

O código deve expressar intenção.

Preferir:

```java
process.rejectDocument(documentId, rejectionReason, analystId);
```

Evitar:

```java
process.setStatus(4);
repository.update(process);
```

## 2. Imutabilidade

- Objetos de valor devem ser imutáveis.
- Coleções internas não devem ser expostas mutavelmente.
- Mudanças de estado devem ocorrer por métodos de negócio.

## 3. Objetos de valor

Usar objetos de valor para conceitos importantes:

- `FinancingProcessId`
- `Cpf`
- `Email`
- `Deadline`
- `DocumentVersion`
- `RejectionReason`
- `ProcessNumber`

## 4. Validação

Separar:

- validação sintática: borda HTTP;
- validação de aplicação: caso de uso;
- invariantes: domínio.

## 5. Exceções

- Exceções de domínio devem representar falhas conhecidas.
- Não usar `RuntimeException` genérica para regra de negócio.
- Não capturar exceção silenciosamente.
- Erros inesperados devem ser registrados com contexto e correlation ID.

## 6. SOLID

### SRP
Cada classe deve ter uma razão principal para mudar.

### OCP
Extensões devem ocorrer por novos componentes/políticas quando adequado, não por grandes condicionais espalhadas.

### LSP
Implementações devem respeitar o contrato da interface.

### ISP
Interfaces pequenas e específicas.

### DIP
Casos de uso dependem de portas, não de infraestrutura.

## 7. Design patterns recomendados

Usar quando resolverem problema real:

- Factory para criação complexa de agregados;
- Strategy para regras variáveis de checklist ou fluxo;
- Specification para critérios compostos;
- State somente se transições crescerem além do controle simples;
- Domain Event para reações secundárias;
- Repository para persistência de agregados;
- Adapter para integração externa;
- Builder apenas em testes ou construção complexa.

Evitar pattern por decoração arquitetural.

## 8. Lombok

Pode reduzir ruído, mas deve ser usado com cautela.

Não usar `@Data` indiscriminadamente em entidades de domínio.

## 9. Datas e horário

- usar `Instant` para eventos;
- usar `LocalDate` para datas sem horário;
- injetar `Clock`;
- nunca chamar horário do sistema diretamente em regra testável.

## 10. Identidade e igualdade

- entidades são comparadas por identidade;
- objetos de valor por conteúdo;
- regras de `equals` e `hashCode` devem ser explícitas.
