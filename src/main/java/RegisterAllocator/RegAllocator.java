package RegisterAllocator;

import IR.*;

import java.util.*;

import Config.*;

/**
 * @author agrmv
 * */

public class RegAllocator {

    private RegAllocator(){}

    // Дан поток инструкций IR с именами символов
    //возвращает поток с именами регистров MIPS
    //и загружает и хранит
    public static ArrayList<IR> allocate(ArrayList<IR> instructions){
        if (Config.REG_ALLOCATOR == Config.RegAllocator.NAIVE){
            return naiveAllocator(instructions);
        }
        else if (Config.REG_ALLOCATOR == Config.RegAllocator.INTRABLOCK){
            return intraBlockAllocator(instructions);
        }
        else { // GLOBAL
            return globalAllocator(instructions);
        }
    }

    // Нет анализа времкни жизни
    private static ArrayList<IR> naiveAllocator(ArrayList<IR> instructions){
        NaiveAllocatorVisitor allocator = new NaiveAllocatorVisitor();
        for (IR i : instructions) {
            i.accept(allocator);
        }
        return allocator.instructions;
    }

    //Анализ времкни жизни на уровне BasicBlock
    private static ArrayList<IR> intraBlockAllocator(ArrayList<IR> instructions){
        ArrayList<IR> out = new ArrayList<>();

        ArrayList<FlowGraph> flows = FlowGraphGen.generate(instructions);
        for (FlowGraph flow : flows){
            // заменить символьные регистры фиксированным набором регистров
            //путем расчета диапазонов живучести

            for (BasicBlock block : flow.getNodes()){
                // ничего не делать для фиктивных блоков входа / выхода
                if (block.size() > 0){

                    block.calcLiveness();
                    LiveRanges ranges = new LiveRanges(block);
                    InterferenceGraph IG = new InterferenceGraph(ranges);
                    Colorer colorer = new Colorer(block, IG);
                    ArrayList<IR> newIR = colorer.color();

                    if (block.startLabel != null)
                        out.add(block.startLabel);
                    out.addAll(newIR);
                }
            }
        }
        return out;
    }

    // Анализ жизнеспособности на уровне процедуры
    // 1. Создание графика потока управления Для каждой процедуры, где узлы графика потока управления являются базовыми блоками
    // 2. Генерируем глобальные живые диапазоны, где живые диапазоны соответствуют абсолютной нумерации строк, а не относительной нумерации строк, а также генерируют список определений для живого диапазона
    // Делаем запись после каждого определения (разрешаем случай, когда одна сеть сливается со следующей
    // 3. Генерируем глобальный граф помех из глобального живого диапазона
    // 4. Цвет на глобальном уровне
    private static ArrayList<IR> globalAllocator(ArrayList<IR> instructions){
        ArrayList<IR> out = new ArrayList<>();

        // Создать контрольный поток
        ArrayList<FlowGraph> flows = FlowGraphGen.generate(instructions);

        for (FlowGraph flow : flows){

            flow.calcGlobalLiveness();
            GlobalLiveRanges ranges = new GlobalLiveRanges(flow);
            GlobalInterferenceGraph IG = new GlobalInterferenceGraph(ranges);
            GlobalColorer colorer = new GlobalColorer(flow.instructions, IG);
            ArrayList<IR> newIR = colorer.color();
            out.addAll(newIR);
        }
        return out;
    }
}
