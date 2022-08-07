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

public class LsCommandSubIT {
    private static final String MULTI_CONTENT = "nest\nA.txt";
    private static final String LS_OUT_WITH_CP = FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE
            + FILE_CP_NAME + STRING_NEWLINE + FILE_FILE_NAME + STRING_NEWLINE
            + NEST_DIR + STRING_NEWLINE + FILE_SORT_NAME + STRING_NEWLINE;
    private static final String LS_OUT_1 = FILE_ONE_NAME + STRING_NEWLINE;
    private static final String LS_OUT_1_DIR = NEST_DIR + ":" + STRING_NEWLINE + LS_OUT_1;
    private static final String PASTE_CONT = NEST_DIR;
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME;

    @TempDir
    public static Path folderPath;
    private static Path rmFilePath;
    private static Path nestDirPath;
    private static Path cpFilePath;
    private static Path teeFilePath;
    private static Path pasteFilePath;
    private static Path uniqFilePath;
    private static ByteArrayOutputStream outCapture, testOutputStream;
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
        outCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outCapture));

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

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);

        // File: remove.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // File: tee.txt
        teeFilePath = folderPath.resolve(FILE_TEE_NAME);

        // File: paste.txt
        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);

        // File: uniq.txt
        uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
    }

    @AfterAll
    public static void tearDown() {
        deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    @AfterEach
    public void reset() throws IOException {
        deleteMvTestFiles();
        deleteFileIfExists(teeFilePath);
        deleteFileIfExists(cpFilePath);
        deleteFileIfExists(pasteFilePath);
        deleteFileIfExists(uniqFilePath);
        testOutputStream.reset();
        outCapture.reset();
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // ls + cat
    // Positive Test Case
    // ls `cat file.txt` => ls A.txt
    @Test
    void lsCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1, testOutputStream.toString());
    }

    // ls + cat
    // Negative Test Case: invalid.txt does not exist
    // ls `cat invalid.txt` => ls invalid.txt
    @Test
    void lsCatCommand_invalidFileNameInvalidSubCommand_shouldThrowCatException() {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // ls + cd
    // Positive Test Case
    // ls `cd nest` => ls nest
    @Test
    void lsCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1, testOutputStream.toString());
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // ls + cd
    // Negative Test Case: The folder/directory "invalid" does not exist
    // ls `cd invalid` => cd invalid
    @Test
    void lsCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // ls + cp
    // Positive Test Case: ls the current directory while cp A.txt cp.txt in sub command
    // ls `cp A.txt cp.txt` => ls (cp command created cp.txt)
    @Test
    void lsCpCommand_noExistingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_WITH_CP, testOutputStream.toString());
        assertTrue(Files.exists(cpFilePath)); // Check that cp.txt is created
        assertEquals(getFileContent(cpFilePath), FILE_ONE_CONTENT); // Check that cp.txt content is same as A.txt
    }

    // ls + CP
    // Positive Test Case: cp.txt is an existing file with content, overwritten by cp
    // ls CS4218 `cp A.txt cp.txt` => ls
    @Test
    void lsCpCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_WITH_CP, testOutputStream.toString());
        assertTrue(Files.exists(cpFilePath)); // Check that cp.txt is created
        assertEquals(FILE_ONE_CONTENT, getFileContent(cpFilePath)); // Check that cp.txt content is same as A.txt
    }

    // ls + cut
    // Positive Test Case
    // ls `cut -b 1-6 file.txt` => ls A.txt
    @Test
    void lsCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1, testOutputStream.toString());
    }

    // ls + cut
    // Negative Test Case: missing flag
    // ls `cut 1-6 file.txt` => cut 1-6 file.txt
    @Test
    void lsCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {LS_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // ls + echo
    // Positive Test Case
    // ls `echo nest` => ls nest/
    @Test
    void lsEchoCommand_echoValidDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, ECHO_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1_DIR, testOutputStream.toString());
    }

    // ls + echo
    // Positive Test Case
    // ls `echo` => ls
    @Test
    void lsEchoCommand_echoEmptySubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(SINGLE_STRING, ECHO_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE
                + FILE_FILE_NAME + STRING_NEWLINE + NEST_DIR + STRING_NEWLINE
                + FILE_SORT_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // ls + echo
    // Negative Test Case: directory does not exist
    // ls `echo invalid` => ls invalid
    @Test
    void lsEchoCommand_echoInvalidDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, ECHO_CMD, DIR_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // ls + exit
    // Positive Test Case
    // ls `exit` => Program should exit
    @Test
    void lsExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {LS_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // ls + grep
    // Positive Test Case
    // ls `grep A.txt file.txt` => ls A.txt
    @Test
    void lsGrepCommand_grepFileNameSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1, testOutputStream.toString());
    }

    // ls + grep
    // Negative Test Case: grep does not match any keyword, ls everything
    // ls `grep A.txt A.txt` => ls
    @Test
    void lsGrepCommand_grepEmptySubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE
                + FILE_FILE_NAME + STRING_NEWLINE + NEST_DIR + STRING_NEWLINE
                + FILE_SORT_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // ls + grep
    // Negative Test Case: missing pattern
    // ls `grep A.txt file.txt` => ls A.txt
    @Test
    void lsGrepCommand_invalidSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // ls + ls
    // Positive Test Case
    // ls `ls nest` => ls A.txt
    @DisabledOnOs(OS.WINDOWS)
    @Test
    void lsLsCommand_lsNestSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, LS_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1, testOutputStream.toString());
    }

    // ls + ls
    // Positive Test Case
    // ls `ls` => ls
    @Test
    void lsLsCommand_lsSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE
                + FILE_FILE_NAME + STRING_NEWLINE + FILE_SORT_NAME + STRING_NEWLINE
                + STRING_NEWLINE + NEST_DIR + ":" + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // ls + ls
    // Negative Test Case
    @Test
    void lsLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // ls + rm
    // Positive Test Case
    // ls `rm remove.txt` => ls
    @Test
    void lsRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(rmFilePath, FILE_ONE_CONTENT);
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE
                + FILE_FILE_NAME + STRING_NEWLINE + NEST_DIR + STRING_NEWLINE + FILE_SORT_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString()); // ls result should not contain remove.txt
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // ls + rm
    // Positive Test Case
    // ls `rm remove.txt` remove.txt => ls remove.txt
    @Test
    void lsRmCommand_lsRmFileNameSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(rmFilePath, FILE_ONE_CONTENT);
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString()); // ls result should not contain remove.txt
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // ls + rm
    // Negative Test Case: invalid.txt does not exist
    // ls `rm invalid.txt` => invalid.txt
    @Test
    void lsRmCommand_lsSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // ls + sort
    // Positive test case
    // ls `sort sort.txt` => ls A.txt
    @Test
    void lsSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_NAME + STRING_NEWLINE + STRING_NEWLINE + NEST_DIR + ":" + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
    }

    // ls + sort
    // Negative Test Case: invalid.txt does not exist
    // ls `sort invalid.txt` => sort invalid.txt
    @Test
    void lsSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // ls + tee
    // Positive Test Case
    // ls `tee tee.txt` => ls A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void lsTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {LS_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1, testOutputStream.toString());
        assertEquals(FILE_ONE_NAME, getFileContent(teeFilePath));
    }

    // ls + tee
    // Positive Test Case
    // ls `tee` => ls nest
    // System.in : nest (this will be outputted)
    // Tee command will return nest which will be passed to ls
    @Test
    void lsTeeCommand_stdinDirectoryValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(NEST_DIR.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {LS_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1_DIR, testOutputStream.toString());
        deleteFileIfExists(folderPath.resolve(FILE_TEE_NAME));
    }

    // ls + wc
    // Positive Test Case
    // The numbers are ignored (as Grep does not throw error for invalid files, only stdout error message) until it takes in A.txt as input
    // ls `wc nest` => ls 0 0 0 nest
    @Test
    void lsWcCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, WC_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1_DIR, testOutputStream.toString());
    }

    // ls + mv
    // Positive Test Case
    // ls `mv mv1.txt mv2.txt` A.txt => mv mv1.txt mv2.txt then ls mv2.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void lsMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(FILE_MV2_NAME + STRING_NEWLINE, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // ls + mv
    // Positive Test Case
    // ls `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then ls mv-folder
    // mv1.txt should be moved into mv-folder
    @Test
    void lsMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), DIR_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(DIR_MV_NAME + ":" + STRING_NEWLINE + FILE_MV1_NAME + STRING_NEWLINE, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME))); // uniq1.txt moved into mv-folder
    }

    // ls + mv
    // Negative Test Case
    // ls `mv mv1.txt mv2.txt` mv1.txt => mv mv1.txt mv2.txt then ls mv1.txt
    // Empty as mv1.txt has already been merged into mv2.txt
    @Test
    void lsMvCommand_invalidSubCommand_shouldOutputEmptyLine() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // ls + mv
    // Negative Test Case
    // ls `mv mv1.txt mv-folder` uniq1.txt => mv mv1.txt mv-folder then ls uniq1.txt
    // mv1.txt should be moved into mv-folder, failing the ls execution
    @Test
    void lsMvCommand_fileAndDirectorySubCommand_shouldOutputEmptyLine() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {LS_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // ls + uniq
    // Positive Test Case
    // ls.txt = A.txt B.txt
    // ls A.txt B.txt
    @Test
    void lsUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(uniqFilePath, DUP_CONT);
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    // ls + uniq
    // Positive Test Case
    // System.in = A.txt B.txt
    // ls A.txt B.txt
    @Test
    void lsUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {LS_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    // ls + uniq
    // Negative Test Case: invalid.txt does not exist
    // ls `uniq invalid.txt`
    @Test
    void lsUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // ls + uniq
    // Negative Test Case: arg is a folder
    // ls `uniq nest`
    @Test
    void lsUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // ls + paste
    // Positive Test Case
    // ls `paste paste.txt` => uniq A.txt B.txt
    @Test
    void lsPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(pasteFilePath, PASTE_CONT);
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(LS_OUT_1_DIR, testOutputStream.toString());
    }

    // ls + paste
    // Negative Test Case
    // ls `paste invalid.txt` => paste invalid.txt
    @Test
    void lsPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {LS_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // ls + unknown
    // Negative Test Case
    @Test
    void lsInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {LS_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
