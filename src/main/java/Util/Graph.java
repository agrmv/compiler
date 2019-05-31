package Util;


import java.util.ArrayList;

public abstract class Graph<T> {

    private ArrayList<Node<T>> nodes = new ArrayList<>();

    public ArrayList<Node<T>> getNodes(){
        return nodes;
    }

    protected void addNode(Node<T> n){
        nodes.add(n);
    }

    public boolean isEmpty(){
        return size() == 0;
    }

    public int size(){
        return nodes.size();
    }

    public Node<T> get(int i){
        return nodes.get(i);
    }
    public void remove(int i){
        Node<T> n = nodes.get(i);

        ArrayList<Node<T>> neighbors = new ArrayList<>(n.getAdj());
        for (Node<T> neighbor : neighbors) {
            neighbor.getAdj().remove(n);
        }
        nodes.remove(i);
    }

    // returns the Node that contains the referenced Var T
    public Node<T> getNode(T t){
        for (Node<T> node : getNodes()){
            if (node.val == t)
                return node;
        }
        return null;
    }

    protected void connect(Node<T> node1, Node<T> node2){
        if (nodes.contains(node1) && nodes.contains(node2))
                node1.connect(node2);
        else{
            System.out.println("ERROR: GRAPH ADD EDGE");
            System.exit(1);
        }
    }

}
