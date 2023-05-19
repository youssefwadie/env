package com.github.youssefwadie.env.exceptions;

public class ParserException extends RuntimeException {
    public ParserException() {
        super();
    }
    public ParserException(String message) {
        super(message);
    }
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }
}
