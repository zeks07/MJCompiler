// generated with ast extension for cup
// version 0.8
// 19/1/2026 13:4:25


package rs.ac.bg.etf.pp1.ast;

public class MJEnumBody extends Enum_body {

    private Enum_entries_opt enum_entries_opt;

    public MJEnumBody (Enum_entries_opt enum_entries_opt) {
        this.enum_entries_opt=enum_entries_opt;
        if(enum_entries_opt!=null) enum_entries_opt.setParent(this);
    }

    public Enum_entries_opt getEnum_entries_opt() {
        return enum_entries_opt;
    }

    public void setEnum_entries_opt(Enum_entries_opt enum_entries_opt) {
        this.enum_entries_opt=enum_entries_opt;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(enum_entries_opt!=null) enum_entries_opt.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(enum_entries_opt!=null) enum_entries_opt.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(enum_entries_opt!=null) enum_entries_opt.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJEnumBody(\n");

        if(enum_entries_opt!=null)
            buffer.append(enum_entries_opt.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJEnumBody]");
        return buffer.toString();
    }
}
