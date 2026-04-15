package com.techstore.bank_system;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureDddTest {

    @Test
    void domain_must_not_depend_on_spring_or_web_or_persistence() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.techstore.bank_system");

        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.servlet..",
                        "org.hibernate.."
                )
                .allowEmptyShould(true);

        rule.check(classes);
    }
}
