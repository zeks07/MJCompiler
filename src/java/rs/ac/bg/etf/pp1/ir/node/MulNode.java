package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public final class MulNode extends BinaryNode {
    public MulNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public IRType compute() {
        if (left().type instanceof TypeInteger && right().type instanceof TypeInteger) {
            if (left().type.isConstant() && right().type.isConstant()) {
                return TypeInteger.constant(((TypeInteger) left().type).value() * ((TypeInteger) right().type).value());
            }
        }
        return IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        Node left = left();
        IRType leftType = left().type;
        IRType rightType = right().type;

        // Multiplication by 1
        if (rightType.isConstant() && rightType instanceof TypeInteger && ((TypeInteger) rightType).value() == 1)
            return left;

        // Move constants to right hand side
        if (leftType.isConstant() && !rightType.isConstant())
            return swap();

        return null;
    }
}
