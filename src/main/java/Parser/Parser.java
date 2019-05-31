package Parser;

import Config.Config;
import java.util.*;
import AST.*;
import SemanticAnalyzer.SemanticAnalyzer;

/**
 * Для синтаксического анализа требуется ОБА файл грамматики и таблица синтаксического анализа для успешного выполнения
 * Файл grammar.txt используется для составления списка нетерминалов и правил
 * Файл ParseTable.csv необходим для ... построения таблицы разбора
 * @author agrmv
 * */

public class Parser {

    private Lexer scanner;
    private Grammar grammar;
    private ParseTable parseTable;
    private Stack<Symbol> parseStack;
    private Token nextToken;
    private SemanticAnalyzer analyzer;

    /**
     * Используется для отключения анализа в случае ошибки парсера
     */
    private boolean doSemanticAnalysis;
    private ASTRoot ast;

    /**
     * Стек, для которого токены ID или LIT, извлеченные из стека разбора,
     * передаются на обработку семантическим анализатором
     */
    private Deque<Token> tokenStack;

    /**
     * Изнгачально true, устанавливается в false, если синтаксический анализатор обнаруживает ошибку
     */
    private boolean parseSuccess;

    /**
     * Используется для восстановления состояния парсера до последней точки последовательности после
     */
    private Stack<Symbol> errorStack;


    public Parser(Lexer scanner) {
        this.scanner = scanner;

        // Устанавливаем Гграмматику
        grammar = new Grammar();

        // Устанавливаем таблицу символов
        parseTable = new ParseTable(grammar);

        // Устанавливаем стек парсера
        parseStack = new Stack<Symbol>();

        // Устанавливаем семантический анализ
        analyzer = new SemanticAnalyzer();

        // Устанавливаем стэк токенов
        tokenStack = new ArrayDeque<>();

        // По умолчанию делать семантический анализ
        doSemanticAnalysis = true;
    }


    public ASTRoot parse() {

        parseStack.push(grammar.nonTerminals.getByID(0)); // Символ Push Start (всегда с индексом 0)
        saveState();
        nextToken = scanner.nextToken();
        parseSuccess = true; // Предположим, что parseSuccess анализирует до сбоя

        while (nextToken.type != TokenType.ENDOFFILE) {

            if (parseStack.empty()) {
                System.out.println("ERROR: Empty Parse Stack. Prefix is a valid program.. But this isn't");
                parseSuccess = false;
                break;
            }
            // Определите, является ли символ в верхней части стека терминальным, нетерминальным
            // или Эпсилон, и отправим соответственно
            Symbol curSymbol = parseStack.peek();
            if (curSymbol instanceof Terminal) processTerminal((Terminal) curSymbol);
            else if (curSymbol instanceof NonTerminal) processNonTerminal((NonTerminal) curSymbol);
            else if (curSymbol instanceof Epsilon) processEpsilon();
            else if (curSymbol instanceof ActionSymbol) processActionSymbol((ActionSymbol) curSymbol);
            else {
                System.out.println("WTH is the symbol?");
                System.exit(1);
            }
        }

        boolean success = parseSuccess && scanner.success;
        System.out.println("\n" + (success ? "successful" : "unsuccessful") + " parse");
        if (!success) System.exit(1);

        return ast;
    }

    /**
     * Обрабатывает терминал в верхней части стека разбора
     *  1. Pop разбирает стек
     *  2. Запросить следующий токен
     * */
    private void processTerminal(Terminal curTerminal) {
        if (curTerminal.type == nextToken.type) {
            parseStack.pop();
            if (nextToken.type.isSequencePoint()) saveState();
            if (nextToken.type == TokenType.ID ||
                    nextToken.type == TokenType.INTLIT ||
                    nextToken.type == TokenType.FLOATLIT ||
                    nextToken.type == TokenType.KINT ||
                    nextToken.type == TokenType.KFLOAT) {
                tokenStack.addFirst(nextToken);
                analyzer.setCurrentLine(scanner.getLineNumber());
            }
            nextToken = scanner.nextToken();


        } else { // ERROR
            error();
        }
    }

    /**
     * Обрабатывает нетерминал в верхней части мешка разбора
     *  В случае успеха:
     *  1. Извлекаем NonTerminal из стека разбора и помещаем его вывод в стек разбора
     *  */
    private void processNonTerminal(NonTerminal curNonTerminal) {

        if (parseTable.containsRuleID(curNonTerminal.id, nextToken.type)) {
            // извлекаем NonTerminal из стека, затем помещаем символы его вывода в стек в обратном порядке
            int nextRuleID = parseTable.getRuleID(curNonTerminal.id, nextToken.type);
            ArrayList<Symbol> derivation = grammar.rules.get(nextRuleID).derivation;
            parseStack.pop();
            for (int i = derivation.size() - 1; i >= 0; i--) {
                parseStack.push(derivation.get(i));
            }
        } else { // ERROR
            error();
        }
    }

    /**
     * Обрабатывает эпсилон вверху стека разбора
     * Всегда успешно. Всегда:
     * 1. Выдает эпсилон из стека разбора
     * */
    private void processEpsilon() {
        parseStack.pop();
    }

    /**
     * обрабатывает символ действия в верхней части стека разбора
     * 1. извлекаем Action Symbol из верхней части стека разбора
     * */
    private void processActionSymbol(ActionSymbol curActionSymbol) {

        SemanticAction(curActionSymbol);

        parseStack.pop();
    }

