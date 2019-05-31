package IR;

import java.util.HashMap;

/**
 * Просто невидимый маркер, отмечающий конец функции так, чтобы эпилог можно вставить
 * @author agrmv
 */


public class FunctionLabel extends Label {

    static HashMap<String, FunctionLabel> uniqueLabels = new HashMap<>();

    FunctionLabel() {}
    private FunctionLabel(String name){
        this.name = name;
    }

    public static FunctionLabel generate(String name){
        if (uniqueLabels.containsKey(name)){
            return uniqueLabels.get(name);
        }
        else{
            FunctionLabel newLabel = new FunctionLabel(name);
            uniqueLabels.put(name, newLabel);
            return newLabel;
        }
    }
    public void accept(IRVisitor v) { v.visit(this); }
}
