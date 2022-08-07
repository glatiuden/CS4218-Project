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

public class MvCommandSubIT {
    private static final String MV_EXCEP_OUT = "%s: %s" + STRING_NEWLINE;
    private static final String FILE_MVF_NAME = "mv3-folder.txt";
    private static final String DUP_CONT = FILE_MV1_NAME + "\n" + FILE_MV1_NAME + "\n" + FILE_MV2_NAME + "\n" + FILE_MV2_NAME;
    private static final String PASTE_CONT = FILE_MV2_NAME + "\n" + FILE_MV1_NAME;

    @TempDir
    public static Path folderPath;
    private static Path rmFilePath;
    private static ByteArrayOutputStream outputCapture, testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;
    private static Path nestDirPath;
    private static Path cpFilePath;
    private static Path pasteFilePath;

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
        Files.writeString(pathFile, FILE_MV1_NAME);

        // ./nest
        nestDirPath = folderPath.resolve(NEST_DIR);
        createNewDirs(nestDirPath);

        // File: nest/file.txt, File Content: "I love CS4218"
        Path dirPath1 = nestDirPath.resolve(FILE_ONE_NAME);
        Path dirPath2 = nestDirPath.resolve(FILE_TWO_NAME);
        Path mvDirPath = nestDirPath.resolve(DIR_MV_NAME);
        Files.writeString(dirPath1, FILE_ONE_CONTENT);
        Files.writeString(dirPath2, FILE_TWO_CONTENT);
        createNewDirs(mvDirPath);

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);

        // File: mv3-folder.txt
        Path mvfPath = folderPath.resolve(FILE_MVF_NAME);
        Files.writeString(mvfPath, FILE_MV1_NAME + "\n" + DIR_MV_NAME);

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
        testOutputStream.reset();
        outputCapture.reset();
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    @BeforeEach
    public void start() throws IOException {
        createMvTestFiles(folderPath);
    }

    // mv + cat
    // Positive Test Case: cat mv.txt which will output mv1.txt mv2.txt
    // mv `cat file.txt` => mv mv1.txt mv2.txt
    @Test
    void mvCatCommand_mvFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_MV_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + cat
    // Positive Test Case: cat mv.txt which will output mv1.txt mv3-folder
    // mv `cat file.txt` => mv mv1.txt mv3-folder
    @Test
    void mvCatCommand_mvFolderSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_MVF_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME)));
    }

    // mv + cat
    // Negative Test Case: invalid.txt does not exist
    // uniq `cat invalid.txt` => cat invalid.txt
    @Test
    void mvCatCommand_invalidFileNameSubCommand_shouldThrowCatException() {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // mv + cd
    // Positive Test Case
    // mv `cd nest` A.txt B.txt => mv nest/A.txt nest/B.txt
    @Test
    void mvCdCommand_fileNameSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME, FILE_TWO_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(nestDirPath.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, getFileContent(nestDirPath.resolve(FILE_TWO_NAME)));
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // mv + cd
    // Positive Test Case
    // mv `cd nest` A.txt mv3-folder => uniq nest/A.txt nest/mv3-folder
    @Test
    void mvCdCommand_folderSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_TWO_NAME, DIR_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(nestDirPath.resolve(FILE_TWO_NAME)));
        assertTrue(Files.exists(nestDirPath.resolve(DIR_MV_NAME).resolve(FILE_TWO_NAME)));
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // uniq + cd
    // Negative Test Case: invalid dir does not exist
    // mv `cd invalid` A.txt => cd invalid
    @Test
    void mvCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory); // Check that the directory was not changed
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // mv + CP
    // Positive Test Case: cp.txt is created by cp command
    // mv `cp A.txt cp.txt` cp.txt mv1.txt => mv cp.txt mv1.txt (where the content from A.txt is copied to cp.txt, then combined into mv1.txt)
    @Test
    void mvCpCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME, FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(folderPath.resolve(FILE_CP_NAME)));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv1Path)); // Content copied from cp.txt from A.txt
    }

    // mv + cp
    // Positive Test Case: cp.txt already exist with other content, overwritten by cp and shift into mv3-folder
    // mv `cp A.txt cp.txt` cp.txt mv3-folder => mv cp.txt mv3-folder (where the content from A.txt is copied to cp.txt)
    @Test
    void mvCpCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {MV_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME, DIR_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(cpFilePath)); // File is not in main directory
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_CP_NAME))); // Shifted into mv3-folder
        assertEquals(FILE_ONE_CONTENT, getFileContent(mvFolderPath.resolve(FILE_CP_NAME))); // Content copied from A.txt
    }

    // mv + cut
    // Positive Test Case: cut the file name (mv1.txt) from file.txt
    // mv `cut -b 1-6 file.txt` mv2.txt => mv mv1.txt mv2.txt
    @Test
    void mvCutCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format("`%s -b 1-7 %s`", CUT_CMD, FILE_FILE_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + cut
    // Positive Test Case: cut the file name (mv1.txt) from file.txt
    // mv `cut -b 1-6 file.txt` mv3-folder => mv mv1.txt mv3-folder
    @Test
    void mvCutCommand_folderSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format("`%s -b 1-7 %s`", CUT_CMD, FILE_FILE_NAME), DIR_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME)));
    }

    // mv + cut
    // Negative Test Case: missing flag
    // mv `cut 1-7 file.txt` => cut 1-7 file.txt
    @Test
    void mvCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {MV_CMD, String.format("`%s 1-7 %s`", CUT_CMD, FILE_FILE_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // mv + echo
    // Positive Test Case: echo mv1.txt
    // mv `echo mv1.txt` mv2.txt => mv mv1.txt mv2.txt
    @Test
    void mvEchoCommand_echoFileNameSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_MV1_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + echo
    // Positive Test Case: echo mv1.txt
    // mv `echo mv1.txt` mv2.txt => mv mv1.txt mv2.txt
    @Test
    void mvEchoCommand_echoFolderSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_MV1_NAME), DIR_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME)));
    }

    // mv + echo
    // Negative Test Case: echo
    // mv `echo` => mv
    @Test
    void mvEchoCommand_emptyEchoSubCommand_shouldThrowMvException() {
        String[] args = {MV_CMD, String.format(SINGLE_STRING, ECHO_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(MvException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(MV_EXCEP, ERR_NO_ARGS), thrown.getMessage());
    }

    // mv + exit
    // Positive Test Case
    // mv `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will turn into input stream)
    @Test
    void mvExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {MV_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // mv + grep
    // Positive Test Case: grep A.txt from file.txt
    // mv `grep mv1.txt file.txt` mv2.txt => mv mv1.txt mv2.txt
    @Test
    void mvGrepCommand_grepFileNameSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(TRIPLE_STRING, GREP_CMD, FILE_MV1_NAME, FILE_FILE_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + grep
    // Negative Test Case: invalid grep pattern
    // mv `grep CS4218\\ file.txt` mv2.txt => grep CS4218\\ file.txt
    @Test
    void mvGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {MV_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // mv + rm
    // Positive Test Case
    // mv should run as per normal and rm should remove rm.txt
    // mv `rm remove.txt` mv1.txt mv2.txt => mv mv1.txt mv2.txt
    @Test
    void mvRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        rmFilePath = folderPath.resolve(FILE_RM_NAME);
        createNewFile(rmFilePath);

        String[] args = {MV_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_MV1_NAME, FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + rm
    // Negative Test Case
    // mv `rm remove.txt` remove.txt mv2.txt => mv remove.txt
    // mv will not run as remove.txt will be removed by the command substitution
    @Test
    void mvRmCommand_fileRemovedInSubCommand_shouldNotEvaluate() throws Exception {
        rmFilePath = folderPath.resolve(FILE_RM_NAME);
        createNewFile(rmFilePath);

        String[] args = {MV_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME, FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
        assertEquals(String.format(MV_EXCEP_OUT, MV_CMD, ERR_FILE_NOT_FOUND), outputCapture.toString());
    }

    // mv + rm
    // Negative Test Case
    // mv `rm invalid.txt` mv1.txt mv2.txt => rm invalid.txt
    // Error thrown in the sub command as invalid.txt does not exist
    @Test
    void mvRmCommand_invalidFileNameCommand_shouldThrowRmException() {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_MV1_NAME, FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // mv + sort
    // Positive Test Case: sort.txt = mv2.txt mv1.txt
    // mv `sort sort.txt` => mv mv1.txt mv2.txt
    @Test
    void mvSortCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(mvPath, FILE_MV2_NAME + "\n" + FILE_MV1_NAME); // Reverse order of the string
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_MV_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + sort
    // Positive Test Case: sort.txt = mv1.txt mv3-folder
    // mv `sort sort.txt` => mv mv1.txt mv3-folder
    @Test
    void mvSortCommand_folderSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_MVF_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME)));
    }

    // mv + sort
    // Negative Test Case: invalid.txt does not exist
    // mv `sort invalid.txt` => sort invalid.txt
    @Test
    void mvSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // mv + tee
    // Positive Test Case
    // mv `tee tee.txt` => mv A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void mvTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV1_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {MV_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
        assertEquals(FILE_MV1_NAME, getFileContent(folderPath.resolve(FILE_TEE_NAME)));
    }

    // mv + tee
    // Positive Test Case
    // mv `tee` => mv mv1.txt mv2.txt
    // System.in : mv2.txt
    // Tee command will return mv2.txt which will be passed to mv
    @Test
    void mvTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {MV_CMD, FILE_MV1_NAME, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + wc
    // Negative Test Case
    // mv `wc A.txt` => mv # # # mv1.txt mv2.tx
    // It is a valid sub command, however not evaluated is thrown as the numbers are treated as directory which is invalid.
    @Test
    void mvWcCommand_validSubCommand_shouldNotEvaluate() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_MV1_NAME), FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(MV_EXCEP_OUT, MV_CMD, ERR_IS_NOT_DIR), outputCapture.toString());
    }

    // mv + mv
    // Positive Test Case
    // mv `mv mv1.txt mv2.txt` A.txt => mv uniq1.txt uniq2.txt then uniq A.txt
    @Test
    void mvMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME, FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertTrue(Files.exists(mv1Path));
        assertFalse(Files.exists(mv2Path));
    }

    // mv + mv
    // Positive Test Case
    // mv `mv mv1.txt mv2.txt` mv2.txt mv3-folder
    @Test
    void mvMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV2_NAME, DIR_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(folderPath.resolve(DIR_MV_NAME).resolve(FILE_MV2_NAME))); // mv2.txt moved into mv3-folder
    }

    // mv + mv
    // Negative Test Case
    // mv `mv mv1.txt mv3-folder` mv1.txt mv2.txt
    // mv1.txt should be moved into mv3-folder, failing the mv execution
    @Test
    void mvMvCommand_fileAndDirectorySubCommand_shouldNotEvaluate() throws Exception {
        String[] args = {MV_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME, FILE_MV2_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(MV_EXCEP_OUT, MV_CMD, ERR_FILE_NOT_FOUND), outputCapture.toString());
    }

    // mv + uniq
    // Positive Test Case
    // mv.txt = A.txt A.txt B.txt B.txt
    // mv A.txt B.txt
    @Test
    void mvUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(mvPath, DUP_CONT);
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_MV_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + uniq
    // Positive Test Case
    // System.in = A.txt A.txt B.txt B.txt
    // mv A.txt B.txt
    @Test
    void mvUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {MV_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // mv + uniq
    // Negative Test Case: invalid.txt does not exist
    // mv `uniq invalid.txt`
    @Test
    void mvUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // mv + uniq
    // Negative Test Case: arg is a folder
    // mv `uniq nest`
    @Test
    void mvUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // mv + paste
    // Positive Test Case
    // mv `paste paste.txt` => mv mv2.txt mv1.txt
    @Test
    void mvPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, PASTE_CMD, pasteFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv2Path));
        assertEquals(FILE_TWO_CONTENT, getFileContent(mv1Path));
    }

    // mv + paste
    // Negative Test Case
    // mv `paste invalid.txt` => paste invalid.txt
    @Test
    void mvPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // mv + ls
    // Negative Test Case
    @Test
    void mvLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {MV_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // mv + unknown
    // Negative Test Case
    @Test
    void mvInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {MV_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
