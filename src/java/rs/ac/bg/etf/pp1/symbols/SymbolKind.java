package rs.ac.bg.etf.pp1.symbols;

public enum SymbolKind {
    CONSTANT(0),
    VARIABLE(1),
    TYPE(2),
    METHOD(3),
    FIELD(4),
    ELEMENT(5),
    PROGRAM(6),
    NONE(-1);

    final int legacyKind;

    SymbolKind(int legacyKind) {
        this.legacyKind = legacyKind;
    }

    public boolean isConstant() {
        return this == CONSTANT;
    }

    public boolean isField() {
        return this == FIELD;
    }

    public boolean isVariable() {
        return this == VARIABLE;
    }

    public boolean isCallable() {
        return this == METHOD;
    }

    public boolean isType() {
        return this == TYPE;
    }

    public boolean isDataStorage() {
        return this == CONSTANT || this == VARIABLE || this == FIELD;
    }

    public boolean isAssignable() {
        return this == VARIABLE || this == FIELD || this == ELEMENT;
    }

    public int toLegacy() {
        return legacyKind;
    }

    public static SymbolKind normalize(int kind) {
        switch (kind) {
            case 0: return CONSTANT;
            case 1: return VARIABLE;
            case 2: return TYPE;
            case 3: return METHOD;
            case 4: return FIELD;
            case 5: return ELEMENT;
            default: throw new IllegalArgumentException("Invalid symbol kind: " + kind);
        }
    }
}
