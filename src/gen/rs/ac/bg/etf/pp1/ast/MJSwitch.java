// generated with ast extension for cup
// version 0.8
// 8/2/2026 1:48:25


package rs.ac.bg.etf.pp1.ast;

public class MJSwitch extends Switch_statement {

    private Expression expression;
    private Switch_block switch_block;

    public MJSwitch (Expression expression, Switch_block switch_block) {
        this.expression=expression;
        if(expression!=null) expression.setParent(this);
        this.switch_block=switch_block;
        if(switch_block!=null) switch_block.setParent(this);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression=expression;
    }

    public Switch_block getSwitch_block() {
        return switch_block;
    }

    public void setSwitch_block(Switch_block switch_block) {
        this.switch_block=switch_block;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(expression!=null) expression.accept(visitor);
        if(switch_block!=null) switch_block.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(expression!=null) expression.traverseTopDown(visitor);
        if(switch_block!=null) switch_block.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(expression!=null) expression.traverseBottomUp(visitor);
        if(switch_block!=null) switch_block.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJSwitch(\n");

        if(expression!=null)
            buffer.append(expression.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(switch_block!=null)
            buffer.append(switch_block.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJSwitch]");
        return buffer.toString();
    }
}
