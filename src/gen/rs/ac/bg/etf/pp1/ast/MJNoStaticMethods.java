// generated with ast extension for cup
// version 0.8
// 8/2/2026 1:48:25


package rs.ac.bg.etf.pp1.ast;

public class MJNoStaticMethods extends Static_method_declarations_opt {

    public MJNoStaticMethods () {
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
        buffer.append("MJNoStaticMethods(\n");

        buffer.append(tab);
        buffer.append(") [MJNoStaticMethods]");
        return buffer.toString();
    }
}
