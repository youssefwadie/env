package com.github.youssefwadie.env.exceptions;

public class TypeMismatchException extends IllegalArgumentException {
    private final static String MESSAGE_TEMPLATE = "type %s cannot be assigned to %s";
    public TypeMismatchException() {
        super();
    }

    public TypeMismatchException(Class<?> requiredType, Class<?> actualType) {
        super(MESSAGE_TEMPLATE.formatted(actualType.getName(), requiredType.getName()));
    }

    public TypeMismatchException(String message) {
        super(message);
    }
}
