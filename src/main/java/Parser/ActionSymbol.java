package Parser;

/**
 * Action symbols are  используются в нашей реализации для:
 1. Генерации таблицу символов
 2. Генерации абстрактное синтаксическое дерево (AST)

 В основном, когда встречается символ действия, парсер манипулирует семантической записью
 построить AST и, если новое объявление, обновить таблицу символов

 @author agrmv
 */

public class ActionSymbol extends Symbol {
    ActionSymbolType type;
    ActionSymbol(ActionSymbolType type){
        this.symbol = type.toString();
        this.type = type;
    }

    public String toString(){
        return this.symbol;
    }

}
