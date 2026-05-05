package com.hardik.calc.cli;

import com.hardik.calc.engine.ast.AstPrinter;
import com.hardik.calc.engine.ast.Expr;
import com.hardik.calc.engine.error.CalcException;
import com.hardik.calc.engine.lexer.Token;
import com.hardik.calc.engine.lexer.Tokenizer;
import com.hardik.calc.engine.parser.Parser;
import java.util.List;
import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("calc parser (phase 2) — Ctrl+D / Ctrl+Z to exit");
        System.out.print("> ");
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.isBlank()) {
                System.out.print("> ");
                continue;
            }
            try {
                List<Token> tokens = Tokenizer.tokenize(line);
                System.out.println("  tokens:");
                for (Token t : tokens) System.out.println("    " + t);

                Expr ast = new Parser(tokens).parseProgram();
                System.out.println("  ast: " + AstPrinter.print(ast));
            } catch (CalcException e) {
                System.out.println("  error at column " + e.column() + ": " + e.getMessage());
                System.out.println("  " + caretLine(line, e.column()));
            }
            System.out.print("> ");
        }
    }
    private static String caretLine(String input, int column) {
        StringBuilder sb = new StringBuilder("  "); // align under "  " prefix
        for (int i = 0; i < column; i++) {
            sb.append(i < input.length() && input.charAt(i) == '\t' ? '\t' : ' ');
        }
        sb.append('^');
        return sb.toString();
    }

    private Main() {}
}
