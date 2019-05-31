package AST;

import java.util.ArrayList;

/**
 * @author agrmv
 */

public abstract class BinOp extends Expr {

    public Expr left;
    public Expr right;
    public boolean convertLeft;
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>();
        children.add(left);
        children.add(right);
        return children;
    }
    public ArrayList<String> attr(){
        return new ArrayList<>();
    }
}
