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

    static final String FILES_PACKAGE = BASE_PACKAGE + ".files..";
    static final String COMMON_PACKAGE = BASE_PACKAGE + ".common..";
    static final String AUTHORIZATION_PACKAGE = BASE_PACKAGE + ".authorization..";
    static final String TOKEN_PACKAGE = BASE_PACKAGE + ".token..";

    static final List<String> ALL_PACKAGES = List.of(
            FILES_PACKAGE,
            COMMON_PACKAGE,
            AUTHORIZATION_PACKAGE,
            TOKEN_PACKAGE
    );

    @ArchTest
    public static final ArchRule filesPackageChecks =
            noClasses().that()
                    .resideInAPackage(FILES_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(except(
                            FILES_PACKAGE,
                            COMMON_PACKAGE,
                            AUTHORIZATION_PACKAGE,
                            TOKEN_PACKAGE));

    private static String[] except(String... packages) {
        return ALL_PACKAGES.stream()
                .filter(p -> !Arrays.asList(packages).contains(p))
                .toList()
                .toArray(String[]::new);
    }
}