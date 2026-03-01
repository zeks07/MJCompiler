package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;

public abstract class MergeNode extends CFGNode {
    protected Node idom;

    public MergeNode(Node... inputs) {
        super(inputs);
    }

    public abstract boolean inProgress();

    @Override
    public Type compute() {
        Type type = Types.X_CONTROL;
        for (int i = 1; i < inSize(); i++) {
            type = type.meet(in(i).type);
        }
        return type;
    }

    @Override
    public Node idealize() {
        if (inProgress())
            return null;

        int path = findDeadInput();
        if (path != 0 && isEntryPath(path)) {
            int outSize = 0;
            while (outSize != outSize()) {
                outSize = outSize();
                for (int i = 0; i < outSize(); i++) {
                    if (out(i) instanceof PhiNode && out(i).inSize() == inSize()) out(i).deleteDefinition(path);
                }
            }
            idom = null;
            return isDead() ? new ConstantNode(Types.X_CONTROL) : deleteDefinition(path);
        }

        if (inSize() == 2 && !hasPhi()) {
            idom = null;
            return in(1);
        }

        return null;
    }

    protected int findDeadInput() {
        for (int i = 1; i < inSize(); i++) {
            if (in(i).type == Types.X_CONTROL) return i;
        }
        return 0;
    }

    protected boolean hasPhi() {
        for (Node phi : outputs) {
            if (phi instanceof PhiNode) return true;
        }
        return false;
    }

    @Override
    boolean eq(Node node) {
        return !inProgress();
    }

    protected boolean isEntryPath(int path) {
        return false;
    }
}
