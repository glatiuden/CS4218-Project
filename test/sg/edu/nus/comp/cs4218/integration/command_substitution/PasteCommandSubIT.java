package sg.edu.nus.comp.cs4218.integration.command_substitution;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.ExitApplication.EXIT_MESSAGE;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class PasteCommandSubIT {
    private static final String PASTE_OUT = FILE_ONE_CONTENT + TAB + FILE_TWO_CONTENT + STRING_NEWLINE;
    private static final String PASTE_OUT_1 = FILE_ONE_CONTENT + STRING_NEWLINE;
    @TempDir
    public static Path folderPath;
    private static String dupContent = FILE_ONE_CONTENT + STRING_NEWLINE;
    private static Path file1Path;
    private static Path file2Path;
    private static Path rmFilePath;
    private static Path nestPathDir;
    private static Path nestFile1Path;
    private static Path nestFile2Path;
    private static Path cpFilePath;
    private static Path filePath;
    private static ByteArrayOutputStream outCapture, testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;

    @BeforeAll
    public static void setUp() throws IOException {
        testOutputStream = new ByteArrayOutputStream();
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();

        FileUtils.createNewDirs(folderPath);
        Environment.setCurrentDirectory(folderPath.toString());
        outCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outCapture));

        // File: A.txt, File Content: "I love CS4218"
        file1Path = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(file1Path, FILE_ONE_CONTENT);

        // File: B.txt, File Content: "CS4218"
        file2Path = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(file2Path, FILE_TWO_CONTENT);

        // File: file.txt, File Content: "A.txt B.txt"
        filePath = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(filePath, file1Path.toString() + "\n" + file2Path.toString());

        // ./nest
        nestPathDir = folderPath.resolve(NEST_DIR);
        createNewDirs(nestPathDir);

        // File: nest/file.txt, File Content: "I love CS4218"
        nestFile1Path = folderPath.resolve(NEST_DIR).resolve(FILE_ONE_NAME);
        nestFile2Path = folderPath.resolve(NEST_DIR).resolve(FILE_TWO_NAME);
        Files.writeString(nestFile1Path, FILE_ONE_CONTENT);
        Files.writeString(nestFile2Path, FILE_TWO_CONTENT);

        // File: sort.txt, File Content: Multiline text
        Path sortFilePath = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(sortFilePath, file2Path.toString() + "\n" + file1Path.toString());

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);

        // File: uniq.txt
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        dupContent = file1Path.toString() + "\n" + file1Path.toString() + "\n" + file2Path.toString();
        Files.writeString(uniqFilePath, dupContent);
    }

    @AfterAll
    public static void tearDown() {
        deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    @AfterEach
    public void reset() throws IOException {
        deleteMvTestFiles();
        deleteFileIfExists(cpFilePath);
        testOutputStream.reset();
        outCapture.reset();
        Environment.setCurrentDirectory(folderPath.toString());
    }

    // paste + cat
    // Positive Test Case
    // paste `cat A.txt` => paste I love CS4218
    @Test
    void pasteCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    // paste + cat
    // Negative Test Case: invalid.txt does not exist
    // paste `cat invalid.txt` => cat invalid.txt
    @Test
    void pasteCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + cat
    // Positive Test Case
    // paste `cd nest`  => paste A.txt
    @Test
    void pasteCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), nestFile1Path.toString(), nestFile2Path.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
        assertEquals(nestPathDir.toString(), Environment.currentDirectory);
    }

    // paste + cd
    // Negative Test Case: dir invalid does not exist
    // paste `cd invalid`  => cd invalid
    @Test
    void pasteCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), nestFile1Path.toString(), nestFile2Path.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + cp
    // Positive Test Case: cp.txt is created by cp command
    // paste `cp A.txt cp.txt` => paste cp.txt
    @Test
    void pasteCpCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), cpFilePath.toString(), file1Path.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertTrue(Files.exists(cpFilePath));
        assertEquals(FILE_ONE_CONTENT, getFileContent(cpFilePath));
        assertEquals(FILE_ONE_CONTENT + TAB + FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // paste + cp
    // Positive Test Case: cp.txt is an existing file with content, overwritten by cp
    // paste `cp A.txt cp.txt` cp.txt => paste
    @Test
    void pasteCpCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), cpFilePath.toString(), file2Path.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    // paste + cut
    // Positive test case
    // paste `cut -b 1-15 file.txt` => paste A.txt B.txt
    @Test
    void pasteCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format("`%s -b 1-100 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    // paste + cut
    // Negative test case: missing flag
    // paste `cut 1-15 A.txt` => cut 1-15 A.txt
    @Test
    void pasteCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {PASTE_CMD, String.format("`%s 1-15 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // paste + echo
    // Positive test case
    // paste `echo A.txt` => paste A.txt
    @Test
    void pasteEchoCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, ECHO_CMD, file1Path.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT_1, testOutputStream.toString());
    }

    // paste + echo
    // Positive Test Case: echo is empty, only output single line
    // paste `echo` => paste
    @Test
    void pasteEchoCommand_emptyEcho_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(SINGLE_STRING, ECHO_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals("", testOutputStream.toString());
    }

    // paste + exit
    // Positive Test Case
    // paste `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will print empty line)
    @Test
    void pasteExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {PASTE_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // paste + grep
    // Positive Test Case
    // paste `grep A.txt file.txt` => paste A.txt
    @Test
    void pasteGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT_1, testOutputStream.toString());
    }

    // paste + grep
    // Negative Test Case: invalid pattern
    // paste `grep CS4218\\ A.txt` => grep CS4218\\ A.txt
    @Test
    void pasteGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // paste + rm
    // Positive Test Case: rm remove.txt
    // paste `rm remove.txt` A.txt => paste A.txt
    @Test
    void pasteRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        rmFilePath = folderPath.resolve(FILE_RM_NAME);
        createNewFile(rmFilePath);

        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), file1Path.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT_1, testOutputStream.toString());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // paste + rm
    // Positive Test Case: rm remove.txt
    // paste `rm remove.txt` A.txt => paste A.txt
    @Test
    void pasteRmCommand_removeFileSubCommand_shouldThrowPasteException() throws Exception {
        rmFilePath = folderPath.resolve(FILE_RM_NAME);
        createNewFile(rmFilePath);

        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), rmFilePath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + rm
    // Negative Test Case: invalid.txt does not exist
    // paste `rm remove.txt` A.txt => paste remove.txt
    @Test
    void pasteRmCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_ONE_CONTENT};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + sort
    // Positive Test Case: sort.txt = B.txt A.txt
    // paste `sort sort.txt` => paste A.txt B.txt
    @Test
    void pasteSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    // paste + sort
    // Negative Test Case: invalid.txt does not exist
    // paste `sort invalid.txt` => sort invalid.txt
    @Test
    void pasteSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + tee
    // Positive Test Case
    // paste `tee tee.txt` => paste A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void pasteTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(file1Path.toString().getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT_1, testOutputStream.toString());
        assertEquals(file1Path.toString(), getFileContent(folderPath.resolve(FILE_TEE_NAME)));
    }

    // paste + tee
    // Positive Test Case
    // paste `tee` => paste A.txt
    // System.in : A.txt (this will be outputted)
    @Test
    void pasteTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(file1Path.toString().getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {PASTE_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT_1, testOutputStream.toString());
    }

    // paste + wc
    // Negative Test Case
    // Sub Command is valid but the output is not suitable for paste
    // paste `wc sort.txt` => paste x x x A.txt
    @Test
    void pasteWcCommand_validSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + wc
    // Negative Test Case
    // paste `wc invalid.txt` => wc invalid.txt
    @Test
    void pasteWcCommand_invalidSubCommand_shouldOutputErrorMessage() throws Exception {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), outCapture.toString().trim());
    }

    // paste + mv
    // Positive Test Case
    // paste `mv mv1.txt mv2.txt` A.txt => mv mv1.txt mv2.txt then paste A.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void pasteMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), mv2Path.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(PASTE_OUT_1, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // paste + mv
    // Positive Test Case
    // paste `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then paste A.txt
    // mv1.txt should be moved into mv-folder
    @Test
    void pasteMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), file1Path.toString(), file2Path.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(PASTE_OUT, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME))); // uniq1.txt moved into mv-folder
    }

    // paste + mv
    // Negative Test Case
    // uniq `mv mv1.txt mv2.txt` mv1.txt => mv mv1.txt mv2.txt
    // Error as mv1.txt has already been merged into mv2.txt
    @Test
    void pasteMvCommand_invalidSubCommand_shouldThrowPasteException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + mv
    // Negative Test Case
    // paste `mv mv1.txt mv2.txt` uniq1.txt => mv mv1.txt mv2.txt then paste mv1.txt
    // mv1.txt is already merged, failing the paste execution
    @Test
    void pasteMvCommand_fileSubCommand_shouldThrowPasteException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), mv1Path.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + mv
    // Negative Test Case
    // paste `mv mv1.txt mv-folder` uniq1.txt => mv mv1.txt mv-folder then paste mv1.txt
    // mv1.txt should be moved into mv-folder, failing the paste execution
    @Test
    void pasteMvCommand_fileAndDirectorySubCommand_shouldThrowPasteException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {PASTE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + uniq
    // Positive Test Case
    // paste.txt = A.txt B.txt
    // paste A.txt B.txt
    @Test
    void pasteUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    // paste + uniq
    // Positive Test Case
    // System.in = A.txt B.txt
    // paste A.txt B.txt
    @Test
    void pasteUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(dupContent.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {PASTE_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    // paste + uniq
    // Negative Test Case: invalid.txt does not exist
    // paste `uniq invalid.txt`
    @Test
    void pasteUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + uniq
    // Negative Test Case: arg is a folder
    // paste `uniq nest`
    @Test
    void pasteUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // paste + paste
    // Positive Test Case
    // paste `paste paste.txt` => uniq A.txt B.txt
    @Test
    void pastePasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, PASTE_CMD, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    // paste + paste
    // Negative Test Case
    // paste `paste invalid.txt` => paste invalid.txt
    @Test
    void pastePasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // paste + ls
    // Negative Test Case
    @Test
    void pasteLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {PASTE_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // paste + unknown
    // Negative Test Case
    @Test
    void pasteInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {PASTE_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
