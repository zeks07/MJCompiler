package rs.ac.bg.etf.pp1.ir.node;


import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public final class DivNode extends BinaryNode {
    public DivNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public IRType compute() {
        if (left().type instanceof TypeInteger && right().type instanceof TypeInteger) {
            if (left().type.isConstant() && right().type.isConstant()) {
                long leftValue = ((TypeInteger) left().type).value();
                long rightValue = ((TypeInteger) right().type).value();

                if (rightValue == 0) {
                    // TODO
                }
                return TypeInteger.constant(leftValue / rightValue);
            }
        }
        return IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        Node right = right();
        // Division by 1
        if (right.type.isConstant() && right.type instanceof TypeInteger && ((TypeInteger) right.type).value() == 1)
            return left();
        return null;
    }
}
