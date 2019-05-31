package RegisterAllocator;

import IR.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import Util.DiNode;

/**
 *  Базовый блок - это поток инструкций, которые всегда выполняются вместе
 *  Базовый блок имеет начальную метку, однако некоторые базовые блоки не имеют связанной метки
 *  например инструкция сразу же после инструкции goto
 *
 * @author agrmv
 * */

class BasicBlock extends DiNode {

    private ArrayList<IR> instructions = new ArrayList<>();

    private ArrayList<HashSet<Var>> live = new ArrayList<>();

    private boolean liveness_initialized = false;

    void initLiveness() {
        if (!liveness_initialized) {
            for (int i = 0; i <= size(); i++) {
                live.add(new HashSet<>());
            }
            liveness_initialized = true;
        }
    }

    private boolean makeChanges() {
        boolean changes = false;
        for (int i = 0; i < size(); i++) {
            HashSet<Var> additions = new HashSet<>(out(i));
            additions.remove(getInstruction(i).def());
            additions.addAll(getInstruction(i).use());
            if (!in(i).containsAll(additions)) {
                changes = true;
                in(i).addAll(additions);
            }
        }
        return changes;
    }

    /** Хранит последнее использование переменной в блоке */
    private HashMap<Var, Integer> lastUse = new HashMap<>();

    /** Хранит последнее определение переменной в блоке */
    private HashMap<Var, Integer> lastDef = new HashMap<>();

    /** Определения переменных, которые наследуются от предков */
    private HashSet<Var> defsIn = new HashSet<>();

    /** Определения переменных, которые используются наследниками */
    private HashSet<Var> defsOut = new HashSet<>();

    private boolean builtLiveness = false;

    FunctionPrologue functionPrologue = null;

    /**
     * Используется для обнаружения циклов в циклах и выхода из них
     * Все блоки в цикле должны иметь переменную, которая будет жить во всех инструкциях к концу
     * */
    private boolean cycleDetect = false;

    /** Текущие диапазоны(range), которые существуют только в блоке */
    ArrayList<GlobalLiveRange> internalLiveRanges = new ArrayList<>();

    /** Текущие диапазоны(range), которые существуют только в блоке */
    private HashMap<Var, GlobalLiveRange> inputRanges = new HashMap<>();

    /** Текущие диапазоны(range), которые выходят из блока */
    HashMap<Var, GlobalLiveRange> outputRanges = new HashMap<>();

    /** Текущий диапазон(range) для переменной */
    private HashMap<Var, GlobalLiveRange> currentRange = new HashMap<>();

    void buildLivenessGlobal() {
        if (builtLiveness) {
            return;
        }
        builtLiveness = true;
        // Пролог функции добавляет определения аргументов
        int argnum = 0;
        if (functionPrologue != null) {
            for (Var arg : functionPrologue.arguments) {
                lastDef.put(arg, 0);
                lastUse.put(arg, 0);
                defsOut.add(arg);
                GlobalLiveRange range = new GlobalLiveRange(arg);
                range.addDefinitionLines(0);
                if (argnum == 0) {
                    range.setColor(Register.Reg.A0);
                } else if (argnum == 1) {
                    range.setColor(Register.Reg.A1);
                } else if (argnum == 2) {
                    range.setColor(Register.Reg.A2);
                } else if (argnum == 3) {
                    range.setColor(Register.Reg.A3);
                }

                outputRanges.put(arg, range);
                currentRange.put(arg, range);
                argnum++;
            }
        }
        //Набор переменных, которые должны быть живыми во всем блоке
        HashSet<Var> loopVars = new HashSet<>();
        for (int i = 0; i < size(); i++) {
            Var def = getInstruction(i).def();
            ArrayList<Var> uses = getInstruction(i).use();
            // Начало нового  диапазона жизни переменной
            if (def != null) {
                lastDef.put(def, i);
                lastUse.put(def, i);
                live.get(i).add(def);
                GlobalLiveRange range = new GlobalLiveRange(def);
                range.addDefinitionLines(i + startIndex);
                internalLiveRanges.add(range);
                currentRange.put(def, range);
            }
            //Если есть использование продлить с последнего использования
            for (Var use : uses) {
                int start = 0;
                GlobalLiveRange range = null;
                if (lastDef.containsKey(use)) {
                    start = lastDef.get(use) + 1;
                    range = currentRange.get(use);
                } else {
                    // Отсутствие предыдущих блоков для поиска означает ошибку
                    if (pred.isEmpty()) {
                        System.out.println("Error: " + use.name + " is used without initialization");
                        System.exit(1);
                    }
                    // Нет определения в этом блоке, поэтому посмотрите в предыдущих блоках
                    cycleDetect = true;
                    range = new GlobalLiveRange(use);
                    for (DiNode p : pred) {
                        if (((BasicBlock) p).useVarBySuccessor(use, range)) {
                            // Весь блок становится живым
                            loopVars.add(use);
                            outputRanges.put(use, range);
                        }
                    }
                    cycleDetect = false;
                    defsIn.add(use);
                    inputRanges.put(use, range);
                    internalLiveRanges.remove(range);
                    currentRange.put(use, range);
                }
                for (int j = start; j <= i; j++) {
                    live.get(i).add(use);
                    range.addUseLine(i + startIndex);
                }
                lastUse.put(def, i);
            }
        }
        for (Var c : loopVars) {
            GlobalLiveRange range = inputRanges.get(c);
            defsIn.add(c);
            defsOut.add(c);
            for (int i = 0; i < size(); i++) {
                live.get(i).add(c);
                range.addUseLine(i + startIndex);
            }
            lastUse.put(c, size() - 1);
        }
    }

