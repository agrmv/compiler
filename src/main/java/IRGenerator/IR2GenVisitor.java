package IRGenerator;

import Config.Config;
import AST.*;
import IR.*;
import SemanticAnalyzer.SemanticSymbol;
import java.util.ArrayList;

/**
 * @author agrmv
 * */

public class IR2GenVisitor implements Visitor {

    private ASTRoot ast;
    private ArrayList<IR> instructions = new ArrayList<>();
    private IRGenVisitorContext context = new IRGenVisitorContext();
    private boolean inFunction = false;

    IR2GenVisitor(ASTRoot ast){
        this.ast = ast;
    }

    ArrayList<IR> generateIR(){
        ast.accept(this);
        return instructions;
    }

    private static boolean intResult(Operand l, Operand r){
        return l.isInt() && r.isInt();
    }

    private TempFloatVar genIntToFloat(Operand op){
        TempFloatVar dest = TempFloatVar.gen(inFunction);
        emit(new intToFloat(op, dest));
        return dest;
    }

    private void emit(IR instruction){
        instructions.add(instruction);
    }

    public void visit(AST.ASTRoot n){
        for (TypeDec d : n.typeDecs){
            d.accept(this);
        }
        for (FunDec d : n.funDecs){
            d.accept(this);
        }
        emit(FunctionLabel.generate("main"));
        for (VarDec d : n.varDecs){
            d.accept(this);
        }
        for (Stat s : n.stats){
            s.accept(this);
        }

        emit(new ret(null));
    }
    //IR CodeGen не предпринимает никаких действий для TypeDecs
    public void visit(TypeDec n) {
    }
    // генерировать назначения для инициализированных значений
    public void visit(VarDec n){

        if (n.init != null){

            n.init.accept(this);
            Operand right = context.getRetVal();

            for (SemanticSymbol var : n.vars){
                NamedVar left = NamedVar.generateNamedVar(var);
                if (var.isArray()){
                    IntImmediate arraySize = new IntImmediate(var.getArraySize());
                    emit(new array_assign(left, arraySize, right, left.isInt()));
                    TempIntVar temp = TempIntVar.gen(inFunction);
                    SharedLabel label = new SharedLabel(left.name + "_assign");
                    if (right.isInt() && !left.isInt()) {
                        TempFloatVar conv = TempFloatVar.gen(inFunction);
                        emit(new intToFloat(right, conv));
                        right = conv;
                    }
                    emit(new assign(temp, new IntImmediate(0), true));
                    emit(label);
                    emit(new array_store(left, temp, right, left.isInt()));
                    emit(new add(temp, new IntImmediate(1), temp, true));
                    emit(new brneq(temp, arraySize, new LabelOp(label), true));
                }
                else {
                    emit(new assign(left, right, left.isInt()));
                }
            }
        }
    }
    public void visit(FunDec n){

        FunctionPrologue prologue = (FunctionPrologue) FunctionPrologue.generate("_" + n.function.getName());
        for (SemanticSymbol s : n.function.getFunctionParameters()) {
            NamedVar var = NamedVar.generateNamedVar(s);
            var.isLocal = true;
            prologue.arguments.add(var);
        }
        emit(prologue);

        inFunction = true;
        for (Stat s : n.stats){
            s.accept(this);
        }

        inFunction = false;

        SharedLabel epilogueLabel = new SharedLabel("_" + n.function.getName() + "_epilogue");
        prologue.epilogueLabel = epilogueLabel;
        emit(epilogueLabel);
        emit(new FunctionEpilogue());
    }

    public void visit(FunCall n){


        ArrayList<Operand> args = new ArrayList<>();

        for (Expr arg : n.args){
            arg.accept(this);
            args.add(context.getRetVal());
        }

        LabelOp fun = new LabelOp(FunctionLabel.generate("_" + n.func.getName()));

        // Проверить возвращаемое значение
        if (n.type == null){
            emit(new call(fun, args));
        }
        else {
            TempVar t = TempVar.gen(n.type.getInferredPrimitive(), inFunction);
            emit(new callr(fun, t, args));
            context.setRetVal(t);
        }
    }

    public void visit(Param n){
        System.out.println("WHY U HERE PARAM!");
        System.exit(1);
    }



