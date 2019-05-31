package Parser;

import Config.Config;

import java.util.*;

/**
 * NonTerminals - это коллекция, предназначенная для хранения всех NonTerminal
 * @author agrmv
 * */

public class Terminals {

    /**
     * Оба отслеживают одинаковые нетерминалы
     * Но допускаем быструю индексацию по символу или идентификатору
     */
    private HashMap<TokenType, Terminal> TermByType;
    private HashMap<String, Terminal> TermByString;

    Terminals() {
        TermByType = new HashMap<TokenType, Terminal>();
        TermByString = new HashMap<String, Terminal>();

        for (TokenType type : TokenType.values()) {
            add(type);
        }
    }


    public void add(TokenType type) {

        if (!this.containsByType(type)) {
            Terminal t = new Terminal(type);
            TermByType.put(type, t);
            TermByString.put(type.toString(), t);
        }
    }


    private Boolean containsByType(TokenType type) {
        return TermByType.containsKey(type);
    }

    Boolean containsBySymbol(String symbol) {
        return TermByString.containsKey(symbol);
    }

    public int size() {
        return TermByType.size();
    }

    Terminal getBySymbol(String symbol) {
        return TermByString.get(symbol);
    }
}

