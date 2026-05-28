package com.engine.core.ast;

import com.engine.core.lexer.Token;

public class BinaryOpNode implements ExpressionNode {
    private final ExpressionNode left;
    private final Token operator;
    private final ExpressionNode right;

    public BinaryOpNode(ExpressionNode left, Token operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    // Getters so the Visitor can see the data
    public ExpressionNode getLeft() { return left; }
    public Token getOperator() { return operator; }
    public ExpressionNode getRight() { return right; }

    // The single, universal accept method
    @Override
    public <T> T accept(MathVisitor<T> visitor) {
        return visitor.visit(this);
    }
}