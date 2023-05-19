package com.github.youssefwadie.env.exceptions;

public class UnsupportedTypeException extends ParserException {
    public static final String UNSUPPORTED_TYPE_MSG_TEMPLATE = "Type : %s unsupported";

    public UnsupportedTypeException(String typeName) {
        super(UNSUPPORTED_TYPE_MSG_TEMPLATE.formatted(typeName));
    }
}
