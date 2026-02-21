package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.symbols.BuiltIn;
import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Type;

import java.util.HashSet;
import java.util.Set;

import static rs.ac.bg.etf.pp1.symbols.Symbol.MethodSymbol;

public class ScopeContext {
    protected final ScopeContext outer;
    protected boolean isConstant = false;
    protected boolean isAbstract = false;

    protected Type currentType;

    protected ScopeContext(ScopeContext outer) {
        this.outer = outer;
    }

    public void setObject(Symbol object) {
    }

    public Symbol getObject() {
        return BuiltIn.NO_OBJECT;
    }

    public Type getThisType() {
        return BuiltIn.VOID;
    }

    public boolean setType(Type type) {
        return true;
    }

    public boolean isConstantAllowed() {
        return false;
    }

    public boolean isAbstractAllowed() {
        return false;
    }

    public void setModifiers() {
        if (isAbstract) {
            getObject().setAbstract();
        }
    }

    public boolean isThisAllowed() {
        return false;
    }

    public boolean isBreakAllowed() {
        return false;
    }

    public boolean isContinueAllowed() {
        return false;
    }

    public boolean isInClass() {
        return false;
    }

    public static final class ProgramContext extends ScopeContext {
        private final Symbol.ProgramSymbol program;

        public ProgramContext(ScopeContext outer, Symbol.ProgramSymbol program) {
            super(outer);
            this.program = program;
        }

        @Override
        public Symbol getObject() {
            return program;
        }
    }

    public static final class VariableDeclarationContext extends ScopeContext {
        public VariableDeclarationContext(ScopeContext outer) {
            super(outer);
        }

        @Override
        public boolean isConstantAllowed() {
            return true;
        }
    }

    public static final class ClassDeclarationContext extends ScopeContext {
        final Symbol clazz;

        public ClassDeclarationContext(ScopeContext outer, Symbol clazz) {
            super(outer);
            this.clazz = clazz;
        }

        @Override
        public boolean setType(Type type) {
            return type.getMJKind().isExtendable();
        }

        @Override
        public Symbol getObject() {
            return clazz;
        }

        @Override
        public boolean isAbstractAllowed() {
            return true;
        }

        @Override
        public boolean isThisAllowed() {
            return true;
        }

        @Override
        public Type getThisType() {
            return clazz.getSymbolType();
        }

        @Override
        public boolean isInClass() {
            return true;
        }
    }

    public static final class EnumDeclarationContext extends ScopeContext {
        final Symbol enumClass;
        int next = 0;

        public EnumDeclarationContext(ScopeContext outer, Symbol enumClass) {
            super(outer);
            this.enumClass = enumClass;
        }

        @Override
        public Symbol getObject() {
            return enumClass;
        }

        public int getNext() {
            return next++;
        }

        public void setNext(int next) {
            this.next = next + 1;
        }
    }

    public static final class MethodDeclarationContext extends ScopeContext {
        private MethodSymbol method;

        public MethodDeclarationContext(ScopeContext outer) {
            super(outer);
        }

        @Override
        public boolean isAbstractAllowed() {
            return outer.isAbstract;
        }

        @Override
        public void setObject(Symbol object) {
            if (!(object instanceof MethodSymbol)) {
                throw new AssertionError("Expected MethodSymbol");
            }
            method = (MethodSymbol) object;
        }

        @Override
        public Symbol getObject() {
            return method;
        }

        @Override
        public Type getThisType() {
            return outer.getThisType();
        }

        @Override
        public boolean isThisAllowed() {
            return outer.isThisAllowed();
        }

        @Override
        public boolean isInClass() {
            return outer.isInClass();
        }

    }

    public static abstract class StatementContext extends ScopeContext {
        public StatementContext(ScopeContext outer) {
            super(outer);
        }

        @Override
        public Type getThisType() {
            return outer.getThisType();
        }

        @Override
        public boolean isThisAllowed() {
            return outer.isThisAllowed();
        }

        @Override
        public boolean isContinueAllowed() {
            return outer.isContinueAllowed();
        }

        @Override
        public boolean isBreakAllowed() {
            return outer.isBreakAllowed();
        }

        @Override
        public Symbol getObject() {
            return outer.getObject();
        }
    }

    public static final class BlockContext extends StatementContext {
        public BlockContext(ScopeContext outer) {
            super(outer);
        }
    }

    public static final class SwitchContext extends StatementContext {
        Set<Integer> cases = new HashSet<>();

        public SwitchContext(ScopeContext outer) {
            super(outer);
        }

        @Override
        public boolean isBreakAllowed() {
            return true;
        }

        public boolean newCase(int literal) {
            return !cases.add(literal);
        }
    }
}
