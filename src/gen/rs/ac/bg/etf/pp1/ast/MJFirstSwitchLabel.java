// generated with ast extension for cup
// version 0.8
// 24/1/2026 16:21:13


package rs.ac.bg.etf.pp1.ast;

public class MJFirstSwitchLabel extends Switch_labels {

    private Switch_label switch_label;

    public MJFirstSwitchLabel (Switch_label switch_label) {
        this.switch_label=switch_label;
        if(switch_label!=null) switch_label.setParent(this);
    }

    public Switch_label getSwitch_label() {
        return switch_label;
    }

    public void setSwitch_label(Switch_label switch_label) {
        this.switch_label=switch_label;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(switch_label!=null) switch_label.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(switch_label!=null) switch_label.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(switch_label!=null) switch_label.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJFirstSwitchLabel(\n");

        if(switch_label!=null)
            buffer.append(switch_label.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJFirstSwitchLabel]");
        return buffer.toString();
    }
}
