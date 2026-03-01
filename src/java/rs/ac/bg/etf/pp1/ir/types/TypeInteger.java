package rs.ac.bg.etf.pp1.ir.types;

public final class TypeInteger extends Type {
    public final static TypeInteger TOP = make(false, 0);
    public final static TypeInteger BOTTOM = make(false, 1);
    public final static TypeInteger ZERO = make(true, 0);

    private final boolean isConstant;
    private final long constant;

    private TypeInteger(boolean isConstant, long constant) {
        this.isConstant = isConstant;
        this.constant = constant;
    }

    public static TypeInteger make(boolean isConstant, long constant) {
        return new TypeInteger(isConstant, constant);
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
    public Type meetSameType(Type other) {
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
    public Type dual() {
        if (isConstant) return this;
        return constant == 0 ? BOTTOM : TOP;
    }

    @Override
    protected boolean eq(Type other) {
        TypeInteger otherInt = (TypeInteger) other;
        return constant == otherInt.constant && isConstant == otherInt.isConstant;
    }
}
