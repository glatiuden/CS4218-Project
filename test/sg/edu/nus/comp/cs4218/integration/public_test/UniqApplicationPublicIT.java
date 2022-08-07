package sg.edu.nus.comp.cs4218.integration.public_test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD") // Not required to check for given test from the prof
public class UniqApplicationPublicIT {
    private static final String INPUT_FILE_TXT = "input_file.txt";
    private static final String OUTPUT_FILE_TXT = "output_file.txt";
    private static final String testInput = "Hello World" + STRING_NEWLINE +
            "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE;
    private static final String withoutFlagOutput = "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE;
    private static final String withCountFlagOutput = "\t2 Hello World" + STRING_NEWLINE +
            "\t2 Alice" + STRING_NEWLINE +
            "\t1 Bob" + STRING_NEWLINE +
            "\t1 Alice" + STRING_NEWLINE +
            "\t1 Bob" + STRING_NEWLINE;
    private static final String withDuplicateFlagOutput = "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE;
    private static final String withAllDuplicateFlagOutput = "Hello World" + STRING_NEWLINE +
            "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE;
    private static final String withCountAndDuplicateFlagsOutput = "\t2 Hello World" + STRING_NEWLINE +
            "\t2 Alice" + STRING_NEWLINE;
    private static final String withCountAndAllDuplicateFlagsOutput = "\t2 Hello World" + STRING_NEWLINE +
            "\t2 Hello World" + STRING_NEWLINE +
            "\t2 Alice" + STRING_NEWLINE +
            "\t2 Alice" + STRING_NEWLINE;
    private static Deque<Path> files = new ArrayDeque<>();
    private static Path CURR_PATH;
    private UniqApplication uniqApplication;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        CURR_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory());
    }

    @BeforeEach
    void init() {
        uniqApplication = new UniqApplication();
    }

    @AfterEach
    void deleteTemp() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
    }

    private Path createFile(String name) throws IOException {
        Path path = CURR_PATH.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    @Test
    void run_NoFilesWithoutFlag_ReadsFromInputAndDisplaysAdjacentLines() {
        String[] args = {};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals(withoutFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithCountFlag_ReadsFromInputAndDisplaysCountOfAdjacentLines() {
        String[] args = {"-c"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals(withCountFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesOnlyOnce() {
        String[] args = {"-d"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals(withDuplicateFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithAllDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() {
        String[] args = {"-D"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals(withAllDuplicateFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithDuplicateAndAllDuplicateFlags_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() {
        String[] args = {"-d", "-D"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals(withAllDuplicateFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithCountAndDuplicateFlags_ReadsFromInputAndDisplaysCountOfRepeatedAdjacentLinesOnlyOnce() {
        String[] args = {"-c", "-d"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals(withCountAndDuplicateFlagsOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithUnknownFlag_Throws() {
        String[] args = {"-x"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_NonemptyInputFile_ReadsFileAndDisplaysAdjacentLines() throws IOException {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, testInput);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals(withoutFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_EmptyInputFile_ReadsFileAndEmptyDisplay() throws IOException {
        createFile(INPUT_FILE_TXT);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals("", outputStream.toString());
        });
    }

    @Test
    void run_NonexistentInputFile_Throws() {
        String[] args = {"nonexistent_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_InputFileToOutputFile_DisplaysNewlineAndOverwritesOutputFile() throws IOException {
        Path inputPath = createFile(INPUT_FILE_TXT);
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(inputPath, testInput);
        writeToFile(outputPath, "This is the output file.");
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals("", outputStream.toString());
            assertArrayEquals(withoutFlagOutput.getBytes(), Files.readAllBytes(outputPath));
        });
    }

    @Test
    void run_InputFileToNonexistentOutputFile_DisplaysNewlineAndCreatesOutputFile() throws IOException {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, testInput);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            uniqApplication.run(args, stdin, outputStream);
            assertEquals("", outputStream.toString());
            Path outputPath = CURR_PATH.resolve(OUTPUT_FILE_TXT);
            assertTrue(Files.exists(outputPath));
            assertArrayEquals(withoutFlagOutput.getBytes(), Files.readAllBytes(outputPath));
            Files.deleteIfExists(outputPath);
        });
    }

    @Test
    void run_NonexistentInputFileToOutputFile_Throws() throws IOException {
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(outputPath, "This is the output file.");
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_NonexistentInputFileToNonexistentOutputFile_Throws() {
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> uniqApplication.run(args, stdin, outputStream));
    }
}
