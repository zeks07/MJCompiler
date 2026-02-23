// generated with ast extension for cup
// version 0.8
// 23/1/2026 14:34:26


package rs.ac.bg.etf.pp1.ast;

public class MJFirstSwitchStatementGroups extends Switch_block_statement_groups {

    private Switch_block_statement_group switch_block_statement_group;

    public MJFirstSwitchStatementGroups (Switch_block_statement_group switch_block_statement_group) {
        this.switch_block_statement_group=switch_block_statement_group;
        if(switch_block_statement_group!=null) switch_block_statement_group.setParent(this);
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
        if(switch_block_statement_group!=null) switch_block_statement_group.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(switch_block_statement_group!=null) switch_block_statement_group.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(switch_block_statement_group!=null) switch_block_statement_group.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJFirstSwitchStatementGroups(\n");

        if(switch_block_statement_group!=null)
            buffer.append(switch_block_statement_group.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJFirstSwitchStatementGroups]");
        return buffer.toString();
    }
}
