package RegisterAllocator;

import Util.Graph;
import Util.Node;

import java.util.*;

class InterferenceGraph extends Graph<LiveRange> {

    LiveRanges ranges;


    //граф, на котором каждый узел представляет текущий диапазон для определенной переменной
    InterferenceGraph(LiveRanges ranges) {
        this.ranges = ranges;

        // Добавить узел на графе для каждого живого диапазона
        LinkedList<LiveRange> allRanges = ranges.allRanges();
        for (LiveRange lr : allRanges) {
            addNode(new Node<>(lr));
        }

        // добавить интерференционные края
        for (Node<LiveRange> lr1 : this.getNodes()) {
            for (Node<LiveRange> lr2 : this.getNodes()) {
                if (lr1.val.interferesWith(lr2.val)) {
                    connect(lr1, lr2);
                }
            }
        }
    }

    private InterferenceGraph(InterferenceGraph other){
        ranges = other.ranges;

        //Добавить узел в графе для каждого узла в другом графе
        for (Node<LiveRange> lr : other.getNodes()) {
            addNode(new Node<>(lr.val));
        }
        for (Node<LiveRange> lr1 : this.getNodes()) {
            for (Node<LiveRange> lr2 : this.getNodes()) {
                if (lr1.val.interferesWith(lr2.val)) {
                    connect(lr1, lr2);
                }
            }
        }
    }

    // создаем интерференционный граф, в котором каждый узел
    // копируется глубоко (но LiveRanges внутри узла копируются мелко)
    InterferenceGraph copy(){
        return new InterferenceGraph(this);
    }
}
