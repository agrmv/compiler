package Parser;

import Config.Config;
import Util.Util;


// A grammar is an ORDERED list of productions
// We represent our grammar as an array of productions
// Each Production is composed of a nonterminal and its derivation, which is a list of symbols it expands to
// The first production in the grammar file goes in rules[0], the second goes in rules[1], and so on
// The grammar also contains the Terminals, NonTerminals, and Epsilon
// We want exactly one copy of each type of Terminal, each type of nonterminal, and Epsilon
// If you're allocating new Terminals, NonTerminals, or Epsilon, you're doing something wrong

import java.util.*;

/**
 * @author agrmv
 * */

class Grammar {

    ArrayList<Production> rules;
    NonTerminals nonTerminals;
    Terminals terminals;
    private Epsilon epsilon;
    private ActionSymbols actionSymbols;

    Grammar() {

        nonTerminals = new NonTerminals();
        terminals = new Terminals();            // terminals are hard coded
        epsilon = new Epsilon();                // as is epsilon
        actionSymbols = new ActionSymbols();    // as are action symbols
        rules = new ArrayList<Production>();

        init();
    }

    // Заполняет массив правил входным рабочим файлом
    private void init() {
        // Сначала разбираем файл грамматики, чтобы определить символы терминала
        // и назначаем им целочисленные идентификаторы
        String[] lines = Util.readLines(Config.GRAMMAR_PATH);
        String[][] lines2 = new String[lines.length][];
        int numProductions = lines.length;

        for (int i = 0; i < numProductions; i++) {
            lines2[i] = lines[i].split(" ");
            String curNonTerm = lines2[i][0];
            nonTerminals.add(curNonTerm);
        }

        // Теперь создайте правила грамматики
        for (int i = 0; i < numProductions; i++) {
            //Первое слово соответствует полученному нетерминалу
            NonTerminal nonTerm = nonTerminals.getBySymbol(lines2[i][0]);
            // // Построить деривацию с правой стороны
            ArrayList<Symbol> derivation = new ArrayList<Symbol>();
            for (int j = 2; j < lines2[i].length; j++) {
                String symbol = lines2[i][j];
                if (symbol.equals(Config.EPSILON)) {
                    derivation.add(epsilon);
                } else if (nonTerminals.contains(symbol)) {
                    derivation.add(nonTerminals.getBySymbol(symbol));
                } else if (terminals.containsBySymbol(symbol)) {
                    derivation.add(terminals.getBySymbol(symbol));
                } else if (actionSymbols.containsBySymbol(symbol)) {
                    derivation.add(actionSymbols.getBySymbol(symbol));
                } else {
                    System.out.println("Something went wrong generating productions on symbol " + symbol);
                    System.exit(1);
                }
            }

            rules.add(new Production(nonTerm, derivation));
        }
    }
}
