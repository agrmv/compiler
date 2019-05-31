package IR;

import java.util.ArrayList;

/**
 * @author agrmv
 */

public class movfi extends instruction {
    public FloatImmediate src;
    public Operand dst;

    public movfi(FloatImmediate src, Var dst) {
        this.src = src;
        this.dst = dst;
        this.isInteger = false;
    }

    public Var def() {
        if (dst instanceof Var) return (Var)dst;
        else return null;
    }

    public ArrayList<Var> use() {
        return new ArrayList<>();
    }

    public void replaceDef(Var old, Register n){
        if (dst == old) dst = n;
    }
    public void replaceUses(Var old, Register n){
    }


    public String toString() {
        return "movfi, " + src + ", " + dst;
    }

    public void accept(IRVisitor v) {
        v.visit(this);
    }
}
