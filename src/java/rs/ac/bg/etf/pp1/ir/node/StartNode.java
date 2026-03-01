package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.TypeTuple;

public final class StartNode extends MultiNode {
    final TypeTuple args;

    public StartNode(Type[] args) {
        super();
        this.args = TypeTuple.make(args);
        type = this.args;
    }

    public Node control() {
        return in(0);
    }

    @Override
    public boolean isCFG() {
        return true;
    }

    @Override
    public Type compute() {
        return args;
    }

    @Override
    public Node idealize() {
        return null;
    }

    @Override
    Node immediateDominator() {
        return null;
    }
}
