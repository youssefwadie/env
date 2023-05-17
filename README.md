A simple library to parse environment variables into objects.



The usage looks like this:

```java
import com.github.youssefwadie.env.EnvParser;
import com.github.youssefwadie.env.annotations.Env;

import java.util.List;

public class Runner {
    public static class AppConfig {
        @Env("SERVER_PORT")
        private Integer port;

        @Env("DB_USERNAME")
        private String dbUsername;

        @Env("DB_PASSWORD")
        private String dbPassword;

        @Env(value = "ALLOWED_ORIGINS", separator = ",")
        private List<String> allowedOrigins;

        // getters and setters
    }

    public static void main(String[] args) {
        final EnvParser parser = new EnvParser();
        // create an instance of the target class (must have an empty constructor)
        final AppConfig appConfig1 = parser.parse(AppConfig.class);
        System.out.printf("%s%n", appConfig1);

        // populate the env variables to the annotated fields' instance
        final AppConfig appConfig2 = new AppConfig();
        parser.parse(appConfig2);
        System.out.printf("%s%n", appConfig2);
    }

}
```

### Supported types
- All primitive types and their respective wrappers
- String
- Number is mapped to BigDecimal
- Object is parsed as a String type
- List and Set types, with a one of the above types.