    private void error() {
        System.out.println("Parser error (line " + scanner.getLineNumber() +
                "): " + scanner.getLineString() + "<---");
        System.out.print("        " + scanner.getLexeme() + " is not a valid token. ");

        Symbol curSymbol = parseStack.peek();
        if (curSymbol instanceof NonTerminal) {
            System.out.println("Expected \"" + parseTable.getAnExpected(((NonTerminal) curSymbol).id).toLexeme() + "\".");
        } else if (curSymbol instanceof Terminal) {
            System.out.println("Expected \"" + ((Terminal) curSymbol).type.toLexeme() + "\".");
        }

        parseSuccess = false;
        doSemanticAnalysis = false;
        if (nextToken.type.isSequencePoint()) recoverState();
        nextToken = scanner.nextToken();
    }


    /** Сохраняет текущее состояние стека*/
    @SuppressWarnings("unchecked")
    private void saveState() {
        errorStack = (Stack<Symbol>) parseStack.clone();
    }

    @SuppressWarnings("unchecked")
    private void recoverState() {
        parseStack = (Stack<Symbol>) errorStack.clone();
    }


    private void SemanticAction(ActionSymbol action) {
        if (!doSemanticAnalysis) {
            return;
        }
        switch (action.type) {
            case START:
                analyzer.semaProgramStart();
                break;
            case END:
                ast = analyzer.semaProgramEnd();
                break;

            case SEMA_INT_LIT:
                Token intlit = tokenStack.removeFirst();
                analyzer.semaIntLit(intlit.lexeme);
                break;
            case SEMA_FLOAT_LIT:
                Token floatlit = tokenStack.removeFirst();
                analyzer.semaFloatLit(floatlit.lexeme);
                break;
            case SEMA_IDENTIFIER:
                Token ident = tokenStack.removeFirst();
                analyzer.semaIdentifier(ident.lexeme);
                break;
            case SEMA_ARRAY_TYPE:
                analyzer.semaArrayType();
                break;

            case SEMA_VAR_DEC:
                analyzer.semaVarDeclaration();
                break;
            case SEMA_TYPE_DEC:
                analyzer.semaTypeDeclaration();
                break;
            case SEMA_VAR_REF:
                analyzer.semaVariableReference(tokenStack.removeFirst().lexeme);
                break;
            case SEMA_VAR_REF_INDEX:
                analyzer.semaVariableReferenceIndex();
                break;
            case SEMA_VAR_REF_ARRAY_CHECK:
                analyzer.semaVariableReferenceArrayCheck();
                break;
            case SEMA_ASSIGN:
                analyzer.semaAssign();
                break;
            case SEMA_PLUS:
                analyzer.semaArithmeticBinOp(new AST.Add());
                break;
            case SEMA_MINUS:
                analyzer.semaArithmeticBinOp(new AST.Sub());
                break;
            case SEMA_MULT:
                analyzer.semaArithmeticBinOp(new AST.Mult());
                break;
            case SEMA_DIV:
                analyzer.semaArithmeticBinOp(new AST.Div());
                break;
            case SEMA_GREATER:
                analyzer.semaComparisonBinOp(new AST.Greater());
                break;
            case SEMA_LESSER:
                analyzer.semaComparisonBinOp(new AST.Lesser());
                break;
            case SEMA_GREATEREQ:
                analyzer.semaComparisonBinOp(new AST.GreaterEq());
                break;
            case SEMA_LESSEREQ:
                analyzer.semaComparisonBinOp(new AST.LesserEq());
                break;
            case SEMA_EQ:
                analyzer.semaComparisonBinOp(new AST.Eq());
                break;
            case SEMA_NEQ:
                analyzer.semaComparisonBinOp(new AST.Neq());
                break;
            case SEMA_AND:
                analyzer.semaLogicBinOp(new AST.And());
                break;
            case SEMA_OR:
                analyzer.semaLogicBinOp(new AST.Or());
                break;
            case SEMA_IF_START:
                analyzer.semaIfStart();
                break;
            case SEMA_IF_BLOCK:
                analyzer.semaIfBlock();
                break;
            case SEMA_ELSE_START:
                analyzer.semaElseStart();
                break;
            case SEMA_ELSE_BLOCK:
                analyzer.semaElseBlock();
                break;
            case SEMA_WHILE_START:
                analyzer.semaWhileStart();
                break;
            case SEMA_WHILE_BLOCK:
                analyzer.semaWhileBlock();
                break;
            case SEMA_FOR_START:
                analyzer.semaForStart();
                break;
            case SEMA_FOR_BLOCK:
                analyzer.semaForBlock();
                break;
            case SEMA_BREAK:
                analyzer.semaBreak();
                break;
            case SEMA_FUNC_START:
                analyzer.semaFunctionStart();
                break;
            case SEMA_FUNC_ARGS:
                analyzer.semaFunctionArgs();
                break;
            case SEMA_FUNC_RET:
                analyzer.semaFunctionReturnType();
                break;
            case SEMA_FUNC_BLOCK:
                analyzer.semaFunctionBlock();
                break;
            case SEMA_RETURN:
                analyzer.semaReturn();
                break;
            case SEMA_PROC_CALL:
                analyzer.semaProcedureCall();
                break;
            case SEMA_FUNC_CALL:
                analyzer.semaFunctionCall();
                break;
        }

        if (analyzer.isSemanticError()) {
            parseSuccess = false;
        }
    }
}
