package RegisterAllocator;

import IR.*;
import Util.Node;
import java.util.*;

/**
 * Окрашивание регистров
 *
 * @author agrmv
 * */

class Colorer {

    private BasicBlock block;
    private InterferenceGraph IG;

    private ArrayList<LoadStore> loadStores = new ArrayList<>();

    private LinkedHashSet<Register.Reg> intRegs = new LinkedHashSet<>();
    private LinkedHashSet<Register.Reg> floatRegs = new LinkedHashSet<>();

    private int numIntColors() {
        return intRegs.size();
    }

    private int numFloatColors() {
        return floatRegs.size();
    }


    Colorer(BasicBlock block, InterferenceGraph IG) {
        this.block = block;
        this.IG = IG;
        for (int i = 0; i < block.size(); i++) loadStores.add(new LoadStore());

        intRegs.add(Register.Reg.T0);
        intRegs.add(Register.Reg.T1);
        intRegs.add(Register.Reg.T2);
        intRegs.add(Register.Reg.T3);
        intRegs.add(Register.Reg.T4);
        intRegs.add(Register.Reg.T5);
        intRegs.add(Register.Reg.T6);
        intRegs.add(Register.Reg.T7);

        floatRegs.add(Register.Reg.F0);
        floatRegs.add(Register.Reg.F1);
        floatRegs.add(Register.Reg.F2);
        floatRegs.add(Register.Reg.F3);
        floatRegs.add(Register.Reg.F4);
        floatRegs.add(Register.Reg.F5);
        floatRegs.add(Register.Reg.F6);
        floatRegs.add(Register.Reg.F7);
    }

    ArrayList<IR> color() {

        colorForType(true); // Первый цвет integers
        colorForType(false); // Второй цвет floats

        allocate(); // теперь испоьзуя цвета, переписываем IR с новыми регистрами и загрузками / сохранениями

        ArrayList<IR> out = new ArrayList<>();

        // Генерируем новый IR для каждой инструкции, включая загрузки и хранилища
        for (int i = 0; i < block.instructions().size(); i++) {
            LoadStore ls = loadStores.get(i);

            // Всавляем загрузку перед
            out.addAll(ls.iloads);
            // фактическая инструкция
            out.add(block.getInstruction(i));

            // вставить запись после
            out.addAll(ls.istores);
        }
        return out;
    }

    /**
     * Цвет или целые числа или числа с плавающей запятой на основе пройденного bool
     */
    private void colorForType(boolean colorInteger) {
        // рабочий граф, который мы изменим, оставит исходный нетронутым, за исключением узлов с плавающей точкой
        InterferenceGraph workGraph = IG.copy();
        for (int i = workGraph.size() - 1; i >= 0; i--) {
            // если узел противоположного типа, удалите его
            if (workGraph.get(i).val.var.isInt() != colorInteger) {
                workGraph.remove(i);
            }
        }

        InterferenceGraph workGraphOriginal = workGraph.copy();

        // Привязка к живым диапазонам, которым должны быть назначены цвета
        Stack<LiveRange> colorStack = new Stack<>();

        // цикл по оставшимя узлам
        while (!workGraph.isEmpty()) {

            // Удалить узлы со степенью <N
            // Поместить удаленные узлы в стек
            for (int i = 0; i < workGraph.size(); ) {
                int degree = workGraph.get(i).degree();
                int numColors = (colorInteger ? numIntColors() : numFloatColors());
                if (degree < numColors) {
                    //пушим Live Range в стек
                    colorStack.push(workGraph.get(i).val);
                    //удаляем узел и все его ребра из интерференционного графа
                    workGraph.remove(i);
                    //перезапуск поиска
                    i = 0;
                } else {
                    i++;
                }
            }
            // Когда все узлы имеют степень> = N
            // Найти узел с наименьшей стоимостью разлива и "пролить" его
            if (!workGraph.isEmpty()) {
                int remove = 0;
                for (int i = 1; i < workGraph.size(); i++) {
                    if (workGraph.get(i).val.spillCost() < workGraph.get(remove).val.spillCost()) {
                        remove = i;
                    }
                }
                // Проливаем значение
                spill(workGraph.get(remove).val);
                // и удаляем из графа
                workGraph.remove(remove);
            }
        }

        // Теперь назначаем цвета для регистров
        while (!colorStack.isEmpty()) {
            LiveRange lr = colorStack.pop();

            Node<LiveRange> node = workGraphOriginal.getNode(lr);

            // Перебирайте цвета, пока не найдете доступный
            for (Register.Reg color : colorInteger ? intRegs : floatRegs) {
                boolean colorUsed = false;
                for (Node<LiveRange> neighbor : node.getAdj()) {
                    if (neighbor.val.getColor() == color) {
                        colorUsed = true;
                    }
                }
                if (!colorUsed) {
                    lr.setColor(color);
                    break;
                }
            }
            if (lr.getColor() == null) {
                System.out.println("NO COLOR AVAILABLE. ERROR. GRAPH SHOULD BE COLORABLE AT THIS STAGE");
            }
        }

    }


