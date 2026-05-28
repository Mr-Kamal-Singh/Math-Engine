package com.engine.core.ast;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

public class NumericalEvaluationVisitor implements MathVisitor<BigDecimal> {

    private final Map<String, BigDecimal> context;
    private final MathContext mc = MathContext.DECIMAL128;

    public NumericalEvaluationVisitor(Map<String, BigDecimal> context) {
        this.context = context;
    }

    @Override
    public BigDecimal visit(NumberNode node) {
        return node.getValue();
    }

    @Override
    public BigDecimal visit(VariableNode node) {
        String name = node.getName();
        if (!context.containsKey(name)) {
            throw new IllegalArgumentException("Execution Fault: Variable '" + name + "' is undefined.");
        }
        return context.get(name);
    }

    @Override
    public BigDecimal visit(BinaryOpNode node) {
        // The visitor recursively visits the left and right children!
        BigDecimal leftVal = node.getLeft().accept(this);
        BigDecimal rightVal = node.getRight().accept(this);

        return switch (node.getOperator().type()) {
            case PLUS -> leftVal.add(rightVal, mc);
            case MINUS -> leftVal.subtract(rightVal, mc);
            case MULTIPLY -> leftVal.multiply(rightVal, mc);
            case DIVIDE -> leftVal.divide(rightVal, mc);
            case POWER -> new BigDecimal(Math.pow(leftVal.doubleValue(), rightVal.doubleValue()), mc);
            default -> throw new UnsupportedOperationException("Unknown operator");
        };
    }

    @Override
    public BigDecimal visit(FunctionNode node) {
        String funcName = node.getName();

        // ==========================================
        // 1. Numerical Integration (Simpson's 1/3 Rule)
        // ==========================================
        if (funcName.equals("integral") && node.getArguments().size() == 4) {
            ExpressionNode expr = node.getArguments().get(0);

            if (!(node.getArguments().get(1) instanceof VariableNode)) {
                throw new IllegalArgumentException("Second argument of integral must be a variable name.");
            }
            String varName = ((VariableNode) node.getArguments().get(1)).getName();

            BigDecimal lowerBound = node.getArguments().get(2).accept(this);
            BigDecimal upperBound = node.getArguments().get(3).accept(this);

            int n = 1000; // Number of slices (must be even for Simpson's Rule)
            BigDecimal h = upperBound.subtract(lowerBound, mc).divide(new BigDecimal(n), mc);
            BigDecimal sum = new BigDecimal("0");

            for (int i = 0; i <= n; i++) {
                // Calculate current X
                BigDecimal x = lowerBound.add(h.multiply(new BigDecimal(i), mc), mc);
                context.put(varName, x);

                // Evaluate f(x)
                BigDecimal y = expr.accept(this);

                // Apply Simpson's multipliers (1, 4, 2, 4, 2... 1)
                if (i == 0 || i == n) {
                    sum = sum.add(y, mc);
                } else if (i % 2 != 0) {
                    sum = sum.add(y.multiply(new BigDecimal("4"), mc), mc);
                } else {
                    sum = sum.add(y.multiply(new BigDecimal("2"), mc), mc);
                }
            }

            // Clean up context
            context.remove(varName);

            // Final calculation: sum * (h / 3)
            return sum.multiply(h, mc).divide(new BigDecimal("3"), mc);
        }

        // ==========================================
        // 2. Numerical Differentiation (Central Difference)
        // ==========================================
        if (funcName.equals("derivative") && node.getArguments().size() == 3) {
            ExpressionNode expr = node.getArguments().get(0);

            if (!(node.getArguments().get(1) instanceof VariableNode)) {
                throw new IllegalArgumentException("Second argument of derivative must be a variable name.");
            }
            String varName = ((VariableNode) node.getArguments().get(1)).getName();

            BigDecimal point = node.getArguments().get(2).accept(this);
            BigDecimal h = new BigDecimal("0.00000001");

            context.put(varName, point.add(h, mc));
            BigDecimal fPlus = expr.accept(this);

            context.put(varName, point.subtract(h, mc));
            BigDecimal fMinus = expr.accept(this);

            context.remove(varName);

            BigDecimal twoH = h.multiply(new BigDecimal("2"), mc);
            return fPlus.subtract(fMinus, mc).divide(twoH, mc);
        }

        // ==========================================
        // Standard Math Functions
        // ==========================================
        if (node.getArguments().size() == 1) {
            BigDecimal arg = node.getArguments().get(0).accept(this);
            double val = arg.doubleValue();

            return switch (funcName) {
                case "sin" -> new BigDecimal(Math.sin(val), mc);
                case "cos" -> new BigDecimal(Math.cos(val), mc);
                case "tan" -> new BigDecimal(Math.tan(val), mc);
                case "log", "ln" -> new BigDecimal(Math.log(val), mc); // Natural log
                case "log10" -> new BigDecimal(Math.log10(val), mc);
                case "sqrt" -> new BigDecimal(Math.sqrt(val), mc);
                case "abs" -> arg.abs(mc);
                default -> new BigDecimal("0"); // Let it fall through if unknown
            };
        }

        // ==========================================
        // 3. Equation Solver (Newton-Raphson Method)
        // ==========================================
        if (funcName.equals("solve")) {
            if (node.getArguments().size() != 3) {
                throw new IllegalArgumentException("Syntax Fault: 'solve' requires 3 arguments (expression, variable, initialGuess).");
            }

            ExpressionNode expr = node.getArguments().get(0);
            if (!(node.getArguments().get(1) instanceof VariableNode)) {
                throw new IllegalArgumentException("Execution Fault: Second argument of solve must be a variable name.");
            }

            String varName = ((VariableNode) node.getArguments().get(1)).getName();
            BigDecimal x_n = node.getArguments().get(2).accept(this); // Initial guess

            BigDecimal tolerance = new BigDecimal("0.00000001");
            BigDecimal h = new BigDecimal("0.00000001");
            int maxIterations = 100;

            for (int i = 0; i < maxIterations; i++) {
                // 1. Calculate f(x_n)
                context.put(varName, x_n);
                BigDecimal fx = expr.accept(this);

                // If we are close enough to 0, we found the root!
                if (fx.abs(mc).compareTo(tolerance) < 0) {
                    context.remove(varName);
                    return x_n;
                }

                // 2. Calculate f'(x_n) using Central Difference
                context.put(varName, x_n.add(h, mc));
                BigDecimal fPlus = expr.accept(this);

                context.put(varName, x_n.subtract(h, mc));
                BigDecimal fMinus = expr.accept(this);

                BigDecimal derivative = fPlus.subtract(fMinus, mc).divide(h.multiply(new BigDecimal("2"), mc), mc);

                if (derivative.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Execution Fault: Derivative became zero; Newton's method failed. Try a different initial guess.");
                }

                // 3. Update x_n using Newton's formula: x = x - f(x)/f'(x)
                x_n = x_n.subtract(fx.divide(derivative, mc), mc);
            }

            context.remove(varName);
            throw new ArithmeticException("Execution Fault: Solver did not converge after " + maxIterations + " iterations.");
        }

        return new BigDecimal("0"); // Fallback
    }
}