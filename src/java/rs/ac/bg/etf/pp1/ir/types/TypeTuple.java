package rs.ac.bg.etf.pp1.ir.types;

public final class TypeTuple extends IRType {
    public static final TypeTuple IF = new TypeTuple(IRTypes.CONTROL, IRTypes.CONTROL);

    public final IRType[] types;

    public TypeTuple(IRType... types) {
        this.types = types;
    }

    @Override
    public IRType meetSameType(IRType other) {
        throw new RuntimeException("Not implemented");
    }
}
