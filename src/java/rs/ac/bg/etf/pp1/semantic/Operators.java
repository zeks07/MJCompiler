package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.symbols.BuiltIn;
import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Type;

public final class Operators {
    private Operators() {}

    public static final UnaryOperator NEG = new UnaryOperator("-");
    public static final AssignableUnaryOperator INC = new AssignableUnaryOperator("++");
    public static final AssignableUnaryOperator DEC = new AssignableUnaryOperator("--");
    public static final BinaryOperator ADD = new ArithmeticOperator("+");
    public static final BinaryOperator SUB = new ArithmeticOperator("-");
    public static final BinaryOperator MUL = new ArithmeticOperator("*");
    public static final BinaryOperator DIV = new ArithmeticOperator("/");
    public static final BinaryOperator MOD = new ArithmeticOperator("%");

    public static final BinaryOperator EQ = new RelationalOperator("==");
    public static final BinaryOperator NE = new RelationalOperator("!=");
    public static final BinaryOperator LT = new RelationalOperator("<");
    public static final BinaryOperator LE = new RelationalOperator("<=");
    public static final BinaryOperator GT = new RelationalOperator(">");
    public static final BinaryOperator GE = new RelationalOperator(">=");

    public static final BinaryOperator AND = new LogicalOperator("&&");
    public static final BinaryOperator OR = new LogicalOperator("||");

    protected static abstract class Operator {
        protected final String symbol;

        public Operator(String symbol) {
            this.symbol = symbol;
        }
    }

    public static final class UnaryOperator extends Operator {
        public UnaryOperator(String symbol) {
            super(symbol);
        }

        Type check(Type type, Environment environment, SyntaxNode node) {
            if (!type.isAssignableTo(BuiltIn.INT)) {
                environment.error("Operator `" + symbol + "` cannot be applied to `" + type.getName() + "`.", node);
                return BuiltIn.VOID;
            }

            return BuiltIn.INT;
        }
    }

    public static final class AssignableUnaryOperator extends Operator {
        public AssignableUnaryOperator(String symbol) {
            super(symbol);
        }

        public Type check(Symbol variable, Environment environment, SyntaxNode node) {
            if (variable == BuiltIn.NO_OBJECT) {
                return BuiltIn.VOID;
            }

            if (!variable.getMJKind().isAssignable()) {
                environment.error("Variable expected.", node);
                return BuiltIn.VOID;
            }

            if (variable.getSymbolType() != BuiltIn.INT) {
                environment.error("Operator `" + symbol + "` cannot be applied to `"
                        + variable.getSymbolType().getName() + "`.", node);
                return BuiltIn.VOID;
            }

            return BuiltIn.INT;
        }
    }

    public interface BinaryOperator {
        void check(
                Type left,
                Type right,
                Environment environment,
                SyntaxNode node
        );

        Type returnType(Type left, Type right);
    }

    public static final class ArithmeticOperator extends Operator implements BinaryOperator {
        public ArithmeticOperator(String symbol) {
            super(symbol);
        }

        @Override
        public void check(
                Type left,
                Type right,
                Environment environment,
                SyntaxNode node
        ) {
            if (!left.isAssignableTo(BuiltIn.INT) || !right.isAssignableTo(BuiltIn.INT)) {
                environment.error("Operator `" + symbol + "` cannot be applied to `"
                        + left.getName() + "` and `" + right.getName() + "`.", node);
            }
        }

        @Override
        public Type returnType(Type left, Type right) {
            return BuiltIn.INT;
        }
    }

    public static final class LogicalOperator extends Operator implements BinaryOperator {
        public LogicalOperator(String symbol) {
            super(symbol);
        }

        @Override
        public void check(Type left, Type right, Environment environment, SyntaxNode node) {
            if (left != BuiltIn.BOOLEAN || right != BuiltIn.BOOLEAN) {
                environment.error("Operator `" + symbol + "` cannot be applied to `"
                        + left.getName() + "` and `" + right.getName() + "`.", node);
            }
        }

        @Override
        public Type returnType(Type left, Type right) {
            return BuiltIn.BOOLEAN;
        }
    }

    public static final class RelationalOperator extends Operator implements BinaryOperator {
        public RelationalOperator(String symbol) {
            super(symbol);
        }

        @Override
        public void check(
                Type left,
                Type right,
                Environment environment,
                SyntaxNode node
        ) {
            if (!left.isCompatibleWith(right)) {
                environment.error("Operator `" + symbol + "` cannot be applied to `"
                        + left.getName() + "` and `" + right.getName() + "`.", node);
            }
        }

        @Override
        public Type returnType(Type left, Type right) {
            return BuiltIn.BOOLEAN;
        }
    }
}
