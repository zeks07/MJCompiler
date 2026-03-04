// generated with ast extension for cup
// version 0.8
// 3/2/2026 16:57:9


package rs.ac.bg.etf.pp1.ast;

public class MJNextEnumEntry extends Enum_entries {

    private Enum_entries enum_entries;
    private Enum_entry enum_entry;

    public MJNextEnumEntry (Enum_entries enum_entries, Enum_entry enum_entry) {
        this.enum_entries=enum_entries;
        if(enum_entries!=null) enum_entries.setParent(this);
        this.enum_entry=enum_entry;
        if(enum_entry!=null) enum_entry.setParent(this);
    }

    public Enum_entries getEnum_entries() {
        return enum_entries;
    }

    public void setEnum_entries(Enum_entries enum_entries) {
        this.enum_entries=enum_entries;
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
        if(enum_entries!=null) enum_entries.accept(visitor);
        if(enum_entry!=null) enum_entry.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(enum_entries!=null) enum_entries.traverseTopDown(visitor);
        if(enum_entry!=null) enum_entry.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(enum_entries!=null) enum_entries.traverseBottomUp(visitor);
        if(enum_entry!=null) enum_entry.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJNextEnumEntry(\n");

        if(enum_entries!=null)
            buffer.append(enum_entries.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(enum_entry!=null)
            buffer.append(enum_entry.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJNextEnumEntry]");
        return buffer.toString();
    }
}
