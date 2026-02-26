package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;
import rs.ac.bg.etf.pp1.ir.types.TypeTuple;

public final class ProjectionNode extends Node {
    // Which slice of the incoming multipart value
    public final int index;

    public ProjectionNode(MultiNode control, int index) {
        super(control);
        this.index = index;
    }

    @Override
    public boolean isCFG() {
        return index == 0;
    }

    public MultiNode control() {
        return (MultiNode) inputs.get(0);
    }

    @Override
    public IRType compute() {
        IRType type = control().type;
        return type instanceof TypeTuple ? ((TypeTuple) type).types[index] : IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        return null;
    }
}
