// generated with ast extension for cup
// version 0.8
// 19/1/2026 13:4:25


package rs.ac.bg.etf.pp1.ast;

public class MJEnumDeclaration extends Enum_declaration {

    private String I1;
    private Enum_body enum_body;

    public MJEnumDeclaration (String I1, Enum_body enum_body) {
        this.I1=I1;
        this.enum_body=enum_body;
        if(enum_body!=null) enum_body.setParent(this);
    }

    public String getI1() {
        return I1;
    }

    public void setI1(String I1) {
        this.I1=I1;
    }

    public Enum_body getEnum_body() {
        return enum_body;
    }

    public void setEnum_body(Enum_body enum_body) {
        this.enum_body=enum_body;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(enum_body!=null) enum_body.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(enum_body!=null) enum_body.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(enum_body!=null) enum_body.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJEnumDeclaration(\n");

        buffer.append(" "+tab+I1);
        buffer.append("\n");

        if(enum_body!=null)
            buffer.append(enum_body.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJEnumDeclaration]");
        return buffer.toString();
    }
}
