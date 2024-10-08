package dev.sirosh.case_folders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class CaseFileArgumentProviderTest {

    @MethodSource("argsSource")
    @ParameterizedTest(name = "{0}")
    void provideArguments(Method method, Object expected) {
        ExtensionContext extensionContext = mock();
        doReturn(TestClass.class).when(extensionContext).getRequiredTestClass();
        doReturn(method).when(extensionContext).getRequiredTestMethod();

        Parameter parameter = method.getParameters()[0];
        ParameterContext parameterContext = mock();
        doReturn(true).when(parameterContext).isAnnotated(CaseFile.class);
        doReturn(method).when(parameterContext).getDeclaringExecutable();
        doReturn(parameter).when(parameterContext).getParameter();

        CaseFileArgumentsProvider provider = new CaseFileArgumentsProvider();

        provider.supportsParameter(parameterContext, extensionContext);

        Object actual = provider.resolveParameter(parameterContext, extensionContext);

        assertThat(actual)
                .isEqualTo(expected);
    }

    public static Stream<Arguments> argsSource() throws NoSuchMethodException, URISyntaxException {
        Path catPath = Path.of(TestClass.class.getResource("/test_cat_and_dog/meme/cat").toURI());
        return Stream.of(
                Arguments.of(named("Path argument", TestClass.class.getMethod("testCat", Path.class)), catPath),
                Arguments.of(named("File argument", TestClass.class.getMethod("testCat", File.class)), catPath.toFile()),
                Arguments.of(named("String argument", TestClass.class.getMethod("testCat", String.class)), "meme cats"));
    }

    private abstract static class TestClass {
        public abstract void testCat(@CaseFile(file = "/test_cat_and_dog/meme/cat") Path cat);

        public abstract void testCat(@CaseFile(file = "/test_cat_and_dog/meme/cat") File cat);
        public abstract void testCat(@CaseFile(file = "/test_cat_and_dog/meme/cat") String cat);
    }
}
