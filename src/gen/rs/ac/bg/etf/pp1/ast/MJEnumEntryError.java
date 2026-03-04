// generated with ast extension for cup
// version 0.8
// 3/2/2026 16:57:9


package rs.ac.bg.etf.pp1.ast;

public class MJEnumEntryError extends Enum_entry {

    public MJEnumEntryError () {
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJEnumEntryError(\n");

        buffer.append(tab);
        buffer.append(") [MJEnumEntryError]");
        return buffer.toString();
    }
}
