A simple library to parse environment variables into objects.



The usage looks like this:

```java
package com.github.youssefwadie.env;

import com.github.youssefwadie.env.annotations.Env;

import java.util.List;

public class ApplicationRunner {
    public static class AppConfig {
        @Env("SERVER_PORT")
        private Integer port;

        @Env(value = "ALLOWED_ORIGINS", separator = ",")
        private List<String> allowedOrigins;

        // getters and setters
    }

    public interface MySQLProperties {
        @Env("DB_USERNAME")
        String getUsername();

        @Env("DB_PASSWORD")
        String getPassword();
    }

    public static void main(String[] args) {
        // Create object parser
        final ObjectEnvParser parser = new ObjectEnvParser();
        // create an instance of the target class (must have an empty constructor)
        final AppConfig appConfig = parser.parse(AppConfig.class);
        System.out.printf("%s%n", appConfig);


        // create proxy factory
        final EnvProxyFactory proxyFactory = new EnvProxyFactory();
        final MySQLProperties mySqlProperties = proxyFactory.createProxy(MySQLProperties.class);

        // access the properties values
        System.out.println(mySqlProperties.getUsername());
        System.out.println(mySqlProperties.getPassword());
    }
}

```

### Supported types
- All primitive types and their respective wrappers
- String
- java.lang.Number is parsed as java.math.BigDecimal
- java.lang.Object is parsed as a java.lang.String type
- List and Set types, with a one of the above types.
