package com.hardik.calc.engine.lexer;


public record Token(TokenType type, String lexeme, Object value, int column) {

    public Token {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (lexeme == null) throw new IllegalArgumentException("lexeme must not be null");
        if (column < 0) throw new IllegalArgumentException("column must be non-negative");
    }

    @Override
    public String toString() {
        return value == null
                ? "%s(%s)@%d".formatted(type, lexeme, column)
                : "%s(%s=%s)@%d".formatted(type, lexeme, value, column);
    }
}
