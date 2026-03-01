package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public final class NegNode extends Node {
    public NegNode(Node operand) {
        super(null, operand);
    }

    public Node operand() {
        return inputs.get(1);
    }

    @Override
    public Type compute() {
        if (!(operand().type instanceof TypeInteger)) {
            return TypeInteger.TOP.meet(operand().type);
        }

        return operand().type.isConstant()
                ? TypeInteger.constant(-((TypeInteger) operand().type).value())
                : operand().type;
    }

    @Override
    public Node idealize() {
        return null;
    }
}
