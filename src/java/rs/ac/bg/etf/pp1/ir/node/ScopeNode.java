package rs.ac.bg.etf.pp1.ir.node;


import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;

import java.util.HashMap;
import java.util.Stack;

public final class ScopeNode extends Node {
    public final Stack<HashMap<String, Integer>> scopes;

    public ScopeNode() {
        scopes = new Stack<>();
        type = IRTypes.BOTTOM;
    }

    public Node control() {
        return in(0);
    }

    public Node setControl(Node node) {
        return setDefinition(0, node);
    }

    @Override
    public IRType compute() {
        return IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        return null;
    }

    /**
     * Pushes a new scope.
     * </p>
     * Used when opening new blocks of statements.
     */
    public void push() {
        scopes.push(new HashMap<>());
    }

    /**
     * Pops the last scope.
     * </p>
     * Used when closing blocks of statements.
     */
    public void pop() {
        popN(scopes.pop().size());
    }

    public Node define(String name, Node node) {
        HashMap<String, Integer> symbols = scopes.lastElement();
        if (symbols.put(name, inSize()) != null) return null;
        return addDefinition(node);
    }

    public Node lookup(String name) {
        return update(name, null, scopes.size() - 1);
    }

    public Node update(String name, Node node) {
        return update(name, node, scopes.size() - 1);
    }

    public Node update(String name, Node node, int nestingLevel) {
        if (nestingLevel < 0) return null;
        HashMap<String, Integer> symbols = scopes.get(nestingLevel);
        Integer index = symbols.get(name);
        if (index == null) return update(name, node, nestingLevel - 1);
        Node old = in(index);
        return node == null ? old : setDefinition(index, node);
    }

    public ScopeNode duplicate() {
        ScopeNode duplicate = new ScopeNode();
        for (HashMap<String, Integer> symbols : scopes) {
            duplicate.scopes.push(new HashMap<>(symbols));
        }
        duplicate.addDefinition(control());
        for (int i = 1; i < inSize(); i++) {
            duplicate.addDefinition(in(i));
        }
        return duplicate;
    }

    public Node mergeScopes(ScopeNode other) {
        RegionNode region = (RegionNode) setControl(new RegionNode(null, control(), other.control()).peephole());
        for (int i = 1; i < inSize(); i++) {
            if (in(i) != other.in(i)) setDefinition(i, new PhiNode(region, in(i), other.in(i)).peephole());
        }
        other.kill();
        return region;
    }
}
