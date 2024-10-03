package dev.sirosh.case_folders;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.Preconditions;

import dev.sirosh.case_folders.classpath_utils.DefaultPathProvider;
import dev.sirosh.case_folders.classpath_utils.PathProvider;
import dev.sirosh.case_folders.classpath_utils.Source;

public class FileArgumentsProvider implements ParameterResolver {
  private final PathProvider pathProvider;

  FileArgumentsProvider() {
    this(DefaultPathProvider.getInstance());
  }

  FileArgumentsProvider(PathProvider pathProvider) {
    this.pathProvider = pathProvider;
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    boolean annotated = parameterContext.isAnnotated(FileArgument.class);
    if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
      throw new ParameterResolutionException(
          "@FileArgument is not supported on constructor parameters. Please use field injection instead.");
      // todo maybe allow constructor
    }
    return annotated;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Parameter parameter = parameterContext.getParameter();
    Class<?> parameterType = parameter.getType();
    FileArgument annotation = parameter.getAnnotation(FileArgument.class);
    Source source = pathProvider.classpathResource(annotation.file());
    Path path = source.get(extensionContext);
    assertIsFile(path);
    if (parameterType == Path.class) {
      return path;
    }
    if (parameterType == File.class) {
      return path.toFile();
    }

    throw new ExtensionConfigurationException("Can only resolve @FileArgument " + parameter.getName() + " of type "
        + Path.class.getName() + " or " + File.class.getName() + " but was: " + parameterType.getName());
  }

  private void assertIsFile(Path path) {
    Preconditions.condition(!Files.isDirectory(path), "path " + path + "should point to file, but points to directory");
  }
}
