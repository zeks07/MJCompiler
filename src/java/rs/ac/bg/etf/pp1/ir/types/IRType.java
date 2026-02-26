package rs.ac.bg.etf.pp1.ir.types;

public abstract class IRType {

    public boolean isSimple() {
        return false;
    }

    public boolean isConstant() {
        return false;
    }

    public IRType meet(IRType other) {
        return IRTypes.BOTTOM;
    }

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
    }

}
