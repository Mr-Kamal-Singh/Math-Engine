package com.engine.core;

import com.engine.core.ast.ExpressionNode;
import com.engine.core.ast.Parser;
import com.engine.core.lexer.Lexer;
import com.engine.core.lexer.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EngineIntegrationTest {

    // Helper method to encapsulate the engine's execution pipeline
    private BigDecimal evaluate(String equation, Map<String, BigDecimal> context) {
        Lexer lexer = new Lexer(equation);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        ExpressionNode syntaxTree = parser.parse();
        return syntaxTree.evaluate(context);
    }

    @Test
    @DisplayName("Verify Basic Arithmetic and Floating Point Precision")
    void testBasicArithmetic() {
        BigDecimal result = evaluate("10.5 + 2.5 * 2", new HashMap<>());
        // Asserts that 2.5 * 2 happens FIRST, resulting in 10.5 + 5 = 15.5
        assertEquals(new BigDecimal("15.5"), result);
    }

    @Test
    @DisplayName("Verify Operator Precedence and Parentheses (PEMDAS)")
    void testOperatorPrecedence() {
        BigDecimal result = evaluate("(10 + 2) * 5 ^ 2", new HashMap<>());
        // Asserts: (12) * 25 = 300
        assertEquals(new BigDecimal("300"), result);
    }

    @Test
    @DisplayName("Verify Dynamic Variable Injection")
    void testVariableInjection() {
        Map<String, BigDecimal> context = new HashMap<>();
        context.put("principal", new BigDecimal("1000"));
        context.put("rate", new BigDecimal("0.05"));

        BigDecimal result = evaluate("principal * rate", context);
        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    @DisplayName("Verify Simpson's 1/3 Rule Calculus Integration")
    void testCalculusIntegration() {
        // Integral of a constant 5 from x=0 to x=10 should be exactly 50
        BigDecimal result = evaluate("integral(5, x, 0, 10)", new HashMap<>());

        // Because of the 10,000 intervals, we strip trailing zeros and check the raw value
        assertEquals(0, new BigDecimal("50").compareTo(result), "Calculus integration drifted from baseline accuracy.");
    }

    @Test
    @DisplayName("Verify System Throws Exception on Missing Variables")
    void testMissingVariableThrowsException() {
        // We attempt to use 'tax_rate' but provide an empty context map
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluate("1000 * tax_rate", new HashMap<>());
        });

        assertEquals("Execution Fault: Variable 'tax_rate' is undefined.", exception.getMessage());
    }
}