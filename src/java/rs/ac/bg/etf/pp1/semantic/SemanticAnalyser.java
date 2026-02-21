package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.symbols.ExpressionValue;
import rs.ac.bg.etf.pp1.symbols.BuiltIn;
import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Type;
import rs.ac.bg.etf.pp1.util.Context;

public final class SemanticAnalyser extends VisitorAdaptor {

    private final Environment environment;

    private final ModifierVisitor modifierVisitor = new ModifierVisitor();
    private final TypeVisitor typeVisitor = new TypeVisitor();
    private final VariableDeclarationVisitor variableDeclarationVisitor = new VariableDeclarationVisitor();
    private final EnumDeclarationVisitor enumDeclarationVisitor = new EnumDeclarationVisitor();
    private final ClassDeclarationVisitor classDeclarationVisitor = new ClassDeclarationVisitor();
    private final MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
    private final StatementVisitor statementVisitor = new StatementVisitor();
    private final ExpressionVisitor expressionVisitor = new ExpressionVisitor();

    public SemanticAnalyser(Context context) {
        this.environment = new Environment(context);
    }

    @Override
    public void visit(MJProgram node) {
        String name = node.getI1();
        environment.enterProgram(name, node);

        node.getStatic_declarations_opt().traverseBottomUp(this);
        node.getStatic_methods_block().traverseBottomUp(this);

        node.programsymbol = environment.exitProgram();
    }

    @Override
    public void visit(MJProgramVariable node) {
        node.getField_declaration().accept(variableDeclarationVisitor);
    }

    @Override
    public void visit(MJProgramEnum node) {
        node.getEnum_declaration().accept(enumDeclarationVisitor);
    }

    @Override
    public void visit(MJProgramClass node) {
        node.getClass_delcaration().accept(classDeclarationVisitor);
    }

    @Override
    public void visit(MJProgramMethod node) {
        node.getMethod_declaration().accept(methodDeclarationVisitor);
    }

    private DeclaratorInfo extractDeclarator(
            Type baseType,
            Variable_declarator_id id,
            Const_literal initializer
    ) {
        String name;
        boolean isArray;

        if (id instanceof MJVariableName) {
            name = ((MJVariableName) id).getI1();
            isArray = false;
        } else {
            name = ((MJArrayName) id).getI1();
            isArray = true;
        }

        Type type = isArray ? BuiltIn.arrayOf(baseType) : baseType;

        boolean hasInitializer = initializer != null;
        int value = 0;
        if (hasInitializer) {
            LiteralVisitor visitor = new LiteralVisitor();
            initializer.traverseBottomUp(visitor);
            value = visitor.value();
        }

        return new DeclaratorInfo(type, name, isArray, hasInitializer, value);
    }

    private static final class LiteralVisitor extends VisitorAdaptor {
        int value = 0;

        @Override
        public void visit(MJConstLiteral node) {
            node.type = node.getConst_literal().type;
        }

        @Override
        public void visit(MJNullLiteral node) {
            node.type = BuiltIn.VOID;
        }

        @Override
        public void visit(MJIntegerLiteral node) {
            value = node.getI1();
            node.type = BuiltIn.INT;
        }

        @Override
        public void visit(MJBooleanLiteral node) {
            value = node.getB1();
            node.type = BuiltIn.BOOLEAN;
        }

        @Override
        public void visit(MJCharacterLiteral node) {
            value = node.getC1();
            node.type = BuiltIn.CHAR;
        }

        public int value() {
            return value;
        }
    }

    private final class TypeVisitor extends VisitorAdaptor {
        @Override
        public void visit(MJType node) {
            String name = node.getI1();
            Symbol type = environment.findType(name, node);
            node.type = type.getSymbolType();
        }
    }

    private final class ModifierVisitor extends VisitorAdaptor {
        @Override
        public void visit(MJConstModifier node) {
            environment.requireConstAllowed(node);
        }

        @Override
        public void visit(MJAbstractModifier node) {
            environment.requireAbstractAllowed(node);
        }
    }

    private final class VariableDeclarationVisitor extends VisitorAdaptor {
        @Override
        public void visit(MJFieldDeclaration node) {
            environment.enterVariableDeclaration();

            node.getModifiers_opt().traverseBottomUp(modifierVisitor);

            MJType typeNode = node.getMJType();
            typeNode.accept(typeVisitor);
            environment.setType(typeNode.type, node);

            node.getVariable_declarators().traverseBottomUp(variableDeclarationVisitor);

            environment.exitVariableDeclaration();
        }

