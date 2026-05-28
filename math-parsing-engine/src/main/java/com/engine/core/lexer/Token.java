package com.engine.core.lexer;

public record Token(TokenType type, String value, int position) {
    @Override
    public String toString() {
        return String.format("Token[%-12s | value: '%s' | pos: %d]", type, value, position);
    }
}