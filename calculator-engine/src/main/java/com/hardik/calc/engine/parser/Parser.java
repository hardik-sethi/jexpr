package com.hardik.calc.engine.parser;

import com.hardik.calc.engine.ast.Expr;
import com.hardik.calc.engine.ast.Expr.Op;
import com.hardik.calc.engine.error.ParserException;
import com.hardik.calc.engine.lexer.Token;
import com.hardik.calc.engine.lexer.TokenType;
import com.hardik.calc.engine.lexer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

// recursive-descent parser that turns a token stream into an @link Expr tree.

public final class Parser {

    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("token list must be non-empty (must contain at least EOF)");
        }
        if (tokens.get(tokens.size() - 1).type() != TokenType.EOF) {
            throw new IllegalArgumentException("token list must end with EOF");
        }
        this.tokens = tokens;
        this.pos = 0;
    }

    public static Expr parse(String source) {
        return new Parser(Tokenizer.tokenize(source)).parseProgram();
    }

    public Expr parseProgram() {
        Expr expr = program();
        if (!check(TokenType.EOF)) {
            Token unexpected = peek();
            throw new ParserException(
                    "Unexpected token after expression: '" + unexpected.lexeme() + "'",
                    unexpected.column());
        }
        return expr;
    }

// --- grammar rules ----

// rules i am following - {@code program -> assignment | expression} 

    private Expr program() {
        if (check(TokenType.IDENT) && checkNext(TokenType.EQUALS)) {
            Token name = advance();
            advance(); // consume '='
            Expr value = expression();
            return new Expr.Assignment(name.lexeme(), value, name.column());
        }
        return expression();
    }

//  {@code expression -> term (("+" | "-") term)*} 
    private Expr expression() {
        Expr left = term();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            Token op = advance();
            Expr right = term();
            Op kind = op.type() == TokenType.PLUS ? Op.ADD : Op.SUB;
            left = new Expr.BinaryOp(kind, left, right, left.column());
        }
        return left;
    }

//  {@code term -> unary (("*" | "/" | "%") unary)*}
    private Expr term() {
        Expr left = unary();
        while (check(TokenType.STAR) || check(TokenType.SLASH) || check(TokenType.PERCENT)) {
            Token op = advance();
            Expr right = unary();
            Op kind = switch (op.type()) {
                case STAR    -> Op.MUL;
                case SLASH   -> Op.DIV;
                case PERCENT -> Op.MOD;
                default -> throw new AssertionError("unreachable");
            };
            left = new Expr.BinaryOp(kind, left, right, left.column());
        }
        return left;
    }

//   {@code unary -> ("-" | "+") unary | power} 
    private Expr unary() {
        if (check(TokenType.MINUS) || check(TokenType.PLUS)) {
            Token op = advance();
            Expr operand = unary();
            Op kind = op.type() == TokenType.MINUS ? Op.NEG : Op.POS;
            return new Expr.UnaryOp(kind, operand, op.column());
        }
        return power();
    }


    private Expr power() {
        Expr base = primary();
        if (check(TokenType.CARET) || check(TokenType.STAR_STAR)) {
            advance();
            Expr exponent = unary();
            return new Expr.BinaryOp(Op.POW, base, exponent, base.column());
        }
        return base;
    }

    private Expr primary() {
        Token t = peek();
        return switch (t.type()) {
            case NUMBER -> {
                advance();
                yield new Expr.NumberLiteral((double) t.value(), t.column());
            }
            case IDENT -> {
                advance();
                if (check(TokenType.LPAREN)) {
                    advance(); // consume '('
                    List<Expr> args = arguments();
                    expect(TokenType.RPAREN, "')' to close function call");
                    yield new Expr.FunctionCall(t.lexeme(), args, t.column());
                }
                yield new Expr.Variable(t.lexeme(), t.column());
            }
            case LPAREN -> {
                advance(); // consume '('
                Expr inner = expression();
                expect(TokenType.RPAREN, "')' to close grouped expression");
                yield inner;
            }
            default -> throw new ParserException(
                    "Expected number, identifier, or '(' but got '" + t.lexeme() + "'",
                    t.column());
        };
    }
// {@code arguments -> expression empty for a no-arg call
    private List<Expr> arguments() {
        List<Expr> args = new ArrayList<>();
        if (check(TokenType.RPAREN)) return args; // f()
        args.add(expression());
        while (check(TokenType.COMMA)) {
            advance();
            args.add(expression());
        }
        return args;
    }

    // --- low-level helpers --

    private Token peek() {
        return tokens.get(pos);
    }

    private boolean checkNext(TokenType type) {
        if (pos + 1 >= tokens.size()) return false;
        return tokens.get(pos + 1).type() == type;
    }

    private boolean check(TokenType type) {
        return peek().type() == type;
    }

    private Token advance() {
        Token t = tokens.get(pos);
        if (t.type() != TokenType.EOF) pos++;
        return t;
    }

    private Token expect(TokenType type, String description) {
        if (!check(type)) {
            Token got = peek();
            throw new ParserException(
                    "Expected " + description + " but got '" + got.lexeme() + "'",
                    got.column());
        }
        return advance();
    }
}
