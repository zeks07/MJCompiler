package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;
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
        return index == 0 || control() instanceof IfNode;
    }

    public MultiNode control() {
        return (MultiNode) inputs.get(0);
    }

    @Override
    public Type compute() {
        Type type = control().type;
        return type instanceof TypeTuple ? ((TypeTuple) type).types[index] : Types.BOTTOM;
    }

    @Override
    public Node idealize() {
        if (control().type instanceof TypeTuple && ((TypeTuple) control().type).types[1-index] == Types.X_CONTROL)
            return control().in(0);

        return null;
    }

    @Override
    boolean eq(Node node) {
        return index == ((ProjectionNode) node).index;
    }

    @Override
    protected int hash() {
        return index;
    }
}
