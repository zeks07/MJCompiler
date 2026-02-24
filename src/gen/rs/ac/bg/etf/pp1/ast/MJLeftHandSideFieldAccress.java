// generated with ast extension for cup
// version 0.8
// 24/1/2026 16:21:13


package rs.ac.bg.etf.pp1.ast;

public class MJLeftHandSideFieldAccress extends Left_hand_side {

    private Field_access field_access;

    public MJLeftHandSideFieldAccress (Field_access field_access) {
        this.field_access=field_access;
        if(field_access!=null) field_access.setParent(this);
    }

    public Field_access getField_access() {
        return field_access;
    }

    public void setField_access(Field_access field_access) {
        this.field_access=field_access;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(field_access!=null) field_access.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(field_access!=null) field_access.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(field_access!=null) field_access.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJLeftHandSideFieldAccress(\n");

        if(field_access!=null)
            buffer.append(field_access.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJLeftHandSideFieldAccress]");
        return buffer.toString();
    }
}
