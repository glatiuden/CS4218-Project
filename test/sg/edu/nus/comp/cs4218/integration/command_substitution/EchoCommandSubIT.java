package sg.edu.nus.comp.cs4218.integration.command_substitution;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.ExitApplication.EXIT_MESSAGE;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public class EchoCommandSubIT {
    private static final String MULTI_CONTENT = "I love CS4218\nCS4218 loves\nMuch loves CS4218";
    private static final String SORTED_CONTENT = "CS4218 loves I love CS4218 Much loves CS4218";
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_ONE_NAME;
    private static final String PASTE_CONT = FILE_ONE_NAME + "\n" + FILE_TWO_NAME;

    @TempDir
    public static Path folderPath;
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
        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(path1, FILE_ONE_CONTENT);

        // File: B.txt, File Content: "CS4218"
        Path path2 = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(path2, FILE_TWO_CONTENT);

        // File: file.txt, File Content: "A.txt"
        Path pathFile = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(pathFile, FILE_ONE_NAME);

        // ./nest
        Files.createDirectories(folderPath.resolve(NEST_DIR));
        // File: nest/file.txt, File Content: "I love CS4218"
        Path dirPath = folderPath.resolve(NEST_DIR).resolve(FILE_ONE_NAME);
        createNewFile(dirPath);
        Files.writeString(dirPath, FILE_ONE_CONTENT);

        // File: sort.txt, File Content: Multiline text
        Path pathSortFile = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(pathSortFile, MULTI_CONTENT);

        // File: uniq.txt, File Content: Multiline text
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        Files.writeString(uniqFilePath, DUP_CONT);

        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);
        Files.writeString(pasteFilePath, PASTE_CONT);
    }

    @AfterAll
    public static void tearDown() {
        deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    @AfterEach
    public void reset() throws IOException {
        deleteMvTestFiles();
        testOutputStream.reset();
        outputCapture.reset();
    }

    // echo + cat
    // Positive Test Case
    // echo `cat A.txt` => echo I love CS4218
    @Test
    void echoCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // echo + cat
    // Negative Test Case: invalid.txt does not exist
    // echo `cat invalid.txt` => cat invalid.txt
    @Test
    void echoCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // echo + cat
    // Positive Test Case
    // echo `cd nest`  => echo A.txt
    @Test
    void echoCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
        assertEquals(folderPath.resolve(NEST_DIR).toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // echo + cd
    // Negative Test Case: dir invalid does not exist
    // echo `cd invalid`  => cd invalid
    @Test
    void echoCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // echo + cp
    // Positive Test Case: cp.txt is created by cp command
    // echo `cp A.txt cp.txt` => echo
    @Test
    void echoCpCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        Path cpFilePath = folderPath.resolve(FILE_CP_NAME);
        assertEquals(getFileContent(cpFilePath), FILE_ONE_CONTENT);
        assertTrue(cpFilePath.toFile().exists());
    }

    // echo + cp
    // Positive Test Case: cp.txt is an existing file with content, overwritten by cp
    // echo `cp A.txt cp.txt` cp.txt => echo
    @Test
    void echoCpCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path cpFilePath = folderPath.resolve(FILE_CP_NAME);
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        assertTrue(cpFilePath.toFile().exists());
        assertEquals(getFileContent(cpFilePath), FILE_TWO_CONTENT);

        String[] args = {ECHO_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cpFilePath), FILE_ONE_CONTENT);
    }

    // echo + cut
    // Positive test case
    // echo `cut -b 1-15 A.txt` => echo I love CS4218
    @Test
    void echoCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format("`%s -b 1-15 %s`", CUT_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // echo + cut
    // Negative test case: missing flag
    // echo `cut 1-15 A.txt` => cut 1-15 A.txt
    @Test
    void echoCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {ECHO_CMD, String.format("`%s 1-15 %s`", CUT_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // echo + echo
    // Positive test case
    // echo `echo I love CS4218` => echo I love CS4218
    @Test
    void echoEchoCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_CONTENT)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // echo + echo
    // Positive Test Case: sub command echo is empty, text is after the sub command
    // echo `echo` I love CS4218 => echo I love CS4218
    @Test
    void echoEchoCommand_textAfterSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format("`%s`", ECHO_CMD), FILE_ONE_CONTENT};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // echo + exit
    // Positive Test Case
    // echo `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will print empty line)
    @Test
    void echoExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {ECHO_CMD, String.format("`%s`", EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // echo + grep
    // Positive Test Case
    // echo `grep CS4218 A.txt` => echo I love CS4218
    @Test
    void echoGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // echo + grep
    // Negative Test Case: invalid pattern
    // echo `grep CS4218\\ A.txt` => grep CS4218\\ A.txt
    @Test
    void echoGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {ECHO_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // echo + ls
    // Positive Test Case
    // echo `ls nest` => echo A.txt
    @Test
    void echoLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, LS_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = NEST_DIR + ": " + FILE_ONE_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // echo + ls
    // Negative Test Case
    @Test
    void echoLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // echo + rm
    // Positive Test Case: rm remove.txt
    // echo `rm remove.txt` I love CS4218 => echo I love CS4218
    @Test
    void echoRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path rmFilePath = folderPath.resolve(FILE_RM_NAME);
        createNewFile(rmFilePath);

        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_CONTENT};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
        assertFalse(rmFilePath.toFile().exists()); // remove.txt should be removed
    }

    // echo + rm
    // Negative Test Case: invalid.txt does not exist
    // echo `rm remove.txt` I love CS4218 => echo I love CS4218
    @Test
    void echoRmCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_ONE_CONTENT};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // echo + sort
    // Positive Test Case
    // echo `sort sort.txt` => echo CS4218 lovesI love CS4218Much loves CS4218
    @Test
    void echoSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = SORTED_CONTENT + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // echo + sort
    // Negative Test Case: invalid.txt does not exist
    // echo `sort invalid.txt` => sort invalid.txt
    @Test
    void echoSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // echo + tee
    // Positive Test Case
    // echo `tee tee.txt` I love CS4218 => echo I love CS4218
    // System.in : I love CS4218 (this will be output and written into tee.txt)
    @Test
    void echoTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_CONTENT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
        assertEquals(FILE_ONE_CONTENT, getFileContent(folderPath.resolve(FILE_TEE_NAME)));
    }

    // echo + tee
    // Positive Test Case
    // echo `tee` => echo I love CS4218
    // System.in : I love CS4218 (this will be outputted)
    @Test
    void echoTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_CONTENT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {ECHO_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // echo + wc
    // Positive Test Case
    // echo `wc sort.txt` => echo 2 8 44 sort.txt
    @Test
    void echoWcCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = "0 3 13 A.txt" + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // echo + wc
    // Negative Test Case
    // echo `wc invalid.txt` => wc invalid.txt
    @Test
    void echoWcCommand_invalidSubCommand_shouldOutputErrorMessage() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
    }

    // echo + mv
    // Positive Test Case
    // echo `mv mv1.txt mv2.txt` mv.txt => mv mv1.txt mv2.txt then echo
    // mv1.txt should be merged into mv2.txt
    @Test
    void echoMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {ECHO_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(Files.exists(folderPath.resolve(FILE_MV1_NAME)));
        assertEquals(getFileContent(folderPath.resolve(FILE_MV2_NAME)), FILE_ONE_CONTENT);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // echo + mv
    // Positive Test Case
    // echo `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then echo
    // mv1.txt should be moved into mv-folder
    @Test
    void echoMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {ECHO_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(Files.exists(folderPath.resolve(FILE_MV1_NAME)));
        assertTrue(Files.exists(folderPath.resolve(DIR_MV_NAME).resolve(FILE_MV1_NAME))); // mv1.txt moved into mv-folder
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // echo + mv
    // Negative Test Case
    // echo `mv invalid.txt mv-folder` => mv invalid.txt mv-folder then echo
    // invalid.txt doesn't exist
    @Test
    void echoMvCommand_invalidSrcSubCommand_shouldOutputErrorMessage() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {ECHO_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_INVALID, DIR_MV_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(MV_EXCEP, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // echo + uniq
    // Positive Test Case
    // uniq.txt = A.txt A.txt B.txt B.txt A.txt
    // Should echo A.txt B.txt A.txt
    @Test
    void echoUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + " " + FILE_TWO_NAME + " " + FILE_ONE_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    // echo + uniq
    // Positive Test Case
    // System.in = A.txt A.txt B.txt B.txt A.txt
    // Should cat A.txt B.txt A.txt
    @Test
    void echoUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {ECHO_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + " " + FILE_TWO_NAME + " " + FILE_ONE_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    // echo + uniq
    // Negative Test Case: invalid.txt does not exist
    // echo `uniq invalid.txt`
    @Test
    void echoUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // echo + uniq
    // Negative Test Case: arg is a folder
    // echo `uniq nest`
    @Test
    void echoUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // echo + paste
    // Positive Test Case
    // echo `paste paste.txt` => echo A.txt B.txt
    @Test
    void echoPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + " " + FILE_TWO_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    // echo + paste
    // Negative Test Case
    // echo `paste invalid.txt` => paste invalid.txt
    @Test
    void echoPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {ECHO_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // echo + unknown
    // Negative Test Case
    @Test
    void echoInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {ECHO_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
