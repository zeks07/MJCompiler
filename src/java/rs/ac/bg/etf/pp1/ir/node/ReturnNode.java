package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.TypeTuple;
import rs.ac.bg.etf.pp1.ir.types.Types;

public final class ReturnNode extends CFGNode {
    public ReturnNode(Node control, Node data) {
        super(control, data);
    }

    public Node expression() {
        return inputs.get(1);
    }

    @Override
    public Type compute() {
        return TypeTuple.make(control().type, expression().type);
    }

    @Override
    public Node idealize() {
        if (control().type == Types.X_CONTROL)
            return control();

        return null;
    }
}
