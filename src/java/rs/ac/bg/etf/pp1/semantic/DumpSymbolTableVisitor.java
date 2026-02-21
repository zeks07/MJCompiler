package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.symbols.BuiltIn;
import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Type;
import rs.ac.bg.etf.pp1.symbols.TypeKind;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

public class DumpSymbolTableVisitor extends SymbolTableVisitor {
    private static final String INDENT_UNIT = "    ";
    private final StringBuilder output = new StringBuilder();
    private final StringBuilder indent = new StringBuilder();
    private boolean isDefinition = false;

    private void indent() {
        output.append(indent);
    }

    private void indentIn() {
        indent.append(INDENT_UNIT);
    }

    private void indentOut() {
        indent.delete(0, INDENT_UNIT.length());
    }

    @Override
    public void visitObjNode(Obj object) {
        Symbol symbol = Symbol.normalize(object);
        isDefinition = false;

        indent();
        output.append(symbol.getMJKind().name().toLowerCase())
                .append(" ")
                .append(symbol.getName())
                .append(": ");

        if (symbol.getMJKind().isType()) isDefinition = true;

        Type type = symbol.getSymbolType();
        if (type == null) {
            output.append("$ERROR");
        } else {
            type.accept(this);
        }

        output.append(" ")
                .append(symbol.getAddress())
                .append(" ")
                .append(symbol.getLevel())
                .append("\n");

        if (symbol.getMJKind().isDataStorage()) return;

        indentIn();

        if (symbol.getMJKind().isType()) {
            for (Symbol member : symbol.getSymbolType().getMJMembers()) {
                if (member == null) {
                    output.append("$ERROR");
                    continue;
                }
                member.accept(this);
            }
        }

        for (Symbol local : symbol.getLocals()) {
            if (local == null) {
                output.append("$ERROR");
                continue;
            }
            local.accept(this);
        }

        indentOut();
    }

    @Override
    public void visitScopeNode(Scope scope) {
        for (Obj object : scope.values()) {
            if (object == null) {
                output.append("$ERROR");
                continue;
            }
            object.accept(this);
        }
    }

    @Override
    public void visitStructNode(Struct structure) {
        Type type = Type.normalize(structure);
        if (isDefinition && type.getMJKind().isClass()) {
            output.append("Class");
            if (type.getElementType() != BuiltIn.VOID) {
                output.append(" extends ").append(type.getElementType().getName());
            }
        } else if (type.getMJKind() == TypeKind.ENUM) {
            output.append("Enum");
        } else {
            output.append(type.getName());
        }
    }

    @Override
    public String getOutput() {
        return output.toString();
    }
}
