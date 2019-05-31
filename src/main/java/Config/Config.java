package Config;


/**
 * @author agrmv
 * */

public class Config {

    public static String PARSE_TABLE_PATH = "src/main/resources/ParseTable.csv";
    public static String GRAMMAR_PATH = "src/main/resources/grammar.txt";
    public static String EPSILON = "EPSILON";

    public enum RegAllocator { NAIVE, INTRABLOCK};
    public static RegAllocator REG_ALLOCATOR = RegAllocator.INTRABLOCK;
}

