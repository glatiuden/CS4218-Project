package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.resolveFilePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

// GodClass: The GrepApplication is complex by itself, and it is handling the functionalities it supposed to.
@SuppressWarnings("PMD.GodClass")
public class GrepApplication implements GrepInterface {
    public static final String INVALID_PATTERN = "Invalid pattern syntax";
    public static final String EMPTY_PATTERN = "Pattern should not be empty.";
    public static final String IS_DIRECTORY = "Is a directory";
    public static final String NULL_POINTER = "Null Pointer Exception";
    public static final String STDIN_FILE_PREFIX = "(standard input): ";

    private static final int NUM_ARGUMENTS = 3;
    private static final char CASE_INSEN_IDENT = 'i';
    private static final char COUNT_IDENT = 'c';
    private static final char PREFIX_FN = 'H';
    private static final int CASE_INSEN_IDX = 0;
    private static final int COUNT_INDEX = 1;
    private static final int PREFIX_FN_IDX = 2;

    private BufferedWriter bufferedWriter;

    /**
     * Extract the lines and count number of lines for grep from files and insert them into
     * lineResults and countResults respectively.
     *
     * @param pattern           supplied by user
     * @param isCaseInsensitive supplied by user
     * @param isCountLines      supplied by user
     * @param isPrefixFileName  supplied by user
     * @param fileNames         a String Array of file names supplied by user
     * @return
     */
    @Override
    public String grepFromFiles(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, String... fileNames) throws Exception {
        if (fileNames == null || pattern == null || isCaseInsensitive == null || isCountLines == null || isPrefixFileName == null) {
            throw new GrepException(NULL_POINTER);
        }

        if (pattern.isEmpty()) {
            throw new GrepException(EMPTY_PATTERN);
        }

        StringJoiner lineResults = new StringJoiner(STRING_NEWLINE);
        StringJoiner countResults = new StringJoiner(STRING_NEWLINE);

        grepResultsFromFiles(pattern, isCaseInsensitive, lineResults, countResults, fileNames);
        String results = "";
        if (isCountLines) {
            results = countResults.toString();
        } else {
            if (!lineResults.toString().isEmpty()) {
                results = lineResults.toString();
            }
        }

        boolean isSingleFile = fileNames.length == 1;
        boolean hasResults = !results.isEmpty();
        int noOfLineSeparator = results.replaceAll(String.format("[^%s]", STRING_NEWLINE), "").length();
        if (isPrefixFileName && isSingleFile && hasResults) {
            String fileName = fileNames[0];
            if (noOfLineSeparator > 0) {
                String[] resultsArray = results.split(STRING_NEWLINE);
                results = Arrays.stream(resultsArray).map((matchingStr) -> String.format("%s: %s", fileName, matchingStr)).collect(Collectors.joining(STRING_NEWLINE));
            } else {
                results = String.format("%s: %s", fileName, results);
            }
        }
        results += STRING_NEWLINE;
        return results;
    }

