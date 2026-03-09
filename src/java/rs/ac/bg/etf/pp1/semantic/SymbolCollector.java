package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.logger.CompilerDiagnostics;
import rs.ac.bg.etf.pp1.symbols.Flags;
import rs.ac.bg.etf.pp1.symbols.Flags.Modifier;
import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Symbol.*;
import rs.ac.bg.etf.pp1.symbols.SymbolTable;
import rs.ac.bg.etf.pp1.symbols.Type;
import rs.ac.bg.etf.pp1.symbols.Type.*;
import rs.ac.bg.etf.pp1.util.Context;
import rs.ac.bg.etf.pp1.util.TreeVisitor;

import java.util.HashMap;
import java.util.Map;

public final class SymbolCollector extends TreeVisitor {

    private final CompilerDiagnostics diagnostics;
    private final SymbolTable table;

    private Flags flags;
    private Type type;

    public SymbolCollector(Context context) {
        diagnostics = CompilerDiagnostics.getInstance(context);
        table = SymbolTable.getInstance(context);
    }

    public void enterProgram(MJProgram tree) {
        String programName = tree.getI1();
        table.initiate(programName);

        tree.getStatic_declarations_opt().accept(this);
        tree.getStatic_methods_block().accept(this);

        tree.programsymbol = (ProgramSymbol) table.closeScope();
    }

    // Enums

    private int nextEnumIndex = 0;

    @Override
    public void visit(MJEnumDeclaration tree) {
        String name = tree.getI1();
        table.openEnum(name);
        tree.getEnum_body().accept(this);
        tree.enumsymbol = (EnumSymbol) table.closeScope();
    }

    @Override
    public void visit(MJSimpleEnumEntry tree) {
        String name = tree.getI1();
        table.declareConstant(name, SymbolTable.INT, nextEnumIndex++);
    }

    @Override
    public void visit(MJEnumEntryWithValue tree) {
        String name = tree.getI1();
        int value = tree.getI2();
        table.declareConstant(name, SymbolTable.INT, value);
        nextEnumIndex = value + 1;
    }

    // Classes

    @Override
    public void visit(MJClassDeclaration tree) {
        getModifiers(tree.getModifiers_opt());
        checkClassModifiers(tree.getModifiers_opt());
        String name = tree.getI2();
        ClassType superType = getSuperType(tree.getSuper_opt());

        table.openClass(name, superType, flags);

        tree.getClass_body().accept(this);

        ClassSymbol clazz = (ClassSymbol) table.closeScope();
        tree.classsymbol = clazz;
        ClassType thisType = clazz.getThisType();

        if (thisType.getSuperType() == null) return;

        for (Symbol member : thisType.getClassMembers()) {
            Symbol superMember = thisType.getSuperType().find(member.getName());
            if (superMember == SymbolTable.NO_SYMBOL) continue;

            if (!member.isSameKind(superMember)) {
                diagnostics.reportError(member.getKindName() + " '" + member.getName() + "' overrides " + superMember.getKindName() + " inherited from super class.", tree.getLine());
                continue;
            }

            if (!member.isCallable()) continue;

            if (!((MethodSymbol) member).matchesSuper((MethodSymbol) superMember)) {
                diagnostics.reportError("Method '" + ((MethodSymbol) member).getFullName() + "' does not match super.", tree.getLine());
            }
        }

        if (clazz.getModifiers().has(Modifier.ABSTRACT)) return;

        for (Symbol superMethod : thisType.getSuperType().getClassMembers()) {
            if (!superMethod.isCallable()) continue;
            if (!superMethod.getModifiers().has(Modifier.ABSTRACT)) continue;
            Symbol thisMethod = thisType.findOwn(superMethod.getName());
            if (thisMethod != SymbolTable.NO_SYMBOL) continue;

            diagnostics.reportError("Class must implement all abstract methods.", tree.getLine());
        }
    }

    private ClassType getSuperType(Super_opt tree) {
        if (!(tree instanceof MJSuper))
            return null;

        Type type = getType(((MJSuper) tree).getMJType());
        if (type == SymbolTable.VOID)
            return null;

        if (!type.isClass()) {
            diagnostics.reportError("Cannot extend from '" + type + "'.", tree.getLine());
            return null;
        }

        return (ClassType) type;
    }

    // Fields

    @Override
    public void visit(MJFieldDeclaration tree) {
        getModifiers(tree.getModifiers_opt());
        type = getType(tree.getMJType());

        checkFieldModifiers(tree.getModifiers_opt());

        tree.getVariable_declarators().accept(this);
    }

    @Override
    public void visit(MJVariable tree) {
        if (flags.has(Modifier.CONST)) {
            diagnostics.reportError("Constant might not be initialized.", tree.getLine());
        }

        tree.getVariable_declarator_id().accept(this);
        VariableDeclarator declarator = tree.getVariable_declarator_id().variabledeclarator;
        if (table.findInScope(declarator.name) != SymbolTable.NO_SYMBOL) {
            diagnostics.reportError("Name `" + declarator.name + "' already declared.", tree.getLine());
            return;
        }

        if (declarator.isArray) {
            tree.dataholdersymbol = table.declareArray(declarator.name, type);
        } else {
            tree.dataholdersymbol = table.declareVariable(declarator.name, type);
        }
    }

