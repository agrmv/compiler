package RegisterAllocator;

import IR.Register;
import IR.Var;
import IR.load;
import IR.store;

import java.util.ArrayList;

/** Список нагрузок, которые будут вставлены перед хранилищем инструкций, которые будут вставлены после инструкций
 * @author agrmv
 * */
public class LoadStore {
    ArrayList<load> iloads = new ArrayList<>(); // any loads before instruction
    ArrayList<store> istores = new ArrayList<>(); // any stores after instruction

    public String toString(){
        StringBuilder out = new StringBuilder();
        if (!iloads.isEmpty()){
            out.append("[");
            for (load l : iloads){
                out.append(l).append(", ");
            }
            out.append("]");
        }
        if (!istores.isEmpty()){
            out.append("[");
            for (store s : istores){
                out.append(s).append(", ");
            }
            out.append("]");
        }
        return out.toString();
    }

    /** Возвращает размер load после добавления */
    void addLoad(Var src, Register dst){
        iloads.add(new load(dst, src, src.isInt()));
        iloads.size();
    }
    /** возвращает размер stored после добавления*/
    void addStore(Register src, Var dst){
        istores.add(new store(src, dst, dst.isInt()));
        istores.size();
    }
}
