package IR;

import SemanticAnalyzer.SemanticSymbol;

/**
 * @author agrmv
 */

public abstract class TempVar extends Var {
    public int id;

    public static TempVar gen(SemanticSymbol.SymbolType type, boolean inFunction){
        if (type == SemanticSymbol.SymbolType.SymbolInt)
            return TempIntVar.gen(inFunction);
        else
            return TempFloatVar.gen(inFunction);
    }

    public static TempVar gen(Operand left, Operand right, boolean inFunction){
        if (left.isInt() && right.isInt())
            return TempIntVar.gen(inFunction);
        else
            return TempFloatVar.gen(inFunction);
    }

    public String toString(){
        return name;
    }
    @Override
	public String getType() {
		// TODO Auto-generated method stub
		return "temp";
    }
    
}
