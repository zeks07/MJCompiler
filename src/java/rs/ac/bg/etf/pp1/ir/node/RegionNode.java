package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;

public final class RegionNode extends Node {
    public RegionNode(Node... inputs) {
        super(inputs);
    }

    @Override
    public boolean isCFG() {
        return true;
    }

    @Override
    public IRType compute() {
        return IRTypes.CONTROL;
    }

    @Override
    public Node idealize() {
        return null;
    }
}
