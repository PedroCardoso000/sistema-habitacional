package com.esteirahabitacional.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LayerDependencyTest {

    private static JavaClasses productionClasses;

    @BeforeAll
    static void importProductionClasses() {
        productionClasses = new ClassFileImporter().importPackages("com.esteirahabitacional");
    }

    @Test
    void domainShouldNotDependOnFrameworksOrOuterLayers() {
        noClasses()
                .that()
                .resideInAnyPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.servlet..",
                        "com.fasterxml.jackson..",
                        "tools.jackson..",
                        "..application..",
                        "..adapter..",
                        "..config..")
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    @Test
    void applicationShouldNotDependOnAdaptersOrFrameworks() {
        noClasses()
                .that()
                .resideInAnyPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..adapter..",
                        "..config..",
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.servlet..")
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    @Test
    void webAdaptersShouldNotAccessPersistenceAdapters() {
        noClasses()
                .that()
                .resideInAnyPackage("..adapter.in.web..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..adapter.out.persistence..")
                .allowEmptyShould(true)
                .check(productionClasses);
    }
}

