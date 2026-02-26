package rs.ac.bg.etf.pp1.ir.node;

import rs.ac.bg.etf.pp1.ir.types.IRType;
import rs.ac.bg.etf.pp1.ir.types.IRTypes;
import rs.ac.bg.etf.pp1.ir.types.TypeInteger;

public final class AddNode extends BinaryNode {
    public AddNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public IRType compute() {
        if (left().type instanceof TypeInteger && right().type instanceof TypeInteger) {
            if (left().type.isConstant() && right().type.isConstant()) {
                return TypeInteger.constant(((TypeInteger) left().type).value() + ((TypeInteger) right().type).value());
            }
        }
        return IRTypes.BOTTOM;
    }

    @Override
    public Node idealize() {
        Node left = left();
        Node right = right();
        IRType leftType = left().type;
        IRType rightType = right().type;

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

        if (splineCompare(((AddNode) left).right(), right))
            return new AddNode(new AddNode(((AddNode) left).left(), right).peephole(), ((AddNode) left).right());

        return null;
    }

    static boolean splineCompare(Node high, Node low) {
        if (low.type.isConstant()) return false;
        if (high.type.isConstant()) return true;

        return low.id > high.id;
    }
}
