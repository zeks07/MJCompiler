package rs.ac.bg.etf.pp1.symbols;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import static rs.ac.bg.etf.pp1.symbols.Type.*;
import static rs.ac.bg.etf.pp1.symbols.Symbol.*;

import java.util.IdentityHashMap;
import java.util.Map;

public final class BuiltIn {
    private static final Map<Struct, Type> structCache = new IdentityHashMap<>();
    private static final IdentityHashMap<Obj, Symbol> objCache = new IdentityHashMap<>();

    public static final Type VOID = new LegacyType(Tab.noType, "void");
    public static final Type INT = new LegacyType(Tab.intType, "int");
    public static final Type BOOLEAN = new MJType(TypeKind.BOOL, "bool");
    public static final Type CHAR = new LegacyType(Tab.charType, "char");
    public static final Type NULL = new LegacyType(Tab.nullType, "null");

    public static final Symbol NO_OBJECT = new LegacySymbol(Tab.noObj);
    public static final Symbol CHR_OBJECT = new LegacySymbol(Tab.chrObj);
    public static final Symbol ORD_OBJECT = new LegacySymbol(Tab.ordObj);
    public static final Symbol LEN_OBJECT = new LegacySymbol(Tab.lenObj);

    public static void init() {
        structCache.put(Tab.noType, VOID);
        structCache.put(Tab.intType, INT);
        structCache.put(Tab.charType, CHAR);
        structCache.put(Tab.nullType, NULL);

        objCache.put(Tab.noObj, NO_OBJECT);
        objCache.put(Tab.chrObj, CHR_OBJECT);
        objCache.put(Tab.ordObj, ORD_OBJECT);
        objCache.put(Tab.lenObj, LEN_OBJECT);
    }

    public static Symbol getBuiltIn(Obj obj) {
        return objCache.get(obj);
    }

    public static Type getBuiltIn(Struct structure) {
        return structCache.get(structure);
    }

    public static Type arrayOf(Type baseType) {
        return new MJType(TypeKind.ARRAY, baseType);
    }
}
