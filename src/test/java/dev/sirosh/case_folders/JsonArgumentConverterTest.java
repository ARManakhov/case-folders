package dev.sirosh.case_folders;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonArgumentConverterTest {
    @Test
    void isAwareOfNull() {
        assertConverts(null, new TypeReference<Object>() {
        }, null);
        assertConverts(null, new TypeReference<String>() {
        }, null);
        assertConverts(null, new TypeReference<Boolean>() {
        }, null);
    }

    @Test
    void throwsExceptionForNotStringInput() {
        assertThatExceptionOfType(ArgumentConversionException.class) //
                .isThrownBy(() -> convert(12, Object.class)) //
                .withMessage("The argument should be a string: 12");
    }

    @Test
    void isAwareOfWrapperTypesForPrimitiveTypes() {
        assertConverts("true", boolean.class, true);
        assertConverts("false", boolean.class, false);
        assertConverts("1", byte.class, (byte) 1);
        assertConverts("1", short.class, (short) 1);
        assertConverts("1", int.class, 1);
        assertConverts("1", long.class, 1L);
        assertConverts("1.0", float.class, 1.0f);
        assertConverts("1.0", double.class, 1.0d);
    }

    @Test
    void convertsStringsToPrimitiveWrapperTypes() {
        assertConverts("true", Boolean.class, true);
        assertConverts("false", Boolean.class, false);
        assertConverts("10", Byte.class, (byte) 10);
        assertConverts("12", Short.class, (short) 12);
        assertConverts("42", Integer.class, 42);
        assertConverts("42", Long.class, 42L);
        assertConverts("42.23", Float.class, 42.23f);
        assertConverts("42.23", Double.class, 42.23);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(classes = {char.class, boolean.class, short.class, byte.class, int.class, long.class, float.class,
            double.class})
    void throwsExceptionForNullToPrimitiveTypeConversion(Class<?> type) {
        assertThatExceptionOfType(ArgumentConversionException.class) //
                .isThrownBy(() -> convert(null, type)) //
                .withMessage("Cannot convert null to primitive value of type " + type.getCanonicalName());
    }


    @Test
    void throwsExceptionOnInvalidStringForPrimitiveTypes() {
        assertThatExceptionOfType(ArgumentConversionException.class) //
                .isThrownBy(() -> convert("tru", boolean.class)) //
                .withMessage("Failed to convert String \"tru\" to type boolean");
    }

    @Test
    void classConversion() {
        assertConverts("{\"name\":\"enigma\"}", Enigma.class, new Enigma("enigma"));
    }

    @Test
    void collectionConversion() {
        assertConverts("[{\"name\":\"enigma\"}]", new TypeReference<>() {
        }, List.of(new Enigma("enigma")));
        assertConverts("[\"enigma\", \"buz\"]", new TypeReference<>() {
        }, List.of("enigma", "buz"));
        assertConverts("[[{\"name\":\"enigma\"}]]", new TypeReference<>() {
        }, List.of(List.of(new Enigma("enigma"))));
        assertConverts("[{\"name\":\"enigma\"}]", new TypeReference<>() {
        }, Set.of(new Enigma("enigma")));
    }

    @Test
    void arrayConversion() {
        assertConverts("[\"foo\", \"buz\"]", new TypeReference<String[]>() {
        }, new String[]{"foo", "buz"});
        assertConverts("[[\"foo1\", \"buz1\"], [\"foo2\", \"buz2\"]]", new TypeReference<String[][]>() {
        }, new String[][]{{"foo1", "buz1"}, {"foo2", "buz2"}});

    }

    @Test
    void objectArrayConversion() {
        String input = "[{\"name\":\"enigma\"}]";
        TypeReference<Enigma[]> type = new TypeReference<>() {
        };
        Enigma[] expected = {new Enigma("enigma")};
        Enigma[] result = (Enigma[]) convert(input, type);

        assertThat(result) //
                .describedAs(input + " --(" + type.getType().getTypeName() + ")--> " + Arrays.toString(expected)) //
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(expected);
    }

    @Test
    void convertsStringsToEnumConstants() {
        assertConverts("\"DAYS\"", TimeUnit.class, TimeUnit.DAYS);
    }

    // --- java.io and java.nio ------------------------------------------------

    @Test
    void convertsStringToCharset() {
        assertConverts("\"ISO-8859-1\"", Charset.class, StandardCharsets.ISO_8859_1);
        assertConverts("\"UTF-8\"", Charset.class, StandardCharsets.UTF_8);
    }

    @Test
    void convertsStringToFile() {
        assertConverts("\"file\"", File.class, new File("file"));
        assertConverts("\"/file\"", File.class, new File("/file"));
        assertConverts("\"/some/file\"", File.class, new File("/some/file"));
    }

    @Test
    void convertsStringToPath() {
        assertConverts("\"path\"", Path.class, Paths.get("path"));
        assertConverts("\"/path\"", Path.class, Paths.get("/path"));
        assertConverts("\"/some/path\"", Path.class, Paths.get("/some/path"));
    }

//---java.math -----------------------------------------------------------

    @Test
    void convertsStringToBigDecimal() {
        assertConverts("123.456e789", BigDecimal.class, new BigDecimal("123.456e789"));
        assertConverts("\"123.456e789\"", BigDecimal.class, new BigDecimal("123.456e789"));
    }

    @Test
    void convertsStringToBigInteger() {
        assertConverts("\"1234567890123456789\"", BigInteger.class, new BigInteger("1234567890123456789"));
    }

    // --- java.net ------------------------------------------------------------

    @Test
    void convertsStringToURI() {
        assertConverts("\"https://docs.oracle.com/en/java/javase/12/\"", URI.class,
                URI.create("https://docs.oracle.com/en/java/javase/12/"));
    }

    @Test
    void convertsStringToURL() throws Exception {
        assertConverts("\"https://junit.org/junit5\"", URL.class, URI.create("https://junit.org/junit5").toURL());
    }

    // --- java.time -----------------------------------------------------------

    @Test
    void convertsStringsToJavaTimeInstances() {
        assertConverts("\"PT1234.5678S\"", Duration.class, Duration.ofSeconds(1234, 567800000));
        assertConverts("\"1970-01-01T00:00:00Z\"", Instant.class, Instant.ofEpochMilli(0));
        assertConverts("\"2017-03-14\"", LocalDate.class, LocalDate.of(2017, 3, 14));
        assertConverts("\"2017-03-14T12:34:56.789\"", LocalDateTime.class,
                LocalDateTime.of(2017, 3, 14, 12, 34, 56, 789_000_000));
        assertConverts("\"12:34:56.789\"", LocalTime.class, LocalTime.of(12, 34, 56, 789_000_000));
        assertConverts("\"--03-14\"", MonthDay.class, MonthDay.of(3, 14));
        assertConverts("\"2017-03-14T12:34:56.789Z\"", OffsetDateTime.class,
                OffsetDateTime.of(2017, 3, 14, 12, 34, 56, 789_000_000, ZoneOffset.UTC));
        assertConverts("\"12:34:56.789Z\"", OffsetTime.class, OffsetTime.of(12, 34, 56, 789_000_000, ZoneOffset.UTC));
        assertConverts("\"P2M6D\"", Period.class, Period.of(0, 2, 6));
        assertConverts("\"2017\"", Year.class, Year.of(2017));
        assertConverts("\"2017-03\"", YearMonth.class, YearMonth.of(2017, 3));
        assertConverts("\"2017-03-14T12:34:56.789Z\"", ZonedDateTime.class,
                ZonedDateTime.of(2017, 3, 14, 12, 34, 56, 789_000_000, ZoneOffset.UTC));
        assertConverts("\"Europe/Berlin\"", ZoneId.class, ZoneId.of("Europe/Berlin"));
        assertConverts("\"+02:30\"", ZoneOffset.class, ZoneOffset.ofHoursMinutes(2, 30));
    }

    // --- java.util -----------------------------------------------------------

    @Test
    void convertsStringToCurrency() {
        assertConverts("\"JPY\"", Currency.class, Currency.getInstance("JPY"));
    }

    @Test
    void convertsStringToLocale() {
        assertConverts("\"en\"", Locale.class, Locale.ENGLISH);
    }

    @Test
    void convertsStringToUUID() {
        var uuidJson = "\"d043e930-7b3b-48e3-bdbe-5a3ccfb833db\"";
        var uuid = "d043e930-7b3b-48e3-bdbe-5a3ccfb833db";
        assertConverts(uuidJson, UUID.class, UUID.fromString(uuid));
    }

    // -------------------------------------------------------------------------

    private void assertConverts(Object input, TypeReference<?> type, Object expectedOutput) {
        var result = convert(input, type);

        assertThat(result) //
                .describedAs(input + " --(" + type.getType().getTypeName() + ")--> " + expectedOutput) //
                .isEqualTo(expectedOutput);
    }

    @SuppressWarnings("unchecked cast")
    private <T> void assertConverts(Object input, TypeReference<Iterable<T>> type, Iterable<T> expectedOutput) {
        var result = (Iterable<T>) convert(input, type);

        assertThat(result) //
                .describedAs(input + " --(" + type.getType().getTypeName() + ")--> " + expectedOutput) //
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(expectedOutput);
    }


    private void assertConverts(Object input, Class<?> type, Object expectedOutput) {
        var result = convert(input, type);

        assertThat(result) //
                .describedAs(input + " --(" + type.getName() + ")--> " + expectedOutput) //
                .usingRecursiveComparison()
                .isEqualTo(expectedOutput);
    }

    private Object convert(Object input, Class<?> type) {
        ParameterContext context = parameterContext(type);
        return convert(input, context);
    }

    private Object convert(Object input, TypeReference<?> type) {
        ParameterContext context = parameterContext(type);
        return convert(input, context);
    }

    private Object convert(Object input, ParameterContext context) {
        JsonArgumentConverter converter = new JsonArgumentConverter();
        return converter.convert(input, context);
    }

    private static ParameterContext parameterContext(TypeReference<?> typeReference) {
        Parameter parameter = mock();
        when(parameter.getParameterizedType()).thenReturn(typeReference.getType());
        ParameterContext parameterContext = mock();
        when(parameterContext.getParameter()).thenReturn(parameter);
        return parameterContext;
    }


    private ParameterContext parameterContext(Class<?> type) {
        Parameter parameter = mock();
        when(parameter.getParameterizedType()).thenReturn(type);
        ParameterContext parameterContext = mock();
        when(parameterContext.getParameter()).thenReturn(parameter);
        return parameterContext;
    }

    @SuppressWarnings("unused")
    private static void foo() {
    }

    private static class Enigma {
        public Enigma(String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        public Enigma() {
        }

        @SuppressWarnings("unused")
        public String getName() {
            return name;
        }

        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }

        private String name;

        @SuppressWarnings("unused")
        void foo() {
        }
    }
}