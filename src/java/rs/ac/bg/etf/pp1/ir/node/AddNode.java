package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public final class AddNode extends BinaryNode {
    public AddNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public Type compute() {
        if (!computable())
            return Types.BOTTOM;

        long leftValue = ((TypeInteger) left().type).value();
        long rightValue = ((TypeInteger) right().type).value();

        return TypeInteger.constant(leftValue + rightValue);
    }

    @Override
    public Node idealize() {
        Node left = left();
        Node right = right();
        Type leftType = left().type;
        Type rightType = right().type;

        // Already handled by peephole constant folding
        assert !(leftType.isConstant() && rightType.isConstant());

        // Add of zero
        if (rightType instanceof TypeInteger && ((TypeInteger) rightType).value() == 0)
            return left;

        // Add of same to multiply by 2
        if (left == right)
            return new MulNode(left, new ConstantNode(TypeInteger.constant(2)).peephole());

        // Move non-additions to right hand side
        if (!(left instanceof AddNode) && right instanceof AddNode)
            return swap();

        // possible: (add add non) (add non non) (add add add), but not (add non add)

        // change x + (y + z) to (x + y) + z
        if (right instanceof AddNode)
            return new AddNode(new AddNode(left, ((AddNode) right).left()).peephole(), ((AddNode) right).right()).peephole();

        // possible: (add add non) (add non non)

        if (!(left instanceof AddNode))
            return splineCompare(left, right) ? swap() : null;

        // possible only (add add non)

        // change (x + con1) + con2 to x + (con1 + con2)
        if (((AddNode) left).right().type.isConstant() && rightType.isConstant())
            return new AddNode(((AddNode) left).left(), new AddNode(((AddNode) left).right(), right).peephole());

        Node phi = phiConstant(true);
        if (phi != null) return phi;

        if (splineCompare(((AddNode) left).right(), right))
            return new AddNode(new AddNode(((AddNode) left).left(), right).peephole(), ((AddNode) left).right());

        return null;
    }

    /**
     * Returns whether we should rotate ((x + low) + high) to ((x + high) + low)
     * Constant should go to the right, then phi-of-constants, then multiplication, then the rest.
     * </p>
     * If nothing is applicable, order by id.
     * @return true if low and high should swap.
     */
    private boolean splineCompare(Node high, Node low) {
        if (low.type.isConstant()) return false;
        if (high.type.isConstant()) return true;

        if (low instanceof PhiNode && ((PhiNode) low).region().type == Types.X_CONTROL) return false;
        if (high instanceof PhiNode && ((PhiNode) high).region().type == Types.X_CONTROL) return true;

        if (low instanceof PhiNode && low.hasOnlyConstants(this)) return false;
        if (high instanceof PhiNode && high.hasOnlyConstants(this)) return true;

        if (low instanceof PhiNode && !(high instanceof PhiNode)) return true;
        if (high instanceof PhiNode && !(low instanceof PhiNode)) return false;

        return low.id > high.id;
    }

    @Override
    Node copy(Node left, Node right) {
        return new AddNode(left, right);
    }
}
