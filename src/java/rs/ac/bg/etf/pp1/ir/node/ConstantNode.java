package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;

public final class ConstantNode extends Node {
    final IRType constant;

    public ConstantNode(IRType type) {
        super();
        this.constant = type;
    }

    @Override
    public IRType compute() {
        return constant;
    }

    @Override
    public Node idealize() {
        return null;
    }
}
