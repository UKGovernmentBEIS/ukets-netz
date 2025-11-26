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

    static final String COMMON_PACKAGE = BASE_PACKAGE + ".common..";
    static final String FILES_PACKAGE = BASE_PACKAGE + ".files..";
    static final String TOKEN_PACKAGE = BASE_PACKAGE + ".token..";
    static final String AUTHORIZATION_PACKAGE = BASE_PACKAGE + ".authorization..";
    static final String CA_PACKAGE = BASE_PACKAGE + ".competentauthority..";
    static final String VERIFICATION_BODY_PACKAGE = BASE_PACKAGE + ".verificationbody..";
    static final String NOTIFICATIONAPI_PACKAGE = BASE_PACKAGE + ".notificationapi..";
    static final String NOTIFICATION_PACKAGE = BASE_PACKAGE + ".notification..";

    static final String ACCOUNT_PACKAGE = BASE_PACKAGE + ".account..";

    static final List<String> ALL_PACKAGES = List.of(
            COMMON_PACKAGE,
            AUTHORIZATION_PACKAGE,
            CA_PACKAGE,
            VERIFICATION_BODY_PACKAGE,
            FILES_PACKAGE, /* for notes */
            TOKEN_PACKAGE,
            ACCOUNT_PACKAGE,
            NOTIFICATIONAPI_PACKAGE,
            NOTIFICATION_PACKAGE
    );

    @ArchTest
    public static final ArchRule accountPackageChecks =
            noClasses().that()
                    .resideInAPackage(ACCOUNT_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(except(
                            ACCOUNT_PACKAGE,
                            COMMON_PACKAGE,
                            AUTHORIZATION_PACKAGE,
                            NOTIFICATIONAPI_PACKAGE,
                            NOTIFICATION_PACKAGE,
                            CA_PACKAGE,
                            FILES_PACKAGE, /* for notes */
                            TOKEN_PACKAGE,
                            VERIFICATION_BODY_PACKAGE));

    private static String[] except(String... packages) {
        return ALL_PACKAGES.stream()
                .filter(p -> !Arrays.asList(packages).contains(p))
                .toList()
                .toArray(String[]::new);
    }
}