package com.hardik.calc.engine.error;

// Thrown when the tokenizer encounters input it cannot lex.
public final class LexerException extends CalcException {
    public LexerException(String message, int column) {
        super(message, column);
    }
}
