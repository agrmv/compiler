package AST;

/**
 * @author agrmv
 */

public abstract class ComparisonBinOp extends BinOp {
    protected boolean isInteger;
    public boolean isInt(){
        return isInteger;
    }
}
