// generated with ast extension for cup
// version 0.8
// 24/1/2026 16:21:13


package rs.ac.bg.etf.pp1.ast;

public class MJNoArgumentList extends Argument_list_opt {

    public MJNoArgumentList () {
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
        buffer.append("MJNoArgumentList(\n");

        buffer.append(tab);
        buffer.append(") [MJNoArgumentList]");
        return buffer.toString();
    }
}
