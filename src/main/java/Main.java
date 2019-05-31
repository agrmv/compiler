import AST.ASTToString;
import Config.Config;
import IRGenerator.IRGen;
import MIPSGenerator.MIPSGen;
import Parser.Parser;
import Parser.Lexer;
import AST.ASTRoot;
import IR.*;
import RegisterAllocator.RegAllocator;
import Util.Util;
import java.util.ArrayList;


/**
 * Класс Compiler должен быть объектом верхнего уровня
 * Он будет состоит из различных этапов компиляции
 *
 * @author agrmv
 */

public class Main {

    private static boolean genMIPS = true;
    private static boolean genIR = false;
    private static boolean genAST = false;
    private static boolean printSRC = false;
    private static boolean printMIPS = false;
    private static boolean printIR = false;
    private static boolean printAST = false;
    private static boolean ASTAsSEXP = true;
    private static String source = "";

    public static void main(String[] args) {
        checkForDependencies();
        parseArgs(args);
        compile();
    }

    private static void checkForDependencies() {
        /** Сбой сразу, если грамматика и таблица разбора не найдены */
        boolean noGrammar = !Util.fileExists(Config.GRAMMAR_PATH);
        boolean noParseTable = !Util.fileExists(Config.PARSE_TABLE_PATH);
        if (noGrammar || noParseTable) {
            System.out.println("Working Directory: " + System.getProperty("user.dir"));
            if (noGrammar)
                System.out.println("Failure, Grammar not at: " + Config.GRAMMAR_PATH);
            if (noParseTable)
                System.out.println("Failure, Parse Table not at: " + Config.PARSE_TABLE_PATH);
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("HELP");
        System.out.println("    -h :    print help message");
        System.out.println("GENERATE FILES (defaults to mips only)");
        System.out.println("    -g=ast  :   generate AST file (.ast extension)");
        System.out.println("    -g=ir   :   generate IR file (.ir extension)");
        System.out.println("    -g=mips :   generate MIPS file (.s extension)");
        System.out.println("PRINT TO STDOUT (all off by default)");
        System.out.println("    -p=src  :   print source code");
        System.out.println("    -p=ast  :   print AST");
        System.out.println("    -p=ir   :   print IR");
        System.out.println("    -p=mips :   print MIPS");
        System.out.println("    -p=mips :   print MIPS");
        System.out.println("AST PRINT OPTIONS (for both gen-file and stdout, S-Expression default)");
        System.out.println("    -ast=sexp :   prints AST as S-Expression");
        System.out.println("    -ast=easy :   print AST in a more readable format");
    }

    private static void parseArgs(String[] args) {
        if (args.length == 0) {
            printHelp();
            System.exit(0);
        }
        for (String arg : args) {

            // переключатели
            if (arg.charAt(0) == '-') {
                if (arg.equals("-h")) {
                    printHelp();
                    System.exit(0);
                }

                // опции файла
                if (arg.equals("-g=ast")) {
                    genAST = true;
                }
                if (arg.equals("-g=ir")) {
                    genIR = true;
                }
                if (arg.equals("-g=mips")) {
                    genMIPS = true;
                }
                // опции вывода
                if (arg.equals("-p=src")) {
                    printSRC = true;
                }
                if (arg.equals("-p=ast")) {
                    printAST = true;
                }
                if (arg.equals("-p=ir")) {
                    printIR = true;
                }
                if (arg.equals("-p=mips")) {
                    printMIPS = true;
                }
                // AST Print Options
                if (arg.equals("-ast=sexp")) {
                    ASTAsSEXP = true;
                }
                if (arg.equals("-ast=easy")) {
                    ASTAsSEXP = false;
                }
                // Варианты размещения регистра
                /*if (arg.equals("-a=n")) {
                    Config.REG_ALLOCATOR = Config.RegAllocator.NAIVE;
                }
                if (arg.equals("-a=i")) {
                    Config.REG_ALLOCATOR = Config.RegAllocator.INTRABLOCK;
                }
                if (arg.equals("-a=g")) {
                    Config.REG_ALLOCATOR = Config.RegAllocator.GLOBAL;
                }*/
            } else {
                // проверить исходный файл
                if (!Util.getFileExtension(arg).equals("agrmv")) {
                    System.out.println("Input file \"" + arg + "\" must have .agrmv extension. Aborting");
                    System.exit(0);
                } else if (!Util.fileExists(arg)) {
                    System.out.println("Input file \"" + arg + "\" does not exist. Aborting");
                    System.exit(0);
                } else {
                    source = arg;
                }
            }
        }
    }

    private static void compile() {
        // Разобрать исходный файл и сгенерировать AST
        Lexer scanner = new Lexer(source);
        Parser parser = new Parser(scanner);
        ASTRoot ast = parser.parse();
        if (printSRC) {
            System.out.println("\n-----SOURCE START-----");
            System.out.println(Util.readFile(source));
            System.out.println("-----SOURCE END--------");
        }
        if (printAST) {
            System.out.println("\n-----AST START-----");
            System.out.println(ASTToString.getTreeString(ast, ASTAsSEXP));
            System.out.println("-----AST END--------");
        }
        if (genAST) {
            Util.writeFile(ASTToString.getTreeString(ast, ASTAsSEXP), source.replace(".agrmv", ".ast"));
        }

        // Проходим по AST и генерируем IR код(промежуточный код)
        IRGen irgen = new IRGen(ast);
        ArrayList<IR> ir1 = irgen.generate();
        if (printIR) {
            System.out.println("\n-----IR START-----");
            System.out.print(IRStreamToString(ir1));
            System.out.println("-----IR END--------");
        }
        if (genIR) {
            Util.writeFile(IRStreamToString(ir1), source.replace(".agrmv", ".ir"));
        }

        // Итерация по IR-коду для назначения регистров и вставки нагрузок / хранилищ
        ArrayList<IR> ir2 = RegAllocator.allocate(ir1);

        // Итерация по расширенному IR для генерации кода MIPS
        String mipscode = MIPSGen.generate(ir2);
        if (printMIPS) {
            System.out.println("\n-----MIPS START-----");
            System.out.print(mipscode);
            System.out.println("-----MIPS END--------");
        }
        if (genMIPS) {
            Util.writeFile(mipscode, source.replace(".agrmv", ".s"));
        }
    }

    private static String IRStreamToString(ArrayList<IR> stream) {
        StringBuilder str = new StringBuilder();
        for (IR i : stream) {
            if (i instanceof Label)
                str.append(i).append("\n");
            else
                str.append("    ").append(i).append("\n");
        }
        return str.toString();
    }
}
