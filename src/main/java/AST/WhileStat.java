package AST;

import java.util.ArrayList;

/**
 * @author agrmv
 * */


public class WhileStat extends Stat {

    public Expr cond;
    public ArrayList<Stat> stats = new ArrayList<Stat>();
    public boolean finalized = false;

    public String type(){return "WhileStat";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>();
        children.add(cond);
        children.addAll(stats);
        return children;
    }
    public ArrayList<String> attr(){
        return new ArrayList<>();
    }
}
