package IR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class Var extends Operand {
    public String name;
    public boolean isLocal = false;
    private static LinkedHashSet<Var> vars = new LinkedHashSet<>();
    private static HashMap<String, Var> names = new HashMap<>();
    static LinkedHashSet<Var> getVars(){
        return vars;
    }
    static HashMap<String, Var> getNames(){
        return names;
    }
}
