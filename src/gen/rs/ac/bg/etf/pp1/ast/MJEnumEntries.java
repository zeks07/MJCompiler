// generated with ast extension for cup
// version 0.8
// 19/1/2026 13:4:25


package rs.ac.bg.etf.pp1.ast;

public class MJEnumEntries extends Enum_entries_opt {

    private Enum_entries enum_entries;

    public MJEnumEntries (Enum_entries enum_entries) {
        this.enum_entries=enum_entries;
        if(enum_entries!=null) enum_entries.setParent(this);
    }

    public Enum_entries getEnum_entries() {
        return enum_entries;
    }

    public void setEnum_entries(Enum_entries enum_entries) {
        this.enum_entries=enum_entries;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(enum_entries!=null) enum_entries.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(enum_entries!=null) enum_entries.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(enum_entries!=null) enum_entries.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJEnumEntries(\n");

        if(enum_entries!=null)
            buffer.append(enum_entries.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJEnumEntries]");
        return buffer.toString();
    }
}
