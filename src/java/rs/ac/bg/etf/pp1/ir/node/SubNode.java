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

        if (left().type instanceof TypeInteger && right().type instanceof TypeInteger) {
            if (left().type.isConstant() && right().type.isConstant()) {
                return TypeInteger.constant(((TypeInteger) left().type).value() - ((TypeInteger) right().type).value());
            }
        }
        return Types.BOTTOM;
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
