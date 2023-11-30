package dev.sirosh.case_folders;

import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Named.named;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

import dev.sirosh.case_folders.classpath_utils.DefaultPathProvider;
import dev.sirosh.case_folders.classpath_utils.PathProvider;
import dev.sirosh.case_folders.classpath_utils.Source;

class FolderStringArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<FolderSource> {
  private FolderSource folderSource;
  private final PathProvider pathProvider;

  FolderStringArgumentsProvider() {
    this(DefaultPathProvider.getInstance());
  }

  FolderStringArgumentsProvider(PathProvider pathProvider) {
    this.pathProvider = pathProvider;
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    List<Path> caseFolders = getCaseFolders(context, folderSource.folder());

    return caseFolders.stream()
        .map(caseFolder -> {
          List<Object> values = getFileParams(folderSource.files(), caseFolder)
              .collect(Collectors.toList());
          Parameter[] parameters = context.getRequiredTestMethod().getParameters();
          findCaseFolderParam(parameters)
              .ifPresent(i -> {
                if (parameters[i].getType().equals(Path.class)) {
                  values.add(i, caseFolder);
                  return;
                }
                if (parameters[i].getType().equals(File.class)) {
                  values.add(i, caseFolder.toFile());
                  return;
                }
                values.add(i, null);
              });
          Object[] valuesArray = values.toArray();
          if (!folderSource.nameFromCaseFolder()) {
            return Arguments.of(valuesArray);
          }
          if (valuesArray.length == 0) {
            return Arguments.of(named(caseFolder.getFileName().toString(), null));
          }
          valuesArray[0] = named(caseFolder.getFileName().toString(), valuesArray[0]);
          return Arguments.of(valuesArray);
        });
  }

  private static Optional<Integer> findCaseFolderParam(Parameter[] parameters) {
    for (int i = 0; i < parameters.length; i++) {
      if (nonNull(parameters[i].getAnnotation(CaseFolder.class))) {
        return Optional.of(i);
      }
    }
    return Optional.empty();
  }

  @Override
  public void accept(FolderSource folderSource) {
    this.folderSource = folderSource;
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
      return caseFoldersStream.collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException("I/O error in case folders listing", e);
    }
  }
}
