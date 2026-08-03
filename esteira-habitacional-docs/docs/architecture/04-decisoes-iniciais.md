# Decisões Arquiteturais Iniciais

As decisões fundacionais aceitas estão registradas no [índice de ADRs](./decisions/README.md). Em caso de conflito, o ADR aceito prevalece.

## Confirmadas

- Backend em Java.
- Domínio desacoplado de framework e infraestrutura.
- Arquitetura hexagonal/clean architecture.
- Casos de uso explícitos.
- Comunicação entre camadas por contratos.
- Código legível e orientado à linguagem do negócio.
- Frontend componentizado e organizado por feature.
- MVP como monólito modular.
- Aplicação web responsiva.
- Documentos privados e controle de acesso por vínculo.
- Spring Boot como runtime backend.
- PostgreSQL como banco relacional.
- Armazenamento compatível com S3 para arquivos.
- OpenAPI para contrato HTTP.
- Maven com Wrapper para o build.
- Java 25 LTS e Spring Boot 4.1.x.
- Flyway para migrations SQL.
- RFC 9457 para erros HTTP.
- Spring Modulith e ArchUnit para limites arquiteturais.
- Eventos de domínio internos desde a fundação.
- Módulo `parties` para clientes, corretores e imobiliárias.
- Processo criado como `RASCUNHO` e submetido para `EM_TRIAGEM`.
- Upload em duas fases com limpeza de órfãos.
- Bootstrap por provisionamento privilegiado executado uma vez.
- React/Next.js no frontend.
- Testcontainers para testes de integração.

## Não decididas

- Provedor de identidade.
- Hospedagem.
- Serviço de e-mail.
- Estratégia final de antivírus/inspeção de upload.
- Política definitiva de retenção.
- Forma de convite temporário para cliente e vendedor.
- Integração futura com WhatsApp.

Cada decisão relevante deve ser registrada por ADR.
