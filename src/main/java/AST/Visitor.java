package AST;

import SemanticAnalyzer.SemanticSymbol;

/**
 * посетитель позволяет добавлять новые виртуальные функции в семейство классов без изменения классов.
 * Вместо этого создается класс посетителя, который реализует все соответствующие специализации виртуальной функции.
 * Посетитель принимает ссылку на экземпляр в качестве входных данных и реализует цель посредством двойной отправки.
 * @author agrmv
 * */


public interface Visitor {
    void visit(ASTRoot n);
    void visit(TypeDec n);
    void visit(VarDec n);
    void visit(FunDec n);

    void visit(FunCall n);
    void visit(Param n);



    void visit(AssignStat stat);
    void visit(BreakStat stat);
    void visit(ReturnStat stat);
    void visit(IfStat stat);
    void visit(ForStat stat);
    void visit(WhileStat stat);
    void visit(ProcedureStat stat);

    void visit(ID n);
    void visit(VarReference n);
    void visit(IntLit n);
    void visit(FloatLit n);


    void visit(Add n);
    void visit(Sub n);
    void visit(Mult n);
    void visit(Div n);

    void visit(And n);
    void visit(Or n);

    void visit(Eq n);
    void visit(Neq n);
    void visit(Greater n);
    void visit(GreaterEq n);
    void visit(Lesser n);
    void visit(LesserEq n);

    void visit(SemanticSymbol n);
    void visit(StupidNode n);
}
