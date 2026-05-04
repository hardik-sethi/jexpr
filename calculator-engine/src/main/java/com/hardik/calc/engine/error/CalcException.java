package com.hardik.calc.engine.error;


public abstract sealed class CalcException extends RuntimeException
        permits LexerException {

    private final int column;

    protected CalcException(String message, int column) {
        super(message);
        this.column = column;
    }

    /** Zero-based column in the source string where the error was detected. */
    public int column() {
        return column;
    }
}
