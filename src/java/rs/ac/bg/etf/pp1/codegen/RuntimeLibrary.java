package rs.ac.bg.etf.pp1.codegen;

import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Symbol.*;
import rs.ac.bg.etf.pp1.symbols.SymbolTable;
import rs.ac.bg.etf.pp1.util.Context;

import java.util.Arrays;
import java.util.List;

import static rs.ac.bg.etf.pp1.codegen.Bytecodes.*;

@SuppressWarnings("DuplicatedCode")
public final class RuntimeLibrary {
    private RuntimeLibrary() {}

    private interface BuiltinEmitter {
        void emit(Context context);
    }

    private static final List<BuiltinEmitter> BUILTINS = Arrays.asList(
            RuntimeLibrary::emitChr,
            RuntimeLibrary::emitOrd
    );

    public static void emit(Context context) {
        for (BuiltinEmitter emitter : BUILTINS) {
            emitter.emit(context);
        }
    }

    private static void emitChr(Context context) {
        BytecodeEmitter code = BytecodeEmitter.getInstance(context);
        SymbolTable table = SymbolTable.getInstance(context);

        Symbol method = table.find("chr");
        if (method == SymbolTable.NO_SYMBOL) {
            throw new AssertionError("Expected method symbol");
        }

        int pc = code.entryPoint();
        method.setAdr(pc);
        code.emitop2(enter, (1 << 8) | 1);

        code.emitop0(load_0);
        code.emitop0(exit);
        code.emitop0(return_);
    }

    private static void emitOrd(Context context) {
        BytecodeEmitter code = BytecodeEmitter.getInstance(context);
        SymbolTable table = SymbolTable.getInstance(context);

        Symbol method = table.find("ord");
        if (method == SymbolTable.NO_SYMBOL) {
            throw new AssertionError("Expected method symbol");
        }

        int pc = code.entryPoint();
        method.setAdr(pc);
        code.emitop2(enter, (1 << 8) | 1);

        code.emitop0(load_0);
        code.emitop0(exit);
        code.emitop0(return_);
    }
}
