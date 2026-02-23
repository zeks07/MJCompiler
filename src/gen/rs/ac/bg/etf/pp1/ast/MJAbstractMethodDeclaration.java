// generated with ast extension for cup
// version 0.8
// 23/1/2026 14:34:26


package rs.ac.bg.etf.pp1.ast;

public class MJAbstractMethodDeclaration extends Member_declaration {

    private Method_declaration method_declaration;

    public MJAbstractMethodDeclaration (Method_declaration method_declaration) {
        this.method_declaration=method_declaration;
        if(method_declaration!=null) method_declaration.setParent(this);
    }

    public Method_declaration getMethod_declaration() {
        return method_declaration;
    }

    public void setMethod_declaration(Method_declaration method_declaration) {
        this.method_declaration=method_declaration;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(method_declaration!=null) method_declaration.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(method_declaration!=null) method_declaration.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(method_declaration!=null) method_declaration.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJAbstractMethodDeclaration(\n");

        if(method_declaration!=null)
            buffer.append(method_declaration.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJAbstractMethodDeclaration]");
        return buffer.toString();
    }
}
