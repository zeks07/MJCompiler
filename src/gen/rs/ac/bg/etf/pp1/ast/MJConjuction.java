// generated with ast extension for cup
// version 0.8
// 3/2/2026 16:57:9


package rs.ac.bg.etf.pp1.ast;

public class MJConjuction extends Conditional_and_expression {

    private Conditional_and_expression conditional_and_expression;
    private Equaliity_expression equaliity_expression;

    public MJConjuction (Conditional_and_expression conditional_and_expression, Equaliity_expression equaliity_expression) {
        this.conditional_and_expression=conditional_and_expression;
        if(conditional_and_expression!=null) conditional_and_expression.setParent(this);
        this.equaliity_expression=equaliity_expression;
        if(equaliity_expression!=null) equaliity_expression.setParent(this);
    }

    public Conditional_and_expression getConditional_and_expression() {
        return conditional_and_expression;
    }

    public void setConditional_and_expression(Conditional_and_expression conditional_and_expression) {
        this.conditional_and_expression=conditional_and_expression;
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
        if(conditional_and_expression!=null) conditional_and_expression.accept(visitor);
        if(equaliity_expression!=null) equaliity_expression.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(conditional_and_expression!=null) conditional_and_expression.traverseTopDown(visitor);
        if(equaliity_expression!=null) equaliity_expression.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(conditional_and_expression!=null) conditional_and_expression.traverseBottomUp(visitor);
        if(equaliity_expression!=null) equaliity_expression.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJConjuction(\n");

        if(conditional_and_expression!=null)
            buffer.append(conditional_and_expression.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(equaliity_expression!=null)
            buffer.append(equaliity_expression.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJConjuction]");
        return buffer.toString();
    }
}
