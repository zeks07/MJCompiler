package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public final class NegNode extends Node {
    public NegNode(Node operand) {
        super(null, operand);
    }

    public Node operand() {
        return inputs.get(1);
    }

    @Override
    public IRType compute() {
        if (operand().type instanceof TypeInteger) {
            return operand().type.isConstant()
                    ? TypeInteger.constant(-((TypeInteger) operand().type).value())
                    : operand().type;
        }
        return IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        return null;
    }
}
