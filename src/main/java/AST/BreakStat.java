package AST;

import java.util.ArrayList;

/**
 * @author agrmv
 */

public class BreakStat extends Stat{

    public String type(){return "BreakStat";}
    public void accept(Visitor v) { v.visit(this); }
    public ArrayList<Node> children(){
        return new ArrayList<>();
    }
    public ArrayList<String> attr(){
        return new ArrayList<>();
    }
}
