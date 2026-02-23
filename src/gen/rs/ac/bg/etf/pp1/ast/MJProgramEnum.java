// generated with ast extension for cup
// version 0.8
// 23/1/2026 14:34:26


package rs.ac.bg.etf.pp1.ast;

public class MJProgramEnum extends Static_declaration {

    private Enum_declaration enum_declaration;

    public MJProgramEnum (Enum_declaration enum_declaration) {
        this.enum_declaration=enum_declaration;
        if(enum_declaration!=null) enum_declaration.setParent(this);
    }

    public Enum_declaration getEnum_declaration() {
        return enum_declaration;
    }

    public void setEnum_declaration(Enum_declaration enum_declaration) {
        this.enum_declaration=enum_declaration;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(enum_declaration!=null) enum_declaration.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(enum_declaration!=null) enum_declaration.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(enum_declaration!=null) enum_declaration.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJProgramEnum(\n");

        if(enum_declaration!=null)
            buffer.append(enum_declaration.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJProgramEnum]");
        return buffer.toString();
    }
}
