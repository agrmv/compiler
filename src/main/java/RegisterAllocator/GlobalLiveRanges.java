package RegisterAllocator;

import IR.Var;
import java.util.*;

/**
 * @author agrmv
 * */

class GlobalLiveRanges {

    // Набор Vars (включая временные), используемый в программе
    private Set<Var> vars = new LinkedHashSet<Var>();

    private void addVar(Var v) {
        vars.add(v);
    }

    // Сопоставляет переменную var с ее глобальными живыми диапазонами (охватывает блоки)
    private Map<Var, LinkedList<GlobalLiveRange>> liveRanges = new HashMap<>();

    private Map<Var, LinkedList<GlobalLiveRange>> getRanges() {
        return liveRanges;
    }

    // Возвращает связанный список всех действующих диапазонов в потоковом графике
    LinkedList<GlobalLiveRange> allRanges() {
        LinkedList<GlobalLiveRange> out = new LinkedList<>();
        for (LinkedList<GlobalLiveRange> liveRanges : getRanges().values()) {
            for (GlobalLiveRange range : liveRanges) {
                out.add(range);
            }
        }
        return out;
    }

    GlobalLiveRanges(FlowGraph flow) {
        ArrayList<GlobalLiveRange> ranges = flow.getGlobalLiveRanges();
        for (GlobalLiveRange range : ranges) {
            Var var = range.var;
            if (!liveRanges.containsKey(var)) {
                addVar(var);
                liveRanges.put(var, new LinkedList<>());
            }
            liveRanges.get(var).add(range);
        }
    }
}
