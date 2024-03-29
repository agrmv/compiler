package IR;

// Class Hierarchy:
// Operand
//    Var
//        NamedVar
//        TempVar
//    Immediate
//        IntImmediate
//        FloatImmediate
//    LabelOp

/**
 * @author agrmv
 */
public abstract class Operand {
    protected boolean isInteger;
    public boolean isInt(){ return isInteger;}
    public abstract String toString();
    public abstract String getType();
}
