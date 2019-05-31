package IR;

/**
 * @author agrmv
 */

public class TempFloatVar extends TempVar {

    private static int num = 0;
    public int id;

    private TempFloatVar(){
        isInteger = false;
        id = num++;
        name = "__f" + id;
    }

    public static TempFloatVar gen(boolean inFunction) {
        TempFloatVar var = new TempFloatVar();
        var.isLocal = inFunction;
        getVars().add(var);
        getNames().put(var.name, var);
        return var;
    }

}
