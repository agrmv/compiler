package AST;
import java.util.ArrayList;

/**
 * @author agrmv
 * */

public class StupidNode extends Node{

    private String typestr;
    StupidNode(String typestr){
        this.typestr = typestr;
    }
    StupidNode(String typestr, String attr){
        this.typestr = typestr;
        this.attr.add(attr);

    }
    public String type(){return typestr;}
    public void accept(Visitor v) { v.visit(this); }

    public ArrayList<Node> children = new ArrayList<>();
    public ArrayList<Node> children(){
        return children;
    }
    ArrayList<String> attr = new ArrayList<>();
    public ArrayList<String> attr(){
        return attr;
    }
}
