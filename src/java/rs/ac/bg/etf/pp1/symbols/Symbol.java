package rs.ac.bg.etf.pp1.symbols;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface Symbol {
    String getName();
    SymbolKind getMJKind();
    Type getSymbolType();
    int getAddress();
    void setAddress(int address);
    int getLevel();
    void increaseLevel();
    void setAbstract();
    boolean isAbstract();
    void setLocalSymbols(SymbolDataStructure locals);
    boolean isStatic();
    Collection<Symbol> getLocals();
    void accept(SymbolTableVisitor visitor);
    Obj getObj();

    static Symbol normalize(Obj object) {
        if (object == null) {
            return BuiltIn.NO_OBJECT;
        }

        Symbol symbol = BuiltIn.getBuiltIn(object);
        if (symbol != null) {
            return symbol;
        }

        if (object instanceof MJSymbol) {
            return (MJSymbol) object;
        }

        SymbolKind kind = SymbolKind.normalize(object.getKind());
        Type type = Type.normalize(object.getType());

        return new MJSymbol(kind, object.getName(), type);
    }

    class MJSymbol extends Obj implements Symbol {
        private final SymbolKind kind;
        private boolean isAbstract;

        public MJSymbol(SymbolKind kind, String name, Type type, int address, int level) {
            super(kind.toLegacy(), name, type.getStruct(), address, level);
            this.kind = kind;
        }

        public MJSymbol(SymbolKind symbolKind, String name, Type elementType) {
            this(symbolKind, name, elementType, 0, 0);
        }

        @Override
        public SymbolKind getMJKind() {
            return kind;
        }

        @Override
        public Type getSymbolType() {
            return rs.ac.bg.etf.pp1.symbols.Type.normalize(getType());
        }

        @Override
        public int getAddress() {
            return getAdr();
        }

        @Override
        public void setAddress(int address) {
            setAdr(address);
        }

        @Override
        public void increaseLevel() {
            setLevel(getLevel() + 1);
        }

        @Override
        public void setAbstract() {
            isAbstract = true;
        }

        @Override
        public boolean isAbstract() {
            return isAbstract;
        }

        @Override
        public void setLocalSymbols(SymbolDataStructure locals) {
            setLocals(locals);
            int address = 0;
            for (Symbol member : getLocals()) {
                if (member.getMJKind().isAssignable()) member.setAddress(address++);
            }
        }

        @Override
        public boolean isStatic() {
            return getLevel() == 0;
        }

        @Override
        public Collection<Symbol> getLocals() {
            return getLocalSymbols().stream()
                    .map(Symbol::normalize)
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public Obj getObj() {
            return this;
        }
    }

    class ProgramSymbol extends MJSymbol {
        public ProgramSymbol(String name) {
            super(SymbolKind.PROGRAM, name, BuiltIn.VOID);
        }

        public int getStaticFieldCount() {
            int count = 0;
            for (Symbol symbol : getLocals()) {
                if (symbol.getMJKind().isVariable()) count++;
            }
            return count;
        }
    }

    class MethodSymbol extends MJSymbol {
        private boolean isMain;
        private Symbol owner;

        public MethodSymbol(String name, Type returnType) {
            super(SymbolKind.METHOD, name, returnType);
        }

        private int arity() {
            return getParameters().size();
        }

        private int arityNoThis() {
            return !getParameters().isEmpty() && getParameters().get(0).getName().equals("this") ? arity() - 1 : arity();
        }

        public boolean equalsExactly(MethodSymbol other) {
            if (!getName().equals(other.getName())) return false;
            if (arity() != other.arity()) return false;

            for (int i = 0; i < arity(); i++) {
                if (getParameters().get(i).getName().equals("this")) continue;
                if (!getParameters().get(i).getSymbolType().equals(other.getParameters().get(i).getSymbolType())) return false;
            }

            return true;
        }

        public boolean acceptsArguments(List<Type> arguments) {
            if (arityNoThis() != arguments.size()) return false;

            for (int i = 0, j = 0; i < arity(); i++, j++) {
                if (getParameters().get(i).getName().equals("this")) {
                    j--;
                    continue;
                }
                if (!getParameters().get(i).getSymbolType().isAssignableTo(arguments.get(j))) return false;
            }

            return true;
        }

        public String formatParameterTypes() {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (Symbol parameter : getParameters()) {
                sb.append(parameter.getSymbolType().getName()).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append(")");
            return sb.toString();
        }

        private List<Symbol> getParameters() {
            List<Symbol> parameters = new ArrayList<>(getLevel());

            for (Symbol variable : getLocals()) {
                if (variable.getMJKind().isVariable() && variable.getAddress() < getLevel()) {
                    parameters.add(variable);
                }
            }

            return parameters;
        }

        public boolean isMain() {
            return isMain;
        }

        public void setMain(boolean isMain) {
            this.isMain = isMain;
        }

        public void setOwner(Symbol owner) {
            this.owner = owner;
        }

        @Override
        public boolean isStatic() {
            return owner == null;
        }
    }

    class LegacySymbol implements Symbol {
        private final Obj legacy;
        private final SymbolKind kind;

        public LegacySymbol(Obj legacy) {
            this.legacy = legacy;
            this.kind = legacy.getName().equals("noObj")
                    ? SymbolKind.NONE
                    : SymbolKind.normalize(legacy.getKind());
        }

        @Override
        public String getName() {
            return legacy.getName();
        }

        @Override
        public SymbolKind getMJKind() {
            return kind;
        }

        @Override
        public Type getSymbolType() {
            if (legacy.getType().getKind() == Struct.Array) {
                return new Type.MJType(TypeKind.ARRAY, Type.normalize(legacy.getType().getElemType()));
            }
            return Type.normalize(legacy.getType());
        }

        @Override
        public int getAddress() {
            return legacy.getAdr();
        }

        @Override
        public void setAddress(int address) {
            legacy.setAdr(address);
        }

        @Override
        public int getLevel() {
            return legacy.getLevel();
        }

        @Override
        public void increaseLevel() {
            legacy.setLevel(legacy.getLevel() + 1);
        }

        @Override
        public void setAbstract() {
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public void setLocalSymbols(SymbolDataStructure locals) {
            legacy.setLocals(locals);
            int address = 0;
            for (Obj member : locals.symbols()) {
                Symbol symbol = Symbol.normalize(member);
                if (symbol.getMJKind().isAssignable()) symbol.setAddress(address++);
            }
        }

        @Override
        public boolean isStatic() {
            return legacy.getLevel() == 0;
        }

        @Override
        public Collection<Symbol> getLocals() {
            return legacy.getLocalSymbols().stream()
                    .map(Symbol::normalize)
                    .collect(Collectors.toList());
        }

        @Override
        public void accept(SymbolTableVisitor visitor) {
            legacy.accept(visitor);
        }

        @Override
        public Obj getObj() {
            return legacy;
        }
    }
}
