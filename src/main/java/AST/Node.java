package AST;

import java.util.ArrayList;

/**
 * Родительв всего
 * @author agrmv
 * */

public abstract class Node {
    public int lineNumber = 1; // line number associated with node

    public Node(){

    }
    public abstract ArrayList<Node> children();
    public abstract ArrayList<String> attr();
    public abstract String type();
    public abstract void accept(Visitor v);
}
