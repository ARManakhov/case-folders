package dev.sirosh.case_folders;

import dev.sirosh.case_folders.classpath_utils.DefaultPathProvider;
import dev.sirosh.case_folders.classpath_utils.PathProvider;
import dev.sirosh.case_folders.classpath_utils.Source;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.sirosh.case_folders.classpath_utils.FileConverter.convertFileParameter;
import static dev.sirosh.case_folders.classpath_utils.FileConverter.convertFolderParameter;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Named.named;

class CaseFolderSourceArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CaseFolderSource> {
    private CaseFolderSource folderSource;
    private final PathProvider pathProvider;

    CaseFolderSourceArgumentsProvider() {
        this(DefaultPathProvider.getInstance());
    }

    CaseFolderSourceArgumentsProvider(PathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        List<Path> caseFolders = getCaseFolders(context, folderSource.folder());

        return caseFolders.stream()
                .map(caseFolder -> {
                    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
                    Object[] arguments = new Object[parameters.length];
                    provideFolderParam(caseFolder, parameters, arguments);
                    provideFileParams(caseFolder, parameters, arguments, context);

                    if (folderSource.nameFromCaseFolder()) {
                        String caseName = caseFolder.getFileName().toString();
                        if (arguments.length == 0) {
                            return Arguments.of(named(caseName, null));
                        }
                        arguments[0] = named(caseName, arguments[0]);
                    }
                    return Arguments.of(arguments);
                });
    }

    public static void provideFolderParam(Path caseFolder, Parameter[] parameters, Object[] arguments) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (isNull(parameter.getAnnotation(CaseFolder.class))) {
                continue;
            }
            arguments[i] = convertFolderParameter(caseFolder, parameter);
        }
    }

    private void provideFileParams(Path caseFolder, Parameter[] parameters, Object[] arguments,
                                   ExtensionContext context) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            CaseFile annotation = parameter.getAnnotation(CaseFile.class);
            if (nonNull(annotation) && nonNull(parameter.getAnnotation(CaseFolder.class))) {
                throw new PreconditionViolationException("Parameter " + parameter.getName()
                        + " has conflicting annotations @CaseFolder and @CaseFile, use only one per field");
            }
            if (nonNull(parameter.getAnnotation(CaseFolder.class))) {
                continue;
            }
            Preconditions.condition(nonNull(annotation), "No @CaseFile annotation found on parameter " + i);


            String fileStr = annotation.file();
            Path path;
            if (annotation.absolute()) {
                path = pathProvider.classpathResource(fileStr).get(context);
            } else {
                path = caseFolder.resolve(fileStr);
            }
            Preconditions.condition(!Files.isDirectory(path), "File " + path + " should be file");
            arguments[i] = convertFileParameter(parameter, path);
        }
    }

    @Override
    public void accept(CaseFolderSource folderSource) {
        this.folderSource = folderSource;
    }

    private Path getRootFolder(ExtensionContext context, String folder) {
        Source source = pathProvider.classpathResource(folder);
        Path rootFolder = source.get(context);
        Preconditions.condition(Files.isDirectory(rootFolder), "Classpath resource [" + folder + "] must be folder");
        return rootFolder;
    }

    private List<Path> getCaseFolders(ExtensionContext context, String folder) {
        Path rootFolder = getRootFolder(context, folder);
        try (Stream<Path> caseFoldersStream = Files.list(rootFolder).filter(Files::isDirectory)) {
            return caseFoldersStream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("I/O error in case folders listing", e);
        }
    }
}
