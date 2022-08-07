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

public class RmCommandSubIT {
    private static final String MULTI_CONTENT = "B.txt\nA.txt";
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME;
    private static final String PASTE_CONT = FILE_TWO_NAME + "\n" + FILE_ONE_NAME;

    @TempDir
    public static Path folderPath;
    private static Path rmFilePath;
    private static Path filePath;
    private static Path filePath2;
    private static Path fileNestPath;
    private static Path nestDirPath;
    private static Path cpFilePath;
    private static Path pasteFilePath;
    private static ByteArrayOutputStream outCapture, testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;

    @BeforeAll
    public static void setUpAll() throws IOException {
        testOutputStream = new ByteArrayOutputStream();
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();

        FileUtils.createNewDirs(folderPath);
        Environment.setCurrentDirectory(folderPath.toString());
        outCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outCapture));

        // File: A.txt
        filePath = folderPath.resolve(FILE_ONE_NAME);

        // File: B.txt, File Content: "CS4218"
        filePath2 = folderPath.resolve(FILE_TWO_NAME);

        // File: file.txt, File Content: "A.txt"
        Path pathFile = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(pathFile, FILE_ONE_NAME);

        // ./nest
        nestDirPath = folderPath.resolve(NEST_DIR);
        Files.createDirectories(nestDirPath);

        // File: nest/file.txt, File Content: "I love CS4218"
        fileNestPath = nestDirPath.resolve(FILE_ONE_NAME);
        Files.writeString(fileNestPath, FILE_ONE_CONTENT);

        // File: sort.txt, File Content: Multiline text
        Path sortFilePath = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(sortFilePath, MULTI_CONTENT);

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);

        // File: remove.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // File: uniq.txt
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        Files.writeString(uniqFilePath, DUP_CONT);

        // File: paste.txt
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
        testOutputStream.reset();
        outCapture.reset();
        Environment.setCurrentDirectory(folderPath.toString());
    }

    @BeforeEach
    public void setUp() throws IOException {
        Files.writeString(filePath, FILE_ONE_CONTENT);
        Files.writeString(filePath2, FILE_TWO_CONTENT);
    }

    // rm + cat
    // Positive Test Case
    // rm `cat file.txt` => rm A.txt
    @Test
    void rmCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath)); // remove.txt should be removed
    }

    // r, + cat
    // Negative Test Case: invalid.txt does not exist
    // rm `cat invalid.txt` => cat invalid.txt
    @Test
    void rmCatCommand_invalidFileNameInvalidSubCommand_shouldThrowCatException() {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + cd
    // Positive Test Case
    // rm `cd nest` A.txt => cat nest/A.txt
    @Test
    void rmCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(fileNestPath)); // remove.txt should be removed
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // rm + cd
    // Negative Test Case: The folder/directory "invalid" does not exist
    // rm `cd invalid` A.txt => cd invalid
    @Test
    void rmCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + cp
    // Positive Test Case
    // rm `cp A.txt cp.txt` cp.txt => rm cp.txt (Removing the cp.txt created by cp command)
    @Test
    void rmCpCommand_noExistingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        assertFalse(Files.exists(cpFilePath));
        String[] args = {RM_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(cpFilePath));
    }

    // rm + CP
    // Positive Test Case: cp.txt is an existing file with content, overwritten by cp then delete by rm
    // rm `cp A.txt cp.txt` => rm cp.txt
    @Test
    void rmCpCommand_existingFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {RM_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(cpFilePath));
    }

    // rm + cut
    // Positive Test Case
    // rm `cut -b 1-6 file.txt` => rm A.txt
    @Test
    void rmCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {RM_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath));
    }

    // rm + cut
    // Negative Test Case: missing flag
    // rm `cut -b 1-6 file.txt` => cut -b 1-6 file.txt
    @Test
    void rmCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {RM_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // rm + echo
    // Positive Test Case
    // rm `echo A.txt` => rm A.txt
    @Test
    void rmEchoCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath));
    }

    // rm + echo
    // Negative Test Case
    // rm `echo invalid.txt` => rm invalid.txt
    @Test
    void rmEchoCommand_invalidFileNameSubCommand_shouldThrowRmException() {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + exit
    // Positive Test Case
    // rm `exit` => Program should exit (Assumption: This behaviour is different from Linux)
    @Test
    void rmExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {RM_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // rm + grep
    // Positive Test Case
    // rm `grep A.txt file.txt` => rm A.txt
    @Test
    void rmGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {RM_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath));
    }

    // rm + grep
    // Negative Test Case: invalid pattern
    // rm `grep CS4218\\ file.txt` => grep CS4218\\ file.txt
    @Test
    void rmGrepCommand_invalidSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {RM_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // rm + ls
    // Positive Test Case
    // rm `ls nest` => rm A.txt
    @Test
    void rmLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Environment.setCurrentDirectory(nestDirPath.toString());
        String[] args = {RM_CMD, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(fileNestPath));
    }

    // rm + ls
    // Negative Test Case
    @Test
    void rmLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // rm + rm
    // Positive Test Case
    // rm `rm remove.txt` A.txt => rm A.txt
    // Both files should be removed
    @Test
    void rmRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(rmFilePath));
    }

    // rm + rm
    // Negative Test Case
    // rm `rm remove.txt` remove.txt => rm remove.txt
    // Throws error as the file has already been removed by the sub command
    @Test
    void rmRmCommand_removeSameFileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_RM_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
        assertFalse(Files.exists(rmFilePath));
    }

    // rm + rm
    // Negative Test Case: invalid.txt does not exist
    // rm `rm invalid.txt` A.txt => rm invalid.txt
    @Test
    void rmRmCommand_removeInvalidFileSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + sort
    // Positive Test Case
    // rm `sort sort.txt` => rm A.txt B.txt
    @Test
    void rmSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(filePath2));
    }

    // rm + sort
    // Negative Test Case: invalid.txt does not exist
    // rm `sort invalid.txt` => sort invalid.txt
    @Test
    void rmSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + tee
    // Positive Test Case
    // rm `tee tee.txt` => rm A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void rmTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {RM_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath)); // A.txt is deleted
        assertEquals(FILE_ONE_NAME, getFileContent(folderPath.resolve(FILE_TEE_NAME))); // "A.txt" written into tee.txt
        deleteFileIfExists(folderPath.resolve(FILE_TEE_NAME));
    }

    // rm + tee
    // Positive Test Case
    // rm `tee` => rm A.txt
    // System.in : A.txt (this will be outputted)
    // Tee command will return "A.txt" which will be passed to ls
    @Test
    void rmTeeCommand_stdinDirectoryValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {RM_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath)); // A.txt is deleted
    }

    // rm + wc
    // Negative Test Case
    // Numbers from wc are treated as file input, hence causing RmException
    // rm `wc A.txt` => rm 0 0 0 A.txt
    @Test
    void rmWcCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
    }

    // rm + mv
    // Positive Test Case
    // rm `mv mv1.txt mv2.txt` A.txt => mv mv1.txt mv2.txt then rm A.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void rmMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {RM_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // rm + mv
    // Positive Test Case
    // rm `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then rm A.txt
    // mv1.txt should be moved into mv-folder
    @Test
    void rmMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {RM_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME))); // uniq1.txt moved into mv-folder
    }

    // rm + mv
    // Negative Test Case
    // uniq `mv mv1.txt mv2.txt` mv1.txt => mv mv1.txt mv2.txt
    // Error as mv1.txt has already been merged into mv2.txt
    @Test
    void rmMvCommand_invalidSubCommand_shouldThrowRmException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {RM_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + mv
    // Negative Test Case
    // rm `mv mv1.txt mv-folder` mv1.txt => mv mv1.txt mv-folder then rm mv1.txt
    // mv1.txt should be moved into mv-folder, failing the rm execution
    @Test
    void rmMvCommand_fileAndDirectorySubCommand_shouldThrowRmException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {RM_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + uniq
    // Positive Test Case
    // rm.txt = A.txt A.txt B.txt
    // rm A.txt B.txt
    @Test
    void rmUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(filePath2));
    }

    // rm + uniq
    // Positive Test Case
    // System.in = A.txt B.txt
    // rm A.txt B.txt
    @Test
    void rmUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {RM_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(filePath2));
    }

    // rm + uniq
    // Negative Test Case: invalid.txt does not exist
    // rm `uniq invalid.txt`
    @Test
    void rmUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + uniq
    // Negative Test Case: arg is a folder
    // rm `uniq nest`
    @Test
    void rmUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {SORT_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // rm + paste
    // Positive Test Case
    // rm `paste paste.txt` => uniq A.txt B.txt
    @Test
    void rmPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(filePath2));
    }

    // rm + paste
    // Negative Test Case
    // rm `paste invalid.txt` => paste invalid.txt
    @Test
    void rmPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {RM_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // rm + unknown
    // Negative Test Case
    @Test
    void rmInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {RM_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
