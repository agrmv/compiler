package SemanticAnalyzer;

import AST.*;
import AST.Node;
import Config.Config;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Семантический анализатор использует синтаксическое дерево(AST) и информацию из таблицы символов для проверки исходной
 * программы на семантическую со­гласованность с определением языка.
 * Он также собирает информацию о типах и сохраняет ее в синтаксическом дереве или в таблице символов для последую­щего
 * использования в процессе генерации промежуточного кода.
 *
 * @author agrmv
 * */

public class SemanticAnalyzer {
    /**
     * Корень АСТ в стадии строительства
     */
    private AST.ASTRoot root;

    /**
     * Таблица символов
     */
    private SymbolTable symbolTable;

    /**
     * Семантический стек, в который помещаются узлы AST
     */
    private Deque<Node> semanticStack;

    /**
     * Increment для назначения временных символов
     */
    private int tempIncrement = 0;

    /**
     * Установите в true, если произошла семантическая ошибка
     */
    private boolean semanticError = false;

    /**
     * Счетчик увеличивается каждый раз при входе в цикл и уменьшается при выходе из него
     * Используется для определения правильности/необходимости перерыва
     */
    private int loopCounter = 0;

    /**
     * Текущая функция (используется для анализа операторов возврата) return
     */
    private SemanticSymbol currentFunction = null;

    /**
     * Переменная и тип ошибки для использования в качестве заполнителей, когда ошибки приводят к тому,
     * что фактические символы не могут быть найдены
     */
    private SemanticSymbol errorType;
    private SemanticSymbol errorVar;
    private SemanticSymbol errorFunc;

    /**
     * Строка для определения текущего узла
     */
    private int currentLine = 0;

    public SemanticAnalyzer() {
        root = null;
        symbolTable = new SymbolTable();
        semanticStack = new ArrayDeque<>();

        // Тип ошибки инициализации
        errorType = new SemanticSymbol("errorType", SemanticSymbol.SymbolClass.TypeDecleration);
        errorType.setSymbolType(SemanticSymbol.SymbolType.SymbolError);
        errorType.setArraySize(0);

        // Переменная ошибки
        errorVar = new SemanticSymbol("errorVar", SemanticSymbol.SymbolClass.VarDeclaration);
        errorVar.setSymbolType(errorType);

        // Функция ошибки
        errorFunc = new SemanticSymbol("errorFunc", SemanticSymbol.SymbolClass.FunctionDeclatation);
        errorFunc.setSymbolType(errorType);
    }

    /**
     * Вывод семантических ошибок
     *
     * @param message тект ошибки
     */
    private void error(String message) {
        System.out.println(message);
        semanticError = true;
    }

    /**
     * Семантические ошибки с номером строки
     *
     * @param message текст ошибки
     * @param cause   прична ошибки
     */
    private void error(String message, Node cause) {
        System.out.println("Line " + cause.lineNumber + ": " + message);
        semanticError = true;
    }

    /**
     * Возвращает, если произошла семантическая ошибка
     */
    public boolean isSemanticError() {
        return semanticError;
    }

    /**
     * Устанавливает текущую строку
     *
     * @param line строка ошибки
     */
    public void setCurrentLine(int line) {
        currentLine = line;
    }

    /**
     * Создает корневой узел AST. Ничего особенного.
     */
    public void semaProgramStart() {
        root = new AST.ASTRoot();
        root.lineNumber = currentLine;
    }

    public AST.ASTRoot semaProgramEnd() {
        while (!semanticStack.isEmpty()) {
            Node node = semanticStack.removeLast();
            if (node instanceof Stat) {
                root.stats.add((Stat) node);
            } else if (node instanceof TypeDec) {
                root.typeDecs.add((TypeDec) node);
            } else if (node instanceof VarDec) {
                root.varDecs.add((VarDec) node);
            } else if (node instanceof FunDec) {
                root.funDecs.add((FunDec) node);
            }
        }
        return root;
    }

