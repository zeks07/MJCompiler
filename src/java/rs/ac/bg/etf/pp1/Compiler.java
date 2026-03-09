package rs.ac.bg.etf.pp1;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.MJProgram;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.symbol.Symbol.*;
import rs.ac.bg.etf.pp1.logger.CompilerDiagnostics;
import rs.ac.bg.etf.pp1.logger.CompilerLogger;
import rs.ac.bg.etf.pp1.sem.SymbolCollector;
import rs.ac.bg.etf.pp1.util.Context;

import java.io.*;
import java.util.List;
import java.util.Map;

public final class Compiler {
    public static void main(String[] args) {
        if (args.length < 1) {
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
        CompilerDiagnostics diagnostics = CompilerDiagnostics.getInstance(context);
        CompilerLogger logger = CompilerLogger.getInstance(context);

        Scanner scanner = new MJScanner(reader, context);
        MJParser parser = new MJParser(scanner, context);

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

        if (diagnostics.hasErrors()) {
            logger.renderAll(diagnostics.all());
            return;
        }

        MJProgram program = (MJProgram) symbol.value;

        SymbolCollector analyser = new SymbolCollector(context);
        Map<MethodSymbol, SyntaxNode> methods = analyser.enterProgram(program);

        if (diagnostics.hasErrors()) {
            logger.renderAll(diagnostics.all());
            return;
        }

//        RuntimeLibrary.emit(context);
//
//        CodeGenerator generator = new CodeGenerator(context);
//        generator.generateProgram((MJProgram) program);
//
//        String fileName = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 3);
//        Path output = Paths.get(fileName + ".obj");
//
//        if (diagnostics.hasErrors()) {
//            logger.renderAll(diagnostics.all());
//            return;
//        }
//
//        try (OutputStream os = Files.newOutputStream(output)) {
//            BytecodeEmitter.write(os);
//        } catch (IOException e) {
//            System.err.println("Failed to write bytecode to file `" + output);
//        }
//
//        if (diagnostics.hasErrors()) {
//            logger.renderAll(diagnostics.all());
//            return;
//        }
//
//        disasm.main( new String[] { output.toString() });
    }
}
