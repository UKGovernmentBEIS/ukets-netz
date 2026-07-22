package uk.gov.netz.docgenerator.client;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "uk.gov.netz.docgenerator.client")
class ArchitectureTest {

    @ArchTest
    static final ArchRule coreDoesNotDependOnAwsSdkOrSpringCloudAws = noClasses()
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("software.amazon..", "io.awspring.cloud..");
}
