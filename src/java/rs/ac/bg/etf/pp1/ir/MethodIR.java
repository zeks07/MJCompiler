package rs.ac.bg.etf.pp1.ir;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.ir.node.*;
import rs.ac.bg.etf.pp1.ir.types.Type;
import rs.ac.bg.etf.pp1.ir.types.Types;
import rs.ac.bg.etf.pp1.symbols.Symbol;

import java.util.Stack;

public final class MethodIR extends VisitorAdaptor {
    private Symbol.MethodSymbol method;

    public static StartNode START;
    public StopNode STOP;

    public ScopeNode scope;
    public Stack<ScopeNode> scopes = new Stack<>();

    public ScopeNode continueScope;
    public ScopeNode breakScope;

    private Node result;

    public MethodIR(Symbol.MethodSymbol method) {
        this.method = method;
        IterPeeps.reset();
        scope = new ScopeNode();
        continueScope = breakScope = null;
        START = new StartNode(new Type[]{ Types.CONTROL });
        STOP = new StopNode();
    }

    private Node control() {
        return scope.control();
    }

    private Node setControl(Node node) {
        return scope.setControl(node);
    }

    public StopNode build() {
        scopes.push(scope);

        scope.push();
        scope.define("$ctrl", new ProjectionNode(START, 0).peephole());
        scope.define("arg", new ProjectionNode(START, 0).peephole());

        method.getNode().accept(this);

        scope.pop();
        scopes.pop();

        STOP.peephole();
        return STOP;
    }

    // Statements

    @Override
    public void visit(MJBlock tree) {
        scope.push();

        tree.childrenAccept(this);

        scope.pop();
    }

    @Override
    public void visit(MJIfThen node) {
        buildIf(node.getIf_condition(), node.getStatement(), null);
    }

    @Override
    public void visit(MJIfThenElse node) {
        buildIf(node.getIf_condition(), node.getStatement_no_short_if(), node.getStatement());
    }

    @Override
    public void visit(MJIfThenElseNoShortIf node) {
        buildIf(node.getIf_condition(), node.getStatement_no_short_if(), node.getStatement_no_short_if1());
    }

    private void buildIf(
            SyntaxNode condition,
            SyntaxNode thenBranch,
            SyntaxNode elseBranch
    ) {
        Node predicate = buildExpression(condition);

        Node ifNode = new IfNode(control(), predicate).peephole();

        Node ifTrue = new ProjectionNode(ifNode.keep(), 0).peephole().keep();
        Node ifFalse = new ProjectionNode(ifNode.unkeep(), 1).peephole().keep();

        ScopeNode fScope = scope.duplicate();
        scopes.push(fScope);

        // then
        setControl(ifTrue.unkeep());
        thenBranch.accept(this);
        ScopeNode thenScope = scope;

        // else
        scope = fScope;
        setControl(ifFalse.unkeep());
        if (elseBranch != null) {
            elseBranch.accept(this);
            fScope = scope;
        }

        // No need to check if there are new names, as it's impossible by language.

        scope = thenScope;
        scopes.pop();

        setControl(thenScope.mergeScopes(fScope));
    }

    @Override
    public void visit(MJFor node) {}

    @Override
    public void visit(MJForNoShortIf node) {}

    private void buildFor(
            SyntaxNode init,
            SyntaxNode condition,
            SyntaxNode update,
            SyntaxNode body
    ) {
        init.accept(this);

        ScopeNode savedContinueScope = continueScope;
        ScopeNode savedBreakScope = breakScope;

        setControl(new LoopNode(control()).peephole());

        ScopeNode head = scope.keep();

        Node predicate = buildExpression(condition);
        Node ifNode = new IfNode(control(), predicate).peephole();
        Node ifTrue = new ProjectionNode(ifNode.keep(), 0).peephole().keep();
        Node ifFalse = new ProjectionNode(ifNode.unkeep(), 1).peephole();

        setControl(ifFalse);
        scopes.push(breakScope = scope.duplicate());

        continueScope = null;

        setControl(ifTrue.unkeep());
        body.accept(this);

        if (continueScope != null) {
            continueScope = jumpTo(continueScope);
            scope.kill();
            scope = continueScope;
        }

        ScopeNode exit = breakScope;
        head.endLoop(scope, exit);
        head.unkeep().kill();

        scopes.pop();
        scopes.pop();

        continueScope = savedContinueScope;
        breakScope = savedBreakScope;

        scope = exit;
    }

