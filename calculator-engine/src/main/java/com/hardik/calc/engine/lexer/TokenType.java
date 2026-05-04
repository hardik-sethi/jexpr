package com.hardik.calc.engine.lexer;


//The set of token categories the tokenizer can produce.

public enum TokenType {
    // Literals
    NUMBER,
    IDENT,

    // Operators
    PLUS,
    MINUS,
    STAR,
    SLASH,
    PERCENT,
    CARET,
    EQUALS,

    // Punctuation
    LPAREN,
    RPAREN,
    COMMA,

 // TODO: real token for ran off the end like conditions
    EOF
}
