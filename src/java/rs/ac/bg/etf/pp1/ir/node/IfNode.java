package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.TypeTuple;

public final class IfNode extends MultiNode {
    public IfNode(Node control, Node predicate) {
        super(control, predicate);
    }

    @Override
    public boolean isCFG() {
        return true;
    }

    public Node control() {
        return in(0);
    }

    public Node predicate() {
        return in(1);
    }

    @Override
    public IRType compute() {
        return TypeTuple.IF;
    }

    @Override
    public Node idealize() {
        return null;
    }
}