        @Override
        public void visit(MJVariable node) {
            DeclaratorInfo info = extractDeclarator(
                    environment.getType(),
                    node.getVariable_declarator_id(),
                    null
            );
            node.symbol = environment.declareVariable(info, node);
        }

        @Override
        public void visit(MJConstant node) {
            environment.requireInitialization(node);

            DeclaratorInfo info = extractDeclarator(
                    environment.getType(),
                    node.getVariable_declarator_id(),
                    node.getConst_literal()
            );
            node.symbol = environment.declareVariable(info, node);
        }
    }

    private final class EnumDeclarationVisitor extends VisitorAdaptor {
        @Override
        public void visit(MJEnumDeclaration node) {
            String name = node.getI1();
            environment.enterEnumDeclaration(name, node);

            node.getEnum_body().traverseBottomUp(enumDeclarationVisitor);

            environment.exitEnumDeclaration();
        }

        @Override
        public void visit(MJSimpleEnumEntry node) {
            String name = node.getI1();
            environment.declareNextEnumConstant(name, node);
        }

        @Override
        public void visit(MJEnumEntryWithValue node) {
            String name = node.getI1();
            int value = node.getI2();
            environment.declareEnumConstant(name, value, node);
        }
    }

    private final class ClassDeclarationVisitor extends VisitorAdaptor {
        @Override
        public void visit(MJClassDeclaration node) {
            String name = node.getI2();
            environment.enterClassDeclaration(name, node);

            node.getModifiers_opt().traverseBottomUp(modifierVisitor);

            node.getSuper_opt().accept(classDeclarationVisitor);

            node.getClass_body().traverseBottomUp(classDeclarationVisitor);

            node.symbol = environment.exitClassDeclaration(node);
        }

        @Override
        public void visit(MJSuper node) {
            MJType typeNode = node.getMJType();
            typeNode.accept(typeVisitor);
            environment.setType(typeNode.type, node);
        }

        @Override
        public void visit(MJNoSuperClass node) {
            environment.setSuper(BuiltIn.VOID);
        }

        @Override
        public void visit(MJClassFieldDeclaration node) {
            environment.requireAbstractClassFields(node);
            node.getField_declaration().accept(variableDeclarationVisitor);
        }

        @Override
        public void visit(MJAbstractMethodDeclaration node) {
            environment.requireAbstractMethod(node);
            node.getMethod_declaration().accept(methodDeclarationVisitor);
        }

        @Override
        public void visit(MJMethodsBlockOption node) {
            environment.requireMethodBlock(node.getParent());
        }

        @Override
        public void visit(MJMethodDeclarationElement node) {
            node.getMethod_declaration().accept(methodDeclarationVisitor);
        }
    }

    private final class MethodDeclarationVisitor extends VisitorAdaptor {
        @Override
        public void visit(MJMethodDeclaration node) {
            node.getMethod_signature().accept(this);

            boolean hasAbstractBody = node.getMethod_body() instanceof MJAbstractMethodBody;

            environment.requireMethodBody(hasAbstractBody, node);

            node.getMethod_variables_opt().traverseBottomUp(methodDeclarationVisitor);
            node.getMethod_body().accept(methodDeclarationVisitor);

            node.methodsymbol = environment.exitMethodDeclaration(node);
        }

        @Override
        public void visit(MJTypeMethodSignature node) {
            environment.enterMethodDeclaration();

            node.getModifiers_opt().traverseBottomUp(modifierVisitor);

            MJType typeNode = node.getMJType();
            typeNode.accept(typeVisitor);
            environment.setType(typeNode.type, node);

            String name = node.getI3();
            environment.declareMethod(name, node);
            node.getFormal_parameters_opt().traverseBottomUp(methodDeclarationVisitor);
        }

        @Override
        public void visit(MJVoidMethodSignature node) {
            environment.enterMethodDeclaration();

            node.getModifiers_opt().traverseBottomUp(modifierVisitor);
            environment.setType(BuiltIn.VOID, node);

            String name = node.getI2();
            environment.declareMethod(name, node);
            node.getFormal_parameters_opt().traverseBottomUp(methodDeclarationVisitor);
        }

