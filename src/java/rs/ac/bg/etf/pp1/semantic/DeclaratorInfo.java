package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.symbols.Type;

public final class DeclaratorInfo {
    public final String name;
    public final Type type;
    public final boolean isArray;
    public final boolean hasInitializer;
    public final int value;


    public DeclaratorInfo(Type type, String name) {
        this(type, name, false, false, 0);
    }

    public DeclaratorInfo(Type type, String name, boolean isArray) {
        this(type, name, isArray, false, 0);
    }

    public DeclaratorInfo(Type type, String name, boolean hasInitializer, int value) {
        this(type, name, false, hasInitializer, value);
    }

    public DeclaratorInfo(
            Type type, String name,
            boolean isArray,
            boolean hasInitializer,
            int value
    ) {
        this.name = name;
        this.type = type;
        this.isArray = isArray;
        this.hasInitializer = hasInitializer;
        this.value = value;
    }
}
