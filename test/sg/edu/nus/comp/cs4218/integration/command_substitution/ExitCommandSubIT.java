package sg.edu.nus.comp.cs4218.integration.command_substitution;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

/**
 * For ExitCommandSub: The command substitution (in the back quotes) will be executed first, then Exit Command.
 * Hence, for each test, need to check whether the command substitution is executed correctly, then check whether ExitException is thrown.
 */
public class ExitCommandSubIT {
    private static final String FILE_UNIQ_O_NAME = "uniq-out.txt";
    private static final String MULTI_CONTENT = "\n54321\n\n  \n  spaced\na123!@#random\n!!!\nb\nAAA 123random\nBBB\n@@\n1123\n1\n55";
    private static final String OVERWRITTEN_TEXT = "Overwritten...";
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_ONE_NAME;
    private static final String DUP_OUT = FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE + FILE_ONE_NAME;
    private static final String PASTE_CONT = FILE_ONE_NAME + "\n" + FILE_TWO_NAME;

    @TempDir
    public static Path folderPath;
    private static Path nestDirPath;
    private static Path teeFilePath;
    private static Path rmFilePath;
    private static Path uniqFileOutPath;
    private static Path pasteFilePath;
    private static Path cpFilePath;
    private static ByteArrayOutputStream outCapture, testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;