        @Override
        public void visit(MJParameter node) {
            MJType typeNode = node.getMJType();
            String typeName = typeNode.getI1();
            Type type = environment.findType(typeName, typeNode).getSymbolType();
            DeclaratorInfo info = extractDeclarator(
                    type,
                    node.getVariable_declarator_id(),
                    null
            );
            node.symbol = environment.declareParameter(info, node);
        }

        @Override
        public void visit(MJMethodVariable node) {
            node.getField_declaration().accept(variableDeclarationVisitor);
        }

        @Override
        public void visit(MJMethodBody node) {
            node.getBlock().accept(statementVisitor);
        }
    }

    private final class StatementVisitor extends VisitorAdaptor {

        // If statement

        @Override
        public void visit(MJIfThen node) {
            node.getIf_condition().accept(statementVisitor);
            node.getStatement().accept(statementVisitor);
        }

        @Override
        public void visit(MJIfThenElse node) {
            node.getIf_condition().accept(statementVisitor);
            node.getStatement_no_short_if().accept(statementVisitor);
            node.getStatement().accept(statementVisitor);
        }

        @Override
        public void visit(MJIfThenElseNoShortIf node) {
            node.getIf_condition().accept(statementVisitor);
            node.getStatement_no_short_if().accept(statementVisitor);
            node.getStatement_no_short_if1().accept(statementVisitor);
        }

        @Override
        public void visit(MJIfCondition node) {
            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            Type type = expression.expressionvalue.getType();
            environment.requireCompatibleWith(type, BuiltIn.BOOLEAN, node);
        }

        // Switch statement

        @Override
        public void visit(MJSwitch node) {
            environment.enterSwitchStatement();

            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            Type type = expression.expressionvalue.getType();
            environment.requireCompatibleWith(type, BuiltIn.INT, node);

            node.getSwitch_block().accept(statementVisitor);

            environment.exitSwitchStatement();
        }

        @Override
        public void visit(MJSwitchStatements node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJFirstSwitchStatementGroups node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJNextSwitchStatementsGroups node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJSwitchStatementsGroup node) {
            node.getSwitch_labels().traverseBottomUp(statementVisitor);
            node.getBlock_statements().accept(statementVisitor);
        }

        @Override
        public void visit(MJSwitchLabelValue node) {
            int value = node.getI1();
            environment.declareSwitchCase(value, node);
        }

        // For loop

        @Override
        public void visit(MJFor node) {
            node.getFor_init_opt().accept(statementVisitor);

            Expression_opt expression = node.getExpression_opt();
            expression.accept(expressionVisitor);
            Type type = expression.expressionvalue.getType();
            environment.requireCompatibleWith(type, BuiltIn.BOOLEAN, node);

            node.getFor_update_opt().accept(statementVisitor);
            node.getStatement().accept(statementVisitor);
        }

        @Override
        public void visit(MJForNoShortIf node) {
            node.getFor_init_opt().accept(statementVisitor);

            Expression_opt expression = node.getExpression_opt();
            expression.accept(expressionVisitor);
            Type type = expression.expressionvalue.getType();
            environment.requireCompatibleWith(type, BuiltIn.BOOLEAN, node);

            node.getFor_update_opt().accept(statementVisitor);
            node.getStatement_no_short_if().accept(statementVisitor);
        }

        @Override
        public void visit(MJForInitOpt node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJForInit node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJForUpdateOpt node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJForUpdate node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJFirstStatementExpression node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJNextStatementExpression node) {
            node.childrenAccept(statementVisitor);
        }

        // Break

        @Override
        public void visit(MJBreak node) {
            environment.requireBreak(node);
        }

        // Continue

        @Override
        public void visit(MJContinue node) {
            environment.requireContinue(node);
        }

        // Return

        @Override
        public void visit(MJReturn node) {
            Expression_opt expression = node.getExpression_opt();
            expression.accept(expressionVisitor);
            Type type = expression.expressionvalue.getType();
            environment.requireReturnType(type, node);
        }

        // Traversal control

        @Override
        public void visit(MJBlock node) {
            environment.enterBlockStatement();
            node.childrenAccept(statementVisitor);
            environment.exitBlockStatement();
        }

