package RegisterAllocator;

import IR.*;

import java.util.ArrayList;

/**
 * @author agrmv
 * */

public class FlowGraphGen {

    //Я генерирую график потока управления для каждой функции. Main - это вызов функции,
    // а вызовы похожи на инструкции binop, которые всегда подключаются к следующей инструкции,
    // ret - билет в один конец.
    public static ArrayList<FlowGraph> generate(ArrayList<IR> instructions){

        // Первый отдельный поток команд для каждой функции
        ArrayList<ArrayList<IR>> functionInstructions = new ArrayList<>();
        ArrayList<IR> current = new ArrayList<>();

        for (IR instruction : instructions){
            if (instruction instanceof FunctionLabel){ // только функции получают уникальные метки
                current = new ArrayList<>();
                functionInstructions.add(current);
            }
            current.add(instruction);
        }

        // Теперь сгенерируйте потоковый граф для каждой функции
        ArrayList<FlowGraph> out = new ArrayList<>();
        for (ArrayList<IR> instructionStream : functionInstructions){
            out.add(new FlowGraph(instructionStream));
        }

        return out;
    }
}
