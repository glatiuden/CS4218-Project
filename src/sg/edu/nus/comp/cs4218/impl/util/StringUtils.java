package sg.edu.nus.comp.cs4218.impl.util;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

public final class StringUtils {
    public static final String STRING_NEWLINE = System.lineSeparator();
    public static final String STRING_CURR_DIR = ".";
    public static final String STRING_PARENT_DIR = "..";
    public static final char CHAR_FILE_SEP = File.separatorChar;
    public static final char CHAR_TAB = '\t';
    public static final char CHAR_SPACE = ' ';
    public static final char CHAR_DOUBLE_QUOTE = '"';
    public static final char CHAR_SINGLE_QUOTE = '\'';
    public static final char CHAR_BACK_QUOTE = '`';
    public static final char CHAR_REDIR_INPUT = '<';
    public static final char CHAR_REDIR_OUTPUT = '>';
    public static final char CHAR_PIPE = '|';
    public static final char CHAR_SEMICOLON = ';';
    public static final char CHAR_ASTERISK = '*';
    public static final char CHAR_FLAG_PREFIX = '-';

    private StringUtils() {
    }

    /**
     * Returns the file separator defined for a particular system.
     * Used for RegexArgument parsing only.
     *
     * @return String of file separator
     */
    public static String fileSeparator() {
        // We need to escape \ in Windows...
        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win")) {
            return '\\' + File.separator;
        }
        return File.separator;
    }

    /**
     * Check if string contains only whitespace
     *
     * @param str String to be checked
     * @return true under any one of the 3 conditions:
     * 1. string is null
     * 2. string is empty
     * 3. string contains only whitespace
     */
    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }

        // BUG FOUND: Never include i++
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false; // BUG FOUND: It should be that if any character is not a white space, means it is false, change condition to !
            }
        }

        return true;
    }

    public static boolean isAll(String str, String... strings) {
        if (strings.length == 0) {
            return false;
        }
        for (int i = 0; i < strings.length; i++) {
            if (!str.equals(strings[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Performs c * n (as in python)
     *
     * @param character char to be multiplied
     * @param num       if num is < 0, num is assumed to be 0
     * @return String result of c * n
     */
    public static String multiplyChar(char character, int num) {
        int length = num <= 0 ? 0 : num;
        char[] arr = new char[length];
        Arrays.fill(arr, character);
        return new String(arr);
    }

    /**
     * Tokenize a string delimited by whitespace
     *
     * @param str String to be tokenized
     * @return String array containing the tokens
     */
    public static String[] tokenize(String str) {
        if (isBlank(str)) {
            return new String[0];
        }
        return str.trim().split("\\s+");
    }

    /**
     * Checks if a string can be represented as a number.
     *
     * @param str String possibly representing a number
     * @return true if str can be represented as a number
     */
    public static boolean isNumber(String str) {
        BigInteger bigInt;
        try {
            bigInt = new BigInteger(str);
        } catch (Exception ex) {
            return false;
        }
        return !bigInt.toString().isEmpty();
    }

    /**
     * Extracts a chunk of numbers or non-numbers from str starting from index 0.
     *
     * @param str Input string to read from
     * @return chunk string of numbers or non-numbers
     */
    public static String getChunk(String str) {
        int startIndexLocal = 0;
        StringBuilder chunk = new StringBuilder();
        final int strLen = str.length();
        char chr = str.charAt(startIndexLocal++);
        chunk.append(chr);
        final boolean extractDigit = Character.isDigit(chr);
        while (startIndexLocal < strLen) {
            chr = str.charAt(startIndexLocal++);
            if ((extractDigit && !Character.isDigit(chr)) || (!extractDigit && Character.isDigit(chr))) {
                break;
            }
            chunk.append(chr);
        }
        return chunk.toString();
    }
}