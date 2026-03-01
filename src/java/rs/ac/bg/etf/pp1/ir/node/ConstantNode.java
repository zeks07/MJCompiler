package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;

public final class ConstantNode extends Node {
    final Type constant;

    public ConstantNode(Type type) {
        super();
        this.constant = type;
    }

    @Override
    public Type compute() {
        return constant;
    }

    @Override
    public Node idealize() {
        return null;
    }

    @Override
    boolean eq(Node node) {
        ConstantNode other = (ConstantNode) node;
        return constant == other.constant;
    }

    @Override
    protected int hash() {
        return constant.hashCode();
    }

    @Override
    Node immediateDominator() {
        return null;
    }
}
