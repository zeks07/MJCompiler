package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public final class SubNode extends BinaryNode {
    public SubNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public IRType compute() {
        if (left().type instanceof TypeInteger && right().type instanceof TypeInteger) {
            if (left().type.isConstant() && right().type.isConstant()) {
                return TypeInteger.constant(((TypeInteger) left().type).value() - ((TypeInteger) right().type).value());
            }
        }
        return IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        Node left = left();
        IRType rightType = right().type;

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
