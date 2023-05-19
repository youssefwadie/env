package com.github.youssefwadie.env;

import com.github.youssefwadie.env.annotations.Env;
import com.github.youssefwadie.env.exceptions.NoArgsConstructorNotFoundException;
import com.github.youssefwadie.env.exceptions.ParserException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ObjectEnvParser {

    private final static Logger log = Logger.getLogger(ObjectEnvParser.class.getName());
    private final EnvParser envParser;
    private final boolean silentFail;

    /**
     * Creates an instance with the system environment actual variables
     */
    public ObjectEnvParser() {
        this(true);
    }

    /**
     * Creates an instance with the system environment actual variables.
     *
     * @param silentFail Indicates whether to silently fail or raise an exception when an environment variable is not found.
     */
    public ObjectEnvParser(boolean silentFail) {
        this(System.getenv(), silentFail);
    }

    /**
     * Creates a parser instance with the given environment variables
     *
     * @param environmentVariables must not be null.
     * @throws IllegalArgumentException if the given {@code environmentVariables} is empty or {@code null}.
     */
    protected ObjectEnvParser(Map<String, String> environmentVariables, boolean silentFail) {
        Assert.notEmpty(environmentVariables, "environmentVariables must not be null");
        this.envParser = new EnvParser(environmentVariables);
        this.silentFail = silentFail;
    }

    /**
     * Parses all the annotated fields in the given instance
     *
     * @param instance must not be {@literal null}.
     * @return the parsed instance
     * @throws IllegalArgumentException if the given clazz is null.
     *                                  <h3>all the thrown exceptions are wrapped in a {@link ParserException}</h3>
     */
    public <T> T parse(T instance) {
        Assert.notNull(instance, "instance cannot be null");
        List<Field> annotatedFields = getAnnotatedFields(instance);
        try {
            return inject(instance, annotatedFields);
        } catch (Throwable t) {
            throw new ParserException(t);
        }
    }


    /**
     * Creates an instance of the given class
     *
     * @param clazz the class type, must not be {@literal null}.
     * @return the instantiated instance
     * @throws IllegalArgumentException           if the given clazz is null.
     * @throws NoArgsConstructorNotFoundException if the given clazz has no default (empty) constructor.
     *                                            <h3>all the thrown exceptions are wrapped in a {@link ParserException}</h3>
     */
    public <T> T parse(Class<T> clazz) {
        Assert.notNull(clazz, "clazz cannot be null");
        try {
            final var emptyConstructor = getEmptyConstructor(clazz);
            final T instance = emptyConstructor.newInstance();
            return parse(instance);
        } catch (Throwable t) {
            throw new ParserException(t);
        }
    }

    /**
     * Returns the empty constructor of the given clazz
     *
     * @param clazz the class type
     * @return the empty constructor
     * @throws NoArgsConstructorNotFoundException if no empty constructor was found.
     */
    private <T> Constructor<T> getEmptyConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            throw new NoArgsConstructorNotFoundException(ex);
        }
    }

    /**
     * Populates the given instance with the values in the list of fields.
     *
     * @param instance the object instance.
     * @param fields   fields annotated with {@link Env}.
     * @return the populated instance.
     */
    private <T> T inject(T instance, List<Field> fields) {
        for (var field : fields) {
            field.setAccessible(true);
            try {
                Env env = field.getAnnotation(Env.class);
                field.set(instance, envParser.parse(env, field.getGenericType()));
            } catch (Throwable t) {
                log.severe(t.getMessage());
                if (!silentFail) {
                    throw new ParserException(t);
                }
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