    @BeforeAll
    public static void setUp() throws IOException {
        testOutputStream = new ByteArrayOutputStream();
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();

        outCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outCapture));
        FileUtils.createNewDirs(folderPath);
        Environment.setCurrentDirectory(folderPath.toString());

        // File: A.txt, File Content: "I love CS4218"
        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(path1, FILE_ONE_CONTENT);

        // File: B.txt, File Content: "CS4218"
        Path path2 = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(path2, FILE_TWO_CONTENT);

        // File: file.txt, File Content: "A.txt"
        Path filePath = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(filePath, FILE_ONE_NAME);

        // ./nest
        nestDirPath = folderPath.resolve(NEST_DIR);
        Files.createDirectories(nestDirPath);
        // File: nest/file.txt, File Content: "I love CS4218"
        Path nestPath = nestDirPath.resolve(FILE_ONE_NAME);
        Files.writeString(nestPath, FILE_ONE_CONTENT);

        // File: sort.txt, File Content: Multiline text
        Path pathSortFile = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(pathSortFile, MULTI_CONTENT);

        // File: uniq.txt, File Content: Multiline text
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        uniqFileOutPath = folderPath.resolve(FILE_UNIQ_O_NAME);
        Files.writeString(uniqFilePath, DUP_CONT);
        createNewFile(uniqFileOutPath);

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);
        // File: tee.txt
        teeFilePath = folderPath.resolve(FILE_TEE_NAME);
        // File: rm.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);
        createNewFile(rmFilePath);
        // File: paste.txt
        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);
        Files.writeString(pasteFilePath, PASTE_CONT);
    }

    @AfterAll
    public static void tearDown() {
        FileUtils.deleteFolder(folderPath);
        Environment.resetCurrentDirectory();
    }

    @AfterEach
    public void reset() throws IOException {
        deleteMvTestFiles();
        testOutputStream.reset();
        outCapture.reset();
    }

    // Exit + Cat
    // Positive Test Case
    // exit `cat file.txt` => cat file.txt then exit
    @Test
    void exitCatCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + Cat
    // Negative Test Case: invalid.txt does not exist
    // exit `cat invalid.txt` => cat invalid.txt
    @Test
    void exitCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // Exit + CD
    // Positive Test Case
    // exit `cd nest` => cd nest then exit
    @Test
    void exitCdCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory to the main test folder
    }

    // Exit + CD
    // Negative Test Case: The folder/directory "invalid" does not exist
    // exit `cd invalid` => cd invalid
    @Test
    void exitCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // Exit + CP
    // Positive Test Case: cp.txt is created by cp command
    // exit `cp A.txt cp.txt` => cp A.txt cp.txt then exit
    @Test
    void exitCpCommand_noExistingFileAndValidSubCommand_shouldThrowExitException() throws IOException {
        String[] args = {EXIT_CMD, String.format("`%s %s %s`", CP_CMD, FILE_ONE_NAME, FILE_CP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(FILE_ONE_CONTENT, getFileContent(cpFilePath));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + CP
    // Positive Test Case: cp.txt is an existing file with content, overwritten by cp
    // exit `cp A.txt cp.txt` => cp A.txt cp.txt then exit
    @Test
    void exitCpCommand_existingFileAndValidSubCommand_shouldThrowExitException() throws IOException {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);

        String[] args = {EXIT_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(FILE_ONE_CONTENT, getFileContent(cpFilePath));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + Cut
    // Positive Test Case: cut the file name (A.txt) from file.txt
    // exit `cut -b 1-6 file.txt` => cut -b 1-6 file.txt then exit
    @Test
    void exitCutCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + Cut
    // Negative Test Case: missing flag
    // Unable to test invalid file from Cut as the error message is thrown using stdout, which ExitException will take precedence.
    // exit `cut 1-6 invalid.txt` => cut 1-6 invalid.txt then exit
    @Test
    void exitCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {EXIT_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // Exit + Echo
    // Positive Test Case: unable to check the stdout as ExitException takes precedence
    // exit `echo CS4218` => echo CS4218 then exit
    @Test
    void exitEchoCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, ECHO_CMD, GREP_PATTERN)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + Echo
    // Positive Test Case: Empty echo as there's technically no way to get invalid echo.
    // exit `echo` => echo then exit
    @Test
    void exitEchoCommand_emptyInputValidSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format("`%s`", ECHO_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + Exit
    // Positive Test Case
    // exit `exit` => exit
    @Test
    void exitExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format("`%s`", EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + Grep
    // Positive Test Case
    // exit `grep CS4218 B.txt` => grep CS4218 B.txt then exit
    @Test
    void exitGrepCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_TWO_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + LS
    // Positive Test Case: valid directory
    // exit `ls nest` => ls nest then exit
    @Test
    void exitLsCommand_validDirSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, LS_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + LS
    // Negative Test Case: Invalid directory but unable to check the stdout as ExitException takes precedence
    // exit `ls invalid` => ls invalid then exit
    @Test
    void exitLsCommand_invalidDirSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, LS_CMD, DIR_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + RM
    // Positive Test Case
    // exit `rm remove.txt` => rm remove.txt then exit
    @Test
    void exitRmCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertFalse(rmFilePath.toFile().exists());
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + RM
    // Negative Test Case: invalid.txt does not exist
    // exit `rm invalid.txt` => rm invalid.txt
    @Test
    void exitRmCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // Exit + Sort
    // Positive Test Case: but unable to check the stdout as ExitException takes precedence
    // exit `sort sort.txt` => sort sort.txt then exit
    @Test
    void exitSortCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + Sort
    // Negative Test Case: invalid.txt does not exist
    // exit `sort invalid.txt` => sort sort.txt
    @Test
    void exitSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // Exit + Tee
    // Positive Test Case: tee.txt does not exist, tee will create this file
    // exit `tee tee.txt` => tee tee.txt then exit
    // System.in: 'I love CS4218'
    @Test
    void exitTeeCommand_validSubCommand_shouldThrowExitException() throws IOException {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_CONTENT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
        assertEquals(FILE_ONE_CONTENT, getFileContent(teeFilePath));
        deleteFileIfExists(teeFilePath);
    }

    // Exit + Tee
    // Positive Test Case: tee.txt already exist with different content, tee cmd sub will overwrite the content
    // exit `tee tee.txt` => tee tee.txt then exit
    // System.in: 'Overwritten...'
    @Test
    void exitTeeCommand_teeOverwriteValidSubCommand_shouldThrowExitException() throws IOException {
        Files.writeString(teeFilePath, FILE_ONE_CONTENT);
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(OVERWRITTEN_TEXT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(teeFilePath));
        deleteFileIfExists(teeFilePath);
    }

    // Exit + WC
    // Positive Test Case
    // exit `wc A.txt` => wc A.txt then exit
    @Test
    void exitWcCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // Exit + WC
    // Negative Test Case: Invalid file but unable to check the stdout as ExitException takes precedence
    // exit `wc A.txt` => wc A.txt then exit
    @Test
    void exitWcCommand_invalidFileSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // exit + mv
    // Positive Test Case
    // exit `mv mv1.txt mv2.txt` mv.txt => mv mv1.txt mv2.txt then exit
    // mv1.txt should be merged into mv2.txt then application exits
    @Test
    void exitMvCommand_twoFilesSubCommand_shouldThrowExitException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {EXIT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());

        assertFalse(Files.exists(mv1Path));
        assertEquals(getFileContent(mv2Path), FILE_ONE_CONTENT);
    }

    // exit + mv
    // Positive Test Case
    // cat `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then exit
    // mv1.txt should be moved into mv-folder then application exits
    @Test
    void exitMvCommand_fileAndDirectorySubCommand_shouldThrowExitException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {EXIT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());

        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(folderPath.resolve(DIR_MV_NAME).resolve(FILE_MV1_NAME))); // mv1.txt moved into mv-folder
    }

    // exit + mv
    // Negative Test Case
    // exit `mv invalid.txt mv2.txt` => mv invalid.txt mv2.txt
    @Test
    void exitMvCommand_invalidSrcSubCommand_shouldThrowCatException() {
        String[] args = {EXIT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_INVALID, FILE_MV2_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // exit + uniq
    // Positive Test Case
    // uniq.txt = A.txt A.txt B.txt B.txt A.txt
    // uniq output = A.txt B.txt A.txt written into uniq-out.txt
    @Test
    void exitUniqCommand_fileSubCommand_shouldThrowExitException() throws Exception {
        String[] args = {EXIT_CMD, String.format(TRIPLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME, FILE_UNIQ_O_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
        assertEquals(DUP_OUT, getFileContent(uniqFileOutPath));
    }

    // exit + uniq
    // Negative Test Case: invalid.txt does not exist
    // exit `uniq invalid.txt`
    @Test
    void exitUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // exit + uniq
    // Negative Test Case: arg is a folder
    // exit `uniq nest`
    @Test
    void exitUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // exit + paste
    // Positive Test Case
    // exit `paste paste.txt` => exit A.txt B.txt
    @Test
    void exitPasteCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // exit + paste
    // Negative Test Case
    // exit `paste invalid.txt` => paste invalid.txt
    @Test
    void exitPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {EXIT_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // exit + unknown
    // Negative Test Case
    @Test
    void exitInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {EXIT_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
