// generated with ast extension for cup
// version 0.8
// 24/1/2026 16:21:13


package rs.ac.bg.etf.pp1.ast;

public class MJNextSwitchStatementsGroups extends Switch_block_statement_groups {

    private Switch_block_statement_groups switch_block_statement_groups;
    private Switch_block_statement_group switch_block_statement_group;

    public MJNextSwitchStatementsGroups (Switch_block_statement_groups switch_block_statement_groups, Switch_block_statement_group switch_block_statement_group) {
        this.switch_block_statement_groups=switch_block_statement_groups;
        if(switch_block_statement_groups!=null) switch_block_statement_groups.setParent(this);
        this.switch_block_statement_group=switch_block_statement_group;
        if(switch_block_statement_group!=null) switch_block_statement_group.setParent(this);
    }

    public Switch_block_statement_groups getSwitch_block_statement_groups() {
        return switch_block_statement_groups;
    }

    public void setSwitch_block_statement_groups(Switch_block_statement_groups switch_block_statement_groups) {
        this.switch_block_statement_groups=switch_block_statement_groups;
    }

    public Switch_block_statement_group getSwitch_block_statement_group() {
        return switch_block_statement_group;
    }

    public void setSwitch_block_statement_group(Switch_block_statement_group switch_block_statement_group) {
        this.switch_block_statement_group=switch_block_statement_group;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(switch_block_statement_groups!=null) switch_block_statement_groups.accept(visitor);
        if(switch_block_statement_group!=null) switch_block_statement_group.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(switch_block_statement_groups!=null) switch_block_statement_groups.traverseTopDown(visitor);
        if(switch_block_statement_group!=null) switch_block_statement_group.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(switch_block_statement_groups!=null) switch_block_statement_groups.traverseBottomUp(visitor);
        if(switch_block_statement_group!=null) switch_block_statement_group.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJNextSwitchStatementsGroups(\n");

        if(switch_block_statement_groups!=null)
            buffer.append(switch_block_statement_groups.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(switch_block_statement_group!=null)
            buffer.append(switch_block_statement_group.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJNextSwitchStatementsGroups]");
        return buffer.toString();
    }
}
