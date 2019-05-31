package AST;

/**
 * @author agrmv
 */

import java.util.ArrayList;

public class ID extends Node{

    public String name;

    public String type(){return "ID";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>();
        return children;
    }
    public ArrayList<String> attr(){
        ArrayList<String> attr = new ArrayList<>();
        attr.add("" + name);
        return attr;
    }
}
