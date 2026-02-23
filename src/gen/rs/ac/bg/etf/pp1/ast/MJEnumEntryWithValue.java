// generated with ast extension for cup
// version 0.8
// 23/1/2026 14:34:26


package rs.ac.bg.etf.pp1.ast;

public class MJEnumEntryWithValue extends Enum_entry {

    private String I1;
    private Integer I2;

    public MJEnumEntryWithValue (String I1, Integer I2) {
        this.I1=I1;
        this.I2=I2;
    }

    public String getI1() {
        return I1;
    }

    public void setI1(String I1) {
        this.I1=I1;
    }

    public Integer getI2() {
        return I2;
    }

    public void setI2(Integer I2) {
        this.I2=I2;
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
        buffer.append("MJEnumEntryWithValue(\n");

        buffer.append(" "+tab+I1);
        buffer.append("\n");

        buffer.append(" "+tab+I2);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MJEnumEntryWithValue]");
        return buffer.toString();
    }
}
