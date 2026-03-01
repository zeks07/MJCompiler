package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public final class SubNode extends BinaryNode {
    public SubNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public Type compute() {
        if (left() == right())
            return TypeInteger.ZERO;

        if (!computable())
            return Types.BOTTOM;

        long leftValue = ((TypeInteger) left().type).value();
        long rightValue = ((TypeInteger) right().type).value();

        return TypeInteger.constant(leftValue - rightValue);
    }

    @Override
    public Node idealize() {
        Node left = left();
        Type rightType = right().type;

        // Subtraction by 0
        if (rightType instanceof TypeInteger && ((TypeInteger) rightType).value() == 0)
            return left;

        return null;
    }

    @Override
    Node copy(Node left, Node right) {
        return new SubNode(left, right);
    }
}
