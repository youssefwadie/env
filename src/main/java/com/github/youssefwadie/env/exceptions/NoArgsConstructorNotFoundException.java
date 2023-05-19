package com.github.youssefwadie.env.exceptions;

public class NoArgsConstructorNotFoundException extends ParserException {
    public final static String NO_EMPTY_CONSTRUCTOR_FOUND_MSG = "no empty constructor found";

    public NoArgsConstructorNotFoundException() {
        super(NO_EMPTY_CONSTRUCTOR_FOUND_MSG);
    }

    public NoArgsConstructorNotFoundException(Throwable cause) {
        super(NO_EMPTY_CONSTRUCTOR_FOUND_MSG, cause);
    }
}
