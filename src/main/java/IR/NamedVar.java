package IR;

import SemanticAnalyzer.SemanticSymbol;

/**
 * @author agrmv
 */

public class NamedVar extends Var {

    private NamedVar(SemanticSymbol symbol){
        this.isInteger = (symbol.getInferredPrimitive() == SemanticSymbol.SymbolType.SymbolInt);
        this.name = "_" + symbol.getName();
        this.isLocal = symbol.isLocal();
    }

    public static NamedVar generateNamedVar(SemanticSymbol symbol) {
        NamedVar var;
        if (getNames().containsKey(symbol.uniqueString())){
            var = (NamedVar)getNames().get(symbol.uniqueString());
        }
        else{
            var = new NamedVar(symbol);
            getVars().add(var);
            getNames().put(symbol.uniqueString(), var);
        }
        return var;
    }

    public String toString(){
        return name;
    }
    
    @Override
	public String getType() {
		return "var";
    }
}
