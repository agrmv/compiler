package Util;

import java.util.ArrayList;

public abstract class DiGraph<T extends DiNode> {

    private ArrayList<T> nodes = new ArrayList<>();

    public ArrayList<T> getNodes(){
        return nodes;
    }

    protected void addNode(T n){
        nodes.add(n);
    }

    protected void addEdge(T from, T to){
        if (nodes.contains(from) && nodes.contains(to))
            from.addSucc(to);
        else{
            System.out.println("ERROR: DIGRAPH ADD EDGE");
            System.exit(1);
        }
    }
}