    /**
     * Extract the lines and count number of lines for grep from files and insert them into
     * lineResults and countResults respectively.
     *
     * @param pattern           supplied by user
     * @param isCaseInsensitive supplied by user
     * @param lineResults       a StringJoiner of the grep line results
     * @param countResults      a StringJoiner of the grep line count results
     * @param fileNames         a String Array of file names supplied by user
     */
    // ExcessiveMethodLength: With the additional isPrefixFileName implementation, the method became lengthier which is necessary for the correct implementation.
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    private void grepResultsFromFiles(String pattern, Boolean isCaseInsensitive, StringJoiner lineResults,
                                      StringJoiner countResults, String... fileNames) throws Exception {
        int count;
        boolean isSingleFile = fileNames.length == 1;
        for (String f : fileNames) {
            BufferedReader reader = null;
            try {
                Path filePath = resolveFilePath(f.trim());
                File file = filePath.toFile();
                if (!file.exists() || f.isBlank()) {
                    System.out.println("grep: " + f + ": " + ERR_FILE_NOT_FOUND);
                    continue;
                }
                if (file.isDirectory()) { // ignore if it's a directory
                    System.out.println("grep: " + f + ": " + IS_DIRECTORY);
                    continue;
                }
                if (!file.canRead()) { // Check if file can be read and display the error message more elegantly
                    System.out.println("grep: " + f + ": " + ERR_NO_PERM);
                    continue;
                }
                reader = new BufferedReader(new FileReader(filePath.toString()));
                String line;
                Pattern compiledPattern;
                if (isCaseInsensitive) {
                    compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                } else {
                    compiledPattern = Pattern.compile(pattern);
                }
                count = 0;
                String lineResult;
                String countResult;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = compiledPattern.matcher(line);
                    if (matcher.find()) { // match
                        lineResult = line;
                        if (!isSingleFile) {
                            lineResult = f + ": " + line;
                        }
                        lineResults.add(lineResult);
                        count++;
                    }
                }

                countResult = "" + count;
                if (!isSingleFile) {
                    countResult = f + ": " + count;
                }
                countResults.add(countResult);
                reader.close();
            } catch (PatternSyntaxException pse) {
                throw new GrepException(ERR_SYNTAX, pse);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
    }

    /**
     * Extract the lines and count number of lines for grep from standard input and output the result as a string.
     *
     * @param pattern           regex pattern supplied by user
     * @param isCaseInsensitive supplied by user
     * @param isCountLines      supplied by user
     * @param isPrefixFileName  supplied by user
     * @param stdin             An InputStream. The input for the command is read from this InputStream.
     * @return a String of grep results
     */
    // ExcessiveMethodLength: With the additional isPrefixFileName implementation, the method became lengthier which is necessary for the correct implementation.
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    @Override
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin) throws Exception {
        if (stdin == null || pattern == null || isCaseInsensitive == null || isCountLines == null || isPrefixFileName == null) {
            throw new GrepException(NULL_POINTER);
        }

        if (pattern.isEmpty()) {
            throw new GrepException(EMPTY_PATTERN);
        }

        int count = 0;
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            String line;
            Pattern compiledPattern;
            if (isCaseInsensitive) {
                compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } else {
                compiledPattern = Pattern.compile(pattern);
            }

            while ((line = reader.readLine()) != null) {
                Matcher matcher = compiledPattern.matcher(line);
                if (matcher.find()) { // match
                    String lineToPrint = line;
                    if (isPrefixFileName) {
                        lineToPrint = STDIN_FILE_PREFIX + line;
                    }
                    stringJoiner.add(lineToPrint);
                    if (!isCountLines) {
                        printOutput(lineToPrint, true);
                    }
                    count++;
                }
            }
            reader.close();
        } catch (PatternSyntaxException pse) {
            throw new GrepException(ERR_INVALID_REGEX, pse);
        } catch (NullPointerException npe) {
            throw new GrepException(ERR_FILE_NOT_FOUND, npe);
        } catch (IOException ioe) {
            throw new GrepException(ERR_IO_EXCEPTION, ioe);
        }

