package AST;

import SemanticAnalyzer.SemanticSymbol;

import java.util.ArrayList;

/**
 * @author agrmv
 * */

public class VarReference extends Expr {
    // Reference to variable symbol that this variable represents
    public SemanticSymbol reference;

    // Expression for index into if one exists
    public Expr index;

    public String type(){return "Variable";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>();
        if (index != null)
            children.add(index);
        return children;
    }
    public ArrayList<String> attr(){
        ArrayList<String> attr = new ArrayList<>();
        attr.add(reference.getName());
        return attr;
    }
}
