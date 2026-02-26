package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Node {
    public final int id;
    public final ArrayList<Node> inputs;
    public final ArrayList<Node> outputs;
    public IRType type;

    private static int uniqueId = 0;

    protected Node(Node... inputs) {
        this.id = uniqueId++;
        this.inputs = new ArrayList<>();
        Collections.addAll(this.inputs, inputs);
        this.outputs = new ArrayList<>();
        for (Node input : inputs) {
            if (input != null) input.outputs.add(this);
        }
    }

    public Node in(int i) {
        return inputs.get(i);
    }

    public int inSize() {
        return inputs.size();
    }

    public int outSize() {
        return outputs.size();
    }

    public boolean isUnused() {
        return outSize() == 0;
    }

    public boolean isCFG() {
        return false;
    }

    Node setDefinition(int idx, Node newDefinition) {
        Node oldDefinition = in(idx);
        if (oldDefinition == newDefinition) return this;

        if (newDefinition != null) newDefinition.addUse(this);

        if (oldDefinition != null && oldDefinition.deleteUse(this)) oldDefinition.kill();

        inputs.set(idx, newDefinition);
        return newDefinition;
    }

    Node addDefinition(Node newDefinition) {
        inputs.add(newDefinition);
        if (newDefinition != null) newDefinition.addUse(this);
        return newDefinition;
    }

    protected <N extends Node> N addUse(Node node) {
        outputs.add(node);
        return (N) this;
    }

    protected boolean deleteUse(Node use) {
        outputs.remove(use);
        return outputs.isEmpty();
    }

    void popN(int n) {
        for(int i = 0; i < n; i++) {
            Node oldDefinition = inputs.remove(inputs.size() - 1);
            if (oldDefinition != null && oldDefinition.deleteUse(this)) oldDefinition.kill();
        }
    }

    public void kill() {
        assert isUnused();
        for (int i = 0; i < inputs.size(); i++) {
            setDefinition(i, null);
        }
        inputs.clear();
        type = null;
        assert isDead();
    }

    boolean isDead() {
        return isUnused() && inSize() == 0 && type == null;
    }

    public <N extends Node> N keep() {
        return addUse(null);
    }

    public <N extends Node> N unkeep() {
        deleteUse(null);
        return (N) this;
    }

    public final Node peephole() {
        IRType type = this.type = compute();

        if (!(this instanceof ConstantNode) && type.isConstant()) {
            kill();
            return deadCodeElimination(new ConstantNode(type).peephole());
        }

        Node node = idealize();
        return node != null ? node : this;
    }

    public abstract IRType compute();

    public abstract Node idealize();

    private Node deadCodeElimination(Node node) {
        if (node != this && isUnused()) {
            node.keep();
            kill();
            node.unkeep();
        }
        return node;
    }
}
