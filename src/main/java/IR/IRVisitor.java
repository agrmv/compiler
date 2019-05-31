package IR;

/**
 *  * посетитель позволяет добавлять новые виртуальные функции в семейство классов без изменения классов.
 *  * Вместо этого создается класс посетителя, который реализует все соответствующие специализации виртуальной функции.
 *  * Посетитель принимает ссылку на экземпляр в качестве входных данных и реализует цель посредством двойной отправки.
 *  * @author agrmv
 *  */

public interface IRVisitor{

    void visit(add i);
    void visit(sub i);
    void visit(mult i);
    void visit(div i);
    void visit(and i);
    void visit(or i);
    void visit(assign i);
    void visit(array_load i);
    void visit(array_store i);
    void visit(array_assign i);
    void visit(goTo i);
    void visit(call i);
    void visit(callr i);
    void visit(ret i);
    void visit(breq i);
    void visit(brneq i);
    void visit(brlt i);
    void visit(brgt i);
    void visit(brleq i);
    void visit(brgeq i);
    void visit(SharedLabel i);
    void visit(FunctionLabel i);
    void visit(FunctionPrologue i);
    void visit(FunctionEpilogue i);

    void visit(intToFloat i);
    void visit(movfi i);

    void visit(load i);
    void visit(store i);
}
