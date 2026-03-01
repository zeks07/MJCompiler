package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.IterPeeps;
import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;

public final class StopNode extends CFGNode {
    public StopNode(Node... inputs) {
        super(inputs);
    }

    public ReturnNode ret() {
        return inSize() == 1 ? (ReturnNode) inputs.get(0) : null;
    }

    public Node addReturn(Node node) {
        return addDefinition(node);
    }

    @Override
    public Type compute() {
        return Types.BOTTOM;
    }

    @Override
    public Node idealize() {
        int size = inSize();
        for (int i = 0; i < size; i++) {
            if (in(i).type == Types.X_CONTROL) deleteDefinition(i--);
        }
        if (size != inSize()) return this;

        return null;
    }

    public StopNode iterate() {
        return IterPeeps.iterate(this);
    }
}
