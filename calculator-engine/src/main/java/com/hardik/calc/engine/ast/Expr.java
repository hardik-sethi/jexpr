package com.hardik.calc.engine.ast;

import java.util.List;

//  The abstract syntax tree of a parsed expression.

public sealed interface Expr {

    int column();

    record NumberLiteral(double value, int column) implements Expr {}

    record Variable(String name, int column) implements Expr {
        public Variable {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name must be non-empty");
            }
        }
    }

    record BinaryOp(Op op, Expr left, Expr right, int column) implements Expr {}

    record UnaryOp(Op op, Expr operand, int column) implements Expr {}

    record FunctionCall(String name, List<Expr> args, int column) implements Expr {
        public FunctionCall {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name must be non-empty");
            }
            args = List.copyOf(args);
        }
    }

    record Assignment(String name, Expr value, int column) implements Expr {
        public Assignment {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name must be non-empty");
            }
        }
    }

   
    enum Op {
        ADD, SUB, MUL, DIV, MOD, POW,
        NEG, POS 
    }
}
