package AST;

import java.util.ArrayList;

/**
 * @author agrmv
 * */

public class ProcedureStat extends Stat {

    public FunCall funCall;

    public String type(){return "ProcedureStat";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>();
        children.add(funCall);
        return children;
    }
    public ArrayList<String> attr(){
        return new ArrayList<>();
    }
}
