package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PasteApplicationTest {

    private final static String LINE_BREAK = System.lineSeparator();
    private final static String SERIAL_FLAG = "-s";
    private final static String FILE = "a.txt";
    private final static String OTHER_FILE = "b.txt";
    private final static String INVALID_FILE = "DONT_EXIST.txt";
    private final static String FOLDER = "DIRECTORY";
    private final static String NEST_FILE = "c.txt";
    private final static String NEST_OTHER_FILE = "d.txt";
    private final static String TEXT_A = construct("a", "b", "c", "d", "e", "f");
    private final static String TEXT_A_SERIAL = constructSerial("a", "b", "c", "d", "e", "f");
    private final static String TEXT_B = construct("1", "2", "3", "4");
    private final static String TEXT_C = construct("z", "x", "c", "v", "b", "n", "m", "o");
    private final static String TEXT_C_SERIAL = constructSerial("z", "x", "c", "v", "b", "n", "m", "o");
    private final static String TEXT_D = construct("q", "w", "e");
    private final static String TEXT_SINGLE = "a";
    private final static String TEXT_MULTI = construct("a", "b", "c");
    private final static String TEXT_MULTI_SERIAL = constructSerial("a", "b", "c");
    private final static String TEXT_EMPTY = "";
    @TempDir
    public static Path testPath;
    private static PasteApplication paste;
    private static PasteArgsParser parser;
    private static ByteArrayInputStream inputCapture;
    private static ByteArrayOutputStream outputCapture;
    private static Path filePath;
    private static Path otherFilePath;
    private static Path invalidFilePath;
    private static Path nestFilePath;
    private static Path nestOtherFilePath;

    /**
     * Construct the given Strings into a String that concatenate them with a line break in between
     *
     * @param lines An array of Strings
     * @return A String that concatenates the Strings with a line break in between
     */
    private static String construct(String... lines) {
        return Arrays.stream(lines)
                .reduce((accum, curr) -> accum + LINE_BREAK + curr)
                .orElse("");
    }

    /**
     * Construct the given Strings into a String that concatenate them with a tab in between
     *
     * @param lines An array of Strings
     * @return A String that concatenates the Strings with a tab in between
     */
    private static String constructSerial(String... lines) {
        return Arrays.stream(lines)
                .reduce((accum, curr) -> accum + "\t" + curr)
                .orElse("");
    }

    @BeforeAll
    public static void setup() throws IOException {
        paste = new PasteApplication();
        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));

        filePath = testPath.resolve(FILE);
        otherFilePath = testPath.resolve(OTHER_FILE);
        invalidFilePath = testPath.resolve(INVALID_FILE);
        nestFilePath = testPath.resolve(FOLDER).resolve(NEST_FILE);
        nestOtherFilePath = testPath.resolve(FOLDER).resolve(NEST_OTHER_FILE);

        Files.createDirectories(testPath);
        Files.createDirectories(testPath.resolve(FOLDER));
        Files.write(filePath, TEXT_A.getBytes());
        Files.write(otherFilePath, TEXT_B.getBytes());
        Files.write(nestFilePath, TEXT_C.getBytes());
        Files.write(nestOtherFilePath, TEXT_D.getBytes());
    }

    /**
     * Return the output printed in the terminal
     *
     * @return A String that represents the terminal's output
     */
    private String getTerminalOutput() {
        return outputCapture.toString();
    }

    /**
     * Merge Strings (represent a file's content) into a formatted String where each String's line is concatenated
     * together with other String's line with tabs
     *
     * @param contents An array of Strings representing files' content
     * @return The formatted String described above
     */
    private String mergeContents(boolean isSerial, String... contents) {
        List<LinkedList<String>> brokenContents = Arrays.stream(contents)
                .map(content -> new LinkedList<>(Arrays.asList(content.split(LINE_BREAK))))
                .collect(Collectors.toList());

        // Merge serially
        if (isSerial) {
            StringBuilder mergedContent = new StringBuilder();
            for (int i = 0; i < brokenContents.size(); i++) {
                mergedContent.append(brokenContents.get(i).stream().reduce((accum, curr) -> accum + "\t" + curr).orElse(""));
                mergedContent.append(LINE_BREAK);
            }
            return mergedContent.toString();
        }

        int mergeCount = brokenContents.stream()
                .map(List::size)
                .reduce((max, curr) -> curr > max ? curr : max)
                .orElse(0);

        StringBuilder mergedContent = new StringBuilder();
        for (int i = 0; i < mergeCount; i++) {
            for (int j = 0; j < brokenContents.size(); j++) {
                LinkedList<String> content = brokenContents.get(j);
                if (content.isEmpty()) {
                    mergedContent.append(j + 1 < brokenContents.size() ? '\t' : "");
                    continue;
                }
                mergedContent.append(content.pollFirst());
                if (j < brokenContents.size() - 1) {
                    mergedContent.append('\t');
                }
            }
            mergedContent.append(LINE_BREAK);
        }
        return mergedContent.toString();
    }

    /**
     * A utility function to setup the mocked parser for each unit test
     *
     * @param rawArgs     Arguments to run paste with
     * @param isSerial    Whether to run paste serially
     * @param inputs      Inputs (Files & Stdin) to run paste with
     * @param isFilesOnly Whether the input is purely file(s)
     * @param isStdinOnly Whether the input is purely stdin(s)
     * @throws InvalidArgsException Exception thrown when the the arguments passed is invalid
     */
    public void setupMock(String[] rawArgs, boolean isSerial, List<String> inputs, boolean isFilesOnly,
                          boolean isStdinOnly) throws InvalidArgsException {
        doNothing().when(parser).parse(rawArgs);
        when(parser.isSerial()).thenReturn(isSerial);
        when(parser.getInputs()).thenReturn(inputs);
        when(parser.isFilesOnly()).thenReturn(isFilesOnly);
        when(parser.isStdinOnly()).thenReturn(isStdinOnly);
    }

    @BeforeEach
    public void start() {
        parser = mock(PasteArgsParser.class);
        paste.setArgsParser(parser);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        if (inputCapture != null) {
            inputCapture.close();
        }
        outputCapture.reset();
        System.setOut(new PrintStream(outputCapture));
    }

    // mergeStdin

    @Test
    public void mergeStdin_serialSingleLineInput_oneLineOnOneLine() {
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        assertEquals(TEXT_SINGLE, paste.mergeStdin(true, inputCapture));
    }

    @Test
    public void mergeStdin_serialMultiLineInput_allLinesOnOneLine() {
        inputCapture = new ByteArrayInputStream(TEXT_MULTI.getBytes());
        assertEquals(TEXT_MULTI_SERIAL, paste.mergeStdin(true, inputCapture));
    }

    @Test
    public void mergeStdin_singleLineInput_oneLine() {
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        assertEquals(TEXT_SINGLE, paste.mergeStdin(false, inputCapture));
    }

    @Test
    public void mergeStdin_multiLineInput_multiLine() {
        inputCapture = new ByteArrayInputStream(TEXT_MULTI.getBytes());
        assertEquals(TEXT_MULTI, paste.mergeStdin(false, inputCapture));
    }

    @Test
    public void mergeStdin_noLine_noLine() {
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        assertEquals(TEXT_EMPTY, paste.mergeStdin(false, inputCapture));
    }

    // mergeFile

    @Test
    public void mergeFile_serialNoFile_noLine() throws Exception {
        assertEquals(TEXT_EMPTY, paste.mergeFile(true));
    }

    @Test
    public void mergeFile_serialFile_allLinesOnOneLine() throws Exception {
        assertEquals(TEXT_A_SERIAL, paste.mergeFile(true, filePath.toString()));
    }

    @Test
    public void mergeFile_serialDirFile_allLinesOnOneLine() throws Exception {
        assertEquals(TEXT_C_SERIAL, paste.mergeFile(true, nestFilePath.toString()));
    }

    @Test
    public void mergeFile_file_allLines() throws Exception {
        assertEquals(TEXT_A, paste.mergeFile(false, filePath.toString()));
    }

    @Test
    public void mergeFile_dirFile_allLines() throws Exception {
        assertEquals(TEXT_C, paste.mergeFile(false, nestFilePath.toString()));
    }

    @Test
    public void mergeFile_nonExistFile_throwException() {
        assertThrows(PasteException.class, () -> paste.mergeFile(false, invalidFilePath.toString()));
    }

    @Test
    public void mergeFile_noFile_noLine() throws Exception {
        assertEquals(TEXT_EMPTY, paste.mergeFile(false));
    }

    // mergeFileAndStdin

    @Test
    public void mergeFileAndStdin_serialNoFileNoInput_noLine() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        assertEquals(TEXT_EMPTY, paste.mergeFileAndStdin(true, inputCapture));
    }

    @Test
    public void mergeFileAndStdin_fileFileDirFile_mergedOutput() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        String output = paste.mergeFileAndStdin(false, inputCapture, filePath.toString(),
                otherFilePath.toString(), nestFilePath.toString()) + LINE_BREAK;
        assertEquals(mergeContents(false, TEXT_A, TEXT_B, TEXT_C), output);
    }

    @Test
    public void mergeFileAndStdin_serialFileDirFileSingleLineInput_fileByFileByInput() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        String expected = mergeContents(true, TEXT_A, TEXT_C, TEXT_SINGLE);
        String output = paste.mergeFileAndStdin(true, inputCapture, filePath.toString(),
                nestFilePath.toString(), "-") + LINE_BREAK;
        assertEquals(expected, output);
    }

    @Test
    public void mergeFileAndStdin_fileFile_allLines() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        String output = paste.mergeFileAndStdin(false, inputCapture, filePath.toString(), otherFilePath.toString()) + LINE_BREAK;
        assertEquals(mergeContents(false, TEXT_A, TEXT_B), output);
    }

    @Test
    public void mergeFileAndStdin_dirFileDirFileMultiLineInput_allLines() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_MULTI.getBytes());
        String output = paste.mergeFileAndStdin(false, inputCapture, nestFilePath.toString(),
                nestOtherFilePath.toString(), "-") + LINE_BREAK;
        assertEquals(mergeContents(false, TEXT_C, TEXT_D, TEXT_MULTI), output);
    }

    @Test
    public void mergeFileAndStdin_serialDirFile_allLinesOnOneLine() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        String output = paste.mergeFileAndStdin(true, inputCapture, nestFilePath.toString()) + LINE_BREAK;
        assertEquals(mergeContents(true, TEXT_C), output);
    }

    @Test
    public void mergeFileAndStdin_dirFileFileFile_allLines() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        String output = paste.mergeFileAndStdin(false, inputCapture, nestFilePath.toString(), filePath.toString(),
                otherFilePath.toString()) + LINE_BREAK;
        assertEquals(mergeContents(false, TEXT_C, TEXT_A, TEXT_B), output);
    }

    @Test
    public void mergeFileAndStdin_serialDirFileFileOneLineInput_allLines() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        String output = paste.mergeFileAndStdin(true, inputCapture, nestFilePath.toString(),
                filePath.toString(), "-") + LINE_BREAK;
        assertEquals(mergeContents(true, TEXT_C, TEXT_A, TEXT_SINGLE), output);
    }

    @Test
    public void mergeFileAndStdin_serialOneLineInputFileDirFile_allLines() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        String output = paste.mergeFileAndStdin(true, inputCapture, "-", filePath.toString(),
                nestFilePath.toString()) + LINE_BREAK;
        assertEquals(mergeContents(true, TEXT_SINGLE, TEXT_A, TEXT_C), output);
    }

    @Test
    public void mergeFileAndStdin_serialOneLineInputFileMultiLineInput_allLines() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(TEXT_SINGLE.getBytes());
        outputStream.write(LINE_BREAK.getBytes());
        outputStream.write(TEXT_MULTI.getBytes());

        inputCapture = new ByteArrayInputStream(outputStream.toByteArray());
        String output = paste.mergeFileAndStdin(true, inputCapture, "-", filePath.toString(), "-") + LINE_BREAK;
        assertEquals(mergeContents(true, TEXT_SINGLE + LINE_BREAK + TEXT_MULTI, TEXT_A, TEXT_EMPTY), output);
    }


    @Test
    public void mergeFileAndStdin_oneLineInputDirFile_allLines() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        String output = paste.mergeFileAndStdin(false, inputCapture, "-", nestFilePath.toString()) + LINE_BREAK;
        assertEquals(mergeContents(false, TEXT_SINGLE, TEXT_C), output);
    }

    @Test
    public void mergeFileAndStdin_serialMultiLineInputFileOneLineInput_allLines() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(TEXT_MULTI.getBytes());
        outputStream.write(LINE_BREAK.getBytes());
        outputStream.write(TEXT_SINGLE.getBytes());

        inputCapture = new ByteArrayInputStream(outputStream.toByteArray());
        String output = paste.mergeFileAndStdin(true, inputCapture, "-", filePath.toString(), "-") + LINE_BREAK;
        assertEquals(mergeContents(true, TEXT_MULTI + LINE_BREAK + TEXT_SINGLE, TEXT_A, TEXT_EMPTY),
                output);
    }

    @Test
    public void mergeFileAndStdin_serialMultiLineInputFile_allLines() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_MULTI.getBytes());
        String output = paste.mergeFileAndStdin(true, inputCapture, "-", filePath.toString()) + LINE_BREAK;
        assertEquals(mergeContents(true, TEXT_MULTI, TEXT_A), output);
    }

    @Test
    public void mergeFileAndStdin_serialMultiLineInputDirFileFile_allLines() throws Exception {
        inputCapture = new ByteArrayInputStream(TEXT_MULTI.getBytes());
        String output = paste.mergeFileAndStdin(true, inputCapture, "-", nestFilePath.toString(),
                filePath.toString()) + LINE_BREAK;
        assertEquals(mergeContents(true, TEXT_MULTI, TEXT_C, TEXT_A), output);
    }

    @Test
    public void mergeFileAndStdin_nonExistFile_throwException() {
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        assertThrows(PasteException.class, () -> paste.mergeFileAndStdin(false, inputCapture,
                invalidFilePath.toString()));
    }

    // run

    @Test
    public void run_serialNoFile_noLine() throws Exception {
        String[] args = {SERIAL_FLAG};
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        setupMock(args, true, List.of(), true, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(TEXT_EMPTY, getTerminalOutput());
    }

    @Test
    public void run_fileFileDirFile_mergedOutput() throws Exception {
        String[] args = {filePath.toString(), otherFilePath.toString(), nestFilePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        setupMock(args, false, Arrays.asList(args), true, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(false, TEXT_A, TEXT_B, TEXT_C), getTerminalOutput());
    }

    @Test
    public void run_serialFileDirFileSingleLineInput_fileByFileByInput() throws Exception {
        String[] args = {SERIAL_FLAG, filePath.toString(), nestFilePath.toString(), "-"};
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        setupMock(args, true, Arrays.asList(args).subList(1, args.length), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(true, TEXT_A, TEXT_C, TEXT_SINGLE), getTerminalOutput());
    }

    @Test
    public void run_fileFile_allLines() throws Exception {
        String[] args = {filePath.toString(), otherFilePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        setupMock(args, false, Arrays.asList(args), true, false);
        paste.run(args, inputCapture, outputCapture);

        String output = getTerminalOutput();
        assertEquals(mergeContents(false, TEXT_A, TEXT_B), output);
    }

    @Test
    public void run_dirFileDirFileMultiLineInput_allLines() throws Exception {
        String[] args = {nestFilePath.toString(), nestOtherFilePath.toString(), "-"};
        inputCapture = new ByteArrayInputStream(TEXT_MULTI.getBytes());
        setupMock(args, false, Arrays.asList(args), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(false, TEXT_C, TEXT_D, TEXT_MULTI), getTerminalOutput());
    }

    @Test
    public void run_serialDirFile_allLinesOnOneLine() throws Exception {
        String[] args = {SERIAL_FLAG, nestFilePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        setupMock(args, true, Arrays.asList(args).subList(1, args.length), true, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(true, TEXT_C), getTerminalOutput());
    }

    @Test
    public void run_dirFileFileFile_allLines() throws Exception {
        String[] args = {nestFilePath.toString(), filePath.toString(), otherFilePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        setupMock(args, false, Arrays.asList(args), true, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(false, TEXT_C, TEXT_A, TEXT_B), getTerminalOutput());
    }

    @Test
    public void run_serialDirFileFileOneLineInput_allLines() throws Exception {
        String[] args = {SERIAL_FLAG, nestFilePath.toString(), filePath.toString(), "-"};
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        setupMock(args, true, Arrays.asList(args).subList(1, args.length), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(true, TEXT_C, TEXT_A, TEXT_SINGLE), getTerminalOutput());
    }

    @Test
    public void run_serialOneLineInputFileDirFile_allLines() throws Exception {
        String[] args = {SERIAL_FLAG, "-", filePath.toString(), nestFilePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        setupMock(args, true, Arrays.asList(args).subList(1, args.length), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(true, TEXT_SINGLE, TEXT_A, TEXT_C), getTerminalOutput());
    }

    @Test
    public void run_serialOneLineInputFileMultiLineInput_allLines() throws Exception {
        String[] args = {SERIAL_FLAG, "-", filePath.toString(), "-"};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(TEXT_SINGLE.getBytes());
        outputStream.write(LINE_BREAK.getBytes());
        outputStream.write(TEXT_MULTI.getBytes());
        inputCapture = new ByteArrayInputStream(outputStream.toByteArray());

        setupMock(args, true, Arrays.asList(args).subList(1, args.length), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(true, TEXT_SINGLE + LINE_BREAK + TEXT_MULTI, TEXT_A, TEXT_EMPTY),
                getTerminalOutput());
    }

    @Test
    public void run_oneLineInputDirFile_allLines() throws Exception {
        String[] args = {"-", nestFilePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_SINGLE.getBytes());
        setupMock(args, false, Arrays.asList(args), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(false, TEXT_SINGLE, TEXT_C), getTerminalOutput());
    }

    @Test
    public void run_serialMultiLineInputFileOneLineInput_allLines() throws Exception {
        String[] args = {SERIAL_FLAG, "-", filePath.toString(), "-"};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(TEXT_MULTI.getBytes());
        outputStream.write(LINE_BREAK.getBytes());
        outputStream.write(TEXT_SINGLE.getBytes());
        inputCapture = new ByteArrayInputStream(outputStream.toByteArray());

        setupMock(args, true, Arrays.asList(args).subList(1, args.length), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(true, TEXT_MULTI + LINE_BREAK + TEXT_SINGLE, TEXT_A, TEXT_EMPTY),
                getTerminalOutput());
    }

    @Test
    public void run_serialMultiLineInputFile_allLines() throws Exception {
        String[] args = {SERIAL_FLAG, "-", filePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_MULTI.getBytes());
        setupMock(args, true, Arrays.asList(args).subList(1, args.length), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(true, TEXT_MULTI, TEXT_A), getTerminalOutput());
    }

    @Test
    public void run_serialMultiLineInputDirFileFile_allLines() throws Exception {
        String[] args = {SERIAL_FLAG, "-", nestFilePath.toString(), filePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_MULTI.getBytes());
        setupMock(args, true, Arrays.asList(args).subList(1, args.length), false, false);
        paste.run(args, inputCapture, outputCapture);
        assertEquals(mergeContents(true, TEXT_MULTI, TEXT_C, TEXT_A), getTerminalOutput());
    }

    @Test
    public void run_nonExistFile_throwException() throws Exception {
        String[] args = {invalidFilePath.toString()};
        inputCapture = new ByteArrayInputStream(TEXT_EMPTY.getBytes());
        setupMock(args, false, Arrays.asList(args), true, false);
        assertThrows(PasteException.class, () -> paste.run(args, inputCapture, outputCapture));
    }

    @Test
    public void run_threeStdInOneMultiInput_allLines() throws InvalidArgsException, PasteException {
        String[] args = {"-", "-", "-"};
        inputCapture = new ByteArrayInputStream(TEXT_C.getBytes());
        setupMock(args, false, Arrays.asList(args), false, true);
        paste.run(args, inputCapture, outputCapture);

        String[] words = TEXT_C.split(LINE_BREAK);
        String expected =
                String.format("%s\t%s\t%s", words[0], words[1], words[2]) + LINE_BREAK +
                        String.format("%s\t%s\t%s", words[3], words[4], words[5]) + LINE_BREAK +
                        String.format("%s\t%s\t", words[6], words[7]);

        assertEquals(expected + LINE_BREAK, getTerminalOutput());
    }
}