        @Override
        public void visit(MJBlockStatements node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJFirstBlockStatement node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJNextBlockStatement node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJBlockStatement node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJStatementWithoutTrailingSubstatement node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJStatementWithoutTrailingSubstatementNoShortIf node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJStatementBlock node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJExpressionStatement node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJStatementExpression node) {
            node.childrenAccept(statementVisitor);
        }

        @Override
        public void visit(MJIfThenStatement node) {
            node.getIf_then_statement().accept(statementVisitor);
        }

        @Override
        public void visit(MJIfThenElseStatement node) {
            node.getIf_then_else_statement().accept(statementVisitor);
        }

        @Override
        public void visit(MJForStatement node) {
            node.getFor_statement().accept(statementVisitor);
        }

        @Override
        public void visit(MJIfThenElseStatementNoShortIf node) {
            node.getIf_then_else_statement_no_short_if().accept(statementVisitor);
        }

        @Override
        public void visit(MJForStatementNoShortIf node) {
            node.getFor_statement_no_short_if().accept(statementVisitor);
        }

        @Override
        public void visit(MJSwitchStatement node) {
            node.getSwitch_statement().accept(statementVisitor);
        }

        @Override
        public void visit(MJBreakStatement node) {
            node.getBreak_statement().accept(statementVisitor);
        }

        @Override
        public void visit(MJContinueStatement node) {
            node.getContinue_statement().accept(statementVisitor);
        }

        @Override
        public void visit(MJReturnStatement node) {
            node.getReturn_statement().accept(statementVisitor);
        }

        @Override
        public void visit(MJAssignmentStatementExpression node) {
            node.getAssignment().accept(expressionVisitor);
        }

        @Override
        public void visit(MJPostincrementStatementExpression node) {
            node.getPostincrement_expression().accept(expressionVisitor);
        }

        @Override
        public void visit(MJPostdecrementStatementExpression node) {
            node.getPostdecrement_expression().accept(expressionVisitor);
        }

        @Override
        public void visit(MJMethodInvocationStatementExpression node) {
            node.getMethod_invocation().accept(expressionVisitor);
        }

        @Override
        public void visit(MJClassInstanceCreationStatementExpression node) {
            node.getClass_instance_creation_expression().accept(expressionVisitor);
        }
    }

    private final class ExpressionVisitor extends VisitorAdaptor {
        @Override
        public void visit(MJNameSimple node) {
            Simple_name name = node.getSimple_name();
            name.accept(expressionVisitor);
            node.expressionvalue = name.expressionvalue;
        }

        @Override
        public void visit(MJNameQualified node) {
            Qualified_name name = node.getQualified_name();
            name.accept(expressionVisitor);
            node.expressionvalue = name.expressionvalue;
        }

        @Override
        public void visit(MJSimpleName node) {
            String name = node.getI1();
            Symbol object = environment.find(name, node);
            node.expressionvalue = new ExpressionValue(object);
        }

        @Override
        public void visit(MJQualifiedName node) {
            Name name = node.getName();
            name.accept(expressionVisitor);
            Type type = name.expressionvalue.getType();

            String field = node.getI2();
            Symbol fieldObject = environment.findIn(field, type, node);
            node.expressionvalue = new ExpressionValue(fieldObject);
        }

        @Override
        public void visit(MJLength node) {
            Name name = node.getName();
            name.accept(expressionVisitor);
            Type type = name.expressionvalue.getType();

            node.expressionvalue = new ExpressionValue(environment.requireLength(type, node));
        }

        @Override
        public void visit(MJPrimaryNoNewArray node) {
            Primary_no_new_array primary = node.getPrimary_no_new_array();
            primary.accept(expressionVisitor);
            node.expressionvalue = primary.expressionvalue;
        }

        @Override
        public void visit(MJPrimaryClassInstanceCreationExpression node) {
            Class_instance_creation_expression expression = node.getClass_instance_creation_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJPrimaryArrayCreationExpression node) {
            Array_creation_expression expression = node.getArray_creation_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJPrimaryLiteral node) {
            Literal literal = node.getLiteral();
            literal.traverseBottomUp(new LiteralVisitor());
            node.expressionvalue = new ExpressionValue(literal.type);
        }

        @Override
        public void visit(MJThis node) {
            node.expressionvalue = new ExpressionValue(environment.requireThis(node));
        }

