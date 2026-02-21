// generated with ast extension for cup
// version 0.8
// 19/1/2026 13:4:25


package rs.ac.bg.etf.pp1.ast;

public class MJSwitchLabel extends Switch_label {

    private Switch_label_value switch_label_value;

    public MJSwitchLabel (Switch_label_value switch_label_value) {
        this.switch_label_value=switch_label_value;
        if(switch_label_value!=null) switch_label_value.setParent(this);
    }

    public Switch_label_value getSwitch_label_value() {
        return switch_label_value;
    }

    public void setSwitch_label_value(Switch_label_value switch_label_value) {
        this.switch_label_value=switch_label_value;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(switch_label_value!=null) switch_label_value.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(switch_label_value!=null) switch_label_value.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(switch_label_value!=null) switch_label_value.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJSwitchLabel(\n");

        if(switch_label_value!=null)
            buffer.append(switch_label_value.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJSwitchLabel]");
        return buffer.toString();
    }
}
