package dev.sirosh.case_folders.classpath_utils;

import java.nio.file.Path;

public interface PathProvider {
  Path getClasspathResource(Class<?> baseClass, String path);

  default Source classpathResource(String path) {
    return context -> getClasspathResource(context.getRequiredTestClass(), path);
  }
}