        @Override
        public void visit(MJParenthesisedExpression node) {
            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJPrimaryFieldAccess node) {
            Field_access fieldAccess = node.getField_access();
            fieldAccess.accept(expressionVisitor);
            node.expressionvalue = fieldAccess.expressionvalue;
        }

        @Override
        public void visit(MJPrimaryMethodInvocation node) {
            Method_invocation methodInvocation = node.getMethod_invocation();
            methodInvocation.accept(expressionVisitor);
            node.expressionvalue = methodInvocation.expressionvalue;
        }

        @Override
        public void visit(MJPrimaryArrayAccess node) {
            Array_access arrayAccess = node.getArray_access();
            arrayAccess.accept(expressionVisitor);
            node.expressionvalue = arrayAccess.expressionvalue;
        }

        @Override
        public void visit(MJClassInstanceCreation node) {
            String name = node.getMJType().getI1();
            node.expressionvalue = new ExpressionValue(environment.requireClassType(name, node.getMJType()));
        }

        @Override
        public void visit(MJArgumentList node) {
            Argument_list argumentList = node.getArgument_list();
            argumentList.accept(expressionVisitor);
            node.expressionvalue = argumentList.expressionvalue;
        }

        @Override
        public void visit(MJNoArgumentList node) {
            node.expressionvalue = new ExpressionValue();
        }

        @Override
        public void visit(MJArgument node) {
            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            Type type = expression.expressionvalue.getType();

            node.expressionvalue = new ExpressionValue();
            node.expressionvalue.getList().add(type);
        }

        @Override
        public void visit(MJNextArgument node) {
            Argument_list argumentList = node.getArgument_list();
            argumentList.accept(expressionVisitor);
            node.expressionvalue = argumentList.expressionvalue;

            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            Type type = expression.expressionvalue.getType();

            node.expressionvalue.getList().add(type);
        }

        @Override
        public void visit(MJArrayCreation node) {
            MJType type = node.getMJType();
            type.accept(typeVisitor);

            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);

            node.expressionvalue = new ExpressionValue(
                    environment.requireArray(type.type, expression.expressionvalue.getType(), node)
            );
        }

        @Override
        public void visit(MJFieldAccess node) {
            Primary primary = node.getPrimary();
            primary.accept(expressionVisitor);
            Type type = primary.expressionvalue.getType();

            String field = node.getI2();
            Symbol fieldObject = environment.findIn(field, type, node);
            node.expressionvalue = new ExpressionValue(fieldObject);
        }

        @Override
        public void visit(MJLengthFieldAcces node) {
            Primary primary = node.getPrimary();
            primary.accept(expressionVisitor);
            Type type = primary.expressionvalue.getType();

            node.expressionvalue = new ExpressionValue(environment.requireLength(type, node));
        }

        @Override
        public void visit(MJMethodInvocation node) {
            Name name = node.getName();
            name.accept(expressionVisitor);
            Symbol method = name.expressionvalue.getSymbol();

            Argument_list_opt argumentsList = node.getArgument_list_opt();
            argumentsList.accept(expressionVisitor);
            ExpressionValue expressionValue = argumentsList.expressionvalue;

            environment.requireMethodInvocation(method, expressionValue, node);

            node.expressionvalue = new ExpressionValue(method.getSymbolType());
        }

        @Override
        public void visit(MJQualifiedMethodInvocation node) {
            Primary primary = node.getPrimary();
            primary.accept(expressionVisitor);
            Type type = primary.expressionvalue.getType();

            String methodName = node.getI2();
            Symbol method = environment.findIn(methodName, type, node);

            Argument_list_opt argumentsList = node.getArgument_list_opt();
            argumentsList.accept(expressionVisitor);
            ExpressionValue expressionValue = argumentsList.expressionvalue;

            environment.requireMethodInvocation(method, expressionValue, node);

            node.expressionvalue = new ExpressionValue(method.getSymbolType());
        }

        @Override
        public void visit(MJRead node) {
            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            environment.requireRead(expression.expressionvalue.getSymbol(), node);
            node.expressionvalue = new ExpressionValue();
        }

        @Override
        public void visit(MJPrint node) {
            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            environment.requirePrint(expression.expressionvalue.getType(), node);
            node.expressionvalue = new ExpressionValue();
        }

        @Override
        public void visit(MJPrintConst node) {
            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            environment.requirePrint(expression.expressionvalue.getType(), node);
            node.expressionvalue = new ExpressionValue();
        }

