package uk.gov.netz.api.restclient;


import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.Arrays;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = ArchUnitTest.BASE_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchUnitTest {

    static final String BASE_PACKAGE = "uk.gov.netz.api";

    static final String REST_CLIENT_PACKAGE = BASE_PACKAGE + ".restclient..";
    static final String REST_LOGGING_PACKAGE = BASE_PACKAGE + ".logging..";

    static final List<String> ALL_PACKAGES = List.of(
    		REST_CLIENT_PACKAGE,
    		REST_LOGGING_PACKAGE
    );

    @ArchTest
    public static final ArchRule packageChecks =
            noClasses().that()
                    .resideInAPackage(REST_CLIENT_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(except(
                    		REST_CLIENT_PACKAGE,
                    		REST_LOGGING_PACKAGE));

    private static String[] except(String... packages) {
        return ALL_PACKAGES.stream()
                .filter(p -> !Arrays.asList(packages).contains(p))
                .toList()
                .toArray(String[]::new);
    }
}