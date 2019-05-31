package AST;

/**
 * @author agrmv
 */

public class Add extends ArithmeticBinOp {
    public String type(){return "Plus";}
    public void accept(Visitor v) { v.visit(this); }
}
