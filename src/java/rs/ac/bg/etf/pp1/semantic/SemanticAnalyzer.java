package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.logger.CompilerDiagnostics;
import rs.ac.bg.etf.pp1.symbols.Flags;
import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Symbol.*;
import rs.ac.bg.etf.pp1.symbols.SymbolTable;
import rs.ac.bg.etf.pp1.symbols.Type;
import rs.ac.bg.etf.pp1.symbols.Type.*;
import rs.ac.bg.etf.pp1.util.Context;
import rs.ac.bg.etf.pp1.util.TreeVisitor;

import java.util.ArrayList;
import java.util.List;

public final class SemanticAnalyzer extends TreeVisitor {
    private final CompilerDiagnostics diagnostics;

    private MethodSymbol currentMethod;

    public SemanticAnalyzer(Context context) {
        diagnostics = CompilerDiagnostics.getInstance(context);
    }

    @Override
    public void visit(MJMethodDeclaration tree) {
        currentMethod = tree.methodsymbol;
        tree.getMethod_body().accept(this);
    }

    // Method body

    @Override
    public void visit(MJMethodBody tree) {
        if (currentMethod.getModifiers().has(Flags.Modifier.ABSTRACT)) {
            diagnostics.reportError("Abstract method cannot have a body.", tree.getLine());
        }

        tree.getBlock().accept(this);
    }

    @Override
    public void visit(MJAbstractMethodBody tree) {
        if (!currentMethod.getModifiers().has(Flags.Modifier.ABSTRACT)) {
            diagnostics.reportError("Method must be implemented or marked as abstract.", tree.getLine());
        }
    }

    // Statements

    @Override
    public void visit(MJIfThen tree) {
        checkIf((MJIfCondition) tree.getIf_condition(), tree.getStatement(), null);
    }

    @Override
    public void visit(MJIfThenElse tree) {
        checkIf((MJIfCondition) tree.getIf_condition(), tree.getStatement_no_short_if(), tree.getStatement());
    }

    @Override
    public void visit(MJIfThenElseNoShortIf tree) {
        checkIf((MJIfCondition) tree.getIf_condition(), tree.getStatement_no_short_if(), tree.getStatement_no_short_if1());
    }

    private void checkIf(
            MJIfCondition condition,
            SyntaxNode thenBranch,
            SyntaxNode elseBranch
    ) {
        condition.getExpression().accept(this);

        Type conditionType = condition.getExpression().expressionvalue.type;
        if (conditionType != SymbolTable.BOOL) {
            reportType(SymbolTable.BOOL, conditionType, condition);
        }

        thenBranch.accept(this);
        if (elseBranch != null) elseBranch.accept(this);
    }

    private int loopDepth = 0;
    private int switchDepth = 0;

    private boolean isContinueAllowed() { return loopDepth > 0; }
    private boolean isBreakAllowed() { return loopDepth > 0 || switchDepth > 0; }

    @Override
    public void visit(MJSwitch tree) {
        tree.getExpression().accept(this);

        Type expressionType = tree.getExpression().expressionvalue.type;
        if (!expressionType.assignable(SymbolTable.INT)) {
            reportType(SymbolTable.INT, expressionType, tree.getExpression());
        }

        switchDepth++;

        tree.getSwitch_block().accept(this);

        switchDepth--;
    }

    @Override
    public void visit(MJFor tree) {
        checkFor(tree.getFor_init_opt(), tree.getExpression_opt(), tree.getFor_update_opt(), tree.getStatement());
    }

    @Override
    public void visit(MJForNoShortIf tree) {
        checkFor(tree.getFor_init_opt(), tree.getExpression_opt(), tree.getFor_update_opt(), tree.getStatement_no_short_if());
    }

    private void checkFor(
            For_init_opt init,
            Expression_opt condition,
            For_update_opt update,
            SyntaxNode body
    ) {
        init.accept(this);

        condition.accept(this);
        Type conditionType = condition.expressionvalue.type;
        if (conditionType != SymbolTable.BOOL) {
            reportType(SymbolTable.BOOL, conditionType, condition);
        }

        update.accept(this);

        loopDepth++;
        body.accept(this);
        loopDepth--;
    }

