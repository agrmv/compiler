package AST;

/**
 * @author agrmv
 */

public class Greater extends ComparisonBinOp{
    public String type(){return "Greater";}
    public void accept(Visitor v) { v.visit(this); }
}
