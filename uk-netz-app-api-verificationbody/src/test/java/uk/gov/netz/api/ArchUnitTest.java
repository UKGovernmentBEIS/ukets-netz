package uk.gov.netz.api;


import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.Arrays;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static uk.gov.netz.api.ArchUnitTest.BASE_PACKAGE;

@AnalyzeClasses(packages = BASE_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchUnitTest {

    static final String BASE_PACKAGE = "uk.gov.netz.api";

    static final String COMMON_PACKAGE = BASE_PACKAGE + ".common..";
    static final String AUTHORIZATION_PACKAGE = BASE_PACKAGE + ".authorization..";
    static final String VERIFICATION_BODY_PACKAGE = BASE_PACKAGE + ".verificationbody..";
    static final String REFERENCE_DATA_PACKAGE = BASE_PACKAGE + ".referencedata..";

    static final List<String> ALL_PACKAGES = List.of(
            COMMON_PACKAGE,
            AUTHORIZATION_PACKAGE,
            VERIFICATION_BODY_PACKAGE,
            REFERENCE_DATA_PACKAGE
    );

    @ArchTest
    public static final ArchRule verificationBodyPackageChecks =
            noClasses().that()
                    .resideInAPackage(VERIFICATION_BODY_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(except(
                            VERIFICATION_BODY_PACKAGE,
                            COMMON_PACKAGE,
                            REFERENCE_DATA_PACKAGE,
                            AUTHORIZATION_PACKAGE));

    private static String[] except(String... packages) {
        return ALL_PACKAGES.stream()
                .filter(p -> !Arrays.asList(packages).contains(p))
                .toList()
                .toArray(String[]::new);
    }
}