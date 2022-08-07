package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_PATH;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.UniqUtils.*;

public class UniqApplication implements UniqInterface {
    public final static String ERR_C_CAP_D = "printing all duplicated lines and repeat counts is meaningless";
    public final static String ERR_EXTRA_FILE = "extra operand ";
    private UniqArgsParser uniqParser;
    private OutputStream stdout;

    /**
     * Constructs a UniqApplication object with default UniqArgsParser.
     */
    public UniqApplication() {
        super();
        uniqParser = new UniqArgsParser();
    }

    /**
     * Runs the uniq application with specified arugments
     *
     * @param args   Array of arguments for the application. Each array element is the uniq option, path to an input
     *               file, and path to an output file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws UniqException If any error occurred while uniq is running
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws UniqException {
        // Format: uniq [Options] [INPUT_FILE [OUTPUT_FILE]]
        if (stdout == null) {
            throw new UniqException(ERR_NULL_STREAMS);
        }
        setStdout(stdout);
        try {
            uniqParser.parse(args);
            boolean isCount = uniqParser.isCount(), isRepeated = uniqParser.isRepeated(), isAllRepeated = uniqParser.isAllRepeated();
            List<String> files = uniqParser.getFiles();

            if (files.size() > 2) {
                throw new Exception(ERR_EXTRA_FILE + "'" + files.get(2) + "'");
            }
            if (isCount && isAllRepeated) {
                throw new Exception(ERR_C_CAP_D);
            }

            String outputFileName = "";
            if (files.isEmpty()) {
                uniqFromStdin(isCount, isRepeated, isAllRepeated, stdin, outputFileName);
            } else {
                String inputFileName = files.get(0);
                if (files.size() == 2) {
                    outputFileName = files.get(1);
                }
                if ("-".equals(inputFileName)) {
                    uniqFromStdin(isCount, isRepeated, isAllRepeated, stdin, outputFileName);
                } else {
                    uniqFromFile(isCount, isRepeated, isAllRepeated, inputFileName, outputFileName);
                }
            }
        } catch (Exception e) {
            throw new UniqException(e, e.getMessage());
        }
    }

    /**
     * Filters adjacent matching lines from INPUT_FILE or standard input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param inputFileName  of path to input file
     * @param outputFileName of path to output file (if any)
     * @return string of filtered results
     * @throws Exception if inputFileName or outputFileName is null or cannot read or write to file or standard output
     */
    @Override
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName, String outputFileName) throws Exception {
        if (inputFileName == null || outputFileName == null) {
            throw new Exception(ERR_NULL_ARGS);
        }
        try {
            File node = IOUtils.resolveFilePath(inputFileName).toFile();
            if (!node.exists()) {
                throw new Exception(inputFileName + ": " + ERR_FILE_NOT_FOUND);
            }
            if (node.isDirectory()) {
                throw new Exception(inputFileName + ": " + ERR_IS_DIR);
            }
            if (!node.canRead()) {
                throw new Exception(inputFileName + ": " + ERR_NO_PERM);
            }
        } catch (InvalidPathException e) {
            throw new Exception(inputFileName + ": " + ERR_INVALID_PATH, e);
        }

        InputStream inputStream = IOUtils.openInputStream(inputFileName); //NOPMD - suppressed CloseResource - Resource is closed below using IOUtils.
        List<String> lines = IOUtils.getLinesFromInputStream(inputStream);
        IOUtils.closeInputStream(inputStream);

        return getUniqString(isCount, isRepeated, isAllRepeated, lines, outputFileName);
    }

    /**
     * Filters adjacent matching lines from INPUT_FILE or standard input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param stdin          InputStream containing arguments from Stdin
     * @param outputFileName of path to output file (if any)
     * @return string of filtered results
     * @throws Exception if stdin or outputFileName is null or cannot write to file or standard output
     */
    @Override
    public String uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin, String outputFileName) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        if (outputFileName == null) {
            throw new Exception(ERR_NULL_ARGS);
        }

        List<String> lines = IOUtils.getLinesFromInputStream(stdin);

        return getUniqString(isCount, isRepeated, isAllRepeated, lines, outputFileName);
    }

    /**
     * Filters adjacent matching lines from List of input and writes to an OUTPUT_FILE or to standard output.
     *
     * @param isCount        Boolean option to prefix lines by the number of occurrences of adjacent duplicate lines
     * @param isRepeated     Boolean option to print only duplicate lines, one for each group
     * @param isAllRepeated  Boolean option to print all duplicate lines (takes precedence if isRepeated is set to true)
     * @param input          List of String input
     * @param outputFileName of path to output file (if any)
     * @return String of filtered results
     * @throws Exception if invalid options or cannot write to file or standard output
     */
    private String getUniqString(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, List<String> input, String outputFileName) throws Exception {
        String uniqResStr;

        if (outputFileName.isEmpty()) {
            List<String> uniqResult = uniqInputList(isCount, isRepeated, isAllRepeated, input, stdout);
            uniqResStr = String.join(STRING_NEWLINE, uniqResult);
        } else {
            OutputStream outputStream = null; //NOPMD - suppressed CloseResource - Resource is closed below using IOUtils.
            try {
                outputStream = IOUtils.openOutputStream(outputFileName);
                List<String> uniqResult = uniqInputList(isCount, isRepeated, isAllRepeated, input, outputStream);
                uniqResStr = String.join(STRING_NEWLINE, uniqResult);
            } catch (ShellException e) {
                throw new Exception(e.getMessage().substring(7), e);
            } finally {
                IOUtils.closeOutputStream(outputStream);
            }
        }

        return uniqResStr;
    }

    /**
     * Sets the UniqArgsParser for UniqApplication. Mainly used for testing.
     *
     * @param uniqParser specified UniqArgsParser to be used
     */
    public void setUniqParser(UniqArgsParser uniqParser) {
        this.uniqParser = uniqParser;
    }

    /**
     * Sets OutputStream for UniqApplication. Mainly used for testing.
     *
     * @param stdout specified OutputStream to be used
     */
    public void setStdout(OutputStream stdout) {
        this.stdout = stdout;
    }
}
