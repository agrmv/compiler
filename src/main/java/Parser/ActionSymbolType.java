package Parser;

/**
 * @author agrmv
 * */

public enum ActionSymbolType {
    START ("#start"),
    END ("#end"),

    // объявление переменной и типа
    SEMA_VAR_DEC ("#semaVarDec"),
    SEMA_TYPE_DEC("#semaTypeDec"),

    // Новые идентификаторы семантических действий
    SEMA_IDENTIFIER ("#semaIdentifier"),
    SEMA_INT_LIT ("#semaIntLit"),
    SEMA_FLOAT_LIT ("#semaFloatLit"),
    SEMA_ARRAY_TYPE ("#semaArrayType"),

    // Переменные ссылки и назначения
    SEMA_VAR_REF ("#semaVarRef"),
    SEMA_VAR_REF_INDEX ("#semaVarRefIndex"),
    SEMA_VAR_REF_ARRAY_CHECK ("#semaVarRefArrayCheck"),
    SEMA_ASSIGN ("#semaAssign"),

    // Логические бинопы
    SEMA_OR ("#semaOr"),
    SEMA_AND ("#semaAnd"),

    // CУсловные бинопы
    SEMA_GREATER ("#semaGreater"),
    SEMA_LESSER ("#semaLesser"),
    SEMA_GREATEREQ ("#semaGreaterEq"),
    SEMA_LESSEREQ ("#semaLesserEq"),
    SEMA_EQ ("#semaEq"),
    SEMA_NEQ ("#semaNeq"),

    // Арифметические бинопы
    SEMA_PLUS ("#semaPlus"),
    SEMA_MINUS ("#semaMinus"),
    SEMA_MULT ("#semaMult"),
    SEMA_DIV ("#semaDiv"),

    // Оператор if
    SEMA_IF_START ("#semaIfStart"),
    SEMA_IF_BLOCK ("#semaIfBlock"),
    SEMA_ELSE_START ("#semaElseStart"),
    SEMA_ELSE_BLOCK ("#semaElseBlock"),

    // Оператор while
    SEMA_WHILE_START ("#semaWhileStart"),
    SEMA_WHILE_BLOCK ("#semaWhileBlock"),

    // Оператор for
    SEMA_FOR_START ("#semaForStart"),
    SEMA_FOR_BLOCK ("#semaForBlock"),

    // Break
    SEMA_BREAK ("#semaBreak"),

    // Functions
    SEMA_FUNC_START ("#semaFuncStart"),
    SEMA_FUNC_ARGS ("#semaFuncArgs"),
    SEMA_FUNC_RET ("#semaFuncRet"),
    SEMA_FUNC_BLOCK ("#semaFuncBlock"),
    SEMA_RETURN ("#semaReturn"),
    SEMA_PROC_CALL ("#semaProcCall"),
    SEMA_FUNC_CALL ("#semaFuncCall");

    private final String name;

    ActionSymbolType(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}