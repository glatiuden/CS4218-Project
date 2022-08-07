package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

class GrepApplicationTest {
    private static final String FILE_ONE_NAME = "A.txt";
    private static final String FILE_TWO_NAME = "B.txt";
    private static final String FILE_INVALID_NAME = "C.txt";
    private static final String FILE_UNREADABLE = "unreadable.txt";
    private static final String FOLDER_NAME = "test";
    private static final String FILE_ONE_CONTENT = "I love CS4218";
    private static final String FILE_TWO_CONTENT = "Does CS4218 loves me?";
    private static final String STDIN_CONTENT = "CS4218 Software Testing";
    private static final String RANDOM_CONTENT = "Line 123";
    private static final String VALID_PATTERN_1 = "CS4218";
    private static final String VALID_PATTERN_2 = "cs4218";
    private static final String INVALID_PATTERN = "Test\\";
    private static final String ERROR_PREFIX = "grep: %s";
    private static final String GREP_EXCEP = "grep: %s: %s";
    private static final String LINE_COUNT = ": 1";
    private static final String STDIN_DASH = "-";
    private static final String ERR_CREATE_DIR = "Unable to create directory";
    private static final ByteArrayOutputStream OUT_CAPTURE = new ByteArrayOutputStream();
    @TempDir
    public static Path folderPath;
    private static GrepApplication grepApplication;
    private static File unreadableFile;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeAll
    static void setUpGrepApplication() throws Exception {
        grepApplication = new GrepApplication();
        System.setOut(new PrintStream(OUT_CAPTURE));

        Files.createDirectories(folderPath);
        Environment.setCurrentDirectory(folderPath.toString());
        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Path path2 = folderPath.resolve(FILE_TWO_NAME);
        Path unreadablePath = folderPath.resolve(FILE_UNREADABLE);
        Path folder = folderPath.resolve(FOLDER_NAME);
        Files.writeString(path1, FILE_ONE_CONTENT);
        Files.writeString(path2, FILE_TWO_CONTENT);
        Files.writeString(unreadablePath, RANDOM_CONTENT);

        unreadableFile = unreadablePath.toFile();
        FileUtils.removeFilePermissions(unreadablePath);
        boolean foldersCreated = folder.toFile().mkdir();
        if (!foldersCreated) {
            throw new Exception(ERR_CREATE_DIR);
        }
    }

    @AfterAll
    static void tearDownGrepApplication() {
        FileUtils.deleteFolder(folderPath);
        Environment.resetCurrentDirectory();
    }

    @BeforeEach
    void setUpInputOutputStream() {
        stdin = new ByteArrayInputStream(STDIN_CONTENT.getBytes(StandardCharsets.UTF_8));
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void teardownInputOutputStream() throws IOException {
        stdin.close();
        stdout.flush();
        stdout.close();
        OUT_CAPTURE.reset();
    }

    // Pairwise Testing for grepFromFiles
    @Test
    void grepFromFiles_SingleFileWithNoFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME};
        String expectedOutput = FILE_ONE_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFiles(VALID_PATTERN_1, false, false, false, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFiles_MultipleFilesWithIsCaseInsensitiveAndIsPrefixFileFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, FILE_TWO_NAME};
        String expectedOutput = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFiles(VALID_PATTERN_1, true, false, true, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFiles_MultipleFilesWithIsCaseInsensitiveAndIsCountLinesFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, FILE_TWO_NAME};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFiles(VALID_PATTERN_2, true, true, false, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFiles_MultipleFilesWithIsCountLinesAndIsPrefixFileNameFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, FILE_TWO_NAME};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFiles(VALID_PATTERN_1, false, true, true, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFiles_SingleFileWithIsCaseInsensitiveAndIsPrefixFileNameFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME};
        String expectedOutput = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFiles(VALID_PATTERN_2, true, false, true, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFiles_SingleFileWithIsCaseInsensitiveAndIsCountLinesFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME};
        String expectedOutput = "1" + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFiles(VALID_PATTERN_2, true, true, false, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFiles_SingleFileWithIsCountLinesAndIsPrefixFileNameFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFiles(VALID_PATTERN_1, false, true, true, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    // Negative test cases (error output/exception) from grepFromFiles: Only need to test once
    @Test
    void grepFromFiles_InvalidPattern_ShouldThrowInvalidSyntaxGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFiles(INVALID_PATTERN, false, false, false, FILE_ONE_NAME));
        assertEquals(String.format(ERROR_PREFIX, ERR_SYNTAX), thrown.getMessage());
    }

