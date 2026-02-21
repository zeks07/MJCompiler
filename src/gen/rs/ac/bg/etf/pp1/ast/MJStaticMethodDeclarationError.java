// generated with ast extension for cup
// version 0.8
// 19/1/2026 13:4:25


package rs.ac.bg.etf.pp1.ast;

public class MJStaticMethodDeclarationError extends Static_method_declarations_element {

    public MJStaticMethodDeclarationError () {
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
        buffer.append("MJStaticMethodDeclarationError(\n");

        buffer.append(tab);
        buffer.append(") [MJStaticMethodDeclarationError]");
        return buffer.toString();
    }
}
