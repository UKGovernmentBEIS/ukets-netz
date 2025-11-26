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

    static final String USER_PACKAGE = BASE_PACKAGE + ".user..";
    static final String COMMON_PACKAGE = BASE_PACKAGE + ".common..";
    static final String USER_INFO_API_PACKAGE = BASE_PACKAGE + ".userinfoapi..";
    static final String TOKEN_PACKAGE = BASE_PACKAGE + ".token..";
    static final String AUTHORIZATION_PACKAGE = BASE_PACKAGE + ".authorization..";
    static final String CA_PACKAGE = BASE_PACKAGE + ".competentauthority..";
    static final String ACCOUNT_PACKAGE = BASE_PACKAGE + ".account..";
    static final String NOTIFICATIONAPI_PACKAGE = BASE_PACKAGE + ".notificationapi..";
    static final String VERIFICATION_BODY_PACKAGE = BASE_PACKAGE + ".verificationbody..";
    static final String FILES_PACKAGE = BASE_PACKAGE + ".files..";

    static final List<String> ALL_PACKAGES = List.of(
            USER_PACKAGE,
            TOKEN_PACKAGE,
            COMMON_PACKAGE,
            USER_INFO_API_PACKAGE,
            AUTHORIZATION_PACKAGE,
            CA_PACKAGE,
            ACCOUNT_PACKAGE,
            NOTIFICATIONAPI_PACKAGE,
            VERIFICATION_BODY_PACKAGE,
            FILES_PACKAGE
    );

    @ArchTest
    public static final ArchRule userPackageChecks =
            noClasses().that()
                    .resideInAPackage(USER_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(except(
                            USER_PACKAGE,
                            USER_INFO_API_PACKAGE,
                            COMMON_PACKAGE,
                            TOKEN_PACKAGE,
                            AUTHORIZATION_PACKAGE,
                            NOTIFICATIONAPI_PACKAGE,
                            ACCOUNT_PACKAGE /* to get account name for notification and to validate account status for invitation */,
                            CA_PACKAGE, /* for regulator invitation */
                            VERIFICATION_BODY_PACKAGE /* for verifier invitation */,
                            FILES_PACKAGE /* for signatures */));

    private static String[] except(String... packages) {
        return ALL_PACKAGES.stream()
                .filter(p -> !Arrays.asList(packages).contains(p))
                .toList()
                .toArray(String[]::new);
    }
}