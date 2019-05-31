package IR;

/**
 * @author agrmv
 */
public class TempIntVar extends TempVar {

    private static int num = 0;
    public int id;

    // default to int
    private TempIntVar(){
        isInteger = true;
        id = num++;
        name = "__t" + id;
    }

    public static TempIntVar gen(boolean inFunction){
        TempIntVar var = new TempIntVar();
        var.isLocal = inFunction;
        getVars().add(var);
        getNames().put(var.name, var);
        return var;
    }

}
