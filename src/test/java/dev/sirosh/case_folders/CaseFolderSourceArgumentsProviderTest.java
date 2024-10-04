package dev.sirosh.case_folders;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class CaseFolderSourceArgumentsProviderTest {

    @MethodSource("argsSource")
    @ParameterizedTest(name = "{0}")
    void provideArguments(Method method, List<Objects[]> expected) {
        ExtensionContext context = mock();
        doReturn(TestClass.class).when(context).getRequiredTestClass();
        doReturn(method).when(context).getRequiredTestMethod();

        CaseFolderSource annotation = method.getAnnotation(CaseFolderSource.class);

        CaseFolderSourceArgumentsProvider provider = new CaseFolderSourceArgumentsProvider();
        provider.accept(annotation);

        Stream<? extends Arguments> actual = provider.provideArguments(context);

        assertThat(actual.map(Arguments::get))
                .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                        .withComparatorForType(TestUtils.inputStreamComparator(), InputStream.class)
                        .build())
                .containsExactlyInAnyOrderElementsOf(expected);
    }


    public static Stream<Arguments> argsSource() throws NoSuchMethodException, URISyntaxException, IOException {
        Path casePath = Path.of(TestClass.class.getResource("/test_cat_and_dog").toURI());
        Path memePath = casePath.resolve("meme");
        Path minePath = casePath.resolve("mine");
        return Stream.of(
                Arguments.of(named("no params test", TestClass.class.getMethod("testNone")), List.of(array(), array())),
                Arguments.of(named("no params test, include name", TestClass.class.getMethod("testNoneNamed")),
                        List.of(array(named("meme", null)), array(named("mine", null)))),
                Arguments.of(named("no params test, include path", TestClass.class.getMethod("testNone", Path.class)),
                        List.of(array(memePath), array(minePath))),
                Arguments.of(named("no params test, include path as file", TestClass.class.getMethod("testNone", File.class)),
                        List.of(array(memePath.toFile()), array(minePath.toFile()))),
                Arguments.of(
                        named("no params test, include path, include name", TestClass.class.getMethod("testNoneNamed", Path.class)),
                        List.of(array(named("meme", memePath)), array(named("mine", minePath)))),
                Arguments.of(
                        named("no params test, include path as File, include name", TestClass.class.getMethod("testNoneNamed", File.class)),
                        List.of(array(named("meme", memePath)), array(named("mine", minePath)))),
                Arguments.of(named("one param test, string params", TestClass.class.getMethod("testCats", String.class)),
                        List.of(array("my cats"), array("meme cats"))),
                Arguments.of(named("one param test, path params", TestClass.class.getMethod("testCats", InputStream.class)),
                        List.of(array(Files.newInputStream(memePath.resolve("cat"))), array(Files.newInputStream(minePath.resolve("cat"))))),
                Arguments.of(named("one param test, path params", TestClass.class.getMethod("testCats", Path.class)),
                        List.of(array(memePath.resolve("cat")), array(minePath.resolve("cat")))),
                Arguments.of(named("one param test, file params", TestClass.class.getMethod("testCats", File.class)),
                        List.of(array(memePath.resolve("cat").toFile()), array(minePath.resolve("cat").toFile()))),
                Arguments.of(named("one param test, byte array params", TestClass.class.getMethod("testCats", byte[].class)),
                        List.of(array(Files.readAllBytes(memePath.resolve("cat"))), array(Files.readAllBytes(minePath.resolve("cat"))))),
                Arguments.of(named("one param test, include name", TestClass.class.getMethod("testCatsNamed", String.class)),
                        List.of(array(named("mine", "my cats")), array(named("meme", "meme cats")))),
                Arguments.of(
                        named("one param test, include path", TestClass.class.getMethod("testCats", Path.class, String.class)),
                        List.of(array(minePath, "my cats"), array(memePath, "meme cats"))),
                Arguments.of(
                        named("one param test, include path, include name",
                                TestClass.class.getMethod("testCatsNamed", Path.class, String.class)),
                        List.of(array(named("mine", minePath), "my cats"), array(named("meme", memePath), "meme cats"))),
                Arguments.of(named("two param test", TestClass.class.getMethod("testCatsAndDogs", String.class, String.class)),
                        List.of(array("my cats", "my dogs"), array("meme cats", "meme dogs"))),
                Arguments.of(
                        named("two param test, include name",
                                TestClass.class.getMethod("testCatsAndDogsNamed", String.class, String.class)),
                        List.of(array(named("mine", "my cats"), "my dogs"), array(named("meme", "meme cats"), "meme dogs"))),
                Arguments.of(
                        named("two param test, include path",
                                TestClass.class.getMethod("testCatsAndDogs", Path.class, String.class, String.class)),
                        List.of(array(minePath, "my cats", "my dogs"), array(memePath, "meme cats", "meme dogs"))),
                Arguments.of(
                        named("two param test, include path, include name",
                                TestClass.class.getMethod("testCatsAndDogsNamed", Path.class, String.class, String.class)),
                        List.of(array(named("mine", minePath), "my cats", "my dogs"),
                                array(named("meme", memePath), "meme cats", "meme dogs"))),
                Arguments.of(
                        named("three param test, one absolute, include path, include name",
                                TestClass.class.getMethod("testCatsAndDogs", Path.class, String.class, String.class, String.class)),
                        List.of(array(minePath, "my cats", "my dogs", "common info about test"),
                                array(memePath, "meme cats", "meme dogs", "common info about test"))));
    }

    private abstract static class TestClass {
        @CaseFolderSource(folder = "/test_cat_and_dog")
        public abstract void testNoneNamed();

        @CaseFolderSource(folder = "/test_cat_and_dog")
        public abstract void testNoneNamed(@CaseFolder Path casePath);

        @CaseFolderSource(folder = "/test_cat_and_dog")
        public abstract void testNoneNamed(@CaseFolder File caseFile);

        @CaseFolderSource(folder = "/test_cat_and_dog")
        public abstract void testCatsNamed(@CaseFile(file = "cat") String cat);

        @CaseFolderSource(folder = "/test_cat_and_dog")
        public abstract void testCatsNamed(@CaseFolder Path casePath, @CaseFile(file = "cat") String cat);

        @CaseFolderSource(folder = "/test_cat_and_dog")
        public abstract void testCatsAndDogsNamed(@CaseFile(file = "cat") String cat, @CaseFile(file = "dog") String dog);

        @CaseFolderSource(folder = "/test_cat_and_dog")
        public abstract void testCatsAndDogsNamed(@CaseFolder Path casePath, @CaseFile(file = "cat") String cat,
                                                  @CaseFile(file = "dog") String dog);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testNone();

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testNone(@CaseFolder Path casePath);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testNone(@CaseFolder File caseFile);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCats(@CaseFile(file = "cat") String cat);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCats(@CaseFile(file = "cat") InputStream cat);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCats(@CaseFile(file = "cat") Path cat);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCats(@CaseFile(file = "cat") File cat);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCats(@CaseFile(file = "cat") byte[] cat);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCats(@CaseFolder Path casePath, @CaseFile(file = "cat") String cat);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCatsAndDogs(@CaseFile(file = "cat") String cat, @CaseFile(file = "dog") String dog);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCatsAndDogs(@CaseFolder Path casePath, @CaseFile(file = "cat") String cat,
                                             @CaseFile(file = "dog") String dog);

        @CaseFolderSource(folder = "/test_cat_and_dog", nameFromCaseFolder = false)
        public abstract void testCatsAndDogs(@CaseFolder Path casePath, @CaseFile(file = "cat") String cat,
                                             @CaseFile(file = "dog") String dog, @CaseFile(file = "/test_cat_and_dog/common", absolute = true) String common);
    }

    private static Object[] array(Object... objects) {
        return objects;
    }
}
