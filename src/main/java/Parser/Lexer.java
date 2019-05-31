package Parser;

import Config.Config;
import java.util.*;
import java.io.*;

/**
 * @author agrmv
 * */

public class Lexer {

    private byte[] inputBuffer;
    private int bufferPosition;
    private int lineNumber;
    private StringBuilder currentLine;
    private HashMap<String, TokenType> keywordMap;
    private String mlexeme;
    Boolean success;

    /** Конструктор
     * @param file исходный файл для компиляции
     **/
    public Lexer(String file){
        bufferPosition = 0;
        lineNumber = 1;
        currentLine = new StringBuilder();
        mlexeme = "";
        success = true;

        keywordMap = new HashMap<String, TokenType>();
        keywordMap.put("array", TokenType.KARRAY);
        keywordMap.put("break", TokenType.KBREAK);
        keywordMap.put("do", TokenType.KDO);
        keywordMap.put("else", TokenType.KELSE);
        keywordMap.put("for", TokenType.KFOR);
        keywordMap.put("function", TokenType.KFUNC);
        keywordMap.put("if", TokenType.KIF);
        keywordMap.put("in", TokenType.KIN);
        keywordMap.put("int", TokenType.KINT);
        keywordMap.put("float", TokenType.KFLOAT);
        keywordMap.put("let", TokenType.KLET);
        keywordMap.put("of", TokenType.KOF);
        keywordMap.put("then", TokenType.KTHEN);
        keywordMap.put("to", TokenType.KTO);
        keywordMap.put("type", TokenType.KTYPE);
        keywordMap.put("var", TokenType.KVAR);
        keywordMap.put("while", TokenType.KWHILE);
        keywordMap.put("endif", TokenType.KENDIF);
        keywordMap.put("begin", TokenType.KBEGIN);
        keywordMap.put("end", TokenType.KEND);
        keywordMap.put("enddo", TokenType.KENDDO);
        keywordMap.put("return", TokenType.KRETURN);

        try {
            FileInputStream stream = new FileInputStream(file);
            byte[] buffer = new byte[stream.available()];
            while (stream.read(buffer) != -1);
            stream.close();
            inputBuffer = buffer.clone();
        }
        catch (FileNotFoundException ex) {
            System.out.println("File not found");
        }
        catch (IOException ex) {
            System.out.println("IO Exception");
        }
    }

    int getLineNumber() {
        return lineNumber;
    }

    String getLineString() {
        return currentLine.toString();
    }

    String getLexeme() {
        return mlexeme;
    }

    Token nextToken() {
        int state = 1;
        boolean reachedEnd = false;
        boolean goodToken = false;
        int newLine = 0;
        int pos = 0;
        int newLinePos = 0;
        boolean lineFresh = false;
        StringBuilder lexeme = new StringBuilder();
        Deque<Integer> stack = new ArrayDeque<Integer>();
        stack.addFirst(0);
        while (state != 99) {
            if (bufferPosition >= inputBuffer.length) {
                reachedEnd = true;
                break;
            }
            if (state == 1) {
                // Очистить лексему, так как, возможно, предшествовали комментарии или пробелы
                lexeme.delete(0, lexeme.length());
            }
            byte next = inputBuffer[bufferPosition];
            byte category = DFACharacterClasses[next];
            if (category != 23) {
                lineFresh = false;
            }
            lexeme.append((char)next);
            if (!lineFresh) {
                currentLine.append((char)next);
                pos++;
            }
            bufferPosition++;
            if (DFAAccept[state-1] != TokenType.NOACCEPT) {
                stack.clear();
                goodToken = true;

                // Новая строка, если у нас есть новая строка перед принимающим состоянием
                if (newLine > 0) {
                    int toKeep = pos - newLinePos;
                    int delEnd = currentLine.length() - toKeep;
                    currentLine.delete(0, delEnd);
                }
                lineNumber += newLine;
                newLine = 0;
            }
            // Все новые строки будут иметь '\ n'
            if (((char)next) == '\n') {
                newLine++;
                newLinePos = pos;
                lineFresh = true;
            }
            stack.addFirst(state);
            state = DFANext[(25*(state-1)) + category];
        }

        if (goodToken || reachedEnd) {
            while (state != 0 && state != 1 && (state == 99 || DFAAccept[state-1] == TokenType.NOACCEPT)) {
                state = stack.removeFirst();
                lexeme.deleteCharAt(lexeme.length() - 1);
                if (currentLine.length() > 0) {
                    currentLine.deleteCharAt(currentLine.length() - 1);
                }
                bufferPosition--;
            }
        } else {
            // Если у нас была новая строка, но нет принимающего состояния, нам все еще нужно увеличить номер строки
            lineNumber += newLine;
            // Новая строка, если у нас есть новая строка перед принимающим состоянием
            if (newLine > 0) {
                int toKeep = pos - newLinePos;
                int delEnd = currentLine.length() - toKeep;
                currentLine.delete(0, delEnd);
            }

            success = false;
            System.out.println("\nLexer error (line " + lineNumber + "): " + currentLine.toString() + "<---\n" +
                    "                        \"" + lexeme.toString() + "\" does not begin a valid token.");

            // Чекаем следующий токен
            return nextToken();
        }

        if (state != 0 && DFAAccept[state-1] != TokenType.NOACCEPT) {
            mlexeme = lexeme.toString();
            if (DFAAccept[state-1] == TokenType.ID) {
                return new Token(keywordMap.getOrDefault(lexeme.toString(), TokenType.ID), 0, 0, mlexeme);
            } else {
                return new Token(DFAAccept[state-1], 0, 0, mlexeme);
            }
        } else {
            mlexeme = "";
            if (reachedEnd) {
                return new Token(TokenType.ENDOFFILE, 0, 0, mlexeme);
            } else {
                return new Token(TokenType.NOACCEPT, 0, 0, mlexeme);
            }
        }
    }


