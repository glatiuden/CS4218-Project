package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.util.Arrays;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CatApplication implements CatInterface {
    public static final String ERR_IS_DIR = "Is a directory";
    public static final String ERR_NO_SUCH_FILE = "No such file or directory";
    public static final String ERR_READING_FILE = "Could not read file";
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";

    private CatArgsParser catArgsParser;

    public CatApplication() {
        super();
        catArgsParser = new CatArgsParser();
    }

    public void setCatArgsParser(CatArgsParser parser) {
        catArgsParser = parser;
    }

    /**
     * Runs the cat application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws CatException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CatException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new CatException(ERR_WRITE_STREAM);
        }

        // parse flags
        CatArgsParser parser = catArgsParser;
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage(), e);
        }

        // setup
        Boolean isLineNumber = parser.isPrefixWithLineNumber();
        String[] fileNames = parser.getFiles()
                .toArray(new String[parser.getFiles().size()]);
        String output = "";

        try {
            if (fileNames.length == 0 || StringUtils.isAll("-", fileNames)) { // no args/all dashs - stdin
                output = catStdin(isLineNumber, stdin);
            } else if (Arrays.asList(fileNames).contains("-")) {
                output = catFileAndStdin(isLineNumber, stdin, fileNames);
            } else {
                output = catFiles(isLineNumber, fileNames);
            }

            if ("".equals(output)) {
                output = "" + System.lineSeparator();
            }
            stdout.write(output.getBytes());
        } catch (CatException catException) {
            throw catException;
        } catch (IOException ioException) {
            throw new CatException(ioException.getMessage(), ioException);
        }
    }

    /**
     * Responsible for handling 'cat' command relating to <u>file</u> only
     *
     * @param isLineNumber A boolean which determines whether to
     *                     prefix lines with their corresponding line number starting from 1
     * @param fileName     An array of string representing file names
     * @return A string representation of output from standard output (stdout)
     * @throws Exception If the files do not exist, is a directory, or has no read permissions
     */
    @Override
    public String catFiles(Boolean isLineNumber, String... fileName) throws CatException, IOException {
        StringBuilder result = new StringBuilder();
        if (fileName == null) {
            throw new CatException(ERR_NULL_ARGS);
        }
        for (String fileN : fileName) {
            File file;

            // validation: check if file exists (directory will pass this test)
            if (new File(fileN).isAbsolute()) {
                file = new File(fileN);
            } else {
                file = new File(Environment.currentDirectory, fileN);
            }

            if (!file.exists()) {
                try {
                    file.toPath();
                    throw new CatException(file, ERR_NO_SUCH_FILE);
                } catch (InvalidPathException e) {
                    throw new CatException(file.toString() + ": " + ERR_INVALID_PATH, e);
                }
            }

            // validation: check if is directory
            if (file.isDirectory()) {
                throw new CatException(file, ERR_IS_DIR);
            }

            // validation: read permissions
            if (!file.canRead()) {
                throw new CatException(file, ERR_READING_FILE);
            }

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getPath()))) {
                String currentLine;
                int counter = 0;
                while ((currentLine = bufferedReader.readLine()) != null) {
                    counter++;
                    if (isLineNumber) {
                        result.append(counter).append(' ');
                    }
                    result.append(currentLine).append(System.lineSeparator());
                }
            }
        }

        return result.toString();
    }

    /**
     * Responsible for handling 'cat' command relating to <u>standard input</u> only
     *
     * @param isLineNumber A boolean which determines whether to prefix lines with their
     *                     corresponding line number starting from 1
     * @param stdin        An InputStream. The text which the user wants to concatenate will be read from this
     *                     InputStream.
     * @return A string representation which has been typed in standard input, followed by new line (\n)
     * @throws Exception If the files do not exist, is a directory, or has no read permissions
     */
    @Override
    public String catStdin(Boolean isLineNumber, InputStream stdin) throws CatException, IOException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }

        StringBuilder result = new StringBuilder();
        int counter = 0;

        // read from input stream once
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        String line = null;
        while ((line = reader.readLine()) != null) {
            counter++;
            if (isLineNumber) {
                result.append(counter).append(' ');
            }
            result.append(line).append(System.lineSeparator());
        }

        return result.toString();
    }

    /**
     * Responsible for handling 'cat' command relating to both <u>files</u> and <u>standard input</u>
     *
     * @param isLineNumber A boolean which determines whether to prefix lines with their
     *                     corresponding line number starting from 1
     * @param stdin        An InputStream. If a dash (-) is a specified, the text inputted under the stream will be
     *                     concatenated.
     * @param fileName     An array of strings of file names. If non-empty, the content of the files will be
     *                     concatenated.
     * @throws Exception If the files do not exist, is a directory, or has no read permissions
     */
    @Override
    public String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileName) throws CatException, IOException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }

        StringBuilder result = new StringBuilder();

        for (String currentFile : fileName) {
            if (("-").equals(currentFile)) {
                result.append(catStdin(isLineNumber, stdin));
            } else {
                result.append(catFiles(isLineNumber, currentFile));
            }
        }

        return result.toString();
    }
}