        @Override
        public void visit(MJArrayAccess node) {
            Name name = node.getName();
            name.accept(expressionVisitor);
            Type type = name.expressionvalue.getType();

            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            Type indexType = expression.expressionvalue.getType();

            node.expressionvalue = new ExpressionValue(
                    environment.requireArrayAccess(type, indexType, node)
            );
        }

        @Override
        public void visit(MJQualifiedArrayAccess node) {
            Primary_no_new_array primary = node.getPrimary_no_new_array();
            primary.accept(expressionVisitor);
            Type type = primary.expressionvalue.getType();

            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            Type indexType = expression.expressionvalue.getType();

            node.expressionvalue = new ExpressionValue(
                    environment.requireArrayAccess(type, indexType, node)
            );
        }

        @Override
        public void visit(MJPrimaryExpression node) {
            Primary primary = node.getPrimary();
            primary.accept(expressionVisitor);
            node.expressionvalue = primary.expressionvalue;
        }

        @Override
        public void visit(MJNameExpression node) {
            Name name = node.getName();
            name.accept(expressionVisitor);
            node.expressionvalue = name.expressionvalue;
        }

        @Override
        public void visit(MJPostincrementExpression node) {
            Postincrement_expression expression = node.getPostincrement_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJPostdecrementExpression node) {
            Postdecrement_expression expression = node.getPostdecrement_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJPostincrement node) {
            Postfix_expression expression = node.getPostfix_expression();
            expression.accept(expressionVisitor);
            Symbol variable = expression.expressionvalue.getSymbol();
            node.expressionvalue = new ExpressionValue(Operators.INC.check(variable, environment, node));
        }

        @Override
        public void visit(MJPostdecrement node) {
            Postfix_expression expression = node.getPostfix_expression();
            expression.accept(expressionVisitor);
            Symbol variable = expression.expressionvalue.getSymbol();
            node.expressionvalue = new ExpressionValue(Operators.DEC.check(variable, environment, node));
        }

        @Override
        public void visit(MJNegation node) {
            Unary_expression expression = node.getUnary_expression();
            expression.accept(expressionVisitor);
            Symbol variable = expression.expressionvalue.getSymbol();
            node.expressionvalue = new ExpressionValue(Operators.NEG.check(variable, environment, node));
        }

        @Override
        public void visit(MJPostfixExpression node) {
            Postfix_expression postfixExpression = node.getPostfix_expression();
            postfixExpression.accept(expressionVisitor);
            node.expressionvalue = postfixExpression.expressionvalue;
        }

        @Override
        public void visit(MJUnaryExpression node) {
            Unary_expression expression = node.getUnary_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJMultiplication node) {
            Multiplicative_expression left = node.getMultiplicative_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Unary_expression right = node.getUnary_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.MUL.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.MUL.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJDivision node) {
            Multiplicative_expression left = node.getMultiplicative_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Unary_expression right = node.getUnary_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.DIV.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.DIV.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJModulo node) {
            Multiplicative_expression left = node.getMultiplicative_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Unary_expression right = node.getUnary_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.MOD.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.MOD.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJMultiplicativeExpression node) {
            Multiplicative_expression expression = node.getMultiplicative_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJAddition node) {
            Additive_expression left = node.getAdditive_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Multiplicative_expression right = node.getMultiplicative_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.ADD.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.ADD.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJSubtraction node) {
            Additive_expression left = node.getAdditive_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Multiplicative_expression right = node.getMultiplicative_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.SUB.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.SUB.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJAdditiveExpression node) {
            Additive_expression expression = node.getAdditive_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJLessThan node) {
            Relational_expression left = node.getRelational_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Additive_expression right = node.getAdditive_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.LT.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.LT.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJGreaterThan node) {
            Relational_expression left = node.getRelational_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Additive_expression right = node.getAdditive_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.GT.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.GT.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJLessThanOrEqualTo node) {
            Relational_expression left = node.getRelational_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Additive_expression right = node.getAdditive_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.LEQ.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.LEQ.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJGreaterThanOrEqualTo node) {
            Relational_expression left = node.getRelational_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Additive_expression right = node.getAdditive_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.GEQ.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.GEQ.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJRelationalExpression node) {
            Relational_expression expression = node.getRelational_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJEqual node) {
            Equaliity_expression left = node.getEqualiity_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Relational_expression right = node.getRelational_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.EQ.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.EQ.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJNotEqual node) {
            Equaliity_expression left = node.getEqualiity_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Relational_expression right = node.getRelational_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.NEQ.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.NEQ.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJEqualityExpression node) {
            Equaliity_expression expression = node.getEqualiity_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJConjuction node) {
            Conditional_and_expression left = node.getConditional_and_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Equaliity_expression right = node.getEqualiity_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.AND.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.AND.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJConditionalAndExpression node) {
            Conditional_and_expression expression = node.getConditional_and_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJDisjunction node) {
            Conditional_or_expression left = node.getConditional_or_expression();
            left.accept(expressionVisitor);
            Type leftType = left.expressionvalue.getType();

            Conditional_and_expression right = node.getConditional_and_expression();
            right.accept(expressionVisitor);
            Type rightType = right.expressionvalue.getType();

            Operators.OR.check(leftType, rightType, environment, node);
            node.expressionvalue = new ExpressionValue(Operators.OR.returnType(leftType, rightType));
        }