    @Test
    void grepFromFiles_NullPattern_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFiles(null, false, false, false, FILE_ONE_NAME));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFiles_InvalidFileName_ShouldReturnFileNotFoundErrorMessage() throws Exception {
        String expectedOutput = String.format(GREP_EXCEP, FILE_INVALID_NAME, ERR_FILE_NOT_FOUND);
        grepApplication.grepFromFiles(VALID_PATTERN_1, false, false, false, FILE_INVALID_NAME);
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    @DisabledOnOs({OS.WINDOWS})
    void grepFromFiles_UnreadableFileNames_ShouldReturnNoPermissionErrorMessage() throws Exception {
        Path filePath = FileUtils.getFileRelativePathToCd(unreadableFile);
        String expectedOutput = String.format(GREP_EXCEP, filePath, ERR_NO_PERM);
        grepApplication.grepFromFiles(VALID_PATTERN_1, false, false, false, filePath.toString());
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void grepFromFiles_FolderFileName_ShouldReturnIsDirectoryErrorMessage() throws Exception {
        String expectedOutput = String.format(GREP_EXCEP, FOLDER_NAME, IS_DIRECTORY);
        grepApplication.grepFromFiles(VALID_PATTERN_1, false, false, false, FOLDER_NAME);
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void grepFromFiles_EmptyFileName_ShouldReturnFileNotFoundErrorMessage() throws Exception {
        String expectedOutput = String.format(GREP_EXCEP, " ", ERR_FILE_NOT_FOUND);
        grepApplication.grepFromFiles(VALID_PATTERN_1, false, false, false, " ");
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void grepFromFiles_ZeroFileName_ShouldThrowNullPointerGrepException() {
        assertThrows(NullPointerException.class,
                () -> grepApplication.grepFromFiles(VALID_PATTERN_1, false, false, false, new String[1]));
    }

    @Test
    void grepFromFiles_NullFileName_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFiles(VALID_PATTERN_1, false, false, false, (String[]) null));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFiles_NullIsCaseInsensitive_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFiles(VALID_PATTERN_1, null, false, false, FILE_ONE_NAME));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFiles_NullIsCountLines_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFiles(VALID_PATTERN_1, false, null, false, FILE_ONE_NAME));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFiles_NullIsPrefixFileName_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFiles(VALID_PATTERN_1, false, false, null, FILE_ONE_NAME));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    // Pairwise Testing & MC/DC
    @Test
    void grepFromStdin_AllFlags_ShouldReturnMatchingLines() throws Exception {
        String expectedOutput = STDIN_FILE_PREFIX + "1" + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromStdin(VALID_PATTERN_1, true, true, true, stdin);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromStdin_IsCaseInsensitiveAndIsCountLinesFlags_ShouldReturnMatchingLines() throws Exception {
        String expectedOutput = "1" + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromStdin(VALID_PATTERN_2, true, true, false, stdin);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromStdin_IsCaseInsensitiveAndIsPrefixFileNameFlags_ShouldReturnMatchingLines() throws Exception {
        String expectedOutput = STDIN_FILE_PREFIX + STDIN_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromStdin(VALID_PATTERN_2, true, false, true, stdin);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromStdin_IsCountLinesAndIsPrefixFileNameFlags_ShouldReturnMatchingLines() throws Exception {
        String expectedOutput = STDIN_FILE_PREFIX + "1" + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromStdin(VALID_PATTERN_1, false, true, true, stdin);
        assertEquals(expectedOutput, actualOutput);
    }

    // Negative test cases (error output/exception) from grepFromStdin: Only need to test once
    @Test
    void grepFromStdin_InvalidPattern_ShouldThrowInvalidRegexGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromStdin(INVALID_PATTERN, false, true, true, stdin));
        assertEquals(String.format(ERROR_PREFIX, ERR_INVALID_REGEX), thrown.getMessage());
    }

    @Test
    void grepFromStdin_NullPattern_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromStdin(null, false, true, true, stdin));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromStdin_NullStdin_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromStdin(VALID_PATTERN_1, false, true, true, null));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromStdin_NullIsCaseInsensitive_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromStdin(VALID_PATTERN_1, null, false, false, stdin));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromStdin_NullIsCountLines_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromStdin(VALID_PATTERN_1, false, null, false, stdin));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromStdin_NullIsPrefixFileName_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromStdin(VALID_PATTERN_1, false, true, null, stdin));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    // TC for grepFromFilesAndStdin
    @Test
    void grepFromFileAndStdin_SingleFileWithAllFlagsDashAfter_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, STDIN_DASH};
        String expectedOutput = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + STDIN_FILE_PREFIX + STDIN_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_MultipleFileWithNoFlagsDashBetween_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, STDIN_DASH, FILE_TWO_NAME};
        String expectedOutput = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + STDIN_FILE_PREFIX + STDIN_CONTENT + STRING_NEWLINE
                + FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_MultipleFilesWithIsCaseInsensitiveAndIsPrefixFileFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, FILE_TWO_NAME, STDIN_DASH};
        String expectedOutput = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE + STDIN_FILE_PREFIX + STDIN_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, true, false, true, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_MultipleFilesWithIsCaseInsensitiveAndIsCountLinesFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, STDIN_DASH, FILE_TWO_NAME};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + STDIN_FILE_PREFIX + "1" + STRING_NEWLINE + FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_2, true, true, false, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_MultipleFilesWithIsCountLinesAndIsPrefixFileNameFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, FILE_TWO_NAME, STDIN_DASH};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE + STDIN_FILE_PREFIX + "1" + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, true, true, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_SingleFileWithIsCaseInsensitiveAndIsPrefixFileNameFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {STDIN_DASH, FILE_ONE_NAME};
        String expectedOutput = STDIN_FILE_PREFIX + STDIN_CONTENT + STRING_NEWLINE + FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_2, true, false, true, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_SingleFileWithIsCaseInsensitiveAndIsCountLinesFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, STDIN_DASH};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + STDIN_FILE_PREFIX + "1" + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_2, true, true, false, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_SingleFileWithIsCountLinesAndIsPrefixFileNameFlags_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {STDIN_DASH, FILE_ONE_NAME};
        String expectedOutput = STDIN_FILE_PREFIX + "1" + STRING_NEWLINE + FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, true, true, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    // Single / Negative test cases (error output/exception) from grepFromFiles: Only need to test once
    @Test
    void grepFromFileAndStdin_MultipleDash_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, STDIN_DASH, FILE_TWO_NAME, STDIN_DASH};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + STDIN_FILE_PREFIX + "1" + STRING_NEWLINE + FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, true, true, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_NoDash_ShouldReturnMatchingLines() throws Exception {
        String[] fileNames = {FILE_ONE_NAME, FILE_TWO_NAME};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, true, true, stdin, fileNames);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void grepFromFileAndStdin_DashStdin_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, null, FILE_ONE_NAME, STDIN_DASH));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFileAndStdin_InvalidFileName_ShouldReturnFileNotFoundErrorMessage() throws Exception {
        String expectedOutput = String.format(GREP_EXCEP, FILE_INVALID_NAME, ERR_FILE_NOT_FOUND);
        grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, stdin, FILE_INVALID_NAME);
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    @DisabledOnOs({OS.WINDOWS})
    void grepFromFileAndStdin_UnreadableFileNames_ShouldReturnNoPermissionErrorMessage() throws Exception {
        Path filePath = FileUtils.getFileRelativePathToCd(unreadableFile);
        String expectedOutput = String.format(GREP_EXCEP, filePath, ERR_NO_PERM);
        grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, stdin, filePath.toString());
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void grepFromFileAndStdin_FolderFileName_ShouldReturnIsDirectoryErrorMessage() throws Exception {
        String expectedOutput = String.format(GREP_EXCEP, FOLDER_NAME, IS_DIRECTORY);
        grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, stdin, FOLDER_NAME);
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void grepFromFileAndStdin_EmptyFileName_ShouldReturnFileNotFoundErrorMessage() throws Exception {
        String expectedOutput = String.format(GREP_EXCEP, " ", ERR_FILE_NOT_FOUND);
        grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, stdin, " ");
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void grepFromFileAndStdin_InvalidPattern_ShouldThrowInvalidSyntaxGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin(INVALID_PATTERN, false, false, false, stdin, FILE_ONE_NAME));
        assertEquals(String.format(ERROR_PREFIX, ERR_SYNTAX), thrown.getMessage());
    }

    @Test
    void grepFromFileAndStdin_NullPattern_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin(null, false, false, false, stdin, FILE_ONE_NAME));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFileAndStdin_NullFileNames_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, stdin, (String[]) null));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFileAndStdin_NullIsCaseInsensitive_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, null, false, false, stdin, FILE_ONE_NAME));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFileAndStdin_NullIsCountLines_ShouldThrowNullPointerGrepException() {
        Throwable thrown = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, null, false, stdin, FILE_ONE_NAME, FILE_TWO_NAME));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void grepFromFileAndStdin_NullStdin_ShouldReturnMatchingLinesFromFiles() throws Exception {
        String expectedOutput = FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE;
        String actualOutput = grepApplication.grepFromFileAndStdin(VALID_PATTERN_1, false, false, false, null, FILE_TWO_NAME);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void run_SingleFileWithAllFlagsAndStdinDashAfter_ShouldReturnMatchingLines() throws Exception {
        String[] args = {"-i", "-c", "-H", VALID_PATTERN_1, FILE_ONE_NAME, STDIN_DASH};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + STDIN_FILE_PREFIX + "1" + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_MultipleFilesWithNoFlags_ShouldReturnMatchingLines() throws Exception {
        String[] args = {VALID_PATTERN_1, FILE_ONE_NAME, FILE_TWO_NAME};
        String expectedOutput = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_MultipleFilesWithIsCaseInsensitiveAndIsPrefixFileNameFlagsDashAfter_ShouldReturnMatchingLines() throws Exception {
        String[] args = {"-i", "-H", VALID_PATTERN_1, FILE_ONE_NAME, FILE_TWO_NAME};
        String expectedOutput = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_SingleFileWithIsCountLinesFlagsAndStdinDashAfter_ShouldReturnMatchingLines() throws Exception {
        String[] args = {"-c", VALID_PATTERN_1, STDIN_DASH, FILE_TWO_NAME};
        String expectedOutput = STDIN_FILE_PREFIX + "1" + STRING_NEWLINE + FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_SingeFileWithIsPrefixFileNameFlagsAndStdinDashBefore_ShouldReturnMatchingLines() throws Exception {
        String[] args = {"-H", VALID_PATTERN_1, STDIN_DASH, FILE_ONE_NAME};
        String expectedOutput = STDIN_FILE_PREFIX + STDIN_CONTENT + STRING_NEWLINE + FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_MultipleFilesWithIsCaseInsensitiveAndIsCountLinesFlagsDashBetween_ShouldReturnMatchingLines() throws Exception {
        String[] args = {"-i", "-c", VALID_PATTERN_1, FILE_TWO_NAME, STDIN_DASH, FILE_ONE_NAME};
        String expectedOutput = FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE + STDIN_FILE_PREFIX + "1" + STRING_NEWLINE + FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_SingleFileWithIsCaseInsensitiveAndIsCountLinesFlags_ShouldReturnMatchingLines() throws Exception {
        String[] args = {"-c", VALID_PATTERN_1, FILE_ONE_NAME};
        String expectedOutput = "1" + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_MultipleFileWithIsCountLinesAndIsPrefixFileNameDashAfter_ShouldReturnMatchingLines() throws Exception {
        String[] args = {"-c", "-H", VALID_PATTERN_1, FILE_ONE_NAME, FILE_TWO_NAME, STDIN_DASH};
        String expectedOutput = FILE_ONE_NAME + LINE_COUNT + STRING_NEWLINE + FILE_TWO_NAME + LINE_COUNT + STRING_NEWLINE + STDIN_FILE_PREFIX + "1" + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    // Negative test cases (error output/exception) for run: Only need to test once
    @Test
    void run_MultipleDash_ShouldIgnoreSubsequentDash() throws Exception {
        String[] args = {VALID_PATTERN_1, FILE_ONE_NAME, STDIN_DASH, STDIN_DASH};
        String expectedOutput = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + STDIN_FILE_PREFIX + STDIN_CONTENT + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_NullStdinWithoutDash_ShouldReturnMatchingLines() throws Exception {
        String[] args = {VALID_PATTERN_1, FILE_ONE_NAME};
        String expectedOutput = FILE_ONE_CONTENT + STRING_NEWLINE;
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, stdout.toString());
    }

    @Test
    void run_DashStdin_ShouldThrowNullPointerGrepException() {
        String[] args = {VALID_PATTERN_1, STDIN_DASH};
        Throwable thrown = assertThrows(GrepException.class, () -> grepApplication.run(args, null, stdout));
        assertEquals(String.format(ERROR_PREFIX, NULL_POINTER), thrown.getMessage());
    }

    @Test
    void run_InvalidPattern_ShouldThrowInvalidSyntaxGrepException() {
        String[] args = {INVALID_PATTERN, FILE_ONE_NAME};
        Throwable thrown = assertThrows(GrepException.class, () -> grepApplication.run(args, stdin, stdout));
        assertEquals(String.format(ERROR_PREFIX, ERR_SYNTAX), thrown.getMessage());
    }

    @Test
    void run_InvalidFileName_ShouldReturnFileNotFoundErrorMessage() throws Exception {
        String[] args = {VALID_PATTERN_1, FILE_INVALID_NAME};
        String expectedOutput = String.format(GREP_EXCEP, FILE_INVALID_NAME, ERR_FILE_NOT_FOUND);
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    @DisabledOnOs({OS.WINDOWS})
    void run_UnreadableFileNames_ShouldReturnNoPermissionErrorMessage() throws Exception {
        Path filePath = FileUtils.getFileRelativePathToCd(unreadableFile);
        String[] args = {VALID_PATTERN_1, filePath.toString()};
        String expectedOutput = String.format(GREP_EXCEP, filePath, ERR_NO_PERM);
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void run_FolderFileNames_ShouldReturnIsDirectoryErrorMessage() throws Exception {
        String[] args = {VALID_PATTERN_1, FOLDER_NAME};
        String expectedOutput = String.format(GREP_EXCEP, FOLDER_NAME, IS_DIRECTORY);
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void run_EmptyFileNames_ShouldReturnFileNotFoundErrorMessage() throws Exception {
        String[] args = {VALID_PATTERN_1, " "};
        String expectedOutput = String.format(GREP_EXCEP, " ", ERR_FILE_NOT_FOUND);
        grepApplication.run(args, stdin, stdout);
        assertEquals(expectedOutput, OUT_CAPTURE.toString().trim());
    }

    @Test
    void run_NullStdout_ShouldThrowNullPointerGrepException() {
        String[] args = {VALID_PATTERN_1, FILE_ONE_NAME};
        assertThrows(GrepException.class, () -> grepApplication.run(args, stdin, null));
    }

    @Test
    void run_NullPattern_ShouldThrowNullPointerGrepException() {
        String[] args = {null, FILE_ONE_NAME};
        assertThrows(GrepException.class, () -> grepApplication.run(args, stdin, stdout));
    }

    @Test
    void run_NullFileNames_ShouldThrowNullPointerGrepException() {
        String[] args = {VALID_PATTERN_1, null};
        assertThrows(GrepException.class, () -> grepApplication.run(args, stdin, stdout));
    }

    @Test
    void run_InvalidFlag_ShouldThrowInvalidSyntaxGrepException() {
        String[] args = {"-g", VALID_PATTERN_1, FILE_ONE_NAME};
        Throwable thrown = assertThrows(GrepException.class, () -> grepApplication.run(args, stdin, stdout));
        assertEquals(String.format(ERROR_PREFIX, ERR_SYNTAX), thrown.getMessage());
    }
}
