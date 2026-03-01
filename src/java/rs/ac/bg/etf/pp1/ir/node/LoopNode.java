package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;

public final class LoopNode extends MergeNode {
    public LoopNode(Node entry) {
        super(null, entry, null);
    }

    Node entry() {
        return in(1);
    }

    Node back() {
        return in(2);
    }

    @Override
    public boolean inProgress() {
        return back() != null;
    }

    @Override
    public Type compute() {
        return inProgress() ? Types.CONTROL : super.compute();
    }

    @Override
    protected boolean isEntryPath(int path) {
        return entry() == in(path);
    }

    @Override
    Node immediateDominator() {
        return entry();
    }
}
