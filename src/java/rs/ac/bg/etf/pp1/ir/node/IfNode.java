package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;
import rs.ac.bg.etf.pp1.ir.types.TypeTuple;

public final class IfNode extends MultiNode {
    public IfNode(Node control, Node predicate) {
        super(control, predicate);
    }

    @Override
    public boolean isCFG() {
        return true;
    }

    public Node control() {
        return in(0);
    }

    public Node predicate() {
        return in(1);
    }

    @Override
    public Type compute() {
        if (control().type != Types.CONTROL && control().type != Types.BOTTOM)
            return TypeTuple.IF_NEITHER;

        Node predicate = predicate();
        Type predicateType = predicate.type;

        if (predicateType == Types.TOP || predicateType == TypeInteger.TOP)
            return TypeTuple.IF_NEITHER;

        if (predicateType instanceof TypeInteger && predicateType.isConstant())
            return predicateType == TypeInteger.ZERO ? TypeTuple.IF_FALSE : TypeTuple.IF_TRUE;

        return TypeTuple.IF_BOTH;
    }

    @Override
    public Node idealize() {
        if (predicate().type.isHighOrConstant())
            return null;

        for (Node dominator = immediateDominator(), prior = this; dominator != null; prior = dominator, dominator = dominator.immediateDominator()) {
            Node dependency = dominator.addDependency(this);
            if (!(dependency instanceof IfNode))
                continue;
            IfNode iff = (IfNode) dependency;

            if (iff.predicate().addDependency(this) != predicate() || !(prior instanceof ProjectionNode))
                continue;

            setDefinition(1, new ConstantNode(TypeInteger.make(true, prior.id == 0 ? 1 : 0)).peephole());
        }

        return null;
    }
}
