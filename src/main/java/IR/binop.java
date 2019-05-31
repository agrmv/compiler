package IR;


/**
 *
 * @author agrmv
 */


import java.util.ArrayList;

public abstract class binop extends regularInstruction {

    public Operand left;
    public Operand right;
    public Operand result;


    public binop(Operand left, Operand right, Operand result, Boolean isInteger){
        this.left = left;
        this.right = right;
        this.result = result;
        this.isInteger = isInteger;
    }

    public Var def(){
        if (result instanceof Var) {
            return (Var) result;
        } else {
            return null;
        }
    }

    public ArrayList<Var> use(){
        ArrayList<Var> uses = new ArrayList<>();
        if (left instanceof Var) uses.add((Var)left);
        if (right instanceof Var) uses.add((Var)right);
        return uses;
    }

    public void replaceDef(Var old, Register n){
        if (result == old) result = n;
        else System.out.println("ERROR binop.replaceDef()");
    }
    public void replaceUses(Var old, Register n){
        if (left == old) left = n;
        if (right == old) right = n;
    }

    public abstract String op();
    public String toString(){
        return  op() + (isInt() ? "" : "f" ) + ", " + left + ", " + right + ", " + result;
    }
}
