package rs.ac.bg.etf.pp1.symbol;

import rs.ac.bg.etf.pp1.symbol.Type.*;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;

import java.util.List;

public abstract class Symbol extends Obj implements Modifiable {
    protected Symbol owner;
    protected Flags flags;

    Symbol(int legacyKind, String name, Type type) {
        super(legacyKind, name, type);
        this.owner = null;
    }

    public Type getMJType() {
        return (Type) getType();
    }

    public Symbol getOwner() { return owner; }

    public void setOwner(Symbol owner) { this.owner = owner; }

    public Flags getModifiers() { return flags; }

    public void setModifiers(Flags flags) { this.flags = flags; }

    public boolean isStatic() { return false; }

    public boolean isDataHolder() { return false; }

    public boolean isParameter() { return false; }

    public boolean isField() { return false; }

    public boolean isCallable() { return false; }

    public boolean isType() { return false; }

    public boolean isClass() { return false; }

    public boolean isSameKind(Symbol other) {
        return getKind() == other.getKind();
    }

    public abstract String getKindName();

    public static abstract class DataHolderSymbol extends Symbol {
        DataHolderSymbol(int legacyKind, String name, Type type) {
            super(legacyKind, name, type);
        }

        @Override
        public boolean isDataHolder() { return true; }
    }

    public static final class ConstantSymbol extends DataHolderSymbol {
        private final int constant;

        ConstantSymbol(String name, Type type, int value) {
            super(Obj.Con, name, type);
            this.constant = value;
            setAdr(value);
        }

        @Override
        public String getKindName() { return "Constant"; }
    }

    public static final class VariableSymbol extends DataHolderSymbol {
        VariableSymbol(String name, Type type) {
            super(Obj.Var, name, type);
        }

        @Override
        public String getKindName() { return "Variable"; }
    }

    public static final class ParameterSymbol extends DataHolderSymbol {
        ParameterSymbol(String name, Type type) {
            super(Obj.Var, name, type);
        }

        @Override
        public boolean isParameter() { return true; }

        @Override
        public String getKindName() { return "Parameter"; }
    }

    public static final class FieldSymbol extends DataHolderSymbol {
        FieldSymbol(String name, Type type) {
            super(Obj.Fld, name, type);
        }

        @Override
        public boolean isField() { return true; }

        @Override
        public String getKindName() { return "Field"; }
    }

    public interface Writable extends Modifiable {
        void addSymbol(Symbol symbol);
        void write();
        Symbol find(String name);
        Symbol getOwner();
        boolean isClass();
    }

    public static final class ProgramSymbol extends Symbol implements Writable {
        private final Scope locals;

        public ProgramSymbol(String name, Scope locals) {
            super(Obj.Prog, name, null);
            this.locals = locals;
            this.flags = new Flags();
        }

        public void addSymbol(Symbol symbol) {
            locals.addToLocals(symbol);
            symbol.setOwner(this);
        }

        @Override
        public void write() {
            this.setLocals(locals.getLocals());
        }

        @Override
        public Symbol find(String name) {
            Obj result = locals.findSymbol(name);
            if (result != null)
                return (Symbol) result;
            return ((Writable) getOwner()).find(name);
        }

        @Override
        public String getKindName() { return "Program"; }
    }

    public static final class MethodSymbol extends Symbol implements Writable {
        private final Scope locals;
        private int parameterCount = 0;

        MethodSymbol(String name, Type returnType, Scope locals) {
            super(Obj.Meth, name, returnType);
            this.locals = locals;
        }

        public void addSymbol(Symbol symbol) {
            if (symbol.isParameter()) parameterCount++;
            locals.addToLocals(symbol);
            symbol.setOwner(this);
        }

        @Override
        public boolean isCallable() { return true; }

        @Override
        public void write() {
            this.setLocals(locals.getLocals());
            setLevel(parameterCount);
        }

        @Override
        public Symbol find(String name) {
            Obj result = locals.findSymbol(name);
            if (result != null)
                return (Symbol) result;
            return ((Writable) getOwner()).find(name);
        }

