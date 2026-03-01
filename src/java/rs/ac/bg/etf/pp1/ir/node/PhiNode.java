package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;

public final class PhiNode extends Node {
    public PhiNode(Node... inputs) {
        super(inputs);
    }

    Node region() {
        return inputs.get(0);
    }

    @Override
    public Type compute() {
        if (!(region() instanceof MergeNode))
            return region().type == Types.X_CONTROL ? Types.TOP : type;

        MergeNode merge = (MergeNode) region();
        if (merge.inProgress())
            return Types.BOTTOM;

        Type t = Types.TOP;
        for (int i = 1; i < inSize(); i++) {
            if (merge.in(i).addDependency(this).type != Types.X_CONTROL && in(i) != this) t = t.meet(in(i).type);
        }

        return t;
    }

    @Override
    public Node idealize() {
        if (hasSameInputs())
            return in(1);

        if (!(region() instanceof MergeNode) || ((MergeNode) region()).inProgress())
            return null;

        Node live = singleUniqueInput();
        if (live != null)
            return live;

        // Change Phi(op(A, X), op(B, Y), op(C, Z)) to op(Phi(A, B, C), Phi(X, Y, Z)) (pulling down)
        Node op = in(1);
        if (op.inSize() == 3 && op instanceof BinaryNode && hasSameOperators()) {
            Node[] lefts = new Node[inSize()];
            Node[] rights = new Node[inSize()];
            lefts[0] = rights[0] = region();

            for (int i = 1; i < inSize(); i++) {
                lefts[i] = in(i).in(1);
                rights[i] = in(i).in(2);
            }

            Node phiLeft = new PhiNode(lefts).peephole();
            Node phiRight = new PhiNode(rights).peephole();
            return ((BinaryNode) op).copy(phiLeft, phiRight);
        }

        return null;
    }

    private boolean hasSameOperators() {
        for (int i = 2; i < inSize(); i++) {
            if (in(1).getClass() != in(i).getClass()) return false;
        }
        return true;
    }

    private Node singleUniqueInput() {
        if (region() instanceof LoopNode && ((LoopNode) region()).entry().type == Types.X_CONTROL)
            return null;

        Node live = null;
        for (int i = 1; i < inSize(); i++) {
            if (region().in(i).type != Types.X_CONTROL && in(i) != this) {
                if (live == null || live == in(i)) live = in(i);
                else return null;
            }
        }
        return live;
    }

    private boolean hasSameInputs() {
        for (int i = 2; i < inSize(); i++) {
            if (in(i) != in(1)) return false;
        }
        return true;
    }

    @Override
    public boolean hasOnlyConstants(Node dependency) {
        if (!(region() instanceof MergeNode) || ((MergeNode) region()).inProgress())
            return false;

        addDependency(dependency);
        if (((MergeNode) region()).inProgress())
            return false;

        return super.hasOnlyConstants(dependency);
    }

    private boolean inProgress() {
        return in(inSize() - 1) == null;
    }

    @Override
    boolean eq(Node node) {
        return !inProgress();
    }
}
