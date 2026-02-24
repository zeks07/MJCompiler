// generated with ast extension for cup
// version 0.8
// 24/1/2026 16:21:13


package rs.ac.bg.etf.pp1.ast;

public class MJSwitchStatements extends Switch_block {

    private Switch_block_statement_groups switch_block_statement_groups;

    public MJSwitchStatements (Switch_block_statement_groups switch_block_statement_groups) {
        this.switch_block_statement_groups=switch_block_statement_groups;
        if(switch_block_statement_groups!=null) switch_block_statement_groups.setParent(this);
    }

    public Switch_block_statement_groups getSwitch_block_statement_groups() {
        return switch_block_statement_groups;
    }

    public void setSwitch_block_statement_groups(Switch_block_statement_groups switch_block_statement_groups) {
        this.switch_block_statement_groups=switch_block_statement_groups;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(switch_block_statement_groups!=null) switch_block_statement_groups.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(switch_block_statement_groups!=null) switch_block_statement_groups.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(switch_block_statement_groups!=null) switch_block_statement_groups.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJSwitchStatements(\n");

        if(switch_block_statement_groups!=null)
            buffer.append(switch_block_statement_groups.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJSwitchStatements]");
        return buffer.toString();
    }
}
