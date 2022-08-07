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

public class CpCommandSubIT {
    private static final String MULTI_CONTENT = "A.txt"; // The argument resolver doesn't append space to break the statements up
    private static final String FILE_CLONE_NAME = "clone.txt";
    private static final String FILE_CLONE2_NAME = "clone2.txt";
    private static final String PASTE_CONT = FILE_ONE_NAME + "\n" + FILE_CLONE_NAME;
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_CLONE_NAME + "\n" + FILE_CLONE_NAME;

    @TempDir
    public static Path folderPath;
    private static Path cloneFilePath;
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
        Path sortFilePath = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(sortFilePath, MULTI_CONTENT);

        // File: uniq.txt, File Content: Multiline text
        Path uniqFilePath = folderPath.resolve(FILE_UNIQ_NAME);
        Files.writeString(uniqFilePath, DUP_CONT);

        pasteFilePath = folderPath.resolve(FILE_PASTE_NAME);
        Files.writeString(pasteFilePath, PASTE_CONT);

        mvPath = folderPath.resolve(FILE_MV_NAME);
        mv1Path = folderPath.resolve(FILE_MV1_NAME);
        mv2Path = folderPath.resolve(FILE_MV2_NAME);
        mvFolderPath = folderPath.resolve(DIR_MV_NAME);
    }

    @AfterAll
    public static void tearDown() {
        FileUtils.deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    @AfterEach
    public void reset() throws IOException {
        deleteMvTestFiles();
        deleteFileIfExists(cloneFilePath);
        testOutputStream.reset();
        outputCapture.reset();
    }

    @BeforeEach
    public void setUpEach() throws IOException {
        cloneFilePath = folderPath.resolve(FILE_CLONE_NAME);
        createNewFile(cloneFilePath);
    }

    // cp + cat
    // Positive Test Case
    // cp `cat file.txt` clone.txt => cat A.txt clone.txt
    @Test
    void cpCatCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cloneFilePath, FILE_TWO_CONTENT);
        assertEquals(getFileContent(cloneFilePath), FILE_TWO_CONTENT);
        assertTrue(cloneFilePath.toFile().exists());

        String[] args = {CP_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneFilePath), FILE_ONE_CONTENT);
    }

    // cp + cat
    // Positive Test Case: cp will create clone2.txt
    // cp `cat file.txt` clone2.txt => cat A.txt clone2.txt
    @Test
    void cpCatCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path cloneFile2Path = folderPath.resolve(FILE_CLONE2_NAME);
        assertFalse(cloneFile2Path.toFile().exists());

        String[] args = {CP_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME), FILE_CLONE2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertTrue(cloneFile2Path.toFile().exists());
        assertEquals(getFileContent(cloneFile2Path), FILE_ONE_CONTENT);
        deleteFileIfExists(cloneFile2Path);
    }

    // cp + cat
    // Negative Test Case: invalid.txt does not exist
    // cp `cat invalid.txt` clone.txt => cat A.txt clone.txt
    @Test
    void cpCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cp + cd
    // Positive Test Case
    // cp `cd nest` A.txt clone.txt => cp nest/A.txt nest/clone.txt
    @Test
    void cpCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path cloneNestPath = folderPath.resolve(NEST_DIR).resolve(FILE_CLONE_NAME);
        createNewFile(cloneNestPath);
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME, FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneNestPath), FILE_ONE_CONTENT);
        assertEquals(folderPath.resolve(NEST_DIR).toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // cp + cd
    // Negative Test Case: The folder/directory "invalid" does not exist
    // grep CS4218 `cd invalid` A.txt => cd invalid
    @Test
    void cpCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cp + cp
    // Positive Test Case
    // cp `cp A.txt cp.txt` cp.txt clone.txt => cp cp.txt clone.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void cpCpCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME, FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneFilePath), FILE_ONE_CONTENT);
    }

    // cp + cat
    // Positive Test Case: cp will create clone2.txt
    // cp `cp A.txt cp.txt` cp.txt clone2.txt => cp cp.txt clone2.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void cpCpCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path cloneFile2Path = folderPath.resolve(FILE_CLONE2_NAME);
        assertFalse(cloneFile2Path.toFile().exists());

        String[] args = {CP_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME, FILE_CLONE2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertTrue(cloneFile2Path.toFile().exists());
        assertEquals(getFileContent(cloneFile2Path), FILE_ONE_CONTENT);
        deleteFileIfExists(cloneFile2Path);
    }

    // cp + cut
    // Positive Test Case
    // cp `cut -b 1-6 file.txt` clone.txt => cp A.txt clone.txt
    @Test
    void cpCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format("`%s -b 1-6 %s`", CUT_CMD, FILE_FILE_NAME), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneFilePath), FILE_ONE_CONTENT);
    }

    // cp + cut
    // Negative Test Case: missing flag
    // cp `cut 1-6 file.txt` clone.txt => cp A.txt clone.txt
    @Test
    void cpCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {CP_CMD, String.format("`%s 1-6 %s`", CUT_CMD, FILE_FILE_NAME), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // cp + echo
    // Positive Test Case: echo 1st file arg
    // cp `echo A.txt` clone.txt => cat A.txt clone.txt
    @Test
    void cpEchoCommand_echoFirstFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneFilePath), FILE_ONE_CONTENT);
    }

    // cp + echo
    // Positive Test Case: echo 1st file arg
    // cp `echo A.txt` clone.txt => cat A.txt clone.txt
    @Test
    void cpEchoCommand_echoSecondFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, FILE_ONE_NAME, String.format(DOUBLE_STRING, ECHO_CMD, FILE_CLONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneFilePath), FILE_ONE_CONTENT);
    }

    // cp + exit
    // Positive Test Case
    // cp `exit` => Program should exit (Assumption: This behaviour is different from Linux)
    @Test
    void cpExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {CP_CMD, String.format("`%s`", EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // cp + grep
    // Positive Test Case
    // cp `grep A.txt file.txt` clone.txt => cp A.txt clone.txt
    @Test
    void cpGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneFilePath), FILE_ONE_CONTENT);
    }

    // cp + grep
    // Positive Test Case
    // cp A.txt `grep clone.txt` => cp A.txt clone.txt (grep input from stdin)
    // System.in: A.txt
    @Test
    void cpGrepCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, FILE_ONE_NAME, String.format(DOUBLE_STRING, GREP_CMD, FILE_CLONE_NAME)};
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_CLONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneFilePath), FILE_ONE_CONTENT);
    }

    // cp + grep
    // Negative Test Case: invalid pattern
    // cp `grep CS4218\\ file.txt` clone.txt => grep CS4218\\ file.txt
    @Test
    void cpGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {CP_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // cp + ls
    // Positive Test Case
    // cp `ls nest` clone.txt => cp A.txt clone.txt
    @Test
    void cpLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Environment.setCurrentDirectory(nestDirPath.toString());
        String[] args = {CP_CMD, String.format(SINGLE_STRING, LS_CMD), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, getFileContent(nestDirPath.resolve(FILE_CLONE_NAME)));
        Environment.setCurrentDirectory(folderPath.toString());
    }

    // cp + ls
    // Negative Test Case
    @Test
    void cpLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g"), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // cp + rm
    // Positive Test Case
    // cp `rm remove.txt` A.txt remove.txt => cp A.txt remove.txt
    // File is deleted in the substituted command, then re-created by cp
    @Test
    void cpRmCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path rmFilePath = folderPath.resolve(FILE_RM_NAME);
        Files.writeString(rmFilePath, FILE_TWO_CONTENT);
        assertEquals(getFileContent(rmFilePath), FILE_TWO_CONTENT);

        String[] args = {CP_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME, FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(rmFilePath), FILE_ONE_CONTENT);
        assertTrue(rmFilePath.toFile().exists()); // remove.txt should be created again
    }

    // cp + rm
    // Negative Test Case
    // cp `rm invalid.txt` A.txt remove.txt => rm invalid.txt
    @Test
    void cpRmCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_ONE_NAME, FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cp + sort
    // Positive Test Case
    // cp `sort sort.txt` clone.txt => cp A.txt clone.txt
    @Test
    void cpSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(cloneFilePath), FILE_ONE_CONTENT);
    }

    // cp + sort
    // Negative Test Case: invalid.txt does not exist
    // cp `sort invalid.txt` clone.txt => sort invalid.txt
    @Test
    void cpSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID), FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cp + tee
    // Positive Test Case
    // cp `tee tee.txt` => cp A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void cpTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME), FILE_CLONE_NAME};
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME, getFileContent(folderPath.resolve(FILE_TEE_NAME)));
        assertEquals(FILE_ONE_CONTENT, getFileContent(cloneFilePath));
    }

    // cp + tee
    // Positive Test Case
    // cp `tee` => cp A.txt
    // System.in : A.txt (this will be outputted)
    // Tee command will return A.txt which will be passed to cp
    @Test
    void cpTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format("`%s`", TEE_CMD), FILE_CLONE_NAME};
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, getFileContent(cloneFilePath));
    }

    // cp + wc
    // Negative Test Case
    // The numbers from wc are treated as files/directory, hence leading to Exception
    // cp `wc A.txt` => cp 0 3 13 A.txt
    @Test
    void cpWcCommand_validSubCommand_shouldThrowCpException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP_DIR, ERR_IS_NOT_DIR), thrown.getMessage());
    }

    // cp + wc
    // Negative Test Case
    // cat `wc A.txt` => cat 0 3 13 A.txt
    // It is a valid sub command, however CatException is thrown as the numbers (0, 3, 13) are invalid file names.
    @Test
    void cpWcCommand_invalidSubCommand_shouldThrowCpException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
    }

    // cp + mv
    // Positive Test Case
    // cp `mv mv1.txt mv2.txt` mv.txt => mv mv1.txt mv2.txt then cat mv.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void cpMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CP_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME, FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(cloneFilePath));
    }

    // cp + mv
    // Positive Test Case
    // cp `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then cp mv.txt
    // mv1.txt should be moved into mv-folder
    @Test
    void cpMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CP_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV_NAME, FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME))); // mv1.txt moved into mv-folder
        assertEquals(FILE_MV1_NAME + STRING_NEWLINE + FILE_MV2_NAME, getFileContent(cloneFilePath));
    }

    // cp + mv
    // Negative Test Case
    // cp `mv mv1.txt mv2.txt` mv1.txt => mv mv1.txt mv2.txt
    // Error as mv1.txt has already been merged into mv2.txt
    @Test
    void cpMvCommand_filesMergedSubCommand_shouldThrowCpException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CP_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME, FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP_DIR, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cp + mv
    // Negative Test Case
    // cp `mv mv1.txt mv-folder` mv1.txt => mv mv1.txt mv-folder then cp mv1.txt
    // mv1.txt should be moved into mv-folder, failing the cp execution
    @Test
    void cpMvCommand_fileAndDirectorySubCommand_shouldThrowCpException() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CP_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME, FILE_CLONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP_DIR, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cp + uniq
    // Positive Test Case
    // uniq.txt = A.txt A.txt B.txt B.txt
    // Should cp A.txt B.txt
    @Test
    void cpUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, getFileContent(cloneFilePath));
    }

    // cp + uniq
    // Positive Test Case
    // System.in = A.txt A.txt B.txt B.txt
    // Should cp A.txt B.txt
    @Test
    void cpUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {CP_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, getFileContent(cloneFilePath));
    }

    // cp + uniq
    // Negative Test Case: invalid.txt does not exist
    // cp `uniq invalid.txt`
    @Test
    void cpUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cp + uniq
    // Negative Test Case: arg is a folder
    // cp `uniq nest`
    @Test
    void cpUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // cp + paste
    // Positive Test Case
    // cp `paste paste.txt` => cp A.txt B.txt
    @Test
    void cpPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, getFileContent(cloneFilePath));
    }

    // cp + paste
    // Negative Test Case
    // cp `paste invalid.txt` => paste invalid.txt
    @Test
    void cpPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {CP_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cp + unknown
    // Negative Test Case
    @Test
    void cpInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {CP_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
