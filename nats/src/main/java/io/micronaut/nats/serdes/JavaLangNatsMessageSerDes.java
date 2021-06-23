/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.nats.serdes;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.nats.client.Message;
import jakarta.inject.Singleton;

/**
 * Serializes and deserializes standard Java types.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class JavaLangNatsMessageSerDes implements NatsMessageSerDes<Object> {

    /**
     * The order of this serDes.
     */
    public static final Integer ORDER = 100;

    protected final List<NatsMessageSerDes> javaSerDes = new ArrayList<>(10);

    /**
     * Default constructor.
     */
    public JavaLangNatsMessageSerDes() {
        javaSerDes.add(getStringSerDes());
        javaSerDes.add(getBooleanSerDes());
        javaSerDes.add(getShortSerDes());
        javaSerDes.add(getIntegerSerDes());
        javaSerDes.add(getLongSerDes());
        javaSerDes.add(getFloatSerDes());
        javaSerDes.add(getDoubleSerDes());
        javaSerDes.add(getByteArraySerDes());
        javaSerDes.add(getByteBufferSerDes());
        javaSerDes.add(getUUIDSerDes());
    }

    @Override
    public byte[] serialize(@Nullable Object data) {
        if (data == null) {
            return null;
        }
        return findSerDes(Argument.of(data.getClass())).serialize(data);
    }

    @Override
    public Object deserialize(Message message, Argument<Object> argument) {
        Argument<?> dataType;
        if (Collection.class.isAssignableFrom(argument.getType())) {
            dataType = argument.getFirstTypeVariable().orElse(argument);
        } else {
            dataType = argument;
        }
        return findSerDes(dataType).deserialize(message, argument);
    }

    @Override
    public boolean supports(Argument<Object> argument) {
        return findSerDes(argument) != null;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * Finds the correct serDes based on the type.
     * @param type The java type
     * @return The serdes, or null if none can be found
     */
    @Nullable
    protected NatsMessageSerDes findSerDes(Argument<?> type) {
        return javaSerDes.stream().filter(serDes -> serDes.supports(type)).findFirst().orElse(null);
    }

    /**
     * @return The serDes that handles {@link String}
     */
    @NonNull
    protected NatsMessageSerDes<String> getStringSerDes() {
        return new StringSerDes();
    }

    /**
     * @return The serDes that handles {@link Short}
     */
    @NonNull
    protected NatsMessageSerDes<Short> getShortSerDes() {
        return new ShortSerDes();
    }

    /**
     * @return The serDes that handles {@link Integer}
     */
    @NonNull
    protected NatsMessageSerDes<Integer> getIntegerSerDes() {
        return new IntegerSerDes();
    }

    /**
     * @return The serDes that handles {@link Long}
     */
    @NonNull
    protected NatsMessageSerDes<Long> getLongSerDes() {
        return new LongSerDes();
    }

    /**
     * @return The serDes that handles {@link Float}
     */
    @NonNull
    protected NatsMessageSerDes<Float> getFloatSerDes() {
        return new FloatSerDes();
    }

    /**
     * @return The serDes that handles {@link Double}
     */
    @NonNull
    protected NatsMessageSerDes<Double> getDoubleSerDes() {
        return new DoubleSerDes();
    }

    /**
     * @return The serDes that handles byte[]
     */
    @NonNull
    protected NatsMessageSerDes<byte[]> getByteArraySerDes() {
        return new ByteArraySerDes();
    }

    /**
     * @return The serDes that handles {@link ByteBuffer}
     */
    @NonNull
    protected NatsMessageSerDes<ByteBuffer> getByteBufferSerDes() {
        return new ByteBufferSerDes();
    }

    /**
     * @return The serDes that handles {@link UUID}
     */
    @NonNull
    protected NatsMessageSerDes<UUID> getUUIDSerDes() {
        return new UUIDSerDes();
    }

    /**
     * @return The serDes that handles {@link UUID}
     */
    @NonNull
    protected NatsMessageSerDes<Boolean> getBooleanSerDes() {
        return new BooleanSerDes();
    }

    /**
     * Serializes/deserializes a {@link String}.
     */
    static class StringSerDes implements NatsMessageSerDes<String> {

        private static final Charset ENCODING = Charset.forName("UTF8");

        @Override
        public String deserialize(Message message, Argument<String> argument) {
            byte[] data = message.getData();
            if (data == null || data.length == 0) {
                return null;
            } else {
                return new String(data, ENCODING);
            }
        }

        @Override
        public byte[] serialize(String data) {
            if (data == null) {
                return null;
            } else {
                return data.getBytes(ENCODING);
            }
        }

        @Override
        public boolean supports(Argument<String> argument) {
            return argument.getType() == String.class;
        }
    }

    /**
     * Serializes/deserializes a {@link Short}.
     */
    static class ShortSerDes implements NatsMessageSerDes<Short> {

        @Override
        public Short deserialize(Message message, Argument<Short> argument) {
            byte[] data = message.getData();
            if (data == null || data.length == 0) {
                return null;
            } else if (data.length != 2) {
                throw new SerializationException("Incorrect message body size to deserialize to a Short");
            } else {
                short value = 0;
                for (byte b : data) {
                    value <<= 8;
                    value |= b & 0xFF;
                }
                return value;
            }
        }

        @Override
        public byte[] serialize(Short data) {
            if (data == null) {
                return null;
            } else {
                return new byte[] {
                        (byte) (data >>> 8), data.byteValue()
                };
            }
        }

        @Override
        public boolean supports(Argument<Short> argument) {
            return argument.getType() == Short.class || argument.getType() == short.class;
        }
    }

    /**
     * Serializes/deserializes an {@link Integer}.
     */
    static class IntegerSerDes implements NatsMessageSerDes<Integer> {

        @Override
        public Integer deserialize(Message message, Argument<Integer> argument) {
            byte[] data = message.getData();
            if (data == null || data.length == 0) {
                return null;
            } else if (data.length != 4) {
                throw new SerializationException("Incorrect message body size to deserialize to an Integer");
            } else {
                int value = 0;
                for (byte b : data) {
                    value <<= 8;
                    value |= b & 0xFF;
                }
                return value;
            }
        }

        @Override
        public byte[] serialize(Integer data) {
            if (data == null) {
                return null;
            } else {
                return new byte[] {
                        (byte) (data >>> 24), (byte) (data >>> 16), (byte) (data >>> 8), data.byteValue()
                };
            }
        }

        @Override
        public boolean supports(Argument<Integer> argument) {
            return argument.getType() == Integer.class || argument.getType() == int.class;
        }
    }

    /**
     * Serializes/deserializes a {@link Long}.
     */
    static class LongSerDes implements NatsMessageSerDes<Long> {

        @Override
        public Long deserialize(Message message, Argument<Long> argument) {
            byte[] data = message.getData();
            if (data == null || data.length == 0) {
                return null;
            } else if (data.length != 8) {
                throw new SerializationException("Incorrect message body size to deserialize to a Long");
            } else {
                long value = 0;
                for (byte b : data) {
                    value <<= 8;
                    value |= b & 0xFF;
                }
                return value;
            }
        }

        @Override
        public byte[] serialize(Long data) {
            if (data == null) {
                return null;
            } else {
                return new byte[] {
                        (byte) (data >>> 56), (byte) (data >>> 48), (byte) (data >>> 40), (byte) (data >>> 32),
                        (byte) (data >>> 24), (byte) (data >>> 16), (byte) (data >>> 8), data.byteValue()
                };
            }
        }

        @Override
        public boolean supports(Argument<Long> argument) {
            return argument.getType() == Long.class || argument.getType() == long.class;
        }
    }

    /**
     * Serializes/deserializes a {@link Float}.
     */
    static class FloatSerDes implements NatsMessageSerDes<Float> {

        @Override
        public Float deserialize(Message message, Argument<Float> argument) {
            byte[] data = message.getData();
            if (data == null || data.length == 0) {
                return null;
            } else if (data.length != 4) {
                throw new SerializationException("Incorrect message body size to deserialize to a Float");
            } else {
                int value = 0;
                for (byte b : data) {
                    value <<= 8;
                    value |= b & 0xFF;
                }
                return Float.intBitsToFloat(value);
            }
        }

        @Override
        public byte[] serialize(Float data) {
            if (data == null) {
                return null;
            } else {
                long bits = Float.floatToRawIntBits(data);
                return new byte[] {
                        (byte) (bits >>> 24), (byte) (bits >>> 16), (byte) (bits >>> 8), (byte) bits
                };
            }
        }

        @Override
        public boolean supports(Argument<Float> argument) {
            return argument.getType() == Float.class || argument.getType() == float.class;
        }
    }

    /**
     * Serializes/deserializes a {@link Double}.
     */
    static class DoubleSerDes implements NatsMessageSerDes<Double> {

        @Override
        public Double deserialize(Message message, Argument<Double> argument) {
            byte[] data = message.getData();
            if (data == null || data.length == 0) {
                return null;
            } else if (data.length != 8) {
                throw new SerializationException("Incorrect message body size to deserialize to a Double");
            } else {
                long value = 0;
                for (byte b : data) {
                    value <<= 8;
                    value |= b & 0xFF;
                }
                return Double.longBitsToDouble(value);
            }
        }

        @Override
        public byte[] serialize(Double data) {
            if (data == null) {
                return null;
            } else {
                long bits = Double.doubleToLongBits(data);
                return new byte[] {
                        (byte) (bits >>> 56), (byte) (bits >>> 48), (byte) (bits >>> 40), (byte) (bits >>> 32),
                        (byte) (bits >>> 24), (byte) (bits >>> 16), (byte) (bits >>> 8), (byte) bits
                };
            }
        }

        @Override
        public boolean supports(Argument<Double> argument) {
            return argument.getType() == Double.class || argument.getType() == double.class;
        }
    }

    /**
     * Serializes/deserializes a byte[].
     */
    static class ByteArraySerDes implements NatsMessageSerDes<byte[]> {

        @Override
        public byte[] deserialize(Message message, Argument<byte[]> argument) {
            return message.getData();
        }

        @Override
        public byte[] serialize(byte[] data) {
            return data;
        }

        @Override
        public boolean supports(Argument<byte[]> argument) {
            return argument.getType() == byte[].class;
        }
    }

    /**
     * Serializes/deserializes a {@link ByteBuffer}.
     */
    static class ByteBufferSerDes implements NatsMessageSerDes<ByteBuffer> {

        @Override
        public ByteBuffer deserialize(Message message, Argument<ByteBuffer> argument) {
            byte[] data = message.getData();
            if (data == null || data.length == 0) {
                return null;
            } else {
                return ByteBuffer.wrap(data);
            }
        }

        @Override
        public byte[] serialize(ByteBuffer data) {
            if (data == null) {
                return null;
            } else {
                data.rewind();

                if (data.hasArray()) {
                    byte[] arr = data.array();
                    if (data.arrayOffset() == 0 && arr.length == data.remaining()) {
                        return arr;
                    }
                }

                byte[] ret = new byte[data.remaining()];
                data.get(ret, 0, ret.length);
                data.rewind();
                return ret;
            }
        }

        @Override
        public boolean supports(Argument<ByteBuffer> argument) {
            return argument.getType() == ByteBuffer.class;
        }
    }

    /**
     * Serializes/deserializes a {@link UUID}.
     */
    static class UUIDSerDes implements NatsMessageSerDes<UUID> {

        StringSerDes stringSerDes = new StringSerDes();

        @Override
        public UUID deserialize(Message message, Argument<UUID> argument) {
            String uuid = stringSerDes.deserialize(message, Argument.of(String.class));
            if (uuid == null) {
                return null;
            } else {
                return UUID.fromString(uuid);
            }
        }

        @Override
        public byte[] serialize(UUID data) {
            if (data == null) {
                return null;
            } else {
                return stringSerDes.serialize(data.toString());
            }
        }

        @Override
        public boolean supports(Argument<UUID> argument) {
            return argument.getType() == UUID.class;
        }
    }

    /**
     * Serializes/deserializes a {@link Boolean}.
     */
    static class BooleanSerDes implements NatsMessageSerDes<Boolean> {

        @Override
        public Boolean deserialize(Message message, Argument<Boolean> argument) {
            byte[] data = message.getData();
            if (data == null || data.length == 0) {
                return null;
            } else if (data.length != 1) {
                throw new SerializationException("Incorrect message body size to deserialize to a Boolean");
            } else {
                return data[0] != 0;
            }
        }

        @Override
        public byte[] serialize(Boolean data) {
            if (data == null) {
                return null;
            } else {
                byte[] bytes = new byte[1];
                bytes[0] = data ? (byte) 1 : (byte) 0;
                return bytes;
            }
        }

        @Override
        public boolean supports(Argument<Boolean> argument) {
            return argument.getType() == Boolean.class || argument.getType() == boolean.class;
        }
    }
}
