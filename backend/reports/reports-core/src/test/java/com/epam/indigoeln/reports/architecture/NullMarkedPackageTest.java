package com.epam.indigoeln.reports.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class NullMarkedPackageTest {

    @Test
    void allPackagesShouldBeNullMarked() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.epam.indigoeln");

        SortedSet<String> violating = classes.stream()
                .map(c -> c.getPackage())
                .filter(pkg -> !pkg.isAnnotatedWith(NullMarked.class))
                .map(JavaPackage::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(violating)
                .as("Packages missing @NullMarked (add package-info.java with @NullMarked)")
                .isEmpty();
    }
}
