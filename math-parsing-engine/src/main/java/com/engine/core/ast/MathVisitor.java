package com.engine.core.ast;

public interface MathVisitor<T> {
    // The visitor must know how to handle every type of node in your engine
    T visit(NumberNode node);
    T visit(VariableNode node);
    T visit(BinaryOpNode node);
    T visit(FunctionNode node);
}