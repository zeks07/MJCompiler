package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;

public final class PhiNode extends Node {
    public PhiNode(Node... inputs) {
        super(inputs);
    }

    Node region() {
        return inputs.get(0);
    }

    @Override
    public IRType compute() {
        return IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        if (hasSameInputs()) return in(1);

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

    private boolean hasSameInputs() {
        for (int i = 2; i < inSize(); i++) {
            if (in(i) != in(1)) return false;
        }
        return true;
    }
}
