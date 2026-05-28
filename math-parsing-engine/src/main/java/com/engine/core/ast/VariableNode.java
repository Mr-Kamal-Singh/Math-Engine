package com.engine.core.ast;

public class VariableNode implements ExpressionNode {

    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    // Getter for the Visitor
    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(MathVisitor<T> visitor) {
        return visitor.visit(this);
    }
}