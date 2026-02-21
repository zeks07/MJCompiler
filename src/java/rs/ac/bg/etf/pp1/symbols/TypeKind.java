package rs.ac.bg.etf.pp1.symbols;

import rs.ac.bg.etf.pp1.codegen.Bytecodes;

public enum TypeKind {
    NONE(Bytecodes.voidCode),
    INT(Bytecodes.intCode),
    CHAR(Bytecodes.charCode),
    ARRAY(Bytecodes.objectCode),
    CLASS(Bytecodes.objectCode),
    BOOL(Bytecodes.byteCode),
    ENUM(Bytecodes.voidCode),
    INTERFACE(Bytecodes.objectCode);

    int typecode;

    TypeKind(int typecode) {
        this.typecode = typecode;
    }

    public boolean isArray() {
        return this == ARRAY;
    }

    public boolean isClass() {
        return this == CLASS;
    }

    public boolean isReferencable() {
        return this == ARRAY || this == CLASS || this == INTERFACE;
    }

    public boolean isExtendable() {
        return this == CLASS || this == INTERFACE || this == NONE;
    }

    public boolean isPrimitive() {
        return this == INT || this == CHAR || this == BOOL;
    }

    public int getTypecode() {
        return typecode;
    }

    public static TypeKind normalize(int kind) {
        if (kind < 0 || kind >= values().length) {
            throw new IllegalArgumentException("Invalid type kind: " + kind);
        }
        return values()[kind];
    }

    public int toLegacy() {
        return ordinal();
    }
}
