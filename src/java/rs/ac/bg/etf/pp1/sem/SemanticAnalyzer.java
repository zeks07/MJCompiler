package rs.ac.bg.etf.pp1.sem;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.logger.CompilerDiagnostics;
import rs.ac.bg.etf.pp1.symbol.Flags;
import rs.ac.bg.etf.pp1.symbol.Symbol;
import rs.ac.bg.etf.pp1.symbol.Symbol.*;
import rs.ac.bg.etf.pp1.symbol.SymbolTable;
import rs.ac.bg.etf.pp1.symbol.Type;
import rs.ac.bg.etf.pp1.symbol.Type.*;
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
        if (expressionType != SymbolTable.INT) {
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
    public void visit(MJConstLiteral tree) {
        tree.expressionvalue = tree.getConst_literal().expressionvalue;
    }

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

        tree.expressionvalue = new ExpressionValue(type);
    }

    @Override
    public void visit(MJArrayCreation tree) {
        tree.getExpression().accept(this);
        Type expressionType = tree.getExpression().expressionvalue.type;
        if (expressionType != SymbolTable.INT) {
            reportType(SymbolTable.INT, expressionType, tree.getExpression());
        }

        visitType(tree.getMJType());
        Type type = tree.getMJType().expressionvalue.type;
        if (type == SymbolTable.VOID) {
            tree.expressionvalue = tree.getMJType().expressionvalue;
            return;
        }
        tree.expressionvalue = new ExpressionValue(type);
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

        if (!qualifierType.isClass()) {
            diagnostics.reportError("Cannot access field on '" + qualifierType + "' type.", tree.getLine());
            return new ExpressionValue(SymbolTable.VOID);
        }

        ClassType classType = (ClassType) qualifierType;
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
        tree.expressionvalue = new ExpressionValue(method.getReturnType());
    }

    @Override
    public void visit(MJQualifiedMethodInvocation tree) {
        tree.getPrimary().accept(this);
        Symbol symbol = tree.getPrimary().expressionvalue.symbol;
        if (symbol == SymbolTable.NO_SYMBOL) {
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!symbol.isClass()) {
            diagnostics.reportError("Cannot access field on '" + symbol.getMJType() + "' type.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }
        String name = tree.getI2();
        ClassType classType = (ClassType) symbol.getMJType();
        Symbol symbol1 = classType.find(name);

        if (symbol1 == SymbolTable.NO_SYMBOL) {
            diagnostics.reportError("Name `" + name + "' not found.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }

        if (!symbol1.isCallable()) {
            diagnostics.reportError("`" + name + "' is not callable.", tree.getLine());
            tree.expressionvalue = new ExpressionValue(SymbolTable.VOID);
            return;
        }
        MethodSymbol method = (MethodSymbol) symbol;

        List<Type> arguments = getArguments(tree.getArgument_list_opt());
        if (!method.matchesArguments(arguments)) {
            diagnostics.reportError("Expected " + method.formatParameters() + ", got " + formatArguments(arguments) + ".", tree.getLine());
        }
        tree.expressionvalue = new ExpressionValue(method.getReturnType());
    }

    private List<Type> arguments;

    private static String formatArguments(List<Type> arguments) {
        return arguments.stream()
                .map(Type::toString)
                .collect(java.util.stream.Collectors.joining(", "));
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
            diagnostics.reportError("Variable expected");
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
        if (expressionType != SymbolTable.INT) {
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

        tree.expressionvalue = new ExpressionValue(((ArrayType) type).getElementType());
    }

    @Override
    public void visit(MJQualifiedArrayAccess tree) {
        tree.getExpression().accept(this);
        Type expressionType = tree.getExpression().expressionvalue.type;
        if (expressionType != SymbolTable.INT) {
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

        tree.expressionvalue = new ExpressionValue(((ArrayType) type).getElementType());
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

        if (variable.getMJType() != SymbolTable.INT) {
            reportType(SymbolTable.INT, variable.getMJType(), tree);
            return new ExpressionValue(SymbolTable.VOID);
        }

        return new ExpressionValue(SymbolTable.INT);
    }

    @Override
    public void visit(MJNegation tree) {
        tree.getUnary_expression().accept(this);
        Type type = tree.getUnary_expression().expressionvalue.type;
        if (type != SymbolTable.INT) {
            reportType(SymbolTable.INT, type, tree.getUnary_expression());
        }
        tree.expressionvalue = new ExpressionValue(SymbolTable.INT);
    }

    private ExpressionValue checkBinaryOperation(
            SyntaxNode left,
            SyntaxNode right
    )

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

        tree.expressionvalue = new ExpressionValue(found.getMJType());
    }

    private void reportType(Type required, Type provided, SyntaxNode tree) {
        diagnostics.reportError("Required: " + required + ", provided: " + provided + ".", tree.getLine());
    }
}
