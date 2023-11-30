package dev.sirosh.case_folders.classpath_utils;

import java.nio.file.Path;

import org.junit.jupiter.api.extension.ExtensionContext;

@FunctionalInterface
public interface Source {
  Path get(ExtensionContext context);
}
