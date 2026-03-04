package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.ast.MJMethodDeclaration;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.logger.CompilerDiagnostics;
import rs.ac.bg.etf.pp1.symbols.*;
import rs.ac.bg.etf.pp1.util.Context;

import static rs.ac.bg.etf.pp1.symbols.Symbol.*;
import static rs.ac.bg.etf.pp1.symbols.Type.*;

public final class Environment {
    private final CompilerDiagnostics diagnostics;
    private ScopeContext context;
    private final SymbolTable table;

    public Environment(Context context) {
        this.table = SymbolTable.getInstance(context);
        this.diagnostics = CompilerDiagnostics.getInstance(context);
    }

    public void error(String message, SyntaxNode node) {
        diagnostics.reportError(message, node.getLine());
    }

    public Symbol insert(
            SymbolKind kind,
            String name,
            Type type,
            SyntaxNode node
    ) {
        if (table.isInScope(name)) {
            error("Name `" + name + "' already declared.", node);
            return BuiltIn.NO_OBJECT;
        }

        return table.insert(kind, name, type, node);
    }

    public Symbol findType(String name, SyntaxNode node) {
        Symbol type = table.find(name);

        if (type == BuiltIn.NO_OBJECT) {
            error("Type `" + name + "` not found.", node);
        }

        if (type.getMJKind() != SymbolKind.TYPE) {
            error("`" + name + "` is not a type.", node);
            return BuiltIn.NO_OBJECT;
        }

        return type;
    }

    // const modifier

    public void requireConstAllowed(SyntaxNode node) {
        if (!context.isConstantAllowed()) {
            error("`const` modifier is not allowed here.", node);
            return;
        }
        context.isConstant = true;
    }

    // abstract modifier

    public void requireAbstractAllowed(SyntaxNode node) {
        if (!context.isAbstractAllowed()) {
            error("`abstract` modifier is not allowed here.", node);
            return;
        }
        context.isAbstract = true;
    }

    private boolean inAbstractClass() {
        return isInClassDeclaration() && context.outer.isAbstract;
    }

    // Type

    public void setType(Type type, SyntaxNode node) {
        if (!context.setType(type)) {
            error("Type `" + type.getName() + "` is not allowed here.", node);
            return;
        }

        if (isInClassDeclaration()) {
            setSuper(type);
        } else {
            context.currentType = type;
        }
    }

    public Type getType() {
        return context.currentType;
    }

    // Program

    public void enterProgram(String name, SyntaxNode node) {
        Symbol program = insert(SymbolKind.PROGRAM, name, BuiltIn.VOID, node);
        context = new ScopeContext.ProgramContext(context, (ProgramSymbol) program);
        table.openScope();
    }

    public ProgramSymbol exitProgram() {
        ProgramSymbol program = (ProgramSymbol) context.getObject();
        table.chainLocalsTo(program);
        table.closeScope();
        context = context.outer;
        return program;
    }

    // Field declaration

    public void enterVariableDeclaration() {
        context = new ScopeContext.VariableDeclarationContext(context);
    }

    public Symbol declareVariable(DeclaratorInfo info, SyntaxNode node) {
        SymbolKind kind = context.outer instanceof ScopeContext.ClassDeclarationContext
                ? SymbolKind.FIELD
                : info.hasInitializer
                    ? SymbolKind.CONSTANT
                    : SymbolKind.VARIABLE;
        Symbol variable = insert(kind, info.name, info.type, node);

        if (!context.isConstant) {
            return variable;
        }

        if (info.isArray) {
            error("Array `" + info.name + "' cannot be constant.'", node);
        } else if (!info.hasInitializer) {
            error("Constant `" + info.name + "' might not be initialized.", node);
        } else {
            variable.setAddress(info.value);
        }

        return variable;
    }

    public void exitVariableDeclaration() {
        context = context.outer;
    }

    public void requireInitialization(SyntaxNode node) {
        if (!context.isConstant) {
            error("Variable cannot be initialized here.", node);
        }
    }

    // Enum declaration

    public void enterEnumDeclaration(String name, SyntaxNode node) {
        Symbol enumType = insert(SymbolKind.TYPE, name, new MJType(TypeKind.ENUM, name), node);
        context = new ScopeContext.EnumDeclarationContext(context, enumType);
        table.openScope();
    }

    public void declareNextEnumConstant(String name, SyntaxNode node) {
        Symbol constant = insert(SymbolKind.CONSTANT, name, BuiltIn.INT, node);
        int value = ((ScopeContext.EnumDeclarationContext) context).getNext();
        constant.setAddress(value);
    }

    public void declareEnumConstant(String name, int value, SyntaxNode node) {
        Symbol constant = insert(SymbolKind.CONSTANT, name, BuiltIn.INT, node);
        ((ScopeContext.EnumDeclarationContext) context).setNext(value);
        constant.setAddress(value);
    }

    public void exitEnumDeclaration() {
        Symbol object = context.getObject();
        table.chainMembersTo(object.getSymbolType());
        table.closeScope();
        context = context.outer;
    }

    // Class declaration

