package com.github.youssefwadie.env;

import com.github.youssefwadie.env.annotations.Env;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public class EnvParser {
    public final static String NO_EMPTY_CONSTRUCTOR_FOUND = "no empty constructor found";
    public static final String WILD_CARD_TYPE_ARE_NOT_SUPPORTED = "wild card type are not supported";
    public static final String UNSUPPORTED_TYPE = "Type : %s unsupported";
    public static final String ENV_WAS_NOT_FOUND = "%s env was not found";

    private final static Logger log = Logger.getLogger(EnvParser.class.getName());
    private final Map<String, String> environmentVariables;

    /**
     * Creates an instance with the system environment actual variables
     */
    public EnvParser() {
        this(System.getenv());
    }

    /**
     * Creates a parser instance with the given environment variables
     * @param environmentVariables must not be null.
     * @throws NullPointerException if the given {@code environmentVariables} is {@code null}.
     */
    protected EnvParser(Map<String, String> environmentVariables) {
        Objects.requireNonNull(environmentVariables, "environmentVariables must not be null");
        this.environmentVariables = environmentVariables;
    }

    /**
     * Parses all the annotated fields in the given instance
     *
     * @param instance must not be {@literal null}.
     * @return the parsed instance
     * @throws NullPointerException          if the given clazz is null.
     * @throws UnsupportedOperationException if the given instance in a private module.
     * @throws InaccessibleObjectException   if the instance has an annotated field with wild card generic type.
     *                                       <h3>all the thrown exceptions are wrapped in a {@link RuntimeException}</h3>
     */
    public <T> T parse(T instance) {
        Objects.requireNonNull(instance, "instance cannot be null");
        List<Field> annotatedFields = getAnnotatedFields(instance);
        Map<Field, String> fieldToEnv = getFieldToEnv(annotatedFields);
        try {
            return populate(instance, fieldToEnv);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    /**
     * Creates an instance of the given class
     *
     * @param clazz the class type, must not be {@literal null}.
     * @return the instantiated instance
     * @throws NullPointerException          if the given clazz is null.
     * @throws IllegalStateException         if the given clazz has no default (empty) constructor.
     * @throws UnsupportedOperationException if the given instance in a private module.
     * @throws InaccessibleObjectException   if the instance has an annotated field with wild card generic type.
     *                                       <h3>all the thrown exceptions are wrapped in a {@link RuntimeException}</h3>
     */
    public <T> T parse(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz cannot be null");
        try {
            final var emptyConstructor = getEmptyConstructor(clazz);
            if (emptyConstructor == null) {
                throw new IllegalStateException(NO_EMPTY_CONSTRUCTOR_FOUND);
            }
            final T instance = emptyConstructor.newInstance();
            return parse(instance);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Creates a {@link Map} with all the fields as its keys, and the {@link Env#value()} as its value
     *
     * @param annotatedFields a list of the annotated fields
     * @return the constructed {@link Map}.
     */
    private Map<Field, String> getFieldToEnv(final List<Field> annotatedFields) {
        Map<Field, String> fieldToEnv = new HashMap<>();
        for (Field annotatedField : annotatedFields) {
            Env annotation = annotatedField.getAnnotation(Env.class);
            String declaredEnvValue = annotation.value();
            String env = environmentVariables.get(declaredEnvValue);
            if (env == null) {
                log.warning(String.format(ENV_WAS_NOT_FOUND, declaredEnvValue));
            }
            fieldToEnv.put(annotatedField, env);
        }
        return fieldToEnv;
    }

    /**
     * Returns the empty constructor of the given clazz
     *
     * @param clazz the class type
     * @return the empty constructor if found, {@literal null} if not found.
     */
    private <T> Constructor<T> getEmptyConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Populates the given instance with the values in the fieldToEnv map.
     *
     * @param instance   the object instance
     * @param fieldToEnv mapping from field to {@link Env#value()}
     * @return the populated instance.
     */
    private <T> T populate(T instance, Map<Field, String> fieldToEnv) {
        for (var fieldObjectEntry : fieldToEnv.entrySet()) {
            final var field = fieldObjectEntry.getKey();
            field.setAccessible(true);
            try {
                final var separator = field.getAnnotation(Env.class).separator();
                if (field.getType().equals(List.class)) {
                    field.set(instance, parseList(field, fieldObjectEntry.getValue(), separator));
                } else if (field.getType().equals(Set.class)) {
                    field.set(instance, parseSet(field, fieldObjectEntry.getValue(), separator));
                } else {
                    field.set(instance, parseValue(field.getType(), fieldObjectEntry.getValue()));
                }
            } catch (Throwable t) {
                // TODO handle silence fail
                log.severe(t.getMessage());
            }
        }

        return instance;
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
        }
        throw new IllegalStateException(String.format(UNSUPPORTED_TYPE, type.getTypeName()));
    }

    /**
     * Parses a set type from the given String value
     *
     * @param field     a field of {@link Set} type
     * @param value     the String representation of the Set elements
     * @param separator the string separator of the elements
     * @return Object of type Set
     * @throws Throwable if the instantiation of the {@link LinkedHashSet} failed for some reason.
     */
    @SuppressWarnings("unchecked")
    private Object parseSet(Field field, String value, String separator) throws Throwable {
        final String[] parts = split(value, separator);
        final var elementType = getGenericType(field);

        final var objects = (Set<Object>) LinkedHashSet.class.getDeclaredConstructor().newInstance();
        return parseCollection(elementType, objects, parts);
    }

    /**
     * Parses a set type from the given String value
     *
     * @param field     a field of {@link List} type
     * @param value     the String representation of the List elements
     * @param separator the string separator of the elements
     * @return Object of type List
     * @throws Throwable if the instantiation of the {@link ArrayList} failed for some reason.
     */
    @SuppressWarnings("unchecked")
    private Object parseList(Field field, String value, String separator) throws Throwable {
        final var parts = split(value, separator);
        final var elementType = getGenericType(field);

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
    private Object parseCollection(Class<?> elementType, Collection<Object> collection, String[] parts) {
        for (final var part : parts) {
            collection.add(parseValue(elementType, part));
        }
        return collection;
    }

    /**
     * Gets the generic type of parameterized (generic) field
     *
     * @param field the parametrized field.
     * @return the class of the genericType.
     */
    private static Class<?> getGenericType(Field field) {
        try {
            final var genericType = (ParameterizedType) field.getGenericType();
            final var genericTypeArg = genericType.getActualTypeArguments()[0];
            if (genericTypeArg instanceof WildcardType) {
                throw new UnsupportedOperationException(WILD_CARD_TYPE_ARE_NOT_SUPPORTED);
            }
            return (Class<?>) genericTypeArg;
        } catch (ClassCastException e) {
            log.warning(String.format("%s Raw use of parameterized class '%s'", field.getName(), field.getType().getSimpleName()));
            return Object.class;
        }
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

    /**
     * Gets the annotated fields with {@link Env} of the given instance
     *
     * @param instance not-null instance
     * @return a list of all annotated fields
     */
    private List<Field> getAnnotatedFields(Object instance) {
        return Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Env.class))
                .toList();
    }
}
