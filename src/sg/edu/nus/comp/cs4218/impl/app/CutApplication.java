package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.app.args.CutArguments;
import sg.edu.nus.comp.cs4218.impl.util.CutUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_PATH;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CutApplication implements CutInterface {
    private static final String KEYWORD = "cut";

    private OutputStream stdout;
    private CutArguments cutArgs;

    /**
     * Constructs a CutApplication object.
     */
    public CutApplication() {
        super();
        this.cutArgs = new CutArguments();
    }

    /**
     * Runs the `cut` application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the cut option, position indexes, or
     *               path to file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws CutException If any error occurred while cut is running
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CutException {
        // Format: cut [Option] [LIST] FILES...
        if (stdout == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }

        setStdout(stdout);
        try {
            cutArgs.parse(args);
            List<int[]> ranges = cutArgs.getNumList();
            boolean isCharPo = cutArgs.isCharacterPosition(), isBytePo = cutArgs.isBytePosition();

            if (cutArgs.getFiles().isEmpty()) {
                cutFromStdin(isCharPo, isBytePo, ranges, stdin);
            } else {
                if (cutArgs.getFiles().contains("-")) {
                    cutFromStdinAndFiles(isCharPo, isBytePo, ranges, stdin, cutArgs.getFiles().toArray(new String[0]));
                } else {
                    cutFromFiles(isCharPo, isBytePo, ranges, cutArgs.getFiles().toArray(new String[0]));
                }
            }
        } catch (Exception e) {
            throw new CutException(e, e.getMessage());
        }
    }

    /**
     * Cuts out selected portions of each line of specified file
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param fileName Array of String of file names
     * @return String of selected portions of each line
     * @throws Exception If fileName is null or cannot read file
     */
    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName) throws Exception {
        if (fileName == null) {
            throw new Exception(ERR_NULL_ARGS);
        }

        List<String> lines = new ArrayList<>();
        for (String file : fileName) {
            if ("-".equals(file)) {
                continue;
            }

            try {
                File node = IOUtils.resolveFilePath(file).toFile();
                if (!node.exists()) {
                    System.out.println(KEYWORD + ": " + file + ": " + ERR_FILE_NOT_FOUND);
                    continue;
                }
                if (node.isDirectory()) {
                    System.out.println(KEYWORD + ": " + file + ": " + ERR_IS_DIR);
                    continue;
                }
                if (!node.canRead()) {
                    System.out.println(KEYWORD + ": " + file + ": " + ERR_NO_PERM);
                    continue;
                }
            } catch (InvalidPathException e) {
                System.out.println(KEYWORD + ": " + file + ": " + ERR_INVALID_PATH);
                continue;
            }

            InputStream input = IOUtils.openInputStream(file); //NOPMD - suppressed CloseResource - Resource is closed below using IOUtils.
            List<String> curLines = IOUtils.getLinesFromInputStream(input);
            lines.addAll(curLines);
            IOUtils.closeInputStream(input);
            if (!curLines.isEmpty()) {
                IOUtils.outputCurrentResults(String.join(STRING_NEWLINE, CutUtils.cutInputStringList(isCharPo, isBytePo, curLines, ranges)), stdout);
            }
        }
        return String.join(STRING_NEWLINE, CutUtils.cutInputStringList(isCharPo, isBytePo, lines, ranges));
    }

    /**
     * Cuts out selected portions of each line from standard input
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param stdin    InputStream containing arguments from Stdin
     * @return String of selected portions of each line
     * @throws Exception If stdin is null
     */
    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        String line;
        List<String> cutLines = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            String cutLine = CutUtils.cutInputString(isCharPo, isBytePo, line, ranges);
            cutLines.add(cutLine);
            IOUtils.outputCurrentResults(cutLine, stdout);
        }
        reader.close();
        return String.join(STRING_NEWLINE, cutLines);
    }

    /**
     * Cuts out selected portions of each line from standard input and specified file
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param ranges   List of 2-element arrays containing the start and end indices for cut.
     *                 For instance, cutting on the first column would be represented using a [1,1] array.
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of String of file names
     * @throws Exception if stdin is null or fileName is null or cannot read file
     */
    public void cutFromStdinAndFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin, String... fileName) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        if (fileName == null) {
            throw new Exception(ERR_NULL_ARGS);
        }

        LinkedList<String> fileNames = new LinkedList<>(Arrays.asList(fileName));
        assert (fileNames.contains("-"));

        List<String> firstFileNames = new ArrayList<>();
        while (!fileNames.isEmpty()) {
            String file = fileNames.removeFirst();

            if ("-".equals(file)) {
                cutFromFiles(isCharPo, isBytePo, ranges, firstFileNames.toArray(new String[0]));
                cutFromStdin(isCharPo, isBytePo, ranges, stdin);
                cutFromFiles(isCharPo, isBytePo, ranges, fileNames.toArray(new String[0]));
                break;
            }

            firstFileNames.add(file);
        }
    }

    /**
     * Sets OutputStream of CutApplication. Mainly used for testing.
     *
     * @param stdout specified OutputStream
     */
    public void setStdout(OutputStream stdout) {
        this.stdout = stdout;
    }

    /**
     * Sets cut arguments parser for CutApplication. Mainly used for testing.
     *
     * @param cutArgs specified cut argument parser
     */
    public void setCutArgs(CutArguments cutArgs) {
        this.cutArgs = cutArgs;
    }
}
