package IR;

/**
 * @author agrmv
 */

public class SharedLabel extends Label {


    private static int labelNum = 0;
    public int id;

    public SharedLabel(String name){
        id = labelNum++;
        this.name = name + "_" + id;
    }
    public void accept(IRVisitor v) { v.visit(this); }
}
