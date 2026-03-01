package rs.ac.bg.etf.pp1.ir;

import rs.ac.bg.etf.pp1.ir.node.ConstantNode;
import rs.ac.bg.etf.pp1.ir.node.Node;
import rs.ac.bg.etf.pp1.ir.node.StopNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

public abstract class IterPeeps {
    private static final WorkList<Node> WORK = new WorkList<>();

    public static <N extends Node> N add(N node) {
        return (N) WORK.push(node);
    }

    public static void addAll(ArrayList<Node> list) {
        WORK.addAll(list);
    }

    public static StopNode iterate(StopNode stop) {
        assert progrtessOnList(stop);

        Node node;
        while ((node = WORK.pop()) != null) {
            if (node.isDead()) continue;

            Node x = node.peepholeOptimization();

            if (x == null || x.isDead()) continue;

            if (x.type == null) x.setType(x.compute());

            if (x != node || !(x instanceof ConstantNode)) {
                for (Node use : node.outputs) WORK.push(use);
                WORK.push(x);
                if (x != node) {
                    for (Node definition : node.inputs) WORK.push(definition);
                    node.subsume(x);
                }
            }

            node.moveDependenciesToWorklist();
            assert progressOnList(stop);
        }

        return stop;
    }

    private static boolean MID_ASSERT;

    public static boolean midAssert() {
        return MID_ASSERT;
    }

    private static boolean progressOnList(Node stop) {
        MID_ASSERT = true;
        Node changed = stop.walk(node -> {
            if (WORK.on(node)) return null;
            Node m = node.peeopholeOptimization();
            if (m == null) return null;
            return m;
        });
        MID_ASSERT = false;
        return changed != null;
    }

    public static void reset() {
        WORK.clear();
    }

    public static class WorkList<E extends Node> {
        private Node[] es;
        private int length;
        private final BitSet on;
        private final Random r;
        private final long seed;

        WorkList() {
            this(123);
        }

        WorkList(long seed) {
            es = new Node[1];
            length = 0;
            on = new BitSet();
            this.seed = seed;
            r = new Random(seed);
            r.setSeed(seed);
        }

        public E push(E x) {
            if (x == null) return null;
            int index = x.id;
            if (!on.get(index)) {
                on.set(index);
                if (length == es.length) es = Arrays.copyOf(es, length * 2);
                es[length++] = x;
            }
            return x;
        }

        public void addAll(ArrayList<E> list) {
            for (E e : list) push(e);
        }

        boolean on(E x) {
            return on.get(x.id);
        }

        E pop() {
            if (length == 0) return null;
            int index = r.nextInt(length);
            @SuppressWarnings("unchecked") E x = (E) es[index];
            es[index] = es[--length];
            on.clear(x.id);
            return x;
        }

        public void clear() {
            length = 0;
            on.clear();
            r.setSeed(seed);
        }
    }
}
