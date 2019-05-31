package RegisterAllocator;

import IR.*;
import Util.*;
import java.util.ArrayList;
import java.util.HashSet;

//statement - оператор

/**
 * График потока управления
 * Используется для анализа живучести
 * Реализуется в виде графа, где узлы имеют подтип BasicBlock
 *
 * 1. Первым оператор в Программе является лидер
 * 2. Любое оператор, которое является целью ветви, является лидером.
 * 3. Любое оператор, следующее непосредственно за оператором ветвления или возврата, является лидером.
 *
 * @author agrmv
 * */

public class FlowGraph extends DiGraph<BasicBlock> {

    public ArrayList<IR> instructions;
    private BasicBlock entryBlock;
    private BasicBlock exitBlock;

    void calcGlobalLiveness() {

        for (BasicBlock block : getNodes()) {
            block.initLiveness();
        }

        for (BasicBlock block : getNodes()) {
            block.buildLivenessGlobal();
        }
    }

    ArrayList<GlobalLiveRange> getGlobalLiveRanges() {
        HashSet<GlobalLiveRange> set = new HashSet<>();
        for (BasicBlock block : getNodes()) {
            set.addAll(block.internalLiveRanges);
            set.addAll(block.outputRanges.values());
        }
        return new ArrayList<>(set);
    }


    FlowGraph(ArrayList<IR> instructions) {

        /** Связывает инструкции FlowGraph с переданным потоком команд */
        this.instructions = instructions;

        /** Лидеры */
        ArrayList<Integer> leaders = generateLeaders(instructions);

        /** Затем создаем базовый блок для каждого лидера. */
        String startLabel = ((Label) instructions.get(0)).name;
        entryBlock = new BasicBlock(FunctionLabel.generate("ENTRY_" + startLabel), -1);
        if (instructions.get(0) instanceof FunctionPrologue) {
            entryBlock.functionPrologue = ((FunctionPrologue) instructions.get(0));
        }
        exitBlock = new BasicBlock(FunctionLabel.generate("EXIT_" + startLabel), -1);
        addNode(entryBlock);
        addNode(exitBlock);

        BasicBlock block = entryBlock;

        // Первыя инструкция всегда будет лейблом
        for (int i = 1; i < instructions.size(); i++) {
            // Если лидер найден, создаем новый базовый блок
            // и соединить текущий блок со следующим, если последняя инструкция
            // текущего блока НЕ было безусловным скачком
            if (leaders.contains(i)) {
                BasicBlock next;
                if (instructions.get(i - 1) instanceof Label) {
                    next = new BasicBlock((Label) instructions.get(i - 1), i);
                } else {
                    next = new BasicBlock(i);
                }
                addNode(next);

                // Если последняя инструкция не была безусловным переходом
                // добавить ребро к следующему блоку
                if (block.size() == 0
                        || !(block.lastInstruction() instanceof goTo)
                        || !(block.lastInstruction() instanceof ret)) {
                    addEdge(block, next);
                }
                block = next;
            }
            // Добавлять в основной блок только если не лейбл
            if (instructions.get(i) instanceof instruction) {
                block.addInstruction(instructions.get(i));
            }
        }


        // Теперь добавляем ребра для вызова goTo и ветвлений, и вызовы обрабатываются как обычные binops и сразу
        // переходят к следующей инструкции, а ret отправляется в выходной блок
        for (BasicBlock bb : getNodes()) {
            IR lastInst = bb.lastInstruction();
            if (lastInst != null) {
                if (lastInst instanceof branch) {
                    branch lastInstB = (branch) lastInst;
                    addEdge(bb, getBlockByLabel(lastInstB.labelOp.label));
                } else if (lastInst instanceof goTo) {
                    goTo lastInstG = (goTo) lastInst;
                    addEdge(bb, getBlockByLabel(lastInstG.labelOp.label));
                } else if (lastInst instanceof ret) {
                    addEdge(bb, exitBlock);
                }
            }
        }
    }


    private ArrayList<Integer> generateLeaders(ArrayList<IR> instructions) {
        // First find leaders
        ArrayList<Integer> leaders = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {

            // Инструкция является лидером, если она является целью инструкции перехода
            //Это обозначено инструкцией после метки
            //Наша реализация позволяет больше не позволяет несколько последовательных меток,
            //Но этот код обрабатывает этот случай
            if (instructions.get(i) instanceof Label) {
                int nextInstruction = i + 1;
                if (nextInstruction < instructions.size()) {
                    if (!(instructions.get(nextInstruction) instanceof Label)) {
                        leaders.add(nextInstruction);
                        i = nextInstruction; // не делайте его лидером дважды
                    }
                }
            }
            if ((instructions.get(i) instanceof controlFlowInstruction)
                    && !(instructions.get(i) instanceof call)
                    && !(instructions.get(i) instanceof callr)) {
                int nextInstruction = i + 1;
                if (nextInstruction < instructions.size()) {
                    if (!(instructions.get(nextInstruction) instanceof Label)) {
                        leaders.add(nextInstruction);
                        i = nextInstruction;
                    }
                }
            }
        }

        return leaders;
    }

    private BasicBlock getBlockByLabel(Label l) {
        for (BasicBlock block : getNodes()) {
            if (block.startLabel == l)
                return block;
        }
        // ничего не найдено? exit block выхода помогает в случае, когда ветвь нацелена на удаленную метку
        return exitBlock;
    }

    public Operand def() {
        return null;
    }

    public ArrayList<Operand> use() {
        return null;
    }
}
