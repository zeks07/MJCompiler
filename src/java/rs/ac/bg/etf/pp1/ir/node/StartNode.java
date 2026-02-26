package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;
import rs.ac.bg.etf.pp1.ir.types.TypeTuple;

public final class StartNode extends Node {
    final TypeTuple args;

    public StartNode(IRType[] args) {
        super();
        this.args = new TypeTuple(args);
        type = this.args;
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
}
