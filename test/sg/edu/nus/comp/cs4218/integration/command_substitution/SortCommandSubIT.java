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
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class SortCommandSubIT {
    private static final String NORM_NEWLINE = "\n";
    private static final String MULTI_LINE_CONT = "\n54321\n\n  \n  spaced\na123!@#random\n!!!\nb\nAAA 123random\nBBB\n@@\n1123\n1\n55";
    private static final String SORT_MULTI = "\n\n  \n  spaced\n!!!\n@@\n1\n1123\n54321\n55\nAAA 123random\nBBB\na123!@#random\nb";
    private static final String SORT_MULTI_1 = "\n\n  \n  spaced\n!!!\n@@\n1\n1123\n54321\n55\nAAA 123random\nBBB\nCS4218\na123!@#random\nb";
    private static final String SORT_OUT = SORT_MULTI.replaceAll(NORM_NEWLINE, STRING_NEWLINE) + STRING_NEWLINE;
    private static final String PASTE_CONT = FILE_TWO_NAME + "\n" + FILE_ONE_NAME;
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME;

    @TempDir
    public static Path folderPath;
    private static Path nestDirPath, cpFilePath, pasteFilePath;
    private static ByteArrayOutputStream outCapture, testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;
    private static Path rmFilePath;

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
        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(path1, MULTI_LINE_CONT);

        // File: B.txt, File Content: "CS4218"
        Path path2 = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(path2, FILE_TWO_CONTENT);

        // File: file.txt, File Content: "A.txt"
        Path filePath = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(filePath, FILE_ONE_NAME);

        // File: sort.txt, File Content: "A.txt"
        Path sortFilePath = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(sortFilePath, FILE_ONE_NAME);

        // ./nest
        nestDirPath = folderPath.resolve(NEST_DIR);
        Files.createDirectories(nestDirPath);

        // File: nest/file.txt, File Content: "I love CS4218"
        Path dirPath = nestDirPath.resolve(FILE_ONE_NAME);
        Files.writeString(dirPath, MULTI_LINE_CONT);

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);

        // File: remove.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // File: paste.txt
        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);
        Files.writeString(pasteFilePath, PASTE_CONT);

        // File: uniq.txt
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        Files.writeString(uniqFilePath, DUP_CONT);
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
    }

    // sort + cat
    // Positive Test Case
    // sort `cat file.txt` => sort A.txt
    @Test
    void sortCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
    }

    // sort + cat
    // Negative Test Case
    // sort `cat invalid.txt` => sort invalid.txt
    @Test
    void sortCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // sort + cd
    // Positive Test Case
    // sort `cd nest` A.txt => sort nest/A.txt
    @Test
    void sortCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // sort + cd
    // Negative Test Case: invalid dir does not exist
    // sort `cd nest` A.txt => cd invalid
    @Test
    void sortCdCommand_validSubCommand_shouldThrowCdException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory); // No change in directory
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // sort + cp
    // Positive Test Case
    // sort `cp A.txt cp.txt` cp.txt => sort cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void sortCpCommand_existingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        // File: sort.txt, File Content: "CS4218"
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {SORT_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
        assertEquals(MULTI_LINE_CONT.replaceAll(NORM_NEWLINE, STRING_NEWLINE), getFileContent(cpFilePath));
    }

    // sort + cp
    // Positive Test Case: cp.txt was created by cp command
    // sort `cp A.txt cp.txt` cp.txt => sort cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void sortCpCommand_noExistingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        assertFalse(Files.exists(cpFilePath));
        String[] args = {SORT_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
        assertEquals(MULTI_LINE_CONT.replaceAll(NORM_NEWLINE, STRING_NEWLINE), getFileContent(cpFilePath));
    }

    // sort + cut
    // Positive Test Case
    // sort `cut -b 1-6 file.txt` => sort A.txt
    @Test
    void sortCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
    }

    // sort + cut
    // Negative Test Case
    // sort `cut 1-6 file.txt` => cut 1-6 file.txt
    @Test
    void sortCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {SORT_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // sort + echo
    // Positive Test Case
    // sort `echo A.txt` => sort A.txt
    @Test
    void sortEchoCommand_echoFileNameSubCommand_ShouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
    }

    // sort + echo
    // Positive Test Case
    // sort `echo` => sort
    // System.in: MULTI_LINE_CONT
    @Test
    void sortEchoCommand_echoEmptySubCommand_ShouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format(SINGLE_STRING, ECHO_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
    }

    // sort + exit
    // Positive Test Case
    // sort `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will turn into input stream)
    @Test
    void sortExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {SORT_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // sort + grep
    // Positive Test Case
    // sort `grep A.txt file.txt` => sort A.txt
    @Test
    void sortGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
    }

    // sort + grep
    // Negative Test Case: invalid pattern
    // sort `grep CS4218\\ file.txt` => grep CS4218\\ file.txt
    @Test
    void sortGrepCommand_invalidSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {SORT_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // sort + ls
    // Positive Test Case (in nest dir)
    // sort `ls` => sort A.txt
    @Test
    void sortLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Environment.setCurrentDirectory(nestDirPath.toString());
        String[] args = {SORT_CMD, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
        Environment.setCurrentDirectory(folderPath.toString());
    }

    // sort + ls
    // Negative Test Case: nest is one of the folder, cannot be sort
    // sort `ls` => ls
    @Test
    void sortLsCommand_lsValidSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {SORT_CMD, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // sort + ls
    // Negative Test Case
    @Test
    void sortLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // sort + rm
    // Positive Test Case: sort A.txt and remove remove.txt
    // sort `rm remove.txt` A.txt => sort A.txt
    @Test
    void sortRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString()); // sort should run as per normal
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // sort + rm
    // Negative Test Case
    // sort `rm remove.txt` remove.txt => sort remove.txt
    // Sort will not run as remove.txt will be removed by the command substitution
    @Test
    void sortRmCommand_validSubCommand_shouldThrowSortException() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_RM_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // sort + rm
    // Negative Test Case: invalid.txt does not exist
    // sort `rm invalid.txt` remove.txt => rm invalid.txt
    @Test
    void sortRmCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // sort + sort
    // Positive Test Case
    // sort `sort sort.txt` A.txt => sort A.txt
    @Test
    void sortSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
    }

    // sort + sort
    // Negative Test Case: input stream goes to the sub command which cause the outer sort command to treat the text input as file directory
    // sort `sort` => sort `sort`
    // System.in: MULTI_LINE_CONT
    @Test
    void sortEchoCommand_emptySortSubCommand_shouldThrowSortException() {
        String[] args = {SORT_CMD, String.format(SINGLE_STRING, SORT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
    }

    // sort + sort
    // Negative Test Case: invalid.txt does not exist
    // sort `sort invalid.txt` => invalid.txt
    @Test
    void sortSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // sort + tee
    // Positive Test Case: the sort command will sort A.txt content
    // sort `tee tee.txt` => sort A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void sortTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
        assertEquals(FILE_ONE_NAME, getFileContent(folderPath.resolve(FILE_TEE_NAME)));
    }

    // sort + tee
    // Positive Test Case: the sort command will sort A.txt content
    // sort `tee` => sort A.txt
    // System.in : A.txt (this will be output)
    @Test
    void sortTeeCommand_stdinFileNameSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {SORT_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_OUT, testOutputStream.toString());
    }

    // sort `wc A.txt` => sort 0 3 13 A.txt
    // It is a valid sub command, however,
    // Expected: SortException as the numbers (0, 3, 13) are invalid files.
    @Test
    void sortWcCommand_validSubCommand_shouldThrowSortException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
    }

    // sort + mv
    // Positive Test Case
    // sort `mv mv1.txt mv2.txt` A.txt => mv mv1.txt mv2.txt then sort A.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void sortMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, MULTI_LINE_CONT); // Overwrite the content for testing
        String[] args = {SORT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(SORT_OUT, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertEquals(MULTI_LINE_CONT.replaceAll(NORM_NEWLINE, STRING_NEWLINE), getFileContent(mv2Path));
    }

    // sort + mv
    // Positive Test Case
    // sort `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then sort A.txt
    // mv1.txt should be moved into mv-folder
    @Test
    void sortMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {SORT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(SORT_OUT, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME))); // uniq1.txt moved into mv-folder
    }

    // sort + mv
    // Negative Test Case
    // uniq `mv mv1.txt mv2.txt` mv1.txt => mv mv1.txt mv2.txt
    // Error as mv1.txt has already been merged into mv2.txt
    @Test
    void sortMvCommand_invalidSubCommand_shouldThrowSortException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {SORT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // sort + mv
    // Negative Test Case
    // sort `mv mv1.txt mv-folder` uniq1.txt => mv mv1.txt mv-folder then sort uniq1.txt
    // mv1.txt should be moved into mv-folder, failing the sort execution
    @Test
    void sortMvCommand_fileAndDirectorySubCommand_shouldThrowSortException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {SORT_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // sort + uniq
    // Positive Test Case
    // sort.txt = A.txt B.txt
    // sort A.txt B.txt
    @Test
    void sortUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_MULTI_1.replaceAll(NORM_NEWLINE, STRING_NEWLINE) + STRING_NEWLINE, testOutputStream.toString());
    }

    // sort + uniq
    // Positive Test Case
    // System.in = A.txt B.txt
    // sort A.txt B.txt
    @Test
    void sortUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {SORT_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_MULTI_1.replaceAll(NORM_NEWLINE, STRING_NEWLINE) + STRING_NEWLINE, testOutputStream.toString());
    }

    // sort + uniq
    // Negative Test Case: invalid.txt does not exist
    // sort `uniq invalid.txt`
    @Test
    void sortUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // sort + uniq
    // Negative Test Case: arg is a folder
    // sort `uniq nest`
    @Test
    void sortUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // sort + paste
    // Positive Test Case
    // sort `paste paste.txt` => uniq A.txt B.txt
    @Test
    void sortPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_MULTI_1.replaceAll(NORM_NEWLINE, STRING_NEWLINE) + STRING_NEWLINE, testOutputStream.toString());
    }

    // sort + paste
    // Negative Test Case
    // sort `paste invalid.txt` => paste invalid.txt
    @Test
    void sortPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // sort + unknown
    // Negative Test Case
    @Test
    void sortInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {SORT_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
