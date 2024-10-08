package examples;

import dev.sirosh.case_folders.CaseFile;
import dev.sirosh.case_folders.CaseFileArgumentsProvider;
import dev.sirosh.case_folders.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * To use @CaseFile anotation in you'r methods, include CaseFileArgumentsProvider.class as extension
 */
@ExtendWith(CaseFileArgumentsProvider.class)
public class OneParameterExampleTest {
    /**
     * @CaseFile annotation allows you to inject data from test resources
     * in this example @param catName is value from @location
     * classpath:/examples/cat_with_red_scarf
     */
    @Test
    void oneCatName(@CaseFile(file = "/examples/cat_with_red_scarf") String catName) {
        assertThat(catName)
                .isEqualTo("Sakamoto");
    }

    /**
     * you can provide more than one parameter within your test
     */
    @Test
    void twoCats(@CaseFile(file = "/examples/cat_with_red_scarf") String catWithRedScarfName,
                @CaseFile(file = "/examples/kikis_cat") String kikisCatName) {
        assertThat(catWithRedScarfName)
                .isEqualTo("Sakamoto");
        assertThat(kikisCatName)
                .isEqualTo("Jiji");
    }

    /**
     * @CaseFile annotation also can be around Path.class parameter
     */
    @Test
    void oneCatNamePath(@CaseFile(file = "/examples/cat_with_red_scarf") Path catNamePath) throws IOException {
        assertThat(Files.readString(catNamePath))
                .isEqualTo("Sakamoto");
    }

    /**
     * or File.class
     */
    @Test
    void oneCatNameFile(@CaseFile(file = "/examples/cat_with_red_scarf") File catNameFile) throws IOException {
        assertThat(Files.readString(catNameFile.toPath()))
                .isEqualTo("Sakamoto");
    }

    /**
     * or InputStream.class
     */
    @Test
    void oneCatNameStream(@CaseFile(file = "/examples/cat_with_red_scarf") InputStream catNameStream) {
        assertThat(TestUtils.readInputStream(catNameStream))
                .isEqualTo("Sakamoto");
    }

    /**
     * or even byte array
     */
    @Test
    void oneCatNameByteArray(@CaseFile(file = "/examples/cat_with_red_scarf") byte[] catNameByteArray) {
        assertThat(catNameByteArray)
                .isEqualTo(new byte[]{83, 97, 107, 97, 109, 111, 116, 111});
    }
}
