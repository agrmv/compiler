package IR;

import java.util.ArrayList;

/**
 *
 * @author agrmv
 */


public class array_store extends regularInstruction {

    /**Аналагично*/
    public Var var;
    public Operand index;
    public Operand right;

    public array_store(NamedVar var, Operand index, Operand right, boolean isInteger){
        this.var = var;
        this.index = index;
        this.right = right;
        this.isInteger = isInteger;
    }

    public Var def(){
        return null;
    }

    public ArrayList<Var> use(){
        ArrayList<Var> uses = new ArrayList<>();
        if (right instanceof Var) uses.add((Var)right);
        if (index instanceof Var) uses.add((Var)index);
        return uses;
    }

    public void replaceDef(Var old, Register n){
    }
    public void replaceUses(Var old, Register n){
        if (index == old) index = n;
        if (right == old) right = n;
    }


    public String toString(){
        return "array_store, " + var + ", " + index + ", " + right;
    }
    public void accept(IRVisitor v) { v.visit(this); }
}
