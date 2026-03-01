package rs.ac.bg.etf.pp1.ir.types;

public final class TypeTuple extends Type {
    public static final TypeTuple IF_BOTH = new TypeTuple(Types.CONTROL, Types.CONTROL);
    public static final TypeTuple IF_NEITHER = new TypeTuple(Types.X_CONTROL, Types.X_CONTROL);
    public static final TypeTuple IF_TRUE = new TypeTuple(Types.CONTROL, Types.X_CONTROL);
    public static final TypeTuple IF_FALSE = new TypeTuple(Types.X_CONTROL, Types.CONTROL);

    public final Type[] types;

    private TypeTuple(Type... types) {
        this.types = types;
    }

    public static TypeTuple make(Type... types) {
        return new TypeTuple(types).intern();
    }

    @Override
    public Type meetSameType(Type other) {
        throw new RuntimeException("Not implemented");
    }
}
