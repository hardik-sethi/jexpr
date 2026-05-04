package com.hardik.calc.cli;

import com.hardik.calc.engine.lexer.Token;
import com.hardik.calc.engine.lexer.Tokenizer;

import java.util.Scanner;

// Phase 1 CLI
 
public final class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("calc tokenizer (phase 1) — Ctrl+D / Ctrl+Z to exit");
        System.out.print("> ");
        while (in.hasNextLine()) {
            String line = in.nextLine();
            try {
                for (Token t : Tokenizer.tokenize(line)) {
                    System.out.println("  " + t);
                }
            } catch (RuntimeException e) {
                System.out.println("  error: " + e.getMessage());
            }
            System.out.print("> ");
        }
    }

    private Main() {} // not instantiable currently, will work on next phase on 5th May 2026
}
