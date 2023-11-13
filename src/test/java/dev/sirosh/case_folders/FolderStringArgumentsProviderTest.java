package dev.sirosh.case_folders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FolderStringArgumentsProviderTest {

    @MethodSource("argsSource")
    @ParameterizedTest(name = "{0}")
    void provideArguments(String folder, String[] files, boolean includeName,
                          Method method, List<Objects[]> expected) {
        ExtensionContext context = mock();
        doReturn(TestClass.class).when(context).getRequiredTestClass();
        doReturn(method).when(context).getRequiredTestMethod();
        FolderSource annotation = mock();
        when(annotation.folder()).thenReturn(folder);
        when(annotation.files()).thenReturn(files);
        when(annotation.nameFromCaseFolder()).thenReturn(includeName);
        FolderStringArgumentsProvider provider = new FolderStringArgumentsProvider();
        provider.accept(annotation);

        Stream<? extends Arguments> actual = provider.provideArguments(context);

        assertThat(actual.map(Arguments::get))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    public static Stream<Arguments> argsSource() throws NoSuchMethodException, URISyntaxException {
        Path memePath = Path.of(TestClass.class.getResource("/test_cat_and_dog/meme").toURI());
        Path minePath = Path.of(TestClass.class.getResource("/test_cat_and_dog/mine").toURI());
        return Stream.of(
                Arguments.of(named("no params test", "/test_cat_and_dog"), new String[]{}, false, TestClass.class.getMethod("testNone"), List.of(array(), array())),
                Arguments.of(named("no params test, include name", "/test_cat_and_dog"), new String[]{}, true, TestClass.class.getMethod("testNone"), List.of(array(named("meme", null)), array(named("mine", null)))),
                Arguments.of(named("no params test, include path", "/test_cat_and_dog"), new String[]{}, false, TestClass.class.getMethod("testNone", Path.class), List.of(array(memePath), array(minePath))),
                Arguments.of(named("no params test, include path as file", "/test_cat_and_dog"), new String[]{}, false, TestClass.class.getMethod("testNone", File.class), List.of(array(memePath.toFile()), array(minePath.toFile()))),
                Arguments.of(named("no params test, include path, include name", "/test_cat_and_dog"), new String[]{}, true, TestClass.class.getMethod("testNone", Path.class), List.of(array(named("meme", memePath)), array(named("mine", minePath)))),
                Arguments.of(named("one param test", "/test_cat_and_dog"), new String[]{"cat"}, false, TestClass.class.getMethod("testCats", String.class), List.of(array("my cats"), array("meme cats"))),
                Arguments.of(named("one param test, include name", "/test_cat_and_dog"), new String[]{"cat"}, true, TestClass.class.getMethod("testCats", String.class), List.of(array(named("mine", "my cats")), array(named("meme", "meme cats")))),
                Arguments.of(named("one param test, include path", "/test_cat_and_dog"), new String[]{"cat"}, false, TestClass.class.getMethod("testCats", Path.class, String.class), List.of(array(minePath, "my cats"), array(memePath, "meme cats"))),
                Arguments.of(named("one param test, include path, include name", "/test_cat_and_dog"), new String[]{"cat"}, true, TestClass.class.getMethod("testCats", Path.class, String.class), List.of(array(named("mine", minePath), "my cats"), array(named("meme", memePath), "meme cats"))),
                Arguments.of(named("two param test", "/test_cat_and_dog"), new String[]{"cat", "dog"}, false, TestClass.class.getMethod("testCatsAndDogs", String.class, String.class), List.of(array("my cats", "my dogs"), array("meme cats", "meme dogs"))),
                Arguments.of(named("two param test, include name", "/test_cat_and_dog"), new String[]{"cat", "dog"}, true, TestClass.class.getMethod("testCatsAndDogs", String.class, String.class), List.of(array(named("mine", "my cats"), "my dogs"), array(named("meme", "meme cats"), "meme dogs"))),
                Arguments.of(named("two param test, include path", "/test_cat_and_dog"), new String[]{"cat", "dog"}, false, TestClass.class.getMethod("testCatsAndDogs", Path.class, String.class, String.class), List.of(array(minePath, "my cats", "my dogs"), array(memePath, "meme cats", "meme dogs"))),
                Arguments.of(named("two param test, include path, include name", "/test_cat_and_dog"), new String[]{"cat", "dog"}, true, TestClass.class.getMethod("testCatsAndDogs", Path.class, String.class, String.class), List.of(array(named("mine", minePath), "my cats", "my dogs"), array(named("meme", memePath), "meme cats", "meme dogs")))
        );
    }

    private abstract static class TestClass {
        public abstract void testNone();

        public abstract void testNone(@CaseFolder Path casePath);

        public abstract void testNone(@CaseFolder File caseFile);

        public abstract void testCats(String cat);

        public abstract void testCats(@CaseFolder Path casePath, String cat);

        public abstract void testCatsAndDogs(String cat, String dogs);

        public abstract void testCatsAndDogs(@CaseFolder Path casePath, String cat, String dogs);
    }

    private static Object[] array(Object... objects) {
        return objects;
    }
}