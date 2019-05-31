package Parser;

import Config.Config;
import Parser.NonTerminal;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * NonTerminals - это коллекция, предназначенная для хранения всех NonTerminals таким образом, чтобы:
 *  1. Каждый NonTerminal адресуется уникальным целочисленным идентификатором (например, 0)
 *   - ArrayList
 *  2. Каждый NonTerminal адресуется уникальным символом строки
 *   - HashMap
 * @author agrmv
 * */

public class NonTerminals {

    /**
     * Оба отслеживают одинаковые нетерминалы
     * Но допускаем быструю индексацию по символу или идентификатору
     */
    private ArrayList<NonTerminal> nonTermByID;
    private HashMap<String, NonTerminal> nonTermByString;

    NonTerminals(){
        nonTermByID = new ArrayList<NonTerminal>();
        nonTermByString = new HashMap<String, NonTerminal>();
    }

    public void add(String symbol){

        if (!this.contains(symbol)){
            NonTerminal nt = new NonTerminal(symbol, nonTermByID.size());
            nonTermByID.add(nt);
            nonTermByString.put(symbol, nt);
        }
    }


    public Boolean contains(String symbol) {
         return nonTermByString.containsKey(symbol);
    }

    public int size(){
        return nonTermByID.size();
    }

    NonTerminal getByID(int id){
        return nonTermByID.get(id);
    }

    NonTerminal getBySymbol(String symbol){
        return nonTermByString.get(symbol);
    }
}