    // Переходы для следующих  состояний в DFA (Детерминированные конечные автоматы)
    // 99 - состояние ошибки
    private char[] DFANext = {
            //  ,   :   ;   (   )   [   ]   {   }   .   +   -   *   /   =   <   >   &   |   aA  0   1-9 _   WS  BT
            2,  3,  5,  6,  7,  8,  9,  10, 11, 12, 13, 14, 15, 16, 19, 20, 23, 25, 26, 27, 30, 28, 99, 1,  99,// 1
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 2
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 4,  99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 3
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 4
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 5
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 6
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 7
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 8
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 9
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 10
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 11
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 12
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 13
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 14
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 15
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 17, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 16
            17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 18, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,// 17
            17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 1,  17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,// 18
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 19
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 21, 99, 22, 99, 99, 99, 99, 99, 99, 99, 99,// 20
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 21
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 22
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 24, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 23
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 24
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 25
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 26
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 27, 27, 27, 27, 99, 99,// 27
            99, 99, 99, 99, 99, 99, 99, 99, 99, 29, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 28, 28, 99, 99, 99,// 28
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 29, 29, 99, 99, 99,// 29
            99, 99, 99, 99, 99, 99, 99, 99, 99, 29, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,// 30
    };

    private TokenType[] DFAAccept = {
            TokenType.NOACCEPT,
            TokenType.COMMA,
            TokenType.COLON,
            TokenType.ASSIGN,
            TokenType.SEMI,
            TokenType.LPAREN,
            TokenType.RPAREN,
            TokenType.LBRACK,
            TokenType.RBRACK,
            TokenType.LBRACE,
            TokenType.RBRACE,
            TokenType.PERIOD,
            TokenType.PLUS,
            TokenType.MINUS,
            TokenType.MULT,
            TokenType.DIV,
            TokenType.NOACCEPT,
            TokenType.NOACCEPT,
            TokenType.EQ,
            TokenType.LESSER,
            TokenType.LESSEREQ,
            TokenType.NEQ,
            TokenType.GREATER,
            TokenType.GREATEREQ,
            TokenType.AND,
            TokenType.OR,
            TokenType.ID,
            TokenType.INTLIT,
            TokenType.FLOATLIT,
            TokenType.INTLIT,
    };

    // Этот массив отображает символ в его класс символа
    //  Классы символа:
    // 0: ,
    // 1: :
    // 2: ;
    // 3: (
    // 4: )
    // 5: [
    // 6: ]
    // 7: {
    // 8: }
    // 9: .
    // 10: +
    // 11: -
    // 12: *
    // 13: /
    // 14: =
    // 15: <
    // 16: >
    // 17: &
    // 18: |
    // 19: [a-zA-Z]
    // 20: 0
    // 21: [1-9]
    // 22: _
    // 23: Whitespace
    // 24: Invalid char
    private byte[] DFACharacterClasses = {
            23, 24, 24, 24, 24, 24, 24, 24, // 0 - 7
            24, 23, 23, 23, 23, 24, 24, 24, // 8 - 15
            24, 24, 24, 24, 24, 24, 24, 24, // 16 - 23
            24, 24, 24, 24, 24, 24, 24, 24, // 24 - 31
            23, 24, 24, 24, 24, 24, 17, 24, // 32 - 39
            3,  4,  12, 10, 0,  11, 9,  13, // 40 - 47
            20, 21, 21, 21, 21, 21, 21, 21, // 48 - 55
            21, 21, 1,  2,  15, 14, 16, 24, // 56 - 63
            24, 19, 19, 19, 19, 19, 19, 19, // 64 - 71
            19, 19, 19, 19, 19, 19, 19, 19, // 72 - 79
            19, 19, 19, 19, 19, 19, 19, 19, // 80 - 87
            19, 19, 19, 5,  24, 6,  24, 22, // 88 - 95
            24, 19, 19, 19, 19, 19, 19, 19, // 96 - 103
            19, 19, 19, 19, 19, 19, 19, 19, // 104 - 111
            19, 19, 19, 19, 19, 19, 19, 19, // 112 - 119
            19, 19, 19, 7,  18, 8,  24, 24, // 120 - 127
    };
}
