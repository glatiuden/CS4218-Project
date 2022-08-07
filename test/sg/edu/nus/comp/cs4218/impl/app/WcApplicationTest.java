package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

@SuppressWarnings("PMD.CloseResource") // Already closed but still showing
public class WcApplicationTest {

    /* TEST FOLDER STRUCTURE:
        test-wc/
        ├─ deep-folder/
        │  ├─ second-folder/
        │  ├─ test2.txt
        ├─ test.txt
    */

    static final PrintStream ORIGINAL_OUT = System.out;
    static final String INVALID_FILE = "test-wc/invalid.txt";
    static final String DIFF_EXT_FILE = "test-wc/test.java"; // Doesn't exist
    static final String FILE1_STRING = "This is a test file that can be used during unit, integration or system testing\n " +
            "This is the 2nd line";
    static final String FILE2_STRING = "This is the 2nd test file that can be used for testing\n" +
            "2nd line\n" + "3rd line\n" + "so on";
    static final String TOTAL = "total";
    static final String MULTILINE_STDIN = "Hi this is a line of data.\n 2 \n 3 \n";
    static final String WC_PREFIX = "wc: ";
    // Test Files
    static File rootTestFolder = new File("test-wc");
    static File[] dirs = new File[]{
            new File(Paths.get(rootTestFolder.toString(), "deep-folder").toUri()),
            new File(Paths.get(rootTestFolder.toString(), "deep-folder", "second-folder").toUri()),
    };
    static File[] files = new File[]{
            new File("test-wc/test.txt"),
            new File(Paths.get(dirs[0].getPath(), "test2.txt").toUri()),
    };
    static final String FILENAME = files[0].toString();
    static final String FILENAME2 = files[1].toString();
    final String DIRECTORY = dirs[0].getPath();
    // CONSTANTS or VARIABLE
    ByteArrayOutputStream stdOutResult = new ByteArrayOutputStream();
    WcApplication wcApp = new WcApplication();

    @BeforeAll
    static void setupAll() throws Exception {
        createAllFileNFolder(rootTestFolder, dirs, files);
        writeToFile(FILENAME, FILE1_STRING);
        writeToFile(FILENAME2, FILE2_STRING);
    }

    @AfterAll
    static void tearDown() {
        deleteAll(rootTestFolder);
        System.setOut(ORIGINAL_OUT);
    }

    @BeforeEach
    public void setup() {
        stdOutResult = new ByteArrayOutputStream();
        wcApp.setStdOut(stdOutResult);
        System.setOut(new PrintStream(stdOutResult));
    }

