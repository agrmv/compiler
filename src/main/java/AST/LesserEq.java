package AST;

/**
 * @author agrmv
 */

public class LesserEq extends ComparisonBinOp {
    public String type(){return "LessEq";}
    public void accept(Visitor v) { v.visit(this); }
}
