package IR;

/**
 *
 * @author agrmv
 */


public class breq extends branch{

    public breq(Operand left, Operand right, LabelOp labelOp, boolean isInteger){
        super(left, right, labelOp, isInteger);
    }
    public String op(){
        return  "breq";
    }
    public void accept(IRVisitor v) { v.visit(this); }
}
