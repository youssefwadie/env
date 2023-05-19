package com.github.youssefwadie.env;

import com.github.youssefwadie.env.annotations.Env;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EnvProxyFactory {
    private final EnvParser envParser;

    protected EnvProxyFactory(Map<String, String> environmentVariables) {
        this.envParser = new EnvParser(environmentVariables);
    }

    public EnvProxyFactory() {
        this.envParser = new EnvParser();
    }


    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceClass) {
        Assert.notNull(interfaceClass, "interfaceClass must not be null");

        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("interfaceClass must be an interface class");
        }
        Map<String, Object> metaData = collectMetaData(interfaceClass);
        InvocationHandler envValueInvocationHandler = new EnvValueInvocationHandler(metaData);
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                envValueInvocationHandler);
    }

    private Map<String, Object> collectMetaData(Class<?> interfaceClass) {
        Method[] methods = interfaceClass.getMethods();
        final Map<String, Object> methodNameToEnvValue = new HashMap<>();
        for (Method method : methods) {
            Env env = method.getAnnotation(Env.class);
            if (env == null) {
                throw new UnsupportedOperationException(String.format("Method [%s] must be annotated with @Env", method.getName()));
            } else if (method.getParameterCount() != 0) {
                throw new UnsupportedOperationException(String.format("Method [%s] must take no args", method.getName()));
            }
            methodNameToEnvValue.put(method.getName(), envParser.parse(env, method.getGenericReturnType()));
        }

        return Collections.unmodifiableMap(methodNameToEnvValue);
    }

    private static class EnvValueInvocationHandler implements InvocationHandler {
        private final Map<String, Object> methodNameToEnvValue;

        private EnvValueInvocationHandler(Map<String, Object> methodNameToEnvValue) {
            this.methodNameToEnvValue = methodNameToEnvValue;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return methodNameToEnvValue.get(method.getName());
        }
    }
}
