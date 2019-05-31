package AST;

import SemanticAnalyzer.SemanticSymbol;

import java.util.ArrayList;

/**
 * @author agrmv
 * */

public class VarDec extends Node {

    public ArrayList<SemanticSymbol> vars = new ArrayList<>();
    public SemanticSymbol type;
    public Const init;

    public String type(){return "VarDec";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>(vars);
        children.add(type);
        if (init != null)
            children.add(init);
        return children;
    }
    public ArrayList<String> attr(){
        return new ArrayList<>();
    }
}
