package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.app.args.WcArguments;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.outputCurrentResults;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class WcApplication implements WcInterface {

    private static final String NUMBER_FORMAT = "\t%7d";
    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;
    long mixTotalBytes = 0, mixTotalLines = 0, mixTotalWords = 0;
    OutputStream stdout = null;

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException For empty stdout.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws WcException {
        // Format: wc [-clw] [FILES]
        if (stdout == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        this.stdout = stdout;

        WcArguments wcArgs = new WcArguments();
        wcArgs.parse(args);
        String result;

        try {
            if (wcArgs.getFiles().isEmpty()) {
                result = countFromStdin(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), stdin);
            } else {
                boolean hasDash = false;
                for (String arg : args) {
                    if ("-".equals(arg)) {
                        hasDash = true;
                        break;
                    }
                }
                if (hasDash) {
                    result = countFromFileAndStdin(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), stdin, wcArgs.getFiles().toArray(new String[0]));
                } else {
                    result = countFromFiles(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), wcArgs.getFiles().toArray(new String[0]));
                }
            }
        } catch (Exception e) {
            throw new WcException(e, ERR_GENERAL);
        }
        try {
            stdout.write(result.getBytes());
            if (!result.endsWith(STRING_NEWLINE) && !result.isEmpty()) {
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (IOException e) {
            throw new WcException(e, ERR_WRITE_STREAM);
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param fileName Array of String of file names
     * @throws Exception For empty filename.
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 String... fileName) throws Exception {
        if (fileName == null) {
            throw new WcException(ERR_GENERAL);
        }
        List<String> result = new ArrayList<>();
        long totalBytes = 0, totalLines = 0, totalWords = 0;
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                System.out.println("wc: " + file + ": " + ERR_FILE_NOT_FOUND);
                continue;
            }
            if (node.isDirectory()) {
                System.out.println("wc: " + file + ": " + ERR_IS_DIR);
                outputCurrentResults(getAllFileResult(isLines, isWords, isBytes, 0, 0, 0, String.format("\t%s", file)), stdout);
                continue;
            }
            if (!node.canRead()) {
                System.out.println("wc: " + file + ": " + ERR_NO_PERM);
                continue;
            }

            InputStream input = IOUtils.openInputStream(file); //NOPMD - suppressed CloseResource - Resources already closed in the 3rd line
            long[] count = getCountReport(input); // Line Words Bytes
            IOUtils.closeInputStream(input);

            // Update total count
            totalLines += count[0];
            totalWords += count[1];
            totalBytes += count[2];

            // Update count for file + stdin case
            mixTotalLines += count[0];
            mixTotalWords += count[1];
            mixTotalBytes += count[2];

            // Format all output: " %7d %7d %7d %s"
            // Output in the following order: lines words bytes filename
            outputCurrentResults(getAllFileResult(isLines, isWords, isBytes, count[0], count[1], count[2], String.format("\t%s", file)), stdout);
        }

        // Print cumulative counts for all the files
        if (fileName.length > 1) {
            outputCurrentResults(getAllFileResult(isLines, isWords, isBytes, totalLines, totalWords, totalBytes, "\ttotal"), stdout);
        }
        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Gets a single line of file result for WC
     *
     * @param isLines    Boolean option to count the number of lines
     * @param isWords    Boolean option to count the number of words
     * @param isBytes    Boolean option to count the number of Bytes
     * @param totalLines Total number of lines of the result
     * @param totalWords Total number of words of the result
     * @param totalBytes Total number of bytes of the result
     * @param endingText Additional text to append at the end (Optional)
     * @return string representation of the file result
     */
    public String getAllFileResult(Boolean isLines, Boolean isWords, Boolean isBytes, long totalLines, long totalWords, long totalBytes, String endingText) {
        StringBuilder output = new StringBuilder();
        if (isLines) {
            output.append(String.format(NUMBER_FORMAT, totalLines));
        }
        if (isWords) {
            output.append(String.format(NUMBER_FORMAT, totalWords));
        }
        if (isBytes) {
            output.append(String.format(NUMBER_FORMAT, totalBytes));
        }
        output.append(endingText);
        return output.toString();
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @throws Exception For empty stdin.
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        long[] count = getCountReport(stdin); // Line words bytes;

        StringBuilder output = new StringBuilder();
        if (isLines) {
            output.append(String.format(NUMBER_FORMAT, count[0]));
        }
        if (isWords) {
            output.append(String.format(NUMBER_FORMAT, count[1]));
        }
        if (isBytes) {
            output.append(String.format(NUMBER_FORMAT, count[2]));
        }

        // Update count for file + stdin case
        mixTotalLines += count[0];
        mixTotalWords += count[1];
        mixTotalBytes += count[2];

        return output.toString();
    }

    /**
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @return String representation of the result from file and stdin
     * @throws Exception For null file
     */
    @Override
    public String countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords, InputStream stdin, String... fileName) throws Exception {
        if (fileName == null) {
            throw new WcException(ERR_GENERAL);
        }
        String result;
        List<String> totalResult = new ArrayList<>();
        mixTotalBytes = 0;
        mixTotalLines = 0;
        mixTotalWords = 0;

        for (String file : fileName) {
            if ("-".equals(file.trim())) {
                result = countFromStdin(isBytes, isLines, isWords, stdin) + "\t-";
                outputCurrentResults(result, stdout);
            } else {
                countFromFiles(isBytes, isLines, isWords, file);
            }
        }

        // Print cumulative counts for all the files and stdin
        if (fileName.length > 1) {
            totalResult.add(getAllFileResult(isLines, isWords, isBytes, mixTotalLines, mixTotalWords, mixTotalBytes, "\ttotal"));
        }
        return String.join(STRING_NEWLINE, totalResult);
    }

    public void setStdOut(OutputStream stdout) {
        this.stdout = stdout;
    }

    /**
     * Returns array containing the number of lines, words, and bytes based on data in InputStream.
     *
     * @param input An InputStream
     * @throws IOException For any input error when reading the input files.
     */
    public long[] getCountReport(InputStream input) throws Exception {
        if (input == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        long[] result = new long[3]; // lines, words, bytes

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int inRead;
        boolean inWord = false;
        while ((inRead = input.read(data, 0, data.length)) != -1) {
            for (int i = 0; i < inRead; ++i) {
                if (Character.isWhitespace(data[i])) {
                    // Use <newline> character here. (Ref: UNIX)
                    if (data[i] == '\n') {
                        ++result[LINES_INDEX];
                    }
                    if (inWord) {
                        ++result[WORDS_INDEX];
                    }

                    inWord = false;
                } else {
                    inWord = true;
                }
            }
            result[BYTES_INDEX] += inRead;
            buffer.write(data, 0, inRead);
        }
        buffer.flush();
        if (inWord) {
            ++result[WORDS_INDEX]; // To handle last word
        }

        return result;
    }
}
