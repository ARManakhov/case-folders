package dev.sirosh.case_folders.classpath_utils;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.platform.commons.util.Preconditions;

public class DefaultPathProvider implements PathProvider {

  private static final DefaultPathProvider INSTANCE = new DefaultPathProvider();

  @Override
  public Path getClasspathResource(Class<?> baseClass, String path) {
    Preconditions.notBlank(path, () -> "Classpath resource [" + path + "] must not be null or blank");
    URL resource = baseClass.getResource(path);
    Preconditions.notNull(resource, "Classpath resource [" + path + "] doesn't exists");
    try {
      return Path.of(resource.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static DefaultPathProvider getInstance() {
    return INSTANCE;
  }
}
