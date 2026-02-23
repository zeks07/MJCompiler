// generated with ast extension for cup
// version 0.8
// 23/1/2026 14:34:26


package rs.ac.bg.etf.pp1.ast;

public class MJSwitchStatement extends Statement_without_trailing_substatement {

    private Switch_statement switch_statement;

    public MJSwitchStatement (Switch_statement switch_statement) {
        this.switch_statement=switch_statement;
        if(switch_statement!=null) switch_statement.setParent(this);
    }

    public Switch_statement getSwitch_statement() {
        return switch_statement;
    }

    public void setSwitch_statement(Switch_statement switch_statement) {
        this.switch_statement=switch_statement;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(switch_statement!=null) switch_statement.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(switch_statement!=null) switch_statement.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(switch_statement!=null) switch_statement.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJSwitchStatement(\n");

        if(switch_statement!=null)
            buffer.append(switch_statement.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJSwitchStatement]");
        return buffer.toString();
    }
}
