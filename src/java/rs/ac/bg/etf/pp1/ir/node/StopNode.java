package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;

public final class StopNode extends Node {
    public StopNode(Node... inputs) {
        super(inputs);
    }

    public ReturnNode ret() {
        return inSize() == 1 ? (ReturnNode) inputs.get(0) : null;
    }

    @Override
    public boolean isCFG() {
        return true;
    }

    @Override
    public IRType compute() {
        return IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        return null;
    }

    public Node addReturn(Node node) {
        return addDefinition(node);
    }
}
