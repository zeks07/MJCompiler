package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public abstract class BooleanNode extends BinaryNode {
    public BooleanNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public Type compute() {
        if (left().type instanceof TypeInteger && right().type instanceof TypeInteger) {
            if (left().type.isConstant() && right().type.isConstant()) {
                return TypeInteger.constant(doOperation(((TypeInteger) left().type).value(), ((TypeInteger) right().type).value()) ? 1 : 0);
            }
            return left().type.meet(right().type);
        }
        return Types.BOTTOM;
    }

    abstract boolean doOperation(long left, long right);

    @Override
    public Node idealize() {
        if (left() == right()) {
            return new ConstantNode(TypeInteger.constant(doOperation(0, 0) ? 1 : 0));
        }

        return phiConstant(false);
    }

    public static class EQ extends BooleanNode { public EQ(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left == right;} @Override Node copy(Node left, Node right) { return new EQ(left, right);} }
    public static class NE extends BooleanNode { public NE(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left != right;} @Override Node copy(Node left, Node right) { return new NE(left, right);} }
    public static class LT extends BooleanNode { public LT(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left < right;} @Override Node copy(Node left, Node right) { return new LT(left, right);} }
    public static class LE extends BooleanNode { public LE(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left <= right;} @Override Node copy(Node left, Node right) { return new LE(left, right);} }
    public static class GT extends BooleanNode { public GT(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left > right;} @Override Node copy(Node left, Node right) { return new GT(left, right);} }
    public static class GE extends BooleanNode { public GE(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left >= right;} @Override Node copy(Node left, Node right) { return new GE(left, right);} }
}
