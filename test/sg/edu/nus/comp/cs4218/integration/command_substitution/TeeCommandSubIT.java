package sg.edu.nus.comp.cs4218.integration.command_substitution;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;
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
import static sg.edu.nus.comp.cs4218.impl.app.UniqApplication.ERR_EXTRA_FILE;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class TeeCommandSubIT {
    private static final String MULTI_CONTENT = "A.txt";
    private final static String OVERWRITTEN_TEXT = "Overwritten...";
    private static final String TEE_OUT = OVERWRITTEN_TEXT + STRING_NEWLINE;
    private static final String UNIQ_CONT = FILE_MV1_NAME + "\n" + FILE_MV1_NAME + "\n" + FILE_MV2_NAME + "\n" + FILE_MV2_NAME;
    private static final String PASTE_CONT = FILE_MV1_NAME + "\n" + FILE_MV2_NAME;

    @TempDir
    public static Path folderPath;
    private static Path nestDirPath;
    private static Path rmFilePath;
    private static Path teeFilePath;
    private static Path cpFilePath;
    private static Path pasteFilePath;
    private static ByteArrayOutputStream outputCapture, testOutputStream;
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
        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));

        // File: A.txt, File Content: "I love CS4218"
        teeFilePath = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(teeFilePath, FILE_ONE_CONTENT);

        // File: B.txt, File Content: "CS4218"
        Path path2 = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(path2, FILE_TWO_CONTENT);

        // File: file.txt, File Content: "A.txt"
        Path pathFile = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(pathFile, FILE_ONE_NAME);

        // ./nest
        nestDirPath = folderPath.resolve(NEST_DIR);
        Files.createDirectories(nestDirPath);

        // File: nest/A.txt, File Content: "I love CS4218"
        Path dirPath = nestDirPath.resolve(FILE_ONE_NAME);
        Files.writeString(dirPath, FILE_ONE_CONTENT);

        // File: sort.txt, File Content: Multiline text
        Path pathSortFile = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(pathSortFile, MULTI_CONTENT);

        // File: remove.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);

        // File: paste.txt
        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);
        Files.writeString(pasteFilePath, PASTE_CONT);

        // File: uniq.txt
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        Files.writeString(uniqFilePath, UNIQ_CONT);
    }

    @AfterAll
    public static void tearDown() {
        deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    @BeforeEach
    public void setUpEach() throws IOException {
        Files.writeString(teeFilePath, FILE_ONE_CONTENT);
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(OVERWRITTEN_TEXT.getBytes());
        System.setIn(inputCapture);
    }

    @AfterEach
    public void reset() throws IOException {
        deleteMvTestFiles();
        testOutputStream.reset();
        outputCapture.reset();
        Files.writeString(teeFilePath, FILE_ONE_CONTENT); // Rewrite the file to original content
    }

    // tee + cat
    // Positive Test Case
    // tee `cat file.txt` => tee A.txt
    @Test
    void teeCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
    }

    // tee + cat
    // Negative Test Case: invalid.txt does not exist
    // tee `cat invalid.txt` => cat invalid.txt
    @Test
    void teeCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // tee + cd
    // Positive Test Case
    // tee `cd nest` A.txt => tee nest/A.txt
    @Test
    void teeCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(nestDirPath.toString(), Environment.currentDirectory); // Check if we are in the cd directory
        // Reset directory back to test folder
        Environment.setCurrentDirectory(folderPath.toString());
    }

    // tee + cd
    // Negative Test Case: invalid dir does not exist
    // tee `cd invalid` A.txt => cd invalid
    @Test
    void teeCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory); // Remains in the current directory
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // tee + cp
    // Positive Test Case
    // tee `cp A.txt cp.txt` cp.txt => tee cp.txt
    @Test
    void teeCpCommand_existingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT); // Before: 'CS4218'
        String[] args = {TEE_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        // Expected: Output stream: "Overwritten...", cp.txt content: "Overwritten..."
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(cpFilePath));
    }

    // tee + cp
    // Positive Test Case: cp creates cp.txt
    // tee `cp A.txt cp.txt` cp.txt => tee cp.txt
    @Test
    void teeCpCommand_noExistingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        assertFalse(Files.exists(cpFilePath)); // File does not exist prior to command
        String[] args = {TEE_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        // Expected: Output stream: "Overwritten...", cp.txt content: "Overwritten..."
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertTrue(Files.exists(cpFilePath)); // File created by tee
        assertEquals(OVERWRITTEN_TEXT, getFileContent(cpFilePath));
    }

    // tee + cut
    // Positive Test Case
    // tee `cut -b 1-6 file.txt` => tee A.txt
    @Test
    void teeCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
    }

    // tee + cut
    // Negative Test Case: missing flag
    // Unable to test invalid file from Cut as the error message is output using stdout, which the input stream will not close.
    // tee `cut 1-6 invalid.txt` => cut 1-6 invalid.txt
    @Test
    void teeCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {TEE_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // tee + echo
    // Positive Test Case
    // tee `echo A.txt` => tee A.txt
    @Test
    void teeEchoCommand_echoFileNameSubCommand_ShouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
    }

    // tee + echo
    // Positive Test Case
    // tee `echo` => tee
    @Test
    void teeEchoCommand_echoEmptySubCommand_ShouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(SINGLE_STRING, ECHO_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
    }

    // tee + exit
    // Positive Test Case
    // tee `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will turn into input stream)
    @Test
    void teeExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {TEE_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // tee + grep
    // Positive Test Case
    // tee `grep A.txt file.txt` => tee A.txt
    @Test
    void teeGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
    }

    // tee + grep
    // Negative Test Case: invalid pattern
    // tee `grep CS4218\\ file.txt` => grep CS4218\\ file.txt
    @Test
    void teeGrepCommand_invalidSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {TEE_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // tee + ls
    // Positive Test Case
    // tee `ls nest` => tee A.txt
    @DisabledOnOs(OS.WINDOWS)
    @Test
    void teeLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, LS_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
    }

    // tee + ls
    // Negative Test Case
    @Test
    void teeLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // tee + rm
    // Positive Test Case
    // tee `rm remove.txt` A.txt => tee A.txt
    @Test
    void teeRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
        assertFalse(Files.exists(rmFilePath));
    }

    // tee + rm
    // Positive Test Case
    // tee `rm remove.txt` remove.txt => tee remove.txt
    // Tee will re-create remove.txt after removed by the command substitution rm
    @Test
    void teeRmCommand_rmFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(rmFilePath, FILE_TWO_CONTENT); // remove.txt content = "CS4218"
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString()); // Tee's output stream
        assertTrue(Files.exists(rmFilePath)); // Tee re-created remove.txt
        assertEquals(OVERWRITTEN_TEXT, getFileContent(rmFilePath)); // Output overwritten
    }

    // tee + rm
    // Positive Test Case
    // tee `rm remove.txt` remove.txt => tee remove.txt
    // Tee will re-create remove.txt after removed by the command substitution rm
    @Test
    void teeRmCommand_invalidSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // tee + sort
    // Positive Test Case
    // tee `sort sort.txt` A.txt => tee A.txt
    @Test
    void teeSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
    }

    // tee + sort
    // Negative Test Case: invalid.txt does not exist
    // tee `sort invalid.txt` A.txt => sort invalid.txt
    @Test
    void teeSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // tee + tee
    // Positive Test Case
    // tee `tee tee.txt` => tee A.txt
    @Test
    void teeTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
    }

    // tee + tee
    // Positive Test Case
    // tee `tee` => tee
    @Test
    void teeTeeCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, getFileContent(teeFilePath));
    }

    // tee + wc
    // Positive Test Case
    // The numbers are ignored (as Grep does not throw error for invalid files, only stdout error message) until it takes in A.txt as input
    // tee `wc A.txt` => tee 0 3 13 A.txt
    @Test
    void teeWcCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
        assertTrue(Files.exists(folderPath.resolve("0")));
    }

    // tee + mv
    // Positive Test Case
    // tee `mv mv1.txt mv2.txt` => mv mv1.txt mv2.txt then tee mv2.txt
    // mv1.txt should be merged into mv2.txt then overwritten by tee
    @Test
    void teeMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {TEE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertEquals(OVERWRITTEN_TEXT, getFileContent(mv2Path));
    }

    // tee + mv
    // Positive Test Case
    // tee `mv mv1.txt mv-folder` A.txt => mv mv1.txt mv-folder then tee A.txt
    // mv1.txt should be moved into mv-folder
    @Test
    void teeMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {TEE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(TEE_OUT, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME))); // mv1.txt moved into mv-folder
    }

    // tee + mv
    // Negative Test Case
    // tee `mv mv1.txt mv-folder` mv1.txt => mv mv1.txt mv-folder then tee mv1.txt
    // mv1.txt should be moved into mv-folder, another mv1.txt was re-created by tee
    @Test
    void teeMvCommand_mvFileIntoFolderSubCmmand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {TEE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertTrue(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME))); // mv1.txt moved into mv-folder
    }

    // tee + mv
    // Negative Test Case
    // tee `mv invalid.txt mv2.txt` mv1.txt => mv invalid.txt mv2.txt
    @Test
    void teeMvCommand_invalidSubCommand_shouldOutputErrorMessage() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {TEE_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_INVALID, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(MV_EXCEP, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
    }

    // tee + uniq
    // Positive Test Case
    // uniq.txt = A.txt B.txt
    // tee A.txt B.txt (results are output into B.txt)
    @Test
    void teeUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(mv1Path));
        assertEquals(OVERWRITTEN_TEXT, getFileContent(mv2Path));
    }

    // tee + uniq
    // Negative Test Case: invalid.txt does not exist
    // tee `uniq invalid.txt`
    @Test
    void teeUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // tee + tee
    // Negative Test Case: arg is a folder
    // tee `uniq nest`
    @Test
    void teeUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // tee + paste
    // Positive Test Case
    // tee `paste paste.txt` => tee A.txt B.txt
    @Test
    void teePasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(TEE_OUT, testOutputStream.toString());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(mv1Path));
        assertEquals(OVERWRITTEN_TEXT, getFileContent(mv2Path));
    }

    // tee + paste
    // Negative Test Case
    // tee `paste invalid.txt` => paste invalid.txt
    @Test
    void teePasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {TEE_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // tee + unknown
    // Negative Test Case
    @Test
    void teeInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {TEE_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
