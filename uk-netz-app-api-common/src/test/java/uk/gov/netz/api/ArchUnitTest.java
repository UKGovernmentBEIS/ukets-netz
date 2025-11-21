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

    static final List<String> ALL_PACKAGES = List.of(
            COMMON_PACKAGE
    );

    @ArchTest
    public static final ArchRule commonPackageChecks =
            noClasses().that()
                    .resideInAPackage(COMMON_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(except(
                            COMMON_PACKAGE));

    private static String[] except(String... packages) {
        return ALL_PACKAGES.stream()
                .filter(p -> !Arrays.asList(packages).contains(p))
                .toList()
                .toArray(String[]::new);
    }
}