package SemanticAnalyzer;

import AST.Node;
import AST.Visitor;

import java.util.ArrayList;

/**
 * Базовый класс для семантического символа, который можно ввести в таблицу символов
 * ТАКЖЕ используется в AST вместо узлов =(
 *
 * @author agrmv
 */

public class SemanticSymbol extends Node {
    // перечисление классов символов
    public enum SymbolClass {
        TypeDecleration("Type"),
        VarDeclaration("Variable"),
        FunctionDeclatation("Function");

        private String name;

        SymbolClass(String text) {
            name = text;
        }

        public String toString() {
            return name;
        }
    }

    /**Тип символа*/
    private SymbolClass symClass;

    /** Имя символа */
    private String name;

    /** Это локальная переменная?*/
    private boolean isLocal;

    public SemanticSymbol(String name, SymbolClass symClass) {
        this.name = name;
        this.symClass = symClass;
    }

    /** Спецификация типа */
    public enum SymbolType {
        SymbolInt("int"),
        SymbolFloat("float"),
        SymbolCustom("custom"),
        SymbolError("error");

        private String name;

        SymbolType(String text) {
            name = text;
        }

        public String toString() {
            return name;
        }
    }

    /** Тип символа псевдоним / хранение / возврат */
    private SymbolType type;

    /** Ссылка на семантический символ, если это псевдоним */
    private SemanticSymbol typeSymbol;

    /** Размер массива (0 означает не массив) */
    private int arraySize = 0;

    /** Параметры функции */
    private ArrayList<SemanticSymbol> functionParameters;
    private SemanticSymbol functionReturnType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocal() {
        return isLocal;
    }

    void setIsLocal(boolean local) {
        isLocal = local;
    }

    SymbolClass getSymbolClass() {
        return symClass;
    }

    void setSymbolType(SymbolType type) {
        this.type = type;
    }

    void setSymbolType(SemanticSymbol typeSymbol) {
        this.type = SymbolType.SymbolCustom;
        this.typeSymbol = typeSymbol;
    }

    SymbolType getSymbolType() {
        return type;
    }

    /** Возвращает примитив, из которого получен этот символ */
    public SymbolType getInferredPrimitive() {
        SemanticSymbol iter = this;
        while (iter.type == SymbolType.SymbolCustom) {
            iter = iter.typeSymbol;
        }
        return iter.type;
    }

    public String getPrintedType() {
        if (type == SymbolType.SymbolCustom) {
            return typeSymbol.getName();
        } else {
            return "" + type;
        }
    }

    public SemanticSymbol getSymbolTypeReference() {
        return typeSymbol;
    }

    void setArraySize(int size) {
        arraySize = size;
    }

    public int getArraySize() {
        // Получить предполагаемый размер массива
        SemanticSymbol iter = this;
        int size = arraySize;
        while (iter.type == SymbolType.SymbolCustom && size <= 0) {
            iter = iter.typeSymbol;
            size = iter.arraySize;
        }
        return size;
    }

    public boolean isIntPrimitive() {
        return getInferredPrimitive() == SemanticSymbol.SymbolType.SymbolInt;
    }

    public boolean isFloatPrimitive() {
        return getInferredPrimitive() == SymbolType.SymbolFloat;
    }

    public boolean isArray() {
        return getArraySize() > 0;
    }

    void setFunctionParameters(ArrayList<SemanticSymbol> parameters) {
        functionParameters = parameters;
    }

    public ArrayList<SemanticSymbol> getFunctionParameters() {
        return functionParameters;
    }

    void setFunctionReturnType(SemanticSymbol type) {
        functionReturnType = type;
    }

    public SemanticSymbol getFunctionReturnType() {
        return functionReturnType;
    }

    /** Уникальная строка для хеширования */
    public String uniqueString() {
        return name + "_" + isLocal + "_" + getInferredPrimitive().toString();
    }

    public String toString() {
        StringBuilder ret = new StringBuilder("Symbol: " + name + "\n" +
                "\tClass: " + symClass + "\n");
        if (symClass == SymbolClass.TypeDecleration) {
            ret.append("\tBase type: ");
            if (type == SymbolType.SymbolCustom) {
                ret.append(typeSymbol.getName());
            } else {
                ret.append(type);
            }
            if (arraySize > 0) {
                ret.append("\n\tArray Size: ").append(arraySize);
            }
        } else if (symClass == SymbolClass.VarDeclaration) {
            ret.append("\tType: ").append(typeSymbol.getName());
        } else {
            if (functionReturnType != null) {
                ret.append("\tReturn type: ").append(functionReturnType.getName());
            } else {
                ret.append("\tReturn type: void");
            }
            if (functionParameters != null) {
                ret.append("\n\tParameters:");
                for (SemanticSymbol param : functionParameters) {
                    ret.append("\n\t\t").append(param.getName()).append(" : ").append(param.getSymbolTypeReference().getName());
                }
            }
        }
        return ret.toString();
    }

    public String type() {
        return getSymbolClass().toString();
    }

    public ArrayList<Node> children() {
        return new ArrayList<>();
    }

    public ArrayList<String> attr() {
        ArrayList<String> attr = new ArrayList<>();
        attr.add(getName());
        return attr;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