    public void visit(AssignStat stat){

        NamedVar left = NamedVar.generateNamedVar(stat.left);
        stat.right.accept(this);
        Operand right = context.getRetVal();

        //Генерация правильного типа назначения / магазина
        if (stat.left.isArray()){
            // без индекса, поэтому генерируем array_assign
            if (stat.index == null){
                IntImmediate arrSize = new IntImmediate(stat.left.getArraySize());
                emit(new array_assign(left, arrSize, right, left.isInt()));
            }
            //индекс, поэтому сгенерируйте array_store
            else {
                stat.index.accept(this);
                Operand index = context.getRetVal();
                emit(new array_store(left, index, right, left.isInt()));
            }
        }
        //Обычная переменная без массива, генерирует нормальное назначение
        else {
            if (stat.left.isFloatPrimitive() && right.isInt()) {
                if (right instanceof IntImmediate) {
                    TempIntVar t = TempIntVar.gen(inFunction);
                    emit(new assign(t, right, true));
                    right = t;
                }
                TempFloatVar temp = TempFloatVar.gen(inFunction);
                emit(new intToFloat(right, temp));
                right = temp;
            }
            emit(new assign(left, right, stat.left.isIntPrimitive()));
        }
    }
    public void visit(BreakStat stat){
        LabelOp breakLabelOp = new LabelOp(context.breakLabels.peek());
        emit(new goTo(breakLabelOp));
    }
    public void visit(ReturnStat stat){
        stat.retVal.accept(this);
        Operand retVal = context.getRetVal();
        if (retVal.isInt() && stat.type.isFloatPrimitive()) {
            if (retVal instanceof IntImmediate) {
                TempIntVar t = TempIntVar.gen(inFunction);
                emit(new assign(t, retVal, true));
                retVal = t;
            }
            TempFloatVar temp = TempFloatVar.gen(inFunction);
            emit(new intToFloat(retVal, temp));
            retVal = temp;
        }

        emit(new ret(retVal));
    }
    public void visit(IfStat stat){
        SharedLabel ifFalse = new SharedLabel("if_false");

        context.setFalseLabel(ifFalse);
        stat.cond.accept(this);

        for (Stat s : stat.trueStats){
            s.accept(this);
        }

        // Не всегда выбрасывается. используется только если есть блок else
        SharedLabel afterElse = new SharedLabel("after_else");

        // Есть еще блок, так что пропустите
        if (stat.falseStats != null){
            emit(new goTo(new LabelOp(afterElse)));
        }

        emit(ifFalse);

        // Есть еще блок
        if (stat.falseStats != null){
            for (Stat s : stat.falseStats){
                s.accept(this);
            }
            emit(afterElse);
        }
    }
    public void visit(ForStat stat){
        SharedLabel before = new SharedLabel("before_for");
        SharedLabel after = new SharedLabel("after_for");

        stat.start.accept(this);
        Operand startIndex = context.getRetVal();
        stat.end.accept(this);
        Operand endIndex = context.getRetVal();

        NamedVar loopVar = NamedVar.generateNamedVar(stat.var);

        emit(new assign(loopVar, startIndex, loopVar.isInt()));
        emit(before);
        emit(new brgeq(loopVar, endIndex, new LabelOp(after), loopVar.isInt()));

        context.breakLabels.push(after);
        for (Stat s : stat.stats){
            s.accept(this);
        }
        context.breakLabels.pop();

        emit(new add(loopVar, new IntImmediate(1), loopVar, loopVar.isInt()));
        emit(new goTo(new LabelOp(before)));
        emit(after);
    }
    public void visit(WhileStat stat){
        SharedLabel before = new SharedLabel("before_while");
        SharedLabel after = new SharedLabel("after_while");
        emit(before);

        context.setFalseLabel(after);
        stat.cond.accept(this);

        context.breakLabels.push(after);
        for (Stat s : stat.stats){
            s.accept(this);
        }
        context.breakLabels.pop();

        emit(new goTo(new LabelOp(before)));
        emit(after);
    }
    public void visit(ProcedureStat stat){
        stat.funCall.accept(this);
        if (stat.funCall.func.getFunctionReturnType() != null){
            context.getRetVal();
        }
    }

    public void visit(ID n){
        //никогда не должен попасть сюда
        System.out.println("WHY VISIT ID NODE!!!");
        System.exit(1);
    }
    public void visit(VarReference n){
        // нормальный доступ без массива
        if (n.index == null){
            context.setRetVal(NamedVar.generateNamedVar(n.reference));
        }
        else {
            n.index.accept(this);
            Operand index = context.getRetVal();
            TempVar left = TempVar.gen(n.reference.getInferredPrimitive(), inFunction);
            NamedVar array = NamedVar.generateNamedVar(n.reference);
            emit(new array_load(left, array, index, left.isInt()));
            context.setRetVal(left);
        }
    }
    public void visit(IntLit n){
        context.setRetVal(new IntImmediate(n.val));
    }
    public void visit(FloatLit n){
        TempFloatVar dst = TempFloatVar.gen(inFunction);
        emit(new movfi(new FloatImmediate(n.val), dst));
        context.setRetVal(dst);
    }

