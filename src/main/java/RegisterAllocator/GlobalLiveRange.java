package RegisterAllocator;

import IR.Register;
import IR.Var;
import IR.IR;
import java.util.*;

/**
 *  Живой диапазон - это переменная и строки, на которых она используется
 *  @author agrmv
 *  */
public class GlobalLiveRange {

    /**  Переменная диапазона строки */
    public Var var;

    // Переменная жива после первого определения до последнего использования
    // Живые диапазоны мешают, если они никогда не используется после определения

    // Живые строки, как индекс в глобальном потоке команд
    private Set<Integer> liveLines = new LinkedHashSet<>();
    Set<Integer> getLiveLines(){
        return liveLines;
    }
    void addUseLine(Integer line){
        liveLines.add(line);
    }

    // Строки определения, как индекс в глобальном потоке команд
    private Set<Integer> definitionLines = new LinkedHashSet<>();
    Set<Integer> getDefinitionLines(){
        return definitionLines;
    }
    void addDefinitionLines(Integer line){
        definitionLines.add(line);
    }

    // Цвет назначен живому диапазону
    private Register.Reg color;
    public Register.Reg getColor(){
        return color;
    }
    void setColor(Register.Reg color){
        this.color = color;
    }

    // Разлив данных / методы
    public int spillCost(){
        return numUses;
    }
    boolean spilled = false;
    private int numUses = 0;

    // Constructor
    GlobalLiveRange(Var var){
        this.var = var;
    }

    // Возвращает true, если живые диапазоны вмешиваются (перекрываются)
    boolean interferesWith(GlobalLiveRange other){

        // плавающие и целые имеют отдельные диапазоны и не могут смешиваться
        if (this.var.isInt() != other.var.isInt()) return false;

         // Если живые диапазоны перекрываются, они смешиваются
        for (Integer i : getLiveLines()){
            if (other.getLiveLines().contains(i)) return true;
        }

        return false;
    }

    //объединяет диапазон с другим диапазоном
    void union(GlobalLiveRange other) {
        definitionLines.addAll(other.definitionLines);
        liveLines.addAll(other.liveLines);
        if (color == null) {
            color = other.color;
        }
    }

    public String toString(){
        String defLines = "";
        boolean first = true;
        for (Integer line : getDefinitionLines()){
            if (!first)
                defLines += ", ";
            defLines += line.toString();
            first = false;
        }

        // Get Live Lines
        String liveLines = "";
        first = true;
        for (Integer line : getLiveLines()){
            if (!first)
                liveLines += ", ";
            liveLines += line.toString();
            first = false;
        }

        String out = "";
        out += var.toString();
        out += (var.isInt() ? "int" : "float") + ": ";
        out += "[defs=" + defLines + "]";
        out += "[lives=" + liveLines + "]";
        out += "[reg=" + color + "]";

        return out;
    }
}
