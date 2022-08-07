package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtil.getFileLines;

// GodClass: The PasteApplication is complex due to its nature, the longer length of the class and higher number of
// methods are to ensure each method is a digestible. Also, everything that it is doing, it is suppose to handle.
@SuppressWarnings("PMD.GodClass")
public class PasteApplication implements PasteInterface {
    public PasteArgsParser parser;

    /**
     * Inject a parser to be used for parsing paste's arguments
     */
    public PasteApplication() {
        super();
        parser = new PasteArgsParser();
    }

    /**
     * Inject a parser to be used for parsing paste's arguments
     *
     * @param newParser New parser to use
     */
    public void setArgsParser(PasteArgsParser newParser) {
        parser = newParser;
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) Stdin arguments. If only one Stdin
     * arg is specified, echo back the Stdin.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @return String of line-wise concatenated (tab-separated) Stdin arguments
     */
    @Override
    public String mergeStdin(Boolean isSerial, InputStream stdin) {
        if (isSerial) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            return reader.lines()
                    .reduce((accum, curr) -> accum + "\t" + curr)
                    .orElse("");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        return reader.lines()
                .reduce((accum, curr) -> accum + System.lineSeparator() + curr)
                .orElse("");
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) files. If only one file is
     * specified, echo back the file content.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param fileName Array of file names to be read and merged
     * @return String of line-wise concatenated (tab-separated) files
     * @throws PasteException Exception thrown when there is an issue in merging content from input files
     */
    @Override
    public String mergeFile(Boolean isSerial, String... fileName) throws PasteException {
        try {
            StringBuilder output = new StringBuilder();
            List<LinkedList<String>> filesLines = new ArrayList<>();
            for (String file : fileName) {
                filesLines.add(new LinkedList<>(getFileLines(Path.of(file))));
            }

            if (isSerial) {
                for (int i = 0; i < filesLines.size(); i++) {
                    String line = filesLines.get(i).stream().reduce((accum, curr) -> accum + "\t" + curr).orElse("");
                    output.append(line).append(i < filesLines.size() - 1 ? System.lineSeparator() : "");
                }
                return output.toString();
            }

            int mergeCount = filesLines.stream().map(List::size)
                    .reduce((max, curr) -> curr > max ? curr : max)
                    .orElse(0);
            for (int j = 0; j < mergeCount; j++) {
                for (int i = 0; i < filesLines.size(); i++) {
                    LinkedList<String> fileLines = filesLines.get(i);
                    output.append(fileLines.isEmpty() ? "" : fileLines.pollFirst());
                    output.append(i < filesLines.size() - 1 ? '\t' : "");
                }
                output.append(j < mergeCount - 1 ? System.lineSeparator() : "");
            }

            return output.toString();
        } catch (IOException | InvalidPathException e) {
            throw new PasteException(ERR_FILE_NOT_FOUND, e);
        }
    }

    /**
     * Verify whether the lists in a String-to-String-List map is empty or not
     *
     * @param fileToLines The String-to-String-List map to verify
     * @return Boolean to indicate whether the lists are empty or not
     */
    private boolean hasLines(Map<String, List<String>> fileToLines) {
        return !fileToLines.values().stream().allMatch(List::isEmpty);
    }

    /**
     * Concatenate (line-wise) the file(s)'s content and the input stream's arguments into a String
     *
     * @param fileName    Array of file names to be read and merged
     * @param fileToLines Mapping from file to its content (in lines)
     * @return String that is a concatenation of the file(s)'s content and the input stream's content
     * @throws IOException Exception thrown when there is an issue in reading from stdin
     */
    private String concatenateFilesAndStdin(String[] fileName, Map<String, List<String>> fileToLines) throws IOException {
        StringBuilder output = new StringBuilder();

        while (hasLines(fileToLines)) {
            for (int i = 0; i < fileName.length; i++) {
                String name = fileName[i];
                List<String> fileLines = fileToLines.get(name);
                fileToLines.put(name, fileLines);
                output.append(fileLines.isEmpty() ? "" : fileLines.remove(0));
                output.append(i < fileName.length - 1 ? '\t' : "");
            }

            if (hasLines(fileToLines) && output.length() > 0) {
                output.append(System.lineSeparator());
            }
        }

        return output.toString();
    }

