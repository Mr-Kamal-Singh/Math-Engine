package com.engine.core.lexer;

public enum TokenType {
    // Literals and Variables
    NUMBER, IDENTIFIER,

    // Operators
    PLUS, MINUS, MULTIPLY, DIVIDE, POWER,

    // Delimiters
    LEFT_PAREN, RIGHT_PAREN, COMMA,

    // Special Markers
    EOF
}