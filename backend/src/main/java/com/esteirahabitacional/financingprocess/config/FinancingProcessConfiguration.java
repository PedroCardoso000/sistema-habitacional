package com.esteirahabitacional.financingprocess.config;

import com.esteirahabitacional.financingprocess.adapter.out.persistence.JdbcFinancingProcessRepository;
import com.esteirahabitacional.financingprocess.adapter.out.persistence.JdbcProcessAudit;
import com.esteirahabitacional.financingprocess.adapter.out.persistence.JdbcProcessNumberGenerator;
import com.esteirahabitacional.financingprocess.application.port.in.ManageFinancingProcessUseCase;
import com.esteirahabitacional.financingprocess.application.port.in.QueryFinancingProcessUseCase;
import com.esteirahabitacional.financingprocess.application.port.out.FinancingProcessRepository;
import com.esteirahabitacional.financingprocess.application.port.out.ProcessAudit;
import com.esteirahabitacional.financingprocess.application.port.out.ProcessNumberGenerator;
import com.esteirahabitacional.financingprocess.application.service.FinancingProcessService;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.parties.BrokerReferenceLookup;
import com.esteirahabitacional.parties.ClientReferenceLookup;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.DomainEventPublisher;
import com.esteirahabitacional.shared.IdentifierGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
class FinancingProcessConfiguration {
    @Bean FinancingProcessRepository financingProcessRepository(JdbcClient jdbc) {
        return new JdbcFinancingProcessRepository(jdbc);
    }
    @Bean ProcessNumberGenerator processNumberGenerator(JdbcClient jdbc) {
        return new JdbcProcessNumberGenerator(jdbc);
    }
    @Bean ProcessAudit processAudit(JdbcClient jdbc) { return new JdbcProcessAudit(jdbc); }

    @Bean
    FinancingProcessService financingProcessService(AuthorizeOrganizationUseCase authorization,
            FinancingProcessRepository processes, ProcessNumberGenerator numbers,
            ClientReferenceLookup clients, BrokerReferenceLookup brokers, ProcessAudit audit,
            DomainEventPublisher events, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        return new FinancingProcessService(authorization, processes, numbers, clients, brokers,
                audit, events, identifiers, time);
    }

    @Bean
    @Primary
    ManageFinancingProcessUseCase manageFinancingProcessUseCase(
            FinancingProcessService service, TransactionTemplate transactions) {
        return new TransactionalManagement(service, transactions);
    }

    @Bean
    @Primary
    QueryFinancingProcessUseCase queryFinancingProcessUseCase(FinancingProcessService service) {
        return new QueryFinancingProcessUseCase() {
            @Override public Detail find(java.util.UUID organizationId, java.util.UUID processId) {
                return service.find(organizationId, processId);
            }
            @Override public Page list(ListQuery query) { return service.list(query); }
        };
    }

    private record TransactionalManagement(
            FinancingProcessService service, TransactionTemplate transactions)
            implements ManageFinancingProcessUseCase {
        @Override public Result create(CreateCommand command) {
            return transactions.execute(status -> service.create(command));
        }
        @Override public Result defineMainClient(MainClientCommand command) {
            return transactions.execute(status -> service.defineMainClient(command));
        }
        @Override public Result associateParticipant(ParticipantCommand command) {
            return transactions.execute(status -> service.associateParticipant(command));
        }
        @Override public Result associateProperty(PropertyCommand command) {
            return transactions.execute(status -> service.associateProperty(command));
        }
        @Override public Result changePriority(PriorityCommand command) {
            return transactions.execute(status -> service.changePriority(command));
        }
    }
}
