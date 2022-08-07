package sg.edu.nus.comp.cs4218.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_DEC_RANGE;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_LIST;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_RANGE;

public final class CutUtils {
    private CutUtils() {
    }

    /**
     * Counts the amount of dashes and checks if the rest of the characters are digit.
     *
     * @param possibleNumRange LIST that is being checked
     * @return Dash count
     * @throws Exception if LIST provided is invalid
     */
    public static int countDashAndNumCheck(String possibleNumRange) throws Exception {
        int dashCount = 0;
        for (int i = 0; i < possibleNumRange.length(); i++) {
            if (possibleNumRange.charAt(i) == '-') {
                dashCount++;
            } else if (!Character.isDigit(possibleNumRange.charAt(i))) {
                throw new Exception(INVALID_LIST);
            }
        }
        return dashCount;
    }

    /**
     * Returns the position interval from the LIST.
     *
     * @param range The LIST
     * @return Integer array indicating the position interval.
     * @throws Exception if LIST provided is invalid
     */
    public static int[] getNumRange(String range) throws Exception {
        List<String> numbers = Arrays.asList(range.split("-"));
        if (numbers.size() > 2) {
            throw new Exception(INVALID_LIST);
        }
        int startIdx, endIdx;
        try {
            if (numbers.isEmpty()) {
                throw new Exception(INVALID_LIST);
            } else if (range.charAt(0) == '-') {
                startIdx = 1;
                endIdx = Integer.parseInt(numbers.get(1));
            } else if (range.charAt(range.length() - 1) == '-') {
                startIdx = Integer.parseInt(numbers.get(0));
                endIdx = Integer.MAX_VALUE;
            } else {
                startIdx = Integer.parseInt(numbers.get(0));
                endIdx = Integer.parseInt(numbers.get(1));
            }

            if (startIdx <= 0 || endIdx <= 0) {
                throw new Exception(INVALID_RANGE);
            }
            if (endIdx < startIdx) {
                throw new Exception(INVALID_DEC_RANGE);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new Exception(INVALID_RANGE, e);
        }
        return new int[]{startIdx, endIdx};
    }

    /**
     * Returns the list of merged position intervals.
     *
     * @param numList list of position intervals to merge
     * @return List of merged position intervals.
     */
    public static List<int[]> mergeNumRanges(List<int[]> numList) {
        List<int[]> mergedNumList = new ArrayList<>();
        numList.sort(Comparator.comparing(o -> o[0]));
        int[] currentRange = numList.get(0);
        mergedNumList.add(currentRange);
        for (int i = 1; i < numList.size(); i++) {
            int endIdx1 = currentRange[1], startIdx2 = numList.get(i)[0], endIdx2 = numList.get(i)[1];
            if (startIdx2 <= endIdx1) {
                currentRange[1] = Math.max(endIdx1, endIdx2);
            } else {
                currentRange = numList.get(i);
                mergedNumList.add(currentRange);
            }
        }
        return mergedNumList;
    }

    /**
     * Cuts list of string by character or byte according to OPTION and LIST provided
     *
     * @param isCharPo  Boolean option to cut by character position
     * @param isBytePo  Boolean option to cut by byte position
     * @param lines     list of string to cut
     * @param listOfNum LIST to cut string by
     * @return list of cut string
     * @throws Exception if OPTION or LIST provided is invalid
     */
    public static List<String> cutInputStringList(Boolean isCharPo, Boolean isBytePo, List<String> lines, List<int[]> listOfNum) throws Exception {
        List<String> cutLines = new ArrayList<>();
        for (String line : lines) {
            cutLines.add(cutInputString(isCharPo, isBytePo, line, listOfNum));
        }
        return cutLines;
    }

    /**
     * Cuts string by character or byte according to OPTION and LIST provided
     *
     * @param isCharPo  Boolean option to cut by character position
     * @param isBytePo  Boolean option to cut by byte position
     * @param line      string to cut
     * @param listOfNum LIST to cut string by
     * @return cut string
     * @throws Exception if OPTION or LIST provided is invalid
     */
    public static String cutInputString(Boolean isCharPo, Boolean isBytePo, String line, List<int[]> listOfNum) throws Exception {
        String cutLine;
        if (isCharPo && !isBytePo) {
            cutLine = cutString(listOfNum, line.toCharArray());
        } else if (isBytePo && !isCharPo) {
            cutLine = cutString(listOfNum, line.getBytes());
        } else {
            throw new Exception(INVALID_FLAG);
        }
        return cutLine;
    }

    /**
     * Cuts string by character according to LIST provided
     *
     * @param listOfNum LIST to cut string by
     * @param chars     string in characters to be cut
     * @return cut string
     * @throws Exception if LIST provided is invalid
     */
    private static String cutString(List<int[]> listOfNum, char... chars) throws Exception {
        if (listOfNum == null) {
            throw new Exception(INVALID_LIST);
        }
        List<Character> charList = new ArrayList<>();
        for (int[] range : listOfNum) {
            for (int i = range[0]; i <= range[1]; i++) {
                if (i - 1 > chars.length - 1) {
                    break;
                }
                charList.add(chars[i - 1]);
            }
        }
        return charList.isEmpty()
                ? ""
                : charList.toString().substring(1, 3 * charList.size() - 1)
                .replaceAll(", ", "");
    }

    /**
     * Cuts string by byte according to LIST provided
     *
     * @param listOfNum LIST to cut string by
     * @param bytes     string in bytes to be cut
     * @return cut string
     * @throws Exception if LIST provided is invalid
     */
    private static String cutString(List<int[]> listOfNum, byte... bytes) throws Exception {
        if (listOfNum == null) {
            throw new Exception(INVALID_LIST);
        }
        List<Byte> byteList = new ArrayList<>();
        for (int[] range : listOfNum) {
            for (int i = range[0]; i <= range[1]; i++) {
                if (i - 1 > bytes.length - 1) {
                    break;
                }
                byteList.add(bytes[i - 1]);
            }
        }
        byte[] cutStringInBytes = byteListToArray(byteList);
        return new String(cutStringInBytes);
    }

    /**
     * Converts a list of bytes into an array of bytes.
     *
     * @param byteList List of bytes
     * @return Array of bytes
     */
    private static byte[] byteListToArray(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }
}
