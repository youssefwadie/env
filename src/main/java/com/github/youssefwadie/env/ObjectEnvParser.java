package com.github.youssefwadie.env;

import com.github.youssefwadie.env.annotations.Env;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

public class ObjectEnvParser {
    public final static String NO_EMPTY_CONSTRUCTOR_FOUND = "no empty constructor found";
    public static final String ENV_WAS_NOT_FOUND = "%s env was not found";

    private final static Logger log = Logger.getLogger(ObjectEnvParser.class.getName());
    private final Map<String, String> environmentVariables;

    /**
     * Creates an instance with the system environment actual variables
     */
    public ObjectEnvParser() {
        this(System.getenv());
    }

    /**
     * Creates a parser instance with the given environment variables
     *
     * @param environmentVariables must not be null.
     * @throws NullPointerException if the given {@code environmentVariables} is {@code null}.
     */
    protected ObjectEnvParser(Map<String, String> environmentVariables) {
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
            return inject(instance, fieldToEnv);
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
    private <T> T inject(T instance, Map<Field, String> fieldToEnv) {
        EnvParser envParser = new EnvParser(environmentVariables);

        for (var fieldObjectEntry : fieldToEnv.entrySet()) {
            final var field = fieldObjectEntry.getKey();
            field.setAccessible(true);
            try {
                Env env = field.getAnnotation(Env.class);
                field.set(instance, envParser.parse(env, field.getGenericType()));
            } catch (Throwable t) {
                // TODO handle silence fail
                log.severe(t.getMessage());
            }
        }

        return instance;
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
