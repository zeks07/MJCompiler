package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;
import rs.ac.bg.etf.pp1.ir.types.Types;

public final class ModNode extends BinaryNode {
    public ModNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public Type compute() {
        if (!computable())
            return Types.BOTTOM;

        long leftValue = ((TypeInteger) left().type).value();
        long rightValue = ((TypeInteger) right().type).value();

        if (rightValue == 0)
            return TypeInteger.ZERO;

        return TypeInteger.constant(leftValue % rightValue);
    }

    @Override
    public Node idealize() {
        Node right = right();

        if (!right.type.isConstant() || !(right.type instanceof TypeInteger))
            return null;

        TypeInteger rightInt = (TypeInteger) right.type;

        // Modulo by 1
        if (rightInt.value() == 1)
            return left();

        return null;
    }

    @Override
    Node copy(Node left, Node right) {
        return new ModNode(left, right);
    }
}
