package rs.ac.bg.etf.pp1.ir.types;

public final class TypeTuple extends IRType {
    public final IRType[] types;

    public TypeTuple(IRType... types) {
        this.types = types;
    }

    @Override
    public IRType meet(IRType other) {
        throw new RuntimeException("Not implemented");
    }
}
