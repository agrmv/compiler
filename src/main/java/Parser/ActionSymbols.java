package Parser;

import Config.Config;
import Parser.ActionSymbol;
import Parser.ActionSymbolType;

import java.util.*;

/**
 * @author agrmv
 * */

public class ActionSymbols {


    /**
     * Оба отслеживают одни и те же символы действий
     * Но допускают быструю индексацию либо по типу токена, либо по символу
     **/
    private HashMap<ActionSymbolType, ActionSymbol> ASByType;
    private HashMap<String, ActionSymbol> ASByString;

    ActionSymbols(){
        ASByType = new HashMap<ActionSymbolType, ActionSymbol>();
        ASByString = new HashMap<String, ActionSymbol>();

        for (ActionSymbolType type : ActionSymbolType.values()) {
            add(type);
        }
    }

    public void add(ActionSymbolType type){

        if (!this.containsByType(type)){
            ActionSymbol t = new ActionSymbol(type);
            ASByType.put(type, t);
            ASByString.put(type.toString(), t);
        }
    }


    private Boolean containsByType(ActionSymbolType type) {
        return ASByType.containsKey(type);
    }

    Boolean containsBySymbol(String symbol) {
        return ASByString.containsKey(symbol);
    }

    public int size(){
        return ASByType.size();
    }

    ActionSymbol getBySymbol(String symbol){
        return ASByString.get(symbol);
    }

    public ActionSymbol getByType(ActionSymbolType type){
        return ASByType.get(type);
    }
}



