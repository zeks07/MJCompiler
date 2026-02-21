package rs.ac.bg.etf.pp1.symbols;

import java.util.ArrayList;
import java.util.List;

public final class ExpressionValue {
    private final Symbol symbol;
    private final Type type;
    private final List<Type> list = new ArrayList<>();

    public ExpressionValue() {
        this(BuiltIn.NO_OBJECT, BuiltIn.VOID);
    }

    public ExpressionValue(Type type) {
        this(BuiltIn.NO_OBJECT, type);
    }

    public ExpressionValue(Symbol symbol) {
        this(symbol, symbol.getSymbolType());
    }

    public ExpressionValue(Symbol symbol, Type type) {
        this.symbol = symbol;
        this.type = type;
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
}
