package com.hardik.calc.engine.parser;

import com.hardik.calc.engine.ast.AstPrinter;
import com.hardik.calc.engine.ast.Expr;
import com.hardik.calc.engine.error.ParserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;


//  Tests assert on the parenthesized prefix form produced AstPrinter.

class ParserTest {

    private static String parse(String src) {
        return AstPrinter.print(Parser.parse(src));
    }

    // --- precedence ----

    @ParameterizedTest(name = "{0}  =>  {1}")
    @CsvSource(delimiter = '|', value = {
            "1                |  1",
            "1 + 2            |  (+ 1 2)",
            "1 + 2 + 3        |  (+ (+ 1 2) 3)",          // left-associative
            "1 - 2 - 3        |  (- (- 1 2) 3)",          // left-associative
            "1 + 2 * 3        |  (+ 1 (* 2 3))",          // * binds tighter
            "1 * 2 + 3        |  (+ (* 1 2) 3)",
            "1 + 2 * 3 - 4    |  (- (+ 1 (* 2 3)) 4)",
            "(1 + 2) * 3      |  (* (+ 1 2) 3)",          
            "10 % 3           |  (% 10 3)",
            "8 / 2 / 2        |  (/ (/ 8 2) 2)",
    })
    @DisplayName("operator precedence and left-associativity")
    void precedence(String input, String expected) {
        assertEquals(expected, parse(input));
    }

    // --- power: right-associative, binds tighter than unary minus ---------

    @Test
    @DisplayName("^ is right-associative: 2^3^4 = 2^(3^4)")
    void powerRightAssociative() {
        assertEquals("(^ 2 (^ 3 4))", parse("2 ^ 3 ^ 4"));
    }

    @Test
    @DisplayName("** is a synonym for ^")
    void starStarSameAsCaret() {
        assertEquals(parse("2 ^ 3"), parse("2 ** 3"));
    }

    @Test
    @DisplayName("-2^2 parses as -(2^2), not (-2)^2")
    void unaryMinusBindsLooserThanPower() {
        assertEquals("(neg (^ 2 2))", parse("-2^2"));
    }

    // --- unary --

    @Test
    @DisplayName("unary minus stacks")
    void doubleNegation() {
        assertEquals("(neg (neg 5))", parse("--5"));
    }

    @Test
    @DisplayName("unary minus applies to whole right-hand side")
    void unaryAcrossExpression() {
        assertEquals("(+ 1 (neg 2))", parse("1 + -2"));
    }

    @Test
    @DisplayName("unary plus is preserved (POS), not silently dropped")
    void unaryPlus() {
        assertEquals("(pos 5)", parse("+5"));
    }

    // --- functions and variables section ---

    @Test
    @DisplayName("zero-argument function call")
    void zeroArgFunction() {
        assertEquals("(rand)", parse("rand()"));
    }

    @Test
    @DisplayName("one-argument function call")
    void oneArgFunction() {
        assertEquals("(sin 45)", parse("sin(45)"));
    }

    @Test
    @DisplayName("multi-argument function call")
    void multiArgFunction() {
        assertEquals("(max 1 2 3)", parse("max(1, 2, 3)"));
    }

    @Test
    @DisplayName("function call with expression arguments")
    void functionWithExpressions() {
        assertEquals("(min (+ 1 2) (* 3 4))", parse("min(1 + 2, 3 * 4)"));
    }

    @Test
    @DisplayName("nested function calls")
    void nestedFunctions() {
        assertEquals("(sin (cos 0))", parse("sin(cos(0))"));
    }

    @Test
    @DisplayName("identifier without parens is a Variable, not a no-arg call")
    void identifierIsVariable() {
        assertEquals("pi", parse("pi"));
    }

    // --- assignment --

    @Test
    @DisplayName("simple assignment")
    void simpleAssignment() {
        assertEquals("(= x 3.14)", parse("x = 3.14"));
    }

    @Test
    @DisplayName("assignment captures the whole right-hand expression")
    void assignmentExpression() {
        assertEquals("(= y (+ 1 (* 2 3)))", parse("y = 1 + 2 * 3"));
    }

    // --- structural sanity - instance-level checks , including strings

    @Test
    @DisplayName("BinaryOp carries the left operand's column for diagnostics")
    void binaryOpColumnComesFromLeft() {
        Expr e = Parser.parse("123 + 4");
        assertEquals(0, e.column());
    }

    @Test
    @DisplayName("FunctionCall carries the identifier's column")
    void functionCallColumn() {
        Expr e = Parser.parse("   sin(45)");
        assertEquals(3, e.column());
    }

    // --- error cases -- TODO: need improvements

    @Test
    @DisplayName("trailing operator is rejected")
    void trailingOperator() {
        ParserException ex = assertThrows(ParserException.class, () -> Parser.parse("2 +"));
        assertTrue(ex.getMessage().toLowerCase().contains("expected"));
    }

    @Test
    @DisplayName("missing right paren is rejected")
    void unclosedParen() {
        ParserException ex = assertThrows(ParserException.class, () -> Parser.parse("(1 + 2"));
        assertTrue(ex.getMessage().contains("')'"));
    }

    @Test
    @DisplayName("stray right paren is rejected")
    void strayRightParen() {
        assertThrows(ParserException.class, () -> Parser.parse("1 + 2)"));
    }

    @Test
    @DisplayName("two operators in a row is rejected (the second one isn't unary-eligible)")
    void doubleOperator() {
        // 2 * * 3 is fixed here,  
        assertThrows(ParserException.class, () -> Parser.parse("2 * * 3"));
    }

    @Test
    @DisplayName("empty input is rejected")
    void emptyInput() {
        assertThrows(ParserException.class, () -> Parser.parse(""));
    }

    @Test
    @DisplayName("missing comma between args is rejected")
    void missingComma() {
        assertThrows(ParserException.class, () -> Parser.parse("max(1 2)"));
    }

    @Test
    @DisplayName("trailing comma in args is rejected")
    void trailingComma() {
        assertThrows(ParserException.class, () -> Parser.parse("max(1, 2,)"));
    }

    @Test
    @DisplayName("error column points at the offending token, not the start of input")
    void errorColumnPrecision() {
        ParserException ex = assertThrows(ParserException.class, () -> Parser.parse("1 + + )"));
        assertEquals(6, ex.column());
    }
}