    @Override
    public void visit(MJConstant tree) {
        if (!flags.has(Modifier.CONST))
            diagnostics.reportError("Variables cannot be initialized here.", tree.getLine());

        tree.getVariable_declarator_id().accept(this);
        VariableDeclarator declarator = tree.getVariable_declarator_id().variabledeclarator;
        if (table.findInScope(declarator.name) != SymbolTable.NO_SYMBOL) {
            diagnostics.reportError("Name `" + declarator.name + "' already declared.", tree.getLine());
            return;
        }

        if (declarator.isArray) {
            diagnostics.reportError("Arrays cannot be constant.", tree.getLine());
            tree.dataholdersymbol = table.declareArray(declarator.name, type);
            return;
        }

        tree.getConst_literal().accept(this);
        if (tree.getConst_literal().expressionvalue.type != type)
            diagnostics.reportError("Cannot convert " + tree.getConst_literal().expressionvalue.type + " to " + type, tree.getLine());

        int value = tree.getConst_literal().expressionvalue.value;

        tree.dataholdersymbol = table.declareConstant(declarator.name, type, value);
    }

    @Override
    public void visit(MJVariableName tree) {
        String name = tree.getI1();
        tree.variabledeclarator = new VariableDeclarator(name, false);
    }

    @Override
    public void visit(MJArrayName tree) {
        String name = tree.getI1();
        tree.variabledeclarator = new VariableDeclarator(name, true);
    }

    // Methods

    @Override
    public void visit(MJMethodDeclaration tree) {
        tree.getMethod_signature().accept(this);
        tree.getMethod_variables_opt().accept(this);

        MethodSymbol method = (MethodSymbol) table.closeScope();
        tree.methodsymbol = method;
        method.setBody(tree.getMethod_body());

        if (table.currentWritable() instanceof ProgramSymbol && method.getName().equals("main")) {
            method.setMain();
        }
    }

    @Override
    public void visit(MJTypeMethodSignature tree) {
        getModifiers(tree.getModifiers_opt());
        type = getType(tree.getMJType());
        String name = tree.getI3();

        declareMethod(name, type, tree.getFormal_parameters_opt(), tree);
    }

    @Override
    public void visit(MJVoidMethodSignature tree) {
        getModifiers(tree.getModifiers_opt());
        type = SymbolTable.VOID;
        String name = tree.getI2();
        declareMethod(name, type, tree.getFormal_parameters_opt(), tree);
    }

    private void declareMethod(
            String name,
            Type returnType,
            Formal_parameters_opt parametersTree,
            SyntaxNode tree
    ) {
        if (table.find(name) != SymbolTable.NO_SYMBOL) {
            diagnostics.reportError("Name '" + name + "' already declared.", tree.getLine());
            // to allow the semantic pass to continue, we still must create this method symbol,
            // though it won't be added to the table.
        }

        checkMethodModifiers(tree);

        table.openMethod(name, returnType, flags);

        parametersTree.accept(this);
    }

    @Override
    public void visit(MJParameter tree) {
        type = getType(tree.getMJType());
        tree.getVariable_declarator_id().accept(this);
        VariableDeclarator declarator = tree.getVariable_declarator_id().variabledeclarator;
        if (table.findInScope(declarator.name) != SymbolTable.NO_SYMBOL) {
            diagnostics.reportError("Name `" + declarator.name + "' already declared.", tree.getLine());
            return;
        }
        tree.parametersymbol = table.declareParameter(declarator.name, type, declarator.isArray);
    }

    // Literals

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

    // Modifiers

    @Override
    public void visit(MJConstModifier tree) {
        if (flags.has(Modifier.CONST)) {
            diagnostics.reportError("Repeated modifier 'const'.", tree.getLine());
        }
        flags.add(Modifier.CONST);
    }

    @Override
    public void visit(MJAbstractModifier tree) {
        if (flags.has(Modifier.ABSTRACT)) {
            diagnostics.reportError("Repeated modifier 'abstract'.", tree.getLine());
        }
        flags.add(Modifier.ABSTRACT);
    }

    private void getModifiers(SyntaxNode tree) {
        flags = new Flags();
        tree.childrenAccept(this);
    }

    private void checkClassModifiers(SyntaxNode tree) {
        for (Modifier flag : flags) {
            if (flag != Modifier.ABSTRACT) {
                diagnostics.reportError("Modifier '" + flag.name() + "' not allowed here", tree.getLine());
                flags.remove(flag);
            }
        }
    }

    private void checkFieldModifiers(SyntaxNode tree) {
        for (Modifier flag : flags) {
            if (flag != Modifier.CONST) {
                diagnostics.reportError("Modifier '" + flag.name() + "' not allowed here.", tree.getLine());
                flags.remove(flag);
            }
        }

        if (!flags.has(Modifier.CONST))
            return;

        if (!(table.currentWritable() instanceof ProgramSymbol)) {
            diagnostics.reportError("Constants can only be declared in the global scope.", tree.getLine());
            flags.remove(Modifier.CONST);
        }

        if (!type.isPrimitive()) {
            diagnostics.reportError("Constants must be of primitive type.", tree.getLine());
            flags.remove(Modifier.CONST);
        }
    }

    private void checkMethodModifiers(SyntaxNode tree) {
        for (Modifier flag : flags) if (flag != Modifier.ABSTRACT) {
            diagnostics.reportError("Modifier '" + flag.name() + "' not allowed here.", tree.getLine());
            flags.remove(flag);
        }

        if (!table.currentWritable().getModifiers().has(Modifier.ABSTRACT) && flags.has(Modifier.ABSTRACT)) {
            diagnostics.reportError("Modifier 'abstract' not allowed here.", tree.getLine());
            flags.remove(Modifier.ABSTRACT);
        }
    }

    // Types

    private Type getType(MJType tree) {
        String name = tree.getI1();
        Symbol found = table.find(name);

        if (found == SymbolTable.NO_SYMBOL) {
            diagnostics.reportError("Type `" + name + "' not found.", tree.getLine());
            return SymbolTable.VOID;
        }

        if (!found.isType()) {
            diagnostics.reportError("`" + name + "' is not a type.", tree.getLine());
            return SymbolTable.VOID;
        }

        return found.getSymbolType();
    }
}
