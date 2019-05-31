package SemanticAnalyzer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.ArrayList;

/** В  моем языке есть ровно две области:
 * 1. Глобальный охват
 * 2. Область действия функции (каждая из которых вложена в область видимости)
 *
 * Все типы, переменные и функции объявляются глобально перед использованием:
 *
 * let
 *      type declarations
 *     var declarations
 *      function declarations
 * in
 *      statements
 * end
 *
 * Все объявления являются последовательными. Например, вы должны объявить тип, прежде чем использовать
 * его для определения другого типа.
 * Все объявления видны везде после их объявления (вы можете увидеть функцию из другой функции,
 * если вторая функция объявлена после функции
 *
 *
 * • переменные: тип, уровень процедуры, смещение кадра
 * • types: дескриптор типа, размер данных / выравнивание
 * • константы: тип, значение
 * • процедуры: формалы (имена / типы), тип результата, информация о блоке (локально
 *  decls.), размер кадра
 *
 *
 * Правило 1: Использовать идентификатор только в том случае, если он определен в прилагаемой области видимости
 * Правило 2: Не объявять идентичные идентификаторы с одинаковые имена более одного раза в одной и той же области видимости
 */


/**
 * В процессе работы компилятор хранит информацию об объектах программы в специальных таблицах символов.
 * Пример моей таблицы символов
 *
 * @author agrmv
 * */

public class SymbolTable {
    /** Стек таблиц символов для каждой используемой области */
    private Deque<HashMap<String, SemanticSymbol>> scopeStack;

    /** Список таблиц символов с окончательной областью действия */
    private ArrayList<HashMap<String, SemanticSymbol>> finalizedScopes;

    SymbolTable() {
        // Инициализируем стек и область хранилища
        scopeStack = new ArrayDeque<>();
        finalizedScopes = new ArrayList<>();

        // Создаем глобальную таблицу символов и пушим на вершину стека
        HashMap<String, SemanticSymbol> globalTable = new HashMap<>();
        scopeStack.addFirst(globalTable);

        // Создать типы int и float
        SemanticSymbol intSymbol = new SemanticSymbol("int", SemanticSymbol.SymbolClass.TypeDecleration);
        intSymbol.setSymbolType(SemanticSymbol.SymbolType.SymbolInt);
        intSymbol.setArraySize(0);
        SemanticSymbol floatSymbol = new SemanticSymbol("float", SemanticSymbol.SymbolClass.TypeDecleration);
        floatSymbol.setSymbolType(SemanticSymbol.SymbolType.SymbolFloat);
        floatSymbol.setArraySize(0);
        put("int", intSymbol);
        put("float", floatSymbol);

        // Стандартные библиотечные функции
        SemanticSymbol printi = new SemanticSymbol("printi", SemanticSymbol.SymbolClass.FunctionDeclatation);
        printi.setFunctionReturnType(null);
        SemanticSymbol num = new SemanticSymbol("num", SemanticSymbol.SymbolClass.VarDeclaration);
        num.setSymbolType(intSymbol);
        ArrayList<SemanticSymbol> args = new ArrayList<>();
        args.add(num);
        printi.setFunctionParameters(args);
        put("printi", printi);

        SemanticSymbol printf = new SemanticSymbol("printf", SemanticSymbol.SymbolClass.FunctionDeclatation);
        printf.setFunctionReturnType(null);
        num = new SemanticSymbol("num", SemanticSymbol.SymbolClass.VarDeclaration);
        num.setSymbolType(floatSymbol);
        args = new ArrayList<>();
        args.add(num);
        printf.setFunctionParameters(args);
        put("printf", printf);
    }

    /** Входим в новую область */
    void beginScope() {
        HashMap<String, SemanticSymbol> newScope = new HashMap<>();
        scopeStack.addFirst(newScope);
    }

    /** Покидаем область */
    void endScope() {
        if (scopeStack.size() <= 1) {
            System.out.println("Error: Attempting to pop global scope");
            return;
        }

        // Добавить вершину стека в окончательный список таблиц символов
        finalizedScopes.add(scopeStack.removeFirst());
    }

    /**
     * Добавляет символ в текущую область
     * @param name имя символа
     * @param symbol символ
     * */
    public void put(String name, SemanticSymbol symbol) {
        scopeStack.peekFirst().put(name, symbol);
        if (scopeStack.size() > 1) {
            symbol.setIsLocal(true);
        }
    }

    /**
     * Выполняет поиск символа во всех активных областях
     * Возвращает символ, ближайший к текущей области
     *
     * @param name имя символа
     * */
    public SemanticSymbol get(String name) {
        SemanticSymbol symbol = null;
        for (HashMap<String, SemanticSymbol> scope : scopeStack) {
            SemanticSymbol temp = scope.get(name);
            if (temp != null) {
                symbol = temp;
            }
        }
        return symbol;
    }

    /**
     * Переименовывает символ в таблице символов. Полезно, когда временным переменным присваивается имя
     * Внутреннее имя символа заменяется только при обнаружении символа
     * Возвращает true в случае успеха, иначе false
     *
     * @param symbol символ который нужно переименовать
     * @param newName новое имя
     * */
    boolean rename(SemanticSymbol symbol, String newName) {
        // Ищем область, в которой находится символ
        HashMap<String, SemanticSymbol> table = null;
        for (HashMap<String, SemanticSymbol> scope : scopeStack) {
            SemanticSymbol temp = scope.get(symbol.getName());
            if (temp != null && temp == symbol) {
                table = scope;
                break;
            }
        }
        if (table != null) {
            table.remove(symbol.getName());
            table.put(newName, symbol);
            symbol.setName(newName);
            return true;
        }
        return false;
    }

    /** Печатает таблицу символов */
    public String toString() {
        StringBuilder ret = new StringBuilder("Global symbols:\n");
        HashMap<String, SemanticSymbol> global = scopeStack.peekLast();
        for (SemanticSymbol sym : global.values()) {
            ret.append(sym).append("\n");
        }
        return ret.toString();
    }
}