        String results = "";
        if (isCountLines) {
            results = count + STRING_NEWLINE;
            if (isPrefixFileName) {
                results = STDIN_FILE_PREFIX + count + STRING_NEWLINE;
            }
            printOutput(results, false);
        } else {
            if (!stringJoiner.toString().isEmpty()) {
                results = stringJoiner.toString() + STRING_NEWLINE;
            }
        }
        return results;
    }

    /**
     * Runs the grep application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        try {
            boolean[] grepFlags = new boolean[NUM_ARGUMENTS];
            ArrayList<String> inputFiles = new ArrayList<>();
            String pattern = getGrepArguments(args, grepFlags, inputFiles);
            String result = "";

            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(stdout));

            if (stdin == null && inputFiles.isEmpty()) {
                throw new Exception(ERR_NO_INPUT);
            }

            if (pattern == null) {
                throw new Exception(ERR_SYNTAX);
            }

            if (pattern.isEmpty()) {
                throw new Exception(EMPTY_PATTERN);
            } else {
                boolean isStdin = inputFiles.size() == 1 && inputFiles.contains("-");
                if (inputFiles.isEmpty() || isStdin) {
                    grepFromStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin);
                } else {
                    String[] inputFilesArray = new String[inputFiles.size()];
                    inputFilesArray = inputFiles.toArray(inputFilesArray);
                    if (inputFiles.contains("-")) {
                        grepFromFileAndStdin(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], stdin, inputFilesArray);
                    } else {
                        result = grepFromFiles(pattern, grepFlags[CASE_INSEN_IDX], grepFlags[COUNT_INDEX], grepFlags[PREFIX_FN_IDX], inputFilesArray);
                        printOutput(result, false);
                    }
                }
            }
        } catch (GrepException grepException) {
            throw grepException;
        } catch (Exception e) {
            throw new GrepException(e.getMessage(), e);
        }
    }

    /**
     * Separates the arguments provided by user into the flags, pattern and input files.
     *
     * @param args       supplied by user
     * @param grepFlags  a bool array of possible flags in grep
     * @param inputFiles a ArrayList<String> of file names supplied by user
     * @return regex pattern supplied by user. An empty String if not supplied.
     */
    private String getGrepArguments(String[] args, boolean[] grepFlags, ArrayList<String> inputFiles) throws Exception {
        String pattern = null;
        boolean isFile = false; // files can only appear after pattern

        for (String s : args) {
            char[] arg = s.toCharArray();
            if (isFile) {
                inputFiles.add(s);
            } else {
                if (!s.isEmpty() && arg[0] == CHAR_FLAG_PREFIX) {
                    arg = Arrays.copyOfRange(arg, 1, arg.length);
                    for (char c : arg) {
                        switch (c) {
                        case CASE_INSEN_IDENT:
                            grepFlags[CASE_INSEN_IDX] = true;
                            break;
                        case COUNT_IDENT:
                            grepFlags[COUNT_INDEX] = true;
                            break;
                        case PREFIX_FN:
                            grepFlags[PREFIX_FN_IDX] = true;
                            break;
                        default:
                            throw new GrepException(ERR_SYNTAX);
                        }
                    }
                } else { // pattern must come before file names
                    pattern = s;
                    isFile = true; // next arg onwards will be file
                }
            }
        }
        return pattern;
    }

    /**
     * Extract the lines and count number of lines for grep from files and standard input and output the result as a string.
     *
     * @param pattern           supplied by user
     * @param isCaseInsensitive supplied by user
     * @param isCountLines      supplied by user
     * @param isPrefixFileName  supplied by user
     * @param stdin             an InputStream to read input from the user
     * @param fileNames         an Array of file names supplied by user
     * @return a String of the grep results
     * @throws Exception
     */
    @Override
    public String grepFromFileAndStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin, String... fileNames) throws Exception {
        List<String> inputFileList = new ArrayList<>();
        StringJoiner stringJoiner = new StringJoiner("");
        try {
            boolean hasStdin = false;
            for (String f : fileNames) {
                boolean isStdin = f.trim().equals("-");
                if (isStdin) {
                    if (hasStdin) { // Ignore the subsequent dashes
                        continue;
                    }
                    hasStdin = true;
                    processFileArrayList(inputFileList, stringJoiner, pattern, isCaseInsensitive, isCountLines);
                    inputFileList.clear();
                    String stdinResult = grepFromStdin(pattern, isCaseInsensitive, isCountLines, true, stdin);
                    stringJoiner.add(stdinResult);
                } else {
                    inputFileList.add(f);
                }
            }

            processFileArrayList(inputFileList, stringJoiner, pattern, isCaseInsensitive, isCountLines);
            String results = "";
            if (!stringJoiner.toString().isEmpty()) {
                results = stringJoiner.toString();
            }
            return results;
        } catch (NullPointerException e) {
            throw new GrepException(NULL_POINTER, e);
        }
    }

    /**
     * Process a collection of files required to be grep, prints the output message and insert the result into a StringJoiner.
     *
     * @param inputFileList     a ArrayList<String> of file names supplied by user
     * @param stringJoiner      a StringJoiner of the grep output
     * @param pattern           supplied by user
     * @param isCaseInsensitive supplied by user
     * @param isCountLines      supplied by user
     * @throws Exception
     */
    private void processFileArrayList(List<String> inputFileList, StringJoiner stringJoiner, String pattern, Boolean isCaseInsensitive, Boolean isCountLines) throws Exception {
        if (inputFileList.isEmpty()) {
            return;
        }
        String[] inputFilesArray = new String[inputFileList.size()];
        inputFilesArray = inputFileList.toArray(inputFilesArray);
        String fileResult = grepFromFiles(pattern, isCaseInsensitive, isCountLines, true, inputFilesArray);
        printOutput(fileResult, false);
        stringJoiner.add(fileResult);
    }

    /**
     * Prints the output message to the output stream.
     *
     * @param message   the message to be printed
     * @param isNewLine boolean flag to print additional new line
     * @throws Exception
     */
    private void printOutput(String message, boolean isNewLine) throws Exception {
        // If the BufferedWriter not initialized: do nothing to avoid Exception when testing.
        if (bufferedWriter == null) {
            return;
        }

        // To avoid empty spaces or additional line from printing
        if (message.isEmpty() || message.isBlank()) {
            return;
        }

        try {
            bufferedWriter.write(message);
            if (isNewLine) {
                bufferedWriter.write(STRING_NEWLINE);
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new GrepException(ERR_WRITE_STREAM, e);
        }
    }
}
