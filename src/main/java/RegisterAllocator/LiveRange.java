package RegisterAllocator;

import IR.Register;
import IR.Var;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Live Range - это переменная и строки, на которых она используется
 * @author agrmv
 * */
public class LiveRange {

    int definitionLine;
    public Var var;
    private Set<Integer> lines = new LinkedHashSet<>();
    private static int rangeNum = 0;
    private int rangeID;
    public BasicBlock block;

    private Register.Reg color;
    public Register.Reg getColor(){
        return color;
    }
    void setColor(Register.Reg color){
        this.color = color;
    }

    private int numUses = 0;
    void incrementUses(){
        numUses++;
    }
    public int spillCost(){
        return numUses;
    }

    boolean spilled = false;


    LiveRange(Var var, int definitionLine){
        this.var = var;
        this.rangeID = rangeNum++;
        this.definitionLine = definitionLine;
    }

    Set<Integer> getLines(){
        return lines;
    }
    public void add(Integer line){
        lines.add(line);
    }

    boolean interferesWith(LiveRange other){
        if (this.var == other.var) return false;
        if (this.var.isInt() != other.var.isInt()) return false;
        for (Integer i : lines){
            if (other.lines.contains(i)) return true;
        }
        return false;
    }

    public String toString(){
        StringBuilder l = new StringBuilder();
        boolean first = true;
        for (Integer line : lines){
            if (!first)
                l.append(", ");
            l.append(line.toString());
            first = false;
        }
        return var.toString() + "#" + rangeID + ": "
                + l + " [" + numUses + " uses][" + (var.isInt() ? "int" : "float") + "][Reg=" + color + "]";
    }
}
