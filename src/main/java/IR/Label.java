package IR;

import java.util.ArrayList;

/**
 * Обратите внимание, что ярлыки также классифицируются как инструкции
 * @author agrmv
 */

public abstract class Label extends IR {

    public String name;

    public Var def(){
        return null;
    }
    public  ArrayList<Var> use(){
        return new ArrayList<>();
    }
    public  void replaceDef(Var old, Register n){}
    public  void replaceUses(Var old, Register n){}

    public String toString() {
        return name + ":";
    }
}
