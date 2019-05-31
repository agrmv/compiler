package Parser;

/**
 * Нетерминал (нетерминальный символ) — объект, обозначающий какую-либо сущность языка (например: формула)
 * @author agrmv
 * */

public class NonTerminal extends Symbol {

    int id; // каждый нетерминал должен быть однозначно идентифицирован по его идентификатору

    public NonTerminal(String symbol, int id){
        this.symbol = symbol;
        this.id = id;
    }

    public String toString(){
        return symbol;
    }
}
