package rs.ac.bg.etf.pp1.symbol;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.Collection;

public abstract class Type extends Struct {
    protected Symbol owner;

    public Type(int legacyKind, Symbol owner) {
        super(legacyKind);
        this.owner = owner;
    }

    public Symbol getOwner() {
        return owner;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isClass() { return false; }

    public boolean isArray() { return false; }

    public boolean assignable(Type other) {
        if (this == other)
            return true;

        return xAssignable(other);
    }

    protected boolean xAssignable(Type other) {
        return false;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Type)) return false;
        Type other = (Type) o;
        return xEquals(other);
    }

    protected boolean xEquals(Type other) {
        return this == other;
    }

    @Override
    public String toString() { return getOwner().getName(); }

    public static final class PrimitiveType extends Type {
        public PrimitiveType(int legacyKind) {
            super(legacyKind, null);
        }

        @Override
        public boolean isPrimitive() { return true; }
    }

    public static final class EnumType extends Type {
        public EnumType(Symbol owner) {
            super(Struct.Enum, owner);
        }
    }

    public static abstract class ReferenceType extends Type {
        public ReferenceType(int legacyKind, Symbol owner) {
            super(legacyKind, owner);
        }

        @Override
        public boolean isRefType() { return true; }
    }

    public static final class ArrayType extends ReferenceType {
        public ArrayType(Type elementType) {
            super(Struct.Array, null);
            setElementType(elementType);
        }

        public Type getElementType() { return (Type) getElemType(); }

        @Override
        public boolean isArray() { return true; }

        @Override
        protected boolean xAssignable(Type other) {
            return xEquals(other);
        }

        @Override
        protected boolean xEquals(Type other) {
            if (!other.isArray())
                return false;

            return getElementType().equals(((ArrayType) other).getElementType());
        }
    }

    public static final class ClassType extends ReferenceType {
        Scope scope;

        public ClassType(ClassType superType, Scope scope) {
            super(Struct.Class, null);
            this.scope = scope;
            setElementType(superType);
            inheritFields();
        }

        private void inheritFields() {
            if (getSuperType() == null) return;

            getSuperType().getClassMembers().stream()
                    .filter(Symbol::isField)
                    .forEach(symbol -> scope.addToLocals(symbol));
        }

        public ClassType getSuperType() {
            return (ClassType) getElemType();
        }

        @Override
        public boolean isClass() { return true; }

        public void addMember(Symbol symbol) {
            scope.addToLocals(symbol);
        }

        public void write() {
            setMembers(scope.getLocals());
        }

        public Symbol find(String name) {
            Obj symbol = scope.findSymbol(name);
            if (symbol != null)
                return (Symbol) symbol;

            if (getSuperType() != null)
                return getSuperType().find(name);

            return SymbolTable.NO_SYMBOL;
        }

        public Symbol findOwn(String name) {
            Obj symbol = scope.findSymbol(name);
            if (symbol != null)
                return (Symbol) symbol;

            return SymbolTable.NO_SYMBOL;
        }

        public Collection<Symbol> getClassMembers() {
            return getMembers().stream().map(obj -> (Symbol) obj).collect(java.util.stream.Collectors.toList());
        }

        @Override
        protected boolean xAssignable(Type other) {
            if (!other.isClass())
                return false;

            return extendsFrom((ClassType) other);
        }

        public boolean extendsFrom(ClassType other) {
            if (getSuperType() == null)
                return false;

            if (getSuperType().equals(other))
                return true;

            return getSuperType().extendsFrom(other);
        }
    }

    public static final class NullType extends ReferenceType {
        public NullType() {
            super(Struct.Class, null);
        }
    }

    public static final class VoidType extends Type {
        public VoidType() {
            super(Struct.None, null);
        }
    }
}
