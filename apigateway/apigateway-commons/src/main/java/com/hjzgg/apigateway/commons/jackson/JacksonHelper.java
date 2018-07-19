package com.hjzgg.apigateway.commons.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hjzgg.apigateway.commons.jackson.deserializers.LocalDateDeserializer;
import com.hjzgg.apigateway.commons.jackson.deserializers.LocalDateTimeDeserializer;
import com.hjzgg.apigateway.commons.jackson.deserializers.LocalTimeDeserializer;
import com.hjzgg.apigateway.commons.jackson.serializers.BigDecimalSerializer;
import com.hjzgg.apigateway.commons.jackson.serializers.LocalDateSerializer;
import com.hjzgg.apigateway.commons.jackson.serializers.LocalDateTimeSerializer;
import com.hjzgg.apigateway.commons.jackson.serializers.LocalTimeSerializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class JacksonHelper {
    private final static SimpleModule module = initModule();
    private final static ObjectMapper mapper = new ObjectMapper().registerModule(module).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    private final static ObjectMapper prettyMapper = mapper.copy().configure(SerializationFeature.INDENT_OUTPUT, true);

    private static SimpleModule initModule() {
        return new SimpleModule().
                addSerializer(BigDecimal.class, new BigDecimalSerializer()).
                addSerializer(LocalTime.class, new LocalTimeSerializer()).
                addDeserializer(LocalTime.class, new LocalTimeDeserializer()).
                addSerializer(LocalDate.class, new LocalDateSerializer()).
                addDeserializer(LocalDate.class, new LocalDateDeserializer()).
                addSerializer(LocalDateTime.class, new LocalDateTimeSerializer()).
                addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    }


    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static ObjectMapper getPrettyMapper() {
        return prettyMapper;
    }

    public static JavaType genJavaType(Type type) {
        return getMapper().getTypeFactory().constructType(type);
    }
}