        public List<Symbol> getSymbols() {
            return getLocalSymbols().stream()
                    .map(obj -> (Symbol) obj)
                    .collect(java.util.stream.Collectors.toList());
        }

        public Type getReturnType() { return (Type) getType(); }

        public boolean matchesSuper(MethodSymbol other) {
            if (parameterCount != other.parameterCount)
                return false;

            if (!getReturnType().equals(other.getReturnType()))
                return false;

            List<Symbol> thisParams = getSymbols();
            List<Symbol> otherParams = other.getSymbols();

            for (int i = 1; i < parameterCount; i++) { // skip "this"
                if (!thisParams.get(i).getMJType().equals(otherParams.get(i).getMJType())) return false;
            }

            return true;
        }

        public boolean matchesArguments(List<Type> arguments) {
            if (parameterCount != arguments.size()) return false;

            List<Symbol> thisParams = getSymbols();

            for (int i = 0; i < parameterCount; i++) {
                if (!arguments.get(i).assignable(thisParams.get(i).getMJType())) return false;
            }
            return true;
        }

        @Override
        public String getKindName() { return "Method"; }

        public String getFullName() {
            return getOwner().getName() + "." + getName();
        }

        public String formatParameters() {
            return getSymbols().stream()
                    .filter(Symbol::isParameter)
                    .map(Symbol::getName)
                    .collect(java.util.stream.Collectors.joining(", "));
        }
    }

    public static abstract class TypeSymbol extends Symbol {
        TypeSymbol(String name, Type type) {
            super(Obj.Type, name, type);
        }

        @Override
        public boolean isType() { return true; }
    }

    public static final class PrimitiveTypeSymbol extends TypeSymbol {
        PrimitiveTypeSymbol(String name, Type type) {
            super(name, type);
            setLevel(0);
        }

        @Override
        public String getKindName() { return "Type"; }
    }

    public static final class ArrayTypeSymbol extends TypeSymbol {
        ArrayTypeSymbol(ArrayType type) {
            super(type.getElementType().owner.getName() + "[]", type);
            setLevel(1);
        }

        @Override
        public String getKindName() { return "Variable"; }
    }

    public static final class ClassSymbol extends TypeSymbol implements Writable {
        public ClassSymbol(String name, ClassType thisType) {
            super(name, thisType);
        }

        public ClassType getThisType() {
            return (ClassType) getMJType();
        }

        @Override
        public boolean isClass() { return true; }

        @Override
        public void addSymbol(Symbol symbol) {
            getThisType().addMember(symbol);
            symbol.setOwner(this);
        }

        @Override
        public void write() {
            getThisType().write();
        }

        @Override
        public Symbol find(String name) {
            Symbol result = getThisType().find(name);
            if (result != SymbolTable.NO_SYMBOL)
                return result;
            return ((Writable) getOwner()).find(name);
        }

        @Override
        public String getKindName() { return "Class"; }
    }

    public static final class EnumSymbol extends TypeSymbol {
        EnumSymbol(String name, EnumType enumType) {
            super(name, enumType);
        }

        @Override
        public String getKindName() { return "Enum"; }
    }

    public static final class UniverseSymbol extends Symbol implements Writable {
        Scope universeScope;

        @Override
        public void addSymbol(Symbol symbol) {
            universeScope.addToLocals(symbol);
            symbol.setOwner(this);
        }

        @Override
        public void write() {}

        @Override
        public Symbol find(String name) {
            Obj result = universeScope.findSymbol(name);
            if (result != null) return (Symbol) result;
            return SymbolTable.NO_SYMBOL;
        }

        UniverseSymbol(Scope universeScope) {
            super(-1, "", null);
            this.universeScope = universeScope;
        }

        @Override
        public String getKindName() { return ""; }
    }

    public static final class NoSymbol extends Symbol {
        NoSymbol(Type type) {
            super(Obj.Var, "noObj", type);
        }

        @Override
        public String getKindName() { return ""; }
    }
}