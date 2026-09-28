package com.boilerplate.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
    packages = "com.boilerplate",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ModuleBoundaryTest {

    private static final String MODULE_PACKAGE_PREFIX = "com.boilerplate.module.";
    private static final Set<String> PUBLIC_CONTRACT_PACKAGES = Set.of(
        "api",
        "dto",
        "domain.dto"
    );

    @ArchTest
    static final ArchRule modulesMustDependOnlyOnOtherModulesPublicContracts =
        classes()
            .that().resideInAPackage("com.boilerplate.module..")
            .should(new ArchCondition<>("depend only on public contract packages of other modules") {
                @Override
                public void check(JavaClass sourceClass, ConditionEvents events) {
                    String sourceModule = moduleName(sourceClass.getPackageName());
                    if (sourceModule == null) {
                        return;
                    }

                    for (Dependency dependency : sourceClass.getDirectDependenciesFromSelf()) {
                        JavaClass targetClass = dependency.getTargetClass();
                        String targetModule = moduleName(targetClass.getPackageName());

                        if (targetModule != null
                            && !sourceModule.equals(targetModule)
                            && isInternalPackage(targetClass.getPackageName(), targetModule)) {
                            events.add(SimpleConditionEvent.violated(
                                dependency,
                                dependency.getDescription()
                                    + " crosses module boundary into internal package "
                                    + targetClass.getPackageName()
                            ));
                        }
                    }
                }
            });

    @ArchTest
    static final ArchRule modulesMustBeFreeOfDependencyCycles =
        slices()
            .matching("com.boilerplate.module.(*)..")
            .should()
            .beFreeOfCycles();

    private static String moduleName(String packageName) {
        if (!packageName.startsWith(MODULE_PACKAGE_PREFIX)) {
            return null;
        }

        String moduleAndPackage = packageName.substring(MODULE_PACKAGE_PREFIX.length());
        int separator = moduleAndPackage.indexOf('.');
        return separator < 0 ? moduleAndPackage : moduleAndPackage.substring(0, separator);
    }

    private static boolean isInternalPackage(String packageName, String moduleName) {
        String moduleRoot = MODULE_PACKAGE_PREFIX + moduleName;
        if (packageName.equals(moduleRoot)) {
            return true;
        }

        String modulePackage = moduleRoot + ".";
        if (!packageName.startsWith(modulePackage)) {
            return false;
        }

        String moduleRelativePackage = packageName.substring(modulePackage.length());

        return PUBLIC_CONTRACT_PACKAGES.stream().noneMatch(contractPackage ->
            moduleRelativePackage.equals(contractPackage)
                || moduleRelativePackage.startsWith(contractPackage + ".")
        );
    }
}
