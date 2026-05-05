package com.hardik.calc.engine.ast;

public final class AstPrinter {

    private AstPrinter() {} 

    public static String print(Expr expr) {
        return switch (expr) {
            case Expr.NumberLiteral n -> formatNumber(n.value());
            case Expr.Variable v      -> v.name();
            case Expr.UnaryOp u       -> "(" + opSymbol(u.op()) + " " + print(u.operand()) + ")";
            case Expr.BinaryOp b      -> "(" + opSymbol(b.op()) + " " + print(b.left())
                                              + " " + print(b.right()) + ")";
            case Expr.FunctionCall f  -> {
                StringBuilder sb = new StringBuilder("(").append(f.name());
                for (Expr arg : f.args()) sb.append(' ').append(print(arg));
                sb.append(')');
                yield sb.toString();
            }
            case Expr.Assignment a    -> "(= " + a.name() + " " + print(a.value()) + ")";
        };
    }

    private static String formatNumber(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v) && Math.abs(v) < 1e16) {
            return Long.toString((long) v);
        }
        return Double.toString(v);
    }

    private static String opSymbol(Expr.Op op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case MOD -> "%";
            case POW -> "^";
            case NEG -> "neg";
            case POS -> "pos";
        };
    }
}
