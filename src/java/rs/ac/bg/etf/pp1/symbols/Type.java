package rs.ac.bg.etf.pp1.symbols;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import rs.ac.bg.etf.pp1.symbols.Symbol.*;

public interface Type {
    String getName();
    TypeKind getMJKind();
    void setElementType(Type elementType);
    Type getElementType();
    void setMembers(SymbolDataStructure members);
    Collection<Symbol> getMJMembers();
    Symbol findMember(String name);
    boolean isCompatibleWith(Type other);
    boolean isAssignableTo(Type other);
    boolean extendsFrom(Type other);
    void accept(SymbolTableVisitor visitor);
    int getFieldCount();

    Struct getStruct();

    static Type normalize(Struct structure) {
        if (structure == null) {
            return BuiltIn.VOID;
        }

        Type type = BuiltIn.getBuiltIn(structure);
        if (type != null) {
            return type;
        }

        if (structure instanceof MJType) {
            return (MJType) structure;
        }

        return BuiltIn.arrayOf(Type.normalize(structure.getElemType()));
    }

    class MJType extends Struct implements Type {
        private final String name;
        private final TypeKind kind;

        public MJType(TypeKind kind, String name) {
            super(kind.toLegacy());
            this.kind = kind;
            this.name = name;
        }

        public MJType(TypeKind kind, Type elemType) {
            super(kind.toLegacy(), elemType.getStruct());
            this.kind = kind;
            this.name = elemType.getName() + "[]";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public TypeKind getMJKind() {
            return kind;
        }

        @Override
        public void setElementType(Type elementType) {
            setElementType(elementType.getStruct());
        }

        @Override
        public Type getElementType() {
            Struct type = getElemType();
            return type != null ? Type.normalize(type) : BuiltIn.VOID;
        }

        @Override
        public Collection<Symbol> getMJMembers() {
            return getMembers().stream()
                    .map(Symbol::normalize)
                    .collect(Collectors.toList());
        }

        @Override
        public Symbol findMember(String name) {
            Obj found = getMembersTable().searchKey(name);
            return found != null ? Symbol.normalize(found) : BuiltIn.NO_OBJECT;
        }

        @Override
        public boolean isCompatibleWith(Type other) {
            return this.equals(other)
                    || this == BuiltIn.NULL && other.getMJKind().isReferencable()
                    || other == BuiltIn.NULL && this.getMJKind().isReferencable();
        }

        @Override
        public boolean isAssignableTo(Type other) {
            return this.equals(other)
                    || (this == BuiltIn.NULL && other.getMJKind().isExtendable())
                    || extendsFrom(other)
                    || (this.kind.isArray() && other.getMJKind().isArray()
                    && other.getElementType().isAssignableTo(this.getElementType()));
        }

        @Override
        public boolean extendsFrom(Type other) {
            if (!kind.isReferencable() || kind.isArray()) return false;
            Type superType = getElementType();
            while (superType != BuiltIn.VOID) {
                if (superType.equals(other)) return true;
                superType = superType.getElementType();
            }
            return false;
        }

        @Override
        public int getFieldCount() {
            int count = 0;
            for (Symbol member : getMJMembers()) {
                if (member.getMJKind().isField()) count++;
            }
            return count;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof MJType)) return false;
            MJType otherType = (MJType) other;

            if (kind.isArray()) {
                return otherType.kind.isArray() && otherType.getElementType().equals(this.getElementType());
            }

            if (kind.isClass()) {
                return otherType.kind.isClass() && otherType.getMembersTable().equals(this.getMembersTable());
            }

            return this == other;
        }

        @Override
        public Struct getStruct() {
            return this;
        }
    }

    class LegacyType implements Type {
        private final Struct legacy;
        private final String name;
        private final TypeKind kind;

        public LegacyType(Struct legacy, String name) {
            this.legacy = legacy;
            this.kind = TypeKind.normalize(legacy.getKind());
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public TypeKind getMJKind() {
            return kind;
        }

        @Override
        public void setElementType(Type elementType) {
        }

        @Override
        public Type getElementType() {
            return BuiltIn.VOID;
        }

        @Override
        public void setMembers(SymbolDataStructure members) {
        }

        @Override
        public Collection<Symbol> getMJMembers() {
            return Collections.emptyList();
        }

        @Override
        public Symbol findMember(String name) {
            return BuiltIn.NO_OBJECT;
        }

        @Override
        public boolean isCompatibleWith(Type other) {
            return this == other;
        }

        @Override
        public boolean isAssignableTo(Type other) {
            return this == other;
        }

        @Override
        public boolean extendsFrom(Type other) {
            return false;
        }

        @Override
        public void accept(SymbolTableVisitor visitor) {
            visitor.visitStructNode(legacy);
        }

        @Override
        public int getFieldCount() {
            return 0;
        }

        @Override
        public Struct getStruct() {
            return legacy;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof LegacyType && ((LegacyType) obj).legacy == legacy;
        }
    }
}
