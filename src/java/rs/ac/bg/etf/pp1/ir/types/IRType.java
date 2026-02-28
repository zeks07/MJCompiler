package rs.ac.bg.etf.pp1.ir.types;

public abstract class IRType {

    public boolean isSimple() {
        return false;
    }

    public boolean isConstant() {
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
    final public IRType meet(IRType other) {
        if (this == other) return this;

        if (this.getClass() == other.getClass()) return meetSameType(other);

        if (isSimple()) return this.meetSameType(other);
        if (other.isSimple()) return other.meetSameType(this);

        return IRTypes.BOTTOM;
    }

    abstract IRType meetSameType(IRType other);

    final static class SimpleType extends IRType {
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
        public boolean isConstant() {
            return isConstant;
        }

        @Override
        IRType meetSameType(IRType other) {
            if (this == IRTypes.BOTTOM || other == IRTypes.TOP) return this;
            if (this == IRTypes.TOP || other == IRTypes.BOTTOM) return other;

            if (!other.isSimple()) return IRTypes.BOTTOM;

            return this == IRTypes.CONTROL || other == IRTypes.CONTROL ? this : IRTypes.X_CONTROL;
        }
    }

}
