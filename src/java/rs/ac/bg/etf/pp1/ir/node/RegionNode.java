package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.IterPeeps;

public final class RegionNode extends MergeNode {
    public RegionNode(Node... inputs) {
        super(inputs);
    }

    @Override
    public boolean inProgress() {
        return false;
    }

    @Override
    Node immediateDominator() {
        if (idom != null) {
            if (idom.isDead())  idom = null;
            else return idom;
        }
        if (inSize() == 2) return in(1);
        if (inSize() != 3) return null;

        Node left = in(1).immediateDominator();
        Node right = in(2).immediateDominator();

        while (left != right) {
            if (left == null || right == null) return null;
            int comparison = left.depth - right.depth;
            if (comparison >= 0) left = left.immediateDominator();
            else right = right.immediateDominator();
        }

        if (left == null) return null;
        depth = left.depth + 1;
        if (!IterPeeps.midAssert()) idom = left;
        return left;
    }
}
