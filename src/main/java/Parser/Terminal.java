package Parser;

/**
 * Терминальный символ соответствует токену определенного типа
 * все терминалы на данный момент жестко запрограммированы на основе перечисления TokenType
 * @author agrmv
 * */

public class Terminal extends Symbol {

    public TokenType type;

    public Terminal(TokenType type) {
        this.symbol = type.toString();
        this.type = type;
    }

    public String toString() {
        return this.symbol;
    }
}
