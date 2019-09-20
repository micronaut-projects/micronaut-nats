package com.github.grimmjo.micronaut.nats.serdes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.nats.client.Message;

/**
 * Serializes and deserializes objects as JSON using Jackson.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class JsonNatsMessageSerDes implements NatsMessageSerDes<Object> {

    /**
     * The order of this serDes.
     */
    public static final Integer ORDER = 200;

    private final ObjectMapper objectMapper;

    /**
     * Default constructor.
     * @param objectMapper The jackson object mapper
     */
    public JsonNatsMessageSerDes(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Nullable
    @Override
    public Object deserialize(Message message, Argument<Object> type) {
        byte[] body = message.getData();
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            if (type.hasTypeVariables()) {
                JavaType javaType = constructJavaType(type);
                return objectMapper.readValue(body, javaType);
            } else {
                return objectMapper.readValue(body, type.getType());
            }
        } catch (IOException e) {
            throw new SerializationException(
                    "Error decoding JSON stream for type [" + type.getName() + "]: " + e.getMessage());
        }
    }

    @Override
    public byte[] serialize(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error encoding object [" + data + "] to JSON: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean supports(Argument<Object> argument) {
        return !ClassUtils.isJavaBasicType(argument.getType());
    }

    private <T> JavaType constructJavaType(Argument<T> type) {
        Map<String, Argument<?>> typeVariables = type.getTypeVariables();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType[] objects = toJavaTypeArray(typeFactory, typeVariables);
        return typeFactory.constructParametricType(type.getType(), objects);
    }

    private JavaType[] toJavaTypeArray(TypeFactory typeFactory, Map<String, Argument<?>> typeVariables) {
        List<JavaType> javaTypes = new ArrayList<>();
        for (Argument<?> argument : typeVariables.values()) {
            if (argument.hasTypeVariables()) {
                javaTypes.add(typeFactory.constructParametricType(argument.getType(),
                        toJavaTypeArray(typeFactory, argument.getTypeVariables())));
            } else {
                javaTypes.add(typeFactory.constructType(argument.getType()));
            }
        }
        return javaTypes.toArray(new JavaType[0]);
    }

}
