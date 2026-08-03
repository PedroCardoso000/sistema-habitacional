package com.esteirahabitacional.platformadministration.adapter.in.bootstrap;

import com.esteirahabitacional.platformadministration.application.port.in.BootstrapFirstOrganizationUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class BootstrapCommandRunner implements ApplicationRunner {

    private final boolean execute;
    private final String suppliedSecret;
    private final String organizationName;
    private final String administratorEmail;
    private final String administratorDisplayName;
    private final BootstrapFirstOrganizationUseCase bootstrap;

    public BootstrapCommandRunner(
            boolean execute,
            String suppliedSecret,
            String organizationName,
            String administratorEmail,
            String administratorDisplayName,
            BootstrapFirstOrganizationUseCase bootstrap) {
        this.execute = execute;
        this.suppliedSecret = suppliedSecret;
        this.organizationName = organizationName;
        this.administratorEmail = administratorEmail;
        this.administratorDisplayName = administratorDisplayName;
        this.bootstrap = bootstrap;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (execute) {
            bootstrap.execute(new BootstrapFirstOrganizationUseCase.Command(
                    suppliedSecret, organizationName, administratorEmail, administratorDisplayName));
        }
    }
}
