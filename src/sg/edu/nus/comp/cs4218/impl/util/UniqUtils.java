package sg.edu.nus.comp.cs4218.impl.util;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.app.UniqApplication.ERR_C_CAP_D;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

public final class UniqUtils {
    private UniqUtils() {
    }

    /**
     * Filters adjacent matching lines from List of input and writes to an OutputStream.
     *
     * @param isCount       Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated    Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param input         List of String input
     * @param outputStream  OutputStream to write uniq results to
     * @return List of filtered String from input
     * @throws Exception if outputStream or input is null or invalid options or cannot write to file or standard output
     */
    public static List<String> uniqInputList(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, List<String> input, OutputStream outputStream) throws Exception {
        if (input == null) {
            throw new Exception(ERR_NULL_ARGS);
        }
        if (outputStream == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        if (isCount && isAllRepeated) {
            throw new Exception(ERR_C_CAP_D);
        }

        List<List<String>> uniqTable = createFreqTable(input);
        return uniqInputTable(isCount, isRepeated, isAllRepeated, uniqTable, outputStream);
    }

    /**
     * Creates a frequency table of the input lines
     *
     * @param input List of input
     * @return frequency table created from input lines
     */
    private static List<List<String>> createFreqTable(List<String> input) {
        List<List<String>> resultTable = new ArrayList<>();
        String prevLine = null;
        int count = 1;

        for (String line : input) {
            if (line.equals(prevLine)) {
                count++;
            } else {
                if (prevLine != null) {
                    resultTable.add(Arrays.asList(Integer.toString(count), prevLine));
                }
                prevLine = line;
                count = 1;
            }
        }
        if (!input.isEmpty()) {
            resultTable.add(Arrays.asList(Integer.toString(count), prevLine));
        }

        return resultTable;
    }

    /**
     * Filters adjacent matching lines from List of input and writes to an OutputStream.
     *
     * @param isCount       Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated    Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param uniqTable     Frequency table created from List of input
     * @param outputStream  OutputStream to write uniq results to
     * @return List of filtered String from input
     * @throws Exception if cannot write to file or standard output
     */
    private static List<String> uniqInputTable(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, List<List<String>> uniqTable, OutputStream outputStream) throws Exception {
        List<String> uniqResult = new ArrayList<>();

        for (List<String> pair : uniqTable) {
            int count = Integer.parseInt(pair.get(0));
            String line = pair.get(1);
            if (isAllRepeated) {
                if (count > 1) {
                    for (int i = 0; i < count; i++) {
                        IOUtils.outputCurrentResults(line, outputStream);
                        uniqResult.add(line);
                    }
                }
            } else if (isCount && isRepeated) {
                if (count > 1) {
                    String outputString = "\t" + count + " " + line;
                    IOUtils.outputCurrentResults(outputString, outputStream);
                    uniqResult.add(outputString);
                }
            } else if (isRepeated) {
                if (count > 1) {
                    IOUtils.outputCurrentResults(line, outputStream);
                    uniqResult.add(line);
                }
            } else if (isCount) {
                String outputString = "\t" + count + " " + line;
                IOUtils.outputCurrentResults(outputString, outputStream);
                uniqResult.add(outputString);
            } else {
                IOUtils.outputCurrentResults(line, outputStream);
                uniqResult.add(line);
            }
        }

        return uniqResult;
    }
}
