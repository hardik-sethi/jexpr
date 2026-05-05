package com.hardik.calc.engine.error;


public abstract sealed class CalcException extends RuntimeException
        permits LexerException, ParserException {

    private final int column;

    protected CalcException(String message, int column) {
        super(message);
        this.column = column;
    }
    public int column() {
        return column;
    }
}