    /**
     * Преобразует строковый литерал в int, создает узел AST и помещает его в семантический стек
     *
     * @param lit литерал
     */
    public void semaIntLit(String lit) {
        int value = Integer.parseInt(lit);
        AST.IntLit node = new IntLit();
        node.val = value;
        node.type = symbolTable.get("int");
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    /**
     * Преобразует строковый литерал в float, создает узел AST и помещает его в семантический стек
     *
     * @param lit литерал
     */
    public void semaFloatLit(String lit) {
        float value = Float.parseFloat(lit);
        AST.FloatLit node = new FloatLit();
        node.val = value;
        node.type = symbolTable.get("float");
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    public void semaIdentifier(String lit) {
        AST.ID node = new ID();
        node.name = lit;
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    /**
     * Объявление типа
     */
    public void semaTypeDeclaration() {
        ID exisitingType = (ID) semanticStack.removeFirst();
        ID newType = (ID) semanticStack.removeFirst();

        // Проверяет, что новый тип еще не определен
        if (symbolTable.get(newType.name) != null) {
            error("Semantic error: " + newType.name + " is already defined", newType);
            return;
        }

        // Создать новый узел объявления типа
        TypeDec node = new TypeDec();

        // Случай 1: новый тип - это массив с уже созданным временным типом
        if (exisitingType.name.charAt(0) == '$') {
            // Посмотри, переименуй
            SemanticSymbol type = symbolTable.get(exisitingType.name);
            if (type == null) {
                // Эта ошибка указывает на программную ошибку
                error("Semantic error: lookup of temporary " + exisitingType.name + " failed", exisitingType);
                return;
            }
            symbolTable.rename(type, newType.name);
            node.newType = type;
        } else if (exisitingType.name.equals("int")) {
            // Случай 2: новый тип является int
            SemanticSymbol type = new SemanticSymbol(newType.name, SemanticSymbol.SymbolClass.TypeDecleration);
            type.setSymbolType(SemanticSymbol.SymbolType.SymbolInt);
            type.setArraySize(0);
            symbolTable.put(newType.name, type);
            node.newType = type;
        } else if (exisitingType.name.equals("float")) {
            // Случай 3: новый тип является float
            SemanticSymbol type = new SemanticSymbol(newType.name, SemanticSymbol.SymbolClass.TypeDecleration);
            type.setSymbolType(SemanticSymbol.SymbolType.SymbolFloat);
            type.setArraySize(0);
            symbolTable.put(newType.name, type);
            node.newType = type;
        } else {
            // Случай 4: новый тип является псевдонимом другого пользовательского типа

            // Создайте новый тип для псевдонима искомого типа
            SemanticSymbol type = new SemanticSymbol(newType.name, SemanticSymbol.SymbolClass.TypeDecleration);
            type.setArraySize(0);

            // Поиск
            SemanticSymbol lookup = symbolTable.get(exisitingType.name);
            if (lookup == null) {
                error("Semantic error: " + exisitingType.name + " is not a defined type", exisitingType);
                // Псевдоним тип ошибки, если не удалось
                type.setSymbolType(errorType);
            } else if (lookup.getSymbolClass() != SemanticSymbol.SymbolClass.TypeDecleration) {
                error("Semantic error: " + exisitingType.name + " is not a defined type", exisitingType);
                type.setSymbolType(errorType);
            } else {
                type.setSymbolType(lookup);
            }

            symbolTable.put(newType.name, type);
            node.newType = type;
        }
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    /**
     * Объявление типа переменной
     */
    public void semaVarDeclaration() {
        Node top = semanticStack.removeFirst();
        Const initializer = null;
        ID type;
        ArrayList<ID> varNames = new ArrayList<>();
        ArrayList<SemanticSymbol> varSymbols = new ArrayList<>();
        if (top instanceof IntLit || top instanceof FloatLit) {
            initializer = (Const) top;
            type = (ID) semanticStack.removeFirst();
        } else {
            type = (ID) top;
        }

        while (semanticStack.peekFirst() instanceof ID) {
            varNames.add(0, (ID) semanticStack.removeFirst()); // add to front
        }

        // Выполнить поиск типа
        SemanticSymbol typeSymbol = symbolTable.get(type.name);
        if (typeSymbol == null) {
            error("Semantic Error: " + type.name + " does not name a valid type", type);
            typeSymbol = errorType;
        }
        if (typeSymbol.getSymbolClass() != SemanticSymbol.SymbolClass.TypeDecleration) {
            error("Semantic Error: " + type.name + " does not name a valid type", type);
            typeSymbol = errorType;
        }

        // Создать новые записи таблицы символов для каждой новой переменной
        for (ID var : varNames) {
            if (symbolTable.get(var.name) != null) {
                error("Semantic Error: variable " + var.name + " is already defined", var);
                /*Переименовывает идентификатор в имя ошибки, на которую нельзя ссылаться
                Это сохранит значения параметров функции, которые хотел я*/
                var.name = "$error" + tempIncrement;
                tempIncrement++;
            }

            SemanticSymbol newSym = new SemanticSymbol(var.name, SemanticSymbol.SymbolClass.VarDeclaration);
            newSym.setSymbolType(typeSymbol);
            symbolTable.put(var.name, newSym);
            varSymbols.add(newSym);
        }

        // Тип проверки инициализации
        if (initializer != null) {
            // Первая проверка, если тип первого порядка
            if (typeSymbol.getSymbolType() == SemanticSymbol.SymbolType.SymbolCustom) {
                if (typeSymbol.getSymbolTypeReference().getSymbolType() != SemanticSymbol.SymbolType.SymbolError) {
                    error("Semantic error: " + typeSymbol.getName() + " is not a 1st order derived type", type);
                }
                // Удалить инициализатор, если он недействителен
                initializer = null;
            } else {
                // Проверить int и конвертировать в float при необходимости
                if (initializer instanceof IntLit) {
                    if (typeSymbol.getName().equals("float")) {
                        // Преобразовать в int по правилам преобразования грамматики
                        int val = ((IntLit) initializer).val;
                        initializer = new FloatLit();
                        ((FloatLit) initializer).val = (float) val;
                    } else if (typeSymbol.getSymbolType() != SemanticSymbol.SymbolType.SymbolInt) {
                        error("Semantic Error: Attempted to assign int to non-integer variables", initializer);
                        initializer = null;
                    }
                }

                // Тип проверки int constant значения
                if (initializer instanceof FloatLit) {
                    if (typeSymbol.getSymbolType() != SemanticSymbol.SymbolType.SymbolFloat) {
                        error("Semantic Error: Attempted to assign float to non-float variables", initializer);
                        initializer = null;
                    }
                }
            }
        }

        // Создает и пушит AST узел
        VarDec node = new VarDec();
        node.type = typeSymbol;
        node.vars = varSymbols;
        node.init = initializer;
        node.lineNumber = type.lineNumber;
        semanticStack.addFirst(node);
    }

    /**
     * Анализ для объявления типа массива. Создает временный тип и помещает идентификатор, который ссылается на него,
     * в семантический стек
     */
    public void semaArrayType() {
        ID type = (ID) semanticStack.removeFirst();
        IntLit literal = (IntLit) semanticStack.removeFirst();
        if (literal.val <= 0) {
            error("Semantic error: Attempted to create array type with size <= 0", literal);
            // В любом случае, дайте ему размер массива 1
            literal.val = 1;
        }

        String tempName = "$temp" + tempIncrement;
        tempIncrement++;

        SemanticSymbol newType = new SemanticSymbol(tempName, SemanticSymbol.SymbolClass.TypeDecleration);
        if (type.name.equals("int")) {
            newType.setSymbolType(SemanticSymbol.SymbolType.SymbolInt);
        } else if (type.name.equals("float")) {
            newType.setSymbolType(SemanticSymbol.SymbolType.SymbolFloat);
        } else {
            error("Semantic error: Array must be of type int or float", type);
            newType.setSymbolType(SemanticSymbol.SymbolType.SymbolError);
        }
        newType.setArraySize(literal.val);
        symbolTable.put(tempName, newType);

        // Идентификационная ссылка на этот новый тип
        ID reference = new ID();
        reference.name = tempName;
        reference.lineNumber = type.lineNumber;
        semanticStack.addFirst(reference);
    }

    /**
     * Объявление переменной
     *
     * @param name имя переменной
     */
    public void semaVariableReference(String name) {
        SemanticSymbol lookup = symbolTable.get(name);
        if (lookup == null || lookup.getSymbolClass() != SemanticSymbol.SymbolClass.VarDeclaration) {
            error("Semantic error: " + name + " is not a declared variable");
            lookup = errorVar;
        }

        VarReference node = new VarReference();
        node.reference = lookup;
        node.index = null;
        node.type = lookup.getSymbolTypeReference();
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    /**
     * Объявление индекса переменной
     */
    public void semaVariableReferenceIndex() {
        Expr index = (Expr) semanticStack.removeFirst();
        VarReference variable = (VarReference) semanticStack.peekFirst();
        if (!index.type.getName().equals("int")) {
            error("Semantic error: Array index must be of type int", index);
        }
        if (variable.type.getArraySize() <= 0) {
            error("Semantic error: Type " + variable.type.getName() + " is not an array type", variable);
            return;
        }
        if (variable.type.getSymbolType() == SemanticSymbol.SymbolType.SymbolInt) {
            variable.type = symbolTable.get("int");
        } else if (variable.type.getSymbolType() == SemanticSymbol.SymbolType.SymbolFloat) {
            variable.type = symbolTable.get("float");
        } else if (variable.type.getSymbolType() == SemanticSymbol.SymbolType.SymbolError) {
            variable.type = errorType;
        } else {
            variable.type = variable.type.getSymbolTypeReference();
        }
        variable.index = index;
    }

    /**
     * Гарантирует, что самая верхняя ссылка на переменную не является массивом
     */
    public void semaVariableReferenceArrayCheck() {
        VarReference var = (VarReference) semanticStack.peekFirst();
        if (var.type.getArraySize() > 0) {
            error("Semantic error: " + var.reference.getName() + " is an array but is not indexed into", var);
        }
    }

    /**
     * Провряет, может ли один тип быть неявно преобразован в другой
     *
     * @param src   исходный тип
     * @param dst   тип в который хотим преобразовать
     * @param fault узел в котором происходит преобразование
     */
    private boolean semaCanConvertType(SemanticSymbol src, SemanticSymbol dst, Node fault) {
        // Тип ошибки может быть преобразован во что угодно
        if (src == errorType || dst == errorType) {
            return true;
        }
        if (src != dst) {
            if (src.getName().equals("float") && dst.getName().equals("int")) {
                error("Semantic error: cannot convert float to int", fault);
                return false;
            }
            if (src.getName().equals("int") && dst.getName().equals("float")) {
                return true;
            }
            if (src.getName().equals("int")) {
                if (dst.getInferredPrimitive() != SemanticSymbol.SymbolType.SymbolInt) {
                    error("Semantic error: cannot assign int to type " + dst.getName(), fault);
                    return false;
                }
            } else if (src.getName().equals("float")) {
                if (dst.getInferredPrimitive() != SemanticSymbol.SymbolType.SymbolFloat) {
                    error("Semantic error: cannot assign float to type " + dst.getName(), fault);
                    return false;
                }
            } else {
                error("Semantic error: " + src.getName() + " and " + dst.getName() + " are incompatible types", fault);
                return false;
            }
        }
        return true;
    }

    /**
     * Знак
     */
    public void semaAssign() {
        Expr assignment = (Expr) semanticStack.removeFirst();
        Expr index = null;
        if (semanticStack.peekFirst() instanceof Expr) {
            index = (Expr) semanticStack.removeFirst();
        }
        ID variableID = (ID) semanticStack.removeFirst();
        SemanticSymbol variable = symbolTable.get(variableID.name);
        if (variable == null || variable.getSymbolClass() != SemanticSymbol.SymbolClass.VarDeclaration) {
            error("Semantic error: " + variableID.name + " is not a declared variable", variableID);
            variable = errorVar;
        }
        if (variable.getSymbolTypeReference().getArraySize() <= 0 && index != null) {
            error("Semantic error: " + variableID.name + " is not of an array type", variableID);
            // Индекс теперь нулевой, лол
            index = null;
        }

        SemanticSymbol baseType = variable.getSymbolTypeReference();
        if (index != null) {
            if (!index.type.getName().equals("int")) {
                error("Semantic error: Array index must be of type int", index);
            }
        }
        // Если тип является массивом, получаем базовый тип для проверки типа
        if (baseType.getArraySize() > 0 || index != null) {
            if (variable.getSymbolTypeReference().getSymbolType() == SemanticSymbol.SymbolType.SymbolInt) {
                baseType = symbolTable.get("int");
            } else if (variable.getSymbolTypeReference().getSymbolType() == SemanticSymbol.SymbolType.SymbolFloat) {
                baseType = symbolTable.get("float");
            } else if (variable.getSymbolTypeReference().getSymbolType() == SemanticSymbol.SymbolType.SymbolError) {
                baseType = errorType;
            } else {
                baseType = variable.getSymbolTypeReference().getSymbolTypeReference();
            }
        }

        semaCanConvertType(assignment.type, baseType, assignment);
        AssignStat node = new AssignStat();
        node.left = variable;
        node.right = assignment;
        node.index = index;
        node.lineNumber = variableID.lineNumber;
        semanticStack.addFirst(node);
    }

    /**
     * Проверяет, может ли один тип быть неявно преобразован в другой без ошибки
     *
     * @param src исходный тип
     * @param dst тип в который хотим преобразовать
     */
    private boolean semaCanConvertTypeNoError(SemanticSymbol src, SemanticSymbol dst) {
        // Error type can be converted to or from anything
        if (src == errorType || dst == errorType) {
            return true;
        }
        if (src != dst) {
            if (src.getName().equals("float") && dst.getName().equals("int")) {
                return false;
            }
            if (src.getName().equals("int") && dst.getName().equals("float")) {
                return true;
            }
            if (src.getName().equals("int")) {
                return dst.getInferredPrimitive() == SemanticSymbol.SymbolType.SymbolInt;
            } else if (src.getName().equals("float")) {
                return dst.getInferredPrimitive() == SemanticSymbol.SymbolType.SymbolFloat;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Арифметические операции
     *
     * @node узел
     */
    public void semaArithmeticBinOp(ArithmeticBinOp node) {
        //Получаем левый и правый элементы/переменные
        Expr right = (Expr) semanticStack.removeFirst();
        Expr left = (Expr) semanticStack.removeFirst();

        // Проверяем, можно ли одно неявно преобразовать в другое
        if (semaCanConvertTypeNoError(right.type, left.type)) {
            node.left = left;
            node.right = right;
            // Если есть тип ошибки, попробуйте вывести возможно не тип ошибки
            if (left.type.getSymbolType() != SemanticSymbol.SymbolType.SymbolError) {
                node.type = left.type;
            } else {
                node.type = right.type;
            }
            node.convertLeft = false;
        } else if (semaCanConvertTypeNoError(left.type, right.type)) {
            node.left = left;
            node.right = right;
            if (right.type.getSymbolType() != SemanticSymbol.SymbolType.SymbolError) {
                node.type = right.type;
            } else {
                node.type = left.type;
            }
            node.convertLeft = true;
        } else {
            error("Semantic error: type mismatch between " + left.type.getName() + " and " + right.type.getName(), left);
            node.left = left;
            node.right = right;
            //Нет хорошего предположения о том, что выводить, так что выводим тип ошибки
            node.type = errorType;
            node.convertLeft = false;
        }
        node.lineNumber = left.lineNumber;

        semanticStack.addFirst(node);
    }

    /**
     * Аналогично арифметическим операциям, но результат всегда является целочисленным значением
     */
    public void semaComparisonBinOp(ComparisonBinOp node) {
        Expr right = (Expr) semanticStack.removeFirst();
        Expr left = (Expr) semanticStack.removeFirst();

        if (semaCanConvertTypeNoError(right.type, left.type)) {
            node.left = left;
            node.right = right;
            node.convertLeft = false;
        } else if (semaCanConvertTypeNoError(left.type, right.type)) {
            node.left = left;
            node.right = right;
            node.convertLeft = true;
        } else {
            error("Semantic error: type mismatch between " + left.type.getName() + " and " + right.type.getName(), left);
            node.left = left;
            node.right = right;
        }
        node.type = symbolTable.get("int");
        node.lineNumber = left.lineNumber;

        semanticStack.addFirst(node);
    }

    /**
     * Логически операции
     */
    public void semaLogicBinOp(LogicBinOp node) {
        Expr right = (Expr) semanticStack.removeFirst();
        Expr left = (Expr) semanticStack.removeFirst();

        // Убедитесь, что они оба целые
        if (!left.type.getName().equals("int") || !right.type.getName().equals("int")) {
            error("Logical comparison operator only acts on integers", left);
        }
        node.left = left;
        node.right = right;
        node.type = errorType;
        node.lineNumber = left.lineNumber;

        semanticStack.addFirst(node);
    }

    /**
     * IF начальная проверка
     */
    public void semaIfStart() {
        // получить выражение
        Expr cond = (Expr) semanticStack.removeFirst();

        // условие должно быть целочисленным типом
        if (!cond.type.getName().equals("int")) {
            error("Semantic error: condition must be an integer", cond);
        }

        // Построить узел
        IfStat node = new IfStat();
        node.cond = cond;
        node.finalized = false;
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    /**
     * If блок
     */
    public void semaIfBlock() {
        Deque<Stat> statements = new ArrayDeque<>();
        while (!semanticStack.isEmpty()) {
            Stat statement = (Stat) semanticStack.peekFirst();
            /* Break если находим оператор if, который не завершен
                Это заявление if, к которому мы все прилагаем. Если оператор if завершен, он является вложенным оператором if,
                который уже будет выполнен, и мы не хотим присоединяться к вложенному оператору if*/
            if (statement instanceof IfStat) {
                if (!((IfStat) statement).finalized) {
                    break;
                }
            }
            /*В противном случае это оператор внутри блока if
            Мы используем стек, чтобы полностью изменить уже обращенные операторы*/
            statements.addFirst((Stat) semanticStack.removeFirst());
        }

        // Add the statements
        IfStat node = (IfStat) semanticStack.peekFirst();
        for (Stat stat : statements) {
            node.trueStats.add(stat);
        }
        node.finalized = true;
    }

    /**
     * Else
     */
    public void semaElseStart() {
        IfStat node = (IfStat) semanticStack.peekFirst();

        // Оператор не завершен
        node.finalized = false;
        node.falseStats = new ArrayList<>();
        node.lineNumber = currentLine;
    }

    /**
     * Очень похоже на анализ блока if
     */
    public void semaElseBlock() {
        Deque<Stat> statements = new ArrayDeque<>();
        while (!semanticStack.isEmpty()) {
            Stat statement = (Stat) semanticStack.peekFirst();
            if (statement instanceof IfStat) {
                if (!((IfStat) statement).finalized) {
                    break;
                }
            }
            statements.addFirst((Stat) semanticStack.removeFirst());
        }

        // Add the statements
        IfStat node = (IfStat) semanticStack.peekFirst();
        for (Stat stat : statements) {
            node.falseStats.add(stat);
        }
        node.finalized = true;
    }

    /**
     * While
     */
    public void semaWhileStart() {
        Expr cond = (Expr) semanticStack.removeFirst();

        // Условие должно быть целочисленным типом
        if (!cond.type.getName().equals("int")) {
            error("Semantic error: condition must be an integer", cond);
        }

        // Увеличиваем счетчик цикла
        loopCounter++;

        // Строим узел
        WhileStat node = new WhileStat();
        node.cond = cond;
        node.finalized = false;
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    public void semaWhileBlock() {
        Deque<Stat> statements = new ArrayDeque<>();
        while (!semanticStack.isEmpty()) {
            Stat statement = (Stat) semanticStack.peekFirst();
            if (statement instanceof WhileStat) {
                if (!((WhileStat) statement).finalized) {
                    break;
                }
            }
            statements.addFirst((Stat) semanticStack.removeFirst());
        }

        // Уменьшаем счетчик цикла
        loopCounter--;

        // Add the statements
        WhileStat node = (WhileStat) semanticStack.peekFirst();
        for (Stat stat : statements) {
            node.stats.add(stat);
        }
        node.finalized = true;
    }

    public void semaForStart() {
        Expr to = (Expr) semanticStack.removeFirst();
        Expr from = (Expr) semanticStack.removeFirst();
        ID varID = (ID) semanticStack.removeFirst();

        //Сначала выполнить поиск
        SemanticSymbol variable = symbolTable.get(varID.name);
        if (variable == null || variable.getSymbolClass() != SemanticSymbol.SymbolClass.VarDeclaration) {
            error("Semantic error: " + varID.name + " is not a defined variable", varID);
            variable = errorVar;
        }

        // Проверка типа
        if (semaCanConvertType(to.type, variable.getSymbolTypeReference(), to)) {
            semaCanConvertType(from.type, variable.getSymbolTypeReference(), from);
        }

        loopCounter++;

        // Строим узел for
        ForStat node = new ForStat();
        node.start = from;
        node.end = to;
        node.var = variable;
        node.finalized = false;
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    public void semaForBlock() {
        Deque<Stat> statements = new ArrayDeque<>();
        while (!semanticStack.isEmpty()) {
            Stat statement = (Stat) semanticStack.peekFirst();
            if (statement instanceof ForStat) {
                if (!((ForStat) statement).finalized) {
                    break;
                }
            }
            statements.addFirst((Stat) semanticStack.removeFirst());
        }

        loopCounter--;

        // Add the statements
        ForStat node = (ForStat) semanticStack.peekFirst();
        node.stats.addAll(statements);
        node.finalized = true;
    }

    public void semaBreak() {
        BreakStat node = new BreakStat();
        node.lineNumber = currentLine;
        if (loopCounter <= 0) {
            error("Semantic error: break statement must be inside a loop", node);
            return;
        }

        semanticStack.addFirst(node);
    }

    /**
     * И так начинаются функции....
     */
    public void semaFunctionStart() {
        ID name = (ID) semanticStack.removeFirst();
        if (symbolTable.get(name.name) != null) {
            error("Semantic error: " + name.name + "is already defined", name);
            name.name = "$errorFunc" + tempIncrement;
            tempIncrement++;
        }

        // Создать запись таблицы символов для этой функции
        SemanticSymbol symbol = new SemanticSymbol(name.name, SemanticSymbol.SymbolClass.FunctionDeclatation);
        symbolTable.put(name.name, symbol);
        currentFunction = symbol;

        // Область действия функции
        symbolTable.beginScope();

        // Создается AST узел
        FunDec node = new FunDec();
        node.function = symbol;
        node.lineNumber = currentLine;
        semanticStack.addFirst(node);
    }

    /**
     * Аргументы функции
     */
    public void semaFunctionArgs() {
        // Получить объявления со стека
        Deque<VarDec> declarations = new ArrayDeque<>();
        while (semanticStack.peekFirst() instanceof VarDec) {
            declarations.addFirst((VarDec) semanticStack.removeFirst());
        }

        // Извлечь символы в список
        ArrayList<SemanticSymbol> args = new ArrayList<>();
        for (VarDec dec : declarations) {
            args.add(dec.vars.get(0));
        }

        // Если есть аргументы, поместите их в символ функции
        if (args.size() > 0) {
            currentFunction.setFunctionParameters(args);
        }
    }

    /**
     * Возврат(return) функциии
     */
    public void semaFunctionReturnType() {
        ID type = (ID) semanticStack.removeFirst();
        SemanticSymbol symbol = symbolTable.get(type.name);
        if (symbol == null || symbol.getSymbolClass() != SemanticSymbol.SymbolClass.TypeDecleration) {
            error("Semantic error: " + type.name + " does not name a defined type", type);
            symbol = errorType;
        }
        currentFunction.setFunctionReturnType(symbol);
    }

    /**
     * Тело функции
     */
    public void semaFunctionBlock() {
        Deque<Stat> statements = new ArrayDeque<>();
        while (semanticStack.peekFirst() instanceof Stat) {
            statements.addFirst((Stat) semanticStack.removeFirst());
        }

        // Add the statements
        FunDec node = (FunDec) semanticStack.peekFirst();
        node.stats.addAll(statements);
        currentFunction = null;
        symbolTable.endScope();
    }

    /**
     * Return функции
     */
    public void semaReturn() {
        Expr ret = (Expr) semanticStack.removeFirst();
        ReturnStat node = new ReturnStat();
        node.lineNumber = currentLine;
        if (currentFunction == null) {
            error("Semantic error: return cannot appear outside a function block", node);
            return;
        }
        if (currentFunction.getFunctionReturnType() == null) {
            error("Semantic error: function with no return type cannot return", node);
            return;
        }
        if (!semaCanConvertType(ret.type, currentFunction.getFunctionReturnType(), ret)) {
            return;
        }
        node.retVal = ret;
        node.type = currentFunction.getFunctionReturnType();
        semanticStack.addFirst(node);
    }

    /**
     * Вызов функции
     */
    public void semaFunctionCall() {
        Deque<Expr> args = new ArrayDeque<>();
        while (semanticStack.peekFirst() instanceof Expr) {
            args.addFirst((Expr) semanticStack.removeFirst());
        }
        ID functionID = (ID) semanticStack.removeFirst();

        // Попытка поиска
        SemanticSymbol function = symbolTable.get(functionID.name);
        if (function == null || function.getSymbolClass() != SemanticSymbol.SymbolClass.FunctionDeclatation) {
            error("Semantic error: " + functionID.name + " is not a defined function", functionID);
            function = errorFunc;
        }

        FunCall call = new FunCall();
        call.func = function;
        call.type = function.getFunctionReturnType();

        // Сравнить количество аргументов
        if (function.getFunctionParameters() == null) {
            if (args.size() != 0 && function != errorFunc) {
                error("Semantic error: " + functionID.name + " does not take any parameters", functionID);
            }
        } else if (function.getFunctionParameters().size() != args.size()) {
            error("Semantic error: attempt to call " + functionID.name + " with invalid number of parameters", functionID);
        } else {
            // Проверить тип и добавьте параметры
            for (SemanticSymbol arg : function.getFunctionParameters()) {
                Expr ex = args.removeFirst();
                semaCanConvertType(ex.type, arg.getSymbolTypeReference(), ex);
                call.args.add(ex);
            }
        }
        call.lineNumber = functionID.lineNumber;
        semanticStack.addFirst(call);
    }

    /**
     * Вызов процедуры
     */
    public void semaProcedureCall() {
        // Получить функцию и обернуть ее в оператор процедуры
        FunCall call = (FunCall) semanticStack.removeFirst();
        ProcedureStat node = new ProcedureStat();
        node.funCall = call;
        node.lineNumber = call.lineNumber;
        semanticStack.addFirst(node);
    }
}
