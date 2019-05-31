package AST;

import SemanticAnalyzer.SemanticSymbol;

import java.util.ArrayList;

/**
 * @author agrmv
 * */

public class ReturnStat extends Stat {

    public SemanticSymbol type;
    public Expr retVal;

    public String type(){return "ReturnStat";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>();
        children.add(retVal);
        return children;
    }
    public ArrayList<String> attr(){
        return new ArrayList<>();
    }
}
