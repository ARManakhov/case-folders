package dev.sirosh.case_folders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public class TestUtils {

    public static Comparator<InputStream> inputStreamComparator() {
        return Comparator.comparing(TestUtils::readInputStream);
    }

    public static String readInputStream(InputStream stream) {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try {
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                out.append(buffer, 0, numRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("error on input stream content matching", e);
        }
        return out.toString();
    }
}
