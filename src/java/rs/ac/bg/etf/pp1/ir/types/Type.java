package rs.ac.bg.etf.pp1.ir.types;

import java.util.HashMap;

public abstract class Type {

    static final HashMap<Type, Type> INTERN = new HashMap<>();

    @SuppressWarnings("unchecked")
    protected <T extends Type> T intern() {
        T nnn = (T) INTERN.get(this);
        if (nnn == null) INTERN.put(nnn = (T) this, this);
        return nnn;
    }

    public boolean isSimple() {
        return false;
    }

    public boolean isConstant() {
        return false;
    }

    public boolean isHighOrConstant() {
        return false;
    }

    /**
     * Types should meet at the first common type when moving downwards.
     * <pre><code>
     *                TOP
     *    ┌────────────┼──────────────────────┐
     * CONTROL      TOP:int               TOP:tuple
     *    │            │                      │
     *    │            │              ┌───────┼─── ┄ ───┐
     *    │   ┌─ ┄ ─┬──┼──┬─ ┄ ─┐   [type]  [type]    [type]
     *    │  -n ... -1 0  1 ... n           [type]     ...
     *    │   └─ ┄ ─┴──┼──┴─ ┄ ─┘                     [type]
     *    │            │              └───────┼─── ┄ ───┘
     *    │            │                      │
     * CONTROL         │                      │
     * BOTTOM      BOTTOM:int            BOTTOM:tuple
     *    └────────────┼──────────────────────┘
     *               BOTTOM
     * </code></pre>
     */
    final public Type meet(Type other) {
        if (this == other) return this;

        if (this.getClass() == other.getClass()) return meetSameType(other);

        if (isSimple()) return this.meetSameType(other);
        if (other.isSimple()) return other.meetSameType(this);

        return Types.BOTTOM;
    }

    public final Type join(Type other) {
        if (this == other) return this;
        return dual().meet(other.dual()).dual();
    }

    public Type dual() {
        if (this == Types.BOTTOM) return Types.TOP;
        if (this == Types.TOP) return Types.BOTTOM;
        if (this == Types.CONTROL) return Types.X_CONTROL;
        if (this == Types.X_CONTROL) return Types.CONTROL;

        throw new RuntimeException("Unknown type: " + this);
    }

    abstract Type meetSameType(Type other);

    public boolean isA(Type other) {
        return meet(other) == other;
    }

    private int hash;

    @Override
    public int hashCode() {
        if (hash != 0) return hash;
        hash = hash();
        if (hash == 0) hash = 0xDEADBEEF;
        return hash;
    }

    protected int hash() {
        return 0;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Type)) return false;
        return eq((Type) obj);
    }

    protected boolean eq(Type other) {
        return true;
    }

    final static class SimpleType extends Type {
        private final boolean isConstant;

        SimpleType() {
            this(false);
        }

        SimpleType(boolean isConstant) {
            this.isConstant = isConstant;
        }

        @Override
        public boolean isSimple() {
            return true;
        }

        @Override
        public boolean isHighOrConstant() {
            return isConstant;
        }

        @Override
        Type meetSameType(Type other) {
            if (this == Types.BOTTOM || other == Types.TOP) return this;
            if (this == Types.TOP || other == Types.BOTTOM) return other;

            if (!other.isSimple()) return Types.BOTTOM;

            return this == Types.CONTROL || other == Types.CONTROL ? this : Types.X_CONTROL;
        }
    }

}