    private ScopeNode jumpTo(ScopeNode toScope) {
        ScopeNode current = scope.duplicate();
        setControl(new ConstantNode(Types.X_CONTROL).peephole());

        while (current.scopes.size() > breakScope.scopes.size())
            current.pop();

        if (toScope == null)
            return current;

        assert toScope.scopes.size() <= breakScope.scopes.size();

        toScope.setControl(toScope.mergeScopes(current));
        return toScope;
    }

    @Override
    public void visit(MJBreak tree) {
        breakScope = jumpTo(breakScope);
    }

    @Override
    public void visit(MJContinue tree) {
        continueScope = jumpTo(continueScope);
    }

    @Override
    public void visit(MJReturn tree) {
        Node expression = buildExpression(tree.getExpression_opt());
        STOP.addReturn(new ReturnNode(control(), expression).peephole());
        setControl(new ConstantNode(Types.X_CONTROL).peephole());
    }

    // Expressions

    private Node buildExpression(SyntaxNode tree) {
        tree.accept(this);
        return result;
    }

    @Override
    public void visit(MJNegation tree) {
        Node operand = buildExpression(tree.getUnary_expression());
        result = new NegNode(operand).peephole();
    }

    @Override
    public void visit(MJMultiplication tree) {
        Node left = buildExpression(tree.getMultiplicative_expression());
        Node right = buildExpression(tree.getUnary_expression());
        result = new AddNode(left, right).peephole();
    }

    @Override
    public void visit(MJDivision tree) {
        Node left = buildExpression(tree.getMultiplicative_expression());
        Node right = buildExpression(tree.getUnary_expression());
        result = new DivNode(left, right).peephole();
    }

    @Override
    public void visit(MJModulo tree) {
        Node left = buildExpression(tree.getMultiplicative_expression());
        Node right = buildExpression(tree.getUnary_expression());
        result = new ModNode(left, right).peephole();
    }

    @Override
    public void visit(MJAddition tree) {
        Node left = buildExpression(tree.getAdditive_expression());
        Node right = buildExpression(tree.getMultiplicative_expression());
        result = new AddNode(left, right).peephole();
    }

    @Override
    public void visit(MJSubtraction tree) {
        Node left = buildExpression(tree.getAdditive_expression());
        Node right = buildExpression(tree.getMultiplicative_expression());
        result = new SubNode(left, right).peephole();
    }

    @Override
    public void visit(MJLessThan tree) {
        Node left = buildExpression(tree.getRelational_expression());
        Node right = buildExpression(tree.getAdditive_expression());
        result = new BooleanNode.LT(left, right).peephole();
    }

    @Override
    public void visit(MJGreaterThan tree) {
        Node left = buildExpression(tree.getRelational_expression());
        Node right = buildExpression(tree.getAdditive_expression());
        result = new BooleanNode.GT(left, right).peephole();
    }

    @Override
    public void visit(MJLessThanOrEqualTo tree) {
        Node left = buildExpression(tree.getRelational_expression());
        Node right = buildExpression(tree.getAdditive_expression());
        result = new BooleanNode.LE(left, right).peephole();
    }

    @Override
    public void visit(MJGreaterThanOrEqualTo tree) {
        Node left = buildExpression(tree.getRelational_expression());
        Node right = buildExpression(tree.getAdditive_expression());
        result = new BooleanNode.GE(left, right).peephole();
    }

    @Override
    public void visit(MJEqual tree) {
        Node left = buildExpression(tree.getEqualiity_expression());
        Node right = buildExpression(tree.getRelational_expression());
        result = new BooleanNode.EQ(left, right).peephole();
    }

    @Override
    public void visit(MJNotEqual tree) {
        Node left = buildExpression(tree.getEqualiity_expression());
        Node right = buildExpression(tree.getRelational_expression());
        result = new BooleanNode.NE(left, right).peephole();
    }
}
