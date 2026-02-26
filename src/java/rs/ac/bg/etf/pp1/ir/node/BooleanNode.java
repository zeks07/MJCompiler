package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public abstract class BooleanNode extends BinaryNode {
    public BooleanNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public IRType compute() {
        if (left().type instanceof TypeInteger && right().type instanceof TypeInteger) {
            if (left().type.isConstant() && right().type.isConstant()) {
                return TypeInteger.constant(doOperation(((TypeInteger) left().type).value(), ((TypeInteger) right().type).value()) ? 1 : 0);
            }
            return left().type.meet(right().type);
        }
        return IRTypes.BOTTOM;
    }

    abstract boolean doOperation(long left, long right);

    @Override
    public Node idealize() {
        if (left() == right()) {
            return new ConstantNode(TypeInteger.constant(doOperation(0, 0) ? 1 : 0));
        }
        return null;
    }

    public static class EQ extends BooleanNode { public EQ(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left == right;} }
    public static class NE extends BooleanNode { public NE(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left != right;} }
    public static class LT extends BooleanNode { public LT(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left < right;} }
    public static class LE extends BooleanNode { public LE(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left <= right;} }
    public static class GT extends BooleanNode { public GT(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left > right;} }
    public static class GE extends BooleanNode { public GE(Node left, Node right) { super(left, right);} @Override boolean doOperation(long left, long right) { return left >= right;} }
}
