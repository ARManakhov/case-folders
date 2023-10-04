package dev.sirosh.case_folders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.util.Preconditions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

class FolderStringArgumentsProvider extends AnnotationBasedArgumentsProvider<FolderSource> {
    private final PathProvider pathProvider;

    FolderStringArgumentsProvider() {
        this(DefaultPathProvider.INSTANCE);
    }

    FolderStringArgumentsProvider(PathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }

    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext context, FolderSource folderSource) {
        List<Path> caseFolders = getCaseFolders(context, folderSource.folder());
        return caseFolders.stream()
                .map(caseFolder -> {
                    Stream<String> params = getFileParams(folderSource.files(), caseFolder);
                    if (folderSource.includeName()) {
                        params = Stream.concat(Stream.of(caseFolder.getFileName().toString()), params);
                    }
                    return Arguments.of(params.toArray());
                });
    }

    private static Stream<String> getFileParams(String[] files, Path caseFolder) {
        return Arrays.stream(files)
                .map(caseFolder::resolve)
                .filter(not(Files::isDirectory))
                .map(file -> {
                    try {
                        return Files.readString(file);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("I/O error while reading " + file, e);
                    }
                });
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
            return caseFoldersStream.toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("I/O error in case folders listing", e);
        }
    }


    @FunctionalInterface
    private interface Source {
        Path get(ExtensionContext context);

    }

    interface PathProvider {
        Path getClasspathResource(Class<?> baseClass, String path);

        default Source classpathResource(String path) {
            return context -> getClasspathResource(context.getRequiredTestClass(), path);
        }
    }

    private static class DefaultPathProvider implements PathProvider {

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
    }
}