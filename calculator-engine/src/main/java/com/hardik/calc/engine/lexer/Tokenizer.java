package com.hardik.calc.engine.lexer;

import com.hardik.calc.engine.error.LexerException;

import java.util.ArrayList;
import java.util.List;

public final class Tokenizer {

    private final String source;
    private int pos;

    public Tokenizer(String source) {
        if (source == null) throw new IllegalArgumentException("source must not be null");
        this.source = source;
        this.pos = 0;
    }

    // Tokenize an entire input in one call. ends with an EOF token.
    public static List<Token> tokenize(String source) {
        return new Tokenizer(source).scanAll();
    }

    public List<Token> scanAll() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            Token t = next();
            tokens.add(t);
            if (t.type() == TokenType.EOF) return tokens;
        }
    }

    // --- core scan loop

    private Token next() {
        skipWhitespace();
        if (isAtEnd()) return new Token(TokenType.EOF, "", null, pos);

        int startCol = pos;
        char c = peek();

        
        if (isDigit(c) || (c == '.' && pos + 1 < source.length() && isDigit(source.charAt(pos + 1)))) {
            return number(startCol);
        }
        if (isIdentStart(c)) {
            return identifier(startCol);
        }

        advance();
        return switch (c) {
            case '+' -> new Token(TokenType.PLUS,    "+", null, startCol);
            case '-' -> new Token(TokenType.MINUS,   "-", null, startCol);
            case '*' -> new Token(TokenType.STAR,    "*", null, startCol);
            case '/' -> new Token(TokenType.SLASH,   "/", null, startCol);
            case '%' -> new Token(TokenType.PERCENT, "%", null, startCol);
            case '^' -> new Token(TokenType.CARET,   "^", null, startCol);
            case '=' -> new Token(TokenType.EQUALS,  "=", null, startCol);
            case '(' -> new Token(TokenType.LPAREN,  "(", null, startCol);
            case ')' -> new Token(TokenType.RPAREN,  ")", null, startCol);
            case ',' -> new Token(TokenType.COMMA,   ",", null, startCol);
            default  -> throw new LexerException(
                    "Unexpected character '" + c + "'", startCol);
        };
    }

    // --- multi-character scanners 2
    // this handles scientific notation

    private Token number(int startCol) {
        int start = pos;
        while (!isAtEnd() && isDigit(peek())) advance();

        if (!isAtEnd() && peek() == '.') {
            advance();
            while (!isAtEnd() && isDigit(peek())) advance();
        }

        
        if (!isAtEnd() && (peek() == 'e' || peek() == 'E')) {
            advance();
            if (!isAtEnd() && (peek() == '+' || peek() == '-')) advance();
            if (isAtEnd() || !isDigit(peek())) {
                throw new LexerException("Malformed number: missing exponent digits", startCol);
            }
            while (!isAtEnd() && isDigit(peek())) advance();
        }

        String lexeme = source.substring(start, pos);
        try {
            return new Token(TokenType.NUMBER, lexeme, Double.parseDouble(lexeme), startCol);
        } catch (NumberFormatException e) {
            throw new LexerException("Malformed number: " + lexeme, startCol);
        }
    }

    private Token identifier(int startCol) {
        int start = pos;
        while (!isAtEnd() && isIdentPart(peek())) advance();
        String name = source.substring(start, pos);
        return new Token(TokenType.IDENT, name, name, startCol);
    }

   

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) advance();
    }

    private char peek() {
        return source.charAt(pos);
    }

    private void advance() {
        pos++;
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
