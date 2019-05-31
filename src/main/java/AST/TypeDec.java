package AST;

import SemanticAnalyzer.SemanticSymbol;

import java.util.ArrayList;

/**
 * @author agrmv
 * */

public class TypeDec extends Node {

    public SemanticSymbol newType;

    public String type(){return "TypeDec";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>();
        children.add(newType);
        if (newType.isArray()){
            StupidNode arrSize = new StupidNode("IntLit", "" + newType.getArraySize());
            children.add(arrSize);
        }

        StupidNode ttype = new StupidNode("Type", newType.getPrintedType());
        children.add(ttype);
        return children;
    }
    public ArrayList<String> attr(){
        return new ArrayList<>();
    }
}