    @Override
    public void visit(MJBreak tree) {
        if (!isBreakAllowed()) {
            diagnostics.reportError("'break' is not allowed here.", tree.getLine());
        }
    }

    @Override
    public void visit(MJContinue tree) {
        if (!isContinueAllowed()) {
            diagnostics.reportError("'continue' is not allowed here.", tree.getLine());
        }
    }

    @Override
    public void visit(MJReturn tree) {
        tree.getExpression_opt().accept(this);
        Type returnType = tree.getExpression_opt().expressionvalue.type;
        if (returnType == SymbolTable.VOID && currentMethod.getReturnType() != SymbolTable.VOID) {
            diagnostics.reportError("Missing return value.", tree.getLine());
            return;
        }

        if (returnType != SymbolTable.VOID && currentMethod.getReturnType() == SymbolTable.VOID) {
            diagnostics.reportError("Cannot return a value from a method with void result type.", tree.getLine());
            return;
        }

        if (returnType != currentMethod.getReturnType()) {
            reportType(currentMethod.getReturnType(), returnType, tree.getExpression_opt());
        }
    }

    // Expressions

    @Override
    public void visit(MJNullLiteral tree) {
        tree.expressionvalue = new ExpressionValue(SymbolTable.NULL);
    }

    @Override
    public void visit(MJIntegerLiteral tree) {
        int value = tree.getI1();
        tree.expressionvalue = new ExpressionValue(SymbolTable.INT, value);
    }

