// generated with ast extension for cup
// version 0.8
// 3/2/2026 16:57:9


package rs.ac.bg.etf.pp1.ast;

public class MJSwitchLabelValue extends Switch_label_value {

    private Integer I1;

    public MJSwitchLabelValue (Integer I1) {
        this.I1=I1;
    }

    public Integer getI1() {
        return I1;
    }

    public void setI1(Integer I1) {
        this.I1=I1;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MJSwitchLabelValue(\n");

        buffer.append(" "+tab+I1);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJSwitchLabelValue]");
        return buffer.toString();
    }
}
