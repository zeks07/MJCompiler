package rs.ac.bg.etf.pp1.semantic;

import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.symbols.BuiltIn;
import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Type;

public final class Operators {
    private Operators() {}

    public static final UnaryOperator NEG = (variable, environment, node) -> {
        if (variable.getSymbolType() != BuiltIn.INT) {
            environment.error("Operator `-` cannot be applied to `" + variable.getName() + "`.", node);
            return BuiltIn.VOID;
        }

        return BuiltIn.INT;
    };
    public static final UnaryOperator INC = new ArithmeticUnaryOperator("++");
    public static final UnaryOperator DEC = new ArithmeticUnaryOperator("--");
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

    public interface UnaryOperator {
        Type check(Symbol variable, Environment environment, SyntaxNode node);
    }

    public static final class ArithmeticUnaryOperator implements UnaryOperator {
        private final String symbol;

        public ArithmeticUnaryOperator(String symbol) {
            this.symbol = symbol;
        }

        @Override
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

    public static final class ArithmeticOperator implements BinaryOperator {
        private final String symbol;

        public ArithmeticOperator(String symbol) {
            this.symbol = symbol;
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

    public static final class LogicalOperator implements BinaryOperator {

        private final String symbol;

        public LogicalOperator(String symbol) {
            this.symbol = symbol;
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

    public static final class RelationalOperator implements BinaryOperator {
        private final String symbol;

        public RelationalOperator(String symbol) {
            this.symbol = symbol;
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