        @Override
        public void visit(MJConditionalOrExpression node) {
            Conditional_or_expression expression = node.getConditional_or_expression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJTernaryOperation node) {
            Conditional_or_expression condition = node.getConditional_or_expression();
            condition.accept(expressionVisitor);
            Type conditionType = condition.expressionvalue.getType();
            environment.requireCompatibleWith(conditionType, BuiltIn.BOOLEAN, node);

            Expression expressionIfTrue = node.getExpression();
            expressionIfTrue.accept(expressionVisitor);
            Type typeIfTrue = expressionIfTrue.expressionvalue.getType();

            Conditional_expression expressionIfFalse = node.getConditional_expression();
            expressionIfFalse.accept(expressionVisitor);
            Type typeIfFalse = expressionIfFalse.expressionvalue.getType();

            environment.requireCompatibleWith(typeIfFalse, typeIfTrue, node);
            node.expressionvalue = new ExpressionValue(typeIfTrue);
        }

        @Override
        public void visit(MJConditionalExpression node) {
            Conditional_expression conditionalExpression = node.getConditional_expression();
            conditionalExpression.accept(expressionVisitor);
            node.expressionvalue = conditionalExpression.expressionvalue;
        }

        @Override
        public void visit(MJAssignmentExpression node) {
            Assignment assignment = node.getAssignment();
            assignment.accept(expressionVisitor);
            node.expressionvalue = assignment.expressionvalue;
        }

        @Override
        public void visit(MJAssignment node) {
            Left_hand_side leftHandSide = node.getLeft_hand_side();
            leftHandSide.accept(expressionVisitor);
            Symbol left = leftHandSide.expressionvalue.getSymbol();

            Assignment_expression assignmentExpression = node.getAssignment_expression();
            assignmentExpression.accept(expressionVisitor);
            Type assignmentType = assignmentExpression.expressionvalue.getType();

            node.expressionvalue = new ExpressionValue(
                    environment.requireAssign(left, assignmentType, node)
            );
        }

        @Override
        public void visit(MJLeftHandSideName node) {
            Name name = node.getName();
            name.accept(expressionVisitor);
            node.expressionvalue = name.expressionvalue;
        }

        @Override
        public void visit(MJLeftHandSideFieldAccress node) {
            Field_access fieldAccess = node.getField_access();
            fieldAccess.accept(expressionVisitor);
            node.expressionvalue = fieldAccess.expressionvalue;
        }

        @Override
        public void visit(MJLeftHandSideArrayAccess node) {
            Array_access arrayAccess = node.getArray_access();
            arrayAccess.accept(expressionVisitor);
            node.expressionvalue = arrayAccess.expressionvalue;
        }

        @Override
        public void visit(MJExpressionOption node) {
            Expression expression = node.getExpression();
            expression.accept(expressionVisitor);
            node.expressionvalue = expression.expressionvalue;
        }

        @Override
        public void visit(MJNoExpression node) {
            node.expressionvalue = new ExpressionValue();
        }

        @Override
        public void visit(MJExpression node) {
            Assignment_expression assignment = node.getAssignment_expression();
            assignment.accept(expressionVisitor);
            node.expressionvalue = assignment.expressionvalue;
        }
    }
}
