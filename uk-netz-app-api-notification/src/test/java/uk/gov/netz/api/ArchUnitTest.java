package uk.gov.netz.api;


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

    static final String NOTIFICATION_PACKAGE = BASE_PACKAGE + ".notification..";
    static final String NOTIFICATION_API_PACKAGE = BASE_PACKAGE + ".notificationapi..";
    static final String COMMON_PACKAGE = BASE_PACKAGE + ".common..";
    static final String CA_PACKAGE = BASE_PACKAGE + ".competentauthority..";
    static final String AUTHORIZATION_PACKAGE = BASE_PACKAGE + ".authorization..";

    static final List<String> ALL_PACKAGES = List.of(
            NOTIFICATION_PACKAGE,
            NOTIFICATION_API_PACKAGE,
            COMMON_PACKAGE,
            CA_PACKAGE,
            AUTHORIZATION_PACKAGE
    );

    @ArchTest
    public static final ArchRule notificationPackageChecks =
            noClasses().that()
                    .resideInAPackage(NOTIFICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(except(
                            NOTIFICATION_PACKAGE,
                            NOTIFICATION_API_PACKAGE,
                            COMMON_PACKAGE,
                            AUTHORIZATION_PACKAGE,
                            CA_PACKAGE));

    private static String[] except(String... packages) {
        return ALL_PACKAGES.stream()
                .filter(p -> !Arrays.asList(packages).contains(p))
                .toList()
                .toArray(String[]::new);
    }
}