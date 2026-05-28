package com.engine.core;

import com.engine.core.ast.ExpressionNode;
import com.engine.core.ast.LaTeXPrintVisitor;
import com.engine.core.ast.NumericalEvaluationVisitor;
import com.engine.core.ast.Parser;
import com.engine.core.lexer.Lexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class CalculatorController {

    @Autowired
    private com.engine.core.CasService casService;

    // ==========================================
    // 1. DATA CONTRACTS (JSON Blueprints)
    // ==========================================
    public record CalculationRequest(String equation, Map<String, BigDecimal> variables) {}
    public record CalculationResponse(String originalEquation, String latexEquation, String result, String computeTimeMs) {}

    public record GraphRequest(String equation, String variable, double min, double max, int points) {}
    public record GraphResponse(List<Double> labels, List<Double> data) {}

    public record QuadraticRequest(double a, double b, double c) {}
    public record QuadraticResponse(String root1, String root2, String discriminant, String latexSteps) {}
    // ==========================================
    // 2. CORE CALCULATION ENDPOINT
    // ==========================================
    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody CalculationRequest request) {
        long startTime = System.nanoTime();

        try {
            String mathEquation = request.equation();
            Map<String, BigDecimal> context = request.variables();
            if (context == null) context = new HashMap<>();

            // Lexical Analysis & Parsing
            Lexer lexer = new Lexer(mathEquation);
            Parser parser = new Parser(lexer.tokenize());
            ExpressionNode syntaxTree = parser.parse();

            // 1. Run the Math Visitor
            NumericalEvaluationVisitor mathVisitor = new NumericalEvaluationVisitor(context);
            BigDecimal result = syntaxTree.accept(mathVisitor);

            // 2. Run the LaTeX Visitor
            LaTeXPrintVisitor latexVisitor = new LaTeXPrintVisitor();
            String latexString = syntaxTree.accept(latexVisitor);

            long endTime = System.nanoTime();
            String computeTimeMs = String.format("%.4f", (endTime - startTime) / 1_000_000.0);



            return ResponseEntity.ok(new CalculationResponse(mathEquation, latexString, result.toPlainString(), computeTimeMs));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==========================================
    // UNIFIED SYMBOLIC CAS ENGINE (All Calculus)
    // ==========================================
    @PostMapping("/symbolic")
    public ResponseEntity<?> solveSymbolic(@RequestBody java.util.Map<String, String> request) {
        long startTime = System.currentTimeMillis();
        try {
            String equation = request.get("equation");
            String variable = request.get("variable");
            String lowerBound = request.get("lowerBound"); // Might be null
            String upperBound = request.get("upperBound"); // Might be null

            if (equation == null || variable == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Execution Fault: Equation and variable are required."));
            }

            // Hand the equation and optional bounds to the Symja CAS Brain
            String rawAnswer = casService.solveCalculus(equation, variable, lowerBound, upperBound);

            long computeTime = System.currentTimeMillis() - startTime;

            // Symja Quirk: If it fails, it usually returns the literal input string back.
            if (rawAnswer == null || rawAnswer.startsWith("Integrate(") || rawAnswer.contains("CAS_ERROR")) {
                throw new Exception("CAS Engine could not find a symbolic integral for this expression. It may diverge.");
            }

            // LaTeX Beautification Engine
            String latexEquation = rawAnswer;

            // Only append '+ C' if it is an indefinite integral
            if (lowerBound == null && upperBound == null) {
                latexEquation += " + C";
            }

            // Convert Symja syntax back to beautiful LaTeX for the UI
            latexEquation = latexEquation.replace("Infinity", "\\infty")
                    .replace("-Infinity", "-\\infty")
                    .replace("Pi", "\\pi")
                    .replace("E", "e");

            return ResponseEntity.ok(java.util.Map.of(
                    "result", rawAnswer,
                    "latexEquation", latexEquation,
                    "computeTimeMs", computeTime
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // ==========================================
    // 3. HIGH-SPEED GRAPHING ENDPOINT
    // ==========================================
    @PostMapping("/graph")
    public ResponseEntity<?> generateGraphData(@RequestBody GraphRequest req) {
        try {
            // Parse the equation ONCE for maximum performance
            Lexer lexer = new Lexer(req.equation());
            Parser parser = new Parser(lexer.tokenize());
            ExpressionNode syntaxTree = parser.parse();

            List<Double> xLabels = new ArrayList<>();
            List<Double> yData = new ArrayList<>();
            Map<String, BigDecimal> context = new HashMap<>();

            double step = (req.max() - req.min()) / (req.points() - 1);

            // High-speed evaluation loop to generate X/Y coordinates
            for (int i = 0; i < req.points(); i++) {
                double currentX = req.min() + (i * step);
                context.put(req.variable(), BigDecimal.valueOf(currentX));

                NumericalEvaluationVisitor visitor = new NumericalEvaluationVisitor(context);
                BigDecimal result = syntaxTree.accept(visitor);

                xLabels.add(Math.round(currentX * 100.0) / 100.0);
                yData.add(result.doubleValue());
            }

            return ResponseEntity.ok(new GraphResponse(xLabels, yData));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==========================================
    // EDUCATIONAL QUADRATIC SOLVER (Aligned LaTeX)
    // ==========================================
    @PostMapping("/quadratic")
    public ResponseEntity<?> solveQuadratic(@RequestBody QuadraticRequest req) {
        try {
            double a = req.a();
            double b = req.b();
            double c = req.c();

            if (a == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "If A is 0, this is a linear equation, not a quadratic."));
            }

            double discriminant = (b * b) - (4 * a * c);
            String root1;
            String root2;

            // 1. Open the Aligned Block
            StringBuilder steps = new StringBuilder();
            steps.append("\\begin{aligned} ");

            // Notice the '&=' which tells LaTeX exactly where to align the vertical stack
            steps.append("x &= \\frac{-(").append(b).append(") \\pm \\sqrt{(").append(b).append(")^2 - 4(").append(a).append(")(").append(c).append(")}}{2(").append(a).append(")} \\\\ ");
            steps.append("\\Delta &= b^2 - 4ac = ").append(String.format("%.4f", discriminant)).append(" \\\\ ");

            if (discriminant > 0) {
                // Case 1: Two Real Roots
                double r1 = (-b + Math.sqrt(discriminant)) / (2 * a);
                double r2 = (-b - Math.sqrt(discriminant)) / (2 * a);
                root1 = String.format("%.4f", r1);
                root2 = String.format("%.4f", r2);
                steps.append("x_1 &= ").append(root1).append(", \\; x_2 = ").append(root2);

            } else if (discriminant == 0) {
                // Case 2: One Real Root (Double Root)
                double r = -b / (2 * a);
                root1 = String.format("%.4f", r);
                root2 = root1;
                steps.append("x &= ").append(root1).append(" \\; \\text{(Double Root)}");

            } else {
                // Case 3: Two Complex/Imaginary Roots
                double realPart = -b / (2 * a);
                double imaginaryPart = Math.sqrt(-discriminant) / (2 * a);

                String realStr = String.format("%.4f", realPart);
                String imagStr = String.format("%.4f", Math.abs(imaginaryPart));

                root1 = realStr + " + " + imagStr + "i";
                root2 = realStr + " - " + imagStr + "i";
                steps.append("x_1 &= ").append(root1).append(", \\; x_2 = ").append(root2).append(" \\; \\text{(Complex Roots)}");
            }

            // 2. Close the Aligned Block
            steps.append(" \\end{aligned}");

            String finalDiscriminantStr = String.format("%.4f", discriminant);

            return ResponseEntity.ok(new QuadraticResponse(root1, root2, finalDiscriminantStr, steps.toString()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}