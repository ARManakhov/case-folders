package dev.sirosh.case_folders;

import dev.sirosh.case_folders.classpath_utils.DefaultPathProvider;
import dev.sirosh.case_folders.classpath_utils.PathProvider;
import dev.sirosh.case_folders.classpath_utils.Source;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.Preconditions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.sirosh.case_folders.classpath_utils.FileConverter.convertFileParameter;

public class CaseFileArgumentsProvider implements ParameterResolver {
    private final PathProvider pathProvider;

    CaseFileArgumentsProvider() {
        this(DefaultPathProvider.getInstance());
    }

    CaseFileArgumentsProvider(PathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        boolean annotated = parameterContext.isAnnotated(CaseFile.class);
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
        CaseFile annotation = parameter.getAnnotation(CaseFile.class);
        Source source = pathProvider.classpathResource(annotation.file());
        Path path = source.get(extensionContext);
        assertIsFile(path);
        return convertFileParameter(parameter, path);
    }

    private void assertIsFile(Path path) {
        Preconditions.condition(!Files.isDirectory(path), "path " + path + "should point to file, but points to directory");
    }
}
