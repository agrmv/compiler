package AST;

/**
 * @author agrmv
 */

public class ASTToString {

    public static String getTreeString(Node n, Boolean AsSEXP){
        if (AsSEXP){
            return getTreeStringSExp(n);
        }
        else{
            return getTreeStringReadable(n);
        }
    }

    private static String getTreeStringReadable(Node n){
        return getTreeStringReadableHelper(n, 0);
    }

    private static String c_spaces(int c){
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < c; i++){
            out.append(" ");
        }
        return out.toString();
    }

    private static String getTreeStringReadableHelper(Node n, int level){
        if (n == null){
            int x = 1;
        }
        assert n != null;
        StringBuilder out = new StringBuilder(n.type());
        for (String attr : n.attr()){
            out.append(" ").append(attr);
        }
        level += 4;
        for (Node child : n.children()){
            out.append("\n").append(c_spaces(level)).append(getTreeStringReadableHelper(child, level));
        }
        return out.toString();
    }


    private static String getTreeStringSExp(Node n){
        StringBuilder out = new StringBuilder("(" + n.type());
        for (String attr : n.attr()){
            out.append(" ").append(attr);
        }
        for (Node child : n.children()){
            out.append(" ").append(getTreeStringSExp(child));
        }
        return out + ")";
    }


}