    /**
     * Concatenate (tab-wise) the file(s)'s content and the input stream's arguments into a String
     *
     * @param fileName    Array of file names to be read and merged
     * @param fileToLines Mapping from file to its content (in lines)
     * @return String that is a concatenation of the file(s)'s content and the input stream's content
     */
    private String concatenateFilesAndStdinSerial(String[] fileName, Map<String, List<String>> fileToLines) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < fileName.length; i++) {
            String name = fileName[i];
            String content = fileToLines.get(name).stream().reduce((accum, curr) -> accum + "\t" + curr).orElse("");
            fileToLines.put(name, new ArrayList<>());
            output.append(content);
            output.append(i < fileName.length - 1 ? System.lineSeparator() : "");
        }

        return output.toString();
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) files and Stdin arguments.
     *
     * @param isSerial Paste one file at a time instead of in parallel
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of file names to be read and merged
     * @return String of line-wise concatenated (tab-separated) files and Stdin arguments
     * @throws PasteException Exception thrown when there is an issue in merging content from input files and streams
     */
    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileName) throws PasteException {
        if (stdin == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }
        try {
            Map<String, List<String>> fileLinesMap = new HashMap<>();
            for (String name : fileName) {
                if ("-".equals(name)) {
                    if (!fileLinesMap.containsKey("-")) {
                        String inputs = mergeStdin(isSerial, stdin);
                        List<String> stdinInput = new ArrayList<>(Arrays.asList(inputs.split(isSerial ? "\t" :
                                System.lineSeparator())));
                        fileLinesMap.put("-", stdinInput);
                    }
                    continue;
                }
                fileLinesMap.put(name, getFileLines(Path.of(name)));
            }

            return isSerial
                    ? concatenateFilesAndStdinSerial(fileName, fileLinesMap)
                    : concatenateFilesAndStdin(fileName, fileLinesMap);
        } catch (IOException e) {
            throw new PasteException(ERR_FILE_NOT_FOUND, e);
        }
    }

    /**
     * Reflect whatever user has given to input onto the output
     *
     * @param input  Input where user gives their input
     * @param output Output to print what user was given
     * @throws IOException Exception thrown when there is issue reading from input or printing to output
     */
    private void echoStdin(InputStream input, OutputStream output) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line = "";
        while (true) {
            line = reader.readLine();
            if (line == null || line.length() == 0) {
                break;
            }
            output.write(line.getBytes());
            output.write(System.lineSeparator().getBytes());
        }
        reader.close();
    }

    /**
     * Run the paste application with the parsed arguments
     *
     * @param args   Arguments given with paste that was parsed
     * @param stdin  Input stream to read from
     * @param stdout Output stream to write to
     * @throws PasteException Exception thrown when there is an issue in processing the paste command
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {
        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }
        try {
            parser.parse(args);
            String[] inputs = parser.getInputs().toArray(new String[0]);
            if (parser.getInputs().size() == 0) {
                echoStdin(stdin, stdout);
                stdout.flush();
                return;
            }

            if (parser.isFilesOnly()) {
                stdout.write(mergeFile(parser.isSerial(), inputs).getBytes());
            } else {
                stdout.write(mergeFileAndStdin(parser.isSerial(), stdin, inputs).getBytes());
            }

            stdout.write(System.lineSeparator().getBytes());
            stdout.flush();
        } catch (InvalidArgsException e) {
            throw new PasteException(e.getMessage(), e);
        } catch (IOException e) {
            throw new PasteException(ERR_WRITE_STREAM, e);
        }
    }
}
