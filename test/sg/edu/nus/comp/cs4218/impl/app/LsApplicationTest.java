package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createAllFileNFolder;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteAll;

public class LsApplicationTest {

    static final PrintStream ORIGINAL_OUT = System.out;
    static final String FILENAME = "test.txt";
    static final String FILENAME2 = "test2.txt";
    static final String FILENAME3 = "test3.a";
    static final String FILENAME4 = "test4.txt";
    static final String INVALID_FILE = "test-ls\\invalid.txt";
    static final String DIFF_EXT_FILE = "test-ls\\test.java"; // Doesn't exist
    static final String FOLDER_NAME = "deep-folder";
    static final String SEC_FOLDER = "second-folder";
    static final String HIDDEN_FOLDER = "hidden-folder";
    static final String FOLDER_STR = "deep-folder:" + System.lineSeparator() +
            SEC_FOLDER + STRING_NEWLINE +
            FILENAME2 + STRING_NEWLINE +
            FILENAME3 + STRING_NEWLINE;
    static final String SORT_FOLDER_STR = "deep-folder:" + System.lineSeparator() +
            SEC_FOLDER + STRING_NEWLINE +
            FILENAME3 + STRING_NEWLINE +
            FILENAME2 + STRING_NEWLINE;
    static final String SORT_FOLDER_SOLO = "second-folder" + STRING_NEWLINE +
            FILENAME3 + STRING_NEWLINE +
            FILENAME2 + STRING_NEWLINE;
    static final String DEEP_FOLD_EXP = "deep-folder" + File.separator + "second-folder:";
    // Test Files
    static File rootTestFolder = new File("test-ls");
    static File[] dirs = new File[]{
            new File(Paths.get(rootTestFolder.toString(), FOLDER_NAME).toUri()),
            new File(Paths.get(rootTestFolder.toString(), FOLDER_NAME, SEC_FOLDER).toUri()),
            new File(Paths.get(rootTestFolder.toString(), HIDDEN_FOLDER).toUri()),
    };
    static File[] files = new File[]{
            new File("test-ls/test.txt"),
            new File(Paths.get(dirs[0].getPath(), FILENAME2).toUri()),
            new File(Paths.get(dirs[0].getPath(), FILENAME3).toUri()),
            new File(Paths.get(dirs[2].getPath(), FILENAME4).toUri()),
    };
    // VARIABLES AND CONSTANTS
    LsApplication lsApp;
    InputStream inputStreamStdIn = mock(ByteArrayInputStream.class);

    /* TEST FOLDER STRUCTURE:
        test-ls/
        ├─ deep-folder/
        │  ├─ second-folder/
        │  ├─ test2.txt
        │  ├─ test3.a
        ├─ hidden-folder/
        │  ├─ test4.txt
        ├─ test.txt
    */
    ByteArrayOutputStream stdOutResult;
    ByteArrayOutputStream outContent;
    LsArgsParser lsArgsParser = mock(LsArgsParser.class);

