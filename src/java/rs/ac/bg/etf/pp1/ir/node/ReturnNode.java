package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.TypeTuple;

public final class ReturnNode extends Node {
    public ReturnNode(Node control, Node data) {
        super(control, data);
    }

    public Node control() {
        return inputs.get(0);
    }

    public Node expression() {
        return inputs.get(1);
    }

    @Override
    public boolean isCFG() {
        return true;
    }

    @Override
    public IRType compute() {
        return new TypeTuple(control().type, expression().type);
    }

    @Override
    public Node idealize() {
        return null;
    }
}
