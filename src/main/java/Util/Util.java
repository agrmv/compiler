package Util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Util {

    public static boolean fileExists(String filename) {
        File f = new File(filename);
        return f.exists() && !f.isDirectory();
    }


    public static String[] readLines(String filename) {
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
            return lines.toArray(new String[0]);

        } catch (IOException e) {
            System.out.println("Error reading " + filename);
            System.exit(1);
        }
        return null;
    }

    public static String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    public static void writeFile(String content, String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename, StandardCharsets.UTF_8);
            writer.print(content);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error generating " + filename);
            System.out.println(e);
        }
    }

    public static String readFile(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("Aborting. Failed to read \"" + path + "\": " + e);
            System.exit(1);
            return "";
        }
    }
}
