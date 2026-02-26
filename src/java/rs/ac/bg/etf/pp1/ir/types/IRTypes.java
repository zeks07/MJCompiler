package rs.ac.bg.etf.pp1.ir.types;

public interface IRTypes {
    IRType BOTTOM = new IRType.SimpleType();
    IRType TOP = new IRType.SimpleType(true);
    IRType CONTROL = new IRType.SimpleType();
}