    // Done: Test case for countFromFiles()
    // isBytes (T/F), isLines (T/F), isWords (T/F), files (1, Many), done using pairwise testing
    @Test
    public void countFromFiles_TFFManyFile_ReturnsTrue() throws Exception {
        String result = wcApp.countFromFiles(true, false, false, FILENAME, FILENAME2);

        String expectedAns = expectedFileLine(101, null, null, FILENAME)
                + STRING_NEWLINE + expectedFileLine(78, null, null, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(179, null, null, TOTAL)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_FFT1File_ReturnsTrue() throws Exception {
        String result = wcApp.countFromFiles(false, false, true, FILENAME);

        String expectedAns = expectedFileLine(null, null, 20, FILENAME) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_FTTManyFile_ReturnsTrue() throws Exception {
        String result = wcApp.countFromFiles(false, true, true, FILENAME2, FILENAME);

        String expectedAns = expectedFileLine(null, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(null, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(null, 4, 38, TOTAL)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_FTF1File_ReturnsTrue() throws Exception {
        String result = wcApp.countFromFiles(false, true, false, FILENAME);

        String expectedAns = expectedFileLine(null, 1, null, FILENAME) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_TTF1File_ReturnsTrue() throws Exception {
        String result = wcApp.countFromFiles(true, true, false, FILENAME);

        String expectedAns = expectedFileLine(101, 1, null, FILENAME) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_AllTrueManyFile_ReturnsTrue() throws Exception {
        String result = wcApp.countFromFiles(true, true, true, FILENAME, FILENAME2);

        String expectedAns = expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(78, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(179, 4, 38, TOTAL)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_TFT1File_ReturnsTrue() throws Exception {
        String result = wcApp.countFromFiles(true, false, true, FILENAME2);

        String expectedAns = expectedFileLine(78, null, 18, FILENAME2) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_WrongExtension_ReturnsFalse() throws Exception {
        String result = wcApp.countFromFiles(true, false, true, DIFF_EXT_FILE);

        String expectedAns = WC_PREFIX + DIFF_EXT_FILE + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_Directory_ReturnsFalse() throws Exception {
        String result = wcApp.countFromFiles(true, false, true, DIRECTORY);

        String expectedAns = WC_PREFIX + DIRECTORY + ": " + ERR_IS_DIR
                + STRING_NEWLINE + expectedFileLine(0, null, 0, DIRECTORY) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_InvalidFile_ReturnsFalse() throws Exception {
        String result = wcApp.countFromFiles(true, true, true, INVALID_FILE);

        String expectedAns = WC_PREFIX + INVALID_FILE + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_InvalidFileWValid_ReturnsFalse() throws Exception {
        String result = wcApp.countFromFiles(true, true, true, INVALID_FILE, FILENAME2);

        String expectedAns = WC_PREFIX + INVALID_FILE + ": " + ERR_FILE_NOT_FOUND
                + STRING_NEWLINE + expectedFileLine(78, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(78, 3, 18, TOTAL) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFiles_DirectoryWValid_ReturnsFalse() throws Exception {
        String result = wcApp.countFromFiles(true, true, true, FILENAME2, DIRECTORY);

        String expectedAns = expectedFileLine(78, 3, 18, FILENAME2)
                + STRING_NEWLINE + WC_PREFIX + DIRECTORY + ": " + ERR_IS_DIR
                + STRING_NEWLINE + expectedFileLine(0, 0, 0, DIRECTORY)
                + STRING_NEWLINE + expectedFileLine(78, 3, 18, TOTAL) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void getCountReport_nullFile_ThrowsException() {
        assertThrows(WcException.class, () -> wcApp.countFromFiles(true, true, true, null));
    }

    // Done: Test case for countFromStdin()
    // From countFromFiles() test cases, we already test the boolean values extensively using pairwise testing. Because the
    // implementation for this is similar, we will focus on the stdIn part instead with some random variation of boolean
    // StdIn (Blank, 1 line, Multiple line)
    @Test
    public void countFromStdin_EmptyStdIn_ReturnsTrue() throws Exception {
        InputStream inputStreamStdIn = new ByteArrayInputStream("".getBytes());
        String result = wcApp.countFromStdin(false, true, true, inputStreamStdIn);

        String expectedAns = expectedFileLine(null, 0, 0, null);

        assertEquals(expectedAns, result);
    }

    @Test
    public void countFromStdin_1LineStdIn_ReturnsTrue() throws Exception {
        InputStream inputStreamStdIn = new ByteArrayInputStream("Hi this is a line of data.".getBytes());
        String result = wcApp.countFromStdin(true, true, false, inputStreamStdIn);

        String expectedAns = expectedFileLine(26, 0, null, null);

        assertEquals(expectedAns, result);
    }

    @Test
    public void countFromStdin_MultiLineStdIn_ReturnsTrue() throws Exception {
        InputStream inputStreamStdIn = new ByteArrayInputStream(MULTILINE_STDIN.getBytes());
        String result = wcApp.countFromStdin(true, true, true, inputStreamStdIn);

        String expectedAns = expectedFileLine(35, 3, 9, null);

        assertEquals(expectedAns, result);
    }

    @Test
    public void countFromStdin_nullFile_ThrowsException() {
        assertThrows(WcException.class, () -> wcApp.countFromStdin(false, true, true, null));
    }

    // Done: Test case for countFromFileAndStdin()
    // Assume that boolean is well tested in countFromFiles() test case, focus on different file and stdIn combination
    // StdIn (1, Many) File (1, Many) = 4 basic test cases, also included other edge cases at the end
    @Test
    public void countFromFileAndStdin_1File1StdIn_ReturnsTrue() throws Exception {
        InputStream inputStreamStdIn = new ByteArrayInputStream("Hi this is a line of data.".getBytes());
        String result = wcApp.countFromFileAndStdin(true, true, true, inputStreamStdIn, FILENAME2, "-");

        String expectedAns = expectedFileLine(78, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(26, 0, 7, "-")
                + STRING_NEWLINE + expectedFileLine(104, 3, 25, TOTAL);

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFileAndStdin_ManyFile1StdIn_ReturnsTrue() throws Exception {
        InputStream inputStreamStdIn = new ByteArrayInputStream("Hi this is a line of data.".getBytes());
        String result = wcApp.countFromFileAndStdin(true, true, true, inputStreamStdIn, FILENAME2, "-", FILENAME);

        String expectedAns = expectedFileLine(78, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(26, 0, 7, "-")
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(205, 4, 45, "total");

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFileAndStdin_ManyFileMultiLineStdIn_ReturnsTrue() throws Exception {
        InputStream inputStreamStdIn = new ByteArrayInputStream(MULTILINE_STDIN.getBytes());
        String result = wcApp.countFromFileAndStdin(true, true, true, inputStreamStdIn, "-", FILENAME, FILENAME2);

        String expectedAns = expectedFileLine(35, 3, 9, "-")
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(78, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(214, 7, 47, TOTAL);

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFileAndStdin_1FileMultiLineStdIn_ReturnsTrue() throws Exception {
        InputStream inputStreamStdIn = new ByteArrayInputStream(MULTILINE_STDIN.getBytes());
        String result = wcApp.countFromFileAndStdin(true, true, true, inputStreamStdIn, "-", FILENAME);

        String expectedAns = expectedFileLine(35, 3, 9, "-")
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(136, 4, 29, TOTAL);

        assertEquals(expectedAns, stdOutResult + result);
    }

    @Test
    public void countFromFileAndStdin_nullFile_ThrowsException() {
        InputStream inputStreamStdIn = new ByteArrayInputStream(MULTILINE_STDIN.getBytes());
        assertThrows(WcException.class, () -> wcApp.countFromFileAndStdin(true, true, true, inputStreamStdIn, null));
    }

    // Done: Test case for getCountReport(input)
    // Input Stream for either file / stdin string
    @Test
    public void getCountReport_stdinAndFile_ReturnsTrue() throws Exception {
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE1_STRING.getBytes());
        InputStream inputStreamFile = IOUtils.openInputStream(FILENAME);
        long[] stdInResult = wcApp.getCountReport(inputStreamStdIn);
        long[] fileResult = wcApp.getCountReport(inputStreamFile);
        long[] expectedAnsArray = {1, 20, 101};
        String expectedAns = Arrays.toString(expectedAnsArray);

        assertEquals(expectedAns, Arrays.toString(stdInResult));
        assertEquals(expectedAns, Arrays.toString(fileResult));
        IOUtils.closeInputStream(inputStreamFile);
    }

    @Test
    public void getCountReport_nullInput_ThrowsException() {
        assertThrows(WcException.class, () -> wcApp.getCountReport(null));
    }

    // Run starts here
    // Args (Empty, 1 file, Files, Files with options, errorFile, correct + error File), stdIn(Empty, has value)
    // Combination = 6 X 2 = 12 Test cases
    @Test
    public void run_allEmpty_ReturnsTrue() throws Exception {
        String[] args = {};
        InputStream inputStreamStdIn = new ByteArrayInputStream("".getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(0, 0, 0, null) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_onlyStdIn_ReturnsTrue() throws Exception {
        String[] args = {};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE2_STRING.getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(78, 3, 18, null) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_onlyFile_ReturnsTrue() throws Exception {
        String fullFileName = Environment.currentDirectory + File.separator + FILENAME;
        String[] args = {fullFileName};
        InputStream inputStreamStdIn = new ByteArrayInputStream("".getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(101, 1, 20, fullFileName) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_fileAndStdIn_ReturnsTrue() throws Exception {
        String[] args = {FILENAME, "-"};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE1_STRING.getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, "-")
                + STRING_NEWLINE + expectedFileLine(202, 2, 40, TOTAL) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_filesOnly_ReturnsTrue() throws Exception {
        String[] args = {FILENAME, FILENAME2, FILENAME};
        InputStream inputStreamStdIn = new ByteArrayInputStream("".getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(78, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(280, 5, 58, TOTAL) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_filesAndStdIn_ReturnsTrue() throws Exception {
        String[] args = {FILENAME, FILENAME2, "-", FILENAME};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE2_STRING.getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(78, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(78, 3, 18, "-")
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(358, 8, 76, TOTAL) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_filesWOptions_ReturnsTrue() throws Exception {
        String[] args = {"-c", "-l", FILENAME, FILENAME2, FILENAME};
        InputStream inputStreamStdIn = new ByteArrayInputStream("".getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(101, 1, null, FILENAME)
                + STRING_NEWLINE + expectedFileLine(78, 3, null, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(101, 1, null, FILENAME)
                + STRING_NEWLINE + expectedFileLine(280, 5, null, TOTAL) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_filesWOptionsNStdIn_ReturnsTrue() throws Exception {
        String[] args = {"-wl", FILENAME, FILENAME2, "-", FILENAME};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE2_STRING.getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(null, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(null, 3, 18, FILENAME2)
                + STRING_NEWLINE + expectedFileLine(null, 3, 18, "-")
                + STRING_NEWLINE + expectedFileLine(null, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(null, 8, 76, TOTAL) + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_errorFile_ReturnsTrue() throws Exception {
        String[] args = {"-c", "-l", INVALID_FILE};
        InputStream inputStreamStdIn = new ByteArrayInputStream("".getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = WC_PREFIX + INVALID_FILE + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_errorFileWStdIn_ReturnsTrue() throws Exception {
        String[] args = {INVALID_FILE, "-"};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE1_STRING.getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = WC_PREFIX + INVALID_FILE + ": " + ERR_FILE_NOT_FOUND
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, "-")
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, TOTAL)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_errorNValidFile_ReturnsTrue() throws Exception {
        String[] args = {"-c", "-lw", INVALID_FILE, FILENAME};
        InputStream inputStreamStdIn = new ByteArrayInputStream("".getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = WC_PREFIX + INVALID_FILE + ": " + ERR_FILE_NOT_FOUND
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, TOTAL)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_errorNValidFileNStdIn_ReturnsTrue() throws Exception {
        String[] args = {"-c", "-lw", "-", INVALID_FILE, FILENAME};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE2_STRING.getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(78, 3, 18, "-")
                + STRING_NEWLINE + WC_PREFIX + INVALID_FILE + ": " + ERR_FILE_NOT_FOUND
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + expectedFileLine(179, 4, 38, TOTAL)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    // Include edge cases for Run
    @Test
    public void run_StdInWODash_IgnoreStdIn() throws Exception {
        String[] args = {INVALID_FILE};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE1_STRING.getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = WC_PREFIX + INVALID_FILE + ": " + ERR_FILE_NOT_FOUND
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_optionAtTheEnd_ReturnErr() throws Exception {
        String[] args = {FILENAME, "-wlc"};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE1_STRING.getBytes());
        wcApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = expectedFileLine(101, 1, 20, FILENAME)
                + STRING_NEWLINE + WC_PREFIX + "-wlc" + ": " + ERR_FILE_NOT_FOUND
                + STRING_NEWLINE + expectedFileLine(101, 1, 20, TOTAL)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_nullInput_ThrowsException() {
        String[] args = {"-c", "-lw", "-", null, FILENAME};
        InputStream inputStreamStdIn = new ByteArrayInputStream(FILE2_STRING.getBytes());
        assertThrows(Exception.class, () -> wcApp.run(args, inputStreamStdIn, stdOutResult));
    }

}
