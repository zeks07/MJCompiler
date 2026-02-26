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

    public Node control(Node node) {
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

    public void push() {
        scopes.push(new HashMap<>());
    }

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
}
