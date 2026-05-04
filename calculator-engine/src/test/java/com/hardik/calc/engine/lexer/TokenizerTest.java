package com.hardik.calc.engine.lexer;

import com.hardik.calc.engine.error.LexerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    @Test
    @DisplayName("empty input yields only EOF")
    void emptyInput() {
        List<Token> tokens = Tokenizer.tokenize("");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
    }

    @Test
    @DisplayName("whitespace-only input yields only EOF")
    void whitespaceOnly() {
        List<Token> tokens = Tokenizer.tokenize("   \t  \n ");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
    }

    @Test
    @DisplayName("simple addition")
    void simpleAddition() {
        List<Token> tokens = Tokenizer.tokenize("2 + 3");
        assertEquals(4, tokens.size());
        assertToken(tokens.get(0), TokenType.NUMBER, "2", 2.0);
        assertToken(tokens.get(1), TokenType.PLUS, "+", null);
        assertToken(tokens.get(2), TokenType.NUMBER, "3", 3.0);
        assertEquals(TokenType.EOF, tokens.get(3).type());
    }

    @Test
    @DisplayName("decimals including leading-dot form")
    void decimals() {
        List<Token> tokens = Tokenizer.tokenize("3.14 + .5");
        assertToken(tokens.get(0), TokenType.NUMBER, "3.14", 3.14);
        assertToken(tokens.get(2), TokenType.NUMBER, ".5", 0.5);
    }

    @Test
    @DisplayName("scientific notation")
    void scientific() {
        List<Token> tokens = Tokenizer.tokenize("1.5e3 + 2E-2 + 4e+1");
        assertToken(tokens.get(0), TokenType.NUMBER, "1.5e3", 1500.0);
        assertToken(tokens.get(2), TokenType.NUMBER, "2E-2", 0.02);
        assertToken(tokens.get(4), TokenType.NUMBER, "4e+1", 40.0);
    }

    @Test
    @DisplayName("function call shape")
    void functionCallShape() {
        List<Token> tokens = Tokenizer.tokenize("sin(45)");
        assertEquals(TokenType.IDENT,  tokens.get(0).type());
        assertEquals("sin",            tokens.get(0).lexeme());
        assertEquals(TokenType.LPAREN, tokens.get(1).type());
        assertEquals(TokenType.NUMBER, tokens.get(2).type());
        assertEquals(TokenType.RPAREN, tokens.get(3).type());
        assertEquals(TokenType.EOF,    tokens.get(4).type());
    }

    @Test
    @DisplayName("identifiers can contain digits and underscores after the first char")
    void identifierShape() {
        List<Token> tokens = Tokenizer.tokenize("ans2 _x snake_case_var");
        assertEquals("ans2",             tokens.get(0).lexeme());
        assertEquals("_x",               tokens.get(1).lexeme());
        assertEquals("snake_case_var",   tokens.get(2).lexeme());
    }

    @Test
    @DisplayName("all single-character operators and punctuation")
    void allOperators() {
        List<Token> tokens = Tokenizer.tokenize("+-*/^%=(),");
        TokenType[] expected = {
                TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH,
                TokenType.CARET, TokenType.PERCENT, TokenType.EQUALS,
                TokenType.LPAREN, TokenType.RPAREN, TokenType.COMMA, TokenType.EOF
        };
        assertEquals(expected.length, tokens.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tokens.get(i).type(), "at index " + i);
        }
    }

    @Test
    @DisplayName("column tracking points at the start of each token")
    void columnTracking() {
        List<Token> tokens = Tokenizer.tokenize("12 + ab");
        assertEquals(0, tokens.get(0).column());  // "12"
        assertEquals(3, tokens.get(1).column());  // "+"
        assertEquals(5, tokens.get(2).column());  // "ab"
    }

    @ParameterizedTest(name = "input ''{0}'' is rejected")
    @CsvSource({
            "'@'",
            "'#'",
            "'$'",
            "'2 & 3'",
            "'!'"
    })
    void rejectsInvalidCharacters(String input) {
        assertThrows(LexerException.class, () -> Tokenizer.tokenize(input));
    }

    @Test
    @DisplayName("malformed exponent is rejected")
    void malformedExponent() {
        LexerException ex = assertThrows(LexerException.class,
                () -> Tokenizer.tokenize("1e"));
        assertTrue(ex.getMessage().toLowerCase().contains("exponent"),
                "message should mention exponent: " + ex.getMessage());
    }

    @Test
    @DisplayName("error column points at the offending character")
    void errorColumn() {
        LexerException ex = assertThrows(LexerException.class,
                () -> Tokenizer.tokenize("2 + @ 3"));
        assertEquals(4, ex.column());
    }

    private static void assertToken(Token t, TokenType type, String lexeme, Object value) {
        assertEquals(type, t.type(),     "token type");
        assertEquals(lexeme, t.lexeme(), "token lexeme");
        assertEquals(value, t.value(),   "token value");
    }
}
