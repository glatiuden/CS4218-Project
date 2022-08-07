package sg.edu.nus.comp.cs4218.integration.command_substitution;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;
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

public class CutCommandSubIT {
    private final static String MULTI_CONTENT = "A.txt";
    private final static String CUT_FLAG = "-b";
    private final static String CUT_POS = "1-13";
    private static final String CUT_OUT = FILE_ONE_CONTENT + STRING_NEWLINE;
    private static final String DUP_CONT = FILE_ONE_NAME + "\n" + FILE_ONE_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_TWO_NAME + "\n" + FILE_ONE_NAME;

    @TempDir
    public static Path folderPath;
    private static Path rmFilePath, cpFilePath, nestDirPath;
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

        // File: rm.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // File: cp.txt
        cpFilePath = folderPath.resolve(FILE_CP_NAME);

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
        testOutputStream.reset();
        outputCapture.reset();
    }

    // cut + cat
    // Positive Test Case
    // cut -b 1-13 `cat file.txt` => cut -b 1-13 A.txt
    @Test
    void cutCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + cat
    // Negative Test Case: invalid.txt does not exist
    // cut -b 1-13 `cat invalid.txt` => cat invalid.txt
    @Test
    void cutCatCommand_invalidSubCommand_shouldThrowCatException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cut + cd
    // Positive Test Case
    // cut -b 1-13 `cd nest` A.txt => cut -b 1-13 nest/A.txt
    @Test
    void cutCdCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        Environment.setCurrentDirectory(folderPath.toString()); // Reset directory back to test folder
    }

    // cut + cd
    // Negative Test Case
    // cut -b 1-13 `cd invalid` A.txt => cd invalid
    @Test
    void cutCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory);
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cut + cp
    // Positive Test Case
    // cut -b 1-13 `cp A.txt cp.txt` cp.txt => cut -b 1-13 cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void cutCpCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + cp
    // Positive Test Case: cp.txt is an existing file with content, overwritten by cp
    // cut -b 1-13 `cp A.txt cp.txt` cp.txt => cut -b 1-13 cp.txt (where the content from A.txt is copied to cp.txt)
    @Test
    void cutCpCommand_existingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), FILE_CP_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + cut
    // Positive Test Case
    // cut -b 1-13 `cut -b 1-13 file.txt` => cut -b 1-13 A.txt
    @Test
    void cutCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format("`%s %s %s %s`", CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + cut
    // Negative Test Case: missing flag
    // cut -b 1-13 `cut 1-13 file.txt` => cut 1-13 file.txt
    @Test
    void cutCutCommand_invalidSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, CUT_CMD, CUT_POS, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // cut + echo
    // Positive Test Case
    // cut -b 1-13 `echo A.txt` => cut -b 1-13 A.txt
    @Test
    void cutEchoCommand_echoFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + echo
    // Positive Test Case
    // cut `echo -b 1-13` A.txt => cut -b 1-13 A.txt
    @Test
    void cutEchoCommand_echoFlagPositionValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, String.format(TRIPLE_STRING, ECHO_CMD, CUT_FLAG, CUT_POS), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + exit
    // Positive Test Case
    // cut -b 1-13 `exit` => Program should exit (Assumption: As this behaviour is different from Linux where it will turn into input stream)
    @Test
    void cutExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // cut + grep
    // Positive Test Case
    // cut -b 1-13 `grep A.txt file.txt` A.txt => cut -b 1-13 A.txt
    @Test
    void cutGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + grep
    // Negative Test Case: invalid pattern
    // cut -b 1-13 `grep CS4218\\ file.txt` A.txt => grep CS4218\\ file.txt
    @Test
    void cutGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // cut + ls
    // Positive Test Case
    // cut -b 1-13 `ls nest` => cut -b 1-13 A.txt
    @Test
    void cutLsCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, LS_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + ls
    // Negative Test Case
    @Test
    void cutLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // cut + rm
    // Positive Test Case
    // cut should run and remove.txt is removed
    // cut -b 1-13 `rm remove.txt` A.txt => cut -b 1-13 A.txt
    @Test
    void cutRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path rmFilePath = folderPath.resolve(FILE_RM_NAME);
        Files.writeString(rmFilePath, FILE_ONE_CONTENT);
        assertTrue(rmFilePath.toFile().exists());

        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
        assertFalse(rmFilePath.toFile().exists()); // remove.txt should be removed
    }

    // cut + rm
    // Negative Test Case
    // attempt to cut the file that has been removed
    // cut -b 1-13 `rm remove.txt` remove.txt => cut -b 1-13 remove.txt
    @Test
    void cutRmCommand_rmFileInvalidSubCommand_shouldNotEvaluate() throws Exception {
        Files.writeString(rmFilePath, FILE_ONE_CONTENT);
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format("%s: %s", String.format(CUT_EXCEP, FILE_RM_NAME), ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
    }

    // cut + rm
    // Negative Test Case: invalid.txt does not exist
    // cut -b 1-13 `rm invalid.txt` remove.txt => rm invalid.txt
    @Test
    void cutRmCommand_invalidSubCommand_shouldThrowRmException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID), FILE_RM_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cut + sort
    // Positive Test Case
    // cut -b 1-13 `sort sort.txt` A.txt => cut -b 1-13 A.txt
    @Test
    void cutSortCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + sort
    // Negative Test Case: invalid.txt does not exist
    // cut -b 1-13 `sort invalid.txt` A.txt => sort invalid.txt
    @Test
    void cutSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cut + tee
    // Positive Test Case
    // cut `tee tee.txt` => cut A.txt
    // System.in : A.txt (this will be output and written into tee.txt)
    @Test
    void cutTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
        assertEquals(FILE_ONE_NAME, getFileContent(folderPath.resolve(FILE_TEE_NAME)));
    }

    // cut + tee
    // Positive Test Case
    // cut `tee` => cut A.txt
    // System.in : A.txt (this will be outputted)
    // Tee command will return A.txt which will be passed to Grep
    @Test
    void cutTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format("`%s`", TEE_CMD)};
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_ONE_NAME.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + wc
    // Positive Test Case
    // The numbers are ignored (as cut does not throw error for invalid files, only stdout error message) until it takes in A.txt as input
    // cut `wc A.txt` => cut 0 3 13 A.txt
    @Test
    void cutWcCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // [single]
    // Sub command is valid but invalid cut command
    // Negative Test Case: missing flag for cut
    @Test
    void cutCommand_validCommand_shouldThrowCatException() {
        String[] args = {CUT_CMD, CUT_POS, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // cut + mv
    // Positive Test Case
    // cut `mv mv1.txt mv2.txt` mv.txt => mv mv1.txt mv2.txt then cut mv.txt
    // mv1.txt should be merged into mv2.txt
    @Test
    void cutMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String expectedResult = FILE_MV1_NAME + StringUtils.STRING_NEWLINE + FILE_MV2_NAME + StringUtils.STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    // cut + mv
    // Positive Test Case
    // cut `mv mv1.txt mv-folder` mv.txt => mv mv1.txt mv-folder then cut mv.txt
    // mv1.txt should be moved into mv-folder
    @Test
    void cutMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String expectedResult = FILE_MV1_NAME + StringUtils.STRING_NEWLINE + FILE_MV2_NAME + StringUtils.STRING_NEWLINE;
        assertEquals(expectedResult, testOutputStream.toString());
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME))); // mv1.txt moved into mv-folder
    }

    // cut + mv
    // Negative Test Case
    // cut `mv mv1.txt mv2.txt` mv1.txt => mv mv1.txt mv2.txt
    // Error as mv1.txt has already been merged into mv2.txt
    @Test
    void cutMvCommand_filesMergedSubCommand_shouldNotEvaluate() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(CUT_EXCEP_DIR, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
    }

    // cut + mv
    // Negative Test Case
    // cut `mv mv1.txt mv-folder` mv1.txt => mv mv1.txt mv-folder then cat mv1.txt
    // mv1.txt should be moved into mv-folder, failing the cat execution
    @Test
    void cutMvCommand_fileAndDirectorySubCommand_shouldNotEvaluate() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, DIR_MV_NAME), FILE_MV1_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(CUT_EXCEP_DIR, FILE_MV1_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString().trim());
    }

    // cut + uniq
    // Positive Test Case
    // uniq.txt = A.txt A.txt B.txt B.txt A.txt
    // Should cut A.txt B.txt A.txt
    @Test
    void cutUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + StringUtils.STRING_NEWLINE + FILE_TWO_CONTENT + StringUtils.STRING_NEWLINE + FILE_ONE_CONTENT + StringUtils.STRING_NEWLINE, testOutputStream.toString());
    }

    // cut + uniq
    // Positive Test Case
    // System.in = A.txt A.txt B.txt B.txt A.txt
    // Should cat A.txt B.txt A.txt
    @Test
    void cutUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + StringUtils.STRING_NEWLINE + FILE_TWO_CONTENT + StringUtils.STRING_NEWLINE + FILE_ONE_CONTENT + StringUtils.STRING_NEWLINE, testOutputStream.toString());
    }

    // cut + uniq
    // Negative Test Case: invalid.txt does not exist
    // cut `uniq invalid.txt`
    @Test
    void cutUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cut + uniq
    // Negative Test Case: arg is a folder
    // cut `uniq nest`
    @Test
    void cutUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // cut + paste
    // Positive Test Case
    // cut -b 1-3 `paste file.txt` => cut -b 1-3 A.txt
    @Test
    void cutPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, PASTE_CMD, folderPath.resolve(FILE_FILE_NAME))};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // cut + paste
    // Negative Test Case
    // cut `paste invalid.txt` => paste invalid.txt
    @Test
    void cutPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {CUT_CMD, CUT_FLAG, CUT_POS, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cut + unknown
    // Negative Test Case
    @Test
    void cutInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {CUT_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