    @Override
    public void visit(MJBooleanLiteral tree) {
        int value = tree.getB1();
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL, value);
    }

    @Override
    public void visit(MJCharacterLiteral tree) {
        int value = tree.getC1();
        tree.expressionvalue = new ExpressionValue(SymbolTable.CHAR, value);
    }

    @Override
    public void visit(MJNameSimple tree) {
        tree.getSimple_name().accept(this);
        tree.expressionvalue = tree.getSimple_name().expressionvalue;
    }

    @Override
    public void visit(MJNameQualified tree) {
        tree.getQualified_name().accept(this);
        tree.expressionvalue = tree.getQualified_name().expressionvalue;
    }

    @Override
    public void visit(MJSimpleName tree) {
        String name = tree.getI1();
        Symbol symbol = currentMethod.find(name);

        if (symbol == SymbolTable.NO_SYMBOL)
            diagnostics.reportError("Name `" + name + "' not found.", tree.getLine());

        tree.expressionvalue = new ExpressionValue(symbol);
    }

    @Override
    public void visit(MJThis tree) {
        if (currentMethod.isStatic()) {
            diagnostics.reportError("'this' not allowed here.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        Symbol symbol = currentMethod.find("this");
        tree.expressionvalue = new ExpressionValue(symbol);
    }

    @Override
    public void visit(MJClassInstanceCreation tree) {
        visitType(tree.getMJType());
        Type type = tree.getMJType().expressionvalue.type;
        if (type == SymbolTable.VOID) {
            tree.expressionvalue = tree.getMJType().expressionvalue;
            return;
        }
        if (!type.isClass()) {
            diagnostics.reportError("Class expected.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        tree.expressionvalue = new ExpressionValue(type.getOwner());
    }

    @Override
    public void visit(MJArrayCreation tree) {
        tree.getExpression().accept(this);
        Type expressionType = tree.getExpression().expressionvalue.type;
        if (!expressionType.assignable(SymbolTable.INT)) {
            reportType(SymbolTable.INT, expressionType, tree.getExpression());
        }

        visitType(tree.getMJType());
        Type type = tree.getMJType().expressionvalue.type;
        if (type == SymbolTable.VOID) {
            tree.expressionvalue = tree.getMJType().expressionvalue;
            return;
        }
        tree.expressionvalue = new ExpressionValue(Type.arrayOf(type));
    }

    @Override
    public void visit(MJQualifiedName tree) {
        String name = tree.getI2();

        tree.getName().accept(this);
        Type type = tree.getName().expressionvalue.type;

        tree.expressionvalue = accessField(type, name, tree);
    }

    @Override
    public void visit(MJFieldAccess tree) {
        String name = tree.getI2();

        tree.getPrimary().accept(this);
        Type type = tree.getPrimary().expressionvalue.type;

        tree.expressionvalue = accessField(type, name, tree);
    }

    private ExpressionValue accessField(
            Type qualifierType,
            String name,
            SyntaxNode tree
    ) {
        if (qualifierType == SymbolTable.VOID) {
            return new ExpressionValue(SymbolTable.VOID);
        }

        if (!qualifierType.isClass() && !qualifierType.isEnum()) {
            diagnostics.reportError("Cannot access field on '" + qualifierType + "' type.", tree.getLine());
            return new ExpressionValue(SymbolTable.VOID);
        }

        Accessible classType = (Accessible) qualifierType;
        Symbol symbol = classType.find(name);

        if (symbol == SymbolTable.NO_SYMBOL)
            diagnostics.reportError("Name `" + name + "' not found.", tree.getLine());

        return new ExpressionValue(symbol);
    }

    @Override
    public void visit(MJLength tree) {
        tree.getName().accept(this);
        Type type = tree.getName().expressionvalue.type;
        tree.expressionvalue = accessArrayLength(type, tree);
    }

    @Override
    public void visit(MJLengthFieldAcces tree) {
        tree.getPrimary().accept(this);
        Type type = tree.getPrimary().expressionvalue.type;
        tree.expressionvalue = accessArrayLength(type, tree);
    }

    private ExpressionValue accessArrayLength(Type type, SyntaxNode tree) {
        if (type == SymbolTable.VOID) {
            return new ExpressionValue(SymbolTable.VOID);
        }

        if (!type.isArray()) {
            diagnostics.reportError("'length' can only be used on arrays.", tree.getLine());
            return new ExpressionValue(SymbolTable.VOID);
        }

        return new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJMethodInvocation tree) {
        tree.getName().accept(this);
        Symbol symbol = tree.getName().expressionvalue.symbol;
        if (symbol == SymbolTable.NO_SYMBOL) {
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!symbol.isCallable()) {
            diagnostics.reportError("`" + symbol.getName() + "' is not callable.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }
        MethodSymbol method = (MethodSymbol) symbol;

        List<Type> arguments = getArguments(tree.getArgument_list_opt());
        if (!method.matchesArguments(arguments)) {
            diagnostics.reportError("Expected " + method.formatParameters() + ", got " + formatArguments(arguments) + ".", tree.getLine());
        }
        tree.expressionvalue = new ExpressionValue(method);
    }

    @Override
    public void visit(MJQualifiedMethodInvocation tree) {
        tree.getPrimary().accept(this);
        Type type = tree.getPrimary().expressionvalue.type;
        if (type == SymbolTable.VOID) {
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!type.isClass()) {
            diagnostics.reportError("Cannot access field on '" + type + "' type.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }
        String name = tree.getI2();
        ClassType classType = (ClassType) type;
        Symbol symbol = classType.find(name);

        if (symbol == SymbolTable.NO_SYMBOL) {
            diagnostics.reportError("Name `" + name + "' not found.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!symbol.isCallable()) {
            diagnostics.reportError("`" + name + "' is not callable.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }
        MethodSymbol method = (MethodSymbol) symbol;

        List<Type> arguments = getArguments(tree.getArgument_list_opt());
        if (!method.matchesArguments(arguments)) {
            diagnostics.reportError("Expected " + method.formatParameters() + ", got " + formatArguments(arguments) + ".", tree.getLine());
        }
        tree.expressionvalue = new ExpressionValue(method);
    }

    private List<Type> arguments;

    private static String formatArguments(List<Type> arguments) {
        return arguments.stream()
                .map(Type::toString)
                .collect(java.util.stream.Collectors.joining(", ", "(", ")"));
    }

    private List<Type> getArguments(Argument_list_opt argumentList) {
        arguments = new ArrayList<>();

        argumentList.accept(this);

        return arguments;
    }

    @Override
    public void visit(MJArgument tree) {
        List<Type> arguments = this.arguments;
        tree.getExpression().accept(this);
        arguments.add(tree.getExpression().expressionvalue.type);
        this.arguments = arguments;
    }

    @Override
    public void visit(MJNextArgument tree) {
        List<Type> arguments = this.arguments;
        tree.getArgument_list().accept(this);
        tree.getExpression().accept(this);
        arguments.add(tree.getExpression().expressionvalue.type);
        this.arguments = arguments;
    }

    @Override
    public void visit(MJRead tree) {
        tree.getExpression().accept(this);
        Symbol symbol = tree.getExpression().expressionvalue.symbol;
        if (!symbol.isDataHolder()) {
            diagnostics.reportError("Variable expected", tree.getExpression().getLine());
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
    }

    @Override
    public void visit(MJPrint tree) {
        tree.getExpression().accept(this);
        Type type = tree.getExpression().expressionvalue.type;
        if (!type.isPrimitive()) {
            diagnostics.reportError("Cannot print non-primitive type.", tree.getLine());
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
    }

    @Override
    public void visit(MJPrintConst tree) {
        tree.getExpression().accept(this);
        Type type = tree.getExpression().expressionvalue.type;
        if (!type.isPrimitive()) {
            diagnostics.reportError("Cannot print non-primitive type.", tree.getLine());
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
    }

    @Override
    public void visit(MJArrayAccess tree) {
        tree.getExpression().accept(this);
        Type expressionType = tree.getExpression().expressionvalue.type;
        if (!expressionType.assignable(SymbolTable.INT)) {
            reportType(SymbolTable.INT, expressionType, tree.getExpression());
        }

        tree.getName().accept(this);
        Type type = tree.getName().expressionvalue.type;
        if (type == SymbolTable.VOID) {
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!type.isArray()) {
            diagnostics.reportError("Array type expected.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        tree.expressionvalue = new ExpressionValue(Symbol.makeDummy(((ArrayType) type).getElementType()));
    }

    @Override
    public void visit(MJQualifiedArrayAccess tree) {
        tree.getExpression().accept(this);
        Type expressionType = tree.getExpression().expressionvalue.type;
        if (!expressionType.assignable(SymbolTable.INT)) {
            reportType(SymbolTable.INT, expressionType, tree.getExpression());
        }

        tree.getPrimary_no_new_array().accept(this);
        Type type = tree.getPrimary_no_new_array().expressionvalue.type;
        if (type == SymbolTable.VOID) {
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!type.isArray()) {
            diagnostics.reportError("Array type expected.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
        }

        tree.expressionvalue = new ExpressionValue(Symbol.makeDummy(((ArrayType) type).getElementType()));
    }

    @Override
    public void visit(MJPostincrement tree) {
        tree.expressionvalue = checkPostfixExpression(tree.getPostfix_expression());
    }

    @Override
    public void visit(MJPostdecrement tree) {
        tree.expressionvalue = checkPostfixExpression(tree.getPostfix_expression());
    }

    private ExpressionValue checkPostfixExpression(Postfix_expression tree) {
        tree.accept(this);
        Symbol variable = tree.expressionvalue.symbol;

        if (!variable.isDataHolder()) {
            diagnostics.reportError("Variable expected", tree.getLine());
            return new ExpressionValue(SymbolTable.VOID);
        }

        if (!variable.getSymbolType().assignable(SymbolTable.INT)) {
            reportType(SymbolTable.INT, variable.getSymbolType(), tree);
            return new ExpressionValue(SymbolTable.VOID);
        }

        return new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJNegation tree) {
        tree.getUnary_expression().accept(this);
        Type type = tree.getUnary_expression().expressionvalue.type;
        if (!type.assignable(SymbolTable.INT)) {
            reportType(SymbolTable.INT, type, tree.getUnary_expression());
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJMultiplication tree) {
        tree.getMultiplicative_expression().accept(this);
        tree.getUnary_expression().accept(this);
        Type type1 = tree.getMultiplicative_expression().expressionvalue.type;
        Type type2 = tree.getUnary_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator("*", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJDivision tree) {
        tree.getMultiplicative_expression().accept(this);
        tree.getUnary_expression().accept(this);
        Type type1 = tree.getMultiplicative_expression().expressionvalue.type;
        Type type2 = tree.getUnary_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator("/", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJModulo tree) {
        tree.getMultiplicative_expression().accept(this);
        tree.getUnary_expression().accept(this);
        Type type1 = tree.getMultiplicative_expression().expressionvalue.type;
        Type type2 = tree.getUnary_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator("%", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJAddition tree) {
        tree.getAdditive_expression().accept(this);
        tree.getMultiplicative_expression().accept(this);
        Type type1 = tree.getAdditive_expression().expressionvalue.type;
        Type type2 = tree.getMultiplicative_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator("+", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJSubtraction tree) {
        tree.getAdditive_expression().accept(this);
        tree.getMultiplicative_expression().accept(this);
        Type type1 = tree.getAdditive_expression().expressionvalue.type;
        Type type2 = tree.getMultiplicative_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator("-", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJLessThan tree) {
        tree.getRelational_expression().accept(this);
        tree.getAdditive_expression().accept(this);
        Type type1 = tree.getRelational_expression().expressionvalue.type;
        Type type2 = tree.getAdditive_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator("<", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL);
    }

    @Override
    public void visit(MJGreaterThan tree) {
        tree.getRelational_expression().accept(this);
        tree.getAdditive_expression().accept(this);
        Type type1 = tree.getRelational_expression().expressionvalue.type;
        Type type2 = tree.getAdditive_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator(">", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL);
    }

    @Override
    public void visit(MJLessThanOrEqualTo tree) {
        tree.getRelational_expression().accept(this);
        tree.getAdditive_expression().accept(this);
        Type type1 = tree.getRelational_expression().expressionvalue.type;
        Type type2 = tree.getAdditive_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator("<=", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL);
    }

    @Override
    public void visit(MJGreaterThanOrEqualTo tree) {
        tree.getRelational_expression().accept(this);
        tree.getAdditive_expression().accept(this);
        Type type1 = tree.getRelational_expression().expressionvalue.type;
        Type type2 = tree.getAdditive_expression().expressionvalue.type;
        if (!type1.assignable(SymbolTable.INT) || !type2.assignable(SymbolTable.INT)) {
            reportBinaryOperator(">=", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL);
    }

    @Override
    public void visit(MJEqual tree) {
        tree.getEqualiity_expression().accept(this);
        tree.getRelational_expression().accept(this);
        Type type1 = tree.getEqualiity_expression().expressionvalue.type;
        Type type2 = tree.getEqualiity_expression().expressionvalue.type;
        if (!type1.assignable(type2) || !type2.assignable(type1)) {
            reportBinaryOperator("==", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL);
    }

    @Override
    public void visit(MJNotEqual tree) {
        tree.getEqualiity_expression().accept(this);
        tree.getRelational_expression().accept(this);
        Type type1 = tree.getEqualiity_expression().expressionvalue.type;
        Type type2 = tree.getEqualiity_expression().expressionvalue.type;
        if (!type1.assignable(type2) || !type2.assignable(type1)) {
            reportBinaryOperator("!=", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL);
    }

    @Override
    public void visit(MJConjuction tree) {
        tree.getConditional_and_expression().accept(this);
        tree.getEqualiity_expression().accept(this);
        Type type1 = tree.getConditional_and_expression().expressionvalue.type;
        Type type2 = tree.getEqualiity_expression().expressionvalue.type;
        if (type1 != SymbolTable.BOOL || type2 != SymbolTable.BOOL) {
            reportBinaryOperator("&&", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL);
    }

    @Override
    public void visit(MJDisjunction tree) {
        tree.getConditional_or_expression().accept(this);
        tree.getConditional_and_expression().accept(this);
        Type type1 = tree.getConditional_and_expression().expressionvalue.type;
        Type type2 = tree.getConditional_or_expression().expressionvalue.type;
        if (type1 != SymbolTable.BOOL || type2 != SymbolTable.BOOL) {
            reportBinaryOperator("||", type1, type2, tree);
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.BOOL);
    }

    @Override
    public void visit(MJTernaryOperation tree) {
        tree.getConditional_or_expression().accept(this);
        Type conditionType = tree.getConditional_or_expression().expressionvalue.type;
        if (conditionType != SymbolTable.BOOL) {
            reportType(SymbolTable.BOOL, conditionType, tree.getConditional_or_expression());
        }

        tree.getExpression().accept(this);
        tree.getConditional_expression().accept(this);
        Type trueType = tree.getExpression().expressionvalue.type;
        Type falseType = tree.getConditional_expression().expressionvalue.type;
        if (!trueType.assignable(falseType) || !falseType.assignable(trueType)) {
            diagnostics.reportError("Ternary operator types mismatch: " + trueType + " and " + falseType + ".", tree.getLine());
        }
        tree.expressionvalue = new ExpressionValue(trueType);
    }

    @Override
    public void visit(MJAssignment tree) {
        tree.getLeft_hand_side().accept(this);
        Type left = tree.getLeft_hand_side().expressionvalue.type;
        if (left == SymbolTable.VOID) {
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        tree.getAssignment_expression().accept(this);
        Type right = tree.getAssignment_expression().expressionvalue.type;
        if (right == SymbolTable.VOID) {
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!right.assignable(left)) {
            diagnostics.reportError("Cannot convert " + right + " to " + left + ".", tree.getLine());
        }
    }

    private void reportBinaryOperator(String operator, Type left, Type right, SyntaxNode tree) {
        diagnostics.reportError("Operator `" + operator + "' cannot be applied to " + left + " and " + right + ".", tree.getLine());
    }

    private void reportType(Type required, Type provided, SyntaxNode tree) {
        diagnostics.reportError("Required: " + required + ", provided: " + provided + ".", tree.getLine());
    }

    private void visitType(MJType tree) {
        String name = tree.getI1();
        Symbol found = currentMethod.find(name);

        if (found == SymbolTable.NO_SYMBOL) {
            diagnostics.reportError("Type `" + name + "' not found.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!found.isType()) {
            diagnostics.reportError("`" + name + "' is not a type.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        tree.expressionvalue = new ExpressionValue(found.getSymbolType());
    }

    @Override
    public void visit(MJNoExpression tree) {
        tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
    }

    // <editor-fold defaultstate="collapsed" desc="Simple traversal methods">

    @Override
    public void visit(MJConstLiteral tree) {
        tree.getConst_literal().accept(this);
        tree.expressionvalue = tree.getConst_literal().expressionvalue;
    }

    @Override
    public void visit(MJPrimaryNoNewArray tree) {
        tree.getPrimary_no_new_array().accept(this);
        tree.expressionvalue = tree.getPrimary_no_new_array().expressionvalue;
    }

    @Override
    public void visit(MJPrimaryClassInstanceCreationExpression tree) {
        tree.getClass_instance_creation_expression().accept(this);
        tree.expressionvalue = tree.getClass_instance_creation_expression().expressionvalue;
    }

    @Override
    public void visit(MJPrimaryArrayCreationExpression tree) {
        tree.getArray_creation_expression().accept(this);
        tree.expressionvalue = tree.getArray_creation_expression().expressionvalue;
    }

    @Override
    public void visit(MJPrimaryLiteral tree) {
        tree.getLiteral().accept(this);
        tree.expressionvalue = tree.getLiteral().expressionvalue;
    }

    @Override
    public void visit(MJParenthesisedExpression tree) {
        tree.getExpression().accept(this);
        tree.expressionvalue = tree.getExpression().expressionvalue;
    }

    @Override
    public void visit(MJPrimaryFieldAccess tree) {
        tree.getField_access().accept(this);
        tree.expressionvalue = tree.getField_access().expressionvalue;
    }

    @Override
    public void visit(MJPrimaryMethodInvocation tree) {
        tree.getMethod_invocation().accept(this);
        tree.expressionvalue = tree.getMethod_invocation().expressionvalue;
    }

    @Override
    public void visit(MJPrimaryArrayAccess tree) {
        tree.getArray_access().accept(this);
        tree.expressionvalue = tree.getArray_access().expressionvalue;
    }

    @Override
    public void visit(MJArgumentList tree) {
        tree.getArgument_list().accept(this);
        tree.expressionvalue = tree.getArgument_list().expressionvalue;
    }

    @Override
    public void visit(MJPrimaryExpression tree) {
        tree.getPrimary().accept(this);
        tree.expressionvalue = tree.getPrimary().expressionvalue;
    }

    @Override
    public void visit(MJNameExpression tree) {
        tree.getName().accept(this);
        tree.expressionvalue = tree.getName().expressionvalue;
    }

    @Override
    public void visit(MJPostincrementExpression tree) {
        tree.getPostincrement_expression().accept(this);
        tree.expressionvalue = tree.getPostincrement_expression().expressionvalue;
    }

    @Override
    public void visit(MJPostdecrementExpression tree) {
        tree.getPostdecrement_expression().accept(this);
        tree.expressionvalue = tree.getPostdecrement_expression().expressionvalue;
    }

    @Override
    public void visit(MJPostfixExpression tree) {
        tree.getPostfix_expression().accept(this);
        tree.expressionvalue = tree.getPostfix_expression().expressionvalue;
    }

    @Override
    public void visit(MJUnaryExpression tree) {
        tree.getUnary_expression().accept(this);
        tree.expressionvalue = tree.getUnary_expression().expressionvalue;
    }

    @Override
    public void visit(MJMultiplicativeExpression tree) {
        tree.getMultiplicative_expression().accept(this);
        tree.expressionvalue = tree.getMultiplicative_expression().expressionvalue;
    }

    @Override
    public void visit(MJAdditiveExpression tree) {
        tree.getAdditive_expression().accept(this);
        tree.expressionvalue = tree.getAdditive_expression().expressionvalue;
    }

    @Override
    public void visit(MJRelationalExpression tree) {
        tree.getRelational_expression().accept(this);
        tree.expressionvalue = tree.getRelational_expression().expressionvalue;
    }

    @Override
    public void visit(MJEqualityExpression tree) {
        tree.getEqualiity_expression().accept(this);
        tree.expressionvalue = tree.getEqualiity_expression().expressionvalue;
    }

    @Override
    public void visit(MJConditionalAndExpression tree) {
        tree.getConditional_and_expression().accept(this);
        tree.expressionvalue = tree.getConditional_and_expression().expressionvalue;
    }

    @Override
    public void visit(MJConditionalOrExpression tree) {
        tree.getConditional_or_expression().accept(this);
        tree.expressionvalue = tree.getConditional_or_expression().expressionvalue;
    }

    @Override
    public void visit(MJConditionalExpression tree) {
        tree.getConditional_expression().accept(this);
        tree.expressionvalue = tree.getConditional_expression().expressionvalue;
    }

    @Override
    public void visit(MJAssignmentExpression tree) {
        tree.getAssignment().accept(this);
        tree.expressionvalue = tree.getAssignment().expressionvalue;
    }

    @Override
    public void visit(MJLeftHandSideName tree) {
        tree.getName().accept(this);
        tree.expressionvalue = tree.getName().expressionvalue;
    }

    @Override
    public void visit(MJLeftHandSideFieldAccress tree) {
        tree.getField_access().accept(this);
        tree.expressionvalue = tree.getField_access().expressionvalue;
    }

    @Override
    public void visit(MJLeftHandSideArrayAccess tree) {
        tree.getArray_access().accept(this);
        tree.expressionvalue = tree.getArray_access().expressionvalue;
    }

    @Override
    public void visit(MJExpressionOption tree) {
        tree.getExpression().accept(this);
        tree.expressionvalue = tree.getExpression().expressionvalue;
    }

    @Override
    public void visit(MJExpression tree) {
        tree.getAssignment_expression().accept(this);
        tree.expressionvalue = tree.getAssignment_expression().expressionvalue;
    }

    // </editor-fold>
}