    /**
     * Вызывается, когда преемник использует переменную
     * Возвращает true, если есть цикл и переменная должна быть помечена как живая на весь блок
     */
    private boolean useVarBySuccessor(Var var, GlobalLiveRange range) {
        // Уходим, если мы в цикле
        if (cycleDetect) {
            return true;
        }
        // Убеждаемя, что range жизни рассчитан
        buildLivenessGlobal();

        // Найти последнее использование
        if (lastUse.containsKey(var)) {
            // Продлить жизнеспособность до конца блока
            GlobalLiveRange last = currentRange.get(var);
            for (int i = lastUse.get(var) + 1; i < size(); i++) {
                live.get(i).add(var);
                last.addDefinitionLines(i + startIndex);
            }
            internalLiveRanges.remove(last);
            outputRanges.remove(last);
            range.union(last);
            outputRanges.put(var, range);
            currentRange.put(var, range);
        } else {
            if (pred.isEmpty()) {
                System.out.println("Error: " + var.name + " is used without initialization");
                System.exit(1);
            }
            // Нет определения в этом блоке, поэтому посмотрите в предыдущих блоках
            cycleDetect = true;
            for (DiNode p : pred) {
                if (((BasicBlock) p).useVarBySuccessor(var, range)) {
                    // весь блок становится живым
                    for (int i = 0; i < size(); i++) {
                        live.get(i).add(var);
                        range.addUseLine(i + startIndex);
                    }
                    defsIn.add(var);
                    lastUse.put(var, size() - 1);
                    defsOut.add(var);
                    outputRanges.put(var, range);
                    currentRange.put(var, range);
                    inputRanges.put(var, range);
                    return true;
                }
            }
            cycleDetect = false;
            defsIn.add(var);
            inputRanges.put(var, range);

            // весь блок становится живым
            for (int i = 0; i < size(); i++) {
                live.get(i).add(var);
                range.addUseLine(i + startIndex);
            }
        }
        // Последнее использование в конце блока теперь lastUse.put (var, size () - 1);
        defsOut.add(var);
        currentRange.put(var, range);
        outputRanges.put(var, range);
        return false;
    }

    void calcLiveness() {

        initLiveness();

        boolean changes;
        do {
            changes = makeChanges();

        } while (changes);
    }

    /**
     * Получаем живые переменные перед инструкцией
     * @param i инструкция
     * */
    public HashSet<Var> in(int i) {
        return live.get(i);
    }

    /**
     * Получаем живые переменные после инструкции
     * @param i инструкция
     * */
    public HashSet<Var> out(int i) {
        return live.get(i + 1);
    }

    /** Индекс в исходный поток инструкций, где начинается BB */
    private int startIndex;

    Label startLabel;

    BasicBlock(int startIndex) {
        this.startIndex = startIndex;
    }

    BasicBlock(Label startLabel, int startIndex) {
        this.startLabel = startLabel;
        this.startIndex = startIndex;
    }

    void addInstruction(IR instruction) {
        instructions.add(instruction);
    }

    IR getInstruction(int i) {
        return instructions.get(i);
    }

    public ArrayList<IR> instructions() {
        return instructions;
    }

    IR lastInstruction() {
        if (instructions.size() == 0) return null;
        else return instructions.get(instructions.size() - 1);
    }

    public int size() {
        return instructions.size();
    }

    public Var def(int i) {
        if (i < 0) return null;
        return instructions.get(i).def();
    }

    public ArrayList<Var> use(int i) {
        return instructions.get(i).use();
    }

    public String toString() {
        String out = "BB[";
        // Пустой блок входа / выхода
        if (size() == 0) {
            out += "empty";
        }
        //Обычный блок
        else {
            out += "line  " + startIndex + "-" + (startIndex + size() - 1);
        }
        out += "]";
        out += ((startLabel == null) ? "unnamed" : startLabel);
        return out;
    }
}


/*
 * часть машинного кода в начале процедуры (функции), который подготавливает стек и машинные регистры — сохраняет
 * контекст выполнения — для их дальнейшего использования в теле процедуры (функции).
 *
 * */