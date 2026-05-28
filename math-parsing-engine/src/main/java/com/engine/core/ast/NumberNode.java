package com.engine.core.ast;

import java.math.BigDecimal;

public class NumberNode implements ExpressionNode {

    private final BigDecimal value;

    public NumberNode(BigDecimal value) {
        this.value = value;
    }

    // Getter for the Visitor
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public <T> T accept(MathVisitor<T> visitor) {
        return visitor.visit(this);
    }
}