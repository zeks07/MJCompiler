package rs.ac.bg.etf.pp1.ir.node;

public abstract class BinaryNode extends Node {
    public BinaryNode(Node left, Node right) {
        super(null, left, right);
    }

    public final Node left() {
        return inputs.get(1);
    }

    public final Node right() {
        return inputs.get(2);
    }

    final Node swap() {
        Node tmp = left();
        inputs.set(1, right());
        inputs.set(2, tmp);
        return this;
    }

    abstract Node copy(Node left, Node right);

    final Node phiConstant(boolean rotate) {
        Node left = left();
        Node right = right();

        PhiNode phiLeft = phiOfConstants(left);
        if (rotate && phiLeft != null && left.inSize() > 2) {
            if (left.getClass() != right.getClass()) return null;
            phiLeft = phiOfConstants(left.in(2));
        }

        if (phiLeft == null) return null;

        if (!(right instanceof ConstantNode) && phiOfConstants(right) == null) return null;

        if (right instanceof PhiNode && phiLeft.region() != ((PhiNode) right).region()) return null;

        Node[] nodes = new Node[phiLeft.inSize()];
        nodes[0] = phiLeft.region();

        for (int i = 1; i < phiLeft.inSize(); i++) {
            nodes[i] = copy(phiLeft.in(i),right instanceof PhiNode ? right.in(i) : right).peephole();
        }

        Node phi = new PhiNode(nodes).peephole();

        return left == phiLeft ? phi : copy(left.in(1), phi);
    }

    private static PhiNode phiOfConstants(Node node) {
        return node instanceof PhiNode && node.hasOnlyConstants() ? (PhiNode) node : null;
    }
}
