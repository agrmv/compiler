package Parser;

import java.util.*;

/**
 * Production - это одно из правил грамматики
 * Например: expr -> expr + expr
 * Production состоит из левой части - нетерминала и деривация: ArrayList символов, где индекс 0 - самый левый символ
 * @author agrmv
 * */

public class Production {


    private NonTerminal nonterminal;
    ArrayList<Symbol> derivation;

    Production(NonTerminal nonterminal, ArrayList<Symbol> derivation) {
        this.nonterminal = nonterminal;
        this.derivation = derivation;
    }

    @Override
    public String toString() {
        StringBuilder derivationStr = new StringBuilder();
        for (Symbol symbol : derivation) {
            derivationStr.append(symbol.symbol).append(" ");
        }
        return nonterminal.symbol + " -> " + derivationStr;
    }

    String toStringID() {
        StringBuilder derivationStr = new StringBuilder();
        for (Symbol symbol : derivation) {
            if (symbol instanceof NonTerminal) {
                derivationStr.append(((NonTerminal) symbol).id).append(" ");
            } else {
                derivationStr.append(symbol.symbol).append(" ");
            }
        }
        return nonterminal.id + " -> " + derivationStr;
    }
}
