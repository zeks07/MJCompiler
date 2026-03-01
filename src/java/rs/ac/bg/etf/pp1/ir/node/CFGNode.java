package rs.ac.bg.etf.pp1.ir.node;

public abstract class CFGNode extends Node {
    public CFGNode(Node... inputs) {
        super(inputs);
    }

    public final Node control() {
        return inputs.get(0);
    }

    @Override
    public final boolean isCFG() {
        return true;
    }
}
