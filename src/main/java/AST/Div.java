package AST;

/**
 * @author agrmv
 */

public class Div extends ArithmeticBinOp {

    public String type(){return "Div";}
    public void accept(Visitor v) { v.visit(this); }
}
