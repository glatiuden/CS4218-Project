package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.SortInterface;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_PATH;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.getChunk;

public class SortApplication implements SortInterface {
    private SortArgsParser sortParser;
    private boolean hasInput;

    /**
     * Constructs a SortApplication object with default SortArgsParser.
     */
    public SortApplication() {
        super();
        sortParser = new SortArgsParser();
        hasInput = false;
    }

    /**
     * Runs the sort application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws SortException If any error occurred while sort is running
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws SortException {
        // Format: sort [-nrf] [FILES]
        if (stdout == null) {
            throw new SortException(ERR_NULL_STREAMS);
        }

        StringBuilder output = new StringBuilder();
        try {
            sortParser.parse(args);
            boolean isFirstWordNumber = sortParser.isFirstWordNumber(), isReverseOrder = sortParser.isReverseOrder(), isCaseIndependent = sortParser.isCaseIndependent();
            if (sortParser.getFiles().isEmpty()) {
                output.append(sortFromStdin(isFirstWordNumber, isReverseOrder, isCaseIndependent, stdin));
            } else {
                if (sortParser.getFiles().contains("-")) {
                    output.append(sortFromStdinAndFiles(isFirstWordNumber, isReverseOrder, isCaseIndependent, stdin, sortParser.getFiles().toArray(new String[0])));
                } else {
                    output.append(sortFromFiles(isFirstWordNumber, isReverseOrder, isCaseIndependent, sortParser.getFiles().toArray(new String[0])));
                }
            }
        } catch (Exception e) {
            throw new SortException(e, e.getMessage());
        }
        try {
            if (!output.toString().isEmpty() || hasInput) {
                stdout.write(output.toString().getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new SortException(e, ERR_WRITE_STREAM);
        }
    }

    /**
     * Returns string containing the orders of the lines of the specified file
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param fileNames         Array of String of file names
     * @return string containing the orders of the lines of the specified file
     * @throws Exception If fileNames is null or cannot read file
     */
    @Override
    public String sortFromFiles(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                String... fileNames) throws Exception {
        if (fileNames == null) {
            throw new Exception(ERR_NULL_ARGS);
        }

        List<String> lines = getLinesFromFile(fileNames);
        hasInput = !lines.isEmpty() || hasInput;
        sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Returns list of string of the lines of the specified file
     *
     * @param fileNames Array of String of file names
     * @return list of string containing the orders of the lines of the specified file
     * @throws Exception if fileNames is null or cannot read file
     */
    private List<String> getLinesFromFile(String... fileNames) throws Exception {
        if (fileNames == null) {
            throw new Exception(ERR_NULL_ARGS);
        }
        List<String> lines = new ArrayList<>();
        for (String file : fileNames) {
            if ("-".equals(file)) {
                continue;
            }
            try {
                File node = IOUtils.resolveFilePath(file).toFile();
                if (!node.exists()) {
                    throw new Exception(file + ": " + ERR_FILE_NOT_FOUND);
                }
                if (node.isDirectory()) {
                    throw new Exception(file + ": " + ERR_IS_DIR);
                }
                if (!node.canRead()) {
                    throw new Exception(file + ": " + ERR_NO_PERM);
                }
            } catch (InvalidPathException e) {
                throw new Exception(file + ": " + ERR_INVALID_PATH, e);
            }

            InputStream input = IOUtils.openInputStream(file); //NOPMD - suppressed CloseResource - Resource is closed below using IOUtils.
            lines.addAll(IOUtils.getLinesFromInputStream(input));
            IOUtils.closeInputStream(input);
        }
        return lines;
    }

    /**
     * Returns string containing the orders of the lines from the standard input
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param stdin             InputStream containing arguments from Stdin
     * @return string containing the orders of the lines from the standard input
     * @throws Exception If stdin is null
     */
    @Override
    public String sortFromStdin(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        List<String> lines = getLinesFromStdin(stdin);
        hasInput = !lines.isEmpty() || hasInput;
        sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Returns list of string of the lines from the standard input
     *
     * @param stdin InputStream containing arguments from Stdin
     * @return list of string containing the orders of the lines from the standard input
     * @throws Exception if stdin is null
     */
    private List<String> getLinesFromStdin(InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        return IOUtils.getLinesFromInputStream(stdin);
    }

    /**
     * Returns string containing the orders of the lines from the standard input and files
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param stdin             InputStream containing arguments from Stdin
     * @param fileNames         Array of String of file names
     * @return string containing the orders of the lines from the standard input and files
     * @throws Exception if stdin is null, fileNames is null, or cannot read file
     */
    public String sortFromStdinAndFiles(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                        InputStream stdin, String... fileNames) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        if (fileNames == null) {
            throw new Exception(ERR_NULL_ARGS);
        }

        List<String> lines = new ArrayList<>();
        lines.addAll(getLinesFromFile(fileNames));
        lines.addAll(getLinesFromStdin(stdin));
        hasInput = !lines.isEmpty() || hasInput;
        sortInputString(isFirstWordNumber, isReverseOrder, isCaseIndependent, lines);
        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Sorts the input ArrayList based on the given conditions. Invoking this function will mutate the ArrayList.
     *
     * @param isFirstWordNumber Boolean option to treat the first word of a line as a number
     * @param isReverseOrder    Boolean option to sort in reverse order
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param input             ArrayList of Strings of lines
     */
    private void sortInputString(Boolean isFirstWordNumber, Boolean isReverseOrder, Boolean isCaseIndependent,
                                 List<String> input) {
        input.sort((str1, str2) -> {
            if (!str1.isEmpty() && !str2.isEmpty()) {
                String chunk1 = getChunk(str1), chunk2 = getChunk(str2);
                String alphaNumericRegex = "[^A-Za-z0-9]";

                // Special characters first
                if (chunk1.substring(0, 1).matches(alphaNumericRegex)
                        && !chunk2.substring(0, 1).matches(alphaNumericRegex)) {
                    return -1;
                } else if (!chunk1.substring(0, 1).matches(alphaNumericRegex)
                        && chunk2.substring(0, 1).matches(alphaNumericRegex)) {
                    return 1;
                }

                // Extract the first group of numbers if possible.
                if (isFirstWordNumber) {
                    // If both chunks can be represented as numbers, sort them numerically.
                    int result;
                    if (Character.isDigit(chunk1.charAt(0)) && Character.isDigit(chunk2.charAt(0))) {
                        result = new BigInteger(chunk1).compareTo(new BigInteger(chunk2));
                    } else {
                        //result = chunk1.compareTo(chunk2);
                        result = compareCaseIndependent(isCaseIndependent, chunk1, chunk2);
                    }
                    if (result != 0) {
                        return result;
                    }

                    return compareCaseIndependent(isCaseIndependent, str1.substring(chunk1.length()), str2.substring(chunk2.length()));
                }
                return compareCaseIndependent(isCaseIndependent, str1, str2);
            }
            return compareCaseIndependent(isCaseIndependent, str1, str2);
        });

        if (isReverseOrder) {
            Collections.reverse(input);
        }
    }

    /**
     * Compares two strings case independent if stated, normally otherwise.
     *
     * @param isCaseIndependent Boolean option to perform case-independent sorting
     * @param firstString       first string to compare
     * @param secondString      second string to compare
     * @return 0 if strings are equivalent, lesser than 0 if firstString is lexicographically less than secondString,
     * more than 0 if firstString is lexicographically greater than secondString
     */
    private int compareCaseIndependent(Boolean isCaseIndependent, String firstString, String secondString) {
        if (isCaseIndependent) {
            String temp1 = firstString.toLowerCase(Locale.getDefault()), temp2 = secondString.toLowerCase(Locale.getDefault());
            int result = temp1.compareTo(temp2);
            if (result != 0) {
                return result;
            }
        }
        return firstString.compareTo(secondString);
    }

    /**
     * Sets the SortArgsParser used by SortApplication. Mainly used for testing.
     *
     * @param sortParser SortArgsParser to be used by SortApplication
     */
    public void setSortParser(SortArgsParser sortParser) {
        this.sortParser = sortParser;
    }
}
