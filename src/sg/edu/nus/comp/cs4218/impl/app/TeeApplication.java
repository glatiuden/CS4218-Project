package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

public class TeeApplication implements TeeInterface {

    private TeeArgsParser parser;
    private BufferedReader reader;
    private int linesWritten = 0;

    /**
     * Construct an instance of TeeApplication with a default instance of argument parser
     */
    public TeeApplication() {
        super();
        parser = new TeeArgsParser();
    }

    /**
     * Inject a parser to be used for parsing tee's arguments
     *
     * @param newParser New argument parser for Tee
     */
    public void setArgsParser(TeeArgsParser newParser) {
        parser = newParser;
    }

    /**
     * Inject a input stream to read input from and to printed what was read to the terminal and files
     *
     * @param input New input stream to read from
     */
    public void setupReader(InputStream input) throws InvalidArgsException {
        if (input == null) {
            throw new InvalidArgsException(ERR_NULL_ARGS);
        }
        reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    /**
     * Verify whether the directories stated in the given path exist. If they do not, create those directories
     *
     * @param path String of a path to verify
     */
    private void verifyPath(String path) {
        if (path.lastIndexOf(File.separator) != -1) {
            File folder = new File(path.substring(0, path.lastIndexOf(File.separator)));
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
    }

    /**
     * Read user's inputs and write them to the terminal and the specified files
     *
     * @param isAppend Boolean option to append the inputs to the contents of the input files
     * @param stdin    Input stream to read from
     * @param fileName Array of String of file paths
     * @return A line read from input to be printed to the terminal and specified files
     * @throws TeeException Exception thrown during a tee command
     */
    @Override
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName) throws TeeException {
        try {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }

            for (String path : fileName) {
                // Create containing folder if doesn't exist
                verifyPath(path);
                File file = new File(path);
                Files.writeString(file.toPath(), line + System.lineSeparator(),
                        StandardOpenOption.CREATE,
                        (isAppend || linesWritten > 0)
                                ? StandardOpenOption.APPEND
                                : StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);
            }

            linesWritten++;
            return line;
        } catch (IOException e) {
            throw new TeeException(e.getMessage(), e);
        }
    }

    /**
     * Run the tee application with the parsed arguments
     *
     * @param args   Arguments given with tee that was parsed
     * @param stdin  Input stream to read from
     * @param stdout Output stream to write to
     * @throws TeeException Exception thrown during a tee command
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws TeeException {
        if (args == null) {
            throw new TeeException(ERR_NULL_ARGS);
        }

        try {
            parser.parse(args);
            setupReader(stdin);
            String input = "";
            while (input != null) {
                input = teeFromStdin(parser.isAppending(), stdin, parser.getFilePaths().toArray(new String[0]));
                if (input != null) {
                    stdout.write(input.getBytes());
                    stdout.write(System.lineSeparator().getBytes());
                }
            }
            reader.close();
        } catch (Exception e) {
            throw new TeeException(e.getMessage(), e);
        }
    }
}
