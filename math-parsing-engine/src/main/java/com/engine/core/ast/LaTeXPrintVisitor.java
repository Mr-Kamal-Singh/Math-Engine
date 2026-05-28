package com.engine.core.ast;

public class LaTeXPrintVisitor implements MathVisitor<String> {

    @Override
    public String visit(NumberNode node) {
        return node.getValue().toPlainString();
    }

    @Override
    public String visit(VariableNode node) {
        // Formats variables with underscores (like tax_rate) as subscripts: tax_{rate}
        String name = node.getName();
        if (name.contains("_")) {
            String[] parts = name.split("_", 2);
            return parts[0] + "_{" + parts[1] + "}";
        }
        return name;
    }

    @Override
    public String visit(BinaryOpNode node) {
        String left = node.getLeft().accept(this);
        String right = node.getRight().accept(this);

        return switch (node.getOperator().type()) {
            case PLUS -> left + " + " + right;
            case MINUS -> left + " - " + right;
            case MULTIPLY -> left + " \\cdot " + right; // Center dot for multiplication
            case DIVIDE -> "\\frac{" + left + "}{" + right + "}"; // Vertical fraction
            case POWER -> left + "^{" + right + "}"; // Superscript exponent
            default -> left + " ? " + right;
        };
    }

    @Override
    public String visit(FunctionNode node) {
        String funcName = node.getName();

        // 1. Integral Formatting
        if (funcName.equals("integral") && node.getArguments().size() == 4) {
            String expr = node.getArguments().get(0).accept(this);
            String var = node.getArguments().get(1).accept(this);
            String lower = node.getArguments().get(2).accept(this);
            String upper = node.getArguments().get(3).accept(this);
            return "\\int_{" + lower + "}^{" + upper + "} " + expr + " \\,d" + var;
        }

        // 2. Derivative Formatting (The Leibniz notation you just built!)
        if (funcName.equals("derivative") && node.getArguments().size() == 3) {
            String expr = node.getArguments().get(0).accept(this);
            String var = node.getArguments().get(1).accept(this);
            String point = node.getArguments().get(2).accept(this);
            return "\\left. \\frac{d}{d" + var + "} (" + expr + ") \\right|_{" + var + "=" + point + "}";
        }
        // 3. Solver Formatting
        if (funcName.equals("solve") && node.getArguments().size() == 3) {
            String expr = node.getArguments().get(0).accept(this);
            String var = node.getArguments().get(1).accept(this);
            String guess = node.getArguments().get(2).accept(this);

            // Returns formatted text: Solve expr = 0 (near x = guess)
            return "\\text{Root of } \\left[" + expr + " = 0\\right] \\; \\text{near } " + var + "_{0}=" + guess;
        }

        // Standard function formatting (e.g., sin(x))
        StringBuilder sb = new StringBuilder("\\" + funcName + "(");
        for (int i = 0; i < node.getArguments().size(); i++) {
            sb.append(node.getArguments().get(i).accept(this));
            if (i < node.getArguments().size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}