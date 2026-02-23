package rs.ac.bg.etf.pp1;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.MJProgram;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.codegen.CodeGenerator;
import rs.ac.bg.etf.pp1.semantic.Environment;
import rs.ac.bg.etf.pp1.logger.CompilerLogger;
import rs.ac.bg.etf.pp1.semantic.SemanticAnalyser;
import rs.ac.bg.etf.pp1.semantic.DumpSymbolTableVisitor;
import rs.ac.bg.etf.pp1.symbols.BuiltIn;
import rs.ac.bg.etf.pp1.symbols.SymbolTable;
import rs.ac.bg.etf.pp1.util.Context;
import rs.etf.pp1.symboltable.Tab;

import java.io.*;

public final class Compiler {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Compiler <source file>");
            System.exit(1);
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("File `" + file.getAbsolutePath() + "' does not exist.");
            System.exit(1);
        }

        if (!file.isFile()) {
            System.err.println("`" + file.getAbsolutePath() + "' is not a file.");
            System.exit(1);
        }

        if (!file.getName().endsWith(".mj")) {
            System.err.println("`" + file.getAbsolutePath() + "' is not a MJ source file.");
            System.exit(1);
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            System.err.println("Failed to open file `" + file.getAbsolutePath() + "':" + (e.getMessage() != null ? e.getMessage() : ""));
            return;
        }

        Context context = new Context();

        Scanner scanner = new MJScanner(reader);
        MJParser parser = new MJParser(scanner);

        Symbol symbol;
        try {
            symbol = parser.parse();
        } catch (Exception e) {
            System.err.println("Error parsing file `" + file.getAbsolutePath() + "'.");
            System.err.println(e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : ""));
            for (StackTraceElement element : e.getStackTrace()) {
                System.err.println("    at " + element);
            }
            return;
        }

        Program program = (Program) symbol.value;

        Tab.init();
        BuiltIn.init();

        SemanticAnalyser analyser = new SemanticAnalyser(context);

        program.accept(analyser);

//        DumpSymbolTableVisitor visitor = new DumpSymbolTableVisitor();
//        SymbolTable table = SymbolTable.getInstance(context);
//        table.dump(visitor);
//        System.out.println(program.toString(" "));

        CodeGenerator generator = new CodeGenerator(context);
        generator.generateProgram((MJProgram) program);
    }
}
