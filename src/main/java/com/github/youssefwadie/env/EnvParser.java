package com.github.youssefwadie.env;

import com.github.youssefwadie.env.annotations.Env;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

public class EnvParser {

    public static final String WILD_CARD_TYPE_ARE_NOT_SUPPORTED = "wild card type are not supported";
    public static final String UNSUPPORTED_TYPE = "Type : %s unsupported";
    private final static Logger log = Logger.getLogger(EnvParser.class.getName());
    private final Map<String, String> environmentVariables;

    public EnvParser() {
        this(System.getenv());
    }

    protected EnvParser(Map<String, String> environmentVariables) {
        Assert.notEmpty(environmentVariables, "environmentVariables cannot be empty");
        this.environmentVariables = environmentVariables;
    }

    /**
     * Parses the given {@link Env} and returns the parsed instance.
     *
     * @param env         the {@link Env} object to parse, must not be {@literal null}.
     * @param resultClass the class representing the desired environment variable type.
     * @return the parsed instance of the specified type.
     * @throws IllegalArgumentException if the {@code env} or {@code resultClass} is null.
     * @throws RuntimeException         if any other exception occurs during the parsing process.
     *                                  The specific exception that occurred will be wrapped in this {@code RuntimeException}.
     */
    @SuppressWarnings("unchecked")
    public <T> T parse(Env env, Class<T> resultClass) {
        Assert.notNull(env, "env must not be null");
        Assert.hasText(env.value(), "env.value() cannot be empty");

        final var envValue = environmentVariables.get(env.value());

        try {
            final var separator = env.separator();
            if (resultClass.equals(List.class)) {
                return (T) parseList(String.class, envValue, separator);
            } else if (resultClass.equals(Set.class)) {
                return (T) parseSet(String.class, envValue, separator);
            } else {
                return (T) parseValue(resultClass, envValue);
            }
        } catch (Throwable t) {
            // TODO handle silence fail
            log.severe(t.getMessage());
        }
        return null;
    }

    /**
     * Parses the given {@link Env} and returns the parsed instance.
     * <p>
     * Convenient method to word with fields and method return types
     * </p>
     *
     * @param env        the {@link Env} object to parse, must not be {@literal null}.
     * @param targetType the class representing the desired environment variable type.
     * @return the parsed instance of the specified type.
     * @throws IllegalArgumentException if the {@code env} or {@code resultClass} is null.
     * @throws RuntimeException         if any other exception occurs during the parsing process.
     *                                  The specific exception that occurred will be wrapped in this {@code RuntimeException}.
     */
    @SuppressWarnings("unchecked")
    public <T> T parse(Env env, Type targetType) {
        Assert.notNull(env, "env must not be null");
        Assert.hasText(env.value(), "env.value() cannot be empty");
        final var envValue = environmentVariables.get(env.value());
        try {
            final var separator = env.separator();

            if (targetType instanceof Class<?> targetClass) {
                return (T) parse(env, targetClass);
            }

            if (!(targetType instanceof ParameterizedType parameterizedType)) {
                return null;
            }

            final var rawType = parameterizedType.getRawType();
            if (rawType.equals(List.class)) {
                return (T) parseList(parameterizedType, envValue, separator);
            } else if (rawType.equals(Set.class)) {
                return (T) parseSet(parameterizedType, envValue, separator);
            } else {
                return (T) parseValue(rawType.getClass(), envValue);
            }

        } catch (Throwable t) {
            // TODO handle silence fail
            log.severe(t.getMessage());
        }
        return null;
    }


    /**
     * Parses a primitive type from the given value string
     *
     * @param type  the primitive type class
     * @param value the string representation of that type
     * @return the parsed Object
     * @throws IllegalStateException if the passed type is not a primitive type.
     */
    private Object parseValue(Class<?> type, String value) {
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            return Short.parseShort(value);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.parseLong(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.parseFloat(value);
        } else if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Object.class)) {
            return value;
        } else if (type.equals(Number.class)) {
            return new BigDecimal(value);
        }
        throw new IllegalStateException(String.format(UNSUPPORTED_TYPE, type.getTypeName()));
    }

    /**
     * Parses a set type from the given String value
     *
     * @param type      a type of {@link Set} type
     * @param value     the String representation of the Set elements
     * @param separator the string separator of the elements
     * @return Object of type Set
     * @throws Throwable if the instantiation of the {@link LinkedHashSet} failed for some reason.
     */
    private Object parseSet(Type type, String value, String separator) throws Throwable {
        final var elementType = getGenericType(type);
        return parseSet(elementType, value, separator);
    }

    /**
     * Parses a set type from the given String value
     *
     * @param elementType elementType class
     * @param value       the String representation of the List elements
     * @param separator   the string separator of the elements
     * @return Object of type List
     * @throws Throwable if the instantiation of the {@link ArrayList} failed for some reason.
     */
    @SuppressWarnings("unchecked")
    private <E> Object parseSet(Class<E> elementType, String value, String separator) throws Throwable {
        final var parts = split(value, separator);
        final var set = (List<Object>) LinkedHashSet.class.getDeclaredConstructor().newInstance();
        return parseCollection(elementType, set, parts);
    }

    /**
     * Parses a set type from the given String value
     *
     * @param type      a field of {@link List} type
     * @param value     the String representation of the List elements
     * @param separator the string separator of the elements
     * @return Object of type List
     * @throws Throwable if the instantiation of the {@link ArrayList} failed for some reason.
     */
    private Object parseList(Type type, String value, String separator) throws Throwable {
        final var elementType = getGenericType(type);
        return parseList(elementType, value, separator);
    }


    /**
     * Parses a set type from the given String value
     *
     * @param elementType elementType class
     * @param value       the String representation of the List elements
     * @param separator   the string separator of the elements
     * @return Object of type List
     * @throws Throwable if the instantiation of the {@link ArrayList} failed for some reason.
     */
    @SuppressWarnings("unchecked")
    private <E> Object parseList(Class<E> elementType, String value, String separator) throws Throwable {
        final var parts = split(value, separator);
        final var list = (List<Object>) ArrayList.class.getDeclaredConstructor().newInstance();
        return parseCollection(elementType, list, parts);
    }

    /**
     * Populates the parts to the given collection
     *
     * @param elementType the collection generic type
     * @param collection  a collection instance.
     * @param parts       the string parts represents the collection elements
     * @return the populated collection
     */
    @SuppressWarnings("unchecked")
    private <T> Collection<T> parseCollection(Class<?> elementType, Collection<Object> collection, String[] parts) {
        for (final var part : parts) {
            collection.add(parseValue(elementType, part));
        }
        return (Collection<T>) collection;
    }

    public static Class<?> getGenericType(Type type) {
        if (!(type instanceof ParameterizedType parameterizedType)) {
            return Object.class;
        }

        final var genericTypeArg = parameterizedType.getActualTypeArguments()[0];

        if (!(genericTypeArg instanceof WildcardType wildcardType)) {
            return (Class<?>) genericTypeArg;
        }

        Type[] upperBounds = wildcardType.getUpperBounds();

        if (upperBounds.length != 1) {
            throw new UnsupportedOperationException(WILD_CARD_TYPE_ARE_NOT_SUPPORTED);
        }

        return (Class<?>) upperBounds[0];

    }

    /**
     * Splits the given {@code value} to parts using the {@code separator}.
     *
     * @param value     the string representation
     * @param separator the string elements separator
     * @return the split {@code value} parts
     */
    private String[] split(String value, String separator) {
        if (value == null || value.length() == 0) return new String[0];
        return value.split(separator);
    }

}
