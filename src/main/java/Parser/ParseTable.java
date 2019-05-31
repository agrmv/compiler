package Parser;

import Config.Config;
import Util.Util;
import java.util.*;


/**
 * Таблица разбора используется для определения следующего правила грамматики для применения
 * Дано: нетерминал и прогнозный символ (тип токена),
 * таблица разбора возвращает индекс следующего правила грамматики для применения
 * @author agrmv
 * */

/**
 *  A Parse table это массив Hashmaps, такой что
 *  1. массив индексируется по номеру NonTerminal
 *  2.Каждый Hashmap содержит сопоставления TokenType to Production для проиндексированного NonTerminal
 *  Конечный результат - это быстрый способ найти правильное правило расширения
 *  1. NonTerminal ID в верхней части стека разбора
 *  2. TokenType токена, возвращаемого сканером
 */

class ParseTable {
    private ArrayList<HashMap<TokenType, Integer>> table;
    private Grammar grammar; // needed for constructing ParseTable

    ParseTable(Grammar grammar) {
        table = new ArrayList<HashMap<TokenType, Integer>>();
        this.grammar = grammar;
        init();
    }

    private void init() {
        // открыть файл ParseTable CSV
        String[] lines = Util.readLines(Config.PARSE_TABLE_PATH);
        if (lines.length == 0) {
            System.out.println("ParseTable Invalid (it's empty). ABORT.");
            System.exit(1);
        }

        // Теперь созраняем индекс, по которому каждый TokenType появляется в файле ParseTable.csv.
        String[] tokenLineEntries = lines[0].split(",");
        // Первая запись будет ненужной и не должна быть написана или прочитана
        TokenType[] tokenTypeAtIndex = new TokenType[tokenLineEntries.length]; // first entry blank in csv file
        for (int i = 1; i < tokenLineEntries.length; i++) {
            tokenTypeAtIndex[i] = grammar.terminals.getBySymbol(tokenLineEntries[i]).type;
        }

        //Создаем ParseTable, добавив сопоставления TokenType-ProductionID для каждого нетерминала
        for (int lineNum = 1; lineNum < lines.length; lineNum++) {

            HashMap<TokenType, Integer> newMappings = new HashMap<TokenType, Integer>();
            String[] lineEntries = lines[lineNum].split(",");

            // Теперь добавим новое сопоставление для каждого токена, если запись разбираемой строки не пуста,
            // первая запись - нетерминальная строка, поэтому начните индекс цикла с 1
            for (int i = 1; i < lineEntries.length; i++) {
                if (!lineEntries[i].trim().isEmpty()) {
                    // Вычитаем 1, потому что правила синтаксического анализа таблицы начинаются с 1, а правила грамматики индексируются с 0
                    newMappings.put(tokenTypeAtIndex[i], Integer.parseInt(lineEntries[i].trim()) - 1);
                }
            }
            table.add(newMappings);
        }
    }

    TokenType getAnExpected(int nonTerminalID) {
        return table.get(nonTerminalID).keySet().iterator().next();
    }

    int getRuleID(int nonTerminalID, TokenType tokenType) {
        return table.get(nonTerminalID).get(tokenType);
    }

    boolean containsRuleID(int nonTerminalID, TokenType tokenType) {
        return (nonTerminalID < table.size()) && table.get(nonTerminalID).containsKey(tokenType);
    }
}
