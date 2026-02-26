package rs.ac.bg.etf.pp1.ir.types;

public final class TypeInteger extends IRType {
    public final static TypeInteger TOP = new TypeInteger(false, 0);
    public final static TypeInteger BOTTOM = new TypeInteger(false, 1);
    public final static TypeInteger ZERO = new TypeInteger(true, 0);

    private final boolean isConstant;
    private final long constant;

    public TypeInteger(boolean isConstant, long constant) {
        this.isConstant = isConstant;
        this.constant = constant;
    }

    public static TypeInteger constant(long constant) {
        return new TypeInteger(true, constant);
    }

    public boolean isTop() {
        return !isConstant && constant == 0;
    }

    public boolean isBottom() {
        return !isConstant && constant == 1;
    }

    @Override
    public boolean isConstant() {
        return isConstant;
    }

    public long value() {
        return constant;
    }

    @Override
    public IRType meet(IRType other) {
        if (this == other) return this;
        if (!(other instanceof TypeInteger)) return super.meet(other);

        TypeInteger otherInt = (TypeInteger) other;
        if (isBottom()) return this;
        if (otherInt.isBottom()) return otherInt;
        if (isTop()) return otherInt;
        if (otherInt.isTop()) return this;

        assert isConstant && otherInt.isConstant;
        return constant == otherInt.constant ? this : BOTTOM;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof TypeInteger)) return false;
        return constant == ((TypeInteger) obj).constant;
    }
}
