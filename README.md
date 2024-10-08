# @CaseFolder

Library to write tests with resources easily

# How to use

import library to your project

```xml
<dependency>
    <groupId>dev.sirosh</groupId>
    <artifactId>case_folders</artifactId>
    <version>0.0.2</version>
</dependency>
```

and use annotations `@CaseFolderSource`, `@CaseFile`, `@CaseFolder` on junit 5 tests

```java
public class ExampleTest {
    @ParameterizedTest
    @CaseFolderSource(folder = "/examples/cats")
    public void iteratingCatNamesInCatCafe_EN(@CaseFile(file = "name") String name) {
        assertThat(name)
                .isIn(List.of(
                        "Blini cat (actual name: Jazz)",
                        "long cat (actual name: Shiroi (白い – “white” in Japanese) or Nobiko)",
                        "Maxwell the Cat, also known as Spinning Cat (actual name: Jess)"
                ));
    }
}
```

for more examples view [examples](src/test/java/examples)
