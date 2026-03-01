package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;
import rs.ac.bg.etf.pp1.ir.types.TypeTuple;

public final class StartNode extends CFGNode {
    final TypeTuple args;

    public StartNode(Type[] args) {
        super();
        this.args = TypeTuple.make(args);
        type = this.args;
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
