package com.engine.core.ast;

import java.util.List;

public class FunctionNode implements ExpressionNode {

    private final String name;
    private final List<ExpressionNode> arguments;

    public FunctionNode(String name, List<ExpressionNode> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    // Getters for the Visitor
    public String getName() {
        return name;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(MathVisitor<T> visitor) {
        return visitor.visit(this);
    }
}