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
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

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
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public class UniqCommandSubIT {
    private static final String UNIQ_OUT = FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE + FILE_ONE_NAME;
    private static final String DIR_TEMP = "uniq-folder";
    private static final String FILE_UNIQ1_NAME = "uniq1.txt";
    private static final String FILE_UNIQ2_NAME = "uniq2.txt";
    private static final String MULTI_CONTENT = FILE_UNIQ1_NAME + "\n" + FILE_UNIQ2_NAME;
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_ONE_NAME;
    private static final String NO_DUP_OUT = FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE;
    private static final String PASTE_CONT = FILE_UNIQ1_NAME + "\n" + FILE_UNIQ2_NAME;

    @TempDir
    public static Path folderPath;
    private static Path rmFilePath, pasteFilePath, nestDirPath, uniqPath, uniq1Path, uniq2Path, dirTempPath, cpFilePath;
    private static ByteArrayOutputStream outputCapture, testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;

    @BeforeAll
    public static void setUp() throws IOException {
        testOutputStream = new ByteArrayOutputStream();
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();

        createNewDirs(folderPath);
        Environment.setCurrentDirectory(folderPath.toString());
        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));

        // File: A.txt, File Content: "I love CS4218"
        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(path1, DUP_CONT);

        // File: B.txt, File Content: "CS4218"
        Path path2 = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(path2, FILE_TWO_CONTENT);

        // File: file.txt, File Content: "A.txt"
        Path pathFile = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(pathFile, FILE_ONE_NAME);

        // ./nest
        nestDirPath = folderPath.resolve(NEST_DIR);
        createNewDirs(nestDirPath);

        // File: nest/file.txt, File Content: "I love CS4218"
        Path dirPath = folderPath.resolve(NEST_DIR).resolve(FILE_ONE_NAME);
        Files.writeString(dirPath, DUP_CONT);

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);

        // File: paste.txt
        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);
        Files.writeString(pasteFilePath, PASTE_CONT);

        // File: rm.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // Uniq Temp Files
        uniqPath = folderPath.resolve(FILE_UNIQ_NAME);
        uniq1Path = folderPath.resolve(FILE_UNIQ1_NAME);
        uniq2Path = folderPath.resolve(FILE_UNIQ2_NAME);
        dirTempPath = folderPath.resolve(DIR_TEMP);
    }

    @AfterAll
    public static void tearDown() {
        FileUtils.deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    @AfterEach
    public void reset() throws IOException {
        deleteTempFiles();
        testOutputStream.reset();
        outputCapture.reset();
    }

    private void createTempFiles() throws IOException {
        Files.writeString(uniqPath, MULTI_CONTENT);
        Files.writeString(uniq1Path, DUP_CONT);
        Files.writeString(uniq2Path, DUP_CONT);
        createNewDirs(dirTempPath);
    }

    private void deleteTempFiles() throws IOException {
        deleteFileIfExists(uniqPath);
        deleteFileIfExists(uniq1Path);
        deleteFileIfExists(uniq2Path);
        deleteFolder(dirTempPath);
    }

    // uniq + cat
    // Positive Test Case: cat file.txt which will output A.txt
    // uniq `cat file.txt` => uniq A.txt
    @Test
    void uniqCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
    }

    // uniq + cat
    // Negative Test Case: invalid.txt does not exist
    // uniq `cat invalid.txt` => cat invalid.txt
    @Test
    void uniqCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + cd
    // Positive Test Case
    // uniq `cd nest` A.txt => uniq nest/A.txt
    @Test
    void uniqCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
        assertEquals(folderPath.resolve(NEST_DIR).toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // uniq + cd
    // Negative Test Case: invalid dir does not exist
    // uniq `cd invalid` A.txt => cd invalid
    @Test
    void uniqCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory); // Check that the directory was not changed
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + CP
    // Positive Test Case: cp.txt is created by cp command
    // uniq `cp A.txt cp.txt` cp.txt => uniq cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void uniqCpCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {UNIQ_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
        assertEquals(DUP_CONT.replaceAll(BREAK_LINE, STRING_NEWLINE), getFileContent(cpFilePath));
    }

    // uniq + cp
    // Positive Test Case: cp.txt already exist with other content, overwritten by cp
    // uniq `cp A.txt cp.txt` cp.txt => uniq cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void uniqCpCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {UNIQ_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
        assertEquals(DUP_CONT.replaceAll(BREAK_LINE, STRING_NEWLINE), getFileContent(cpFilePath));
    }

    // uniq + cut
    // Positive Test Case: cut the file name (A.txt) from file.txt
    // uniq `cut -b 1-6 file.txt` => uniq A.txt
    @Test
    void uniqCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {UNIQ_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
    }

    // uniq + cut
    // Negative Test Case: missing flag
    // uniq `cut -b 1-6 file.txt` => uniq A.txt
    @Test
    void uniqCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {UNIQ_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // uniq + echo
    // Positive Test Case: echo A.txt
    // uniq `echo A.txt` => uniq A.txt
    @Test
    void uniqEchoCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
    }

    // uniq + exit
    // Positive Test Case
    // uniq `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will turn into input stream)
    @Test
    void uniqExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {UNIQ_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // uniq + grep
    // Positive Test Case: grep A.txt from file.txt
    // uniq `grep A.txt file.txt` => uniq A.txt
    @Test
    void uniqGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {UNIQ_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
    }

    // uniq + grep
    // Negative Test Case: invalid grep pattern
    // uniq `grep CS4218\\ file.txt` => grep CS4218\\ file.txt
    @Test
    void uniqGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {UNIQ_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // uniq + ls
    // Positive Test Case
    // uniq `ls nest` => uniq A.txt
    @Test
    void uniqLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Environment.setCurrentDirectory(nestDirPath.toString());
        String[] args = {UNIQ_CMD, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
        Environment.setCurrentDirectory(folderPath.toString());
    }

    // uniq + ls
    // Negative Test Case
    @Test
    void uniqLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {UNIQ_OUT, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // uniq + rm
    // Positive Test Case
    // uniq should run as per normal and rm should remove rm.txt
    // uniq `rm remove.txt` A.txt => uniq A.txt
    @Test
    void uniqRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // uniq + rm
    // Negative Test Case
    // uniq `rm remove.txt` remove.txt => uniq remove.txt
    // uniq will not run as remove.txt will be removed by the command substitution
    @Test
    void uniqRmCommand_fileRemovedInSubCommand_shouldThrowUniqException() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_RM_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // uniq + rm
    // Negative Test Case
    // uniq `rm invalid.txt` => rm invalid.txt
    // Error thrown in the sub command as invalid.txt does not exist
    @Test
    void uniqRmCommand_invalidFileNameCommand_shouldThrowRmException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + sort
    // Positive Test Case
    // uniq `sort sort.txt` => uniq A.txt
    @Test
    void uniqSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(UNIQ_OUT, getFileContent(uniq2Path));
    }

    // uniq + sort
    // Negative Test Case: invalid.txt does not exist
    // uniq `sort invalid.txt` => sort invalid.txt
    @Test
    void uniqSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + sort
    // Negative Test Case: file sorted does not contain file name
    // uniq `sort A.txt` => sort A.txt
    @Test
    void uniqSortCommand_invalidPathFromSubCommand_shouldThrowUniqException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_TWO_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_TWO_CONTENT, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + tee
    // Positive Test Case
    // uniq `tee tee.txt` => uniq A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void uniqTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
        assertEquals(FILE_ONE_NAME, getFileContent(folderPath.resolve(FILE_TEE_NAME)));
    }

    // uniq + tee
    // Positive Test Case
    // uniq `tee` => uniq A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    // Tee command will return A.txt which will be passed to uniq
    @Test
    void uniqTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {UNIQ_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(NO_DUP_OUT, testOutputStream.toString());
    }

    // uniq + wc
    // Negative Test Case
    // uniq `wc A.txt` => uniq # # # A.txt
    // It is a valid sub command, however UniqException is thrown as the numbers are treated as extra operand.
    @Test
    void uniqWcCommand_validSubCommand_shouldThrowUniqException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertTrue(thrown.getMessage().contains(ERR_EXTRA_FILE));
    }

    // uniq + mv
    // Positive Test Case
    // uniq `mv uniq1.txt uniq2.txt` A.txt => mv uniq1.txt uniq2.txt then uniq A.txt
    // uniq1.txt should be merged into uniq2.txt
    @Test
    void uniqMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = {UNIQ_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_UNIQ1_NAME, FILE_UNIQ2_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(NO_DUP_OUT, testOutputStream.toString());
        assertFalse(Files.exists(uniq1Path));
        assertEquals(DUP_CONT.replaceAll(BREAK_LINE, STRING_NEWLINE), getFileContent(uniq2Path));
    }

    // uniq + mv
    // Positive Test Case
    // uniq `mv uniq1.txt mv-folder` mv.txt => mv uniq1.txt mv-folder then uniq mv.txt
    // uniq1.txt should be moved into mv-folder
    @Test
    void uniqMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = {UNIQ_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_UNIQ1_NAME, DIR_TEMP), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(NO_DUP_OUT, testOutputStream.toString());
        assertFalse(Files.exists(uniq1Path));
        assertTrue(Files.exists(dirTempPath.resolve(FILE_UNIQ1_NAME))); // uniq1.txt moved into mv-folder
    }

    // uniq + mv
    // Negative Test Case
    // uniq `mv uniq1.txt uniq2.txt` uniq1.txt => mv uniq1.txt uniq2.txt
    // Error as uniq1.txt has already been merged into mv2.txt
    @Test
    void uniqMvCommand_invalidSubCommand_shouldThrowUniqException() throws Exception {
        createTempFiles();
        String[] args = {UNIQ_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_UNIQ1_NAME, FILE_UNIQ2_NAME), FILE_UNIQ1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_UNIQ1_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + mv
    // Negative Test Case
    // uniq `mv uniq1.txt mv-folder` uniq1.txt => mv uniq1.txt mv-folder then uniq uniq1.txt
    // uniq1.txt should be moved into mv-folder, failing the uniq execution
    @Test
    void uniqMvCommand_fileAndDirectorySubCommand_shouldThrowUniqException() throws Exception {
        createTempFiles();
        String[] args = {UNIQ_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_UNIQ1_NAME, DIR_TEMP), FILE_UNIQ1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_UNIQ1_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + uniq
    // Positive Test Case
    // uniq.txt = A.txt B.txt
    // uniq A.txt B.txt (results are output into B.txt)
    @Test
    void uniqUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(UNIQ_OUT, getFileContent(uniq2Path));
    }

    // uniq + uniq
    // Positive Test Case
    // System.in = A.txt B.txt
    // uniq A.txt B.txt (results are output into B.txt)
    @Test
    void uniqUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(MULTI_CONTENT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {UNIQ_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(UNIQ_OUT, getFileContent(uniq2Path));
    }

    // uniq + uniq
    // Negative Test Case: invalid.txt does not exist
    // uniq `uniq invalid.txt`
    @Test
    void uniqUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + uniq
    // Negative Test Case: arg is a folder
    // uniq `uniq nest`
    @Test
    void uniqUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // uniq + paste
    // Positive Test Case
    // uniq `paste paste.txt` => uniq A.txt B.txt
    @Test
    void uniqPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(UNIQ_OUT, getFileContent(uniq2Path));
    }

    // uniq + paste
    // Negative Test Case
    // uniq `paste invalid.txt` => paste invalid.txt
    @Test
    void uniqPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {UNIQ_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // uniq + unknown
    // Negative Test Case
    @Test
    void uniqInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {UNIQ_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
