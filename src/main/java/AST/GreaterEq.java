package AST;

/**
 * @author agrmv
 */

public class GreaterEq extends ComparisonBinOp {
    public String type(){return "GreaterEq";}
    public void accept(Visitor v) { v.visit(this); }
}
