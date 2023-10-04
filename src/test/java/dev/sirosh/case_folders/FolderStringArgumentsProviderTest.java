package dev.sirosh.case_folders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FolderStringArgumentsProviderTest {

    @Test
    void provideArguments_folderExistsTwoCasesOneFileIncludeName() {
        String folder = "/test_cat";
        String[] files = {"cat"};
        boolean includeName = true;

        Stream<Object[]> stream = provideArguments(folder, files, includeName);

        assertThat(stream)
                .containsExactly(array("mine", "my cats"),
                        array("meme", "meme cats"));
    }

    @Test
    void provideArguments_folderExistsTwoCasesOneFileDontIncludeName() {
        String folder = "/test_cat";
        String[] files = {"cat"};
        boolean includeName = false;

        Stream<Object[]> stream = provideArguments(folder, files, includeName);

        assertThat(stream)
                .containsExactly(array("my cats"),
                        array("meme cats"));
    }

    @Test
    void provideArguments_folderExistsTwoCasesTwoFileIncludeName() {
        String folder = "/test_cat_and_dog";
        String[] files = {"cat", "dog"};
        boolean includeName = true;

        Stream<Object[]> stream = provideArguments(folder, files, includeName);

        assertThat(stream)
                .containsExactly(array("mine", "my cats", "my dogs"),
                        array("meme", "meme cats", "meme dogs"));
    }

    private static Stream<Object[]> provideArguments(String folder, String[] files, boolean includeName) {
        ExtensionContext context = mock();
        doReturn(FolderStringArgumentsProviderTest.class).when(context).getRequiredTestClass();
        FolderSource annotation = mock();
        when(annotation.folder()).thenReturn(folder);
        when(annotation.files()).thenReturn(files);
        when(annotation.includeName()).thenReturn(includeName);
        FolderStringArgumentsProvider provider = new FolderStringArgumentsProvider();
        return provider.provideArguments(context, annotation)
                .map(Arguments::get);
    }

    private static Object[] array(Object... objects) {
        return objects;
    }
}