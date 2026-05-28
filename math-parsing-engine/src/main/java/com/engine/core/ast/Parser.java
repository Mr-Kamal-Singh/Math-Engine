package com.engine.core.ast;

import com.engine.core.lexer.Token;
import com.engine.core.lexer.TokenType;
import java.math.BigDecimal;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        if (current >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(current);
    }

    private Token advance() {
        if (current < tokens.size()) current++;
        return tokens.get(current - 1);
    }

    private boolean match(TokenType type) {
        if (peek().type() == type) {
            advance();
            return true;
        }
        return false;
    }

    public ExpressionNode parse() {
        return parseExpression();
    }

    // Handles + and -
    private ExpressionNode parseExpression() {
        ExpressionNode left = parseTerm();
        while (peek().type() == TokenType.PLUS || peek().type() == TokenType.MINUS) {
            Token operator = advance();
            ExpressionNode right = parseTerm();
            left = new BinaryOpNode(left, operator, right);
        }
        return left;
    }

    // Handles * and /
    private ExpressionNode parseTerm() {
        ExpressionNode left = parseFactor();
        while (peek().type() == TokenType.MULTIPLY || peek().type() == TokenType.DIVIDE) {
            Token operator = advance();
            ExpressionNode right = parseFactor();
            left = new BinaryOpNode(left, operator, right);
        }
        return left;
    }

    // Handles ^ (Powers)
    private ExpressionNode parseFactor() {
        ExpressionNode left = parsePrimary();
        while (peek().type() == TokenType.POWER) {
            Token operator = advance();
            ExpressionNode right = parsePrimary();
            left = new BinaryOpNode(left, operator, right);
        }
        return left;
    }

    // Handles raw numbers, variables, functions, and parentheses
    private ExpressionNode parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new NumberNode(new BigDecimal(tokens.get(current - 1).value()));
        }

        if (match(TokenType.IDENTIFIER)) {
            String identifierName = tokens.get(current - 1).value();

            // Look ahead: is this a function call or just a variable?
            if (match(TokenType.LEFT_PAREN)) {
                List<ExpressionNode> args = parseArguments();
                if (!match(TokenType.RIGHT_PAREN)) {
                    throw new RuntimeException("Syntax Error: Missing closing parenthesis ')' for function " + identifierName);
                }
                return new FunctionNode(identifierName, args);
            }

            return new VariableNode(identifierName);
        }

        if (match(TokenType.LEFT_PAREN)) {
            ExpressionNode expr = parseExpression();
            if (!match(TokenType.RIGHT_PAREN)) {
                throw new RuntimeException("Syntax Error: Missing closing parenthesis ')'");
            }
            return expr;
        }
        throw new RuntimeException("Syntax Error: Unexpected token " + peek().type() + " at position " + peek().position());
    }

    // Helper to parse comma-separated arguments
    private java.util.List<ExpressionNode> parseArguments() {
        java.util.List<ExpressionNode> args = new java.util.ArrayList<>();
        if (peek().type() != TokenType.RIGHT_PAREN) {
            do {
                args.add(parseExpression());
            } while (match(TokenType.COMMA));
        }
        return args;
    }
}