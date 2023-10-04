package dev.sirosh.case_folders;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(FolderStringArgumentsProvider.class)
public @interface FolderSource {
    String folder();

    String[] files() default {};

    boolean includeName() default true;
}