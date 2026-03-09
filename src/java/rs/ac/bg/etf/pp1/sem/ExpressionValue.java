package rs.ac.bg.etf.pp1.sem;

import rs.ac.bg.etf.pp1.symbol.Symbol;
import rs.ac.bg.etf.pp1.symbol.Type;

public final class ExpressionValue {
    public final Type type;
    public final Symbol symbol; // optional; for names and fields.
    public final boolean isConstant;
    public final int value;

    public ExpressionValue(Type type) {
        this.type = type;
        this.symbol = null;
        this.isConstant = false;
        this.value = 0;
    }

    public ExpressionValue(Type type, int value) {
        this.type = type;
        this.symbol = null;
        this.isConstant = true;
        this.value = value;
    }

    public ExpressionValue(Symbol symbol) {
        this.type = symbol.getMJType();
        this.symbol = symbol;
        this.isConstant = false;
        this.value = 0;
    }
}
