package AST;

/**
 * @author agrmv
 */
import java.util.ArrayList;

public class ASTRoot extends Node {

    public ArrayList<TypeDec> typeDecs = new ArrayList<TypeDec>();
    public ArrayList<VarDec> varDecs = new ArrayList<VarDec>();
    public ArrayList<FunDec> funDecs = new ArrayList<FunDec>();
    public ArrayList<Stat> stats = new ArrayList<Stat>();

    public String type(){return "agrmv";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        ArrayList<Node> children = new ArrayList<>();
        children.addAll(typeDecs);
        children.addAll(varDecs);
        children.addAll(funDecs);
        children.addAll(stats);
        return children;
    }
    public ArrayList<String> attr(){
        return new ArrayList<>();
    }
}
