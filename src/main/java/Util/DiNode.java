package Util;

import java.util.ArrayList;

public abstract class DiNode {

    private ArrayList<DiNode> succ = new ArrayList<>();
    protected ArrayList<DiNode> pred = new ArrayList<>();

    void addSucc(DiNode n){
        succ.add(n);
        n.pred.add(this);
    }

    public String toString(){
        return "(DINODE)";
    }
}