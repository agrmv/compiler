package AST;

/**
 * @author agrmv
 * */

public class Or extends LogicBinOp {
    public String type(){return "Or";}
    public void accept(Visitor v) { v.visit(this); }
}
