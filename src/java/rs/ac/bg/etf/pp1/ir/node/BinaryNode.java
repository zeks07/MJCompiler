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
}
