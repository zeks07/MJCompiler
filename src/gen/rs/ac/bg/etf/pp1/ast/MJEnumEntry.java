// generated with ast extension for cup
// version 0.8
// 8/2/2026 1:48:25


package rs.ac.bg.etf.pp1.ast;

public class MJEnumEntry extends Enum_entries {

    private Enum_entry enum_entry;

    public MJEnumEntry (Enum_entry enum_entry) {
        this.enum_entry=enum_entry;
        if(enum_entry!=null) enum_entry.setParent(this);
    }

    public Enum_entry getEnum_entry() {
        return enum_entry;
    }

    public void setEnum_entry(Enum_entry enum_entry) {
        this.enum_entry=enum_entry;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(enum_entry!=null) enum_entry.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(enum_entry!=null) enum_entry.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(enum_entry!=null) enum_entry.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJEnumEntry(\n");

        if(enum_entry!=null)
            buffer.append(enum_entry.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJEnumEntry]");
        return buffer.toString();
    }
}
