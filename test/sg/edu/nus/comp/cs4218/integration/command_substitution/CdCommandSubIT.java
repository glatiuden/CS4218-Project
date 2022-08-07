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
import static sg.edu.nus.comp.cs4218.impl.app.RmApplication.IS_DIRECTORY;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public class CdCommandSubIT {
    private static final String FILE_INV_NAME = "file-inv.txt";
    private static final String FILE_TEMP_NAME = "temp.txt";
    private static final String DUP_CONT = NEST_DIR + "\n" + NEST_DIR + "\n" + NEST_DIR + "\n" + NEST_DIR;
    private static final String MV_EXCEP_OUT = "%s: %s" + STRING_NEWLINE;

    @TempDir
    public static Path folderPath;
    private static Path rmFilePath, dirFilePath, nestDirPath, nest2DirPath, teeFilePath;
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

        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(path1, FILE_ONE_CONTENT);

        // File: file.txt, File Content: "nest"
        dirFilePath = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(dirFilePath, NEST_DIR);

        // File: file-inv.txt, File Content: "invalid"
        Path invDirPath = folderPath.resolve(FILE_INV_NAME);
        Files.writeString(invDirPath, DIR_INVALID);

        // ./nest
        nestDirPath = folderPath.resolve(NEST_DIR);
        createNewDirs(nestDirPath);

        // File: nest/file.txt, File Content: "I love CS4218"
        Path dirPath1 = nestDirPath.resolve(FILE_ONE_NAME);
        Path dirPath2 = nestDirPath.resolve(FILE_TWO_NAME);
        nest2DirPath = nestDirPath.resolve(NEST_DIR);
        Files.writeString(dirPath1, FILE_ONE_CONTENT);
        Files.writeString(dirPath2, FILE_TWO_CONTENT);
        createNewDirs(nest2DirPath);

        // File: rm.txt
        rmFilePath = folderPath.resolve(FILE_RM_NAME);

        // File: tee.txt
        teeFilePath = folderPath.resolve(FILE_TEE_NAME);
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
        Environment.setCurrentDirectory(folderPath.toString());
    }

    // cd + cat
    // Positive Test Case: cat file.txt will output nest
    // cd `cat file.txt` => cd nest
    @Test
    void cdCatCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + cat
    // Negative Test Case: cat file.txt will output invalid
    // cd `cat file.txt` => cd invalid
    @Test
    void cdCatCommand_invalidDirFromCatSubCommand_shouldThrowCdException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INV_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + cat
    // Negative Test Case: invalid.txt does not exist
    // cd `cat invalid.txt` => cat invalid.txt
    @Test
    void cdCatCommand_invalidDirSubCommand_shouldThrowCatException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + cd
    // Positive Test Case
    // cd `cd nest` nest => cd nest/nest
    @Test
    void cdCdCommand_fileNameSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, CD_CMD, NEST_DIR), NEST_DIR};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nest2DirPath.toString(), Environment.currentDirectory);
    }

    // cd + cd
    // Negative Test Case: invalid dir does not exist
    // cd `cd invalid` => cd invalid
    @Test
    void cdCdCommand_invalidSubCommand_shouldThrowCdException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, CD_CMD, DIR_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory); // Check that the directory was not changed
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + cd
    // Negative Test Case
    // cd `cd` => cd (invalid)
    @Test
    void cdCdCommand_emptyCdSubCommand_shouldThrowCdException() {
        String[] args = {CD_CMD, String.format(SINGLE_STRING, CD_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory); // Check that the directory was not changed
        assertEquals(String.format(CD_EXCEP_DIR, ERR_MISSING_ARG), thrown.getMessage());
    }

    // cd + cp
    // Positive Test Case: cp.txt is created by cp command
    // cd `cp A.txt cp.txt` nest => cp A.txt cp.txt then cd nest
    @Test
    void cdCpCommand_noExistingFileValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CD_CMD, String.format(TRIPLE_STRING, CP_CMD, FILE_ONE_NAME, FILE_CP_NAME), NEST_DIR};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertTrue(Files.exists(folderPath.resolve(FILE_CP_NAME)));
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + cp
    // Negative Test Case: cd nest `cp B.txt cp.txt`
    @Test
    void cdCpCommand_invalidSubCommand_shouldThrowCpException() throws Exception {
        Path cpFilePath = folderPath.resolve(FILE_CP_NAME);
        Files.writeString(cpFilePath, FILE_TWO_CONTENT);
        assertEquals(FILE_TWO_CONTENT, getFileContent(cpFilePath));

        String[] args = {CD_CMD, NEST_DIR, String.format(TRIPLE_STRING, CP_CMD, FILE_TWO_NAME, FILE_CP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(folderPath.toString(), Environment.currentDirectory); // Directory not changed
        assertEquals(String.format(CP_EXCEP_DIR, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + cut
    // Positive Test Case: cut the file name (mv1.txt) from file.txt
    // cd `cut -b 1-6 file.txt` => cd nest
    @Test
    void cdCutCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CD_CMD, String.format("`%s -b 1-7 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + cut
    // Negative Test Case
    // cd `cut -b 1-6 file-inv.txt` => cd invalid
    @Test
    void cdCutCommand_invalidDirSubCommand_shouldThrowCdException() {
        String[] args = {CD_CMD, String.format("`%s -b 1-7 %s`", CUT_CMD, FILE_INV_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + cut
    // Negative Test Case: missing flag
    // cd `cut 1-7 file.txt` => cut 1-7 file.txt
    @Test
    void cdCutCommand_invalidSubCommand_shouldThrowCutException() {
        String[] args = {CD_CMD, String.format("`%s 1-7 %s`", CUT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    // cd + echo
    // Positive Test Case:
    // cd `echo nest`
    @Test
    void cdEchoCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, ECHO_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + echo
    // Negative Test Case
    // mv `echo invalid` => cd invalid
    @Test
    void cdEchoCommand_echoInvalidDirSubCommand_shouldThrowCdException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, ECHO_CMD, DIR_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + echo
    // Negative Test Case: echo
    // cd `echo` => cd
    @Test
    void cdEchoCommand_emptyEchoSubCommand_shouldThrowCdException() {
        String[] args = {CD_CMD, String.format(SINGLE_STRING, ECHO_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP_DIR, ERR_MISSING_ARG), thrown.getMessage());
    }

    // cd + echo
    // Negative Test Case: echo `A.txt`
    // cd `echo A.txt` => cd A.txt
    @Test
    void cdEchoCommand_echoFileNameSubCommand_shouldThrowCdException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, ECHO_CMD, FILE_ONE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP, FILE_ONE_NAME, ERR_IS_NOT_DIR), thrown.getMessage());
    }

    // cd + exit
    // Positive Test Case
    // cd `exit`
    @Test
    void cdExitCommand_validSubCommand_shouldThrowExitException() {
        String[] args = {CD_CMD, String.format(SINGLE_STRING, EXIT_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // cd + grep
    // Positive Test Case: grep A.txt from file.txt
    // mv `grep mv1.txt file.txt` mv2.txt => mv mv1.txt mv2.txt
    @Test
    void cdGrepCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CD_CMD, String.format(TRIPLE_STRING, GREP_CMD, NEST_DIR, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // mv + grep
    // Negative Test Case: invalid grep pattern
    // mv `grep CS4218\\ file.txt` mv2.txt => grep CS4218\\ file.txt
    @Test
    void cdGrepCommand_invalidSubCommand_shouldThrowGrepException() {
        String[] args = {CD_CMD, String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    // cd + ls
    // Negative Test Case: as LS does not support -d, unable to list out only folders hence not suitable for command sub
    // cd `ls` => cd file1 file2 file3
    @Test
    void cdLsCommand_validSubCommand_shouldEvaluateSuccessfully() {
        String[] args = {CD_CMD, String.format(SINGLE_STRING, LS_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP_DIR, ERR_TOO_MANY_ARGS), thrown.getMessage());
    }

    // cd + ls
    // Negative Test Case
    @Test
    void cdLsCommand_invalidSubCommand_shouldThrowLsException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, LS_CMD, "-g")};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    // cd + rm
    // Positive Test Case
    // cd should run as per normal and rm should remove rm.txt
    // cd `rm remove.txt` nest => rm remove.txt then cd nest
    @Test
    void cdRmCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createNewFile(rmFilePath);
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), NEST_DIR};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(rmFilePath)); // remove.txt should be removed
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + rm
    // Negative Test Case
    // cd `rm remove.txt` nest
    // cd will not run as rm has an exception for attempting to remove folder
    @Test
    void cdRmCommand_rmFolderSubCommand_shouldThrowRmException() throws Exception {
        createNewDirs(rmFilePath);
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_RM_NAME), NEST_DIR};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_RM_NAME, IS_DIRECTORY), thrown.getMessage());
        assertEquals(folderPath.toString(), Environment.currentDirectory); // Directory not changed
        deleteFileIfExists(rmFilePath);
    }

    // cd + rm
    // Negative Test Case
    // cd `rm invalid.txt` => rm invalid.txt
    // Error thrown in the sub command as invalid.txt does not exist
    @Test
    void cdRmCommand_invalidFileNameCommand_shouldThrowRmException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, RM_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + sort
    // Positive Test Case: file.txt = nest
    // cd `sort file.txt` => cd nest
    @Test
    void cdSortCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + sort
    // Negative Test Case: temp.txt = nest nest
    // cd `sort sort.txt` => cd nest nest
    @Test
    void cdSortCommand_twoFileNamesInSortFileSubCommand_shouldThrowCdException() throws Exception {
        Path tempFile = folderPath.resolve(FILE_TEMP_NAME);
        Files.writeString(tempFile, NEST_DIR + "\n" + NEST_DIR);
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_TEMP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP_DIR, ERR_TOO_MANY_ARGS), thrown.getMessage());
        deleteFileIfExists(tempFile);
    }

    // cd + sort
    // Negative Test Case: invalid.txt does not exist
    // cd `sort invalid.txt` => sort invalid.txt
    @Test
    void cdSortCommand_invalidSubCommand_shouldThrowSortException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + tee
    // Positive Test Case
    // cd `tee tee.txt` => cd nest
    // System.in : nest (this will be output and written into tee.txt)
    @Test
    void cdTeeCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(NEST_DIR.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {CD_CMD, String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        assertEquals(NEST_DIR, getFileContent(teeFilePath));
        deleteFileIfExists(teeFilePath);
    }

    // cd + tee
    // Positive Test Case
    // cd `tee` => cd nest
    // System.in : nest
    // Tee command will return nest which will be passed to cd
    @Test
    void cdTeeCommand_stdinFileNameValidSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(NEST_DIR.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {CD_CMD, String.format(SINGLE_STRING, TEE_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + wc
    // Negative Test Case
    // cd `wc A.txt` => cd # # # dir
    // It is a valid sub command, however cd exception thrown as there are too many arguments.
    @Test
    void cdWcCommand_validSubCommand_shouldThrowCdException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, WC_CMD, FILE_FILE_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP_DIR, ERR_TOO_MANY_ARGS), thrown.getMessage());
    }

    // cd + mv
    // Positive Test Case
    // cd `mv mv1.txt mv2.txt` nest => mv mv1.txt mv2.txt then cd nest
    @Test
    void cdMvCommand_twoFilesSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CD_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV1_NAME, FILE_MV2_NAME), NEST_DIR};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mv2Path));
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + mv
    // Positive Test Case
    // cd `mv2.txt mv3-folder` then cd nest
    @Test
    void cdMvCommand_fileAndDirectorySubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CD_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_MV2_NAME, DIR_MV_NAME), NEST_DIR};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv2Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV2_NAME))); // mv2.txt moved into mv3-folder
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + mv
    // Positive Test Case
    // cd `invalid.txt mv3-folder` => fails but cd succeed
    @Test
    void cdMvCommand_invalidSrcCommand_shouldNotEvaluateSubCommandButEvaluateCommand() throws Exception {
        createMvTestFiles(folderPath);
        String[] args = {CD_CMD, String.format(TRIPLE_STRING, MV_CMD, FILE_INVALID, DIR_MV_NAME), NEST_DIR};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(MV_EXCEP_OUT, MV_CMD, ERR_FILE_NOT_FOUND), outputCapture.toString());
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + uniq
    // Positive Test Case
    // temp.txt = nest nest nest nest
    // cd nest
    @Test
    void cdUniqCommand_fileSubCommand_shouldEvaluateSuccessfully() throws Exception {
        Path tempFile = folderPath.resolve(FILE_TEMP_NAME);
        Files.writeString(tempFile, DUP_CONT);
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_TEMP_NAME)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
        deleteFileIfExists(tempFile);
    }

    // cd + uniq
    // Positive Test Case
    // System.in = nest nest nest nest
    // cd nest
    @Test
    void cdUniqCommand_stdinSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(DUP_CONT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate

        String[] args = {CD_CMD, String.format(SINGLE_STRING, UNIQ_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + uniq
    // Negative Test Case: invalid.txt does not exist
    // cd `uniq invalid.txt`
    @Test
    void cdUniqCommand_invalidFileSubCommand_shouldThrowUniqException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + uniq
    // Negative Test Case: arg is a folder
    // cd `uniq nest`
    @Test
    void cdUniqCommand_directorySubCommand_shouldThrowUniqException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, UNIQ_CMD, NEST_DIR)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, NEST_DIR, ERR_IS_DIR), thrown.getMessage());
    }

    // cd + paste
    // Positive Test Case
    // cd `paste paste.txt` => cd nest
    @Test
    void cdPasteCommand_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, PASTE_CMD, dirFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(nestDirPath.toString(), Environment.currentDirectory);
    }

    // cd + paste
    // Negative Test Case
    // cd `paste invalid.txt` => paste invalid.txt
    @Test
    void cdPasteCommand_invalidSubCommand_shouldThrowPasteException() {
        String[] args = {CD_CMD, String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // cd + unknown
    // Negative Test Case
    @Test
    void cdInvalidCommand_invalidSubCommand_shouldThrowShellException() {
        String[] args = {CD_CMD, String.format(SINGLE_STRING, INVALID_CMD)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }
}
