package sg.edu.nus.comp.cs4218.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.ErrorConstants;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static sg.edu.nus.comp.cs4218.impl.app.CatApplication.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.app.ExitApplication.EXIT_MESSAGE;
import static sg.edu.nus.comp.cs4218.impl.app.UniqApplication.ERR_EXTRA_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class GlobbingIT {
    public static final String TOTAL_STRING = "total";
    private static final String CS4218_STRING = "CS4218";
    private static final String NUMBER_FORMAT = "\t%7d";
    private static final String STRING_NEWLINE = System.lineSeparator();
    private static final String ECHO_CMD = "echo";
    private static final String LS_CMD = "ls";
    private static final String WC_CMD = "wc";
    private static final String CAT_CMD = "cat";
    private static final String GREP_CMD = "grep";
    private static final String CUT_CMD = "cut";
    private static final String SORT_CMD = "sort";
    private static final String CP_CMD = "cp";
    private static final String RM_CMD = "rm";
    private static final String TEE_CMD = "tee";
    private static final String EXIT_CMD = "exit";
    private static final String CD_CMD = "cd";
    private static final String MV_CMD = "mv";
    private static final String UNIQ_CMD = "uniq";
    private static final String PASTE_CMD = "paste";
    private static final String ASTERISK = "*";
    private static final String FILE_ONE_NAME = "test-a-file.txt";
    private static final String FILE_TWO_NAME = "test-b-file.txt";
    private static final String FILE_PREFIX = "test";
    private static final String FILE_SUFFIX = "txt";
    private static final String FILE_INFIX = "file";
    private static final String FOLDER_PREIFX = "subdir";
    private static final String FOLDER_INFIX = "test";
    private static final String FOLDER_SUFFIX = "folder";
    private static final String FILE_ONE_CONTENT = "Hello from CS4218";
    private static final String FILE_TWO_CONTENT = "CS4218 says hello";
    private static final String FOLDER_ONE_NAME = "subdir1test-folder";
    private static final String FOLDER_TWO_NAME = "subdir2-test-folder";
    private static final String OVERWRITTEN_TEXT = "Overwritten..";
    private static final String SUBDIRECTORY_NAME = "glob-test-folder";
    private static final String SUBDIRECTORY_FILE = "glob.txt";
    private static final String INVALID_FIX = "invalid";
    private static final String ERR_CREATE_DIR = "Unable to create directory";
    private static final String RM_DIR_NAME = "rm-folder";
    private static final String CUT_FLAG = "-b";
    private static final String CUT_POS = "1-6";
    private static final String CUT_OUT = "Hello " + STRING_NEWLINE + CS4218_STRING + STRING_NEWLINE;
    private static final String FILE_UNIQ_CONTENT = "CS4218\nCS4218\nHello\nWorld\nhello";
    private static final String FILE_UNIQ_OUT = CS4218_STRING + STRING_NEWLINE + CS4218_STRING + STRING_NEWLINE + "Hello" + STRING_NEWLINE + "World" + STRING_NEWLINE + "hello";
    private static final String UNIQ_OUT = CS4218_STRING + STRING_NEWLINE + "Hello" + STRING_NEWLINE + "World" + STRING_NEWLINE + "hello";
    private static final String PASTE_OUT = FILE_ONE_CONTENT + "\t" + FILE_TWO_CONTENT;
    private static final String LS_EXCEP = "ls: cannot access '%s': %s";
    private static final String CAT_EXCEP = "cat: %s: %s";
    private static final String EXIT_EXCEP = "exit: %s";
    private static final String CD_EXCEP = "cd: %s";
    private static final String WC_EXCEP = "wc: %s: %s";
    private static final String UNIQ_EXCEP = "uniq: %s: %s";
    private static final String UNIQ_EXCEP2 = "uniq: %s'%s'";
    private static final String MV_EXCEP = "mv: %s";
    private static final String CP_EXCEP = "cp: %s";
    private static final String TEE_EXCEP = "tee: %s";
    private static final String RM_EXCEP = "rm: %s: %s";
    private static final String CUT_EXCEP = "cut: %s: %s";
    private static final String GREP_EXCEP = "grep: %s: %s";
    private static final String SORT_EXCEP = "sort: %s: %s";
    private static final String PASTE_EXCEP = "paste: %s: %s";
    @TempDir
    public static Path folderPath;
    private static Path tempPath1;
    private static Path rmFolder;
    private static ByteArrayInputStream inputCapture;
    private static ByteArrayOutputStream outputCapture;
    private static ByteArrayOutputStream testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;

    @BeforeAll
    static void setup() throws Exception {
        testOutputStream = new ByteArrayOutputStream();
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();

        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));

        FileUtils.createNewDirs(folderPath);
        Environment.setCurrentDirectory(folderPath.toString());

        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Path path2 = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(path1, FILE_ONE_CONTENT);
        Files.writeString(path2, FILE_TWO_CONTENT);

        Path folder1 = folderPath.resolve(FOLDER_ONE_NAME);
        Path folder2 = folderPath.resolve(FOLDER_TWO_NAME);
        Path folder3 = folderPath.resolve(SUBDIRECTORY_NAME);
        boolean foldersCreated = folder1.toFile().mkdir() && folder2.toFile().mkdir() && folder3.toFile().mkdir();
        if (!foldersCreated) {
            throw new Exception(ERR_CREATE_DIR);
        }

        Path path3 = folder3.resolve(SUBDIRECTORY_FILE);
        Files.writeString(path3, FILE_TWO_CONTENT);

        rmFolder = folderPath.resolve(RM_DIR_NAME);
    }

    @AfterAll
    static void tearDown() {
        FileUtils.deleteFolder(folderPath);
        Environment.resetCurrentDirectory();
    }

    // Used to create temporary files meant for modification purpose
    private void createTempFiles() throws IOException {
        FileUtils.createNewDirs(rmFolder);
        Environment.setCurrentDirectory(rmFolder.toString());
        tempPath1 = rmFolder.resolve(FILE_ONE_NAME);
        Path path2 = rmFolder.resolve(FILE_TWO_NAME);
        Files.writeString(tempPath1, FILE_ONE_CONTENT);
        Files.writeString(path2, FILE_TWO_CONTENT);
    }

    private void deleteTempFiles() {
        FileUtils.deleteAll(rmFolder.toFile());
        Environment.setCurrentDirectory(folderPath.toString());
    }

    private String createWcOutput(int num1, int num2, int num3, String fileName) {
        return String.format(NUMBER_FORMAT, num1) + String.format(NUMBER_FORMAT, num2) + String.format(NUMBER_FORMAT, num3) + "\t" + fileName;
    }

    @BeforeEach
    public void start() {
        inputCapture = new ByteArrayInputStream(OVERWRITTEN_TEXT.getBytes());
        System.setIn(inputCapture);
    }

    @AfterEach
    public void reset() throws IOException {
        testOutputStream.reset();
        inputCapture.close();
        outputCapture.reset();
        deleteTempFiles();
    }

    @Test
    public void lsGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE + STRING_NEWLINE + SUBDIRECTORY_NAME + ":" + STRING_NEWLINE + SUBDIRECTORY_FILE + STRING_NEWLINE + STRING_NEWLINE + FOLDER_ONE_NAME + ":" + STRING_NEWLINE + STRING_NEWLINE + FOLDER_TWO_NAME + ":" + STRING_NEWLINE, testOutputStream.toString());
    }

    // LS + File Prefix, Infix & Suffix
    @Test
    public void lsGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void lsGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void lsGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void lsGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    // LS + Folder Prefix, Infix and Suffix
    @Test
    public void lsGlobIntegration_folderPrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, FOLDER_PREIFX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FOLDER_ONE_NAME + ":" + STRING_NEWLINE + STRING_NEWLINE + FOLDER_TWO_NAME + ":" + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void lsGlobIntegration_asteriskAndFolderSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, ASTERISK + FOLDER_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SUBDIRECTORY_NAME + ":" + STRING_NEWLINE + SUBDIRECTORY_FILE + STRING_NEWLINE + STRING_NEWLINE + FOLDER_ONE_NAME + ":" + STRING_NEWLINE + STRING_NEWLINE + FOLDER_TWO_NAME + ":" + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void lsGlobIntegration_asteriskBetweenFolderPrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, FOLDER_PREIFX + ASTERISK + FOLDER_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FOLDER_ONE_NAME + ":" + STRING_NEWLINE + STRING_NEWLINE + FOLDER_TWO_NAME + ":" + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void lsGlobIntegration_folderInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{LS_CMD, ASTERISK + FOLDER_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_TWO_NAME + STRING_NEWLINE + STRING_NEWLINE + SUBDIRECTORY_NAME + ":" + STRING_NEWLINE + SUBDIRECTORY_FILE + STRING_NEWLINE + STRING_NEWLINE + FOLDER_ONE_NAME + ":" + STRING_NEWLINE + STRING_NEWLINE + FOLDER_TWO_NAME + ":" + STRING_NEWLINE, testOutputStream.toString());
    }

    // LS + Glob Negative TCs
    // Disabled on Windows as * is an illegal character
    @DisabledOnOs(WINDOWS)
    @Test
    public void lsGlobIntegration_invalidFix_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{LS_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(LS_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND), outputCapture.toString());
    }

    // Cat
    @Test
    public void catGlobIntegration_asteriskOnly_shouldThrowCatException() {
        String[] args = new String[]{CAT_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, SUBDIRECTORY_NAME, ERR_IS_DIR), thrown.getMessage());
    }

    // Cat + File Prefix, Infix and Suffix
    @Test
    public void catGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{CAT_CMD, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void catGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{CAT_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void catGlobIntegration_asteriskBetweenExpression_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{CAT_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void catGlobIntegration_expressionBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{CAT_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // Cat Negative TCs
    @DisabledOnOs(WINDOWS)
    @Test
    public void catGlobIntegration_invalidFix_shouldThrowCatException() {
        String[] args = new String[]{CAT_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // Cat + Folder Prefix => Error as Cat does not accept folders
    @Test
    public void catGlobIntegration_folder_shouldThrowCatException() {
        String[] args = new String[]{CAT_CMD, FOLDER_PREIFX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FOLDER_ONE_NAME, ERR_IS_DIR), thrown.getMessage());
    }

    // Echo
    @Test
    public void echoGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {SUBDIRECTORY_NAME, FOLDER_ONE_NAME, FOLDER_TWO_NAME, FILE_ONE_NAME, FILE_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    // echo + File Prefix, Infix & Suffix
    @Test
    public void echoGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME, FILE_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void echoGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME, FILE_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void echoGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME, FILE_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void echoGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME, FILE_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void echoGlobIntegration_folderPrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, FOLDER_PREIFX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FOLDER_ONE_NAME, FOLDER_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void echoGlobIntegration_asteriskAndFolderSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, ASTERISK + FOLDER_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {SUBDIRECTORY_NAME, FOLDER_ONE_NAME, FOLDER_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void echoGlobIntegration_asteriskBetweenFolderPrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, FOLDER_PREIFX + ASTERISK + FOLDER_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FOLDER_ONE_NAME, FOLDER_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void echoGlobIntegration_folderInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{ECHO_CMD, ASTERISK + FOLDER_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {SUBDIRECTORY_NAME, FOLDER_ONE_NAME, FOLDER_TWO_NAME, FILE_ONE_NAME, FILE_TWO_NAME};
        assertEquals(String.join(" ", expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    // Negative TC
    @DisabledOnOs(WINDOWS)
    @Test
    public void echoGlobIntegration_invalidFix_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{ECHO_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(INVALID_FIX + ASTERISK + STRING_NEWLINE, testOutputStream.toString());
    }

    // Sort
    @Test
    public void sortGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{SORT_CMD, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_TWO_CONTENT, FILE_ONE_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void sortGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{SORT_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_TWO_CONTENT, FILE_ONE_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void sortGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{SORT_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_TWO_CONTENT, FILE_ONE_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void sortGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{SORT_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_TWO_CONTENT, FILE_ONE_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    // Negative Test Case
    @DisabledOnOs(WINDOWS)
    @Test
    public void sortGlobIntegration_invalidFix_shouldThrowSortException() {
        String[] args = new String[]{SORT_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // Unable to sort folder, throws Exception
    @Test
    public void sortGlobIntegration_folder_shouldThrowSortException() {
        String[] args = new String[]{SORT_CMD, FOLDER_PREIFX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FOLDER_ONE_NAME, ErrorConstants.ERR_IS_DIR), thrown.getMessage());
    }

    // Grep
    @Test
    public void grepGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{GREP_CMD, CS4218_STRING, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME + ": " + FILE_ONE_CONTENT, FILE_TWO_NAME + ": " + FILE_TWO_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void grepGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{GREP_CMD, CS4218_STRING, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME + ": " + FILE_ONE_CONTENT, FILE_TWO_NAME + ": " + FILE_TWO_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void grepGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{GREP_CMD, CS4218_STRING, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME + ": " + FILE_ONE_CONTENT, FILE_TWO_NAME + ": " + FILE_TWO_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void grepGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{GREP_CMD, CS4218_STRING, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME + ": " + FILE_ONE_CONTENT, FILE_TWO_NAME + ": " + FILE_TWO_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void grepGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{GREP_CMD, CS4218_STRING, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String[] expectedResults = {FILE_ONE_NAME + ": " + FILE_ONE_CONTENT, FILE_TWO_NAME + ": " + FILE_TWO_CONTENT};
        assertEquals(String.join(STRING_NEWLINE, expectedResults) + STRING_NEWLINE, testOutputStream.toString());
    }

    // Negative Test Case
    @DisabledOnOs(WINDOWS)
    @Test
    public void grepGlobIntegration_invalidFix_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{GREP_CMD, CS4218_STRING, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(GREP_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
    }

    @Test
    public void grepGlobIntegration_folder_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{GREP_CMD, CS4218_STRING, FOLDER_PREIFX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(GREP_EXCEP, FOLDER_PREIFX, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
    }

    // Cut
    @Test
    public void cutGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    @Test
    public void cutGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    @Test
    public void cutGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    @Test
    public void cutGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    @Test
    public void cutGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(CUT_OUT, testOutputStream.toString());
    }

    // Negative TCs
    @DisabledOnOs(WINDOWS)
    @Test
    public void cutGlobIntegration_invalidFix_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(CUT_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
    }

    @Test
    public void cutGlobIntegration_folder_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, FOLDER_PREIFX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(CUT_EXCEP, FOLDER_PREIFX, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
    }

    // RM
    @Test
    public void rmGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{RM_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(rmFolder.resolve(FILE_ONE_NAME).toFile().exists());
        assertFalse(rmFolder.resolve(FILE_TWO_NAME).toFile().exists());
    }

    @Test
    public void rmGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{RM_CMD, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(rmFolder.resolve(FILE_ONE_NAME).toFile().exists());
        assertFalse(rmFolder.resolve(FILE_TWO_NAME).toFile().exists());
    }

    @Test
    public void rmGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{RM_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(rmFolder.resolve(FILE_ONE_NAME).toFile().exists());
        assertFalse(rmFolder.resolve(FILE_TWO_NAME).toFile().exists());
    }

    @Test
    public void rmGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{RM_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(rmFolder.resolve(FILE_ONE_NAME).toFile().exists());
        assertFalse(rmFolder.resolve(FILE_TWO_NAME).toFile().exists());
    }

    @Test
    public void rmGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{RM_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(rmFolder.resolve(FILE_ONE_NAME).toFile().exists());
        assertFalse(rmFolder.resolve(FILE_TWO_NAME).toFile().exists());
    }

    // Negative TCs
    @DisabledOnOs(WINDOWS)
    @Test
    public void rmGlobIntegration_invalidFix_shouldThrowRmException() {
        String[] args = new String[]{RM_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // TEE
    @Test
    public void teeGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{TEE_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void teeGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{TEE_CMD, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void teeGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{TEE_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void teeGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{TEE_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void teeGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{TEE_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(OVERWRITTEN_TEXT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    // Negative TCs
    @Test
    public void teeGlobIntegration_containsDirectory_shouldThrowTeeException() {
        String[] args = new String[]{TEE_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(TeeException.class, () -> callCommand.evaluate(System.in, testOutputStream));
    }

    // CP
    @Test
    public void cpGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CP_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void cpGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CP_CMD, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void cpGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CP_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void cpGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CP_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void cpGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{CP_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    // Negative TCs
    @DisabledOnOs(WINDOWS)
    @Test
    public void cpGlobIntegration_invalidFix_shouldThrowCpException() {
        String[] args = new String[]{CP_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP, ERR_MISSING_ARG), thrown.getMessage());
    }

    @Test
    public void cpGlobIntegration_isNotDirectory_shouldThrowCpException() {
        String[] args = new String[]{CP_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP, ERR_IS_NOT_DIR), thrown.getMessage());
    }

    // MV
    @Test
    public void mvGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{MV_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void mvGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{MV_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void mvGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{MV_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void mvGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        String[] args = new String[]{MV_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(FILE_ONE_CONTENT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    // Negative TCs
    @DisabledOnOs(WINDOWS)
    @Test
    public void mvGlobIntegration_invalidFix_shouldThrowMvException() {
        String[] args = new String[]{MV_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(MvException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(MV_EXCEP, ERR_NO_ARGS), thrown.getMessage());
    }

    // Uniq
    @Test
    public void uniqGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        Files.writeString(tempPath1, FILE_UNIQ_CONTENT);
        String[] args = new String[]{UNIQ_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_UNIQ_OUT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(UNIQ_OUT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void uniqGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        Files.writeString(tempPath1, FILE_UNIQ_CONTENT);
        String[] args = new String[]{UNIQ_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_UNIQ_OUT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(UNIQ_OUT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void uniqGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        Files.writeString(tempPath1, FILE_UNIQ_CONTENT);
        String[] args = new String[]{UNIQ_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_UNIQ_OUT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(UNIQ_OUT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    @Test
    public void uniqGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        createTempFiles();
        Files.writeString(tempPath1, FILE_UNIQ_CONTENT);
        String[] args = new String[]{UNIQ_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_UNIQ_OUT, FileUtils.getFileContent(rmFolder.resolve(FILE_ONE_NAME)));
        assertEquals(UNIQ_OUT, FileUtils.getFileContent(rmFolder.resolve(FILE_TWO_NAME)));
    }

    // Negative TCs
    @Test
    public void uniqGlobIntegration_extraOperand_shouldThrowUniqException() {
        String[] args = new String[]{UNIQ_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP2, ERR_EXTRA_FILE, FOLDER_TWO_NAME), thrown.getMessage());
    }

    @DisabledOnOs(WINDOWS)
    @Test
    public void uniqGlobIntegration_invalidFix_shouldThrowUniqException() {
        String[] args = new String[]{UNIQ_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // WC
    @Test
    public void wcGlobIntegration_asteriskOnly_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{WC_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(createWcOutput(0, 0, 0, SUBDIRECTORY_NAME) + STRING_NEWLINE + createWcOutput(0, 0, 0, FOLDER_ONE_NAME) + STRING_NEWLINE + createWcOutput(0, 0, 0, FOLDER_TWO_NAME) + STRING_NEWLINE + createWcOutput(0, 3, 17, FILE_ONE_NAME) + STRING_NEWLINE + createWcOutput(0, 3, 17, FILE_TWO_NAME) + STRING_NEWLINE + createWcOutput(0, 6, 34, TOTAL_STRING) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void wcGlobIntegration_filePrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{WC_CMD, FILE_PREFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(createWcOutput(0, 3, 17, FILE_ONE_NAME) + STRING_NEWLINE + createWcOutput(0, 3, 17, FILE_TWO_NAME) + STRING_NEWLINE + createWcOutput(0, 6, 34, TOTAL_STRING) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void wcGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{WC_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(createWcOutput(0, 3, 17, FILE_ONE_NAME) + STRING_NEWLINE + createWcOutput(0, 3, 17, FILE_TWO_NAME) + STRING_NEWLINE + createWcOutput(0, 6, 34, TOTAL_STRING) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void wcGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{WC_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(createWcOutput(0, 3, 17, FILE_ONE_NAME) + STRING_NEWLINE + createWcOutput(0, 3, 17, FILE_TWO_NAME) + STRING_NEWLINE + createWcOutput(0, 6, 34, TOTAL_STRING) + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    public void wcGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{WC_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(createWcOutput(0, 3, 17, FILE_ONE_NAME) + STRING_NEWLINE + createWcOutput(0, 3, 17, FILE_TWO_NAME) + STRING_NEWLINE + createWcOutput(0, 6, 34, TOTAL_STRING) + STRING_NEWLINE, testOutputStream.toString());
    }

    // Single: As the output will always be 0
    @Test
    public void wcGlobIntegration_folderPrefixAndAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{WC_CMD, FOLDER_PREIFX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(createWcOutput(0, 0, 0, FOLDER_ONE_NAME) + STRING_NEWLINE + createWcOutput(0, 0, 0, FOLDER_TWO_NAME) + STRING_NEWLINE + createWcOutput(0, 0, 0, TOTAL_STRING) + STRING_NEWLINE, testOutputStream.toString());
    }

    // Negative TCs: Invalid Prefix
    @DisabledOnOs(WINDOWS)
    @Test
    public void wcGlobIntegration_invalidFix_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{WC_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
    }

    // Paste
    @Disabled
    @Test
    public void pasteGlobIntegration_asteriskAndFileSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{PASTE_CMD, ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    @Disabled
    @Test
    public void pasteGlobIntegration_asteriskBetweenFilePrefixAndSuffix_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{PASTE_CMD, FILE_PREFIX + ASTERISK + FILE_SUFFIX};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    @Disabled
    @Test
    public void pasteGlobIntegration_fileInfixBetweenAsterisk_shouldEvaluateSuccessfully() throws Exception {
        String[] args = new String[]{PASTE_CMD, ASTERISK + FILE_INFIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(PASTE_OUT, testOutputStream.toString());
    }

    // Negative
    @Disabled
    @Test
    public void pasteGlobIntegration_folder_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{PASTE_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(PASTE_EXCEP, FOLDER_ONE_NAME, ERR_FILE_NOT_FOUND), outputCapture.toString());
    }

    @Disabled
    @DisabledOnOs(WINDOWS)
    @Test
    public void pasteGlobIntegration_invalidFix_shouldNotEvaluate() throws Exception {
        String[] args = new String[]{PASTE_CMD, INVALID_FIX + ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(PASTE_EXCEP, INVALID_FIX + ASTERISK, ERR_FILE_NOT_FOUND), outputCapture.toString());
    }

    // Single: Testing with incompatible Application
    // Exit + Globbing: Args after "exit" after ignored.
    @Test
    public void exitGlobIntegration_asteriskOnly_shouldThrowExitException() {
        String[] args = new String[]{EXIT_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(EXIT_EXCEP, EXIT_MESSAGE), thrown.getMessage());
    }

    // CD + Globbing: Invalid as CD only accepts single arg (directory).
    @Test
    public void cdGlobIntegration_asteriskOnly_shouldThrowCdException() {
        String[] args = new String[]{CD_CMD, ASTERISK};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CdException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CD_EXCEP, ERR_TOO_MANY_ARGS), thrown.getMessage());
    }
}
