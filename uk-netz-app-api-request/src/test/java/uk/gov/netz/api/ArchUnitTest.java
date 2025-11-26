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

    static final String WORKFLOW_PACKAGE = BASE_PACKAGE + ".workflow..";
    static final String COMMON_PACKAGE = BASE_PACKAGE + ".common..";
    static final String TOKEN_PACKAGE = BASE_PACKAGE + ".token..";
    static final String AUTHORIZATION_PACKAGE = BASE_PACKAGE + ".authorization..";
    static final String CA_PACKAGE = BASE_PACKAGE + ".competentauthority..";
    static final String NOTIFICATIONAPI_PACKAGE = BASE_PACKAGE + ".notificationapi..";
    static final String ACCOUNT_PACKAGE = BASE_PACKAGE + ".account..";
    static final String USER_PACKAGE = BASE_PACKAGE + ".user..";
    static final String USER_INFO_API_PACKAGE = BASE_PACKAGE + ".userinfoapi..";
    static final String DOCUMENT_TEMPLATE_PACKAGE = BASE_PACKAGE + ".documenttemplate..";
    static final String VERIFICATION_BODY_PACKAGE = BASE_PACKAGE + ".verificationbody..";
    static final String FILES_PACKAGE = BASE_PACKAGE + ".files..";

    static final List<String> ALL_PACKAGES = List.of(
            COMMON_PACKAGE,
            DOCUMENT_TEMPLATE_PACKAGE,
            NOTIFICATIONAPI_PACKAGE,
            TOKEN_PACKAGE,
            AUTHORIZATION_PACKAGE,
            CA_PACKAGE,
            VERIFICATION_BODY_PACKAGE,
            USER_PACKAGE,
            USER_INFO_API_PACKAGE,
            ACCOUNT_PACKAGE,
            FILES_PACKAGE,
            WORKFLOW_PACKAGE
    );

    @ArchTest
    public static final ArchRule workflowPackageChecks =
            noClasses().that()
                    .resideInAPackage(WORKFLOW_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(except(
                            WORKFLOW_PACKAGE,
                            COMMON_PACKAGE,
                            TOKEN_PACKAGE,
                            AUTHORIZATION_PACKAGE,
                            CA_PACKAGE,
                            NOTIFICATIONAPI_PACKAGE,
                            ACCOUNT_PACKAGE,
                            FILES_PACKAGE,
                            USER_PACKAGE,
                            USER_INFO_API_PACKAGE,
                            DOCUMENT_TEMPLATE_PACKAGE,
                            VERIFICATION_BODY_PACKAGE));

    private static String[] except(String... packages) {
        return ALL_PACKAGES.stream()
                .filter(p -> !Arrays.asList(packages).contains(p))
                .toList()
                .toArray(String[]::new);
    }
}