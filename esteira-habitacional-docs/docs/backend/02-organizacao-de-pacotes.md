# Organização de Pacotes

## Recomendação: pacote por módulo, camada dentro do módulo

```text
com.esteirahabitacional
├── financingprocess
│   ├── domain
│   │   ├── model
│   │   ├── event
│   │   ├── policy
│   │   └── exception
│   ├── application
│   │   ├── port.in
│   │   ├── port.out
│   │   ├── command
│   │   ├── query
│   │   └── service
│   ├── adapter
│   │   ├── in.web
│   │   └── out.persistence
│   └── config
├── documents
├── workflow
├── pendingactions
├── parties
├── organizations
├── identityaccess
├── platformadministration
├── notifications
├── reporting
└── shared
```

## Shared kernel

O pacote `shared` deve ser mínimo.

Permitido:

- tipos realmente transversais;
- abstração de relógio;
- identificadores base;
- paginação;
- resultado comum de aplicação;
- utilitários técnicos sem regra de negócio.

Proibido:

- pasta de utilidades genéricas sem dono;
- regras de negócio compartilhadas por conveniência;
- DTOs globais;
- entidades comuns que criem acoplamento entre módulos.

## Convenções

- nomes em inglês no código;
- termos do domínio coerentes e documentados;
- uma classe pública por arquivo;
- métodos pequenos e intencionais;
- pacotes refletem negócio antes de tecnologia.
