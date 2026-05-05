package com.hardik.calc.engine.error;

// Fallback Safety, it thrown when the parser encounters tokens it cannot match against the grammar
public final class ParserException extends CalcException {
    public ParserException(String message, int column) {
        super(message, column);
    }
}
