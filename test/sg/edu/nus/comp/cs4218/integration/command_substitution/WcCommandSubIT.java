package sg.edu.nus.comp.cs4218.integration.command_substitution;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
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

public class WcCommandSubIT {
    private static final String TOTAL = "total";
    private static final String MULTI_CONTENT = "B.txt\nA.txt";
    private static final String WC_OUT = String.format(NUMBER_FORMAT, 0) + String.format(NUMBER_FORMAT, 3) + String.format(NUMBER_FORMAT, 13);
    private static final String WC_OUT_FILE = WC_OUT + TAB + "%s" + STRING_NEWLINE;
    private static final String PASTE_CONT = FILE_ONE_NAME + "\n" + FILE_TWO_NAME;
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_ONE_NAME;

    @TempDir
    public static Path folderPath;
    private static Path rmFilePath, cpFilePath, pasteFilePath, nestDirPath, uniqFilePath;
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
        nestDirPath = folderPath.resolve(NEST_DIR);
        Files.createDirectories(nestDirPath);

        // File: nest/file.txt, File Content: "I love CS4218"
        Path dirPath = nestDirPath.resolve(FILE_ONE_NAME);
        Files.writeString(dirPath, FILE_ONE_CONTENT);

        // File: sort.txt, File Content: Multiline text
        Path pathSortFile = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(pathSortFile, MULTI_CONTENT);

        // File Name: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // File: uniq.txt, File Content: Multiline text
        uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        Files.writeString(uniqFilePath, DUP_CONT);

