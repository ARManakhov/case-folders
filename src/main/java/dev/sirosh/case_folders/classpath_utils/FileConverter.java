package dev.sirosh.case_folders.classpath_utils;

import org.junit.platform.commons.PreconditionViolationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileConverter {
    public static Object convertFolderParameter(Path caseFolder, Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        if (parameterType.equals(Path.class)) {
            return caseFolder;
        }
        if (parameterType.equals(File.class)) {
            return caseFolder.toFile();
        }
        if (parameterType.equals(String.class)) {
            return caseFolder.toString();
        }
        throw new PreconditionViolationException("Can only resolve @CaseFolder " + parameter.getName()
                + " of type " + Path.class.getName()
                + " or " + File.class.getName()
                + " or " + String.class.getName()
                + " but was: " + parameterType.getName());
    }

    public static Object convertFileParameter(Parameter parameter, Path path) {
        Class<?> parameterType = parameter.getType();
        if (parameterType.equals(Path.class)) {
            return path;
        }
        if (parameterType.equals(File.class)) {
            return path.toFile();
        }
        if (parameterType.equals(String.class)) {
            try {
                return Files.readString(path);
            } catch (IOException e) {
                throw new RuntimeException("I/O error while reading " + path, e);
            }
        }
        if (InputStream.class.isAssignableFrom(parameterType)) {
            try {
                return Files.newInputStream(path);
            } catch (IOException e) {
                throw new RuntimeException("I/O error while reading " + path, e);
            }
        }
        if (parameterType.equals(byte[].class)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                throw new RuntimeException("I/O error while reading " + path, e);
            }
        }
        throw new PreconditionViolationException("Can only resolve @CaseFile " + parameter.getName()
                + " of type " + Path.class.getName()
                + " or " + File.class.getName()
                + " or " + String.class.getName()
                + " but was: " + parameterType.getName());
    }

}
