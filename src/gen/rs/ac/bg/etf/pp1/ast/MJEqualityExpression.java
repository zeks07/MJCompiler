// generated with ast extension for cup
// version 0.8
// 23/1/2026 14:34:26


package rs.ac.bg.etf.pp1.ast;

public class MJEqualityExpression extends Conditional_and_expression {

    private Equaliity_expression equaliity_expression;

    public MJEqualityExpression (Equaliity_expression equaliity_expression) {
        this.equaliity_expression=equaliity_expression;
        if(equaliity_expression!=null) equaliity_expression.setParent(this);
    }

    public Equaliity_expression getEqualiity_expression() {
        return equaliity_expression;
    }

    public void setEqualiity_expression(Equaliity_expression equaliity_expression) {
        this.equaliity_expression=equaliity_expression;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(equaliity_expression!=null) equaliity_expression.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(equaliity_expression!=null) equaliity_expression.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(equaliity_expression!=null) equaliity_expression.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJEqualityExpression(\n");

        if(equaliity_expression!=null)
            buffer.append(equaliity_expression.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJEqualityExpression]");
        return buffer.toString();
    }
}
