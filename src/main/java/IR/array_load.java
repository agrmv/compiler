package IR;

import java.util.ArrayList;

/**
 *
 * @author agrmv
 */

public class array_load extends regularInstruction {

    // значение l всегда именуется
    // не имеет смысла присваивать временную
    public Operand left;
    // правая сторона может быть чем угодно
    public Operand var;
    public Operand index;

    public array_load(Operand left, NamedVar var, Operand index, boolean isInteger){
        this.left = left;
        this.var = var;
        this.index = index;
        this.isInteger = isInteger;
    }

    public Var def(){
        if (left instanceof Var) return (Var)left;
        else return null;
    }

    public ArrayList<Var> use(){
        ArrayList<Var> uses = new ArrayList<>();
        if (index instanceof Var) uses.add((Var)index);
        return uses;
    }

    public void replaceDef(Var old, Register n){
        if (left == old) left = n;
        else System.out.println("ERROR array_load.replaceDef()");
    }
    public void replaceUses(Var old, Register n){
        if (index == old) index = n;
    }


    public String toString(){
        return "array_load, " + left + ", " + var + ", " + index;
    }
    public void accept(IRVisitor v) { v.visit(this); }
}
