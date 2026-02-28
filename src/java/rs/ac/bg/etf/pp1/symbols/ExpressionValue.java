package rs.ac.bg.etf.pp1.symbols;

import java.util.ArrayList;
import java.util.List;

public final class ExpressionValue {
    private final Symbol symbol;
    private final Type type;
    private final List<Type> list = new ArrayList<>();
    private final boolean isConstant;

    public ExpressionValue() {
        this(BuiltIn.NO_OBJECT, BuiltIn.VOID, false);
    }

    public ExpressionValue(Type type) {
        this(BuiltIn.NO_OBJECT, type, false);
    }

    public ExpressionValue(Type type, boolean isConstant) {
        this(BuiltIn.NO_OBJECT, type, isConstant);
    }

    public ExpressionValue(Symbol symbol) {
        this(symbol, symbol.getSymbolType(), symbol.getMJKind().isConstant());
    }

    public ExpressionValue(Symbol symbol, Type type, boolean isConstant) {
        this.symbol = symbol;
        this.type = type;
        this.isConstant = isConstant;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Type getType() {
        return type;
    }

    public List<Type> getList() {
        return list;
    }

    public String formatParameterTypes() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Type type : list) {
            sb.append(type.getName()).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(")");
        return sb.toString();
    }

    public boolean isConstant() {
        return isConstant;
    }
}