    private void allocate() {

        // Для каждого живого диапазона
        for (LiveRange liveRange : IG.ranges.allRanges()) {

            // не перераспределяем разлитые переменные, которым уже назначены цвета.
            if (liveRange.spilled) {
                continue;
            }

            Var var = liveRange.var;

            Register reg = new Register(liveRange.getColor());
            int definitionLine = liveRange.definitionLine;

            // не обязательно, чтобы строка определения фактически содержала определение

            // если строка определения имеет определение лол
            if (block.def(definitionLine) == var) {

                // если это определение никогда не используется, присваиваем зарезервированному var и сохраните его
                if (liveRange.getLines().size() == 0) {
                    Register res1 = Register.res1(var.isInt());
                    block.getInstruction(definitionLine).replaceDef(var, res1);
                    loadStores.get(definitionLine).addStore(res1, var);
                }
                // если используется это определение, присвойте ему цвет и сохраните его
                else {
                    block.getInstruction(definitionLine).replaceDef(var, reg);
                    loadStores.get(definitionLine).addStore(reg, var);
                }
            }
            // если строка определения не имеет определения, это означает, что переменная используется без определения,
            // поэтому загрузите ее из mem в следующей строке
            else {
                // переменная используется в строке 0 без определения
                // так что загружаем его в строку определения
                if (definitionLine + 1 >= block.size())
                    System.out.println("ERROR: allocate() tried to add load past end of block");
                else loadStores.get(definitionLine + 1).addLoad(var, reg);
            }


            // Для каждой линии в живом диапазоне
            for (Integer i : liveRange.getLines()) {
                block.getInstruction(i).replaceUses(var, reg);
            }

        }
    }

    /**
     * To spill:
     * Для данного живого диапазона
     * Для каждой строки в живом диапозоне
     * если в инструкции в этой строке используется var
     * 1. вставляем load в зарезервированный var перед строкой
     * 2. вставляем stored из того же зарезервированного var после строки вставить stored после первого определения,
     * если оно было
     */
    private void spill(LiveRange liveRange) {
        liveRange.spilled = true;
        Var var = liveRange.var;
        Register res1 = Register.res1(var.isInt());
        Register res2 = Register.res2(var.isInt());
        int definitionLine = liveRange.definitionLine;

        // добавить загрузку и сохранить для каждой строки использования, если она действительно используется

        // за каждую линию в живом диапазоне
        for (Integer i : liveRange.getLines()) {

            if (block.getInstruction(i).use().contains(var)) {
                if (!(block.getInstruction(i) instanceof callInstruction)) {

                    // вставить load перед использованием, для инструкций, не связанных с вызовом функции

                    if (loadStores.get(i).iloads.size() == 0) {
                        loadStores.get(i).addLoad(var, res1);
                        block.getInstruction(i).replaceUses(liveRange.var, res1);
                        liveRange.setColor(res1.register);
                    } else if (loadStores.get(i).iloads.size() == 1) {
                        loadStores.get(i).addLoad(var, res2);
                        block.getInstruction(i).replaceUses(liveRange.var, res2);
                        liveRange.setColor(res2.register);
                    } else {
                        System.out.println("WTH? BOTH LOADS ARE USED.");
                    }
                } else {
                    System.out.println("RegisterAllocator.Colorer.spill(): NO LOADING OF FUNCTION ARGS SUPPORTED");

                }
            }
        }

        // добавить stored для строки определения
        // если на самом деле есть определение (напомним, что определения может и не быть)
        if (block.def(definitionLine) == var) {
            block.getInstruction(definitionLine).replaceDef(var, res1);
            loadStores.get(definitionLine).addStore(res1, var);
        }


    }
}