    public void enterClassDeclaration(String name, SyntaxNode node) {
        Type type = new MJType(TypeKind.CLASS, name);
        ClassSymbol clazz = (ClassSymbol) insert(SymbolKind.TYPE, name, type, node);
        context = new ScopeContext.ClassDeclarationContext(context, clazz);
        table.openScope();
    }

    public void setSuper(Type type) {
        context.getObject().getSymbolType().setElementType(type);
        for (Symbol member : type.getMJMembers()) {
            if (member.getMJKind().isCallable()) continue;
            table.insert(member);
        }
    }

    public ClassSymbol exitClassDeclaration(SyntaxNode node) {
        addSuperMethods(node);
        context.setModifiers();

        ClassSymbol clazz = (ClassSymbol) context.getObject();
        context = context.outer;
        table.chainMembersTo(clazz.getSymbolType());
        table.closeScope();
        return clazz;
    }

    private void addSuperMethods(SyntaxNode node) {
        Type superType = context.getObject().getSymbolType().getElementType();
        if (superType == BuiltIn.VOID) return;

        for (Symbol member : superType.getMJMembers()) {
            if (!member.getMJKind().isCallable()) continue;
            if (table.isInScope(member.getName())) continue;
            table.insert(member);
            if (member.isAbstract()) {
                error("Class `" + context.getObject().getName() + "` must implement all abstract methods.", node);
            }
        }
    }

    public boolean isInClassDeclaration() {
        return context instanceof ScopeContext.ClassDeclarationContext;
    }

    public boolean isInClass() {
        return context.isInClass();
    }

    public void requireAbstractClassFields(SyntaxNode node) {
        if (context.isAbstract) {
            error("Abstract class cannot have fields.", node);
        }
    }

    public void requireAbstractMethod(SyntaxNode node) {
        if (!context.isAbstract) {
            error("Field declaration expected.", node);
        }
    }

    public void requireMethodBlock(SyntaxNode node) {
        if (context.isAbstract) {
            error("Abstract class cannot have method declaration block.", node);
        }
    }

    // Method declaration

    public void enterMethodDeclaration() {
        context = new ScopeContext.MethodDeclarationContext(context);
    }

    public void declareMethod(String name, SyntaxNode node) {
        MethodSymbol method = (MethodSymbol) insert(SymbolKind.METHOD, name, getType(), ((MJMethodDeclaration) node.getParent()).getMethod_body());
        context.setObject(method);

        table.openScope();

        if (isInClass()) {
            Symbol clazz = context.outer.getObject();
            declareParameter(new DeclaratorInfo(clazz.getSymbolType(), "this"), node);
            method.setOwner(clazz);
        } else {
            if (method.getName().equals("main")) {
                method.setMain(true);
            }
        }
    }

    public Symbol declareParameter(DeclaratorInfo info, SyntaxNode node) {
        Symbol parameter = insert(SymbolKind.VARIABLE, info.name, info.type, node);
        context.getObject().increaseLevel();
        return parameter;
    }

    public MethodSymbol exitMethodDeclaration(SyntaxNode node) {
        MethodSymbol method = (MethodSymbol) context.getObject();

        context.setModifiers();

        table.chainLocalsTo(method);
        table.closeScope();
        context = context.outer;

        String name = method.getName();
        if (!isInClassDeclaration()) return method;

        Symbol superMember = context.getObject().getSymbolType().getElementType().findMember(name);

        if (superMember == BuiltIn.NO_OBJECT) {
            return method;
        }

        if (!superMember.getMJKind().isCallable()) {
            error("Cannot override non-method `" + name + "'.", node);
        }

        MethodSymbol superMethod = (MethodSymbol) superMember;

        if (!method.equalsExactly(superMethod)) {
            error("Cannot override method `" + name + "' with different signature. Expected "
                    + superMethod.formatParameterTypes()
                    + ", got "
                    + method.formatParameterTypes()
                    + ".", node);
        }

        if (method.getType() != superMethod.getType()) {
            error("Incompatible return type. Expected `"
                    + superMethod.getSymbolType().getName()
                    + "`, got `"
                    + method.getSymbolType().getName()
                    + "`.", node);
        }

        return method;
    }

    public void requireMethodBody(boolean hasAbstractBody, SyntaxNode node) {
        if (hasAbstractBody && !context.isAbstract && inAbstractClass()) {
            error("Method body or `abstract` expected.", node);
        }
        if (hasAbstractBody && !context.isAbstract && !inAbstractClass()) {
            error("Method body expected.", node);
        }
        if (!hasAbstractBody && context.isAbstract) {
            error("Abstract method cannot have body.", node);
        }
    }

    // Statements

    public void enterBlockStatement() {
        context = new ScopeContext.BlockContext(context);
    }

    public void exitBlockStatement() {
        context = context.outer;
    }

    public void requireCompatibleWith(Type type, Type required, SyntaxNode node) {
        if (!type.isCompatibleWith(required)) {
            error("Incompatible types. `" + type.getName()
                    + "` cannot be converted to `" + required.getName() + "`.", node);
        }
    }

    public void enterSwitchStatement() {
        context = new ScopeContext.SwitchContext(context);
    }

