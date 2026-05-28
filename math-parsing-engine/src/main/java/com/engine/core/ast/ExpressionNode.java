package com.engine.core.ast;

public interface ExpressionNode {
    // Nodes accept a visitor and pass themselves to it
    <T> T accept(MathVisitor<T> visitor);
}