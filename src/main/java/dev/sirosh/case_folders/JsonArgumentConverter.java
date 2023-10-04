package dev.sirosh.case_folders;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

import java.lang.reflect.Type;

import static java.util.Objects.isNull;

public class JsonArgumentConverter implements ArgumentConverter, AnnotationConsumer<JsonArgument> {
    private static final ObjectMapper OBJECT_MAPPER = objectMapper();

    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setDateFormat(new StdDateFormat());
    }

    public final void accept(JsonArgument annotation) {
        Preconditions.notNull(annotation, "annotation must not be null");
    }

    public final Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        Type type = context.getParameter().getParameterizedType();
        if (isNull(source)) {
            if (((Class<?>) type).isPrimitive()) {
                throw new ArgumentConversionException(
                        "Cannot convert null to primitive value of type " + type.getTypeName());
            }
            return null;
        }
        if (!(source instanceof String)) {
            throw new ArgumentConversionException("The argument should be a string: " + source);
        }
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(type);
            return OBJECT_MAPPER.readValue((String) source, javaType);
        } catch (Exception e) {
            throw new ArgumentConversionException("Failed to convert String \"" + source + "\" to type " + type.getTypeName(), e);
        }
    }
}