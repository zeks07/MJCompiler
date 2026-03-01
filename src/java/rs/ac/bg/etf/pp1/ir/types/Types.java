package rs.ac.bg.etf.pp1.ir.types;

public interface Types {
    Type BOTTOM = new Type.SimpleType().intern();
    Type TOP = new Type.SimpleType(true).intern();
    Type CONTROL = new Type.SimpleType().intern();
    Type X_CONTROL = new Type.SimpleType(true).intern();
}
