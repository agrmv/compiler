package RegisterAllocator;


import IR.IR;
import IR.Var;


import java.util.*;

/**
 * @author agrmv
 * */

public class LiveRanges {

    private Map<Var, LinkedList<LiveRange>> liveRanges = new HashMap<>();
    private LinkedHashSet<Var> vars = new LinkedHashSet<Var>();

    /**
     * Возвращает переменные, используемые Basic Block
     */
    public LinkedHashSet<Var> getVars() {
        return vars;
    }

    /**
     * Возвращает список текущих диапазонов переменной,
     * каждое определение переменной получает свою собственную запись в списке
     */
    private Map<Var, LinkedList<LiveRange>> getRanges() {
        return liveRanges;
    }

    LinkedList<LiveRange> allRanges() {
        LinkedList<LiveRange> out = new LinkedList<>();
        for (LinkedList<LiveRange> liveRanges : getRanges().values()) {
            out.addAll(liveRanges);
        }
        return out;
    }


    private void addVar(Var v) {
        vars.add(v);
    }

    private void addVars(ArrayList<Var> vars) {
        this.vars.addAll(vars);
    }

    private void startNewLiveRange(Var var, int definitionLine) {
        liveRanges.get(var).add(new LiveRange(var, definitionLine));
    }

    private void addLiveEntry(Var var, int line) {
        liveRanges.get(var).getLast().add(line);
    }

    LiveRanges(BasicBlock block) {

        // initialize vars
        for (IR instruction : block.instructions()) {
            if (instruction.def() != null)
                addVar(instruction.def());
            addVars(instruction.use());
        }

        // теперь вычисляем живые диапазоны
        // сопоставляет Var со списком применений
        // где каждая запись в списке соответствует определению
        // первая запись будет соответствовать неинициализированному живому диапазону
        // когда переменная используется без определения (как в параметрах функции)
        for (Var var : vars) {
            liveRanges.put(var, new LinkedList<>());
        }

        for (int i = 0; i < block.size(); i++) {

            // Если переменная жива в этой инструкции
            // добавить диапазон к своему последнему определению
            for (Var var : block.in(i)) {
                if (liveRanges.get(var).isEmpty())
                    startNewLiveRange(var, i - 1);
                addLiveEntry(var, i);
            }

            // Если переменная определена, добавьте новый текущий диапазон
            Var def = block.getInstruction(i).def();
            if (def != null) {
                startNewLiveRange(def, i);
                if (block.out(i).contains(def)) addLiveEntry(def, i + 1);
            }
        }

        // рассчитать количество использований для каждого живого диапазона
        for (LiveRange lr : allRanges()) {
            for (Integer i : lr.getLines()) {
                for (Var v : block.getInstruction(i).use()) {
                    if (v == lr.var) {
                        lr.incrementUses();
                        break;
                    }
                }

            }
        }


    }
}