        // File Name: paste.txt
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
        deleteFileIfExists(cpFilePath);
        deleteFileIfExists(uniqFilePath);
        deleteFileIfExists(pasteFilePath);
        testOutputStream.reset();
        outputCapture.reset();
    }

    private String generateWcOutputString(int num1, int num2, int num3, String fileName) {
        return String.format(NUMBER_FORMAT, num1) + String.format(NUMBER_FORMAT, num2) + String.format(NUMBER_FORMAT, num3) + TAB + fileName;
    }

    // wc + cat
    // Positive Test Case
    // wc `cat file.txt` => wc A.txt
    @Test
    void wcCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME), testOutputStream.toString());
    }

    // wc + cat
    // Negative Test Case
    // wc `cat invalid.txt` => cat invalid.txt
    @Test
    void wcCatCommand_invalidFileNameSubCommand_shouldThrowCatException() {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // wc + cat
    // Negative Test Case: cat A.txt produce "I love CS4218" and wc treats the string inputs as file names which cannot be evaluated
    // wc `cat A.txt` => wc "I love CS4218"
    @Test
    void wcCatCommand_invalidContentSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(generateWcOutputString(0, 0, 0, TOTAL) + STRING_NEWLINE, testOutputStream.toString());
    }

    // wc + cd
    // Positive Test Case
    // wc `cd nest` A.txt => wc nest/A.txt
    @Test
    void wcCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME), testOutputStream.toString());
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // wc + cd
    // Negative Test Case
    // wc `cd invalid` A.txt => cd invalid
    @Test
    void wcCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // wc + cp
    // Positive Test Case
    // wc `cp A.txt cp.txt` cp.txt => wc cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void wcCpCommand_noExistingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        assertFalse(Files.exists(cpFilePath));
        String[] args = {WC_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_CP_NAME), testOutputStream.toString());
    }

    // wc + cp
    // Positive Test Case: cp.txt already have existing content, overwritten
    // wc `cp A.txt cp.txt` cp.txt => wc cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void wcCpCommand_existingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {WC_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_CP_NAME), testOutputStream.toString());
    }

    // wc + cut
    // Positive Test Case
    // wc `cut -b 1-6 file.txt` => wc A.txt
    @Test
    void wcCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME), testOutputStream.toString());
    }

    // wc + cut
    // Negative Test Case: missing flag
    // wc `cut 1-6 file.txt` => cut 1-6 file.txt
    @Test
    void wcCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {WC_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // wc + echo
    // Positive Test Case
    // wc `echo A.txt` => wc A.txt
    @Test
    void wcEchoCommand_echoFileNameSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME), testOutputStream.toString());
    }

    // wc + echo
    // Positive Test Case: input the string via stdin
    // wc `echo` => wc
    @Test
    void wcEchoCommand_echoEmptySubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(SINGLE_STRING, ECHO_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_CONTENT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(WC_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // wc + exit
    // wc `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will turn into input stream)
    @Test
    void wcExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {WC_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // wc + grep
    // Positive Test Case
    // wc `grep A.txt file.txt` => wc A.txt
    @Test
    void wcGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME), testOutputStream.toString());
    }

    // wc + grep
    // Negative Test Case: invalid pattern
    // wc `grep CS4218\\ file.txt` => grep CS4218\\ file.txt
    @Test
    void wcGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {WC_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // wc + ls
    // Positive Test Case
    // wc `ls nest` => wc A.txt
    @DisabledOnOs(OS.WINDOWS)
    @Test
    void wcLsCommand_lsSubDirSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, LS_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = WC_OUT + TAB + FILE_ONE_NAME + STRING_NEWLINE + WC_OUT + TAB + TOTAL + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // wc + ls
    // Positive Test Case: wc everything in the test folder
    // wc `ls` => wc
    @Test
    void wcLsCommand_lsAllSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = WC_OUT + TAB + FILE_ONE_NAME + STRING_NEWLINE
                + generateWcOutputString(0, 1, 6, FILE_TWO_NAME) + STRING_NEWLINE
                + generateWcOutputString(0, 1, 5, FILE_FILE_NAME) + STRING_NEWLINE
                + generateWcOutputString(0, 0, 0, NEST_DIR) + STRING_NEWLINE
                + generateWcOutputString(1, 2, 11, FILE_SORT_NAME) + STRING_NEWLINE
                + generateWcOutputString(1, 7, 35, TOTAL) + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // wc + ls
    // Negative Test Case
    @Test
    void wcLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // wc + rm
    // Positive Test Case
    // wc CS4218 `rm remove.txt` A.txt => wc A.txt
    @Test
    void wcRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME), testOutputStream.toString()); // wc should run as per normal
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // wc + rm
    // Negative Test Case
    // wc `rm remove.txt` remove.txt => wc remove.txt
    // WC will not run as remove.txt will be removed by the command substitution
    @Test
    void wcRmCommand_validSubCommand_shouldOutputErrorMessage() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_EXCEP, FILE_RM_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // wc + rm
    // Negative Test Case
    // wc `rm invalid.txt` remove.txt => wc invalid.txt
    @Test
    void wcRmCommand_invalidSubCommand_shouldOutputErrorMessage() {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // wc + sort
    // Positive Test Case
    // wc `sort sort.txt` => wc A.txt B.txt
    @Test
    void wcSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = WC_OUT + TAB + FILE_ONE_NAME + STRING_NEWLINE
                + generateWcOutputString(0, 1, 6, FILE_TWO_NAME) + STRING_NEWLINE
                + generateWcOutputString(0, 4, 19, TOTAL) + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // wc + sort
    // Negative Test Case: invalid.txt does not exist
    // wc `sort invalid.txt` A.txt => sort invalid.txt
    @Test
    void wcSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // wc + tee
    // Positive Test Case
    // wc `tee tee.txt` => wc A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void wcTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME), testOutputStream.toString());
    }

    // wc + tee
    // Positive Test Case
    // wc `tee` => wc A.txt
    // System.in : A.txt (this will be output)
    @Test
    void wcTeeCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME), testOutputStream.toString());
    }

    // wc + wc
    // Positive Test Case
    // The numbers are ignored (as wc does not throw error for invalid files, only stdout error message) until it takes in A.txt as input
    // wc `wc A.txt` => wc A.txt
    @Test
    void wcWcCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = WC_OUT + TAB + FILE_ONE_NAME + STRING_NEWLINE +
                WC_OUT + TAB + TOTAL + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // cat + mv
    // Positive Test Case
    // cat `mv mv1.txt mv2.txt` mv.txt => mv mv1.txt mv2.txt then cat mv.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void wcMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {WC_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(Files.exists(mv1Path)); // File merged into mv2.txt
        assertEquals(String.format(WC_OUT_FILE, FILE_MV2_NAME), testOutputStream.toString());
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // wc + mv
    // Negative Test Case
    // wc `mv mv1.txt mv2.txt` mv1.txt => wc mv1.txt
    // Error as mv1.txt has already been merged into mv2.txt
    @Test
    void wcMvCommand_filesMergedSubCommand_shouldOutputErrorMessage() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {WC_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_EXCEP, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
    }

    // wc + mv
    // Negative Test Case
    // cat `mv mv1.txt mv-folder` mv1.txt => mv mv1.txt mv-folder then wc mv1.txt
    // mv1.txt should be moved into mv-folder, failing the wc execution
    @Test
    void wcMvCommand_fileAndDirectorySubCommand_shouldOutputErrorMessage() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {WC_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_EXCEP, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
    }

    // wc + uniq
    // Positive Test Case
    // uniq.txt = A.txt A.txt B.txt B.txt A.txt
    // Should wc CS4218 from A.txt B.txt A.txt
    @Test
    void wcUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(uniqFilePath, DUP_CONT);
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME) + generateWcOutputString(0, 1, 6, FILE_TWO_NAME) + STRING_NEWLINE
                + String.format(WC_OUT_FILE, FILE_ONE_NAME) + generateWcOutputString(0, 7, 32, TOTAL) + STRING_NEWLINE, testOutputStream.toString());
    }

    // wc + uniq
    // Positive Test Case
    // System.in = A.txt A.txt B.txt B.txt A.txt
    // Should wc CS4218 from A.txt B.txt A.txt
    @Test
    void wcUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {WC_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_OUT_FILE, FILE_ONE_NAME) + generateWcOutputString(0, 1, 6, FILE_TWO_NAME) + STRING_NEWLINE
                + String.format(WC_OUT_FILE, FILE_ONE_NAME) + generateWcOutputString(0, 7, 32, TOTAL) + STRING_NEWLINE, testOutputStream.toString());
    }

    // wc + uniq
    // Negative Test Case: invalid.txt does not exist
    // wc `uniq invalid.txt`
    @Test
    void wcUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // wc + uniq
    // Negative Test Case: arg is a folder
    // wc `uniq nest`
    @Test
    void wcUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // wc + uniq
    // Negative Test Case
    // System.in = A.txt A.txt B.txt B.txt A.txt
    // Should wc -x from A.txt B.txt A.txt
    @Test
    void wcUniqCommand_invalidOuterCommand_shouldThrowWcException() {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        String[] args = {WC_CMD, "-x", String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(WcException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(WC_EXCEP_DIR, ERR_INVALID_FLAG), thrown.getMessage());
    }

    // wc + paste
    // Positive Test Case
    // wc `paste paste.txt` => wc A.txt B.txt
    @Test
    void wcPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(pasteFilePath, PASTE_CONT);
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(WC_OUT + TAB + FILE_ONE_NAME + STRING_NEWLINE
                + generateWcOutputString(0, 1, 6, FILE_TWO_NAME) + STRING_NEWLINE
                + generateWcOutputString(0, 4, 19, TOTAL) + STRING_NEWLINE, testOutputStream.toString());
    }

    // wc + paste
    // Negative Test Case
    // wc `paste invalid.txt` => wc invalid.txt
    @Test
    void wcPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {WC_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // wc + paste
    // Negative Test Case
    // wc -x `paste paste.txt`
    @Test
    void wcPasteCommand_invalidOuterCommand_shouldThrowWcException() throws Exception {
        Files.writeString(pasteFilePath, PASTE_CONT);
        String[] args = {WC_CMD, "-x", String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(WcException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(WC_EXCEP_DIR, ERR_INVALID_FLAG), thrown.getMessage());
    }

    // wc + unknown
    // Negative Test Case
    @Test
    void wcInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {WC_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
