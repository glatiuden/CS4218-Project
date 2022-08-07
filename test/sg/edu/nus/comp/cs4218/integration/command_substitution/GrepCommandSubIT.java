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
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteAll;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.getFileContent;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class GrepCommandSubIT {
    private static final String MULTI_CONTENT = "B.txt\nA.txt";
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_ONE_NAME;
    private static final String PASTE_CONT = FILE_ONE_NAME + "\n" + FILE_TWO_NAME;
    private static final String GREP_OUT = FILE_ONE_CONTENT + STRING_NEWLINE;
    private static final String GREP_OUT_ABA = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE + FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE;
    private static final String GREP_OUT_AB = FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE;

    @TempDir
    public static Path folderPath;
    private static Path rmFilePath, nestDirPath;
    private static Path pasteFilePath, cpFilePath;
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

        // File: uniq.txt, File Content: Multiline text
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        Files.writeString(uniqFilePath, DUP_CONT);

        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);
        Files.writeString(pasteFilePath, PASTE_CONT);

        cpFilePath = folderPath.resolve(FILE_CP_NAME);
        rmFilePath = folderPath.resolve(FILE_RM_NAME);
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

    // grep + cat
    // Positive Test Case: cat file.txt will output A.txt which will be used by grep
    // grep CS4218 `cat file.txt` => grep CS418 A.txt
    @Test
    void grepCatCommand_catFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
    }

    // grep + cat
    // Positive Test Case: cat B.txt will output CS4218 which will be used by grep as keyword
    // grep `cat B.txt` A.txt => grep CS418 A.txt
    @Test
    void grepCatCommand_catKeywordValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_TWO_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
    }

    // grep + cat
    // Negative Test Case: invalid.txt does not exist
    // grep `cat invalid.txt` A.txt => cat invalid.txt
    @Test
    void grepCatCommand_invalidFileNameInvalidSubCommand_shouldThrowCatException() {
        String[] args = {GREP_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // grep + CD
    // Positive Test Case: cd into /nest (which already have A.txt in the subfolder) then execute grep
    // grep CS4218 `cd nest` A.txt => grep CS4218 nest/A.txt
    @Test
    void grepCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // grep + CD
    // Negative Test Case: The folder/directory "invalid" does not exist
    // grep CS4218 `cd invalid` A.txt => cd invalid
    @Test
    void grepCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // grep + CP
    // Positive Test Case: cp.txt is created by cp command
    // grep CS4218 `cp A.txt cp.txt` cp.txt => grep CS4218 cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void grepCpCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        assertFalse(Files.exists(cpFilePath));
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
        assertTrue(Files.exists(cpFilePath));
        assertEquals(getFileContent(cpFilePath), FILE_ONE_CONTENT);
    }

    // grep + CP
    // Positive Test Case: cp.txt is an existing file with content, overwritten by cp
    // grep CS4218 `cp A.txt cp.txt` cp.txt => grep CS4218 cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void grepCpCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
        assertEquals(getFileContent(cpFilePath), FILE_ONE_CONTENT);
    }

    // grep + cut
    // Positive Test Case: cut the file name (A.txt) from file.txt
    // grep CS4218 `cut -b 1-6 file.txt` => grep CS4218 A.txt
    @Test
    void grepCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
    }

    // grep + cut
    // Negative Test Case: missing flag
    // Unable to test invalid file from Cut as the error message is output using stdout, which the input stream will not close.
    // grep CS4218 `cut 1-6 invalid.txt` => cut 1-6 invalid.txt
    @Test
    void grepCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format("`%s 1-6 %s`", CUT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // grep + echo
    // Positive Test Case
    // grep `echo CS4218` A.txt => grep CS4218 A.txt
    @Test
    void grepEchoCommand_echoKeywordValidSubCommand_ShouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, String.format(DOUBLE_STRING, ECHO_CMD, GREP_PATTERN), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
    }

    // grep + echo
    // Positive Test Case
    // grep CS4218 `echo A.txt` => grep CS4218 A.txt
    @Test
    void grepEchoCommand_echoFileNameValidSubCommand_ShouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
    }

    // grep + exit
    // Positive Test Case
    // grep CS4218 `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will turn into input stream)
    @Test
    void grepExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // grep + grep
    // Positive Test Case
    // grep `grep CS4218 B.txt` A.txt => grep CS4218 A.txt
    @Test
    void grepGrepCommand_grepKeywordValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_TWO_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
    }

    // grep + grep
    // Positive Test Case
    // grep CS4218 `grep A.txt file.txt` => grep CS4218 A.txt
    @Test
    void grepGrepCommand_grepFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
    }

    // grep + grep
    // Negative Test Case: Invalid pattern
    // grep CS4218 `grep CS4218\\ file.txt` => grep CS4218\\ file.txt
    @Test
    void grepGrepCommand_invalidPatternInvalidSubCommand_shouldThrowGrepException() {
        String[] args = {GREP_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_TWO_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // grep + LS
    // Positive Test Case: ls (in nest) => A.txt (in the nest folder)
    // grep CS4218 `ls` => grep CS4218 A.txt
    @Test
    void grepLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Environment.setCurrentDirectory(nestDirPath.toString());
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
        Environment.setCurrentDirectory(folderPath.toString());
    }

    // grep + ls
    // Negative Test Case
    @Test
    void grepLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // grep + rm
    // Positive Test Case
    // grep CS4218 `rm remove.txt` A.txt => grep CS4218 A.txt
    @Test
    void grepRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(rmFilePath, FILE_ONE_CONTENT);

        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(GREP_OUT, testOutputStream.toString()); // grep should run as per normal
        assertFalse(rmFilePath.toFile().exists()); // remove.txt should be removed
    }

    // grep + rm
    // Negative Test Case: attempt to grep a file that was removed by command substitution
    // grep CS4218 `rm remove.txt` remove.txt => grep CS4218 remove.txt
    @Test
    void grepRmCommand_rmFileInvalidSubCommand_shouldNotEvaluate() throws Exception {
        Files.writeString(rmFilePath, FILE_ONE_CONTENT);
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(GREP_EXCEP_DIR, FILE_RM_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // grep + rm
    // Negative Test Case: attempt to grep a file that was removed by command substitution
    // grep CS4218 `rm remove.txt` remove.txt => grep CS4218 remove.txt
    @Test
    void grepRmCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // grep + sort
    // Positive Test Case
    // grep CS4218 `sort sort.txt` A.txt => grep CS4218 A.txt
    @Test
    void grepSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT_AB, testOutputStream.toString());
    }

    // grep + sort
    // Negative Test Case: invalid.txt does not exist
    // grep CS4218 `sort invalid.txt` A.txt => sort invalid.txt
    @Test
    void grepSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // grep + tee
    // Positive Test Case
    // grep `tee tee.txt` => grep A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void grepTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
        assertEquals(FILE_ONE_NAME, getFileContent(folderPath.resolve(FILE_TEE_NAME)));
    }

    // grep + tee
    // Positive Test Case
    // grep `tee` => grep A.txt
    // System.in : A.txt (this will be outputted)
    // Tee command will return A.txt which will be passed to Grep
    @Test
    void grepTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {GREP_CMD, GREP_PATTERN, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT, testOutputStream.toString());
    }

    // grep + wc
    // Positive Test Case
    // The numbers are ignored (as Grep does not throw error for invalid files, only stdout error message) until it takes in A.txt as input
    // grep `wc A.txt` => grep 0 3 13 A.txt
    @Test
    void grepWcCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // cat + mv
    // Positive Test Case
    // cat `mv mv1.txt mv2.txt` mv.txt => mv mv1.txt mv2.txt then cat mv.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void grepMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(Files.exists(mv1Path)); // File merged into mv2.txt
        assertEquals(GREP_OUT, testOutputStream.toString());
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // cat + mv
    // Negative Test Case
    // grep CS4218\\ `mv mv1.txt mv2.txt` mv2.txt => mv mv1.txt mv2.txt then grep CS4218\\ mv2.txt
    // mv1.txt should be merged into mv2.txt, then failed the outer command.
    @Test
    void grepMvCommand_invalidOuterCommand_shouldOutputErrorMessage() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {GREP_CMD, INVALID_PATTERN, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
        assertFalse(Files.exists(mv1Path)); // File merged into mv2.txt
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // grep + mv
    // Negative Test Case
    // grep `mv mv1.txt mv2.txt` mv1.txt => grep mv1.txt mv2.txt
    // Error as mv1.txt has already been merged into mv2.txt
    @Test
    void grepMvCommand_filesMergedSubCommand_shouldThrowCatException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(GREP_EXCEP_DIR, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
    }

    // grep + mv
    // Negative Test Case
    // cat `mv mv1.txt mv-folder` mv1.txt => mv mv1.txt mv-folder then grep CS4218 mv1.txt
    // mv1.txt should be moved into mv-folder, failing the grep execution
    @Test
    void grepMvCommand_fileAndDirectorySubCommand_shouldOutputErrorMessage() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(GREP_EXCEP_DIR, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
    }

    // grep + uniq
    // Positive Test Case
    // uniq.txt = A.txt A.txt B.txt B.txt A.txt
    // Should grep CS4218 from A.txt B.txt A.txt
    @Test
    void grepUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT_ABA, testOutputStream.toString());
    }

    // grep + uniq
    // Positive Test Case
    // System.in = A.txt A.txt B.txt B.txt A.txt
    // Should grep CS4218 from A.txt B.txt A.txt
    @Test
    void grepUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {GREP_CMD, GREP_PATTERN, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT_ABA, testOutputStream.toString());
    }

    // grep + uniq
    // Negative Test Case: invalid.txt does not exist
    // grep `uniq invalid.txt`
    @Test
    void grepUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // grep + uniq
    // Negative Test Case: arg is a folder
    // grep `uniq nest`
    @Test
    void grepUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // grep + uniq
    // Negative Test Case
    // System.in = A.txt A.txt B.txt B.txt A.txt
    // Should grep CS4218\\ from A.txt B.txt A.txt
    @Test
    void grepUniqCommand_invalidOuterCommand_shouldThrowGrepExcetion() {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {GREP_CMD, INVALID_PATTERN, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // grep + paste
    // Positive Test Case
    // cat `paste paste.txt` => cat A.txt B.txt
    @Test
    void grepPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(GREP_OUT_AB, testOutputStream.toString());
    }

    // grep + paste
    // Negative Test Case
    // grep `paste invalid.txt` => paste invalid.txt
    @Test
    void grepPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {GREP_CMD, GREP_PATTERN, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // grep + paste
    // Negative Test Case
    // grep CS4218\\ `paste invalid.txt` => grep CS4218\\ A.txt B.txt
    @Test
    void grepPasteCommand_invalidOuterCommand_shouldThrowGrepException() {
        String[] args = {GREP_CMD, INVALID_PATTERN, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // grep + unknown
    // Negative Test Case
    @Test
    void grepInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {INVALID_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
