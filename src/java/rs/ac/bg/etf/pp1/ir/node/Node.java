package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.IterPeeps;
import rs.ac.bg.etf.pp1.ir.types.Type;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;

public abstract class Node {
    public final int id;

    // definitions
    public final ArrayList<Node> inputs;
    // uses
    public final ArrayList<Node> outputs;

    public Type type;

    // unique id, 0 is reserved
    private static int uniqueId = 1;

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

    public Node out(int i) {
        return outputs.get(i);
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

    final Node addDefinition(Node newDefinition) {
        inputs.add(newDefinition);
        if (newDefinition != null) newDefinition.addUse(this);
        return newDefinition;
    }

    protected Node deleteDefinition(int index) {
        unlock();
        Node oldDefinition = in(index);
        if (oldDefinition != null && oldDefinition.deleteUse(this)) oldDefinition.kill();
        oldDefinition.moveDependenciesToWorklist();
        inputs.remove(index);
        return this;
    }

    final protected <N extends Node> N addUse(Node node) {
        outputs.add(node);
        return (N) this;
    }

    protected boolean deleteUse(Node use) {
        outputs.remove(use);
        return outputs.isEmpty();
    }

    final void popN(int n) {
        for(int i = 0; i < n; i++) {
            Node oldDefinition = inputs.remove(inputs.size() - 1);
            if (oldDefinition != null && oldDefinition.deleteUse(this)) oldDefinition.kill();
        }
    }

    final public void kill() {
        assert isUnused();
        for (int i = 0; i < inputs.size(); i++) {
            setDefinition(i, null);
        }
        inputs.clear();
        type = null;
        assert isDead();
    }

    public final boolean isDead() {
        return isUnused() && inSize() == 0 && type == null;
    }

    public <N extends Node> N keep() {
        return addUse(null);
    }

    public <N extends Node> N unkeep() {
        deleteUse(null);
        return (N) this;
    }

    public void subsume(Node nnn) {
        assert nnn != this;
        while(outSize() > 0) {
            Node n = outputs.remove(outSize() - 1);
            int index = inputs.indexOf(n);
            n.inputs.set(index, nnn);
            nnn.addUse(n);
        }
        kill();
    }

    public final Node peephole() {
        Type type = this.type = compute();

        if (!(this instanceof ConstantNode) && type.isConstant()) {
            kill();
            return deadCodeElimination(new ConstantNode(type).peephole());
        }

        Node node = idealize();
        return node != null ? node : this;
    }

    public final Node peepholeOptimization() {
        Type old = setType(compute());

        if (!(this instanceof ConstantNode) && type.isHighOrConstant())
            return new ConstantNode(type).peephole();

        if (hash == 0) {
            Node node = GVN.get(this);
            if (node == null) GVN.put(this, this);
            else {
                node.setType(node.type.join(type));
                hash = 0;
                return deadCodeElimination(node);
            }
        }

        Node node = idealize();
        if (node != null)
            return node;

        return old == type ? null : this;
    }

    // All nodes must implement these methods

    public abstract Type compute();

    public abstract Node idealize();

    private Node deadCodeElimination(Node node) {
        if (node != this && isUnused()) {
            node.keep();
            kill();
            node.unkeep();
        }
        return node;
    }

    @Deprecated
    public boolean hasOnlyConstants() {
        for (int i = 1; i < inSize(); i++) {
            if (!in(i).type.isConstant()) return false;
        }
        return true;
    }

    public boolean hasOnlyConstants(Node dependency) {
        for (int i = 1; i < inSize(); i++) {
            if (!in(i).type.isConstant()) {
                in(i).addDependency(dependency);
                return false;
            }
        }
        return true;
    }

    int depth;

    Node immediateDominator() {
        Node idom = in(0);
        if (idom.depth == 0) idom.immediateDominator();
        if (depth == 0) depth = idom.depth + 1;
        return idom;
    }

    // Used in iterative post-creation optimization

    public Type setType(Type type) {
        Type old = this.type;
        assert old == null || type.isA(old);
        if (old == type) return old;
        this.type = type;
        IterPeeps.addAll(outputs);
        moveDependenciesToWorklist();
        return old;
    }

    ArrayList<Node> dependencies;

    Node addDependency(Node dependency) {
        if (IterPeeps.midAssert()) return this;

        if(dependencies == null) dependencies = new ArrayList<>();
        if (dependencies.contains(dependency)) return this;
        if (inputs.contains(dependency)) return this;
        if (outputs.contains(dependency)) return this;

        dependencies.add(dependency);
        return this;
    }

    public void moveDependenciesToWorklist() {
        if (dependencies == null) return;
        IterPeeps.addAll(dependencies);
        dependencies.clear();
    }

    // Global Value Numbering
    public static final HashMap<Node, Node> GVN = new HashMap<>();

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        Node node = (Node) obj;
        int size = inSize();
        if (size != node.inSize()) return false;

        for (int i = 0; i < size; i++) {
            if (in(i) != node.in(i)) return false;
        }

        return eq(node);
    }

    boolean eq(Node node) {
        return true;
    }

    // Zero means not in GVN.
    int hash;

    final void unlock() {
        if (hash == 0) return;
        Node old = GVN.remove(this);
        assert old == this;
        hash = 0;
    }

    @Override
    public int hashCode() {
        if (hash != 0) return hash;
        int hash = hash();

        for (Node node : inputs) {
            if (node != null) hash = hash ^ (hash << 17) ^ (hash >> 13) ^ node.hashCode();
        }
        if (hash == 0) hash = 0xDEADBEEF;
        this.hash = hash;
        return hash;
    }

    protected int hash() {
        return 0;
    }

    private static final BitSet WALK_VISIT = new BitSet();

    final public <E> E walk(Function<Node, E> pred) {
        assert WALK_VISIT.isEmpty();
        E res = _walk(pred);
        WALK_VISIT.clear();
        return res;
    }

    private <E> E _walk(Function<Node, E> pred) {
        if (WALK_VISIT.get(id)) return null;
        WALK_VISIT.set(id);
        E res = pred.apply(this);
        if (res != null) return res;
        for (Node definition : inputs) if (definition != null && (res = definition._walk(pred)) != null) return res;
        for (Node use : outputs) if (use != null && (res = use._walk(pred)) != null) return res;
        return null;
    }

    public static void reset() {
        GVN.clear();
    }
}
