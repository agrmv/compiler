package AST;

/**
 * @author agrmv
 */

public class Mult extends ArithmeticBinOp {
    public String type(){return "Mult";}
    public void accept(Visitor v) { v.visit(this); }
}
