package com.engine.core.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int position = 0;

    public Lexer(String input) {
        this.input = input != null ? input : "";
    }

    private char peek() {
        if (position >= input.length()) return '\0';
        return input.charAt(position);
    }

    private char advance() {
        if (position >= input.length()) return '\0';
        return input.charAt(position++);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = peek();

            if (Character.isWhitespace(current)) {
                advance();
                continue;
            }

            if (Character.isDigit(current) || current == '.') {
                tokens.add(readNumberToken());
                continue;
            }

            if (Character.isLetter(current) || current == '_') {
                tokens.add(readIdentifierToken());
                continue;
            }

            int startPos = position;
            switch (advance()) {
                case '+' -> tokens.add(new Token(TokenType.PLUS, "+", startPos));
                case '-' -> tokens.add(new Token(TokenType.MINUS, "-", startPos));
                case '*' -> tokens.add(new Token(TokenType.MULTIPLY, "*", startPos));
                case '/' -> tokens.add(new Token(TokenType.DIVIDE, "/", startPos));
                case '^' -> tokens.add(new Token(TokenType.POWER, "^", startPos));
                case '(' -> tokens.add(new Token(TokenType.LEFT_PAREN, "(", startPos));
                case ')' -> tokens.add(new Token(TokenType.RIGHT_PAREN, ")", startPos));
                case ',' -> tokens.add(new Token(TokenType.COMMA, ",", startPos));
                default -> throw new IllegalArgumentException(
                        "Lexical Fault: Unrecognized character '" + current + "' at index " + startPos
                );
            }
        }

        tokens.add(new Token(TokenType.EOF, "", position));
        return tokens;
    }

    private Token readNumberToken() {
        int start = position;
        boolean hasDecimal = false;

        while (Character.isDigit(peek()) || peek() == '.') {
            if (peek() == '.') {
                if (hasDecimal) {
                    throw new IllegalArgumentException("Lexical Fault: Malformed number with multiple decimals at index " + position);
                }
                hasDecimal = true;
            }
            advance();
        }
        return new Token(TokenType.NUMBER, input.substring(start, position), start);
    }

    private Token readIdentifierToken() {
        int start = position;
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            advance();
        }
        return new Token(TokenType.IDENTIFIER, input.substring(start, position), start);
    }
}