    //передается как один элемент массива в обход
    //хотим изменить то, на что указывает левый и правый
    private TempVar processBinOp(BinOp n, Operand[] left, Operand[] right){
        n.left.accept(this);
        left[0] = context.getRetVal();
        n.right.accept(this);
        right[0] = context.getRetVal();

        if (!right[0].isInt() && left[0].isInt()){
            left[0] = genIntToFloat(left[0]);
        }
        if (!left[0].isInt() && right[0].isInt()){
            right[0] = genIntToFloat(right[0]);
        }
        return TempVar.gen(left[0], right[0], inFunction);
    }

    public void visit(Add n){
        Operand[] left = new Operand[1], right = new Operand[1];
        TempVar result = processBinOp(n, left, right);

        emit(new add(left[0], right[0], result, intResult(left[0], right[0])));

        context.setRetVal(result);
    }
    public void visit(Sub n){
        Operand[] left = new Operand[1], right = new Operand[1];
        TempVar result = processBinOp(n, left, right);

        emit(new sub(left[0], right[0], result, intResult(left[0], right[0])));
        context.setRetVal(result);
    }
    public void visit(Mult n){
        Operand[] left = new Operand[1], right = new Operand[1];
        TempVar result = processBinOp(n, left, right);

        emit(new mult(left[0], right[0], result, intResult(left[0], right[0])));
        context.setRetVal(result);
    }
    public void visit(Div n){
        Operand[] left = new Operand[1], right = new Operand[1];
        TempVar result = processBinOp(n, left, right);

        emit(new div(left[0], right[0], result, intResult(left[0], right[0])));
        context.setRetVal(result);
    }

    public void visit(And n){
        Operand[] left = new Operand[1], right = new Operand[1];
        TempVar result = processBinOp(n, left, right);

        emit(new and(left[0], right[0], result));
        context.setRetVal(result);
    }
    public void visit(Or n){
        Operand[] left = new Operand[1], right = new Operand[1];
        TempVar result = processBinOp(n, left, right);

        emit(new or(left[0], right[0], result));
        context.setRetVal(result);
    }

    private void processCompBinOp(BinOp n, Operand[] left, Operand[] right){
        n.left.accept(this);
        left[0] = context.getRetVal();
        n.right.accept(this);
        right[0] = context.getRetVal();

        if (!right[0].isInt() && left[0].isInt()){
            left[0] = genIntToFloat(left[0]);
        }
        if (!left[0].isInt() && right[0].isInt()){
            right[0] = genIntToFloat(right[0]);
        }
    }

    public void visit(Eq n){
        Label falseLabel = context.getFalseLabel();

        Operand[] left = new Operand[1], right = new Operand[1];
        processCompBinOp(n, left, right);

        emit(new brneq(left[0], right[0], new LabelOp(falseLabel), intResult(left[0], right[0])));

    }
    public void visit(Neq n){
        Label falseLabel = context.getFalseLabel();

        Operand[] left = new Operand[1], right = new Operand[1];
        processCompBinOp(n, left, right);

        emit(new breq(left[0], right[0], new LabelOp(falseLabel), intResult(left[0], right[0])));
    }
    public void visit(Greater n){
        Label falseLabel = context.getFalseLabel();

        Operand[] left = new Operand[1], right = new Operand[1];
        processCompBinOp(n, left, right);

        emit(new brleq(left[0], right[0], new LabelOp(falseLabel), intResult(left[0], right[0])));
    }
    public void visit(GreaterEq n){
        Label falseLabel = context.getFalseLabel();

        Operand[] left = new Operand[1], right = new Operand[1];
        processCompBinOp(n, left, right);

        emit(new brlt(left[0], right[0], new LabelOp(falseLabel), intResult(left[0], right[0])));
    }
    public void visit(Lesser n){
        Label falseLabel = context.getFalseLabel();

        Operand[] left = new Operand[1], right = new Operand[1];
        processCompBinOp(n, left, right);

        emit(new brgeq(left[0], right[0], new LabelOp(falseLabel), intResult(left[0], right[0])));
    }
    public void visit(LesserEq n){
        Label falseLabel = context.getFalseLabel();

        Operand[] left = new Operand[1], right = new Operand[1];
        processCompBinOp(n, left, right);

        emit(new brgt(left[0], right[0], new LabelOp(falseLabel), intResult(left[0], right[0])));
    }

    public void visit(SemanticSymbol n){

    }
    public void visit(StupidNode n){

    }
}

