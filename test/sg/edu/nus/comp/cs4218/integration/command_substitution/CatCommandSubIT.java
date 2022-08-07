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
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public class CatCommandSubIT {
    public static final String INV_FLAG = "-!";
    private static final String MULTI_CONTENT = FILE_TWO_NAME + "\n" + FILE_ONE_NAME;
    private static final String CAT_OUT = FILE_ONE_CONTENT + STRING_NEWLINE;
    private static final String PASTE_CONT = FILE_ONE_NAME + "\n" + FILE_TWO_NAME;
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_ONE_NAME;
    @TempDir
    public static Path folderPath;
    private static Path rmFilePath;
    private static Path pasteFilePath;
    private static Path nestDirPath;
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
        Files.writeString(path1, FILE_ONE_CONTENT);

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
        Path dirPath = nestDirPath.resolve(FILE_ONE_NAME);
        Files.writeString(dirPath, FILE_ONE_CONTENT);

        // File: sort.txt, File Content: Multiline text
        Path sortFilePath = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(sortFilePath, MULTI_CONTENT);

        // File: rm.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // File: uniq.txt, File Content: Multiline text
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        Files.writeString(uniqFilePath, DUP_CONT);

        // File: paste.txt
        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);
        Files.writeString(pasteFilePath, PASTE_CONT);
    }

    @AfterAll
    public static void tearDown() {
        FileUtils.deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    @AfterEach
    public void reset() throws IOException {
        deleteMvTestFiles();
        deleteFileIfExists(rmFilePath);
        testOutputStream.reset();
        outputCapture.reset();
    }

    // cat + cat
    // Positive Test Case: cat file.txt which will output A.txt
    // cat `cat file.txt` => cat A.txt
    @Test
    void catCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
    }

    // cat + cat
    // Negative Test Case: invalid.txt does not exist
    // cat `cat invalid.txt` => cat invalid.txt
    @Test
    void catCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cat + cd
    // Positive Test Case
    // cat `cd nest` A.txt => cat nest/A.txt
    @Test
    void catCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // cat + cd
    // Negative Test Case: invalid dir does not exist
    // cat `cd invalid` A.txt => cd invalid
    @Test
    void catCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory); // Check that the directory was not changed
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cat + CP
    // Positive Test Case: cp.txt is created by cp command
    // cat `cp A.txt cp.txt` cp.txt => cat cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void catCpCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path cpFilePath = folderPath.resolve(FILE_CP_NAME);
        String[] args = {CAT_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
        assertEquals(FILE_ONE_CONTENT, getFileContent(cpFilePath));
        deleteFileIfExists(cpFilePath);
    }

    // cat + cp
    // Positive Test Case: cp.txt already exist with other content, overwritten by cp
    // cat `cp A.txt cp.txt` cp.txt => cat cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void catCpCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path cpFilePath = folderPath.resolve(FILE_CP_NAME);
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);

        String[] args = {CAT_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(CAT_OUT, testOutputStream.toString());
        assertEquals(FILE_ONE_CONTENT, getFileContent(cpFilePath));
    }

    // cat + cut
    // Positive Test Case: cut the file name (A.txt) from file.txt
    // cat `cut -b 1-6 file.txt` => cat A.txt
    @Test
    void catCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CAT_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
    }

    // cat + cut
    // Negative Test Case: missing flag
    // cat `cut 1-6 file.txt` => cut 1-6 file.txt
    @Test
    void catCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {CAT_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // cat + echo
    // Positive Test Case: echo A.txt
    // cat `echo A.txt` => cat A.txt
    @Test
    void catEchoCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
    }

    // cat + exit
    // Positive Test Case
    // cat `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will turn into input stream)
    @Test
    void catExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {CAT_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // cat + grep
    // Positive Test Case: grep A.txt from file.txt
    // cat `grep A.txt file.txt` => cat A.txt
    @Test
    void catGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CAT_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
    }

    // cat + grep
    // Negative Test Case: invalid grep pattern
    // cat `grep CS4218\\ file.txt` => grep CS4218\\ file.txt
    @Test
    void catGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {CAT_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // cat + ls
    // Positive Test Case
    // cat `ls` => cat A.txt
    @Test
    void catLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Environment.setCurrentDirectory(nestDirPath.toString());
        String[] args = {CAT_CMD, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
        Environment.setCurrentDirectory(folderPath.toString());
    }

    // cat + ls
    // Negative Test Case
    @Test
    void catLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // cat + rm
    // Positive Test Case
    // cat should run as per normal and rm should remove rm.txt
    // cat `rm remove.txt` A.txt => cat A.txt
    @Test
    void catRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(rmFilePath, FILE_ONE_CONTENT);
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // cat + rm
    // Negative Test Case
    // cat `rm remove.txt` remove.txt => cat remove.txt
    // Cat will not run as remove.txt will be removed by the command substitution
    @Test
    void catRmCommand_fileRemovedInSubCommand_shouldThrowCatException() throws Exception {
        Files.writeString(rmFilePath, FILE_ONE_CONTENT);
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_RM_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // cat + rm
    // Negative Test Case
    // cat `rm invalid.txt` => rm invalid.txt
    // Error thrown in the sub command as invalid.txt does not exist
    @Test
    void catRmCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cat + sort
    // Positive Test Case
    // cat `sort sort.txt` => cat A.txt B.txt
    @Test
    void catSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // cat + sort
    // Negative Test Case: invalid.txt does not exist
    // cat `sort invalid.txt` => sort invalid.txt
    @Test
    void catSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cat + sort
    // Negative Test Case: file sorted does not contain file name
    // cat `sort A.txt` => sort A.txt
    @Test
    void catSortCommand_invalidPathFromSubCommand_shouldThrowCatException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
    }

    // cat + tee
    // Positive Test Case
    // cat `tee tee.txt` => cat A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void catTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        Path teeFilePath = folderPath.resolve(FILE_TEE_NAME);

        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
        assertEquals(FILE_ONE_NAME, getFileContent(teeFilePath));
        deleteFileIfExists(teeFilePath);
    }

    // cat + tee
    // Positive Test Case
    // cat `tee` => cat A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    // Tee command will return A.txt which will be passed to Grep
    @Test
    void catTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {CAT_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CAT_OUT, testOutputStream.toString());
    }

    // cat + wc
    // Negative Test Case
    // cat `wc A.txt` => cat 0 3 13 A.txt
    // It is a valid sub command, however CatException is thrown as the numbers (0, 3, 13) are invalid file names.
    @Test
    void catWcCommand_validSubCommand_shouldThrowCatException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
    }

    // cat + wc
    // Negative Test Case
    @Test
    void catWcCommand_invalidOuterCommand_shouldThrowCatException() {
        String[] args = {CAT_CMD, INV_FLAG, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, ERR_INVALID_FLAG, "!"), thrown.getMessage());
    }

    // cat + mv
    // Positive Test Case
    // cat `mv mv1.txt mv2.txt` mv.txt => mv mv1.txt mv2.txt then cat mv.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void catMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CAT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String expectedResult = FILE_MV1_NAME + STRING_NEWLINE + FILE_MV2_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
        assertFalse(Files.exists(folderPath.resolve(FILE_MV1_NAME)));
        assertEquals(getFileContent(folderPath.resolve(FILE_MV2_NAME)), FILE_ONE_CONTENT);
    }

    // cat + mv
    // Positive Test Case
    // cat `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then cat mv.txt
    // mv1.txt should be moved into mv-folder
    @Test
    void catMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CAT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String expectedResult = FILE_MV1_NAME + STRING_NEWLINE + FILE_MV2_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
        assertFalse(Files.exists(folderPath.resolve(FILE_MV1_NAME)));
        assertTrue(Files.exists(folderPath.resolve(DIR_MV_NAME).resolve(FILE_MV1_NAME))); // mv1.txt moved into mv-folder
    }

    // cat + mv
    // Negative Test Case
    // cat `mv mv1.txt mv2.txt` mv1.txt => mv mv1.txt mv2.txt
    // Error as mv1.txt has already been merged into mv2.txt
    @Test
    void catMvCommand_filesMergedSubCommand_shouldThrowCatException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CAT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cat + mv
    // Negative Test Case
    // cat `mv mv1.txt mv-folder` mv1.txt => mv mv1.txt mv-folder then cat mv1.txt
    // mv1.txt should be moved into mv-folder, failing the cat execution
    @Test
    void catMvCommand_fileAndDirectorySubCommand_shouldThrowCatException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CAT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cat + mv
    // Negative Test Case
    @Test
    void catMvCommand_invalidOuterCommand_shouldThrowCatException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CAT_CMD, INV_FLAG, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, ERR_INVALID_FLAG, "!"), thrown.getMessage());
    }

    // cat + uniq
    // Positive Test Case
    // uniq.txt = A.txt A.txt B.txt B.txt A.txt
    // Should cat A.txt B.txt A.txt
    @Test
    void catUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_CONTENT + STRING_NEWLINE + FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // cat + uniq
    // Positive Test Case
    // System.in = A.txt A.txt B.txt B.txt A.txt
    // Should cat A.txt B.txt A.txt
    @Test
    void catUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {CAT_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_CONTENT + STRING_NEWLINE + FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // cat + uniq
    // Negative Test Case: invalid.txt does not exist
    // cat `uniq invalid.txt`
    @Test
    void catUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cat + uniq
    // Negative Test Case: arg is a folder
    // cat `uniq nest`
    @Test
    void catUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // cat + uniq
    // Negative Test Case
    @Test
    void catUniqCommand_invalidOuterCommand_shouldThrowCatException() {
        String[] args = {CAT_CMD, INV_FLAG, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, ERR_INVALID_FLAG, "!"), thrown.getMessage());
    }

    // cat + paste
    // Positive Test Case
    // cat `paste paste.txt` => cat A.txt B.txt
    @Test
    void catPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // cat + paste
    // Negative Test Case
    @Test
    void catPasteCommand_invalidOuterCommand_shouldThrowCatException() {
        String[] args = {CAT_CMD, INV_FLAG, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, ERR_INVALID_FLAG, "!"), thrown.getMessage());
    }

    // cat + paste
    // Negative Test Case
    // cat `paste invalid.txt` => paste invalid.txt
    @Test
    void catPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {CAT_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cat + unknown
    // Negative Test Case
    @Test
    void catInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {CAT_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
