package rs.ac.bg.etf.pp1.symbols;

import rs.ac.bg.etf.pp1.ast.MJProgram;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.util.Context;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

import static rs.ac.bg.etf.pp1.symbols.Symbol.*;

public final class SymbolTable {
    private static final Context.Key<SymbolTable> symbolTableKey = new Context.Key<>();
    private int nestingLevel;

    private SymbolTable(Context context) {
        context.put(symbolTableKey, this);
        Tab.currentScope.addToLocals(new Obj(Obj.Type, "bool", BuiltIn.BOOLEAN.getStruct()));
        nestingLevel = -1;
    }

    public static SymbolTable getInstance(Context context) {
        SymbolTable symbolTable = context.get(symbolTableKey);
        if (symbolTable == null) {
            symbolTable = new SymbolTable(context);
        }
        return symbolTable;
    }

    public void openScope() {
        Tab.openScope();
        nestingLevel++;
    }

    public void closeScope() {
        Tab.closeScope();
        nestingLevel--;
    }

    public Symbol insert(
            SymbolKind kind,
            String name,
            Type type,
            SyntaxNode node
    ) {
        int level = nestingLevel > 0 && !kind.isCallable() ? 1 : 0;
        MJSymbol symbol;
        switch (kind) {
            case PROGRAM: symbol = new ProgramSymbol(name); break;
            case METHOD: symbol = new MethodSymbol(name, type, node); break;
            case TYPE: symbol = new ClassSymbol(name, type, node); break;
            default: symbol = new MJSymbol(kind, name, type, 0, level);
        }
        if (!Tab.currentScope().addToLocals(symbol)) {
            return Symbol.normalize(Tab.currentScope().findSymbol(name));
        }
        return symbol;
    }

    public void insert(Symbol member) {
        Tab.currentScope().addToLocals(member.getObj());
    }

    public Symbol find(String name) {
        for (Scope scope = Tab.currentScope(); scope != null; scope = scope.getOuter()) {
            if (scope.getLocals() == null) continue;
            Symbol found = Symbol.normalize(scope.getLocals().searchKey(name));
            if (found != BuiltIn.NO_OBJECT) return found;
        }
        return BuiltIn.NO_OBJECT;
    }

    public Symbol findInThisScope(String name) {
        if (Tab.currentScope().getLocals() == null) return BuiltIn.NO_OBJECT;
        return Symbol.normalize(Tab.currentScope().getOuter().getLocals().searchKey(name));
    }

    public boolean isInScope(String name) {
        if (Tab.currentScope().getLocals() == null) return false;
        return Tab.currentScope().getLocals().searchKey(name) != null;
    }

    public void chainLocalsTo(Symbol owner) {
        owner.setLocalSymbols(Tab.currentScope().getLocals());
    }

    public void chainMembersTo(Type owner) {
        owner.setMembers(Tab.currentScope().getLocals());
    }

    public void dump(SymbolTableVisitor visitor) {
        Tab.dump(visitor);
    }

    public static void reset() {
        Tab.closeScope();
    }
}
