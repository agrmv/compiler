package Parser;

/**
 * Токен состоит из:
 *  1. Лексема (фактическая строка или имя, прочитанное сканером)
 *  2. Тип токена (синтаксическая категория входного слова)
 *  3. Другие метаданные, такие как номер строки
 * @author agrmv
 * */

public class Token {

    public TokenType type;
    public int line;
    private int number;
    String lexeme;

    Token(TokenType type, int line, int number, String lexeme) {
        this.type = type;
        this.line = line;
        this.number = number;
        this.lexeme = lexeme;
    }

    public String toString(){
        return "(" + type + ", line " + line + ", num " + number + ")";
    }
}
