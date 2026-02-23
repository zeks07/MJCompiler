// generated with ast extension for cup
// version 0.8
// 22/1/2026 20:0:33


package rs.ac.bg.etf.pp1.ast;

public class MJSwitchStatementsGroup extends Switch_block_statement_group {

    private Switch_labels switch_labels;
    private Block_statements block_statements;

    public MJSwitchStatementsGroup (Switch_labels switch_labels, Block_statements block_statements) {
        this.switch_labels=switch_labels;
        if(switch_labels!=null) switch_labels.setParent(this);
        this.block_statements=block_statements;
        if(block_statements!=null) block_statements.setParent(this);
    }

    public Switch_labels getSwitch_labels() {
        return switch_labels;
    }

    public void setSwitch_labels(Switch_labels switch_labels) {
        this.switch_labels=switch_labels;
    }

    public Block_statements getBlock_statements() {
        return block_statements;
    }

    public void setBlock_statements(Block_statements block_statements) {
        this.block_statements=block_statements;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(switch_labels!=null) switch_labels.accept(visitor);
        if(block_statements!=null) block_statements.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(switch_labels!=null) switch_labels.traverseTopDown(visitor);
        if(block_statements!=null) block_statements.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(switch_labels!=null) switch_labels.traverseBottomUp(visitor);
        if(block_statements!=null) block_statements.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJSwitchStatementsGroup(\n");

        if(switch_labels!=null)
            buffer.append(switch_labels.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(block_statements!=null)
            buffer.append(block_statements.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJSwitchStatementsGroup]");
        return buffer.toString();
    }
}
