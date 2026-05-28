package com.engine.core;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.stereotype.Service;

@Service
public class CasService {

    /**
     * Unified Calculus Engine: Solves Indefinite, Definite, and Improper Integrals.
     */
    public String solveCalculus(String equation, String variable, String lowerBound, String upperBound) {
        try {
            ExprEvaluator util = new ExprEvaluator();
            String casCommand;

            // CASE 1: Definite or Improper Integral (Has Bounds)
            if (lowerBound != null && upperBound != null) {
                // Translate LaTeX infinity into Symja's algebraic Infinity
                String cleanLower = lowerBound.replace("\\infty", "Infinity").replace("infty", "Infinity");
                String cleanUpper = upperBound.replace("\\infty", "Infinity").replace("infty", "Infinity");

                // Command: Integrate(x^2, {x, 0, Infinity})
                casCommand = "Integrate(" + equation + ", {" + variable + ", " + cleanLower + ", " + cleanUpper + "})";
            }
            // CASE 2: Indefinite Integral (No Bounds)
            else {
                // Command: Integrate(x^2, x)
                casCommand = "Integrate(" + equation + ", " + variable + ")";
            }

            // Evaluate the exact algebraic result
            IExpr result = util.evaluate(casCommand);
            return result.toString();

        } catch (Exception e) {
            return "CAS_ERROR: " + e.getMessage();
        }
    }
}