    @BeforeAll
    static void setupAll() throws Exception {
        createAllFileNFolder(rootTestFolder, dirs, files);
        Files.setAttribute(dirs[2].toPath(), "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
        Environment.currentDirectory = rootTestFolder.getAbsolutePath();
    }

    @AfterAll
    static void tearDown() {
        deleteAll(rootTestFolder);
        Environment.resetCurrentDirectory();
        System.setOut(ORIGINAL_OUT);
    }

    @BeforeEach
    public void setup() {
        lsApp = new LsApplication();
        stdOutResult = new ByteArrayOutputStream();
        lsApp.setParser(lsArgsParser);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    private void setUpMock(Boolean isRecursive, Boolean isSort, String[] dir, Boolean illegal) throws InvalidArgsException {
        if (illegal) {
            doThrow(new InvalidArgsException(ERR_INVALID_FLAG)).when(lsArgsParser).parse(any());
        } else {
            doNothing().when(lsArgsParser).parse();
        }
        when(lsArgsParser.isRecursive()).thenReturn(isRecursive);
        when(lsArgsParser.isSortByExt()).thenReturn(isSort);
        when(lsArgsParser.getDirectories()).thenReturn(List.of(dir));
    }

    private String cannotAccessMsg(String filePath) {
        return "ls: cannot access '" + filePath + "': " + ERR_FILE_NOT_FOUND + "";
    }

    // Test case for listFolderContent()
    // Ignores isFolder because I have confirmed from the TA that it is not required nor inside the requirement folder
    // Done using pairwise testing to get 8 test cases
    // Recursive (T/F), SortByExt(T/F), Files (Empty, File, Folder + File, Folder)
    @Test
    public void listFolderContent_TTEmptyFile_ReturnsTrue() throws Exception {
        String expectedAns = "." + File.separator + ":" + System.lineSeparator() +
                FOLDER_NAME + STRING_NEWLINE +
                FILENAME + STRING_NEWLINE +
                STRING_NEWLINE +
                SORT_FOLDER_STR +
                STRING_NEWLINE +
                DEEP_FOLD_EXP;
        String result = lsApp.listFolderContent(true, true);

        assertEquals(expectedAns, result);
    }

    @Test
    public void listFolderContent_TFFile_ReturnsTrue() throws Exception {
        String result = lsApp.listFolderContent(true, false, FILENAME);

        assertEquals(FILENAME, result);
    }

    @Test
    public void listFolderContent_TTFileNFolder_ReturnsTrue() throws Exception {
        String expectedAns = FILENAME + STRING_NEWLINE +
                STRING_NEWLINE +
                SORT_FOLDER_STR +
                STRING_NEWLINE +
                DEEP_FOLD_EXP;
        String result = lsApp.listFolderContent(true, true, FILENAME, FOLDER_NAME);

        assertEquals(expectedAns, result);
    }

    @Test
    public void listFolderContent_TFFolder_ReturnsTrue() throws Exception {
        String expectedAns = FOLDER_STR +
                STRING_NEWLINE +
                DEEP_FOLD_EXP;
        String result = lsApp.listFolderContent(true, false, FOLDER_NAME);

        assertEquals(expectedAns, result);
    }

    @Test
    public void listFolderContent_FFFileNFolder_ReturnsTrue() throws Exception {
        String expectedAns = FILENAME + STRING_NEWLINE +
                STRING_NEWLINE +
                FOLDER_STR.trim();
        String result = lsApp.listFolderContent(false, false, FILENAME, FOLDER_NAME);

        assertEquals(expectedAns, result);
    }

    @Test
    public void listFolderContent_FTFolder_ReturnsTrue() throws Exception {
        String expectedAns = SORT_FOLDER_STR.trim();
        String result = lsApp.listFolderContent(false, true, FOLDER_NAME);

        assertEquals(expectedAns, result);
    }

    @Test
    public void listFolderContent_FFEmpty_ReturnsTrue() throws Exception {
        String expectedAns = FOLDER_NAME + STRING_NEWLINE +
                FILENAME;
        String result = lsApp.listFolderContent(false, false);

        assertEquals(expectedAns, result);
    }

    @Test
    public void listFolderContent_FTFile_ReturnsTrue() throws Exception {
        String result = lsApp.listFolderContent(false, true, FILENAME);

        assertEquals(FILENAME, result);
    }

    // Fix for bug 16
    @Test
    public void listFolderContent_HiddenFolder_ReturnsTrue() throws Exception {
        String expectedAns = HIDDEN_FOLDER + ":" + STRING_NEWLINE + FILENAME4;
        String result = lsApp.listFolderContent(false, false, HIDDEN_FOLDER);

        assertEquals(expectedAns, result);
    }

    // Negative test case
    @Test
    public void listFolderContent_hasInvalidFile_RunSuccess() throws Exception {
        String expectedAns = cannotAccessMsg(INVALID_FILE) + STRING_NEWLINE +
                FILENAME;

        String result = lsApp.listFolderContent(false, false, FILENAME, INVALID_FILE);

        assertEquals(expectedAns, outContent + result);
    }

    @Test
    public void listFolderContent_wrongExtension_ErrorMsg() throws Exception {
        String result = lsApp.listFolderContent(false, false, DIFF_EXT_FILE);

        assertEquals(cannotAccessMsg(DIFF_EXT_FILE), outContent + result);
    }

    @Test
    public void listFolderContent_multiInvalid_RunSuccess() throws Exception {
        String expectedAns = cannotAccessMsg(DIFF_EXT_FILE) + STRING_NEWLINE +
                cannotAccessMsg(INVALID_FILE) + STRING_NEWLINE +
                FOLDER_STR + STRING_NEWLINE +
                DEEP_FOLD_EXP;
        String result = lsApp.listFolderContent(true, false, DIFF_EXT_FILE, INVALID_FILE, FOLDER_NAME);

        assertEquals(expectedAns, outContent + result);
    }

    @Test
    public void listFolderContent_nullInput_RunSuccess() {
        assertThrows(NullPointerException.class,
                () -> lsApp.listFolderContent(true, false, DIFF_EXT_FILE, null));
    }

    // Test case for run(), this is to ensure that runs the correct listFolderContent implementation (Same 8 pairwise test case)
    // Change the order of args between test cases to ensure that the position doesn't affect correctness
    @Test
    public void run_TTEmptyFile_ReturnsTrue() throws Exception {
        String[] args = {"-RX"};
        setUpMock(true, true, new String[]{}, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(true, true)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_TFFile_ReturnsTrue() throws Exception {
        String[] args = {FILENAME, "-R"};
        setUpMock(true, false, new String[]{FILENAME}, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(true, false, FILENAME)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_TTFileNFolder_ReturnsTrue() throws Exception {
        String[] args = {"-X", FILENAME, "-R", FOLDER_NAME};
        setUpMock(true, true, new String[]{FILENAME, FOLDER_NAME}, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(true, true, FILENAME, FOLDER_NAME)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_TFFolder_ReturnsTrue() throws Exception {
        String[] args = {FOLDER_NAME, "-R"};
        setUpMock(true, false, new String[]{FOLDER_NAME}, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(true, false, FOLDER_NAME)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_FFFileNFolder_ReturnsTrue() throws Exception {
        String[] args = {FOLDER_NAME, FILENAME};
        setUpMock(false, false, new String[]{FOLDER_NAME, FILENAME}, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(false, false, FILENAME, FOLDER_NAME)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_FTFolder_ReturnsTrue() throws Exception {
        String[] args = {FOLDER_NAME, "-X"};
        setUpMock(false, true, new String[]{FOLDER_NAME}, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(false, true, FOLDER_NAME)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_FFEmpty_ReturnsTrue() throws Exception {
        String[] args = {};
        setUpMock(false, false, args, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(false, false)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_FTFile_ReturnsTrue() throws Exception {
        String[] args = {FILENAME};
        setUpMock(false, false, args, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(false, false, FILENAME)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    // Negative Test case
    @Test
    public void run_hasInvalidFile_RunSuccess() throws Exception {
        String[] args = {FILENAME, INVALID_FILE};
        setUpMock(false, false, args, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(false, false, FILENAME, INVALID_FILE)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_wrongExtension_RunSuccess() throws Exception {
        String[] args = {DIFF_EXT_FILE};
        setUpMock(false, false, args, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(false, false, DIFF_EXT_FILE)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_multiInvalid_RunSuccess() throws Exception {
        String[] args = {DIFF_EXT_FILE, "-R", INVALID_FILE, FOLDER_NAME};
        setUpMock(true, false, new String[]{DIFF_EXT_FILE, INVALID_FILE, FOLDER_NAME}, false);
        lsApp.run(args, inputStreamStdIn, stdOutResult);
        String expectedAns = lsApp.listFolderContent(true, false, DIFF_EXT_FILE, INVALID_FILE, FOLDER_NAME)
                + STRING_NEWLINE;

        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_unknownOption_ThrowsException() throws InvalidArgsException {
        String[] args = {"-P", FILENAME};
        setUpMock(false, false, new String[]{FILENAME}, true);
        Exception exception = assertThrows(LsException.class,
                () -> lsApp.run(args, inputStreamStdIn, stdOutResult));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    @Test
    public void run_caseSensitiveOption_ThrowsException() throws InvalidArgsException {
        String[] args = {"-x", FILENAME};
        setUpMock(false, false, new String[]{FILENAME}, true);
        Exception exception = assertThrows(LsException.class,
                () -> lsApp.run(args, inputStreamStdIn, stdOutResult));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    @Test
    public void run_unknownOptions_ThrowsException() throws InvalidArgsException {
        String[] args = {"-XRP", FILENAME};
        setUpMock(false, false, new String[]{FILENAME}, true);
        Exception exception = assertThrows(Exception.class,
                () -> lsApp.run(args, inputStreamStdIn, stdOutResult));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    @Test
    public void run_nullArgs_ThrowsException() {
        Exception exception = assertThrows(LsException.class,
                () -> lsApp.run(null, inputStreamStdIn, stdOutResult));
        assertTrue(exception.getMessage().contains(ERR_NULL_ARGS));
    }

    @Test
    public void run_nullStdOut_ThrowsException() {
        String[] args = {"test"};
        Exception exception = assertThrows(LsException.class,
                () -> lsApp.run(args, inputStreamStdIn, null));
        assertTrue(exception.getMessage().contains(ERR_NO_OSTREAM));
    }
}
