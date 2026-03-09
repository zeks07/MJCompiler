// generated with ast extension for cup
// version 0.8
// 8/2/2026 1:48:25


package rs.ac.bg.etf.pp1.ast;

public class MJContinue extends Continue_statement {

    public MJContinue () {
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
        buffer.append("MJContinue(\n");

        buffer.append(tab);
        buffer.append(") [MJContinue]");
        return buffer.toString();
    }
}
