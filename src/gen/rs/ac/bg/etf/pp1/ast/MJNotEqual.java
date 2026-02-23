// generated with ast extension for cup
// version 0.8
// 22/1/2026 20:0:33


package rs.ac.bg.etf.pp1.ast;

public class MJNotEqual extends Equaliity_expression {

    private Equaliity_expression equaliity_expression;
    private Relational_expression relational_expression;

    public MJNotEqual (Equaliity_expression equaliity_expression, Relational_expression relational_expression) {
        this.equaliity_expression=equaliity_expression;
        if(equaliity_expression!=null) equaliity_expression.setParent(this);
        this.relational_expression=relational_expression;
        if(relational_expression!=null) relational_expression.setParent(this);
    }

    public Equaliity_expression getEqualiity_expression() {
        return equaliity_expression;
    }

    public void setEqualiity_expression(Equaliity_expression equaliity_expression) {
        this.equaliity_expression=equaliity_expression;
    }

    public Relational_expression getRelational_expression() {
        return relational_expression;
    }

    public void setRelational_expression(Relational_expression relational_expression) {
        this.relational_expression=relational_expression;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(equaliity_expression!=null) equaliity_expression.accept(visitor);
        if(relational_expression!=null) relational_expression.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(equaliity_expression!=null) equaliity_expression.traverseTopDown(visitor);
        if(relational_expression!=null) relational_expression.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(equaliity_expression!=null) equaliity_expression.traverseBottomUp(visitor);
        if(relational_expression!=null) relational_expression.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJNotEqual(\n");

        if(equaliity_expression!=null)
            buffer.append(equaliity_expression.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(relational_expression!=null)
            buffer.append(relational_expression.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJNotEqual]");
        return buffer.toString();
    }
}
