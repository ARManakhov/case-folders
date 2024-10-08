package examples;


import dev.sirosh.case_folders.CaseFile;
import dev.sirosh.case_folders.CaseFolder;
import dev.sirosh.case_folders.CaseFolderSource;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MultipleCasesExampleTest {
    /**
     * to create parametrized test that uses resources you can use @CaseFolderSource
     */
    @ParameterizedTest
    @CaseFolderSource(folder = "/examples/cat_cafe_cats", nameFromCaseFolder = false)
    public void iteratingCatNamesInCatCafe_EN(@CaseFile(file = "name") String name) {
        assertThat(name)
                .isIn(List.of(
                        "Blini cat (actual name: Jazz)",
                        "long cat (actual name: Shiroi (白い – “white” in Japanese) or Nobiko)",
                        "Maxwell the Cat, also known as Spinning Cat (actual name: Jess)"
                ));
    }

    /**
     * @CaseFolderSource supports files with different names than parameter
     */
    @ParameterizedTest
    @CaseFolderSource(folder = "/examples/cat_cafe_cats", nameFromCaseFolder = false)
    public void iteratingCatNamesInCatCafe_RUS(@CaseFile(file = "name_rus") String name) {
        assertThat(name)
                .isIn(List.of(
                        "Кот с блинами (настоящее имя: Джаз)",
                        "Длинный кот (настоящее имя: Шиори (白い – с Японского “белый”) или Нобико)",
                        "Кот Максвел, также известный как кртящийся кот (настоящее имя: Джесс)"
                ));
    }


    /**
     * @param caseFolder can be Path, File or String (will provide path to folder)
     * @CaseFolder annotation allows to access folder with case itself
     */
    @ParameterizedTest
    @CaseFolderSource(folder = "/examples/cat_cafe_cats", nameFromCaseFolder = false)
    public void iteratingCatNamesInCatCafe_caseFolder(@CaseFolder Path caseFolder) throws IOException {
        String en = Files.readString(caseFolder.resolve("name"));
        String rus = Files.readString(caseFolder.resolve("name_rus"));
        assertThat("English name is " + en + "\nRussian name is " + rus + "\n")
                .isIn(List.of(
                        "English name is long cat (actual name: Shiroi (白い – “white” in Japanese) or Nobiko)\n" +
                                "Russian name is Длинный кот (настоящее имя: Шиори (白い – с Японского “белый”) или Нобико)\n",
                        "English name is Maxwell the Cat, also known as Spinning Cat (actual name: Jess)\n" +
                                "Russian name is Кот Максвел, также известный как кртящийся кот (настоящее имя: Джесс)\n",
                        "English name is Blini cat (actual name: Jazz)\n" +
                                "Russian name is Кот с блинами (настоящее имя: Джаз)\n"
                ));
    }

    /**
     * on nameFromCaseFolder = true (default value), first parameter will be named after case folder.
     * no matter if it is @CaseFolder or CaseFile
     */
    @ParameterizedTest(name = "{0}")
    @CaseFolderSource(folder = "/examples/cat_cafe_cats")
    public void iteratingCatNamesInCatCafe_caseFolderNamed(@CaseFolder Path caseFolder) {
        assertThat(caseFolder).isNotNull();
    }

    @ParameterizedTest(name = "{0}")
    @CaseFolderSource(folder = "/examples/cat_cafe_cats")
    public void iteratingCatNamesInCatCafe_caseFileNamed(@CaseFile(file = "name") File caseFile) {
        assertThat(caseFile).isNotNull();
    }

    /**
     * @CaseFile annotation can be set absolute = true to get file outside of case folder
     */
    @ParameterizedTest(name = "{0}")
    @CaseFolderSource(folder = "/examples/cat_cafe_cats")
    public void iteratingCatNamesInCatCafe_caseFolder(@CaseFile(file = "name") String catName,
                                                      @CaseFile(file = "/examples/cat_cafe_cats/cat_cafe_name", absolute = true)
                                                      String cafeName) {
        assertThat(catName + " likes to hang out in " + cafeName)
                .isIn(List.of(
                        "Blini cat (actual name: Jazz) likes to hang out in Paw&Tail cafe",
                        "long cat (actual name: Shiroi (白い – “white” in Japanese) or Nobiko) likes to hang out in Paw&Tail cafe",
                        "Maxwell the Cat, also known as Spinning Cat (actual name: Jess) likes to hang out in Paw&Tail cafe"
                ));
    }
}