    public void declareSwitchCase(int value, SyntaxNode node) {
        if (((ScopeContext.SwitchContext) context).newCase(value)) {
            error("Duplicate case value `" + value + "'.", node);
        }
    }
    public void exitSwitchStatement() {
        context = context.outer;
    }

    public void requireBreak(SyntaxNode node) {
        if (!context.isBreakAllowed()) {
            error("`break` is not allowed here.", node);
        }
    }

    public void requireContinue(SyntaxNode node) {
        if (!context.isContinueAllowed()) {
            error("`continue` is not allowed here.", node);
        }
    }

    public void requireReturnType(Type type, SyntaxNode node) {
        Type required = context.getObject().getSymbolType();

        if (required == BuiltIn.VOID && type != BuiltIn.VOID) {
            error("Cannot return a value from a method with void result type.", node);
            return;
        }

        if (required != BuiltIn.VOID && type == BuiltIn.VOID) {
            error("Missing return value.", node);
            return;
        }

        requireCompatibleWith(type, required, node);
    }

    public Symbol findIn(String field, Type type, SyntaxNode node) {
        if (type == BuiltIn.VOID) {
            return BuiltIn.NO_OBJECT;
        }

        Symbol member;
        if (type == context.getThisType()) {
            member = table.findInThisScope(field);
        } else {
            member = type.findMember(field);
        }

        if (member == BuiltIn.NO_OBJECT) {
            error("Cannot resolve symbol `" + field + "' in `" + type.getName() + "'.", node);
        }
        return member;
    }

    // Expressions

    public Symbol find(String name, SyntaxNode node) {
        Symbol symbol = table.find(name);

        if (symbol == BuiltIn.NO_OBJECT) {
            error("Cannot resolve symbol `" + name + "'.", node);
        }

        return symbol;
    }

    public Type requireThis(SyntaxNode node) {
        if (!context.isThisAllowed()) {
            error("`this` is not allowed here.", node);
        }
        return context.getThisType();
    }

    public Type requireLength(Type type, SyntaxNode node) {
        if (!type.getMJKind().isArray()) {
            error("`length` can only be used on arrays.", node);
            return BuiltIn.VOID;
        }
        return BuiltIn.INT;
    }

    public void requireClass(Symbol clazz, SyntaxNode node) {
        String name = clazz.getName();
        if (clazz.getSymbolType().getMJKind() != TypeKind.CLASS) {
            error("`" + name + "` is not a class.", node);
            return;
        }
        if (clazz.isAbstract()) {
            error("Cannot instantiate abstract class `" + name + "'.", node);
        }
    }

    public Type requireArray(Type type, Type expressionType, SyntaxNode node) {
        if (type == BuiltIn.VOID) {
            return BuiltIn.VOID;
        }

        requireCompatibleWith(expressionType, BuiltIn.INT, node);

        return new MJType(TypeKind.ARRAY, type);
    }

    public void requireMethodInvocation(Symbol symbol, ExpressionValue expressionValue, SyntaxNode node) {
        if (!symbol.getMJKind().isCallable()) {
            error("Cannot invoke non-method `" + symbol.getName() + "'.", node);
            return;
        }

        if (!(symbol instanceof MethodSymbol)) {
            throw new AssertionError("Expepcted method symbol");
        }

        MethodSymbol method = (MethodSymbol) symbol;

        if (!method.acceptsArguments(expressionValue.getList())) {
            error("Expected "
                    + method.formatParameterTypes()
                    + ", got "
                    + expressionValue.formatParameterTypes()
                    + ".", node);
        }
    }

    public void requireRead(Symbol variable, SyntaxNode node) {
        if (!variable.getMJKind().isAssignable()) {
            error("Variable expected.", node);
            return;
        }

        if (!variable.getSymbolType().getMJKind().isPrimitive()) {
            error("Cannot read to `" + variable.getSymbolType().getName() + "`.", node);
        }
    }

    public void requirePrint(Type type, SyntaxNode node) {
        if (!type.getMJKind().isPrimitive()) {
            error("Cannot print non-primitive type.", node);
        }
    }

    public Symbol requireArrayAccess(Type type, Type indexType, SyntaxNode node) {
        if (indexType != BuiltIn.INT) {
            error("Array index must be of type `int'.", node);
        }

        if (type == BuiltIn.VOID) {
            return BuiltIn.NO_OBJECT;
        }

        if (!type.getMJKind().isArray()) {
            error("Cannot access non-array `" + type.getName() + "'.", node);
        }

        return new MJSymbol(SymbolKind.ELEMENT, "$DUMMY", type.getElementType());
    }

    public Type requireAssign(Symbol left, Type assignmentType, SyntaxNode node) {
        if (!left.getMJKind().isAssignable()) {
            error("Cannot assign to `" + left.getName() + "'.", node);
            return BuiltIn.VOID;
        }

        if (!assignmentType.isAssignableTo(left.getSymbolType())) {
            error("Can't convert `" + assignmentType.getName()
                    + "' to `" + left.getSymbolType().getName() + "'.", node);
        }

        return left.getSymbolType();
    }
}
