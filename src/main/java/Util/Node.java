package Util;


import java.util.ArrayList;

public class Node<T> {

    public T val;

    private ArrayList<Node<T>> adj = new ArrayList<>();
    public ArrayList<Node<T>> getAdj(){
        return adj;
    }

    public Node(T val){
        this.val = val;
    }
    void connect(Node n){
        if (!this.neighbors(n)){
            adj.add(n);
            n.adj.add(this);
        }
    }

    public int degree(){
        return adj.size();
    }
    private boolean neighbors(Node n){
        return adj.contains(n);
    }
    public String toString(){
        return val.toString() + "[Deg=" + getAdj().size() + "]";
    }
}
